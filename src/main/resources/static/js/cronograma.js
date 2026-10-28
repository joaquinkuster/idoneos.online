/**
 * Pantalla del cronograma de un programa (MOD-F-02): consulta (CU-23) y modificación (CU-24).
 *
 *  - Vista por semanas: una fila por tema con una barra que ocupa las semanas que dura. Vista por temas: tabla.
 *  - Si se puede modificar: las filas se reordenan arrastrándolas por el asa, la duración se cambia arrastrando el borde
 *    derecho de la barra, los temas se agregan arrastrándolas desde el panel de la izquierda y se quitan arrastrándolas
 *    de vuelta a ese panel. Nada se guarda hasta apretar "Modificar".
 *  - "Nuevo tema" lleva al ABM de temas; si hay cambios sin guardar, antes pide confirmación.
 *  - El botón de información junto al título muestra u oculta el texto de ayuda.
 *
 * Los datos (temas del curso y temas del cronograma, en orden) vienen del servidor en el HTML.
 */
(function () {
    'use strict';

    var raiz = document.getElementById('cronograma');
    if (!raiz) { return; }

    var editable = raiz.dataset.editable === 'true';
    var COLORES = ['#2F5D8C', '#D4A03D', '#3B6D11', '#993C1D', '#534AB7', '#0F6E56'];
    // Semanas que dibuja la grilla: como mínimo las visibles y, al modificar, algunas libres después del último tema
    // para poder estirarla. El máximo que puede sumar un cronograma es un parámetro del sistema.
    var SEMANAS_VISIBLES = 20;
    var SEMANAS_LIBRES = editable ? 6 : 0;
    var SEMANAS_MAXIMAS = parseInt(raiz.dataset.semanasMaximas, 10) || 52;
    var columnasCongeladas = 0; // mientras se estira un tema, la grilla no cambia de columnas (si no, se mueve el borde)

    var temas = Array.prototype.map.call(raiz.querySelectorAll('[data-tema]'), function (e, indice) {
        return { id: e.dataset.id, titulo: e.dataset.titulo, material: parseInt(e.dataset.material, 10) || 0, color: COLORES[indice % COLORES.length] };
    });
    var colocadas = Array.prototype.map.call(raiz.querySelectorAll('[data-colocada]'), function (e) {
        return { id: e.dataset.id, semanas: parseInt(e.dataset.semanas, 10), estado: e.dataset.estado || '' };
    });
    var semanaHoy = parseInt(raiz.dataset.semanaActual, 10) || 0; // 0: sin línea roja
    var inicial = JSON.stringify(colocadas.map(function (c) { return { id: c.id, semanas: c.semanas }; }));

    var grilla = raiz.querySelector('[data-grilla]');
    var disponibles = raiz.querySelector('[data-disponibles]');
    var buscar = raiz.querySelector('[data-buscar]');
    var resumen = raiz.querySelector('[data-resumen]');

    function temaDe(id) {
        return temas.filter(function (u) { return u.id === id; })[0];
    }

    function totalSemanas() {
        return colocadas.reduce(function (suma, c) { return suma + c.semanas; }, 0);
    }

    function hayCambios() {
        return JSON.stringify(colocadas.map(function (c) { return { id: c.id, semanas: c.semanas }; })) !== inicial;
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
        var columnas = Math.max(Math.min(total + SEMANAS_LIBRES, SEMANAS_MAXIMAS), SEMANAS_VISIBLES, columnasCongeladas);
        // min-width: si las columnas no entran, todas las filas se ensanchan juntas (y el fondo del encabezado llega hasta el final)
        var plantilla = '170px repeat(' + columnas + ', minmax(26px, 1fr));min-width:' + (170 + columnas * 26) + 'px';

        var html = '<div class="cn-fila cn-cabecera-fila" style="grid-template-columns:' + plantilla + '"><div class="cn-celda-temas">Temas</div>';
        for (var s = 1; s <= columnas; s++) { html += '<div' + (s === semanaHoy ? ' class="cn-numero-actual"' : '') + '>' + s + '</div>'; }
        html += '</div>';
        // Línea roja de la semana actual: una por fila, para que se vea continua
        var lineaActual = semanaHoy >= 1 && semanaHoy <= columnas
            ? '<div class="cn-actual" style="grid-column:' + (semanaHoy + 1) + '" data-tip="Hoy: semana ' + semanaHoy + ' de ' + (raiz.dataset.duracion || columnas) + '"></div>' : '';

        var desde = 1;
        colocadas.forEach(function (c, i) {
            var u = temaDe(c.id);
            html += '<div class="cn-fila" ' + (editable ? 'draggable="true" ' : '') + 'data-i="' + i + '" style="grid-template-columns:' + plantilla + ';--c:' + u.color + '">'
                + '<div class="cn-tema" style="grid-row:1;grid-column:1"><span class="cn-numero">' + (i + 1) + '</span>'
                + '<div class="cn-tarjeta"><b>' + escapar(u.titulo) + '</b><small>' + textoSemanas(c.semanas) + '</small></div>'
                + (editable ? '<span class="cn-asa-fila" title="Arrastrar para reordenar"><i class="fa-solid fa-grip-vertical"></i></span>' : '') + '</div>';
            for (var k = 2; k <= columnas + 1; k++) { html += '<div class="cn-semana" style="grid-row:1;grid-column:' + k + '"></div>'; }
            var hasta = desde + c.semanas - 1;
            var tip = 'Tema ' + (i + 1) + ' · ' + u.titulo + ' · ' + (c.semanas === 1 ? 'semana ' + desde : 'semanas ' + desde + ' a ' + hasta) + (c.estado ? ' · ' + c.estado : '');
            html += lineaActual;
            html += '<div class="cn-barra" data-tip="' + escapar(tip).replace(/"/g, '&quot;') + '" style="grid-column:' + (desde + 1) + ' / span ' + c.semanas + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + c.semanas + ' sem.'
                + (editable ? '<span class="cn-borde" data-i="' + i + '" title="Arrastrar para cambiar la duración"></span>' : '') + '</div></div>';
            desde += c.semanas;
        });
        grilla.innerHTML = html;

        var textoTotal = textoSemanas(total);
        raiz.ownerDocument.querySelectorAll('[data-total]').forEach(function (e) { e.textContent = textoTotal; });
        raiz.querySelector('[data-total-resumen]').textContent = 'Total: ' + textoTotal;
        resumen.innerHTML = colocadas.map(function (c) {
            var u = temaDe(c.id);
            return '<span class="cn-pastilla" style="--c:' + u.color + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + c.semanas + ' sem.</span>';
        }).join('');

        // Vista por temas
        var semanaActual = 0;
        raiz.querySelector('[data-tabla]').innerHTML = colocadas.length ? colocadas.map(function (c, i) {
            var u = temaDe(c.id);
            var primera = semanaActual + 1;
            semanaActual += c.semanas;
            return '<tr><td>' + (i + 1) + '</td><td class="text-start"><span class="pn-celda-principal pn-truncar pn-truncar-largo" title="' + escapar(u.titulo).replace(/"/g, '&quot;') + '">' + escapar(u.titulo) + '</span></td>'
                + '<td>' + u.material + (u.material === 1 ? ' material' : ' materiales') + '</td><td>' + textoSemanas(c.semanas) + '</td>'
                + '<td>' + (primera === semanaActual ? 'Semana ' + primera : 'Semanas ' + primera + ' a ' + semanaActual) + '</td></tr>';
        }).join('') : '<tr><td colspan="5" class="pn-vacio-grande"><i class="fa-solid fa-calendar pn-vacio-icono"></i><h3>El cronograma está vacío</h3><p>Arrastrá temas desde el panel izquierdo para armarlo.</p></td></tr>';

        if (editable) { dibujarDisponibles(); }
    }

    function dibujarDisponibles() {
        var filtro = (buscar.value || '').toLowerCase();
        disponibles.innerHTML = temas.map(function (u) {
            if (filtro && u.titulo.toLowerCase().indexOf(filtro) < 0) { return ''; }
            var usada = colocadas.some(function (c) { return c.id === u.id; });
            return '<div class="cn-disponible' + (usada ? ' usada' : '') + '" ' + (usada ? '' : 'draggable="true" ') + 'data-u="' + u.id + '" style="--c:' + u.color + '">'
                + '<div><b>' + escapar(u.titulo) + '</b><small>' + (usada ? 'En el cronograma' : '1 semana al agregarla') + '</small></div>'
                + (usada ? '' : '<span class="cn-asa-fila"><i class="fa-solid fa-grip-vertical"></i></span>') + '</div>';
        }).join('') || '<p class="cn-vacio">No hay temas que coincidan.</p>';
    }

    // ------------------------------------------------------------------ pestañas
    raiz.querySelectorAll('[data-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var porTemas = boton.dataset.vista === 'temas';
            raiz.querySelectorAll('[data-vista]').forEach(function (b) { b.classList.toggle('activa', b === boton); });
            grilla.hidden = porTemas;
            var zona = raiz.querySelector('[data-zona]');
            if (zona) { zona.hidden = porTemas; }
            raiz.querySelector('[data-vista-temas]').hidden = !porTemas;
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
        } else if ((fila || zona) && totalSemanas() >= SEMANAS_MAXIMAS) {
            window.mostrarError('Error! El cronograma es demasiado largo. Máximo: ' + SEMANAS_MAXIMAS + ' semanas.');
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
        columnasCongeladas = Math.max(Math.min(totalSemanas() + SEMANAS_LIBRES, SEMANAS_MAXIMAS), SEMANAS_VISIBLES);
    });
    document.addEventListener('pointermove', function (e) {
        if (!cambio) { return; }
        // La duración no puede hacer que el cronograma pase del máximo de semanas
        var tope = SEMANAS_MAXIMAS - (totalSemanas() - colocadas[cambio.i].semanas);
        var semanas = Math.min(tope, Math.max(1, cambio.semanas + Math.round((e.clientX - cambio.x) / cambio.ancho)));
        if (semanas !== colocadas[cambio.i].semanas) {
            colocadas[cambio.i].semanas = semanas;
            dibujar();
        }
    });
    document.addEventListener('pointerup', function () {
        if (cambio) {
            cambio = null;
            columnasCongeladas = 0;
            dibujar();
        }
    });

    buscar.addEventListener('input', dibujarDisponibles);

    // ------------------------------------------------------------------ botones
    raiz.querySelector('[data-nueva-tema]').addEventListener('click', async function () {
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
