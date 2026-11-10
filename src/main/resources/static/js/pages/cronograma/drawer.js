/** Cajón del cronograma de la vista del curso. */
(function () {
    'use strict';

    var contenedor = document.querySelector('.ac-tiene-drawer');
    if (!contenedor) { return; }

    var CLAVE = 'idoneos.curso.drawerOculto';
    var estrecho = window.matchMedia('(max-width: 1100px)');

    function recordado() {
        try { return window.localStorage.getItem(CLAVE) === '1'; } catch (e) { return false; }
    }

    function recordar(oculto) {
        try { window.localStorage.setItem(CLAVE, oculto ? '1' : '0'); } catch (e) { /* sin almacenamiento */ }
    }

    function aplicar(oculto) {
        contenedor.classList.toggle('drawer-oculto', oculto);
    }

    aplicar(estrecho.matches ? true : recordado());

    document.querySelectorAll('[data-drawer-alternar]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var oculto = !contenedor.classList.contains('drawer-oculto');
            aplicar(oculto);
            if (!estrecho.matches) { recordar(oculto); }
        });
    });

    estrecho.addEventListener('change', function (evento) {
        aplicar(evento.matches ? true : recordado());
    });
})();
