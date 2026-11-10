/** Listado de intentos: "Aprobar intento" pide la confirmación con la alerta del sistema y. */
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
