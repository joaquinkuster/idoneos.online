/** Selectores de programa y cohorte por defecto de la vista del curso (docente). */
(function () {
    'use strict';

    // Un selector con data-cambia-envia envía su formulario (GET) apenas se elige una opción
    document.addEventListener('change', function (evento) {
        var selector = evento.target.closest('[data-cambia-envia]');
        if (selector && selector.form) { selector.form.submit(); }
    });

    document.addEventListener('change', async function (evento) {
        var selector = evento.target.closest('[data-selector-contexto]');
        if (!selector || !selector.value) {
            return;
        }
        selector.disabled = true;
        try {
            var datos = new FormData();
            if (selector.dataset.parametro) {
                datos.append(selector.dataset.parametro, selector.value);
            }
            var respuesta = await fetch(selector.dataset.url.replace('{valor}', encodeURIComponent(selector.value)), {
                method: 'POST',
                body: datos,
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var resultado = await respuesta.json();
            if (!respuesta.ok || !resultado.mensaje) {
                await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
            }
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        }
        window.location.reload();
    });
})();
