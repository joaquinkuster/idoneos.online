/**
 * Sliders del sitio (socios fundadores de "Acerca de" y cursos destacados del inicio).
 *
 * Estructura: [data-slider] > .ac-slider-ventana > [data-pista] > [data-slide] y [data-puntos] para los puntos.
 *  - Cuántas cartas se ven a la vez lo define el CSS con la variable --por-vista (responsivo); las páginas se calculan solas.
 *  - data-intervalo (milisegundos): si está, pasa solo; se detiene con el mouse encima, con el foco dentro o con la
 *    pestaña oculta, y no se mueve solo si la persona pidió menos animaciones.
 *  - Se puede arrastrar (mouse o dedo) y ir a una página con los puntos.
 */
(function () {
    'use strict';

    document.querySelectorAll('[data-slider]').forEach(iniciar);

    function iniciar(slider) {
        var ventana = slider.querySelector('.ac-slider-ventana');
        var pista = slider.querySelector('[data-pista]');
        var slides = Array.prototype.slice.call(slider.querySelectorAll('[data-slide]'));
        var contenedorPuntos = slider.querySelector('[data-puntos]');
        if (!ventana || !pista || !slides.length) { return; }

        var intervalo = parseInt(slider.dataset.intervalo, 10) || 0;
        var sinMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        var pagina = 0;
        var porVista = 1;
        var paginas = 1;
        var temporizador = null;
        var detenido = false;

        function medir() {
            porVista = Math.max(1, parseInt(getComputedStyle(slider).getPropertyValue('--por-vista'), 10) || 1);
            paginas = Math.max(1, Math.ceil(slides.length / porVista));
        }

        // Cuánto se desplaza la pista para mostrar una página (la última no deja huecos a la derecha)
        function desplazamiento(numero) {
            var separacion = parseFloat(getComputedStyle(pista).columnGap) || 0;
            var paso = slides[0].offsetWidth + separacion;
            return Math.min(numero * porVista * paso, Math.max(0, slides.length - porVista) * paso);
        }

        function dibujarPuntos() {
            contenedorPuntos.innerHTML = '';
            contenedorPuntos.hidden = paginas <= 1;
            for (var i = 0; i < paginas; i++) {
                (function (destino) {
                    var punto = document.createElement('button');
                    punto.type = 'button';
                    punto.className = 'ac-slider-punto';
                    punto.setAttribute('aria-label', 'Ir a la página ' + (destino + 1) + ' de ' + paginas);
                    punto.addEventListener('click', function () { ir(destino); reiniciar(); });
                    contenedorPuntos.appendChild(punto);
                })(i);
            }
        }

        function ir(numero) {
            pagina = ((numero % paginas) + paginas) % paginas;
            pista.style.transform = 'translateX(-' + desplazamiento(pagina) + 'px)';
            if (porVista === 1) {
                slides.forEach(function (slide, i) { slide.setAttribute('aria-hidden', String(i !== pagina)); });
            }
            Array.prototype.forEach.call(contenedorPuntos.children, function (punto, i) {
                punto.classList.toggle('activo', i === pagina);
                punto.setAttribute('aria-current', String(i === pagina));
            });
        }

        function iniciarPase() {
            detenerPase();
            if (!intervalo || sinMovimiento || paginas < 2 || detenido) { return; }
            temporizador = window.setInterval(function () { ir(pagina + 1); }, intervalo);
        }

        function detenerPase() {
            if (temporizador) { window.clearInterval(temporizador); temporizador = null; }
        }

        function reiniciar() { iniciarPase(); }

        // Arrastrar para pasar de página: sigue al puntero y, si se movió lo suficiente, pasa a la anterior o a la siguiente
        var inicioX = 0;
        var arrastre = 0;
        var arrastrando = false;
        var seMovio = false;

        function alMover(evento) {
            if (!arrastrando) { return; }
            arrastre = evento.clientX - inicioX;
            if (Math.abs(arrastre) > 6) { seMovio = true; }
            pista.style.transform = 'translateX(' + (-desplazamiento(pagina) + arrastre) + 'px)';
        }

        function alSoltar() {
            if (!arrastrando) { return; }
            arrastrando = false;
            ventana.classList.remove('arrastrando');
            document.removeEventListener('pointermove', alMover);
            document.removeEventListener('pointerup', alSoltar);
            document.removeEventListener('pointercancel', alSoltar);
            ir(Math.abs(arrastre) > ventana.offsetWidth * 0.1 ? pagina + (arrastre < 0 ? 1 : -1) : pagina);
            reiniciar();
        }

        ventana.addEventListener('pointerdown', function (evento) {
            if (evento.pointerType === 'mouse' && evento.button !== 0) { return; }
            arrastrando = true;
            seMovio = false;
            inicioX = evento.clientX;
            arrastre = 0;
            ventana.classList.add('arrastrando');
            detenerPase();
            document.addEventListener('pointermove', alMover);
            document.addEventListener('pointerup', alSoltar);
            document.addEventListener('pointercancel', alSoltar);
        });

        // Lo que se arrastra no debe abrir enlaces (los íconos de redes o el botón de una carta) ni arrastrar imágenes
        ventana.addEventListener('click', function (evento) {
            if (seMovio) { evento.preventDefault(); evento.stopPropagation(); seMovio = false; }
        }, true);
        ventana.addEventListener('dragstart', function (evento) { evento.preventDefault(); });

        // Se detiene mientras la persona lo mira o lo usa
        slider.addEventListener('mouseenter', function () { detenido = true; detenerPase(); });
        slider.addEventListener('mouseleave', function () { detenido = false; iniciarPase(); });
        slider.addEventListener('focusin', function () { detenido = true; detenerPase(); });
        slider.addEventListener('focusout', function () { detenido = false; iniciarPase(); });
        document.addEventListener('visibilitychange', function () {
            if (document.hidden) { detenerPase(); } else { iniciarPase(); }
        });

        // Al cambiar el ancho de la pantalla cambia cuántas cartas se ven: se recalculan las páginas
        var redimension = null;
        window.addEventListener('resize', function () {
            window.clearTimeout(redimension);
            redimension = window.setTimeout(function () {
                var anteriores = paginas;
                medir();
                if (paginas !== anteriores) { dibujarPuntos(); }
                ir(Math.min(pagina, paginas - 1));
            }, 120);
        });

        medir();
        dibujarPuntos();
        ir(0);
        iniciarPase();
    }
})();
