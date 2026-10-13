/**
 * Slider de los socios fundadores (página "Acerca de"): pasa solo cada cierto tiempo (data-intervalo, en milisegundos),
 * se detiene mientras el mouse está encima, hay foco dentro o la pestaña está oculta, y no se mueve solo si la persona
 * pidió menos animaciones. Se puede pasar de carta arrastrando (con el mouse o con el dedo) y con los puntos.
 */
(function () {
    'use strict';

    var slider = document.querySelector('[data-slider]');
    if (!slider) { return; }

    var pista = slider.querySelector('[data-pista]');
    var cartas = slider.querySelectorAll('[data-slide]');
    var contenedorPuntos = slider.querySelector('[data-puntos]');
    var intervalo = parseInt(slider.dataset.intervalo, 10) || 6000;
    var actual = 0;
    var temporizador = null;
    var detenido = false;
    var sinMovimiento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    var puntos = Array.prototype.map.call(cartas, function (carta, i) {
        var punto = document.createElement('button');
        punto.type = 'button';
        punto.className = 'ac-slider-punto';
        punto.setAttribute('aria-label', 'Ir al socio ' + (i + 1) + ' de ' + cartas.length);
        punto.addEventListener('click', function () { ir(i); reiniciar(); });
        contenedorPuntos.appendChild(punto);
        return punto;
    });

    function ir(indice) {
        actual = (indice + cartas.length) % cartas.length;
        pista.style.transform = 'translateX(-' + (actual * 100) + '%)';
        Array.prototype.forEach.call(cartas, function (carta, i) {
            carta.setAttribute('aria-hidden', String(i !== actual));
        });
        puntos.forEach(function (punto, i) {
            punto.classList.toggle('activo', i === actual);
            punto.setAttribute('aria-current', String(i === actual));
        });
    }

    function iniciar() {
        detener();
        if (sinMovimiento || cartas.length < 2 || detenido) { return; }
        temporizador = window.setInterval(function () { ir(actual + 1); }, intervalo);
    }

    function detener() {
        if (temporizador) { window.clearInterval(temporizador); temporizador = null; }
    }

    function reiniciar() { iniciar(); }

    // Arrastrar para pasar de carta: se sigue el dedo o el mouse y, si se movió lo suficiente, se pasa a la anterior o a la siguiente
    var ventana = slider.querySelector('.ac-slider-ventana');
    var inicioX = 0;
    var desplazamiento = 0;
    var arrastrando = false;
    var seMovio = false;

    function alMover(evento) {
        if (!arrastrando) { return; }
        desplazamiento = evento.clientX - inicioX;
        if (Math.abs(desplazamiento) > 6) { seMovio = true; }
        pista.style.transform = 'translateX(calc(-' + (actual * 100) + '% + ' + desplazamiento + 'px))';
    }

    function alSoltar() {
        if (!arrastrando) { return; }
        arrastrando = false;
        ventana.classList.remove('arrastrando');
        document.removeEventListener('pointermove', alMover);
        document.removeEventListener('pointerup', alSoltar);
        document.removeEventListener('pointercancel', alSoltar);
        if (Math.abs(desplazamiento) > ventana.offsetWidth * 0.12) {
            ir(actual + (desplazamiento < 0 ? 1 : -1));
        } else {
            ir(actual);
        }
        reiniciar();
    }

    ventana.addEventListener('pointerdown', function (evento) {
        if (evento.pointerType === 'mouse' && evento.button !== 0) { return; }
        arrastrando = true;
        seMovio = false;
        inicioX = evento.clientX;
        desplazamiento = 0;
        ventana.classList.add('arrastrando');
        detener();
        document.addEventListener('pointermove', alMover);
        document.addEventListener('pointerup', alSoltar);
        document.addEventListener('pointercancel', alSoltar);
    });

    // Si se arrastró, el clic que sigue (por ejemplo sobre un ícono de red social) no debe abrir el enlace
    ventana.addEventListener('click', function (evento) {
        if (seMovio) { evento.preventDefault(); evento.stopPropagation(); seMovio = false; }
    }, true);

    // Se detiene mientras la persona lo mira o lo usa
    slider.addEventListener('mouseenter', function () { detenido = true; detener(); });
    slider.addEventListener('mouseleave', function () { detenido = false; iniciar(); });
    slider.addEventListener('focusin', function () { detenido = true; detener(); });
    slider.addEventListener('focusout', function () { detenido = false; iniciar(); });
    document.addEventListener('visibilitychange', function () {
        if (document.hidden) { detener(); } else { iniciar(); }
    });

    ir(0);
    iniciar();
})();
