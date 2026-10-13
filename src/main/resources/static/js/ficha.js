// Ficha del curso: la línea de apoyo de la tarjeta de inscripción sigue a la cohorte elegida y la ventana de
// inscripción (CU-45) recibe la cohorte que se eligió
(function () {
    'use strict';
    var selector = document.querySelector('[data-cohortes]');
    var apoyo = document.querySelector('[data-apoyo]');
    if (!selector || !apoyo) { return; }
    selector.addEventListener('change', function () {
        var opcion = selector.options[selector.selectedIndex];
        apoyo.textContent = 'Inscripción hasta el ' + opcion.getAttribute('data-hasta') + ' · ' + opcion.getAttribute('data-cupos');
    });

    var modal = document.getElementById('modalInscribirme');
    if (!modal) { return; }
    modal.addEventListener('show.bs.modal', function () {
        var opcion = selector.options[selector.selectedIndex];
        modal.querySelector('[data-modal-cohorte]').textContent = opcion.textContent + ' · ' + apoyo.textContent;
        modal.querySelector('[data-modal-cohorte-id]').value = opcion.value;
    });
})();
