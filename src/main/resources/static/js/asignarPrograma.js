/**
 * Ventana "Asignar programa" de una cohorte pendiente: al elegir el programa se avisa en qué estado queda la cohorte (activa si el
 * contenido del programa está completo, pendiente si no).
 */
(function () {
    'use strict';

    document.querySelectorAll('form[data-asignar-programa]').forEach(function (formulario) {
        var select = formulario.querySelector('select[name="programaId"]');
        var aviso = formulario.querySelector('[data-estado-resultante]');
        var valor = formulario.querySelector('[data-estado-valor]');
        var actual = aviso.dataset.estadoActual;
        function actualizar() {
            var opcion = select.selectedOptions[0];
            var completo = opcion && opcion.dataset.completo === 'true';
            valor.textContent = completo ? 'Activa' : actual;
            aviso.innerHTML = completo
                ? 'El contenido del programa está completo: la cohorte queda <strong>activa</strong>.'
                : 'La cohorte sigue <strong>pendiente</strong> hasta que el contenido del programa esté completo.';
        }
        select.addEventListener('change', actualizar);
        actualizar();
    });
})();
