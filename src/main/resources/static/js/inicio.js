/**
 * Índice de categorías del inicio: muestra de a 5 filas por página (data-por-pagina) y arma el paginador (anterior,
 * números y siguiente). Si hay una sola página no muestra el paginador.
 */
(function () {
    'use strict';

    var indice = document.querySelector('[data-categorias]');
    if (!indice) { return; }

    var filas = Array.prototype.slice.call(indice.querySelectorAll('[data-fila-categoria]'));
    var paginador = indice.querySelector('[data-paginas]');
    var porPagina = parseInt(indice.dataset.porPagina, 10) || 5;
    var totalPaginas = Math.max(1, Math.ceil(filas.length / porPagina));
    var pagina = 1;

    function boton(texto, etiqueta, destino, activo, deshabilitado) {
        var b = document.createElement('button');
        b.type = 'button';
        b.className = 'ix-pagina' + (activo ? ' activa' : '');
        b.innerHTML = texto;
        b.setAttribute('aria-label', etiqueta);
        if (activo) { b.setAttribute('aria-current', 'page'); }
        b.disabled = !!deshabilitado;
        b.addEventListener('click', function () { pagina = destino; dibujar(); });
        return b;
    }

    function dibujar() {
        var inicio = (pagina - 1) * porPagina;
        filas.forEach(function (fila, i) { fila.hidden = !(i >= inicio && i < inicio + porPagina); });
        if (totalPaginas <= 1) { paginador.hidden = true; return; }
        paginador.hidden = false;
        paginador.innerHTML = '';
        paginador.appendChild(boton('<i class="fa-solid fa-chevron-left"></i>', 'Página anterior', pagina - 1, false, pagina === 1));
        for (var n = 1; n <= totalPaginas; n++) {
            paginador.appendChild(boton(String(n), 'Página ' + n, n, n === pagina, false));
        }
        paginador.appendChild(boton('<i class="fa-solid fa-chevron-right"></i>', 'Página siguiente', pagina + 1, false, pagina === totalPaginas));
    }

    dibujar();
})();

/**
 * Hero del inicio: puntos del mismo estilo que los de "Acerca de" (siguen a la imagen que se muestra y llevan a ella) y
 * la imagen se puede arrastrar para pasar a la anterior o a la siguiente.
 */
(function () {
    'use strict';

    var carrusel = document.getElementById('heroCarousel');
    var contenedor = document.querySelector('[data-hero-puntos]');
    if (!carrusel || !contenedor || !window.bootstrap) { return; }

    var instancia = window.bootstrap.Carousel.getOrCreateInstance(carrusel);
    var cantidad = carrusel.querySelectorAll('.carousel-item').length;
    var puntos = [];

    for (var i = 0; i < cantidad; i++) {
        (function (destino) {
            var punto = document.createElement('button');
            punto.type = 'button';
            punto.className = 'ix-hero-punto';
            punto.setAttribute('aria-label', 'Ir a la imagen ' + (destino + 1) + ' de ' + cantidad);
            punto.addEventListener('click', function () { instancia.to(destino); });
            contenedor.appendChild(punto);
            puntos.push(punto);
        })(i);
    }

    function marcar(indice) {
        puntos.forEach(function (punto, i) {
            punto.classList.toggle('activo', i === indice);
            punto.setAttribute('aria-current', String(i === indice));
        });
    }

    marcar(0);
    carrusel.addEventListener('slide.bs.carousel', function (evento) { marcar(evento.to); });

    // Arrastrar la imagen: si se mueve lo suficiente pasa a la anterior o a la siguiente
    var marco = carrusel.querySelector('.wf-hero-parallelogram');
    if (!marco) { return; }
    var inicioX = 0;
    var arrastrando = false;

    function alSoltar(evento) {
        if (!arrastrando) { return; }
        arrastrando = false;
        marco.classList.remove('arrastrando');
        document.removeEventListener('pointerup', alSoltar);
        document.removeEventListener('pointercancel', alSoltar);
        var delta = evento.clientX - inicioX;
        if (Math.abs(delta) > 40) {
            if (delta < 0) { instancia.next(); } else { instancia.prev(); }
        }
        instancia.cycle();
    }

    marco.addEventListener('pointerdown', function (evento) {
        if (evento.pointerType === 'mouse' && evento.button !== 0) { return; }
        arrastrando = true;
        inicioX = evento.clientX;
        marco.classList.add('arrastrando');
        instancia.pause();
        document.addEventListener('pointerup', alSoltar);
        document.addEventListener('pointercancel', alSoltar);
    });
    marco.addEventListener('dragstart', function (evento) { evento.preventDefault(); });
})();
