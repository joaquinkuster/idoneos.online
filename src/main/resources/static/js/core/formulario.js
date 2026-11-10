/** Envío de formularios por JavaScript (fetch). */
(function () {
    'use strict';

    var MENSAJE_INESPERADO = 'Error! Ocurrió un error inesperado. Intentá nuevamente.';

    function alertaDe(formulario) {
        // Las alertas van debajo del formulario, justo antes de los botones. Sin botones, justo antes del formulario
        var pie = formulario.querySelector(':scope > .pn-modal-pie');
        var alerta = pie ? formulario.querySelector(':scope > .alert-formulario') : formulario.previousElementSibling;
        if (!alerta || !alerta.classList.contains('alert-formulario')) {
            alerta = document.createElement('div');
            alerta.className = 'alert alert-danger alert-formulario py-2 px-3 small mb-3 d-none';
            alerta.setAttribute('role', 'alert');
            if (pie) {
                formulario.insertBefore(alerta, pie);
            } else {
                formulario.parentNode.insertBefore(alerta, formulario);
            }
        }
        return alerta;
    }

    function mostrarError(formulario, texto) {
        var alerta = alertaDe(formulario);
        alerta.textContent = texto;
        alerta.classList.remove('d-none');
        alerta.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
    }

    function ocultarError(formulario) {
        var alerta = alertaDe(formulario);
        alerta.textContent = '';
        alerta.classList.add('d-none');
    }

    document.addEventListener('submit', async function (evento) {
        var formulario = evento.target.closest ? evento.target.closest('form.form-ajax') : null;
        if (!formulario || evento.defaultPrevented) {
            return;
        }
        if (formulario.dataset.confirmarTitulo && !formulario.dataset.confirmado) {
            evento.preventDefault();
            window.mostrarAviso({ tipo: 'advertencia', titulo: formulario.dataset.confirmarTitulo, texto: formulario.dataset.confirmarTexto, html: formulario.dataset.confirmarHtml,
                confirmar: formulario.dataset.confirmarBoton || 'Confirmar', cancelar: 'Cancelar', bloquear: true }).then(function (resultado) {
                if (resultado.isConfirmed) {
                    formulario.dataset.confirmado = '1';
                    formulario.requestSubmit();
                }
            });
            return;
        }
        delete formulario.dataset.confirmado;
        evento.preventDefault();
        ocultarError(formulario);

        var boton = formulario.querySelector('button[type="submit"]');
        if (boton) {
            boton.disabled = true;
        }

        try {
            var respuesta = await fetch(formulario.action, {
                method: 'POST',
                body: new FormData(formulario),
                headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var esJson = (respuesta.headers.get('content-type') || '').indexOf('json') !== -1;
            var datos = esJson ? await respuesta.json() : {};

            if (respuesta.ok && (datos.mensaje || datos.destino)) {
                if (datos.mensaje) {
                    await window.mostrarExito(datos.mensaje);
                }
                // Al final del alta de un recurso: ¿se publica en la biblioteca digital?
                if (datos.publicarId && window.preguntarPublicarRecurso) {
                    await window.preguntarPublicarRecurso(datos.publicarId);
                }
                if (datos.siguienteUrl && window.manejarSiguientePaso && await window.manejarSiguientePaso(datos)) {
                    return;
                }
                if (datos.destino) {
                    window.location.href = datos.destino;
                } else {
                    window.location.reload();
                }
                return;
            }
            var texto = datos.error
                || (respuesta.status === 403 ? 'Error! No tenés permisos para realizar esta operación.' : MENSAJE_INESPERADO);
            // Si el error es de un campo, se marca ese campo (en su paso, si el formulario es por pasos) y no se repite en
            // la alerta de arriba. Si no, se muestra la alerta del formulario
            var marcado = window.PanelValidacion && window.PanelValidacion.irAlError
                && window.PanelValidacion.irAlError(formulario, texto, datos.campo);
            if (!marcado) { mostrarError(formulario, texto); }
        } catch (error) {
            mostrarError(formulario, 'Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        }
        if (boton) {
            boton.disabled = false;
        }
    });
})();
