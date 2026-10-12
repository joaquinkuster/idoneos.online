/**
 * Cajón del cronograma de la vista del curso: se oculta con la cruz y se vuelve a mostrar con la pestaña del borde
 * derecho. En pantallas anchas se recuerda la elección; en las chicas arranca oculto y se abre sobre el contenido.
 */
(function () {
    'use strict';

    var contenedor = document.querySelector('.ac-tiene-cajon');
    if (!contenedor) { return; }

    var CLAVE = 'idoneos.curso.cajonOculto';
    var estrecho = window.matchMedia('(max-width: 1100px)');

    function recordado() {
        try { return window.localStorage.getItem(CLAVE) === '1'; } catch (e) { return false; }
    }

    function recordar(oculto) {
        try { window.localStorage.setItem(CLAVE, oculto ? '1' : '0'); } catch (e) { /* sin almacenamiento */ }
    }

    function aplicar(oculto) {
        contenedor.classList.toggle('cajon-oculto', oculto);
    }

    aplicar(estrecho.matches ? true : recordado());

    document.querySelectorAll('[data-cajon-alternar]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var oculto = !contenedor.classList.contains('cajon-oculto');
            aplicar(oculto);
            if (!estrecho.matches) { recordar(oculto); }
        });
    });

    estrecho.addEventListener('change', function (evento) {
        aplicar(evento.matches ? true : recordado());
    });
})();
