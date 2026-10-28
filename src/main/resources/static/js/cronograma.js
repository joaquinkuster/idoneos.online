/**
 * Pantalla del cronograma de un programa (MOD-F-02): consulta (CU-23) y modificación (CU-24).
 *
 *  - Vista de calendario: una fila por tema con una barra que ocupa los días que dura, en una escala de días, semanas o
 *    meses a elección. Vista por temas: tabla, donde la duración se escribe con un número y su unidad.
 *  - Si se puede modificar: las filas se reordenan arrastrándolas por el asa, la duración se cambia arrastrando el borde
 *    derecho de la barra (salta de a una unidad de la escala), los temas se agregan arrastrándolas desde el panel de la izquierda y se quitan arrastrándolas
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
    // La duración se guarda siempre en días. La escala (días, semanas o meses) solo cambia cómo se dibuja la grilla
    // y de a cuánto salta la duración al arrastrar el borde de una barra.
    var DIAS_POR_SEMANA = 7;
    var DIAS_POR_MES = 30;
    var DIAS_NUEVO = DIAS_POR_SEMANA; // duración de un tema al agregarlo al cronograma
    var PASOS = { dias: 1, semanas: DIAS_POR_SEMANA, meses: DIAS_POR_MES };
    // Unidades que dibuja la grilla: como mínimo las visibles y, al modificar, algunas libres después del último tema
    // para poder estirarla. El máximo que puede sumar un cronograma es un parámetro del sistema (en días).
    var VISIBLES = { dias: 30, semanas: 20, meses: 12 };
    var LIBRES = editable ? { dias: 14, semanas: 6, meses: 2 } : { dias: 0, semanas: 0, meses: 0 };
    var ANCHO_UNIDAD = 26;
    var DIAS_MAXIMOS = parseInt(raiz.dataset.diasMaximos, 10) || 365;
    var unidadesCongeladas = 0; // mientras se estira un tema, la grilla no cambia de columnas (si no, se mueve el borde)

    var temas = Array.prototype.map.call(raiz.querySelectorAll('[data-tema]'), function (e, indice) {
        return { id: e.dataset.id, titulo: e.dataset.titulo, material: parseInt(e.dataset.material, 10) || 0, color: COLORES[indice % COLORES.length] };
    });
    var colocadas = Array.prototype.map.call(raiz.querySelectorAll('[data-colocada]'), function (e) {
        return { id: e.dataset.id, dias: parseInt(e.dataset.dias, 10), estado: e.dataset.estado || '' };
    });
    var diaHoy = parseInt(raiz.dataset.diaActual, 10) || 0; // 0: sin línea roja
    var inicial = JSON.stringify(colocadas.map(function (c) { return { id: c.id, dias: c.dias }; }));

    var grilla = raiz.querySelector('[data-grilla]');
    var disponibles = raiz.querySelector('[data-disponibles]');
    var buscar = raiz.querySelector('[data-buscar]');
    var resumen = raiz.querySelector('[data-resumen]');
    var selectorEscala = raiz.querySelector('[data-escala]');

    // Escala inicial: semanas si todos los temas duran semanas completas; si no, días
    var escala = colocadas.every(function (c) { return c.dias % DIAS_POR_SEMANA === 0; }) ? 'semanas' : 'dias';
    selectorEscala.value = escala;

    function temaDe(id) {
        return temas.filter(function (u) { return u.id === id; })[0];
    }

    function totalDias() {
        return colocadas.reduce(function (suma, c) { return suma + c.dias; }, 0);
    }

    function hayCambios() {
        return JSON.stringify(colocadas.map(function (c) { return { id: c.id, dias: c.dias }; })) !== inicial;
    }

    function escapar(texto) {
        var div = document.createElement('div');
        div.textContent = texto;
        return div.innerHTML;
    }

    function pluralizar(n, singular, plural) {
        return n + ' ' + (n === 1 ? singular : plural);
    }

    // Misma regla que el servidor: meses si es múltiplo de 30 días, semanas si es múltiplo de 7 y días en otro caso
    function formatear(dias) {
        if (dias > 0 && dias % DIAS_POR_MES === 0) { return pluralizar(dias / DIAS_POR_MES, 'mes', 'meses'); }
        if (dias > 0 && dias % DIAS_POR_SEMANA === 0) { return pluralizar(dias / DIAS_POR_SEMANA, 'semana', 'semanas'); }
        return pluralizar(dias, 'día', 'días');
    }

    // Descompone una duración en la unidad más grande que la expresa en forma exacta
    function descomponer(dias) {
        if (dias % DIAS_POR_MES === 0) { return { valor: dias / DIAS_POR_MES, unidad: 'meses' }; }
        if (dias % DIAS_POR_SEMANA === 0) { return { valor: dias / DIAS_POR_SEMANA, unidad: 'semanas' }; }
        return { valor: dias, unidad: 'dias' };
    }

    function periodo(desde, hasta) {
        return desde === hasta ? 'día ' + desde : 'días ' + desde + ' a ' + hasta;
    }

    function nombreCorto(titulo) {
        return titulo.split(' ').slice(0, 2).join(' ');
    }

    function unidadesDeLaGrilla() {
        var paso = PASOS[escala];
        var necesarias = Math.ceil(totalDias() / paso);
        var tope = Math.ceil(DIAS_MAXIMOS / paso);
        return Math.max(Math.min(necesarias + LIBRES[escala], tope), necesarias, VISIBLES[escala], unidadesCongeladas);
    }

    // ------------------------------------------------------------------ dibujo
    function dibujar() {
        var total = totalDias();
        var paso = PASOS[escala];
        var unidades = unidadesDeLaGrilla();
        var columnas = unidades * paso; // una columna por día: las barras son exactas aunque la escala sea mayor
        var ancho = ANCHO_UNIDAD / paso;
        // min-width: si las columnas no entran, todas las filas se ensanchan juntas (y el fondo del encabezado llega hasta el final)
        var plantilla = '170px repeat(' + columnas + ', minmax(' + ancho + 'px, 1fr));min-width:' + (170 + columnas * ancho) + 'px';
        var unidadHoy = diaHoy >= 1 ? Math.ceil(diaHoy / paso) : 0;

        var html = '<div class="cn-fila cn-cabecera-fila" style="grid-template-columns:' + plantilla + '"><div class="cn-celda-temas">Temas</div>';
        for (var s = 1; s <= unidades; s++) {
            html += '<div' + (s === unidadHoy ? ' class="cn-numero-actual"' : '') + ' style="grid-column:' + ((s - 1) * paso + 2) + ' / span ' + paso + '">' + s + '</div>';
        }
        html += '</div>';
        // Línea roja del día actual: una por fila, para que se vea continua
        var lineaActual = diaHoy >= 1 && diaHoy <= columnas
            ? '<div class="cn-actual" style="grid-column:' + (diaHoy + 1) + '" data-tip="Hoy: día ' + diaHoy + ' de ' + (raiz.dataset.duracion || columnas) + '"></div>' : '';

        var desde = 1;
        colocadas.forEach(function (c, i) {
            var u = temaDe(c.id);
            html += '<div class="cn-fila" ' + (editable ? 'draggable="true" ' : '') + 'data-i="' + i + '" style="grid-template-columns:' + plantilla + ';--c:' + u.color + '">'
                + '<div class="cn-tema" style="grid-row:1;grid-column:1"><span class="cn-numero">' + (i + 1) + '</span>'
                + '<div class="cn-tarjeta"><b>' + escapar(u.titulo) + '</b><small>' + formatear(c.dias) + '</small></div>'
                + (editable ? '<span class="cn-asa-fila" title="Arrastrar para reordenar"><i class="fa-solid fa-grip-vertical"></i></span>' : '') + '</div>';
            for (var k = 1; k <= unidades; k++) { html += '<div class="cn-unidad" style="grid-row:1;grid-column:' + ((k - 1) * paso + 2) + ' / span ' + paso + '"></div>'; }
            var hasta = desde + c.dias - 1;
            var tip = 'Tema ' + (i + 1) + ' · ' + u.titulo + ' · ' + periodo(desde, hasta) + (c.estado ? ' · ' + c.estado : '');
            html += lineaActual;
            html += '<div class="cn-barra" data-tip="' + escapar(tip).replace(/"/g, '&quot;') + '" style="grid-column:' + (desde + 1) + ' / span ' + c.dias + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + formatear(c.dias)
                + (editable ? '<span class="cn-borde" data-i="' + i + '" title="Arrastrar para cambiar la duración"></span>' : '') + '</div></div>';
            desde += c.dias;
        });
        grilla.innerHTML = html;

        var textoTotal = formatear(total);
        raiz.ownerDocument.querySelectorAll('[data-total]').forEach(function (e) { e.textContent = textoTotal; });
        raiz.querySelector('[data-total-resumen]').textContent = 'Total: ' + textoTotal;
        resumen.innerHTML = colocadas.map(function (c) {
            var u = temaDe(c.id);
            return '<span class="cn-pastilla" style="--c:' + u.color + '">' + escapar(nombreCorto(u.titulo)) + ' · ' + formatear(c.dias) + '</span>';
        }).join('');

        // Vista por temas: al modificar, la duración se escribe con un número y su unidad
        var diaActual = 0;
        raiz.querySelector('[data-tabla]').innerHTML = colocadas.length ? colocadas.map(function (c, i) {
            var u = temaDe(c.id);
            var primero = diaActual + 1;
            diaActual += c.dias;
            var duracion = formatear(c.dias);
            if (editable) {
                var d = descomponer(c.dias);
                duracion = '<span class="cn-duracion-campo"><input type="number" min="1" class="pn-input" data-duracion-numero data-i="' + i + '" value="' + d.valor + '" aria-label="Duración del tema">'
                    + '<select class="pn-input" data-duracion-unidad data-i="' + i + '" aria-label="Unidad de la duración">'
                    + ['dias', 'semanas', 'meses'].map(function (o) { return '<option value="' + o + '"' + (o === d.unidad ? ' selected' : '') + '>' + { dias: 'Días', semanas: 'Semanas', meses: 'Meses' }[o] + '</option>'; }).join('')
                    + '</select></span>';
            }
            return '<tr><td>' + (i + 1) + '</td><td class="text-start"><span class="pn-celda-principal pn-truncar pn-truncar-largo" title="' + escapar(u.titulo).replace(/"/g, '&quot;') + '">' + escapar(u.titulo) + '</span></td>'
                + '<td>' + u.material + (u.material === 1 ? ' material' : ' materiales') + '</td><td>' + duracion + '</td>'
                + '<td>' + (primero === diaActual ? 'Día ' + primero : 'Días ' + primero + ' a ' + diaActual) + '</td></tr>';
        }).join('') : '<tr><td colspan="5" class="pn-vacio-grande"><i class="fa-solid fa-calendar pn-vacio-icono"></i><h3>El cronograma está vacío</h3><p>Arrastrá temas desde el panel izquierdo para armarlo.</p></td></tr>';

        if (editable) { dibujarDisponibles(); }
    }

    function dibujarDisponibles() {
        var filtro = (buscar.value || '').toLowerCase();
        disponibles.innerHTML = temas.map(function (u) {
            if (filtro && u.titulo.toLowerCase().indexOf(filtro) < 0) { return ''; }
            var usada = colocadas.some(function (c) { return c.id === u.id; });
            return '<div class="cn-disponible' + (usada ? ' usada' : '') + '" ' + (usada ? '' : 'draggable="true" ') + 'data-u="' + u.id + '" style="--c:' + u.color + '">'
                + '<div><b>' + escapar(u.titulo) + '</b><small>' + (usada ? 'En el cronograma' : formatear(DIAS_NUEVO) + ' al agregarlo') + '</small></div>'
                + (usada ? '' : '<span class="cn-asa-fila"><i class="fa-solid fa-grip-vertical"></i></span>') + '</div>';
        }).join('') || '<p class="cn-vacio">No hay temas que coincidan.</p>';
    }

    selectorEscala.addEventListener('change', function () {
        escala = selectorEscala.value;
        dibujar();
    });

    // ------------------------------------------------------------------ pestañas
    raiz.querySelectorAll('[data-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var porTemas = boton.dataset.vista === 'temas';
            raiz.querySelectorAll('[data-vista]').forEach(function (b) { b.classList.toggle('activa', b === boton); });
            grilla.hidden = porTemas;
            raiz.querySelector('[data-escala-etiqueta]').hidden = porTemas;
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
        } else if ((fila || zona) && totalDias() >= DIAS_MAXIMOS) {
            window.mostrarError('Error! El cronograma es demasiado largo. Máximo: ' + DIAS_MAXIMOS + ' días.');
        } else if ((fila || zona) && !colocadas.some(function (c) { return c.id === arrastre.id; })) {
            var nueva = { id: arrastre.id, dias: Math.min(DIAS_NUEVO, DIAS_MAXIMOS - totalDias()) };
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
        cambio = { i: parseInt(borde.dataset.i, 10), x: e.clientX, dias: colocadas[parseInt(borde.dataset.i, 10)].dias,
            ancho: fila.querySelector('.cn-unidad').getBoundingClientRect().width };
        unidadesCongeladas = unidadesDeLaGrilla();
    });
    document.addEventListener('pointermove', function (e) {
        if (!cambio) { return; }
        // La duración salta de a una unidad de la escala y no puede hacer que el cronograma pase del máximo de días
        var tope = DIAS_MAXIMOS - (totalDias() - colocadas[cambio.i].dias);
        var saltos = Math.round((e.clientX - cambio.x) / cambio.ancho);
        var dias = Math.min(tope, Math.max(1, cambio.dias + saltos * PASOS[escala]));
        if (dias !== colocadas[cambio.i].dias) {
            colocadas[cambio.i].dias = dias;
            dibujar();
        }
    });
    document.addEventListener('pointerup', function () {
        if (cambio) {
            cambio = null;
            unidadesCongeladas = 0;
            dibujar();
        }
    });

    buscar.addEventListener('input', dibujarDisponibles);

    // Duración escrita con número y unidad (vista por temas)
    raiz.addEventListener('change', function (e) {
        var campo = e.target.closest('[data-duracion-numero], [data-duracion-unidad]');
        if (!campo) { return; }
        var i = parseInt(campo.dataset.i, 10);
        var celda = campo.closest('.cn-duracion-campo');
        var valor = Number(celda.querySelector('[data-duracion-numero]').value);
        var dias = valor * PASOS[celda.querySelector('[data-duracion-unidad]').value];
        if (!Number.isInteger(valor) || valor <= 0) {
            window.mostrarError('Error! La duración de cada tema no es válida. Formato válido: número entero mayor a cero.');
        } else if (totalDias() - colocadas[i].dias + dias > DIAS_MAXIMOS) {
            window.mostrarError('Error! El cronograma es demasiado largo. Máximo: ' + DIAS_MAXIMOS + ' días.');
        } else {
            colocadas[i].dias = dias;
        }
        dibujar();
    });

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
            datos.append('dias', c.dias);
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
