/**
 * Visor de PDF integrado (MOD-F-02): muestra el documento de a una página, con botones de página anterior y
 * siguiente, número de página, zoom, pantalla completa y navegación con el teclado.
 *
 *  - Zoom: botones de acercar y alejar (de 50% a 300% del ajuste al ancho), clic en el porcentaje para volver al 100%,
 *    Ctrl + rueda del mouse y las teclas + , - y 0. Con la página más grande que el recuadro se desplaza con las
 *    barras o arrastrando con el mouse.
 *  - Teclado: flechas izquierda y derecha para cambiar de página.
 *
 * Usa PDF.js (cargado desde un CDN antes de este script). El archivo se pide a la plataforma con la sesión del usuario
 * (/material/archivo/{id}), que controla el acceso.
 */
(function () {
    'use strict';

    var visor = document.querySelector('[data-visor-pdf]');
    if (!visor) { return; }

    var estado = visor.querySelector('[data-visor-estado]');
    var desplazable = visor.querySelector('.mv-desplazable');
    var lienzo = visor.querySelector('canvas');
    var contexto = lienzo.getContext('2d');
    var entradaPagina = visor.querySelector('[data-visor-pagina]');
    var etiquetaTotal = visor.querySelector('[data-visor-total]');
    var botonAnterior = visor.querySelector('[data-visor-anterior]');
    var botonSiguiente = visor.querySelector('[data-visor-siguiente]');
    var botonPantalla = visor.querySelector('[data-visor-pantalla-completa]');
    var botonAlejar = visor.querySelector('[data-visor-alejar]');
    var botonAcercar = visor.querySelector('[data-visor-acercar]');
    var botonZoom = visor.querySelector('[data-visor-zoom]');

    var ZOOM_MINIMO = 0.5;
    var ZOOM_MAXIMO = 3;
    var PASO_ZOOM = 0.25;
    var MAXIMO_DE_PIXELES = 16000000; // límite del lienzo para no agotar la memoria con zoom alto y pantallas densas

    function mostrarEstado(texto) {
        estado.textContent = texto;
        estado.hidden = !texto;
        lienzo.hidden = !!texto;
    }

    if (!window.pdfjsLib) {
        mostrarEstado('No se pudo cargar el visor. Intentá nuevamente más tarde.');
        return;
    }
    window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

    var documento = null;
    var pagina = 1;
    var total = 0;
    var zoom = 1; // 1 = la página ajustada al ancho del visor
    var renderizando = false;
    var pendiente = null;
    var reajustesSeguidos = 0; // evita un bucle si el ancho disponible sigue cambiando

    function actualizarControles() {
        entradaPagina.value = pagina;
        botonAnterior.disabled = pagina <= 1;
        botonSiguiente.disabled = pagina >= total;
        botonZoom.textContent = Math.round(zoom * 100) + '%';
        botonAlejar.disabled = zoom <= ZOOM_MINIMO;
        botonAcercar.disabled = zoom >= ZOOM_MAXIMO;
    }

    /** Dibuja una página ajustada al ancho del visor (y al alto, en pantalla completa), multiplicada por el zoom. */
    function dibujar(numero) {
        if (renderizando) { pendiente = numero; return; }
        renderizando = true;
        pagina = numero;

        // Se recuerda qué parte de la página estaba en el centro para mantenerla a la vista después de redibujar
        var centroX = desplazable.scrollWidth ? (desplazable.scrollLeft + desplazable.clientWidth / 2) / desplazable.scrollWidth : 0.5;
        var centroY = desplazable.scrollHeight ? (desplazable.scrollTop + desplazable.clientHeight / 2) / desplazable.scrollHeight : 0.5;

        var anchoUsado = desplazable.clientWidth;
        documento.getPage(numero).then(function (p) {
            var tamanioBase = p.getViewport({ scale: 1 });
            var escala = anchoUsado / tamanioBase.width;
            if (document.fullscreenElement === visor) {
                escala = Math.min(escala, desplazable.clientHeight / tamanioBase.height);
            }
            escala *= zoom;
            var densidad = window.devicePixelRatio || 1;
            while (tamanioBase.width * tamanioBase.height * Math.pow(escala * densidad, 2) > MAXIMO_DE_PIXELES && densidad > 1) {
                densidad = Math.max(1, densidad - 0.5);
            }
            var vista = p.getViewport({ scale: escala * densidad });
            lienzo.width = vista.width;
            lienzo.height = vista.height;
            lienzo.style.width = (vista.width / densidad) + 'px';
            lienzo.style.height = (vista.height / densidad) + 'px';
            return p.render({ canvasContext: contexto, viewport: vista }).promise;
        }).then(function () {
            renderizando = false;
            mostrarEstado('');
            actualizarControles();
            desplazable.scrollLeft = centroX * desplazable.scrollWidth - desplazable.clientWidth / 2;
            desplazable.scrollTop = centroY * desplazable.scrollHeight - desplazable.clientHeight / 2;
            if (pendiente !== null) {
                var siguiente = pendiente;
                pendiente = null;
                dibujar(siguiente);
            } else if (Math.abs(desplazable.clientWidth - anchoUsado) > 2 && reajustesSeguidos < 2) {
                // Apareció o desapareció una barra de desplazamiento: cambió el ancho disponible, se vuelve a ajustar
                reajustesSeguidos++;
                dibujar(pagina);
            } else {
                reajustesSeguidos = 0;
            }
        }).catch(function () {
            renderizando = false;
            mostrarEstado('No se pudo mostrar la página.');
        });
    }

    function irA(numero) {
        if (!documento) { return; }
        dibujar(Math.min(Math.max(numero, 1), total));
    }

    function cambiarZoom(nuevo) {
        if (!documento) { return; }
        zoom = Math.min(Math.max(Math.round(nuevo * 100) / 100, ZOOM_MINIMO), ZOOM_MAXIMO);
        actualizarControles();
        dibujar(pagina);
    }

    window.pdfjsLib.getDocument({ url: visor.dataset.url, withCredentials: true }).promise.then(function (doc) {
        documento = doc;
        total = doc.numPages;
        etiquetaTotal.textContent = total;
        entradaPagina.max = total;
        dibujar(1);
    }).catch(function () {
        mostrarEstado('No se pudo abrir el archivo. Intentá nuevamente más tarde.');
    });

    botonAnterior.addEventListener('click', function () { irA(pagina - 1); });
    botonSiguiente.addEventListener('click', function () { irA(pagina + 1); });
    entradaPagina.addEventListener('change', function () { irA(parseInt(entradaPagina.value, 10) || pagina); });
    botonAlejar.addEventListener('click', function () { cambiarZoom(zoom - PASO_ZOOM); });
    botonAcercar.addEventListener('click', function () { cambiarZoom(zoom + PASO_ZOOM); });
    botonZoom.addEventListener('click', function () { cambiarZoom(1); });

    document.addEventListener('keydown', function (e) {
        if (e.target === entradaPagina) { return; }
        if (e.key === 'ArrowLeft') { irA(pagina - 1); }
        if (e.key === 'ArrowRight') { irA(pagina + 1); }
        if (e.key === '+' || e.key === '=') { cambiarZoom(zoom + PASO_ZOOM); }
        if (e.key === '-') { cambiarZoom(zoom - PASO_ZOOM); }
        if (e.key === '0') { cambiarZoom(1); }
    });

    // Ctrl + rueda: zoom (sin Ctrl, la rueda desplaza la página como siempre)
    visor.addEventListener('wheel', function (e) {
        if (!e.ctrlKey) { return; }
        e.preventDefault();
        cambiarZoom(zoom + (e.deltaY < 0 ? PASO_ZOOM : -PASO_ZOOM));
    }, { passive: false });

    // Arrastrar con el mouse para moverse por la página cuando está ampliada
    var arrastre = null;
    desplazable.addEventListener('mousedown', function (e) {
        if (e.button !== 0) { return; }
        arrastre = { x: e.clientX, y: e.clientY, izquierda: desplazable.scrollLeft, arriba: desplazable.scrollTop };
        desplazable.classList.add('arrastrando');
    });
    window.addEventListener('mousemove', function (e) {
        if (!arrastre) { return; }
        desplazable.scrollLeft = arrastre.izquierda - (e.clientX - arrastre.x);
        desplazable.scrollTop = arrastre.arriba - (e.clientY - arrastre.y);
    });
    window.addEventListener('mouseup', function () {
        arrastre = null;
        desplazable.classList.remove('arrastrando');
    });

    botonPantalla.addEventListener('click', function () {
        if (document.fullscreenElement) {
            document.exitFullscreen();
        } else if (visor.requestFullscreen) {
            visor.requestFullscreen();
        }
    });

    // Al cambiar el tamaño de la ventana o entrar y salir de pantalla completa, se vuelve a ajustar la página
    var temporizador = null;
    function reajustar() {
        clearTimeout(temporizador);
        temporizador = setTimeout(function () { if (documento) { dibujar(pagina); } }, 150);
    }
    window.addEventListener('resize', reajustar);
    document.addEventListener('fullscreenchange', reajustar);
})();
