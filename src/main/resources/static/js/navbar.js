/**
 * Comportamiento del navbar único (fragments/navbar.html).
 *
 *  1. Marca como activo el enlace de la página actual.
 *  2. Color: en el sitio público con banner oscuro es transparente arriba y pasa a blanco al hacer scroll. Los paneles y
 *     las páginas sin banner oscuro (inicio de sesión, error) lo muestran siempre blanco.
 *  3. Botón del menú lateral (solo si la página lo tiene): contrae o expande el menú en pantallas grandes y lo abre como
 *     cajón en pantallas chicas.
 *  4. Pantallas chicas: el botón de la derecha abre y cierra los enlaces y el menú del usuario (también con Escape, con un
 *     clic afuera y al agrandarse la ventana).
 */
(function () {
    'use strict';

    var CLAVE_MENU = 'idoneos.panel.menuColapsado';
    var PUNTO_DE_QUIEBRE_MENU = 900;
    var PUNTO_DE_QUIEBRE_NAVBAR = 992;

    function iniciar() {
        var navbar = document.querySelector('[data-navbar]');
        if (!navbar) { return; }
        var enlaces = navbar.querySelector('.nb-enlaces');
        var toggle = navbar.querySelector('[data-nb-toggle]');

        // 1. Enlace activo
        var ruta = window.location.pathname;
        navbar.querySelectorAll('.nb-enlace').forEach(function (enlace) {
            var destino = enlace.getAttribute('href');
            if (destino && (destino === ruta || (destino !== '/' && destino !== '/inicio' && ruta.indexOf(destino) === 0))) {
                enlace.classList.add('activo');
            }
        });

        // 2. Color
        var hayBannerOscuro = !!document.querySelector('.wf-hero-banner, .wf-hero-section, .auth-pagina');
        if (!hayBannerOscuro) {
            navbar.classList.add('con-color');
        } else {
            var alHacerScroll = function () {
                var desplazamiento = window.scrollY || document.documentElement.scrollTop || document.body.scrollTop;
                navbar.classList.toggle('con-color', desplazamiento > 20);
            };
            // En captura, para atender el scroll sin importar qué elemento de la página se desplace
            document.addEventListener('scroll', alHacerScroll, { passive: true, capture: true });
            alHacerScroll();
        }

        // La transición de colores se activa recién ahora, para que el estado inicial no se anime
        window.requestAnimationFrame(function () {
            window.requestAnimationFrame(function () { navbar.classList.add('nb-animado'); });
        });

        // 3. Menú lateral
        var app = document.getElementById('pnApp');
        if (app && document.querySelector('.pn-menu')) {
            navbar.classList.add('con-menu-lateral');
        }

        // 4. Enlaces en pantallas chicas
        function cerrar() {
            if (!enlaces) { return; }
            enlaces.classList.remove('abierto');
            navbar.classList.remove('menu-abierto');
            if (toggle) {
                toggle.setAttribute('aria-expanded', 'false');
                toggle.querySelector('i').className = 'fa-solid fa-bars';
            }
        }

        if (toggle) {
            toggle.addEventListener('click', function () {
                var abierto = enlaces.classList.toggle('abierto');
                navbar.classList.toggle('menu-abierto', abierto);
                toggle.setAttribute('aria-expanded', abierto ? 'true' : 'false');
                toggle.querySelector('i').className = abierto ? 'fa-solid fa-xmark' : 'fa-solid fa-bars';
            });
        }
        document.addEventListener('click', function (evento) {
            if (!evento.target.closest('[data-navbar]')) { cerrar(); }
        });
        document.addEventListener('keydown', function (evento) {
            if (evento.key === 'Escape') { cerrar(); }
        });
        window.addEventListener('resize', function () {
            if (window.innerWidth >= PUNTO_DE_QUIEBRE_NAVBAR) { cerrar(); }
        });
    }

    // Botón del menú lateral (lo atiende panel.js junto con el resto del panel)
    window.alternarMenuLateral = function () {
        var app = document.getElementById('pnApp');
        if (!app) { return; }
        if (window.innerWidth <= PUNTO_DE_QUIEBRE_MENU) {
            app.classList.toggle('menu-abierto');
            return;
        }
        var contraido = app.classList.toggle('colapsado');
        try { window.localStorage.setItem(CLAVE_MENU, contraido ? '1' : '0'); } catch (e) { /* almacenamiento no disponible */ }
    };

    document.addEventListener('click', function (evento) {
        if (evento.target.closest('[data-menu-lateral]')) { window.alternarMenuLateral(); }
    });

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', iniciar);
    } else {
        iniciar();
    }
})();
