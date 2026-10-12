/**
 * Abre sola la ventana indicada en el atributo data-abrir-modal del contenido de la página (por ejemplo cuando se llega
 * con ?nuevo desde otra pantalla) y quita ese parámetro de la dirección, para que al recargar la ventana no vuelva a
 * abrirse.
 */
(function () {
    'use strict';

    var principal = document.querySelector('[data-abrir-modal]');
    var ventana = principal && document.getElementById(principal.dataset.abrirModal);
    if (!ventana || !window.bootstrap) { return; }
    window.bootstrap.Modal.getOrCreateInstance(ventana).show();
    var direccion = new URL(window.location.href);
    direccion.searchParams.delete('nuevo');
    window.history.replaceState(null, '', direccion.pathname + direccion.search);
})();
