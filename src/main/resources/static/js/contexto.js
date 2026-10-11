/**
 * Selectores de programa y cohorte por defecto de la vista del curso (docente): al elegir una opción se guarda en la
 * participación del docente en el curso y la página se recarga con ese contexto.
 *
 * El selector indica su destino en data-url. Si tiene data-parametro, el valor se envía con ese nombre; si no, la
 * dirección lleva el marcador {valor}, que se reemplaza por el valor elegido.
 */
(function () {
    'use strict';

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
                await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intente nuevamente.');
            }
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intente nuevamente.');
        }
        window.location.reload();
    });
})();
