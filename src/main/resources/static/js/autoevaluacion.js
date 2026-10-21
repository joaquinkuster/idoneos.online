/**
 * Formulario de autoevaluaciones (CU-65 y CU-66).
 *
 * En el alta, los pools que se ofrecen dependen de la unidad elegida: los de la propia unidad y, si es la última unidad del
 * programa, los de las otras unidades (evaluación final). Cada opción de pool lleva en data-unidades las unidades que
 * pueden usarla. El selector de pools es un Tom Select que inicia panel.js al abrir la ventana.
 */
(function () {
    'use strict';

    function filtrarPools(formulario) {
        var unidad = formulario.querySelector('[data-autoevaluacion-unidad]');
        var pools = formulario.querySelector('select[data-autoevaluacion-pools]');
        if (!unidad || !pools || pools.dataset.filtroListo) {
            return;
        }
        pools.dataset.filtroListo = '1';
        var todas = Array.prototype.map.call(pools.options, function (o) {
            return { value: o.value, text: o.textContent, unidades: (o.getAttribute('data-unidades') || '').split(',') };
        });

        function aplicar() {
            var permitidas = todas.filter(function (o) { return o.unidades.indexOf(unidad.value) >= 0; });
            var ts = pools.tomselect;
            if (ts) {
                var elegidas = ts.items.slice().filter(function (v) {
                    return permitidas.some(function (o) { return o.value === v; });
                });
                ts.clear(true);
                ts.clearOptions();
                permitidas.forEach(function (o) { ts.addOption({ value: o.value, text: o.text }); });
                ts.addItems(elegidas, true);
                ts.refreshOptions(false);
            } else {
                Array.prototype.forEach.call(pools.options, function (o) {
                    var ok = permitidas.some(function (p) { return p.value === o.value; });
                    o.hidden = !ok;
                    o.disabled = !ok;
                    if (!ok) { o.selected = false; }
                });
            }
        }
        unidad.addEventListener('change', aplicar);
        aplicar();
    }

    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('shown.bs.modal', function () {
            modal.querySelectorAll('form[data-autoevaluacion-formulario]').forEach(filtrarPools);
        });
    });
})();
