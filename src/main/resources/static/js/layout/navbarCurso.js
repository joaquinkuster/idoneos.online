/** Pestañas del curso: cada pestaña con secciones (Programas, Temas, Cohortes) abre y cierra su menú. */
(function () {
    'use strict';

    var botones = Array.prototype.slice.call(document.querySelectorAll('[data-navbar-mas]'));
    if (!botones.length) { return; }

    function menuDe(boton) { return boton.parentNode.querySelector('[data-navbar-menu]'); }

    function cerrarTodos(excepto) {
        botones.forEach(function (boton) {
            if (boton === excepto) { return; }
            menuDe(boton).hidden = true;
            boton.setAttribute('aria-expanded', 'false');
        });
    }

    botones.forEach(function (boton) {
        boton.addEventListener('click', function (evento) {
            evento.stopPropagation();
            var menu = menuDe(boton);
            cerrarTodos(boton);
            menu.hidden = !menu.hidden;
            boton.setAttribute('aria-expanded', String(!menu.hidden));
        });
    });
    document.addEventListener('click', function () { cerrarTodos(null); });
    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape') { cerrarTodos(null); }
    });
})();
