/** Progreso de los alumnos: el botón "Ver detalle" de cada fila despliega, debajo. */
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
