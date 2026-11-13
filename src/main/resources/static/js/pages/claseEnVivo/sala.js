/** Sala de la clase en vivo: reproductor, estado de la señal y chat, tanto para el alumno como para el docente. */
(function () {
    'use strict';

    var raiz = document.querySelector('[data-sala]');
    if (!raiz) { return; }

    var idClase = raiz.dataset.clase;
    var esDocente = raiz.dataset.rol === 'docente';
    var base = '/claseEnVivo/';
    var video = raiz.querySelector('[data-video]');
    var aviso = raiz.querySelector('[data-video-aviso]');
    var etiquetaVivo = raiz.querySelector('[data-etiqueta-vivo]');
    var terminada = false;
    var finalizando = false;

    // ----------------------------------------------------------------------
    // Reproductor
    // ----------------------------------------------------------------------

    var reproductor = null;
    var reintento = null;

    function mostrarAviso(texto) {
        aviso.textContent = texto || '';
        aviso.hidden = !texto;
    }

    function conectar() {
        if (terminada || !window.Hls || !Hls.isSupported()) {
            if (!window.Hls || !Hls.isSupported()) { mostrarAviso('Tu navegador no puede reproducir la transmisión.'); }
            return;
        }
        var token = encodeURIComponent(raiz.dataset.token);
        reproductor = new Hls({
            lowLatencyMode: true,
            // Cada pedido lleva el acceso firmado: el servidor de medios lo verifica contra la aplicación
            xhrSetup: function (xhr, url) {
                xhr.open('GET', url + (url.indexOf('?') >= 0 ? '&' : '?') + 'token=' + token, true);
            }
        });
        reproductor.on(Hls.Events.MANIFEST_PARSED, function () {
            mostrarAviso('');
            etiquetaVivo.hidden = false;
            video.play().catch(function () { /* el navegador pide un toque para reproducir */ });
        });
        reproductor.on(Hls.Events.ERROR, function (evento, datos) {
            if (!datos.fatal) { return; }
            // Se cortó la señal: se espera a que la consulta de estado avise que volvió, sin pedir la lista a ciegas
            reproductor.destroy();
            reproductor = null;
            etiquetaVivo.hidden = true;
            mostrarAviso(esDocente ? 'Esperando la señal de OBS...' : 'Esperando la señal del docente...');
        });
        reproductor.loadSource(raiz.dataset.url);
        reproductor.attachMedia(video);
    }

    // ----------------------------------------------------------------------
    // Estado de la clase y de la señal
    // ----------------------------------------------------------------------

    var senal = raiz.querySelector('[data-senal]');
    var conectados = raiz.querySelector('[data-conectados]');
    var volver = raiz.querySelector('[data-volver]');

    function terminar() {
        terminada = true;
        clearTimeout(reintento);
        if (reproductor) { reproductor.destroy(); }
        etiquetaVivo.hidden = true;
        // El alumno se entera apenas el docente finaliza, sin esperar a que se procese el video
        mostrarAviso('La clase terminó. La grabación va a estar disponible en el tema ' + (raiz.dataset.tema || 'de la clase') + ' cuando termine de procesarse.');
        if (volver) { volver.hidden = false; }
        var campo = raiz.querySelector('[data-chat-formulario] input');
        if (campo) { campo.disabled = true; }
        if (fuente) { fuente.close(); }
    }

    function consultarEstado() {
        if (terminada) { return; }
        fetch(base + 'estado/' + idClase, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (r) { return r.ok ? r.json() : null; })
            .then(function (estado) {
                if (!estado) { return; }
                if (!estado.enVivo) {
                    // Terminó la clase: el alumno ve el aviso y el docente vuelve al listado
                    if (esDocente) { if (!finalizando) { window.location.href = raiz.dataset.urlclases; } } else { terminar(); }
                    return;
                }
                if (senal) {
                    senal.textContent = estado.transmitiendo ? 'Señal estable' : 'Sin señal';
                    senal.classList.toggle('pn-insignia-verde', !!estado.transmitiendo);
                    senal.classList.toggle('pn-insignia-gris', !estado.transmitiendo);
                }
                // Solo se pide el video cuando el servidor ya recibe la señal: así no se piden listas que todavía no existen
                if (estado.transmitiendo && !reproductor) { conectar(); }
                if (conectados) {
                    conectados.textContent = estado.conectados;
                }
            })
            .catch(function () { /* se vuelve a preguntar en el próximo ciclo */ });
    }

    // ----------------------------------------------------------------------
    // Chat
    // ----------------------------------------------------------------------

    var chat = raiz.querySelector('[data-chat]');
    var lista = chat.querySelector('[data-chat-lista]');
    var vacio = chat.querySelector('[data-chat-vacio]');
    var formulario = chat.querySelector('[data-chat-formulario]');
    var errorChat = chat.querySelector('[data-chat-error]');
    var interruptor = chat.querySelector('[data-chat-habilitado]');
    var fuente = null;

    function dibujar(mensaje) {
        var fila = document.createElement('div');
        fila.className = 'sc-mensaje' + (mensaje.docente ? ' sc-mensaje-docente' : '');
        fila.dataset.id = mensaje.id;
        // Foto de perfil, o las iniciales si no tiene
        var avatar = document.createElement('span');
        avatar.className = 'sc-avatar';
        if (mensaje.imagen) {
            var foto = document.createElement('img');
            foto.src = mensaje.imagen;
            foto.alt = '';
            avatar.appendChild(foto);
        } else {
            avatar.textContent = mensaje.iniciales || '?';
        }
        fila.appendChild(avatar);
        var cuerpo = document.createElement('div');
        cuerpo.className = 'sc-mensaje-cuerpo';
        var autor = document.createElement('b');
        autor.textContent = mensaje.autor + (mensaje.docente ? ' (docente)' : '');
        var hora = document.createElement('small');
        hora.textContent = mensaje.hora;
        cuerpo.appendChild(autor);
        cuerpo.appendChild(hora);
        cuerpo.appendChild(document.createElement('br'));
        // El texto se inserta como texto, nunca como HTML
        cuerpo.appendChild(document.createTextNode(mensaje.texto));
        fila.appendChild(cuerpo);
        if (esDocente) {
            var borrar = document.createElement('button');
            borrar.type = 'button';
            borrar.className = 'sc-mensaje-borrar';
            borrar.title = 'Eliminar mensaje';
            borrar.setAttribute('aria-label', 'Eliminar mensaje');
            borrar.innerHTML = '<i class="fa-regular fa-trash-can"></i>';
            borrar.addEventListener('click', function () {
                fetch(base + 'chat/' + idClase + '/eliminar/' + mensaje.id, { method: 'POST', credentials: 'same-origin' });
            });
            fila.appendChild(borrar);
        }
        var alFinal = lista.scrollTop + lista.clientHeight >= lista.scrollHeight - 40;
        if (vacio) { vacio.hidden = true; }
        lista.appendChild(fila);
        if (alFinal) { lista.scrollTop = lista.scrollHeight; }
    }

    function habilitarEscritura(habilitado) {
        var campo = formulario.querySelector('input');
        campo.disabled = !habilitado || terminada;
        campo.placeholder = habilitado ? 'Escribí un mensaje' : 'El docente deshabilitó el chat';
        if (interruptor) { interruptor.checked = habilitado; }
    }

    function conectarChat() {
        fetch(base + 'chat/' + idClase + '/mensajes', { credentials: 'same-origin' })
            .then(function (r) { return r.ok ? r.json() : []; })
            .then(function (mensajes) { mensajes.forEach(dibujar); })
            .catch(function () { /* el chat arranca vacío */ });
        fuente = new EventSource(base + 'chat/' + idClase + '/eventos');
        fuente.addEventListener('mensaje', function (e) { dibujar(JSON.parse(e.data)); });
        fuente.addEventListener('eliminado', function (e) {
            var fila = lista.querySelector('[data-id="' + JSON.parse(e.data).id + '"]');
            if (fila) { fila.remove(); }
        });
        fuente.addEventListener('habilitado', function (e) { habilitarEscritura(JSON.parse(e.data).habilitado); });
        fuente.addEventListener('cerrada', function () { if (!esDocente) { terminar(); } if (fuente) { fuente.close(); } });
    }

    formulario.addEventListener('submit', function (e) {
        e.preventDefault();
        var campo = formulario.querySelector('input');
        var texto = campo.value.trim();
        if (!texto) { return; }
        errorChat.textContent = '';
        var datos = new URLSearchParams();
        datos.append('texto', texto);
        fetch(base + 'chat/' + idClase + '/mensajes', { method: 'POST', body: datos, credentials: 'same-origin',
            headers: { 'Accept': 'application/json' } })
            .then(function (r) { return r.json().then(function (cuerpo) { return { ok: r.ok, cuerpo: cuerpo }; }); })
            .then(function (res) {
                if (res.ok) { campo.value = ''; } else { errorChat.textContent = res.cuerpo.error || 'No se pudo enviar el mensaje.'; }
            })
            .catch(function () { errorChat.textContent = 'No se pudo enviar el mensaje.'; });
    });

    if (interruptor) {
        interruptor.addEventListener('change', function () {
            var datos = new URLSearchParams();
            datos.append('valor', interruptor.checked);
            fetch(base + 'chat/' + idClase + '/habilitado', { method: 'POST', body: datos, credentials: 'same-origin' });
        });
    }

    // ----------------------------------------------------------------------
    // Datos de conexión del docente (están fuera de la sala, en su propia tarjeta)
    // ----------------------------------------------------------------------

    document.querySelectorAll('[data-copiar]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(boton.dataset.copiar).then(function () {
                    var texto = boton.textContent;
                    boton.textContent = 'Copiado';
                    setTimeout(function () { boton.textContent = texto; }, 1500);
                });
            }
        });
    });

    var mostrar = document.querySelector('[data-mostrar-clave]');
    if (mostrar) {
        mostrar.addEventListener('click', function () {
            var texto = document.querySelector('[data-clave-texto]');
            var oculta = document.querySelector('[data-clave-oculta]');
            var visible = !texto.hidden;
            texto.hidden = visible;
            oculta.hidden = !visible;
            mostrar.textContent = visible ? 'Mostrar' : 'Ocultar';
        });
    }

    // Barra del estudio: ocultar el chat, cajón de datos de conexión y finalizar
    var grilla = raiz.querySelector('[data-grilla]');
    var alternarChat = raiz.querySelector('[data-alternar-chat]');
    if (alternarChat) {
        alternarChat.addEventListener('click', function () {
            var oculto = grilla.classList.toggle('sc-sin-chat');
            alternarChat.querySelector('span').textContent = oculto ? 'Mostrar chat' : 'Ocultar chat';
        });
    }
    var cajon = raiz.querySelector('[data-cajon]');
    if (cajon) {
        raiz.querySelectorAll('[data-abrir-cajon]').forEach(function (b) {
            b.addEventListener('click', function () { cajon.classList.add('abierto'); });
        });
        cajon.querySelector('[data-cerrar-cajon]').addEventListener('click', function () { cajon.classList.remove('abierto'); });
    }
    // Al finalizar, el docente vuelve al listado recién cuando cierra la alerta de éxito
    var formularioFinalizar = document.querySelector('#modalFinalizarClase form');
    if (formularioFinalizar) {
        formularioFinalizar.addEventListener('submit', function () { finalizando = true; });
    }

    habilitarEscritura(raiz.querySelector('[data-chat-habilitado]') ? raiz.querySelector('[data-chat-habilitado]').checked : true);
    conectarChat();
    consultarEstado();
    setInterval(consultarEstado, 5000);
})();
