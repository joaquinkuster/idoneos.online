/** Perfil del usuario: foto (cambiar y quitar) y validación del teléfono. */
(function () {
    'use strict';

    // ----------------------------------------------------------------------
    // Foto de perfil
    // ----------------------------------------------------------------------

    var formulario = document.querySelector('form[data-validador="perfil"]');
    if (!formulario) {
        return;
    }
    var archivo = formulario.querySelector('input[type="file"]');
    var quitar = formulario.querySelector('[data-quitar-imagen]');
    var vista = formulario.querySelector('.pn-vista-previa');

    // Elegir una foto nueva cancela el pedido de quitar la actual
    archivo.addEventListener('change', function () {
        if (archivo.files && archivo.files.length) {
            quitar.value = 'false';
        }
    });

    formulario.querySelector('[data-quitar-foto]').addEventListener('click', function () {
        archivo.value = '';
        quitar.value = 'true';
        window.PanelValidacion.marcar(archivo, '');
        if (vista.dataset.urlTemporal) {
            URL.revokeObjectURL(vista.dataset.urlTemporal);
            delete vista.dataset.urlTemporal;
        }
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
