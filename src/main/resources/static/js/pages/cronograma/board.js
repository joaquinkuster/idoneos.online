/**
 * Pantalla del cronograma de un programa (MOD-F-02): consulta (CU-23) y modificación (CU-24).
 *
 *  - Vista de calendario: una fila por tema con una barra que ocupa los días que dura, en una escala de días, semanas o
 *    meses a elección. Vista por temas: tabla, donde la duración se escribe en meses, semanas y días.
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
    var DIAS_NUEVO = parseInt(raiz.dataset.diasNuevo, 10) || DIAS_POR_SEMANA; // duración de un tema al agregarlo (parámetro del sistema)
    var PASOS = { dias: 1, semanas: DIAS_POR_SEMANA, meses: DIAS_POR_MES };
    // Unidades que dibuja la grilla: como mínimo las visibles y, al modificar, algunas libres después del último tema
    // para poder estirarla. El máximo que puede sumar un cronograma es un parámetro del sistema (en días).
    var VISIBLES = { dias: 10, semanas: 8, meses: 6 };
    var LIBRES = editable ? { dias: 7, semanas: 6, meses: 2 } : { dias: 0, semanas: 0, meses: 0 };
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
    var selectorEscala = raiz.querySelector('[data-escala]');
    var balancePorcentaje = raiz.querySelector('[data-prog-balance-porcentaje]');
    var balanceBarra = raiz.querySelector('[data-prog-balance-barra]');
    var faltan = raiz.querySelector('[data-prog-faltan]');
    var PALABRAS = ['cero', 'uno', 'dos', 'tres', 'cuatro', 'cinco', 'seis', 'siete', 'ocho', 'nueve', 'diez'];
    var minimo = parseInt(raiz.dataset.minimo, 10) || 0;
    var textoMinimo = raiz.querySelector('[data-minimo-texto]');
    if (textoMinimo && minimo) {
        textoMinimo.innerHTML = 'El mínimo requerido de temas es <strong>' + minimo + (minimo < PALABRAS.length ? ' (' + PALABRAS[minimo] + ')' : '') + '</strong>.';
    }

    // Escala inicial: la que corresponde a la duración total del cronograma (la calcula el servidor, igual que en el cajón
    // del curso). Después la persona puede elegir otra.
    var escala = 'semanas';

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
    // Reparte una duración en meses, semanas y días (la misma regla que el servidor: 1 mes = 30 días, 1 semana = 7)
    function partes(dias) {
        var meses = Math.floor(dias / DIAS_POR_MES);
        var resto = dias - meses * DIAS_POR_MES;
        var semanas = Math.floor(resto / DIAS_POR_SEMANA);
        return [meses, semanas, resto - semanas * DIAS_POR_SEMANA];
    }

    function periodo(desde, hasta) {
        return desde === hasta ? 'día ' + desde : 'días ' + desde + ' a ' + hasta;
    }

    function nombreCorto(titulo) {
        return titulo.split(' ').slice(0, 2).join(' ');
    }

    function unidadesDeLaGrilla(esc) {
        var paso = PASOS[esc];
        var necesarias = Math.ceil(totalDias() / paso);
        var tope = Math.ceil(DIAS_MAXIMOS / paso);
        return Math.max(Math.min(necesarias + LIBRES[esc], tope), necesarias, VISIBLES[esc], unidadesCongeladas);
    }

    // La escala inicial según la duración total: días si es corta, semanas si es media y meses si es larga
    function escalaInicial() {
        // La unidad más grande con la que el curso ocupa al menos seis columnas
        if (Math.ceil(DIAS_MAXIMOS / DIAS_POR_MES) >= 6) { return 'meses'; }
        if (Math.ceil(DIAS_MAXIMOS / DIAS_POR_SEMANA) >= 6) { return 'semanas'; }
        return 'dias';
    }

    function aplicarEscala(nueva) {
        escala = nueva;
        selectorEscala.value = nueva;
    }

    // Duración de un tema en la vista por temas: meses, semanas y días por separado (el formato Mixto de los formularios)
    function campoDuracion(c, i) {
        var tope = DIAS_MAXIMOS - (totalDias() - c.dias);
        var p = partes(c.dias);
        return '<div class="pn-duracion-fila cn-duracion-fila" data-i="' + i + '"><div class="pn-duracion-mixto">'
            + '<label class="pn-tiempo-parte"><span>Meses</span><input type="number" min="0" max="' + Math.floor(tope / DIAS_POR_MES) + '" class="pn-input" data-duracion-parte data-factor="30" value="' + p[0] + '"></label>'
            + '<label class="pn-tiempo-parte"><span>Semanas</span><input type="number" min="0" max="' + Math.floor(tope / DIAS_POR_SEMANA) + '" class="pn-input" data-duracion-parte data-factor="7" value="' + p[1] + '"></label>'
            + '<label class="pn-tiempo-parte"><span>Días</span><input type="number" min="0" max="' + tope + '" class="pn-input" data-duracion-parte data-factor="1" value="' + p[2] + '"></label>'
            + '</div></div>';
    }

    // ------------------------------------------------------------------ dibujo
    function dibujar() {
        var total = totalDias();
        var paso = PASOS[escala];
        var unidades = unidadesDeLaGrilla(escala);
        var columnas = unidades * paso; // una columna por día: las barras son exactas aunque la escala sea mayor
        var ancho = ANCHO_UNIDAD / paso;
        // min-width: si las columnas no entran, todas las filas se ensanchan juntas (y el fondo del encabezado llega hasta el final)
        // Sin columna de temas: la grilla son solo los días, igual que en el paso Cronograma del asistente de programas
        var plantilla = 'repeat(' + columnas + ', minmax(' + ancho + 'px, 1fr));min-width:' + (columnas * ancho) + 'px';
        var unidadHoy = diaHoy >= 1 ? Math.ceil(diaHoy / paso) : 0;

        var html = '<div class="cn-fila cn-cabecera-fila" style="grid-template-columns:' + plantilla + '">';
        for (var s = 1; s <= unidades; s++) {
            var clases = (s === unidadHoy ? 'cn-numero-actual' : '') + ((s - 1) * paso + 1 > DIAS_MAXIMOS ? ' cn-fuera' : '');
            html += '<div' + (clases.trim() ? ' class="' + clases.trim() + '"' : '') + ' style="grid-column:' + ((s - 1) * paso + 1) + ' / span ' + paso + '">' + s + '</div>';
        }
        html += '</div>';
        // Línea roja del día actual: una por fila, para que se vea continua
        var lineaActual = diaHoy >= 1 && diaHoy <= columnas
            ? '<div class="cn-actual" style="grid-column:' + diaHoy + '" data-tip="Hoy: día ' + diaHoy + ' de ' + (raiz.dataset.duracion || columnas) + '"></div>' : '';

        var desde = 1;
        colocadas.forEach(function (c, i) {
            var u = temaDe(c.id);
            html += '<div class="cn-fila" ' + (editable ? 'draggable="true" ' : '') + 'data-i="' + i + '" style="grid-template-columns:' + plantilla + ';--c:' + u.color + '">';
            for (var k = 1; k <= unidades; k++) { html += '<div class="cn-unidad' + ((k - 1) * paso + 1 > DIAS_MAXIMOS ? ' cn-fuera' : '') + '" style="grid-row:1;grid-column:' + ((k - 1) * paso + 1) + ' / span ' + paso + '"></div>'; }
            var hasta = desde + c.dias - 1;
            var tip = 'Tema ' + (i + 1) + ' · ' + u.titulo + ' · ' + periodo(desde, hasta) + (c.estado ? ' · ' + c.estado : '');
            html += lineaActual;
            html += '<div class="cn-barra" data-tip="' + escapar(tip).replace(/"/g, '&quot;') + '" style="grid-column:' + desde + ' / span ' + c.dias + '">'
                + (editable ? '<i class="fa-solid fa-grip-vertical pn-prog-asa" aria-hidden="true"></i>' : '')
                + '<span class="pn-prog-barra-texto">' + escapar(nombreCorto(u.titulo)) + ' · ' + formatear(c.dias) + '</span>'
                + (editable ? '<span class="cn-borde" data-i="' + i + '" title="Arrastrar para cambiar la duración"></span>' : '') + '</div></div>';
            desde += c.dias;
        });
        grilla.innerHTML = html;

        var textoTotal = formatear(total);
        raiz.ownerDocument.querySelectorAll('[data-total]').forEach(function (e) { e.textContent = textoTotal; });
        if (balancePorcentaje) {
            var porcentaje = DIAS_MAXIMOS ? Math.round(total / DIAS_MAXIMOS * 100) : 0;
            balancePorcentaje.textContent = porcentaje + '%';
            balanceBarra.style.width = Math.min(100, porcentaje) + '%';
            balanceBarra.parentNode.parentNode.setAttribute('data-estado', total === DIAS_MAXIMOS ? 'completo' : 'parcial');
            faltan.textContent = '(Faltan ' + Math.max(0, DIAS_MAXIMOS - total) + ' de ' + DIAS_MAXIMOS + ' días)';
        }

        // Vista por temas: al modificar, la duración se escribe con un número y su unidad
        var diaActual = 0;
        raiz.querySelector('[data-tabla]').innerHTML = colocadas.length ? colocadas.map(function (c, i) {
            var u = temaDe(c.id);
            var primero = diaActual + 1;
            diaActual += c.dias;
            var duracion = formatear(c.dias);
            if (editable) { duracion = campoDuracion(c, i); }
            return '<tr><td>' + (i + 1) + '</td><td><span class="pn-celda-principal pn-truncar pn-truncar-largo" title="' + escapar(u.titulo).replace(/"/g, '&quot;') + '">' + escapar(u.titulo) + '</span></td>'
                + '<td>' + duracion + '</td>'
                + '<td>' + (primero === diaActual ? 'Día ' + primero : 'Días ' + primero + ' a ' + diaActual) + '</td></tr>';
        }).join('') : '<tr><td colspan="4" class="pn-empty-grande"><i class="fa-solid fa-calendar pn-empty-icon"></i><h3>El cronograma está vacío</h3><p>Arrastrá temas desde el panel izquierdo para armarlo.</p></td></tr>';

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
        }).join('') || '<p class="cn-empty">No hay temas que coincidan.</p>';
    }

    selectorEscala.addEventListener('change', function () {
        aplicarEscala(selectorEscala.value);
        dibujar();
    });

    // ------------------------------------------------------------------ pestañas
    raiz.querySelectorAll('[data-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var porTemas = boton.dataset.vista === 'temas';
            raiz.querySelectorAll('[data-vista]').forEach(function (b) { b.classList.toggle('activa', b === boton); });
            grilla.hidden = porTemas;
            raiz.querySelector('[data-escala-etiqueta]').classList.toggle('invisible', porTemas); // invisible, no oculta: los botones no se mueven
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
        aplicarEscala(escalaInicial());
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
            window.mostrarError('Error! El cronograma ya alcanzó la duración máxima del curso (' + formatear(DIAS_MAXIMOS) + '): no se pueden agregar más temas.');
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
        unidadesCongeladas = unidadesDeLaGrilla(escala);
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

    // Repartir en partes iguales: la duración del curso se divide entre los temas del cronograma (en semanas enteras si se puede)
    raiz.querySelector('[data-repartir]').addEventListener('click', function () {
        if (!colocadas.length) { return; }
        var n = colocadas.length;
        var unidad = DIAS_MAXIMOS % DIAS_POR_SEMANA === 0 && DIAS_MAXIMOS / DIAS_POR_SEMANA >= n ? DIAS_POR_SEMANA : 1;
        var unidades = Math.floor(DIAS_MAXIMOS / unidad);
        var base = Math.floor(unidades / n);
        var resto = unidades % n;
        colocadas.forEach(function (c, i) { c.dias = Math.max(1, (base + (i < resto ? 1 : 0)) * unidad); });
        dibujar();
    });

    // Duración escrita en meses, semanas y días (vista por temas). Nunca baja de un día (simplemente no se deja disminuir más)
    // y lo que pasa del máximo del curso se recorta, sin alertas
    raiz.addEventListener('change', function (e) {
        var campo = e.target.closest('[data-duracion-parte]');
        if (!campo) { return; }
        var fila = campo.closest('.cn-duracion-fila');
        var c = colocadas[parseInt(fila.dataset.i, 10)];
        var tope = DIAS_MAXIMOS - (totalDias() - c.dias);
        var dias = 0;
        Array.prototype.forEach.call(fila.querySelectorAll('[data-duracion-parte]'), function (parte) {
            dias += Math.max(0, parseInt(parte.value, 10) || 0) * parseInt(parte.dataset.factor, 10);
        });
        if (dias <= 0) { dias = c.dias; }
        c.dias = Math.max(1, Math.min(dias, tope));
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
                // Alerta guía del paso siguiente (por ejemplo, completar el contenido de los temas pendientes)
                if (resultado.siguienteUrl && window.preguntarSiguientePaso
                        && await window.preguntarSiguientePaso(resultado.siguienteTitulo, resultado.siguienteTexto, resultado.siguienteConfirmar, resultado.siguienteTipo)) {
                    window.location.href = resultado.siguienteUrl;
                    return;
                }
                window.location.reload();
                return;
            }
            await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        }
        botonModificar.disabled = false;
    });

    aplicarEscala(escalaInicial());
    dibujar();
})();
