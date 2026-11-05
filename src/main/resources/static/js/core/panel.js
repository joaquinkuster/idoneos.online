/**
 * Arranque del panel: menú lateral en pantallas chicas, textos truncados y filtros automáticos de los formularios
 * con data-filtros. El resto vive en módulos aparte: core/validacion.js (formularios) y core/tabla.js (listados).
 */
(function () {
    'use strict';

    var CLAVE_FOCO = 'idoneos.panel.focoFiltro';

    // ------------------------------------------------------------------
    // 1. Menú lateral
    // ------------------------------------------------------------------
    var app = document.getElementById('pnApp');
    if (app) {
        document.addEventListener('click', function (e) {
            if (e.target.closest('[data-cerrar-menu]')) {
                app.classList.remove('menu-abierto');
            }
        });
    }

    // Los grupos desplegables del menú lateral los arma layout/sidebar.js, que se carga junto al menú

    // Textos truncados con "...": el texto completo queda disponible al pasar el mouse
    document.querySelectorAll('.pn-truncar').forEach(function (e) {
        if (!e.title) { e.title = e.textContent.trim(); }
    });

    // ------------------------------------------------------------------
    // 3. Filtros automáticos
    // ------------------------------------------------------------------
    document.querySelectorAll('form[data-filtros]').forEach(function (form) {
        var temporizador = null;
        form.addEventListener('change', function (e) {
            if (e.target.matches('select, input[type="date"]')) { form.submit(); }
        });
        form.addEventListener('input', function (e) {
            if (e.target.matches('input[type="text"], input[type="search"]')) {
                clearTimeout(temporizador);
                temporizador = setTimeout(function () {
                    try { window.sessionStorage.setItem(CLAVE_FOCO, e.target.name); } catch (err) { /* sin almacenamiento */ }
                    form.submit();
                }, 450);
            }
        });
        // Devuelve el foco al campo de texto que se estaba escribiendo
        try {
            var nombre = window.sessionStorage.getItem(CLAVE_FOCO);
            if (nombre) {
                window.sessionStorage.removeItem(CLAVE_FOCO);
                var campo = form.querySelector('[name="' + nombre + '"]');
                if (campo) {
                    campo.focus();
                    campo.setSelectionRange(campo.value.length, campo.value.length);
                }
            }
        } catch (err) { /* sin almacenamiento */ }
    });
})();
