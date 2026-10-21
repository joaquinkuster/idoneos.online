// Ficha del curso: la línea de apoyo de la tarjeta de inscripción y la cohorte que recibe el formulario de inscripción
// (CU-45) siguen a la cohorte elegida
(function () {
    'use strict';
    var selector = document.querySelector('[data-cohortes]');
    var apoyo = document.querySelector('[data-apoyo]');
    if (!selector || !apoyo) { return; }
    var cohorteId = document.querySelector('[data-cohorte-id]');
    selector.addEventListener('change', function () {
        var opcion = selector.options[selector.selectedIndex];
        apoyo.textContent = 'Inscripción hasta el ' + opcion.getAttribute('data-hasta') + ' · ' + opcion.getAttribute('data-cupos');
        if (cohorteId) { cohorteId.value = opcion.value; }
    });

    // Aviso de cómo se paga: el botón de información lo despliega y lo recoge con una transición suave
    var botonAyuda = document.querySelector('[data-ayuda]');
    var textoAyuda = document.querySelector('[data-ayuda-texto]');
    if (botonAyuda && textoAyuda) {
        botonAyuda.addEventListener('click', function () {
            var abierto = textoAyuda.classList.toggle('abierto');
            botonAyuda.setAttribute('aria-expanded', String(abierto));
            botonAyuda.classList.toggle('activa', abierto);
        });
    }
})();
