/**
 * Progreso de los alumnos (CU-55): el botón "Ver detalle" de cada fila despliega, debajo, la tabla de temas del alumno.
 * El despliegue se anima con CSS (ver .pn-detalle en panel.css); acá solo se alterna la clase y el texto del botón.
 */
(function () {
    'use strict';

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-progreso-detalle]');
        if (!boton) {
            return;
        }
        var fila = document.getElementById(boton.dataset.progresoDetalle);
        if (!fila) {
            return;
        }
        var abierto = fila.classList.toggle('abierto');
        boton.setAttribute('aria-expanded', abierto ? 'true' : 'false');
        boton.textContent = abierto ? 'Ocultar detalle' : 'Ver detalle';
    });
})();
