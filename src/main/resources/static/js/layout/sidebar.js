/**
 * Grupos desplegables del menú lateral. Se carga justo después del menú (no al final de la página) para que los grupos
 * ya estén armados cuando el navegador lo dibuja por primera vez.
 *
 * Los enlaces que siguen a un título, hasta el próximo, se ocultan o se muestran al hacerle clic. Arranca abierto el grupo
 * del enlace activo y colapsados los demás (en el inicio del curso, todos colapsados).
 */
(function () {
    'use strict';

    function guardar(clave, valor) {
        try { window.localStorage.setItem(clave, valor); } catch (e) { /* almacenamiento no disponible */ }
    }

    document.querySelectorAll('.pn-menu-nav:not(.agrupado)').forEach(function (menu) {
        Array.prototype.slice.call(menu.querySelectorAll(':scope > .pn-menu-grupo')).forEach(function (titulo) {
            var cuerpo = document.createElement('div');
            cuerpo.className = 'pn-menu-grupo-cuerpo';
            var siguiente = titulo.nextElementSibling;
            while (siguiente && !siguiente.classList.contains('pn-menu-grupo')) {
                var proximo = siguiente.nextElementSibling;
                cuerpo.appendChild(siguiente);
                siguiente = proximo;
            }
            titulo.parentNode.insertBefore(cuerpo, titulo.nextSibling);

            var clave = 'idoneos.menu.grupo.' + (titulo.getAttribute('title') || titulo.textContent.trim());
            // Solo queda desplegado el grupo donde está la persona; los demás arrancan colapsados (se pueden abrir a mano)
            var cerrado = !cuerpo.querySelector('.pn-menu-enlace.activo') && !titulo.classList.contains('activo');
            if (!cuerpo.children.length) { cuerpo.hidden = true; titulo.classList.add('sin-hijos'); }
            function aplicar() {
                titulo.classList.toggle('cerrado', cerrado);
                cuerpo.classList.toggle('cerrado', cerrado);
                titulo.setAttribute('aria-expanded', cerrado ? 'false' : 'true');
            }
            function alternar() {
                cerrado = !cerrado;
                guardar(clave, cerrado ? 'cerrado' : 'abierto');
                aplicar();
            }
            titulo.addEventListener('click', function (e) { if (!e.target.closest('a')) { alternar(); } });
            titulo.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); alternar(); }
            });
            aplicar();
        });
        menu.classList.add('agrupado');
    });
})();
