/**
 * Comportamiento del ABM de temas (MOD-F-02).
 *
 *  1. Si la página se abre con ?nuevo, o con ?accion=editar|baja&temaId=…, abre sola la ventana correspondiente
 *     (son los destinos del botón "Registrar tema" y del menú de cada tema de la vista del curso). Se quita esa
 *     parte de la dirección para que, al recargar después de la operación, la ventana no vuelva a abrirse.
 *  2. Formulario de alta: elegir "un tema de otro programa del curso" cambia los campos que pide, y la lista de
 *     temas a incorporar muestra solo las que todavía no están en el programa elegido.
 */
(function () {
    'use strict';

    var principal = document.querySelector('[data-abrir-modal]');
    var ventana = principal && document.getElementById(principal.dataset.abrirModal);
    if (ventana && window.bootstrap) {
        window.bootstrap.Modal.getOrCreateInstance(ventana).show();
        var direccion = new URL(window.location.href);
        ['nuevo', 'accion', 'temaId'].forEach(function (nombre) { direccion.searchParams.delete(nombre); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    var formulario = document.querySelector('[data-tema-formulario]');
    if (!formulario) { return; }
    var modo = formulario.querySelector('[data-tema-modo]');
    var programa = formulario.querySelector('[data-tema-programa]');
    var existente = formulario.querySelector('[data-tema-existente]');
    var aviso = formulario.querySelector('[data-tema-sin-opciones]');

    /** Muestra solo los temas que no están ya en el cronograma del programa elegido. */
    function filtrarIncorporables() {
        var disponibles = 0;
        Array.prototype.forEach.call(existente.options, function (opcion) {
            if (!opcion.value) { return; }
            var programas = opcion.dataset.programas ? opcion.dataset.programas.split(',') : [];
            var yaEsta = programas.indexOf(programa.value) !== -1;
            opcion.hidden = yaEsta;
            opcion.disabled = yaEsta;
            if (!yaEsta) { disponibles++; }
        });
        if (existente.selectedOptions.length && existente.selectedOptions[0].disabled) { existente.value = ''; }
        aviso.hidden = disponibles > 0 || modo.value !== 'existente';
    }

    /** Adapta los campos al modo: tema nueva (título, contenido, descripción) o tema existente (un selector). */
    function aplicarModo() {
        var esExistente = modo.value === 'existente';
        formulario.querySelectorAll('[data-bloque="nueva"]').forEach(function (bloque) {
            bloque.hidden = esExistente;
            bloque.querySelectorAll('input, textarea, select').forEach(function (campo) {
                campo.disabled = esExistente;
                if (campo.hasAttribute('data-requerido')) { campo.required = !esExistente; }
                if (esExistente) { window.PanelValidacion.marcar(campo, ''); }
            });
        });
        formulario.querySelector('[data-bloque="existente"]').hidden = !esExistente;
        existente.disabled = !esExistente;
        existente.required = esExistente;
        if (!esExistente) { window.PanelValidacion.marcar(existente, ''); }
        filtrarIncorporables();
    }

    modo.addEventListener('change', aplicarModo);
    programa.addEventListener('change', filtrarIncorporables);
    aplicarModo();
})();
