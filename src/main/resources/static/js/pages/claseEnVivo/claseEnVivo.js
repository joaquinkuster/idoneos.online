/** Formulario de las clases en vivo: muestra las fechas de dictado de la cohorte elegida. */
(function () {
    'use strict';

    var TEXTO_INICIAL = 'Debe estar dentro de las fechas de dictado de la cohorte.';

    function iniciar(formulario) {
        var cohorte = formulario.querySelector('[data-clase-cohorte]');
        var ayuda = formulario.querySelector('[data-clase-dictado]');
        if (!cohorte || !ayuda) { return; }

        function actualizar() {
            var opcion = cohorte.options[cohorte.selectedIndex];
            ayuda.textContent = opcion && opcion.dataset.inicio
                ? 'Dictado de la cohorte: del ' + opcion.dataset.inicio + ' al ' + opcion.dataset.fin + '.'
                : TEXTO_INICIAL;
        }
        cohorte.addEventListener('change', actualizar);
        actualizar();
    }

    document.querySelectorAll('form[data-clase-formulario]').forEach(iniciar);

    // Mientras una grabación se procesa, el listado se recarga hasta que aparece la opción de verla
    if (document.querySelector('[data-procesando]')) {
        setTimeout(function () { window.location.reload(); }, 10000);
    }
})();
