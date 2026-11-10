/** Formulario de autoevaluaciones. */
(function () {
    'use strict';

    function agruparPools(formulario) {
        var tema = formulario.querySelector('[data-autoevaluacion-tema]');
        var pools = formulario.querySelector('select[data-autoevaluacion-pools]');
        if (!pools || pools.dataset.filtroListo) {
            return;
        }
        pools.dataset.filtroListo = '1';
        // Todas las opciones con el tema de su pool
        var todas = Array.prototype.map.call(pools.options, function (o) {
            return { value: o.value, text: o.textContent, tema: o.getAttribute('data-tema-pool') || '' };
        });

        // En el alta el tema se elige en el formulario. Al editar es el de la autoevaluación
        function temaActual() {
            return tema ? tema.value : (formulario.dataset.temaId || '');
        }

        function aplicar() {
            var ts = pools.tomselect;
            if (!ts) { return; }
            var actual = temaActual();
            var propios = todas.filter(function (o) { return actual && o.tema === actual; });
            var otros = todas.filter(function (o) { return !actual || o.tema !== actual; });
            var elegidas = ts.items.slice();
            ts.settings.lockOptgroupOrder = true;
            ts.clear(true);
            ts.clearOptions();
            ts.clearOptionGroups();
            [['propio', 'Tu tema', propios], ['otros', actual ? 'Otros temas' : 'Pools del curso', otros]].forEach(function (grupo) {
                if (!grupo[2].length) { return; }
                ts.addOptionGroup(grupo[0], { label: grupo[1], value: grupo[0] });
                grupo[2].forEach(function (o) { ts.addOption({ value: o.value, text: o.text, optgroup: grupo[0] }); });
            });
            ts.addItems(elegidas, true);
            ts.refreshOptions(false);
        }
        if (tema) { tema.addEventListener('change', aplicar); }
        aplicar();
    }

    // Se inicia todo al cargar la pagina (con las ventanas aun cerradas): asi el selector ya esta armado y agrupado
    // cuando se abre la ventana y no se ve cargarse despues
    if (window.iniciarMultiselect) { window.iniciarMultiselect(document); }
    document.querySelectorAll('form[data-autoevaluacion-formulario]').forEach(agruparPools);
    // La ventana de modificación llega después de la carga (core/modalModificar.js)
    document.addEventListener('ventana-cargada', function (e) {
        e.detail.ventana.querySelectorAll('form[data-autoevaluacion-formulario]').forEach(agruparPools);
    });
})();
