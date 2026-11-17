/** Reportes: genera el informe pedido, lo abre en una pestaña nueva y recarga el historial. */
(function () {
    'use strict';

    var formulario = document.getElementById('formGenerarInforme');
    if (!formulario) { return; }
    var boton = document.getElementById('btnGenerarInforme');

    formulario.addEventListener('submit', async function (e) {
        e.preventDefault();
        // La pestaña se abre ahora, con el clic del usuario, para que el navegador no la bloquee; el PDF se carga cuando llega
        var pestania = window.open('', '_blank');
        var texto = boton.textContent;
        boton.disabled = true;
        boton.textContent = 'Generando...';
        try {
            var respuesta = await fetch(formulario.action, { method: 'POST', body: new URLSearchParams(new FormData(formulario)),
                headers: { 'X-Requested-With': 'fetch' }, credentials: 'same-origin' });
            if (!respuesta.ok || (respuesta.headers.get('Content-Type') || '').indexOf('application/pdf') < 0) {
                if (pestania) { pestania.close(); }
                var cuerpo = await respuesta.json().catch(function () { return {}; });
                await window.mostrarError(cuerpo.error || 'Error! No se pudo generar el informe. Intentá nuevamente.');
                return;
            }
            var blob = await respuesta.blob();
            var direccion = URL.createObjectURL(blob);
            if (pestania) {
                pestania.location.href = direccion;
            } else {
                window.location.href = direccion;
            }
            setTimeout(function () { URL.revokeObjectURL(direccion); }, 60000);
            await window.mostrarExito('Informe generado con éxito.');
            window.location.reload();
        } catch (error) {
            if (pestania) { pestania.close(); }
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        } finally {
            boton.disabled = false;
            boton.textContent = texto;
        }
    });
})();
