/** Comportamiento de las pantallas de gestión académica. */
(function () {
    'use strict';

    document.addEventListener('click', function (e) {
        if (e.target.closest('[data-sin-accion]')) {
            e.preventDefault();
            return;
        }

        var expandir = e.target.closest('[data-ac-expandir]');
        var colapsar = e.target.closest('[data-ac-colapsar]');
        if (!expandir && !colapsar) {
            return;
        }
        document.querySelectorAll('.ac-tema .collapse').forEach(function (panel) {
            var instancia = window.bootstrap.Collapse.getOrCreateInstance(panel, { toggle: false });
            if (expandir) {
                instancia.show();
            } else {
                instancia.hide();
            }
        });
    });
})();
