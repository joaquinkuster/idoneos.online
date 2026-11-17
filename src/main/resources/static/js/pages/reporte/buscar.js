/** Reportes: genera el informe pedido, descarga el PDF y recarga el historial. */
(function () {
    'use strict';

    var formulario = document.getElementById('formGenerarInforme');
    if (!formulario) { return; }
    var boton = document.getElementById('btnGenerarInforme');

    function marcarError(campo, mensaje) {
        if (window.PanelValidacion) { window.PanelValidacion.marcar(campo, mensaje); }
    }

    formulario.addEventListener('submit', async function (e) {
        e.preventDefault();
        var valido = true;
        formulario.querySelectorAll('[required]').forEach(function (campo) {
            var vacio = !campo.value;
            marcarError(campo, vacio ? 'Este campo es obligatorio.' : '');
            if (vacio) { valido = false; }
        });
        if (!valido) { return; }
        var texto = boton.textContent;
        boton.disabled = true;
        boton.textContent = 'Generando...';
        try {
            var respuesta = await fetch(formulario.action, { method: 'POST', body: new URLSearchParams(new FormData(formulario)),
                headers: { 'X-Requested-With': 'fetch' }, credentials: 'same-origin' });
            if (!respuesta.ok || (respuesta.headers.get('Content-Type') || '').indexOf('application/pdf') < 0) {
                var cuerpo = await respuesta.json().catch(function () { return {}; });
                await window.mostrarError(cuerpo.error || 'Error! No se pudo generar el informe. Intentá nuevamente.');
                return;
            }
            var archivo = (respuesta.headers.get('Content-Disposition') || '').match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
            var blob = await respuesta.blob();
            var enlace = document.createElement('a');
            enlace.href = URL.createObjectURL(blob);
            enlace.download = archivo ? decodeURIComponent(archivo[1]) : 'informe.pdf';
            document.body.appendChild(enlace);
            enlace.click();
            enlace.remove();
            setTimeout(function () { URL.revokeObjectURL(enlace.href); }, 10000);
            await window.mostrarExito('Informe generado con éxito.');
            window.location.reload();
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        } finally {
            boton.disabled = false;
            boton.textContent = texto;
        }
    });
})();
