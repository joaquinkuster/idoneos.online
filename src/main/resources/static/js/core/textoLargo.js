/** Texto largo en una celda de tabla (fragments/components/textoLargo.html). */
(function () {
    'use strict';
    if (!window.bootstrap) { return; }

    function globo(enlace) {
        var instancia = bootstrap.Popover.getInstance(enlace);
        if (!instancia) {
            enlace.setAttribute('data-bs-toggle', 'popover');
            instancia = new bootstrap.Popover(enlace, {
                placement: 'bottom',
                animation: false,
                trigger: 'manual',
                container: 'body',
                customClass: 'pn-texto-popover'
            });
        }
        return instancia;
    }

    function cerrarTodos(excepto) {
        document.querySelectorAll('[data-texto-completo]').forEach(function (otro) {
            var instancia = bootstrap.Popover.getInstance(otro);
            if (instancia && otro !== excepto) { instancia.hide(); }
        });
    }

    document.addEventListener('click', function (e) {
        var enlace = e.target.closest('[data-texto-completo]');
        if (enlace) {
            e.preventDefault();
            e.stopPropagation();
            cerrarTodos(enlace);
            globo(enlace).toggle();
            return;
        }
        if (e.target.closest('.popover')) { return; }
        cerrarTodos(null);
    });
})();
