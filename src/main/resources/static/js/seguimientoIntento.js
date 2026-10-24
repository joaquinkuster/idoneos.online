/**
 * Listado de intentos (CU-68): "Aprobar intento" (CU-71) pide la confirmación con la alerta del sistema y, si el docente
 * confirma, envía el formulario del intento (que lo procesa form-ajax y muestra el resultado).
 */
(function () {
    'use strict';

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-aprobar-intento]');
        if (!boton) { return; }
        var formulario = document.getElementById(boton.dataset.formulario);
        if (!formulario) { return; }
        confirmarAccion(boton.dataset.pregunta, boton.dataset.aclaracion, 'Aprobar intento').then(function (confirmado) {
            if (confirmado) { formulario.requestSubmit(); }
        });
    });
})();
