/**
 * Pantalla del cronograma de un programa (MOD-F-02): consulta (CU-23) y modificación (CU-24).
 *
 *  - Vista por semanas: una fila por unidad con una barra que ocupa las semanas que dura. Vista por unidades: tabla.
 *  - Si se puede modificar: las filas se reordenan arrastrándolas por el asa, la duración se cambia arrastrando el borde
 *    derecho de la barra, las unidades se agregan arrastrándolas desde el panel de la izquierda y se quitan arrastrándolas
 *    de vuelta a ese panel. Nada se guarda hasta apretar "Modificar".
 *  - "Nueva unidad" lleva al ABM de unidades; si hay cambios sin guardar, antes pide confirmación.
 *  - El botón de información junto al título muestra u oculta el texto de ayuda.
 *
 * Los datos (unidades del curso y unidades del cronograma, en orden) vienen del servidor en el HTML.
 */
(function () {
    'use strict';

    var raiz = document.getElementById('cronograma');
    if (!raiz) { return; }

    var editable = raiz.dataset.editable === 'true';
    var COLORES = ['#2F5D8C', '#D4A03D', '#3B6D11', '#993C1D', '#534AB7', '#0F6E56'];
    var MINIMO_SEMANAS_VISIBLES = 9;

    var unidades = Array.prototype.map.call(raiz.querySelectorAll('[data-unidad]'), function (e, indice) {
        return { id: e.dataset.id, titulo: e.dataset.titulo, material: parseInt(e.dataset.material, 10) || 0, color: COLORES[indice % COLORES.length] };
    });
    var colocadas = Array.prototype.map.call(raiz.querySelectorAll('[data-colocada]'), function (e) {
        return { id: e.dataset.id, semanas: parseInt(e.dataset.semanas, 10) };
    });
    var inicial = JSON.stringify(colocadas);

    var grilla = raiz.querySelector('[data-grilla]');
    var disponibles = raiz.querySelector('[data-disponibles]');
    var buscar = raiz.querySelector('[data-buscar]');
    var resumen = raiz.querySelector('[data-resumen]');

    function unidadDe(id) {
        return unidades.filter(function (u) { return u.id === id; })[0];
    }

    function totalSemanas() {
        return colocadas.reduce(function (suma, c) { return suma + c.semanas; }, 0);
    }

    function hayCambios() {
        return JSON.stringify(colocadas) !== inicial;
    }

    function escapar(texto) {
        var div = document.createElement('div');
        div.textContent = texto;
        return div.innerHTML;
    }

    function textoSemanas(n) {
        return n + (n === 1 ? ' semana' : ' semanas');
    }

    function nombreCorto(titulo) {
        return titulo.split(' ').slice(0, 2).join(' ');
    }

    // ------------------------------------------------------------------ dibujo
    function dibujar() {
        var total = totalSemanas();
        var columnas = Math.max(total, MINIMO_SEMANAS_VISIBLES);
        var plantilla = '170px repeat(' + columnas + ', minmax(26px, 1fr))';

        var html = '<div class="cn-fila cn-cabecera-fila" style="grid-template-columns:' + plantilla + '"><div class="cn-celda-unidades">Unidades</div>';
        for (var s = 1; s <= columnas; s++) { html += '<div>' + s + '</div>'; }
        html += '</div>';

        var desde = 1;
        colocadas.forEach(function (c, i) {
            var u = unidadDe(c.id);
            html += '<div class="cn-fila" ' + (editable ? 'draggable="true" ' : '') + 'data-i="' + i + '" style="grid-template-columns:' + plantilla + ';--c:' + u.color + '">'
                + '<div class="cn-unidad" style="grid-row:1;grid-column:1"><span class="cn-numero">' + (i + 1) + '</span>'
                + '<div class="cn-tarjeta"><b>' + escapar(u.titulo) + '</b><small>' + textoSemanas(c.semanas) + '</small></div>'
                + (editable ? '<span class="cn-asa-fila" title="Arrastrar para reordenar"><i class="fa-solid fa-grip-vertical"></i></span>' : '') + '</div>';
            for (var k = 2; k <= columnas + 1; k++) { html += '<div class="cn-semana" style="grid-row:1;grid-column:' + k + '"></div>'; }
            html += '<div class="cn-barra" style="grid-column:' + (desde + 1) + ' / span ' + c.semanas + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + c.semanas + ' sem.'
                + (editable ? '<span class="cn-borde" data-i="' + i + '" title="Arrastrar para cambiar la duración"></span>' : '') + '</div></div>';
            desde += c.semanas;
        });
        grilla.innerHTML = html;

        var textoTotal = textoSemanas(total);
        raiz.ownerDocument.querySelectorAll('[data-total]').forEach(function (e) { e.textContent = textoTotal; });
        raiz.querySelector('[data-total-resumen]').textContent = 'Total: ' + textoTotal;
        resumen.innerHTML = colocadas.map(function (c) {
            var u = unidadDe(c.id);
            return '<span class="cn-pastilla" style="--c:' + u.color + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + c.semanas + ' sem.</span>';
        }).join('');

        // Vista por unidades
        var semanaActual = 0;
        raiz.querySelector('[data-tabla]').innerHTML = colocadas.length ? colocadas.map(function (c, i) {
            var u = unidadDe(c.id);
            var primera = semanaActual + 1;
            semanaActual += c.semanas;
            return '<tr><td>' + (i + 1) + '</td><td class="text-start"><span class="pn-celda-principal">' + escapar(u.titulo) + '</span></td>'
                + '<td>' + u.material + (u.material === 1 ? ' material' : ' materiales') + '</td><td>' + textoSemanas(c.semanas) + '</td>'
                + '<td>' + (primera === semanaActual ? 'Semana ' + primera : 'Semanas ' + primera + ' a ' + semanaActual) + '</td></tr>';
        }).join('') : '<tr><td colspan="5" class="pn-vacio-grande"><i class="fa-solid fa-calendar pn-vacio-icono"></i><h3>El cronograma está vacío</h3><p>Arrastrá unidades desde el panel izquierdo para armarlo.</p></td></tr>';

        if (editable) { dibujarDisponibles(); }
    }

    function dibujarDisponibles() {
        var filtro = (buscar.value || '').toLowerCase();
        disponibles.innerHTML = unidades.map(function (u) {
            if (filtro && u.titulo.toLowerCase().indexOf(filtro) < 0) { return ''; }
            var usada = colocadas.some(function (c) { return c.id === u.id; });
            return '<div class="cn-disponible' + (usada ? ' usada' : '') + '" ' + (usada ? '' : 'draggable="true" ') + 'data-u="' + u.id + '" style="--c:' + u.color + '">'
                + '<div><b>' + escapar(u.titulo) + '</b><small>' + (usada ? 'En el cronograma' : '1 semana al agregarla') + '</small></div>'
                + (usada ? '' : '<span class="cn-asa-fila"><i class="fa-solid fa-grip-vertical"></i></span>') + '</div>';
        }).join('') || '<p class="cn-vacio">No hay unidades que coincidan.</p>';
    }

    // ------------------------------------------------------------------ pestañas
    raiz.querySelectorAll('[data-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var porUnidades = boton.dataset.vista === 'unidades';
            raiz.querySelectorAll('[data-vista]').forEach(function (b) { b.classList.toggle('activa', b === boton); });
            grilla.hidden = porUnidades;
            var zona = raiz.querySelector('[data-zona]');
            if (zona) { zona.hidden = porUnidades; }
            raiz.querySelector('[data-vista-unidades]').hidden = !porUnidades;
        });
    });

    // Ayuda (solo al modificar): el botón de información muestra u oculta el texto explicativo
    var botonAyuda = raiz.querySelector('[data-ayuda]');
    var textoAyuda = raiz.querySelector('[data-ayuda-texto]');
    if (botonAyuda && textoAyuda) {
        botonAyuda.addEventListener('click', function () {
            textoAyuda.hidden = !textoAyuda.hidden;
            botonAyuda.setAttribute('aria-expanded', String(!textoAyuda.hidden));
            botonAyuda.classList.toggle('activa', !textoAyuda.hidden);
        });
    }

    if (!editable) {
        dibujar();
        return;
    }

    // ------------------------------------------------------------------ arrastrar y soltar
    var arrastre = null;

    function limpiarMarcas() {
        raiz.querySelectorAll('.sobre').forEach(function (e) { e.classList.remove('sobre'); });
    }

    raiz.addEventListener('dragstart', function (e) {
        var fila = e.target.closest('.cn-fila[data-i]');
        var disponible = e.target.closest('.cn-disponible[data-u]');
        if (fila) {
            arrastre = { tipo: 'fila', i: parseInt(fila.dataset.i, 10) };
            fila.classList.add('arrastrando');
        } else if (disponible) {
            arrastre = { tipo: 'disponible', id: disponible.dataset.u };
        }
        if (arrastre) { e.dataTransfer.setData('text', 'cronograma'); }
    });
    raiz.addEventListener('dragend', function () { arrastre = null; dibujar(); });
    raiz.addEventListener('dragover', function (e) {
        if (!arrastre) { return; }
        var fila = e.target.closest('.cn-fila[data-i]');
        var zona = e.target.closest('[data-zona]');
        var panel = arrastre.tipo === 'fila' ? e.target.closest('[data-panel-disponibles]') : null;
        var destino = fila || zona || panel;
        if (destino) {
            e.preventDefault();
            limpiarMarcas();
            destino.classList.add('sobre');
        }
    });
    raiz.addEventListener('drop', function (e) {
        if (!arrastre) { return; }
        e.preventDefault();
        var fila = e.target.closest('.cn-fila[data-i]');
        var zona = e.target.closest('[data-zona]');
        var panel = e.target.closest('[data-panel-disponibles]');
        if (arrastre.tipo === 'fila') {
            var movida = colocadas.splice(arrastre.i, 1)[0];
            if (fila) {
                colocadas.splice(parseInt(fila.dataset.i, 10), 0, movida);
            } else if (zona) {
                colocadas.push(movida);
            } else if (!panel) {
                colocadas.splice(arrastre.i, 0, movida); // soltada en cualquier otro lado: queda donde estaba
            }
            // soltada en el panel de disponibles: se quita del cronograma
        } else if ((fila || zona) && !colocadas.some(function (c) { return c.id === arrastre.id; })) {
            var nueva = { id: arrastre.id, semanas: 1 };
            if (fila) { colocadas.splice(parseInt(fila.dataset.i, 10), 0, nueva); } else { colocadas.push(nueva); }
        }
        arrastre = null;
        dibujar();
    });

    // ------------------------------------------------------------------ duración: se arrastra el borde derecho de la barra
    var cambio = null;
    raiz.addEventListener('pointerdown', function (e) {
        var borde = e.target.closest('.cn-borde');
        if (!borde) { return; }
        e.preventDefault();
        e.stopPropagation();
        var fila = borde.closest('.cn-fila');
        fila.draggable = false;
        cambio = { i: parseInt(borde.dataset.i, 10), x: e.clientX, semanas: colocadas[parseInt(borde.dataset.i, 10)].semanas,
            ancho: fila.querySelector('.cn-semana').getBoundingClientRect().width };
    });
    document.addEventListener('pointermove', function (e) {
        if (!cambio) { return; }
        var semanas = Math.max(1, cambio.semanas + Math.round((e.clientX - cambio.x) / cambio.ancho));
        if (semanas !== colocadas[cambio.i].semanas) {
            colocadas[cambio.i].semanas = semanas;
            dibujar();
        }
    });
    document.addEventListener('pointerup', function () { cambio = null; });

    buscar.addEventListener('input', dibujarDisponibles);

    // ------------------------------------------------------------------ botones
    raiz.querySelector('[data-nueva-unidad]').addEventListener('click', async function () {
        if (!hayCambios() || await window.confirmarCambiosSinGuardar('Si salís ahora, los cambios del cronograma se pierden.')) {
            window.location.href = raiz.dataset.urlNueva;
        }
    });

    raiz.querySelector('[data-cancelar]').addEventListener('click', async function () {
        if (!hayCambios()) { window.location.reload(); return; }
        if (await window.confirmarCambiosSinGuardar('Si cancelás, los cambios del cronograma se pierden.')) {
            window.location.reload();
        }
    });

    var botonModificar = raiz.querySelector('[data-modificar]');
    botonModificar.addEventListener('click', async function () {
        if (!hayCambios()) {
            await window.mostrarInformacion('Sin cambios', 'No hiciste cambios en el cronograma.');
            return;
        }
        var datos = new FormData();
        datos.append('programaId', raiz.dataset.programaId);
        colocadas.forEach(function (c) {
            datos.append('ids', c.id);
            datos.append('semanas', c.semanas);
        });
        botonModificar.disabled = true;
        try {
            var respuesta = await fetch(raiz.dataset.urlModificar, {
                method: 'POST',
                body: datos,
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var resultado = await respuesta.json();
            if (respuesta.ok && resultado.mensaje) {
                await window.mostrarExito(resultado.mensaje);
                window.location.reload();
                return;
            }
            await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intente nuevamente.');
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intente nuevamente.');
        }
        botonModificar.disabled = false;
    });

    dibujar();
})();
