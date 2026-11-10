/** Vista Versiones del listado de programas: dibuja las versiones en ramas, de izquierda a derecha por fecha. Solo se mira, no se opera. */
(function () {
    'use strict';

    var ANCHO = 216;
    var ALTO = 100;
    var SALTO_X = 292;
    var SALTO_Y = 124;
    var MARGEN = 28;

    var vista = document.querySelector('[data-vista-versiones]');
    var conmutador = document.querySelector('[data-conmutador-vista]');
    var tabla = document.querySelector('[data-vista-tabla]');
    var datos = window.PROGRAMA_VERSIONES || [];
    if (!vista || !conmutador || !tabla) { return; }

    /** Ordena las versiones en ramas: la primera derivada sigue en la fila de su origen y las otras abren una fila nueva. */
    function ubicar(versiones) {
        var porId = {};
        versiones.forEach(function (v) { porId[v.id] = v; v.hijos = []; });
        var raices = [];
        versiones.forEach(function (v) {
            if (v.anterior && porId[v.anterior]) { porId[v.anterior].hijos.push(v); } else { raices.push(v); }
        });
        var siguienteFila = 0;
        function colocar(v, columna, fila) {
            v.columna = columna;
            v.fila = fila;
            v.hijos.forEach(function (hijo, i) {
                colocar(hijo, columna + 1, i === 0 ? fila : ++siguienteFila);
            });
        }
        raices.forEach(function (raiz) {
            colocar(raiz, 0, siguienteFila);
            siguienteFila++;
        });
        return versiones;
    }

    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function dibujar() {
        var versiones = ubicar(datos.map(function (v) { return Object.assign({}, v); }));
        var porId = {};
        versiones.forEach(function (v) { porId[v.id] = v; });
        var columnas = 0;
        var filas = 0;
        versiones.forEach(function (v) { columnas = Math.max(columnas, v.columna + 1); filas = Math.max(filas, v.fila + 1); });
        var ancho = MARGEN * 2 + (columnas - 1) * SALTO_X + ANCHO;
        var alto = MARGEN * 2 + (filas - 1) * SALTO_Y + ALTO;

        var grafico = document.createElement('div');
        grafico.className = 'pv-grafico';
        grafico.style.width = ancho + 'px';
        grafico.style.height = alto + 'px';
        var trazos = '';
        versiones.forEach(function (v) {
            var origen = porId[v.anterior];
            if (!origen) { return; }
            var x1 = MARGEN + origen.columna * SALTO_X + ANCHO;
            var y1 = MARGEN + origen.fila * SALTO_Y + ALTO / 2;
            var x2 = MARGEN + v.columna * SALTO_X;
            var y2 = MARGEN + v.fila * SALTO_Y + ALTO / 2;
            var medio = (x2 - x1) / 2;
            var d = y1 === y2 ? 'M' + x1 + ' ' + y1 + ' L' + x2 + ' ' + y2
                : 'M' + x1 + ' ' + y1 + ' C' + (x1 + medio) + ' ' + y1 + ',' + (x1 + medio) + ' ' + y2 + ',' + x2 + ' ' + y2;
            var demora = (v.columna * 0.18 + 0.12) + 's';
            // La línea termina en una flecha que apunta a la versión que deriva
            trazos += '<path class="pv-linea entra" style="--largo:340;--demora:' + demora + '" d="' + d + '"/>'
                + '<path class="pv-punta nodo-punta" style="--demora:' + demora + '" d="M' + (x2 - 8) + ' ' + (y2 - 5) + ' L' + x2 + ' ' + y2 + ' L' + (x2 - 8) + ' ' + (y2 + 5) + '"/>';
        });
        grafico.innerHTML = '<svg class="pv-svg" aria-hidden="true">' + trazos + '</svg>';
        versiones.forEach(function (v) {
            var nodo = document.createElement('div');
            nodo.className = 'pv-nodo entra' + (v.vigente ? ' vigente' : '') + (v.cohortes === 0 ? ' sin-cohortes' : '');
            nodo.style.left = (MARGEN + v.columna * SALTO_X) + 'px';
            nodo.style.top = (MARGEN + v.fila * SALTO_Y) + 'px';
            nodo.style.setProperty('--demora', (v.columna * 0.18 + v.fila * 0.08) + 's');
            nodo.innerHTML = '<div class="pv-nodo-nombre" title="' + esc(v.nombre) + '">' + esc(v.nombre) + '</div>'
                + '<div class="pv-nodo-dato">' + v.temas + (v.temas === 1 ? ' tema · ' : ' temas · ') + esc(v.fecha) + '</div>'
                + '<div class="pv-nodo-dato">' + (v.cohortes === 0 ? 'sin cohortes' : v.cohortes + (v.cohortes === 1 ? ' cohorte' : ' cohortes')) + '</div>'
                + (v.vigente ? '<span class="pv-nodo-chip">Vigente</span>' : '');
            grafico.appendChild(nodo);
        });
        var envoltura = vista.querySelector('[data-versiones-grafico]');
        envoltura.innerHTML = '';
        envoltura.appendChild(grafico);
        // La animación dibuja cada línea completa: se usa el largo real de cada una
        grafico.querySelectorAll('.pv-linea').forEach(function (linea) {
            linea.style.setProperty('--largo', Math.ceil(linea.getTotalLength()) + 2);
        });
    }

    function mostrar(nombre) {
        var versiones = nombre === 'versiones';
        conmutador.querySelectorAll('button').forEach(function (b) { b.classList.toggle('activa', b.dataset.vista === nombre); });
        tabla.hidden = versiones;
        vista.hidden = !versiones;
        if (versiones) { dibujar(); }
    }

    conmutador.addEventListener('click', function (e) {
        var boton = e.target.closest('button[data-vista]');
        if (boton) { mostrar(boton.dataset.vista); }
    });
})();
