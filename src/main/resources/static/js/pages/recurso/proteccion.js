/**
 * Protección del contenido de los materiales (MOD-F-02): el material se ve solo dentro de la plataforma.
 *
 *  - Sin menú contextual, sin arrastrar ni seleccionar el contenido, y sin los atajos de guardar, imprimir o ver el
 *    código de la página.
 *  - Marca de agua con el nombre y el correo de quien mira (y la fecha) sobre el visor de PDF y el reproductor: no
 *    impide capturar la pantalla, pero deja identificada cualquier filtración.
 *  - Pantalla completa propia del reproductor, para que la marca de agua siga a la vista.
 *
 * Ningún mecanismo del navegador puede impedir del todo la captura de pantalla o la copia del pedido desde las
 * herramientas del navegador: estas medidas cierran los caminos habituales y disuaden el resto.
 */
(function () {
    'use strict';

    var contenedor = document.querySelector('.mv-contenedor');
    if (!contenedor) { return; }

    // ------------------------------------------------------------------
    // Sin menú contextual, arrastre ni selección
    // ------------------------------------------------------------------
    ['contextmenu', 'dragstart', 'selectstart', 'copy', 'cut'].forEach(function (evento) {
        contenedor.addEventListener(evento, function (e) { e.preventDefault(); });
    });

    // Atajos de guardar (Ctrl+S), imprimir (Ctrl+P) y ver el código de la página (Ctrl+U)
    document.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && ['s', 'p', 'u'].indexOf(e.key.toLowerCase()) !== -1) {
            e.preventDefault();
        }
    });

    // ------------------------------------------------------------------
    // Marca de agua
    // ------------------------------------------------------------------
    function escapar(texto) {
        return texto.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function ponerMarca(superficie, color) {
        var marca = document.createElement('div');
        marca.className = 'mv-marca';
        marca.setAttribute('aria-hidden', 'true');
        var texto = escapar((contenedor.dataset.marca || '') + ' · ' + new Date().toLocaleDateString('es-AR'));
        var svg = '<svg xmlns="http://www.w3.org/2000/svg" width="420" height="220">'
            + '<text x="210" y="110" text-anchor="middle" transform="rotate(-24 210 110)" font-family="Arial, sans-serif" '
            + 'font-size="15" font-weight="bold" fill="' + color + '">' + texto + '</text></svg>';
        marca.style.backgroundImage = 'url("data:image/svg+xml;utf8,' + encodeURIComponent(svg) + '")';
        superficie.appendChild(marca);
    }

    var lienzo = contenedor.querySelector('.mv-lienzo');
    if (lienzo) { ponerMarca(lienzo, 'rgba(15,23,42,0.18)'); }
    var reproductor = contenedor.querySelector('.mv-reproductor');
    if (reproductor) { ponerMarca(reproductor, 'rgba(255,255,255,0.28)'); }

    // ------------------------------------------------------------------
    // Pantalla completa del reproductor (la nativa queda deshabilitada para conservar la marca de agua)
    // ------------------------------------------------------------------
    var botonPantalla = contenedor.querySelector('[data-video-pantalla-completa]');
    if (botonPantalla && reproductor) {
        botonPantalla.addEventListener('click', function () {
            if (document.fullscreenElement) {
                document.exitFullscreen();
            } else if (reproductor.requestFullscreen) {
                reproductor.requestFullscreen();
            }
        });
    }
})();
