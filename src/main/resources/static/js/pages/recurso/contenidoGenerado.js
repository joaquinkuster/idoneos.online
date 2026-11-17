/**
 * Edición del contenido de un material generado con IA directamente sobre el documento, en el visor: el docente escribe sobre el título, los títulos de
 * sección y los textos. Nada se guarda hasta tocar «Guardar cambios» (o pasar a Visualización), que guarda y regenera el archivo (PDF).
 * Cada sección tiene un nivel de 1 a 4 (como el índice): el botón de sangría la pasa al nivel siguiente.
 */
(function () {
    'use strict';

    var NIVEL_MAXIMO = 4;
    var panel = document.querySelector('[data-contenido]');
    var botonGuardar = document.querySelector('[data-contenido-guardar]');
    var botonVisualizar = document.querySelector('[data-contenido-visualizar]');
    var url = panel ? panel.getAttribute('data-url') : null;
    var contenedor = panel ? panel.querySelector('[data-contenido-secciones]') : null;
    var error = panel ? panel.querySelector('[data-contenido-error]') : null;
    var tituloDocumento = panel ? panel.querySelector('[data-titulo-documento]') : null;

    function mostrarError(texto) { if (error) { error.textContent = texto || ''; } }
    function limpio(elemento) { return (elemento.innerText || '').replace(/\s+/g, ' ').trim(); }

    function leerContenido() {
        return {
            titulo: tituloDocumento ? limpio(tituloDocumento) : '',
            secciones: Array.prototype.map.call(contenedor.querySelectorAll('[data-seccion]'), function (caja) {
                return {
                    titulo: limpio(caja.querySelector('[data-titulo]')),
                    nivel: Number(caja.getAttribute('data-nivel')) || 1,
                    textos: Array.prototype.map.call(caja.querySelectorAll('[data-texto]'), limpio)
                };
            })
        };
    }

    /** Guarda el contenido tal como está. */
    function guardar() {
        var datos = new FormData();
        datos.append('contenido', JSON.stringify(leerContenido()));
        return fetch(url, { method: 'POST', body: datos, credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (r) { return r.json().catch(function () { return {}; }); })
            .then(function (respuesta) {
                if (respuesta.error) { mostrarError(respuesta.error); return false; }
                mostrarError('');
                return true;
            })
            .catch(function () { mostrarError('No se pudo guardar el contenido.'); return false; });
    }

    /** Guarda lo editado, regenera el archivo y, si salió bien, sigue con lo que se pida. */
    function actualizarArchivo(luego, boton) {
        if (boton) { boton.disabled = true; }
        mostrarError('');
        return guardar().then(function (ok) {
            if (!ok) { if (boton) { boton.disabled = false; } return null; }
            return fetch(url + '/archivo', { method: 'POST', credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
                .then(function (r) { return r.json().catch(function () { return {}; }); })
                .then(function (respuesta) {
                    if (respuesta.error) { mostrarError(respuesta.error); if (boton) { boton.disabled = false; } return; }
                    luego();
                });
        }).catch(function () { mostrarError('No se pudo actualizar el archivo.'); if (boton) { boton.disabled = false; } });
    }

    /** Un texto o un título que se escribe sobre el propio documento. */
    function editable(etiqueta, clase, atributo, marcador, valor) {
        var e = document.createElement(etiqueta);
        e.className = clase;
        e.contentEditable = 'true';
        e.spellcheck = true;
        e.setAttribute(atributo, '');
        e.setAttribute('data-ph', marcador);
        e.textContent = valor || '';
        // Se pega siempre texto plano: el documento no lleva formato
        e.addEventListener('paste', function (ev) {
            ev.preventDefault();
            var texto = (ev.clipboardData || window.clipboardData).getData('text').replace(/\s+/g, ' ');
            document.execCommand('insertText', false, texto);
        });
        return e;
    }

    function boton(clase, icono, titulo, alClick) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = clase;
        b.title = titulo;
        b.setAttribute('aria-label', titulo);
        b.innerHTML = '<i class="fa-solid ' + icono + '"></i>';
        b.addEventListener('click', alClick);
        return b;
    }

    function crearTexto(valor) {
        var fila = document.createElement('div');
        fila.className = 'ce-fila';
        var texto = editable('p', 'ce-texto', 'data-texto', 'Escribí el texto', valor);
        // Enter abre un texto nuevo debajo, como un párrafo nuevo del documento
        texto.addEventListener('keydown', function (ev) {
            if (ev.key === 'Enter' && !ev.shiftKey) {
                ev.preventDefault();
                var nueva = crearTexto('');
                fila.parentNode.insertBefore(nueva, fila.nextSibling);
                nueva.querySelector('[data-texto]').focus();
            }
        });
        fila.appendChild(texto);
        fila.appendChild(boton('ce-quitar', 'fa-trash', 'Quitar texto', function () { fila.remove(); }));
        return fila;
    }

    function crearSeccion(seccion) {
        var caja = document.createElement('div');
        caja.className = 'ce-seccion';
        caja.setAttribute('data-seccion', '');
        var nivel = Math.max(1, Math.min(NIVEL_MAXIMO, Number(seccion.nivel) || 1));
        caja.setAttribute('data-nivel', String(nivel));
        var fila = document.createElement('div');
        fila.className = 'ce-fila';
        var titulo = editable('h3', 'ce-subtitulo', 'data-titulo', 'Título de la sección', seccion.titulo);
        titulo.setAttribute('data-nivel', String(nivel));
        titulo.addEventListener('keydown', function (ev) {
            if (ev.key === 'Enter') {
                ev.preventDefault();
                var primero = caja.querySelector('[data-texto]');
                if (primero) { primero.focus(); }
            }
        });
        fila.appendChild(titulo);
        // Pasa la sección al nivel siguiente (del 4 vuelve al 1), con el mismo ícono que el armado del índice
        var nivelBoton = boton('ce-nivel', 'fa-indent', 'Nivel ' + nivel + ': pasar al siguiente', function () {
            var nuevo = (Number(caja.getAttribute('data-nivel')) % NIVEL_MAXIMO) + 1;
            caja.setAttribute('data-nivel', String(nuevo));
            titulo.setAttribute('data-nivel', String(nuevo));
            nivelBoton.title = 'Nivel ' + nuevo + ': pasar al siguiente';
        });
        fila.appendChild(nivelBoton);
        fila.appendChild(boton('ce-quitar', 'fa-trash', 'Quitar sección', function () { caja.remove(); }));
        var textos = document.createElement('div');
        (seccion.textos || []).forEach(function (t) { textos.appendChild(crearTexto(t)); });
        var agregar = document.createElement('button');
        agregar.type = 'button';
        agregar.className = 'ce-agregar';
        agregar.innerHTML = '<i class="fa-solid fa-plus"></i> Agregar texto';
        agregar.addEventListener('click', function () {
            var nuevo = crearTexto('');
            textos.appendChild(nuevo);
            nuevo.querySelector('[data-texto]').focus();
        });
        caja.appendChild(fila);
        caja.appendChild(textos);
        caja.appendChild(agregar);
        return caja;
    }

    if (panel) {
        if (tituloDocumento) {
            tituloDocumento.addEventListener('keydown', function (ev) { if (ev.key === 'Enter') { ev.preventDefault(); } });
            tituloDocumento.addEventListener('paste', function (ev) {
                ev.preventDefault();
                document.execCommand('insertText', false, (ev.clipboardData || window.clipboardData).getData('text').replace(/\s+/g, ' '));
            });
        }
        contenedor.textContent = 'Cargando contenido...';
        fetch(url, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
            .then(function (r) { if (!r.ok) { throw new Error(); } return r.json(); })
            .then(function (contenido) {
                contenedor.textContent = '';
                if (tituloDocumento && contenido.titulo) { tituloDocumento.textContent = contenido.titulo; }
                (contenido.secciones || []).forEach(function (s) { contenedor.appendChild(crearSeccion(s)); });
            })
            .catch(function () { contenedor.textContent = ''; mostrarError('No se pudo cargar el contenido.'); });

        panel.querySelector('[data-contenido-agregar-seccion]').addEventListener('click', function () {
            var caja = crearSeccion({ titulo: '', nivel: 1, textos: [''] });
            contenedor.appendChild(caja);
            caja.scrollIntoView({ block: 'center' });
            caja.querySelector('[data-titulo]').focus();
        });
    }

    if (botonGuardar) {
        botonGuardar.addEventListener('click', function () { actualizarArchivo(function () { window.location.reload(); }, botonGuardar); });
    }

    // Pasar a Visualización: el archivo se actualiza con lo editado antes de mostrarlo
    if (botonVisualizar) {
        botonVisualizar.addEventListener('click', function () {
            var destino = botonVisualizar.getAttribute('data-destino');
            if (!panel) { window.location.href = destino; return; }
            actualizarArchivo(function () { window.location.href = destino; }, botonVisualizar);
        });
    }
})();
