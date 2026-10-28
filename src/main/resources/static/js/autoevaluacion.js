/**
 * Formulario de autoevaluaciones (CU-65 y CU-66).
 *
 * En el alta, los pools que se ofrecen dependen del tema elegido: los del propio tema y los de los temas
 * anteriores del programa, nunca los de las posteriores. Los pools se muestran agrupados por tema. Cada opción de pool
 * lleva en data-temas los temas que pueden usarla. El selector de pools es un Tom Select que inicia panel.js al abrir
 * la ventana.
 */
(function () {
    'use strict';

    function filtrarPools(formulario) {
        var tema = formulario.querySelector('[data-autoevaluacion-tema]');
        var pools = formulario.querySelector('select[data-autoevaluacion-pools]');
        if (!tema || !pools || pools.dataset.filtroListo) {
            return;
        }
        pools.dataset.filtroListo = '1';
        // Todas las opciones con su tema (el optgroup en el que están), en el orden del cronograma
        var todas = Array.prototype.map.call(pools.options, function (o) {
            var grupo = o.parentElement && o.parentElement.tagName === 'OPTGROUP' ? o.parentElement.label : '';
            return { value: o.value, text: o.textContent, grupo: grupo, temas: (o.getAttribute('data-temas') || '').split(',') };
        });

        function aplicar() {
            var permitidas = todas.filter(function (o) { return o.temas.indexOf(tema.value) >= 0; });
            var ts = pools.tomselect;
            if (ts) {
                var elegidas = ts.items.slice().filter(function (v) {
                    return permitidas.some(function (o) { return o.value === v; });
                });
                ts.settings.lockOptgroupOrder = true;
                ts.clear(true);
                ts.clearOptions();
                ts.clearOptionGroups();
                permitidas.forEach(function (o) {
                    if (o.grupo && !ts.optgroups[o.grupo]) { ts.addOptionGroup(o.grupo, { label: o.grupo, value: o.grupo }); }
                    ts.addOption({ value: o.value, text: o.text, optgroup: o.grupo || undefined });
                });
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
        tema.addEventListener('change', aplicar);
        aplicar();
    }

    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('shown.bs.modal', function () {
            modal.querySelectorAll('form[data-autoevaluacion-formulario]').forEach(filtrarPools);
        });
    });

    // Apertura automatica de la ventana de una autoevaluacion (?accion=editar|baja&autoevaluacionId=...), desde la vista del curso
    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && principal.dataset.abrirModal && document.getElementById(principal.dataset.abrirModal)) {
        bootstrap.Modal.getOrCreateInstance(document.getElementById(principal.dataset.abrirModal)).show();
    }
})();
