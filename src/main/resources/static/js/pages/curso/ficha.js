// Ficha del curso: la card de inscripción sigue a la cohorte elegida. Si está abierta, muestra «Inscribirme»; si es próxima,
// «Recordarme» (el sistema le avisa al alumno unos días antes de que abra la inscripción) o el aviso de que ya tiene el recordatorio.
(function () {
    'use strict';
    var selector = document.querySelector('[data-cohortes]');
    var apoyo = document.querySelector('[data-apoyo]');
    if (!selector || !apoyo) { return; }
    var cohorteId = document.querySelector('[data-cohorte-id]');
    var bloqueAbierta = document.querySelector('[data-bloque="abierta"]');
    var bloqueProxima = document.querySelector('[data-bloque="proxima"]');
    var botonAyuda = document.querySelector('[data-ayuda]');
    var textoAyuda = document.querySelector('[data-ayuda-texto]');
    var botonRecordar = document.querySelector('[data-recordar]');
    var notaRecordar = document.querySelector('[data-recordar-nota]');

    function opcion() { return selector.options[selector.selectedIndex]; }

    function pintar() {
        var o = opcion();
        var abierta = o.getAttribute('data-abierta') === 'true';
        apoyo.textContent = abierta
            ? 'Inscripción hasta el ' + o.getAttribute('data-hasta') + ' · ' + o.getAttribute('data-cupos')
            : 'La inscripción abre el ' + o.getAttribute('data-abre');
        if (cohorteId) { cohorteId.value = o.value; }
        if (bloqueAbierta) { bloqueAbierta.hidden = !abierta; }
        if (bloqueProxima) { bloqueProxima.hidden = abierta; }
        if (botonAyuda) { botonAyuda.hidden = !abierta; }
        if (textoAyuda && !abierta) {
            textoAyuda.classList.remove('abierto');
            if (botonAyuda) { botonAyuda.setAttribute('aria-expanded', 'false'); botonAyuda.classList.remove('activa'); }
        }
        if (botonRecordar) {
            // Con el recordatorio pedido, el mismo botón pasa a «Quitar recordatorio», con la campana tachada
            var recordada = o.getAttribute('data-recordada') === 'true';
            botonRecordar.querySelector('[data-recordar-texto]').textContent = recordada ? 'Quitar recordatorio' : 'Recordarme';
            var icon = botonRecordar.querySelector('[data-recordar-icon]');
            icon.classList.toggle('fa-bell', !recordada);
            icon.classList.toggle('fa-bell-slash', recordada);
        }
    }

    async function llamar(url) {
        try {
            var respuesta = await fetch(url, { method: 'POST', headers: { 'Accept': 'application/json' }, credentials: 'same-origin' });
            var resultado = await respuesta.json();
            if (!respuesta.ok || !resultado.mensaje) {
                await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
                return false;
            }
            return true;
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
            return false;
        }
    }

    selector.addEventListener('change', pintar);
    if (botonRecordar) {
        botonRecordar.addEventListener('click', async function () {
            var recordada = opcion().getAttribute('data-recordada') === 'true';
            if (await llamar('/recordatorio/' + (recordada ? 'quitar/' : 'recordar/') + opcion().value)) {
                opcion().setAttribute('data-recordada', recordada ? 'false' : 'true');
                pintar();
            }
        });
    }

    // Aviso de cómo se paga: el botón de información lo despliega y lo recoge con una transición suave
    if (botonAyuda && textoAyuda) {
        botonAyuda.addEventListener('click', function () {
            var abierto = textoAyuda.classList.toggle('abierto');
            botonAyuda.setAttribute('aria-expanded', String(abierto));
            botonAyuda.classList.toggle('activa', abierto);
        });
    }
    pintar();
})();
