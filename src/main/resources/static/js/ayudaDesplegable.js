/**
 * Ayuda desplegable de los formularios: el botón de información (data-ayuda-destino="id") muestra u oculta, con una
 * transición suave, el aviso que tiene ese id (clase ayuda-desplegable). Mismo estilo que la ayuda de la ficha y del
 * cronograma.
 */
(function () {
    'use strict';

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-ayuda-destino]');
        if (!boton) { return; }
        var destino = document.getElementById(boton.getAttribute('data-ayuda-destino'));
        if (!destino) { return; }
        var abierto = destino.classList.toggle('abierto');
        boton.setAttribute('aria-expanded', String(abierto));
        boton.classList.toggle('activa', abierto);
    });
})();
