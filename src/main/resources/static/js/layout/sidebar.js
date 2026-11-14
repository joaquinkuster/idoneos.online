/** Grupos desplegables del menú lateral. */
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

            // Cada grupo va dentro de su contenedor con borde. Si tiene la página actual, el contenedor lo marca
            var caja = document.createElement('div');
            caja.className = 'pn-menu-caja';
            titulo.parentNode.insertBefore(caja, titulo);
            caja.appendChild(titulo);
            caja.appendChild(cuerpo);
            caja.classList.toggle('con-activo', !!cuerpo.querySelector('.pn-menu-enlace.activo') || titulo.classList.contains('activo'));

            var clave = 'idoneos.menu.grupo.' + (titulo.getAttribute('title') || titulo.textContent.trim());
            // Solo queda desplegado el grupo donde está la persona. Los demás arrancan colapsados (se pueden abrir a mano)
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
        // Entrada del menú: los enlaces y títulos aparecen con un fundido, uno tras otro (movimiento.css)
        if (!(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches)) {
            Array.prototype.forEach.call(menu.querySelectorAll('.pn-menu-grupo, .pn-menu-enlace'), function (el, i) {
                el.style.setProperty('--i', Math.min(i, 12));
                el.classList.add('pn-entra-menu');
            });
            setTimeout(function () {
                menu.querySelectorAll('.pn-entra-menu').forEach(function (el) { el.classList.remove('pn-entra-menu'); el.style.removeProperty('--i'); });
            }, 900);
        }
        menu.classList.add('agrupado');
    });
})();
