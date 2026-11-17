/** Compresión de imágenes antes de subirlas: las fotos pesadas se recortan y se achican en el navegador, así viajan y se guardan rápido. */
(function () {
    'use strict';

    var LADO_MAXIMO = 640;
    var CALIDAD = 0.85;
    var TIPOS = ['image/jpeg', 'image/png', 'image/webp'];

    /** Recorta la imagen al cuadrado central, la achica y la devuelve como un archivo JPG. */
    function comprimir(archivo) {
        return createImageBitmap(archivo).then(function (imagen) {
            var lado = Math.min(imagen.width, imagen.height);
            var destino = Math.min(lado, LADO_MAXIMO);
            var lienzo = document.createElement('canvas');
            lienzo.width = destino;
            lienzo.height = destino;
            var contexto = lienzo.getContext('2d');
            contexto.fillStyle = '#FFFFFF'; // lo transparente de un PNG queda blanco en el JPG
            contexto.fillRect(0, 0, destino, destino);
            contexto.drawImage(imagen, (imagen.width - lado) / 2, (imagen.height - lado) / 2, lado, lado, 0, 0, destino, destino);
            if (imagen.close) { imagen.close(); }
            return new Promise(function (resolver, rechazar) {
                lienzo.toBlob(function (blob) {
                    if (!blob) { rechazar(new Error('No se pudo comprimir la imagen.')); return; }
                    resolver(new File([blob], 'foto.jpg', { type: 'image/jpeg', lastModified: Date.now() }));
                }, 'image/jpeg', CALIDAD);
            });
        });
    }

    function continuar(campo) {
        // El evento que se vuelve a lanzar pasa sin comprimirse otra vez y sigue su camino hacia la validación
        campo.dataset.comprimido = '1';
        campo.dispatchEvent(new Event('change', { bubbles: true }));
    }

    // En captura, antes que la validación del formulario: la imagen ya comprimida es la que se valida y se previsualiza
    document.addEventListener('change', function (e) {
        var campo = e.target;
        if (!campo.matches || !campo.matches('input[type="file"][data-comprimir-imagen]')) { return; }
        if (campo.dataset.comprimido) { delete campo.dataset.comprimido; return; }
        var archivo = campo.files && campo.files[0];
        if (!archivo || TIPOS.indexOf(archivo.type) === -1 || !window.createImageBitmap) { return; }
        e.stopImmediatePropagation();
        comprimir(archivo).then(function (nuevo) {
            var lista = new DataTransfer();
            lista.items.add(nuevo);
            campo.files = lista.files;
            continuar(campo);
        }).catch(function () { continuar(campo); });
    }, true);
})();
