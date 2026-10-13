// Ficha del curso: la línea de apoyo de la tarjeta de inscripción sigue a la cohorte elegida
(function () {
    'use strict';
    var selector = document.querySelector('[data-cohortes]');
    var apoyo = document.querySelector('[data-apoyo]');
    if (!selector || !apoyo) { return; }
    selector.addEventListener('change', function () {
        var opcion = selector.options[selector.selectedIndex];
        apoyo.textContent = 'Inscripción hasta el ' + opcion.getAttribute('data-hasta') + ' · ' + opcion.getAttribute('data-cupos');
    });
})();
