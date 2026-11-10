/** Ventanas de edición que se piden al servidor cuando se las necesita. */
(function () {
    'use strict';

    var cargando = false;

    function iniciar(ventana) {
        ventana.querySelectorAll('[data-duracion]').forEach(function (c) { if (window.DuracionIniciar) { window.DuracionIniciar(c); } });
        ventana.querySelectorAll('form[data-pasos]').forEach(function (f) { if (window.PanelIniciarPasos) { window.PanelIniciarPasos(f); } });
        if (window.iniciarMultiselect) { window.iniciarMultiselect(ventana); }
        // Cada página inicia lo suyo (por ejemplo las preguntas de un pool o los pools de una autoevaluación) cuando llega la ventana
        document.dispatchEvent(new CustomEvent('ventana-cargada', { detail: { ventana: ventana } }));
        // El resumen toma como "antes" lo que hay al terminar de armarse el formulario.
        ventana.querySelectorAll('form[data-resumen]').forEach(function (f) { if (window.HojaDeRutaIniciar) { window.HojaDeRutaIniciar(f); } });
    }

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-modal-url]');
        if (!boton || cargando) { return; }
        e.preventDefault();
        e.stopPropagation();
        var id = (boton.getAttribute('data-bs-target') || '').replace('#', '');
        cargando = true;
        boton.disabled = true;
        // Si la ventana tarda, mientras llega se muestra su skeleton (con respuestas rápidas ni se ve)
        var esqueleto = null;
        var espera = setTimeout(function () {
            esqueleto = document.createElement('div');
            esqueleto.className = 'pn-sk-ventana';
            esqueleto.setAttribute('aria-busy', 'true');
            esqueleto.innerHTML = '<div class="pn-sk-ventana-caja" aria-hidden="true"><span class="pn-sk pn-sk-titulo"></span>'
                + '<span class="pn-sk pn-sk-etiqueta"></span><span class="pn-sk pn-sk-campo"></span>'
                + '<span class="pn-sk pn-sk-etiqueta"></span><span class="pn-sk pn-sk-campo"></span>'
                + '<span class="pn-sk pn-sk-etiqueta"></span><span class="pn-sk pn-sk-campo"></span>'
                + '<div class="pn-sk-pie"><span class="pn-sk"></span><span class="pn-sk"></span></div></div>';
            document.body.appendChild(esqueleto);
        }, 150);
        fetch(boton.getAttribute('data-modal-url'), { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' })
            .then(function (r) { if (!r.ok) { throw new Error('No se pudo abrir la ventana.'); } return r.text(); })
            .then(function (html) {
                clearTimeout(espera);
                if (esqueleto) { esqueleto.remove(); esqueleto = null; }
                var anterior = document.getElementById(id);
                if (anterior) {
                    var inst = window.bootstrap && bootstrap.Modal.getInstance(anterior);
                    if (inst) { inst.dispose(); }
                    anterior.remove();
                }
                var envoltorio = document.createElement('div');
                envoltorio.innerHTML = html.trim();
                var ventana = envoltorio.firstElementChild;
                document.body.appendChild(ventana);
                iniciar(ventana);
                bootstrap.Modal.getOrCreateInstance(ventana).show();
            })
            .catch(function () { clearTimeout(espera); if (esqueleto) { esqueleto.remove(); } window.location.reload(); })
            .then(function () { cargando = false; boton.disabled = false; });
    }, true);
})();
