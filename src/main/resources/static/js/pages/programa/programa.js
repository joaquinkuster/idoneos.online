/**
 * Asistente de programas del docente (MOD-F-02, CU-15 a CU-18 y cronograma del programa).
 *
 *  1. Un único asistente registra y edita programas, en tres pasos: Datos básicos, Cronograma y Resumen.
 *  2. El cronograma se arma como en su pantalla: a la izquierda los temas disponibles (los que ya tienen su contenido completo, con un
 *     buscador) que se arrastran al calendario, y a la derecha el cronograma, que se ve como calendario (cada tema es una línea que se
 *     reordena arrastrándola, con una barra cuyo borde derecho se arrastra para cambiar la duración) o como tabla por temas. Los días
 *     de los temas suman siempre la duración del curso y nunca la superan, ni en el calendario ni por temas; "Repartir en partes iguales"
 *     divide la duración. Un tema se saca arrastrándolo al panel de la izquierda o con el tacho.
 *  3. Un programa nuevo puede partir de uno anterior (paso Cronograma): se carga su cronograma, que no se guarda hasta confirmar.
 *  4. Un programa que ya no se puede modificar (inscripciones o inscripción abierta) se ve, pero no se edita: el asistente queda de
 *     solo lectura con un aviso.
 *  5. Si la página se abre con ?nuevo o ?editar=<id>, abre sola el asistente; se quitan esos parámetros de la dirección.
 */
(function () {
    'use strict';

    var ventana = document.getElementById('modalPrograma');
    var formulario = ventana && ventana.querySelector('[data-programa-formulario]');
    var datos = window.PROGRAMA_DATOS;
    if (!formulario || !datos) { return; }

    var DIAS_POR_MES = 30;
    var DIAS_POR_SEMANA = 7;
    var COLORES = ['#2F5D8C', '#D4A03D', '#3B6D11', '#993C1D', '#534AB7', '#0F6E56'];
    // Unidades que dibuja la grilla (igual que la pantalla del cronograma): como mínimo unas pocas visibles y algunas libres después del último tema
    var VISIBLES = { 1: 10, 7: 8, 30: 6 };
    var LIBRES = { 1: 7, 7: 6, 30: 2 };
    var ANCHO_UNIDAD = 26;
    var MAXIMO = datos.duracionDias;
    var PALABRAS = ['cero', 'uno', 'dos', 'tres', 'cuatro', 'cinco', 'seis', 'siete', 'ocho', 'nueve', 'diez', 'once', 'doce', 'trece',
        'catorce', 'quince', 'dieciséis', 'diecisiete', 'dieciocho', 'diecinueve', 'veinte'];

    var editor = formulario.querySelector('.pn-prog-editor');
    var campoNombre = formulario.querySelector('[name="nombre"]');
    var campoDescripcion = formulario.querySelector('[name="descripcion"]');
    var selectOrigen = formulario.querySelector('[data-programa-origen-select]');
    var contenedorOrigen = formulario.querySelector('[data-programa-origen]');
    var calendario = formulario.querySelector('[data-prog-calendario]');
    var tabla = formulario.querySelector('[data-prog-tabla]');
    var tablaContenedor = formulario.querySelector('[data-prog-tabla-contenedor]');
    var zona = formulario.querySelector('[data-prog-zona]');
    var vacio = formulario.querySelector('[data-prog-vacio]');
    var errorCronograma = formulario.querySelector('[data-prog-error]');
    var listaDisponibles = formulario.querySelector('[data-prog-disponibles]');
    var buscador = formulario.querySelector('[data-prog-buscar]');
    var botonRepartir = formulario.querySelector('[data-prog-repartir]');
    var errorBuscador = formulario.querySelector('[data-prog-error-buscador]');
    var selectEscala = formulario.querySelector('[data-prog-escala]');
    var barra = formulario.querySelector('[data-prog-barra]');
    var faltan = formulario.querySelector('[data-prog-faltan]');
    var escalaContenedor = formulario.querySelector('[data-prog-escala-contenedor]');
    var balancePorcentaje = formulario.querySelector('[data-prog-balance-porcentaje]');
    var balanceBarra = formulario.querySelector('[data-prog-balance-barra]');
    var textoMinimo = formulario.querySelector('[data-prog-minimo-texto]');
    var arbol = formulario.querySelector('[data-prog-arbol]');
    var notaCohortes = formulario.querySelector('[data-prog-nota-cohortes]');
    var avisoBloqueado = formulario.querySelector('[data-programa-bloqueado]');
    var titulo = ventana.querySelector('[data-programa-titulo]');

    var temasPorId = {};
    datos.temas.forEach(function (tema, indice) {
        tema.completo = Object.keys(tema.contenido || {}).every(function (columna) { return tema.contenido[columna]; });
        tema.color = COLORES[indice % COLORES.length];
        temasPorId[tema.id] = tema;
    });

    /** La escala que mejor entra en la pantalla: la unidad más grande con la que el curso ocupa al menos seis columnas. */
    function escalaAutomatica() {
        var opciones = [DIAS_POR_MES, DIAS_POR_SEMANA, 1];
        for (var i = 0; i < opciones.length; i++) {
            if (Math.ceil(MAXIMO / opciones[i]) >= 6) { return opciones[i]; }
        }
        return 1;
    }

    var cronograma = [];        // { id, dias }
    var ajustado = false;       // si el docente ya cambió a mano alguna duración
    var escala = escalaAutomatica();
    var vista = 'calendario';
    var edicion = null;         // { id, nombre, descripcion, cronograma, bloqueado, motivo } del programa que se edita; null si se registra
    var origenNombre = '';
    var arrastre = null;        // { tipo: 'fila', i } o { tipo: 'disponible', id }
    var cambio = null;          // { i, x, dias, ancho } mientras se estira el borde de una barra
    var unidadesCongeladas = 0; // mientras se estira un tema, la grilla no cambia de columnas (si no, se mueve el borde)

    // ---------------------------------------------------------------- utilidades
    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function plural(cantidad, singular, pluralTexto) {
        return cantidad + ' ' + (cantidad === 1 ? singular : pluralTexto);
    }

    /** Mismo formato que DuracionUtil.formatear del servidor: la unidad más grande que expresa la duración exacta. */
    function texto(dias) {
        if (dias > 0 && dias % DIAS_POR_MES === 0) { return plural(dias / DIAS_POR_MES, 'mes', 'meses'); }
        if (dias > 0 && dias % DIAS_POR_SEMANA === 0) { return plural(dias / DIAS_POR_SEMANA, 'semana', 'semanas'); }
        return plural(dias, 'día', 'días');
    }

    function normalizar(valor) {
        return String(valor || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
    }

    function total() {
        return cronograma.reduce(function (suma, fila) { return suma + fila.dias; }, 0);
    }

    function temaDe(fila) { return temasPorId[fila.id]; }

    function soloLectura() { return !!(edicion && edicion.bloqueado); }

    function marcar(campo, mensaje) {
        if (window.PanelValidacion && window.PanelValidacion.marcar) { window.PanelValidacion.marcar(campo, mensaje); }
    }

    function nombreCorto(tituloTema) { return tituloTema.split(' ').slice(0, 2).join(' '); }

    function periodo(desde, hasta) {
        return desde === hasta ? 'Día ' + desde : 'Días ' + desde + ' a ' + hasta;
    }

    /** Reparte una duración en meses, semanas y días (1 mes = 30 días, 1 semana = 7). */
    function partes(dias) {
        var meses = Math.floor(dias / DIAS_POR_MES);
        var resto = dias - meses * DIAS_POR_MES;
        var semanas = Math.floor(resto / DIAS_POR_SEMANA);
        return [meses, semanas, resto - semanas * DIAS_POR_SEMANA];
    }

    /** Reparte la duración del curso por igual entre n temas: en semanas enteras si se puede y, si no, en días. */
    function repartoParejo(n) {
        var duracion = datos.duracionDias;
        if (n <= 0) { return []; }
        var unidad = duracion % DIAS_POR_SEMANA === 0 && duracion / DIAS_POR_SEMANA >= n ? DIAS_POR_SEMANA : 1;
        var unidades = duracion / unidad;
        var base = Math.floor(unidades / n);
        var resto = unidades % n;
        var reparto = [];
        for (var i = 0; i < n; i++) { reparto.push((base + (i < resto ? 1 : 0)) * unidad); }
        return reparto;
    }

    function repartirParejo() {
        var reparto = repartoParejo(cronograma.length);
        cronograma.forEach(function (fila, i) { fila.dias = reparto[i]; });
    }

    // ---------------------------------------------------------------- cronograma: cambios
    /** Suma un tema al cronograma, antes de la posición indicada (o al final). */
    function agregar(id, posicion) {
        errorBuscador.textContent = '';
        if (cronograma.some(function (fila) { return fila.id === id; })) { return; }
        if (cronograma.length + 1 > MAXIMO || (ajustado && total() >= MAXIMO)) {
            errorBuscador.textContent = 'Error! El cronograma ya alcanzó la duración del curso (' + texto(MAXIMO) + '): no se pueden agregar más temas.';
            return;
        }
        var nueva = { id: id, dias: 1 };
        if (posicion === undefined || posicion === null || posicion >= cronograma.length) { cronograma.push(nueva); } else { cronograma.splice(posicion, 0, nueva); }
        if (!ajustado) {
            repartirParejo();
        } else {
            nueva.dias = Math.min(Math.max(escala, 1), MAXIMO - (total() - 1));
        }
        dibujar();
    }

    function quitar(id) {
        cronograma = cronograma.filter(function (fila) { return fila.id !== id; });
        if (!ajustado) { repartirParejo(); }
        dibujar();
    }

    function mover(desde, hasta) {
        if (desde === hasta || desde < 0 || hasta < 0 || desde >= cronograma.length || hasta >= cronograma.length) { return; }
        var fila = cronograma.splice(desde, 1)[0];
        cronograma.splice(hasta, 0, fila);
        dibujar();
    }

    /** Cambia los días de un tema sin dejar que el cronograma pase de la duración del curso. */
    function cambiarDias(indice, dias) {
        var tope = MAXIMO - (total() - cronograma[indice].dias);
        cronograma[indice].dias = Math.max(1, Math.min(dias, tope));
        ajustado = true;
    }

    // ---------------------------------------------------------------- dibujo
    function dibujarDisponibles() {
        var buscado = normalizar(buscador.value);
        var usados = {};
        cronograma.forEach(function (fila) { usados[fila.id] = true; });
        var completos = datos.temas.filter(function (tema) { return tema.completo && (!buscado || normalizar(tema.titulo).indexOf(buscado) >= 0); });
        var lectura = soloLectura();
        listaDisponibles.innerHTML = completos.map(function (tema) {
            var usada = !!usados[tema.id];
            return '<div class="cn-disponible' + (usada ? ' usada' : '') + '" ' + (usada || lectura ? '' : 'draggable="true" ') + 'data-u="' + tema.id + '" style="--c:' + tema.color + '">'
                + '<div><b>' + esc(tema.titulo) + '</b><small>' + (usada ? 'En el cronograma' : 'Arrastralo al cronograma') + '</small></div>'
                + (usada || lectura ? '' : '<span class="cn-asa-fila"><i class="fa-solid fa-grip-vertical"></i></span>') + '</div>';
        }).join('') || '<p class="cn-empty">' + (datos.temas.length ? 'No hay temas que coincidan.' : 'El curso todavía no tiene temas.') + '</p>';
    }

    function dibujarBalance() {
        var asignado = total();
        var porcentaje = MAXIMO ? Math.round(asignado / MAXIMO * 100) : 0;
        balancePorcentaje.textContent = porcentaje + '%';
        balanceBarra.style.width = Math.min(100, porcentaje) + '%';
        balanceBarra.parentNode.parentNode.setAttribute('data-estado', asignado === MAXIMO ? 'completo' : 'parcial');
        faltan.textContent = '(Faltan ' + Math.max(0, MAXIMO - asignado) + ' de ' + MAXIMO + ' días)';
        var minimo = datos.minimoTemas || 1;
        textoMinimo.innerHTML = 'El mínimo requerido de temas es <strong>' + minimo + (minimo < PALABRAS.length ? ' (' + PALABRAS[minimo] + ')' : '') + '</strong>.';
    }

    /** Las unidades de la grilla (días, semanas o meses): las necesarias, unas pocas visibles y algunas libres, sin pasar de la duración del curso. */
    function unidadesDeLaGrilla() {
        var necesarias = Math.ceil(total() / escala);
        var tope = Math.ceil(MAXIMO / escala);
        return Math.max(Math.min(necesarias + LIBRES[escala], tope), necesarias, VISIBLES[escala], unidadesCongeladas);
    }

    function dibujarCalendario() {
        var unidades = unidadesDeLaGrilla();
        var columnas = unidades * escala; // una columna por día: las barras son exactas aunque la escala sea mayor
        var ancho = ANCHO_UNIDAD / escala;
        var plantilla = 'repeat(' + columnas + ', minmax(' + ancho + 'px, 1fr));min-width:' + (columnas * ancho) + 'px';
        var html = '<div class="cn-fila cn-cabecera-fila" style="grid-template-columns:' + plantilla + '">';
        for (var s = 1; s <= unidades; s++) {
            html += '<div' + ((s - 1) * escala + 1 > MAXIMO ? ' class="cn-fuera"' : '') + ' style="grid-column:' + ((s - 1) * escala + 1) + ' / span ' + escala + '">' + s + '</div>';
        }
        html += '</div>';
        var desde = 1;
        var lectura = soloLectura();
        cronograma.forEach(function (fila, i) {
            var tema = temaDe(fila);
            html += '<div class="cn-fila" ' + (lectura ? '' : 'draggable="true" ') + 'data-i="' + i + '" style="grid-template-columns:' + plantilla + ';--c:' + tema.color + '">';
            for (var k = 1; k <= unidades; k++) {
                html += '<div class="cn-unidad' + ((k - 1) * escala + 1 > MAXIMO ? ' cn-fuera' : '') + '" style="grid-row:1;grid-column:' + ((k - 1) * escala + 1) + ' / span ' + escala + '"></div>';
            }
            var hasta = desde + fila.dias - 1;
            var tip = 'Tema ' + (i + 1) + ' · ' + tema.titulo + ' · ' + periodo(desde, hasta).toLowerCase();
            html += '<div class="cn-barra" title="' + esc(tip) + '" style="grid-column:' + desde + ' / span ' + fila.dias + '"><i class="fa-solid fa-grip-vertical pn-prog-asa" aria-hidden="true"></i>'
                + '<span class="pn-prog-barra-texto">' + esc(nombreCorto(tema.titulo)) + ' · ' + esc(texto(fila.dias)) + '</span>'
                + '<span class="cn-borde" data-i="' + i + '" title="Arrastrar para cambiar la duración"></span></div></div>';
            desde += fila.dias;
        });
        calendario.innerHTML = html;
    }

    /** La duración de un tema en la vista por temas: meses, semanas y días por separado. */
    function campoDuracion(fila, i) {
        var tope = MAXIMO - (total() - fila.dias);
        var p = partes(fila.dias);
        var deshabilitado = soloLectura() ? ' disabled' : '';
        return '<div class="pn-duracion-fila cn-duracion-fila" data-i="' + i + '"><div class="pn-duracion-mixto">'
            + '<label class="pn-tiempo-parte"><span>Meses</span><input type="number" min="0" max="' + Math.floor(tope / DIAS_POR_MES) + '" class="pn-input" data-duracion-parte data-factor="30" value="' + p[0] + '"' + deshabilitado + '></label>'
            + '<label class="pn-tiempo-parte"><span>Semanas</span><input type="number" min="0" max="' + Math.floor(tope / DIAS_POR_SEMANA) + '" class="pn-input" data-duracion-parte data-factor="7" value="' + p[1] + '"' + deshabilitado + '></label>'
            + '<label class="pn-tiempo-parte"><span>Días</span><input type="number" min="0" max="' + tope + '" class="pn-input" data-duracion-parte data-factor="1" value="' + p[2] + '"' + deshabilitado + '></label>'
            + '</div></div>';
    }

    function dibujarTabla() {
        var dia = 0;
        tabla.innerHTML = cronograma.map(function (fila, i) {
            var primero = dia + 1;
            dia += fila.dias;
            return '<tr><td>' + (i + 1) + '</td><td><span class="pn-celda-principal pn-truncar pn-truncar-largo" title="' + esc(temaDe(fila).titulo) + '">' + esc(temaDe(fila).titulo) + '</span></td>'
                + '<td>' + campoDuracion(fila, i) + '</td><td>' + periodo(primero, dia) + '</td>'
                + '</tr>';
        }).join('');
    }

    function dibujar() {
        vacio.hidden = cronograma.length > 0;
        calendario.hidden = vista !== 'calendario' || cronograma.length === 0;
        zona.hidden = vista !== 'calendario';
        tablaContenedor.classList.toggle('d-none', vista !== 'temas' || cronograma.length === 0);
        escalaContenedor.classList.toggle('invisible', vista !== 'calendario'); // invisible, no oculta: los botones no se mueven
        // Sin temas no hay nada que ver: las vistas y la escala aparecen con el primero
        barra.classList.toggle('d-none', cronograma.length === 0);
        formulario.classList.toggle('pn-prog-solo-lectura', soloLectura());
        errorCronograma.textContent = '';
        dibujarDisponibles();
        dibujarBalance();
        dibujarCalendario();
        dibujarTabla();
    }

    // ---------------------------------------------------------------- eventos del cronograma
    buscador.addEventListener('input', dibujarDisponibles);
    botonRepartir.addEventListener('click', function () {
        errorBuscador.textContent = '';
        if (!cronograma.length) { return; }
        repartirParejo();
        ajustado = false;
        dibujar();
    });
    selectEscala.addEventListener('change', function () {
        escala = parseInt(selectEscala.value, 10);
        dibujar();
    });
    formulario.querySelectorAll('[data-prog-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            vista = boton.dataset.progVista;
            formulario.querySelectorAll('[data-prog-vista]').forEach(function (otro) { otro.classList.toggle('activa', otro === boton); });
            dibujar();
        });
    });
    formulario.addEventListener('click', function (e) {
        // Con un clic en un tema disponible también se suma, al final
        var tarjeta = e.target.closest('.cn-disponible[data-u]');
        if (tarjeta && !tarjeta.classList.contains('usada') && !soloLectura()) { agregar(parseInt(tarjeta.dataset.u, 10)); }
    });

    // Duración escrita en meses, semanas y días (vista por temas): nunca baja de un día y lo que pasa de la duración del curso se recorta
    tabla.addEventListener('change', function (e) {
        var campo = e.target.closest('[data-duracion-parte]');
        if (!campo || soloLectura()) { return; }
        var fila = campo.closest('.cn-duracion-fila');
        var i = parseInt(fila.dataset.i, 10);
        var dias = 0;
        Array.prototype.forEach.call(fila.querySelectorAll('[data-duracion-parte]'), function (parte) {
            dias += Math.max(0, parseInt(parte.value, 10) || 0) * parseInt(parte.dataset.factor, 10);
        });
        if (dias <= 0) { dias = cronograma[i].dias; }
        cambiarDias(i, dias);
        dibujar();
    });

    // Arrastre del borde derecho de una barra: cambia la duración del tema (los siguientes se corren) sin pasar de la duración del curso
    calendario.addEventListener('pointerdown', function (e) {
        var borde = e.target.closest('.cn-borde');
        if (!borde || soloLectura()) { return; }
        e.preventDefault();
        e.stopPropagation();
        var fila = borde.closest('.cn-fila');
        fila.draggable = false;
        var i = parseInt(borde.dataset.i, 10);
        cambio = { i: i, x: e.clientX, dias: cronograma[i].dias, ancho: fila.querySelector('.cn-unidad').getBoundingClientRect().width };
        unidadesCongeladas = unidadesDeLaGrilla();
    });
    document.addEventListener('pointermove', function (e) {
        if (!cambio) { return; }
        var tope = MAXIMO - (total() - cronograma[cambio.i].dias);
        var saltos = Math.round((e.clientX - cambio.x) / cambio.ancho);
        var dias = Math.min(tope, Math.max(1, cambio.dias + saltos * escala));
        if (dias !== cronograma[cambio.i].dias) {
            cronograma[cambio.i].dias = dias;
            ajustado = true;
            dibujarBalance();
            dibujarCalendario();
            dibujarTabla();
        }
    });
    document.addEventListener('pointerup', function () {
        if (cambio) {
            cambio = null;
            unidadesCongeladas = 0;
            dibujar();
        }
    });

    // Arrastrar y soltar: las líneas se reordenan, un tema disponible se suma al soltarlo en una línea (queda antes de ella) o en la zona
    // de abajo (queda al final), y una línea soltada en el panel de la izquierda se saca del cronograma
    function limpiarMarcas() {
        editor.querySelectorAll('.sobre').forEach(function (e) { e.classList.remove('sobre'); });
    }

    editor.addEventListener('dragstart', function (e) {
        if (soloLectura()) { return; }
        var fila = e.target.closest('.cn-fila[data-i]');
        var disponible = e.target.closest('.cn-disponible[data-u]');
        if (fila) {
            arrastre = { tipo: 'fila', i: parseInt(fila.dataset.i, 10) };
            fila.classList.add('arrastrando');
        } else if (disponible) {
            arrastre = { tipo: 'disponible', id: parseInt(disponible.dataset.u, 10) };
        }
        if (arrastre) {
            e.dataTransfer.effectAllowed = 'move';
            e.dataTransfer.setData('text/plain', 'cronograma');
        }
    });
    editor.addEventListener('dragend', function () { arrastre = null; limpiarMarcas(); dibujarCalendario(); });
    editor.addEventListener('dragover', function (e) {
        if (!arrastre) { return; }
        var fila = e.target.closest('.cn-fila[data-i]');
        var destinoZona = e.target.closest('[data-prog-zona]');
        var panel = arrastre.tipo === 'fila' ? e.target.closest('[data-prog-panel]') : null;
        var destino = fila || destinoZona || panel;
        if (destino) {
            e.preventDefault();
            limpiarMarcas();
            destino.classList.add('sobre');
        }
    });
    editor.addEventListener('drop', function (e) {
        if (!arrastre) { return; }
        e.preventDefault();
        var fila = e.target.closest('.cn-fila[data-i]');
        var destinoZona = e.target.closest('[data-prog-zona]');
        var panel = e.target.closest('[data-prog-panel]');
        var actual = arrastre;
        arrastre = null;
        limpiarMarcas();
        if (actual.tipo === 'fila') {
            if (panel) {
                quitar(cronograma[actual.i].id);
            } else if (fila) {
                mover(actual.i, parseInt(fila.dataset.i, 10));
            } else if (destinoZona) {
                mover(actual.i, cronograma.length - 1);
            } else {
                dibujar();
            }
        } else if (fila || destinoZona) {
            agregar(actual.id, fila ? parseInt(fila.dataset.i, 10) : null);
        }
    });

    // ---------------------------------------------------------------- partir de un programa anterior
    function leerCronograma(cadena) {
        return (cadena || '').split(',').filter(Boolean).map(function (par) {
            var partesPar = par.split(':');
            return { id: parseInt(partesPar[0], 10), dias: parseInt(partesPar[1], 10) };
        }).filter(function (fila) { return temasPorId[fila.id]; });
    }

    if (selectOrigen) {
        selectOrigen.addEventListener('change', function () {
            var opcion = selectOrigen.selectedOptions[0];
            origenNombre = opcion && opcion.value ? opcion.dataset.nombre || '' : '';
            cronograma = leerCronograma(opcion ? opcion.dataset.cronograma || '' : '');
            ajustado = cronograma.length > 0;
            dibujar();
        });
    }

    // ---------------------------------------------------------------- abrir el asistente
    function abrir(programa) {
        edicion = programa;
        origenNombre = '';
        campoNombre.value = programa ? programa.nombre : '';
        campoDescripcion.value = programa ? programa.descripcion : '';
        [campoNombre, campoDescripcion].forEach(function (campo) {
            campo.dispatchEvent(new Event('input', { bubbles: true }));
            campo.disabled = !!(programa && programa.bloqueado);
            marcar(campo, '');
        });
        if (selectOrigen) { selectOrigen.value = ''; selectOrigen.dispatchEvent(new Event('change', { bubbles: true })); }
        if (contenedorOrigen) { contenedorOrigen.hidden = !!programa; }
        if (avisoBloqueado) {
            avisoBloqueado.hidden = !(programa && programa.bloqueado);
        }
        titulo.textContent = programa ? 'Editar programa' : 'Registrar programa';
        formulario.querySelector('[data-paso-guardar]').textContent = programa ? 'Confirmar cambios' : 'Confirmar y guardar';
        errorBuscador.textContent = '';
        buscador.value = '';
        vista = 'calendario';
        escala = escalaAutomatica();
        selectEscala.value = String(escala);
        formulario.querySelectorAll('[data-prog-vista]').forEach(function (b) { b.classList.toggle('activa', b.dataset.progVista === 'calendario'); });
        // El cambio del selector de origen vació el cronograma: se vuelve a cargar el del programa que se edita
        cronograma = programa ? leerCronograma(programa.cronograma) : [];
        ajustado = !!programa;
        configurarConfirmacion();
        dibujar();
        if (formulario.pasos) { formulario.pasos.mostrar(0); }
        if (window.bootstrap) { window.bootstrap.Modal.getOrCreateInstance(ventana).show(); }
    }

    function programaDe(boton) {
        return {
            id: parseInt(boton.dataset.id, 10), nombre: boton.dataset.nombre || '', descripcion: boton.dataset.descripcion || '',
            cronograma: boton.dataset.cronograma || '', bloqueado: boton.dataset.congelado === 'true', motivo: boton.dataset.motivo || ''
        };
    }

    document.querySelectorAll('[data-programa-nuevo]').forEach(function (boton) {
        boton.addEventListener('click', function () { abrir(null); });
    });
    // Delegado: las filas llegan del servidor al filtrar o cambiar de página
    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-programa-editar]');
        if (boton) { abrir(programaDe(boton)); }
    });

    // ---------------------------------------------------------------- resumen
    function crear(etiqueta, clase, html) {
        var e = document.createElement(etiqueta);
        if (clase) { e.className = clase; }
        if (html !== undefined) { e.innerHTML = html; }
        return e;
    }

    function nodo(color, tituloNodo, tipo, estado, extra) {
        var n = crear('div', 'pn-nodo pn-nodo-' + color);
        var fila = crear('div', 'pn-nodo-fila');
        fila.appendChild(crear('span', 'pn-punto'));
        fila.appendChild(crear('span', 'pn-nodo-titulo', tituloNodo));
        if (tipo) { fila.appendChild(crear('span', 'pn-chip pn-chip-' + tipo[0], esc(tipo[1]))); }
        if (estado) { fila.appendChild(crear('span', 'pn-chip pn-chip-estado pn-chip-' + estado[0], esc(estado[1]))); }
        if (extra) { fila.appendChild(extra); }
        n.appendChild(fila);
        return n;
    }

    function enlaceEditar(indice) {
        var boton = crear('button', 'pn-arbol-editar', '<i class="fa-solid fa-pen" aria-hidden="true"></i> Editar');
        boton.type = 'button';
        boton.addEventListener('click', function () { if (formulario.pasos) { formulario.pasos.mostrar(indice); } });
        return boton;
    }

    /** Una línea de tema del cronograma: «Título (14 días)». */
    function lineaDeTema(fila) {
        return fila ? temasPorId[fila.id].titulo + ' (' + texto(fila.dias) + ')' : '';
    }

    function dibujarResumen() {
        arbol.innerHTML = '';
        var alta = !edicion;
        var nombre = campoNombre.value.trim();
        var descripcion = campoDescripcion.value.trim();
        var basicos = [];       // { etiqueta, antes, despues }
        var lineasCronograma = [];
        var hayCohortes = alta && datos.cohortesPendientes && datos.cohortesPendientes.length > 0;
        if (alta) {
            basicos.push({ etiqueta: 'Nombre', despues: nombre });
            if (descripcion) { basicos.push({ etiqueta: 'Descripción', despues: descripcion }); }
            if (origenNombre) { basicos.push({ etiqueta: 'Parte de', despues: origenNombre }); }
            cronograma.forEach(function (fila, i) { lineasCronograma.push({ etiqueta: 'Tema ' + (i + 1), despues: lineaDeTema(fila) }); });
        } else {
            if (nombre !== edicion.nombre) { basicos.push({ etiqueta: 'Nombre', antes: edicion.nombre, despues: nombre }); }
            if (descripcion !== edicion.descripcion) { basicos.push({ etiqueta: 'Descripción', antes: edicion.descripcion, despues: descripcion }); }
            var antes = leerCronograma(edicion.cronograma);
            var maximo = Math.max(antes.length, cronograma.length);
            for (var i = 0; i < maximo; i++) {
                var a = lineaDeTema(antes[i]);
                var d = lineaDeTema(cronograma[i]);
                if (a !== d) { lineasCronograma.push({ etiqueta: 'Tema ' + (i + 1), antes: a, despues: d }); }
            }
        }

        var l = crear('div', 'pn-arbol-leyenda');
        [['oro', 'Curso'], ['azul', 'Programa'], ['oro', 'Cronograma'], ['verde', alta ? 'Lo que se registra' : 'Lo que se modifica']].forEach(function (e) {
            l.appendChild(crear('span', 'pn-leyenda-item pn-nodo-' + e[0], '<i></i>' + esc(e[1])));
        });
        arbol.appendChild(l);

        var curso = nodo('oro', esc(formulario.dataset.curso), ['oro', 'Curso']);
        curso.classList.add('pn-nodo-primero');
        arbol.appendChild(curso);
        var cambios = basicos.length + lineasCronograma.length;
        var programa = nodo('azul', esc(nombre || (edicion ? edicion.nombre : 'Programa')), ['azul', 'Programa'],
            alta ? ['verde', 'Nuevo'] : (cambios ? ['naranja', cambios + (cambios === 1 ? ' cambio' : ' cambios')] : ['gris', 'Sin cambios']));
        programa.classList.add('pn-nodo-tercero');
        curso.appendChild(programa);
        notaCohortes.hidden = !hayCohortes;
        if (!alta && !cambios) {
            programa.appendChild(crear('p', 'pn-arbol-sin-cambios', 'No hay cambios para guardar'));
            return;
        }
        function lineas(nodoPadre, lista) {
            lista.forEach(function (linea) {
                if (alta) {
                    nodoPadre.appendChild(crear('div', 'pn-arbol-dato pn-dato-nuevo', (linea.etiqueta ? esc(linea.etiqueta) + ': ' : '') + '<strong>' + esc(linea.despues || '—') + '</strong>'));
                } else {
                    nodoPadre.appendChild(crear('div', 'pn-arbol-dato pn-dato-cambio', esc(linea.etiqueta) + ': <span class="pn-arbol-antes">'
                        + esc(linea.antes || '—') + '</span><span class="pn-arbol-flecha">→</span><strong>' + esc(linea.despues || '—') + '</strong>'));
                }
            });
        }
        // Datos del programa: Datos básicos y las cohortes a las que se asigna (verde, como lo que se registra)
        if (basicos.length) {
            var datosBasicos = nodo(alta ? 'verde' : 'naranja', 'Datos básicos', null, null, alta ? enlaceEditar(0) : null);
            datosBasicos.classList.add('pn-nodo-grupo');
            lineas(datosBasicos, basicos);
            programa.appendChild(datosBasicos);
        }
        if (hayCohortes) {
            var cohortes = nodo('verde', 'Cohortes asignadas');
            cohortes.classList.add('pn-nodo-grupo');
            lineas(cohortes, datos.cohortesPendientes.map(function (etiqueta) { return { etiqueta: '', despues: etiqueta }; }));
            programa.appendChild(cohortes);
        }
        // El cronograma es una rama del árbol (con su propia leyenda); sus temas van en un grupo verde, "Temas asignados"
        if (lineasCronograma.length) {
            var ramaCronograma = nodo('oro', 'Cronograma', ['oro', 'Cronograma']);
            var grupoTemas = nodo(alta ? 'verde' : 'naranja', 'Temas asignados', null, null, alta ? enlaceEditar(1) : null);
            grupoTemas.classList.add('pn-nodo-grupo');
            lineas(grupoTemas, lineasCronograma);
            ramaCronograma.appendChild(grupoTemas);
            programa.appendChild(ramaCronograma);
        }
    }

    var pasoResumen = arbol.closest('.pn-paso');
    new MutationObserver(function () {
        if (pasoResumen.classList.contains('activo')) { dibujarResumen(); }
    }).observe(pasoResumen, { attributes: true, attributeFilter: ['class'] });

    // ---------------------------------------------------------------- validación y envío
    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.programa = function (paso) {
        if (paso !== 2 || soloLectura()) { return true; }
        var accion = edicion ? 'modificar' : 'registrar';
        var nombre = campoNombre.value.trim() || (edicion ? edicion.nombre : '');
        var sujeto = 'Error! No se puede ' + accion + ' el programa' + (nombre ? ' «' + nombre + '»' : '') + ': ';
        var minimo = datos.minimoTemas || 1;
        if (cronograma.length < minimo) {
            errorCronograma.textContent = sujeto + 'tiene ' + plural(cronograma.length, 'tema', 'temas') + ' y hacen falta al menos ' + minimo
                + '. Sumá temas con el contenido completo hasta llegar al mínimo.';
            return false;
        }
        var asignado = total();
        if (asignado !== MAXIMO) {
            errorCronograma.textContent = sujeto + 'asigna ' + texto(asignado) + ' y el curso dura ' + texto(MAXIMO)
                + '. Asigná entre los temas los ' + texto(MAXIMO) + ' del curso.';
            return false;
        }
        errorCronograma.textContent = '';
        return true;
    };

    /** Si el programa nuevo se asigna a cohortes pendientes, guardarlo finaliza su diseño: se pide confirmación antes de enviar. */
    function configurarConfirmacion() {
        var cohortes = !edicion && datos.cohortesPendientes ? datos.cohortesPendientes : [];
        if (!cohortes.length) {
            delete formulario.dataset.confirmarTitulo;
            delete formulario.dataset.confirmarHtml;
            return;
        }
        var nombres = cohortes.map(function (c) { return '«' + esc(c) + '»'; });
        var lista = nombres.length === 1 ? nombres[0] : nombres.slice(0, -1).join(', ') + ' y ' + nombres[nombres.length - 1];
        var uno = cohortes.length === 1;
        formulario.dataset.confirmarTitulo = '¿Deseás finalizar el diseño?';
        formulario.dataset.confirmarBoton = 'Finalizar diseño';
        formulario.dataset.confirmarHtml = 'El programa «' + esc(campoNombre.value.trim() || 'sin nombre') + '» se asignará a ' + (uno ? 'la cohorte ' : 'las cohortes ')
            + lista + ', que ' + (uno ? 'quedará publicada' : 'quedarán publicadas') + ' en el catálogo. Lo publicado es un compromiso de la empresa con los alumnos '
            + 'que lo consultan o ya se inscribieron, por eso ya no se podrán modificar los datos del curso. '
            + '<strong>Si te equivocás, solicitá al administrador la baja de la cohorte para crearla de nuevo.</strong>';
    }

    /** Deja en el formulario lo que se envía: la acción (registrar o modificar) y los temas con sus días, en orden. */
    function prepararEnvio() {
        configurarConfirmacion(); // con el nombre que quedó escrito
        formulario.action = edicion ? formulario.dataset.accionModificar + edicion.id : formulario.dataset.accionAlta;
        formulario.querySelectorAll('[data-generado]').forEach(function (campo) { campo.remove(); });
        cronograma.forEach(function (fila) {
            [['ids', fila.id], ['dias', fila.dias]].forEach(function (par) {
                var oculto = document.createElement('input');
                oculto.type = 'hidden';
                oculto.name = par[0];
                oculto.value = par[1];
                oculto.setAttribute('data-generado', '');
                formulario.appendChild(oculto);
            });
        });
    }

    formulario.addEventListener('submit', prepararEnvio);

    // ---------------------------------------------------------------- apertura desde la dirección
    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && principal.dataset.abrirModal === 'modalPrograma') {
        var boton = principal.dataset.editar ? document.querySelector('[data-programa-editar][data-id="' + principal.dataset.editar + '"]') : null;
        abrir(boton ? programaDe(boton) : null);
        var direccion = new URL(window.location.href);
        ['nuevo', 'editar'].forEach(function (nombreParametro) { direccion.searchParams.delete(nombreParametro); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    selectEscala.value = String(escala);
    dibujar();
})();
