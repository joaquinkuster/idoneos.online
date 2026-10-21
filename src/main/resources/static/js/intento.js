/**
 * Autoevaluación del alumno (CU-70): el cuestionario, su resumen y la ficha (confirmación de inicio).
 *
 * Temporizador: el tiempo lo manda el servidor. Cada respuesta del servidor trae los segundos que le quedan al intento y el
 * navegador calcula el vencimiento con performance.now() (un reloj monótono: no le afecta que el alumno cambie la hora de la
 * computadora ni los atrasos de las pestañas inactivas). Además se resincroniza al volver a la pestaña y cada minuto. Al
 * llegar a cero se entrega el intento: aunque no lo hiciera, el servidor lo cierra solo, con las respuestas ya guardadas.
 */
(function () {
    'use strict';

    // ------------------------------------------------------------------ Ficha: confirmar el inicio cuando no se puede salir
    document.querySelectorAll('form[data-confirmar-inicio]').forEach(function (formulario) {
        formulario.addEventListener('submit', function (evento) {
            if (formulario.dataset.confirmado || formulario.getAttribute('data-sin-salida') !== 'true') {
                return;
            }
            evento.preventDefault();
            Swal.fire({
                iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
                title: 'Realizar intento',
                text: 'Esta autoevaluación no permite salir: una vez iniciada, no vas a poder acceder a otras secciones '
                    + 'de la plataforma hasta entregar el intento o agotarse el tiempo. ¿Querés comenzar?',
                showCancelButton: true,
                reverseButtons: true,
                confirmButtonText: 'Comenzar',
                cancelButtonText: 'Cancelar',
                customClass: { icon: 'swal-icono-peligro', popup: 'swal-confirmacion', confirmButton: 'swal-boton-peligro', cancelButton: 'swal-boton-cancelar' },
                buttonsStyling: false
            }).then(function (resultado) {
                if (resultado.isConfirmed) {
                    formulario.dataset.confirmado = '1';
                    formulario.submit();
                }
            });
        });
    });

    var raiz = document.getElementById('intento');
    if (!raiz) {
        return;
    }
    var id = raiz.dataset.intento;
    var urlFicha = raiz.dataset.urlFicha;
    var libre = raiz.dataset.libre === 'true';

    // ------------------------------------------------------------------ Utilidades
    function enviar(url, datos, metodo) {
        var opciones = { method: metodo || 'POST', headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' },
            credentials: 'same-origin' };
        if (!metodo || metodo === 'POST') {
            opciones.headers['Content-Type'] = 'application/x-www-form-urlencoded;charset=UTF-8';
            opciones.body = new URLSearchParams(datos || {}).toString();
        }
        return fetch(url, opciones).then(function (respuesta) {
            return respuesta.json().catch(function () { return {}; }).then(function (cuerpo) {
                cuerpo.ok = respuesta.ok;
                return cuerpo;
            });
        });
    }

    function dialogo(titulo, texto, icono) {
        return Swal.fire({ icon: icono || 'info', title: titulo, text: texto, confirmButtonText: 'Aceptar',
            customClass: { confirmButton: 'swal-boton-peligro' }, buttonsStyling: false, allowOutsideClick: false });
    }

    // ------------------------------------------------------------------ Temporizador sincronizado con el servidor
    var reloj = document.querySelector('[data-reloj]');
    var cajaReloj = document.querySelector('[data-reloj-caja]');
    var venceEn = null; // instante (performance.now) en el que se agota el tiempo
    var entregando = false;

    function fijarRestantes(segundos) {
        if (segundos === null || segundos === undefined || isNaN(segundos)) {
            return;
        }
        venceEn = performance.now() + segundos * 1000;
        pintarReloj();
    }

    function formato(milisegundos) {
        var total = Math.max(0, Math.ceil(milisegundos / 1000));
        var h = Math.floor(total / 3600);
        var m = Math.floor((total % 3600) / 60);
        var s = total % 60;
        function dos(n) { return (n < 10 ? '0' : '') + n; }
        return h > 0 ? h + ':' + dos(m) + ':' + dos(s) : dos(m) + ':' + dos(s);
    }

    function pintarReloj() {
        if (venceEn === null || !reloj) {
            return;
        }
        var restante = venceEn - performance.now();
        reloj.textContent = formato(restante);
        cajaReloj.classList.toggle('in-reloj-alerta', restante <= 5 * 60 * 1000 && restante > 60 * 1000);
        cajaReloj.classList.toggle('in-reloj-critico', restante <= 60 * 1000);
        if (restante <= 0 && !entregando) {
            entregar(true);
        }
    }

    function sincronizar() {
        if (entregando) { return; }
        enviar('/intento/tiempo/' + id, null, 'GET').then(function (datos) {
            if (datos.ok && datos.enCurso === false) {
                window.location.href = urlFicha; // ya se cerró (por ejemplo, por tiempo)
            } else if (datos.ok) {
                fijarRestantes(datos.segundosRestantes);
            }
        });
    }

    if (raiz.dataset.segundos !== undefined && reloj) {
        fijarRestantes(parseFloat(raiz.dataset.segundos));
        setInterval(pintarReloj, 500);
        setInterval(sincronizar, 60000);
        document.addEventListener('visibilitychange', function () { if (!document.hidden) { sincronizar(); } });
        window.addEventListener('focus', sincronizar);
        var ocultar = document.querySelector('[data-ocultar-reloj]');
        if (ocultar) {
            ocultar.addEventListener('click', function () {
                var oculto = reloj.classList.toggle('d-none');
                ocultar.textContent = oculto ? 'Mostrar' : 'Ocultar';
            });
        }
    }

    // ------------------------------------------------------------------ Entrega
    function entregar(tiempoAgotado) {
        if (entregando) { return; }
        entregando = true;
        enviar('/intento/entregar/' + id, { tiempoAgotado: tiempoAgotado ? 'true' : 'false' }).then(function (datos) {
            if (!datos.ok) {
                entregando = false;
                dialogo('No se pudo entregar', datos.error || 'Intentá nuevamente.', 'error').then(function () {
                    if (tiempoAgotado) { window.location.href = urlFicha; }
                });
                return;
            }
            dialogo(datos.titulo, datos.mensaje, datos.aprobado ? 'success' : 'info').then(function () {
                window.location.href = datos.destino || urlFicha;
            });
        }).catch(function () {
            entregando = false;
            dialogo('Sin conexión', 'No se pudo conectar con el servidor. Intentá nuevamente.', 'error');
        });
    }

    var botonEntregar = document.querySelector('[data-entregar]');
    if (botonEntregar) {
        botonEntregar.addEventListener('click', function () {
            var sinResponder = parseInt(raiz.dataset.sinResponder || '0', 10);
            Swal.fire({
                iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
                title: 'Entregar el intento',
                text: sinResponder > 0
                    ? 'Quedan ' + sinResponder + ' preguntas sin responder y se contarán como vacías. ¿Querés entregar de todas formas?'
                    : '¿Querés entregar el intento? No vas a poder modificarlo.',
                showCancelButton: true,
                reverseButtons: true,
                confirmButtonText: 'Entregar',
                cancelButtonText: 'Volver',
                customClass: { icon: 'swal-icono-peligro', popup: 'swal-confirmacion', confirmButton: 'swal-boton-peligro', cancelButton: 'swal-boton-cancelar' },
                buttonsStyling: false
            }).then(function (resultado) { if (resultado.isConfirmed) { entregar(false); } });
        });
    }

    // ------------------------------------------------------------------ Cuestionario: guardar respuestas, marcar y avanzar
    var preguntaId = raiz.dataset.pregunta;
    if (!preguntaId) {
        return;
    }
    var opciones = Array.prototype.slice.call(document.querySelectorAll('[data-opcion]'));
    var guardado = document.querySelector('[data-guardado]');
    var estadoPregunta = document.querySelector('[data-estado-pregunta]');
    var botonLimpiar = document.querySelector('[data-limpiar]');
    var multiple = raiz.dataset.multiple === 'true';

    function elegidas() {
        return opciones.filter(function (o) { return o.checked; }).map(function (o) { return o.value; });
    }

    function pintarEstado() {
        var hay = elegidas().length > 0;
        opciones.forEach(function (o) { o.closest('.in-opcion').classList.toggle('in-opcion-elegida', o.checked); });
        estadoPregunta.textContent = hay ? 'Respuesta guardada' : 'Sin responder';
        if (botonLimpiar) { botonLimpiar.classList.toggle('d-none', !hay); }
        var casilla = document.querySelector('.in-nav-casilla.in-nav-actual');
        if (casilla) { casilla.classList.toggle('in-nav-respondida', hay); }
    }

    function guardar() {
        guardado.textContent = 'Guardando...';
        guardado.classList.remove('in-guardado-error');
        var datos = new URLSearchParams();
        datos.append('preguntaIntentoId', preguntaId);
        elegidas().forEach(function (v) { datos.append('opcionIds', v); });
        enviar('/intento/responder/' + id, datos).then(function (respuesta) {
            if (!respuesta.ok) {
                guardado.textContent = '';
                dialogo('No se pudo guardar', respuesta.error || 'Intentá nuevamente.', 'error').then(function () {
                    window.location.href = urlFicha;
                });
                return;
            }
            fijarRestantes(respuesta.segundosRestantes);
            guardado.textContent = 'Respuesta guardada';
        }).catch(function () {
            guardado.textContent = 'Sin conexión: reintentá elegir la opción.';
            guardado.classList.add('in-guardado-error');
        });
    }

    opciones.forEach(function (o) {
        o.addEventListener('change', function () { pintarEstado(); guardar(); });
    });
    if (botonLimpiar) {
        botonLimpiar.addEventListener('click', function () {
            opciones.forEach(function (o) { o.checked = false; });
            pintarEstado();
            guardar();
        });
    }

    // Teclado: a, b, c... eligen la opción y las flechas se mueven entre preguntas (si se puede)
    document.addEventListener('keydown', function (evento) {
        if (evento.ctrlKey || evento.metaKey || evento.altKey) { return; }
        var indice = 'abcdefghij'.indexOf(evento.key.toLowerCase());
        if (evento.key.length === 1 && indice >= 0 && indice < opciones.length) {
            var opcion = opciones[indice];
            opcion.checked = multiple ? !opcion.checked : true;
            opcion.dispatchEvent(new Event('change', { bubbles: true }));
        }
    });

    var botonMarcar = document.querySelector('[data-marcar]');
    if (botonMarcar) {
        botonMarcar.addEventListener('click', function () {
            var marcar = botonMarcar.dataset.marcada !== 'true';
            enviar('/intento/marcar/' + id, { preguntaIntentoId: preguntaId, marcada: marcar ? 'true' : 'false' }).then(function (datos) {
                if (!datos.ok) { dialogo('No se pudo marcar', datos.error || 'Intentá nuevamente.', 'error'); return; }
                fijarRestantes(datos.segundosRestantes);
                botonMarcar.dataset.marcada = marcar ? 'true' : 'false';
                botonMarcar.classList.toggle('in-marcada', marcar);
                botonMarcar.querySelector('[data-marcar-texto]').textContent = marcar ? 'Quitar marca' : 'Marcar para revisar';
                var casilla = document.querySelector('.in-nav-casilla.in-nav-actual');
                if (casilla) { casilla.classList.toggle('in-nav-marcada', marcar); }
            });
        });
    }

    var botonAvanzar = document.querySelector('[data-avanzar]');
    if (botonAvanzar) {
        function avanzar() {
            enviar('/intento/avanzar/' + id).then(function (datos) {
                if (!datos.ok) { dialogo('No se pudo avanzar', datos.error || 'Intentá nuevamente.', 'error'); return; }
                window.location.href = datos.destino;
            });
        }
        botonAvanzar.addEventListener('click', function () {
            if (elegidas().length > 0) { avanzar(); return; }
            Swal.fire({
                iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
                title: 'Pregunta sin responder',
                text: 'Si avanzás sin responder, no vas a poder volver a esta pregunta y se contará como vacía.',
                showCancelButton: true,
                reverseButtons: true,
                confirmButtonText: 'Avanzar',
                cancelButtonText: 'Volver',
                customClass: { icon: 'swal-icono-peligro', popup: 'swal-confirmacion', confirmButton: 'swal-boton-peligro', cancelButton: 'swal-boton-cancelar' },
                buttonsStyling: false
            }).then(function (resultado) { if (resultado.isConfirmed) { avanzar(); } });
        });
    }
})();
