/**
 * Envío de formularios por JavaScript (fetch).
 *
 * Los formularios con la clase "form-ajax" se envían sin recargar la página:
 *  - Si el servidor responde con éxito, se muestra un SweetAlert y, al aceptarlo, se recarga el listado
 *    (o se va al destino que indique la respuesta).
 *  - Si responde con un error, se muestra una alerta fija dentro del formulario (sin cerrarlo ni perder
 *    lo que el usuario escribió).
 *
 * La alerta es el elemento "alert-formulario" que está justo antes del formulario; si no existe, se crea.
 * Las validaciones propias de cada página se ejecutan primero: si cancelan el envío (preventDefault),
 * este script no hace nada.
 */
(function () {
    'use strict';

    var MENSAJE_INESPERADO = 'Error! Ocurrió un error inesperado. Intente nuevamente.';

    function alertaDe(formulario) {
        var alerta = formulario.previousElementSibling;
        if (!alerta || !alerta.classList.contains('alert-formulario')) {
            alerta = document.createElement('div');
            alerta.className = 'alert alert-danger alert-formulario py-2 px-3 small mb-3 d-none';
            alerta.setAttribute('role', 'alert');
            formulario.parentNode.insertBefore(alerta, formulario);
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

            if (respuesta.ok && datos.mensaje) {
                await window.mostrarExito(datos.mensaje);
                if (datos.destino) {
                    window.location.href = datos.destino;
                } else {
                    window.location.reload();
                }
                return;
            }
            mostrarError(formulario, datos.error
                || (respuesta.status === 403 ? 'Error! No tenés permisos para realizar esta operación.' : MENSAJE_INESPERADO));
        } catch (error) {
            mostrarError(formulario, 'Error! No se pudo conectar con el servidor. Intente nuevamente.');
        }
        if (boton) {
            boton.disabled = false;
        }
    });
})();
