/**
 * Abre sola la ventana indicada en el atributo data-abrir-modal del contenido de la página (por ejemplo cuando se llega
 * con ?nuevo desde otra pantalla) y quita ese parámetro de la dirección, para que al recargar la ventana no vuelva a
 * abrirse.
 */
(function () {
    'use strict';

    function limpiar() {
        var direccion = new URL(window.location.href);
        ['nuevo', 'accion', 'id', 'registrarPara', 'asignar'].forEach(function (parametro) { direccion.searchParams.delete(parametro); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    var principal = document.querySelector('[data-abrir-modal]');
    var idVentana = principal && principal.dataset.abrirModal;
    var ventana = idVentana && document.getElementById(idVentana);
    if (idVentana && !ventana) {
        // Las ventanas de edición se piden al abrirlas (core/modalModificar.js): se abre con el botón de esa fila
        var boton = document.querySelector('[data-modal-url][data-bs-target="#' + idVentana + '"]');
        // La ventana de baja es una sola por pantalla (core/modalBaja.js): se abre con el botón de baja de esa fila
        var baja = idVentana.match(/^(modalBaja[A-Za-z]+)([0-9]+)$/);
        if (!boton && baja) {
            boton = Array.prototype.find.call(document.querySelectorAll('[data-bs-target="#' + baja[1] + '"]'), function (b) {
                return (b.dataset.bajaAccion || '').slice(-(baja[2].length + 1)) === '/' + baja[2];
            });
        }
        if (boton) { boton.click(); limpiar(); return; }
    }
    if (!ventana || !window.bootstrap) { return; }
    window.bootstrap.Modal.getOrCreateInstance(ventana).show();
    var direccion = new URL(window.location.href);
    ['nuevo', 'accion', 'id', 'registrarPara', 'asignar'].forEach(function (parametro) { direccion.searchParams.delete(parametro); });
    window.history.replaceState(null, '', direccion.pathname + direccion.search);
})();
