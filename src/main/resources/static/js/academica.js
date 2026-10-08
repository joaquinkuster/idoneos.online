/**
 * Comportamiento de las pantallas de gestión académica (MOD-F-02).
 *
 *  1. Enlaces sin acción por ahora: marcados con data-sin-accion, no navegan a ningún lado.
 *  2. Acordeón de unidades: "Expandir todo" y "Colapsar todo".
 */
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
        document.querySelectorAll('.ac-unidad .collapse').forEach(function (panel) {
            var instancia = window.bootstrap.Collapse.getOrCreateInstance(panel, { toggle: false });
            if (expandir) {
                instancia.show();
            } else {
                instancia.hide();
            }
        });
    });
})();
