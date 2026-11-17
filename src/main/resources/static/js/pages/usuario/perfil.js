/** Perfil del usuario: foto (vista previa, cambiar y quitar) y validación del teléfono. */
(function () {
    'use strict';

    var TIPOS = ['image/jpeg', 'image/png', 'image/webp'];
    var TAMANIO_MAXIMO = 2 * 1024 * 1024;

    var formulario = document.querySelector('form[data-validador="perfil"]');
    if (!formulario) {
        return;
    }
    var archivo = formulario.querySelector('input[type="file"]');
    var quitar = formulario.querySelector('[data-quitar-imagen]');
    var vista = formulario.querySelector('.pn-vista-previa');

    // ----------------------------------------------------------------------
    // Foto de perfil
    // ----------------------------------------------------------------------

    function liberarVistaTemporal() {
        if (vista.dataset.urlTemporal) {
            URL.revokeObjectURL(vista.dataset.urlTemporal);
            delete vista.dataset.urlTemporal;
        }
    }

    // Elegir una foto nueva cancela el pedido de quitar la actual y la muestra enseguida en la ficha
    archivo.addEventListener('change', function () {
        var elegida = archivo.files && archivo.files[0];
        if (!elegida) { return; }
        quitar.value = 'false';
        // Si la validación del formulario rechaza la imagen (formato o tamaño), se conserva la que había
        setTimeout(function () {
            if (archivo.classList.contains('es-invalido') || !archivo.files || !archivo.files[0]) { return; }
            var nueva = archivo.files[0];
            if (TIPOS.indexOf(nueva.type) === -1 || nueva.size > TAMANIO_MAXIMO) { return; }
            liberarVistaTemporal();
            vista.dataset.urlTemporal = URL.createObjectURL(nueva);
            vista.src = vista.dataset.urlTemporal;
            vista.classList.remove('d-none');
        }, 0);
    });

    formulario.querySelector('[data-quitar-foto]').addEventListener('click', function () {
        archivo.value = '';
        quitar.value = 'true';
        window.PanelValidacion.marcar(archivo, '');
        liberarVistaTemporal();
        delete vista.dataset.original;
        vista.removeAttribute('src');
        vista.classList.add('d-none');
    });

    // ----------------------------------------------------------------------
    // Validación
    // ----------------------------------------------------------------------

    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.perfil = function (paso, form) {
        var telefono = form.elements.telefono;
        if (telefono && telefono.value.trim() && !/^[0-9+()\s-]{6,20}$/.test(telefono.value.trim())) {
            window.PanelValidacion.marcar(telefono, 'No es válido. Formato válido: entre 6 y 20 dígitos, con +, espacios o guiones.');
            return false;
        }
        return true;
    };
})();
