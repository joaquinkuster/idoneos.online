/**
 * Botón "Ver motivo" del historial de pagos: muestra el motivo del rechazo en un globo (popover) al hacer clic. Se cierra
 * al hacer clic en otro lado o en otro botón. El texto del motivo viene en data-bs-content.
 */
(function () {
    'use strict';
    if (!window.bootstrap) { return; }

    var botones = document.querySelectorAll('[data-motivo-pago]');
    botones.forEach(function (boton) {
        boton.setAttribute('data-bs-toggle', 'popover');
        new bootstrap.Popover(boton, {
            title: 'Motivo del rechazo',
            placement: 'bottom',
            animation: false,
            trigger: 'manual',
            container: 'body',
            customClass: 'pn-motivo-popover'
        });
        boton.addEventListener('click', function (e) {
            e.stopPropagation();
            botones.forEach(function (otro) {
                if (otro !== boton) { bootstrap.Popover.getInstance(otro).hide(); }
            });
            bootstrap.Popover.getInstance(boton).toggle();
        });
    });

    document.addEventListener('click', function (e) {
        if (e.target.closest('.popover')) { return; }
        botones.forEach(function (boton) { bootstrap.Popover.getInstance(boton).hide(); });
    });
})();
