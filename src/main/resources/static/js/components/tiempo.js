/**
 * Campos de tiempo escritos en partes (fragments/campoTiempo.html): horas y minutos, o meses, semanas y días.
 *
 * Cada campo tiene un total oculto (el que se envía al servidor, en minutos o en días) y una casilla por parte. Al escribir,
 * el total se recalcula y una línea debajo lo muestra ("= 400 minutos", o 0 si no se escribió nada); al salir de una casilla, las partes se normalizan
 * (6 h 90 min pasan a 7 h 30 min). Las casillas vacías cuentan como 0; si todas están vacías, el total queda vacío.
 * El total guarda también su texto ("6 h 40 min") para el resumen del formulario.
 */
(function () {
    'use strict';

    // Cuánto vale, en la unidad de guardado (minutos o días), cada parte de izquierda a derecha
    var FACTORES = { minutos: [60, 1], dias: [30, 7, 1] };
    var ETIQUETAS = { minutos: ['h', 'min'], dias: ['mes', 'sem', 'días'] };
    var UNIDAD_TOTAL = { minutos: 'minutos', dias: 'días' };

    function partesDe(contenedor) {
        return Array.prototype.slice.call(contenedor.querySelectorAll('[data-tiempo-parte]'));
    }

    /** Suma las partes; null si todas están vacías. */
    function calcular(contenedor) {
        var factores = FACTORES[contenedor.dataset.tiempo];
        var total = 0;
        var hayAlgo = false;
        partesDe(contenedor).forEach(function (parte, i) {
            var texto = parte.value.trim();
            if (texto === '') { return; }
            hayAlgo = true;
            total += Math.max(0, parseInt(texto, 10) || 0) * factores[i];
        });
        return hayAlgo ? total : null;
    }

    /** Reparte un total en partes, de la más grande a la más chica. */
    function repartir(total, factores) {
        var resto = total;
        return factores.map(function (factor) {
            var cantidad = Math.floor(resto / factor);
            resto -= cantidad * factor;
            return cantidad;
        });
    }

    function texto(total, tipo) {
        var etiquetas = ETIQUETAS[tipo];
        var partes = repartir(total, FACTORES[tipo]).map(function (cantidad, i) {
            return cantidad > 0 ? cantidad + ' ' + etiquetas[i] : '';
        }).filter(Boolean);
        return partes.length ? partes.join(' ') : '0 ' + etiquetas[etiquetas.length - 1];
    }

    function actualizar(contenedor, normalizar) {
        var tipo = contenedor.dataset.tiempo;
        var campoTotal = contenedor.querySelector('[data-tiempo-total]');
        var total = calcular(contenedor);
        campoTotal.value = total === null ? '' : String(total);
        campoTotal.dataset.texto = total === null ? '' : texto(total, tipo);
        var linea = contenedor.querySelector('[data-tiempo-eq]');
        if (linea) { linea.innerHTML = '= <b>' + (total === null ? 0 : total) + '</b> ' + UNIDAD_TOTAL[tipo]; }
        if (normalizar && total !== null) {
            var nuevas = repartir(total, FACTORES[tipo]);
            partesDe(contenedor).forEach(function (parte, i) { parte.value = String(nuevas[i]); });
        }
    }

    document.addEventListener('input', function (e) {
        var parte = e.target.closest ? e.target.closest('[data-tiempo-parte]') : null;
        if (!parte) { return; }
        var contenedor = parte.closest('.pn-tiempo');
        actualizar(contenedor, false);
        // Si el campo tenía un error, se limpia al escribir
        var total = contenedor.querySelector('[data-tiempo-total]');
        if (window.PanelValidacion && total.classList.contains('es-invalido')) { window.PanelValidacion.marcar(total, ''); }
    });

    document.addEventListener('change', function (e) {
        var parte = e.target.closest ? e.target.closest('[data-tiempo-parte]') : null;
        if (parte) { actualizar(parte.closest('.pn-tiempo'), true); }
    });

    document.querySelectorAll('.pn-tiempo').forEach(function (contenedor) { actualizar(contenedor, false); });
})();
