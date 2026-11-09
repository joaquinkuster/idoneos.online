/**
 * Ventana "Asignar programa" de una cohorte pendiente de contenido: los programas siempre están completos, así que la cohorte pasa a
 * inscripción próxima al recibir el suyo y se publica en el catálogo. Asignar el programa finaliza el diseño de la cohorte, por eso el
 * formulario pide una confirmación (data-confirmar-*, ver core/formulario.js) con la cohorte y el programa elegidos.
 */
(function () {
    'use strict';

    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    document.querySelectorAll('form[data-asignar-programa]').forEach(function (formulario) {
        var select = formulario.querySelector('select[name="programaId"]');
        var cohorte = formulario.getAttribute('data-cohorte-nombre') || 'la cohorte';

        function configurar() {
            var opcion = select.selectedOptions[0];
            var programa = opcion && opcion.value ? opcion.textContent.replace(/\s*\((Vigente|Anterior)\)\s*$/, '').trim() : 'elegido';
            formulario.dataset.confirmarTitulo = '¿Deseás finalizar el diseño?';
            formulario.dataset.confirmarBoton = 'Finalizar diseño';
            formulario.dataset.confirmarHtml = 'El programa «' + esc(programa) + '» se asignará a la cohorte «' + esc(cohorte) + '», que quedará publicada en el catálogo. '
                + 'Lo publicado es un compromiso de la empresa con los alumnos que lo consultan o ya se inscribieron, por eso ya no se podrán modificar '
                + 'los datos del curso. <strong>Si te equivocás, solicitá al administrador la baja de la cohorte para crearla de nuevo.</strong>';
        }

        select.addEventListener('change', configurar);
        formulario.addEventListener('submit', configurar);
        configurar();
    });
})();
