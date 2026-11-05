/**
 * Botón "Ver motivo" del historial de pagos: muestra el motivo del rechazo en un globo (popover) al hacer clic. Se cierra
 * al hacer clic en otro lado o en otro botón. El texto del motivo viene en data-bs-content.
 *
 * Funciona por delegación: los botones pueden llegar del servidor al filtrar o cambiar de página.
 */
(function () {
    'use strict';
    if (!window.bootstrap) { return; }

    function globo(boton) {
        var instancia = bootstrap.Popover.getInstance(boton);
        if (!instancia) {
            boton.setAttribute('data-bs-toggle', 'popover');
            instancia = new bootstrap.Popover(boton, {
                title: 'Motivo del rechazo',
                placement: 'bottom',
                animation: false,
                trigger: 'manual',
                container: 'body',
                customClass: 'pn-motivo-popover'
            });
        }
        return instancia;
    }

    function cerrarTodos(excepto) {
        document.querySelectorAll('[data-motivo-pago]').forEach(function (otro) {
            var instancia = bootstrap.Popover.getInstance(otro);
            if (instancia && otro !== excepto) { instancia.hide(); }
        });
    }

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-motivo-pago]');
        if (boton) {
            e.stopPropagation();
            cerrarTodos(boton);
            globo(boton).toggle();
            return;
        }
        if (e.target.closest('.popover')) { return; }
        cerrarTodos(null);
    });
})();
