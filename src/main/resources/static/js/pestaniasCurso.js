/**
 * Pestañas del curso: el botón "Más" (docente) abre y cierra el menú con el resto de las secciones.
 * Se cierra al hacer clic afuera o con Escape.
 */
(function () {
    'use strict';

    var boton = document.querySelector('[data-pestanias-mas]');
    var menu = document.querySelector('[data-pestanias-menu]');
    if (!boton || !menu) { return; }

    function alternar(abrir) {
        menu.hidden = !abrir;
        boton.setAttribute('aria-expanded', String(abrir));
    }

    boton.addEventListener('click', function (evento) {
        evento.stopPropagation();
        alternar(menu.hidden);
    });
    document.addEventListener('click', function (evento) {
        if (!menu.hidden && !menu.contains(evento.target)) { alternar(false); }
    });
    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape') { alternar(false); }
    });
})();
