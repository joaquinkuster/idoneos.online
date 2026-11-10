/** Comportamiento de la vista del glosario. */
(function () {
    'use strict';

    var principal = document.querySelector('[data-abrir-modal]');
    if (!principal) { return; }
    var ventana = document.getElementById(principal.dataset.abrirModal);
    if (ventana && window.bootstrap) {
        window.bootstrap.Modal.getOrCreateInstance(ventana).show();
        // Se quita "nuevo" de la dirección: al recargar después de registrar, el formulario no vuelve a abrirse
        var direccion = new URL(window.location.href);
        direccion.searchParams.delete('nuevo');
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }
})();
