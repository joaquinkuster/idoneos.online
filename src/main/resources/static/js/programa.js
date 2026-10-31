/**
 * Asistente de programas del docente (MOD-F-02, CU-15 a CU-18 y cronograma del programa).
 *
 *  1. Un único asistente registra y edita programas, en cuatro pasos: Datos básicos, Cronograma, Contenido y Resumen.
 *  2. El cronograma se arma con los temas del curso ("Temas disponibles") y se ve como calendario (filas que se reordenan
 *     con el asa y barras cuyo borde derecho se arrastra para cambiar la duración) o como tabla por temas. Una barra
 *     arriba muestra cuánto de la duración del curso está asignado. Mientras no se ajuste a mano, la duración se reparte
 *     por igual entre los temas; un tema que se suma después toma lo que queda sin asignar (o una unidad de la escala).
 *  3. El paso Contenido muestra qué tiene cada tema; lo que falta se registra desde un enlace. Como salir del asistente
 *     deja la tarea a medias, antes se ofrece guardar el programa o seguir editando.
 *  4. Si la página se abre con ?nuevo o ?editar=<id>, abre sola el asistente; se quitan esos parámetros de la dirección.
 */
(function () {
    'use strict';

    var ventana = document.getElementById('modalPrograma');
    var formulario = ventana && ventana.querySelector('[data-programa-formulario]');
    var datos = window.PROGRAMA_DATOS;
    if (!formulario || !datos) { return; }

    var DIAS_POR_MES = 30;
    var DIAS_POR_SEMANA = 7;
    var TITULOS_DE_COLUMNA = {
        'Video clon': 'Video con el clon del docente. Lo graba el sistema a partir de lo que registra el docente.',
        'Clase grabada': 'Grabación de la clase en vivo. Solo el docente que la dicta puede registrarla.',
        'Material': 'Material de lectura o de práctica del tema.',
        'Autoevaluación': 'Autoevaluación con la que el alumno controla lo aprendido.'
    };

    var campoNombre = formulario.querySelector('[name="nombre"]');
    var campoDescripcion = formulario.querySelector('[name="descripcion"]');
    var selectOrigen = formulario.querySelector('[data-programa-origen-select]');
    var contenedorOrigen = formulario.querySelector('[data-programa-origen]');
    var calendario = formulario.querySelector('[data-prog-calendario]');
    var tabla = formulario.querySelector('[data-prog-tabla]');
    var tablaContenedor = formulario.querySelector('[data-prog-tabla-contenedor]');
    var vacio = formulario.querySelector('[data-prog-vacio]');
    var errorCronograma = formulario.querySelector('[data-prog-error]');
    var disponibles = formulario.querySelector('[data-prog-disponibles]');
    var buscador = formulario.querySelector('[data-prog-buscar]');
    var selectEscala = formulario.querySelector('[data-prog-escala]');
    var escalaContenedor = formulario.querySelector('[data-prog-escala-contenedor]');
    var balanceTexto = formulario.querySelector('[data-prog-balance-texto]');
    var balancePorcentaje = formulario.querySelector('[data-prog-balance-porcentaje]');
    var balanceBarra = formulario.querySelector('[data-prog-balance-barra]');
    var contenidoCabecera = formulario.querySelector('[data-prog-contenido-cabecera]');
    var contenidoCuerpo = formulario.querySelector('[data-prog-contenido]');
    var contenidoVacio = formulario.querySelector('[data-prog-contenido-vacio]');
    var arbol = formulario.querySelector('[data-prog-arbol]');
    var titulo = ventana.querySelector('[data-programa-titulo]');

    var temasPorId = {};
    datos.temas.forEach(function (tema) { temasPorId[tema.id] = tema; });

    var cronograma = [];        // { id, dias }
    var ajustado = false;       // si el docente ya cambió a mano alguna duración
    var escala = DIAS_POR_SEMANA;
    var vista = 'calendario';
    var edicion = null;         // { id, nombre, descripcion, cronograma } del programa que se edita; null si se registra
    var origenCronograma = '';
    var arrastrado = null;

    // ---------------------------------------------------------------- utilidades
    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function plural(cantidad, singular, pluralTexto) {
        return cantidad + ' ' + (cantidad === 1 ? singular : pluralTexto);
    }

    /** Mismo formato que DuracionUtilidad.formatear del servidor: la unidad más grande que expresa la duración exacta. */
    function texto(dias) {
        if (dias > 0 && dias % DIAS_POR_MES === 0) { return plural(dias / DIAS_POR_MES, 'mes', 'meses'); }
        if (dias > 0 && dias % DIAS_POR_SEMANA === 0) { return plural(dias / DIAS_POR_SEMANA, 'semana', 'semanas'); }
        return plural(dias, 'día', 'días');
    }

    function normalizar(valor) {
        return String(valor || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
    }

    function total() {
        return cronograma.reduce(function (suma, fila) { return suma + fila.dias; }, 0);
    }

    function temaDe(fila) { return temasPorId[fila.id]; }

    function marcar(campo, mensaje) {
        if (window.PanelValidacion && window.PanelValidacion.marcar) { window.PanelValidacion.marcar(campo, mensaje); }
    }

    function unidadDeEscala() {
        return escala === DIAS_POR_MES ? 'meses' : (escala === DIAS_POR_SEMANA ? 'semanas' : 'días');
    }

    function enUnidades(dias) {
        var valor = dias / escala;
        return Math.round(valor * 10) / 10;
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
    function agregar(id) {
        if (cronograma.some(function (fila) { return fila.id === id; })) { return; }
        if (!ajustado) {
            cronograma.push({ id: id, dias: 1 });
            repartirParejo();
        } else {
            var sinAsignar = datos.duracionDias - total();
            cronograma.push({ id: id, dias: sinAsignar > 0 ? sinAsignar : escala });
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

    function cambiarDias(indice, dias) {
        cronograma[indice].dias = Math.max(1, dias);
        ajustado = true;
    }

    // ---------------------------------------------------------------- dibujo
    function dibujarDisponibles() {
        var buscado = normalizar(buscador.value);
        var usados = {};
        cronograma.forEach(function (fila) { usados[fila.id] = true; });
        var html = datos.temas.filter(function (tema) {
            return !usados[tema.id] && (!buscado || normalizar(tema.titulo).indexOf(buscado) >= 0);
        }).map(function (tema) {
            return '<button type="button" class="pn-prog-disponible" data-agregar="' + tema.id + '"><span>' + esc(tema.titulo)
                + '</span><i class="fa-solid fa-plus"></i></button>';
        }).join('');
        disponibles.innerHTML = html || '<p class="small text-muted mb-0">'
            + (datos.temas.length ? 'No hay más temas para sumar.' : 'El curso todavía no tiene temas.') + '</p>';
    }

    function dibujarBalance() {
        var asignado = total();
        var proporcion = datos.duracionDias ? asignado / datos.duracionDias : 0;
        var porcentaje = Math.round(proporcion * 100);
        balanceTexto.textContent = 'Asignadas ' + enUnidades(asignado) + ' de ' + enUnidades(datos.duracionDias) + ' ' + unidadDeEscala();
        balancePorcentaje.textContent = porcentaje + '%';
        balanceBarra.style.width = Math.min(100, porcentaje) + '%';
        var estado = asignado === datos.duracionDias ? 'completo' : (asignado > datos.duracionDias ? 'excedido' : 'parcial');
        balanceBarra.parentNode.parentNode.setAttribute('data-estado', estado);
    }

    function dibujarCalendario() {
        var asignado = total();
        var horizonte = Math.max(datos.duracionDias, asignado);
        horizonte = Math.ceil(horizonte / escala) * escala;
        var unidades = horizonte / escala;
        var fuera = Math.round(datos.duracionDias / horizonte * 1000) / 10;
        var regla = '';
        var paso = unidades > 24 ? Math.ceil(unidades / 12) : 1;
        for (var u = 0; u < unidades; u += paso) {
            regla += '<span style="left:' + (u / unidades * 100) + '%">' + (u + 1) + '</span>';
        }
        var html = '<div class="pn-prog-fila pn-prog-regla"><div class="pn-prog-rotulo">'
            + (escala === DIAS_POR_MES ? 'Mes' : (escala === DIAS_POR_SEMANA ? 'Semana' : 'Día')) + '</div>'
            + '<div class="pn-prog-pista" style="--fuera:' + fuera + '%">' + regla + '</div></div>';
        var inicio = 0;
        cronograma.forEach(function (fila, i) {
            var tema = temaDe(fila);
            html += '<div class="pn-prog-fila" data-indice="' + i + '">'
                + '<div class="pn-prog-rotulo">'
                + '<button type="button" class="pn-prog-asa" aria-label="Mover «' + esc(tema.titulo) + '»" title="Arrastrá para reordenar"><i class="fa-solid fa-grip-vertical"></i></button>'
                + '<span class="pn-prog-orden">' + (i + 1) + '</span><span class="pn-prog-nombre" title="' + esc(tema.titulo) + '">' + esc(tema.titulo) + '</span>'
                + '<button type="button" class="pn-prog-quitar" data-quitar="' + fila.id + '" aria-label="Quitar «' + esc(tema.titulo) + '»" title="Quitar del cronograma"><i class="fa-solid fa-xmark"></i></button></div>'
                + '<div class="pn-prog-pista" style="--fuera:' + fuera + '%"><div class="pn-prog-barra" style="left:' + (inicio / horizonte * 100)
                + '%;width:' + (fila.dias / horizonte * 100) + '%" title="' + esc(texto(fila.dias)) + '"><span>' + esc(texto(fila.dias))
                + '</span><i class="pn-prog-borde" data-redimensionar="' + i + '" title="Arrastrá para cambiar la duración"></i></div></div></div>';
            inicio += fila.dias;
        });
        calendario.innerHTML = html;
        calendario.dataset.horizonte = horizonte;
    }

    function descomponer(dias) {
        var meses = Math.floor(dias / DIAS_POR_MES);
        var semanas = Math.floor((dias % DIAS_POR_MES) / DIAS_POR_SEMANA);
        return [meses, semanas, (dias % DIAS_POR_MES) % DIAS_POR_SEMANA];
    }

    function periodo(inicio, dias) {
        return dias === 1 ? 'Día ' + (inicio + 1) : 'Días ' + (inicio + 1) + ' a ' + (inicio + dias);
    }

    function dibujarTabla() {
        var inicio = 0;
        tabla.innerHTML = cronograma.map(function (fila, i) {
            var partes = descomponer(fila.dias);
            var celdas = partes.map(function (valor, j) {
                return '<td><input type="number" class="pn-input pn-prog-numero" min="0" value="' + valor + '" data-parte="' + j + '" data-indice="' + i + '" aria-label="' + ['Meses', 'Semanas', 'Días'][j] + '"></td>';
            }).join('');
            var celda = '<tr data-indice="' + i + '"><td>' + (i + 1) + '</td><td>' + esc(temaDe(fila).titulo) + '</td>' + celdas
                + '<td data-periodo>' + periodo(inicio, fila.dias) + '</td></tr>';
            inicio += fila.dias;
            return celda;
        }).join('');
    }

    /** Actualiza los períodos de la tabla sin volver a dibujarla (para no perder el foco al escribir). */
    function actualizarPeriodos() {
        var inicio = 0;
        tabla.querySelectorAll('tr').forEach(function (tr, i) {
            tr.querySelector('[data-periodo]').textContent = periodo(inicio, cronograma[i].dias);
            inicio += cronograma[i].dias;
        });
    }

    function dibujarContenido() {
        contenidoCabecera.innerHTML = '<th>Tema</th>' + datos.columnas.map(function (columna) {
            return '<th title="' + esc(TITULOS_DE_COLUMNA[columna] || '') + '">' + esc(columna) + '</th>';
        }).join('');
        contenidoCuerpo.innerHTML = cronograma.map(function (fila) {
            var tema = temaDe(fila);
            return '<tr><td>' + esc(tema.titulo) + '</td>' + datos.columnas.map(function (columna) {
                if (tema.contenido[columna]) { return '<td>Sí</td>'; }
                var url = (columna === 'Autoevaluación' ? window.PROGRAMA_URLS.autoevaluacion : window.PROGRAMA_URLS.material)
                    + '?cursoId=' + datos.cursoId + (columna === 'Autoevaluación' ? '' : '&temaId=' + tema.id);
                return '<td><a href="' + url + '" class="pn-prog-registrar" data-salida>Registrar</a></td>';
            }).join('') + '</tr>';
        }).join('');
        contenidoVacio.hidden = cronograma.length > 0;
    }

    function dibujar() {
        vacio.hidden = cronograma.length > 0;
        calendario.hidden = vista !== 'calendario' || cronograma.length === 0;
        tablaContenedor.classList.toggle('d-none', vista !== 'temas' || cronograma.length === 0);
        escalaContenedor.hidden = vista !== 'calendario';
        errorCronograma.textContent = '';
        dibujarDisponibles();
        dibujarBalance();
        dibujarCalendario();
        dibujarTabla();
        dibujarContenido();
    }

    // ---------------------------------------------------------------- eventos del cronograma
    buscador.addEventListener('input', dibujarDisponibles);
    selectEscala.addEventListener('change', function () {
        escala = parseInt(selectEscala.value, 10);
        dibujar();
    });
    formulario.querySelectorAll('[data-prog-vista]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            vista = boton.dataset.progVista;
            formulario.querySelectorAll('[data-prog-vista]').forEach(function (otro) { otro.classList.toggle('activo', otro === boton); });
            dibujar();
        });
    });
    disponibles.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-agregar]');
        if (boton) { agregar(parseInt(boton.dataset.agregar, 10)); }
    });
    calendario.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-quitar]');
        if (boton) { quitar(parseInt(boton.dataset.quitar, 10)); }
    });
    tabla.addEventListener('input', function (e) {
        var entrada = e.target.closest('input[data-parte]');
        if (!entrada) { return; }
        var fila = entrada.closest('tr');
        var valores = Array.prototype.map.call(fila.querySelectorAll('input[data-parte]'), function (i) { return Math.max(0, parseInt(i.value, 10) || 0); });
        cambiarDias(parseInt(fila.dataset.indice, 10), valores[0] * DIAS_POR_MES + valores[1] * DIAS_POR_SEMANA + valores[2]);
        actualizarPeriodos();
        dibujarBalance();
        dibujarCalendario();
    });
    tabla.addEventListener('change', dibujarTabla);

    // Arrastre del borde derecho de una barra: cambia la duración del tema (los siguientes se corren)
    calendario.addEventListener('pointerdown', function (e) {
        var borde = e.target.closest('[data-redimensionar]');
        if (!borde) { return; }
        e.preventDefault();
        var indice = parseInt(borde.dataset.redimensionar, 10);
        var pista = borde.closest('.pn-prog-pista');
        var horizonte = parseInt(calendario.dataset.horizonte, 10);
        var porDia = pista.getBoundingClientRect().width / horizonte;
        var inicioX = e.clientX;
        var inicioDias = cronograma[indice].dias;
        function mover(evento) {
            var desplazados = Math.round((evento.clientX - inicioX) / porDia / escala) * escala;
            var nuevo = Math.max(escala > 1 ? escala : 1, inicioDias + desplazados);
            if (nuevo === cronograma[indice].dias) { return; }
            cambiarDias(indice, nuevo);
            dibujarBalance();
            dibujarCalendario();
            dibujarTabla();
            calendario.dataset.horizonte = horizonte;
        }
        function soltar() {
            document.removeEventListener('pointermove', mover);
            document.removeEventListener('pointerup', soltar);
            dibujar();
        }
        document.addEventListener('pointermove', mover);
        document.addEventListener('pointerup', soltar);
    });

    // Reordenar con el asa: arrastrar la fila (o, con el teclado, las flechas arriba y abajo)
    calendario.addEventListener('pointerdown', function (e) {
        var asa = e.target.closest('.pn-prog-asa');
        if (asa) { asa.closest('.pn-prog-fila').draggable = true; }
    });
    calendario.addEventListener('dragstart', function (e) {
        var fila = e.target.closest ? e.target.closest('.pn-prog-fila[data-indice]') : null;
        if (!fila) { return; }
        arrastrado = parseInt(fila.dataset.indice, 10);
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', String(arrastrado));
        fila.classList.add('arrastrando');
    });
    calendario.addEventListener('dragover', function (e) {
        if (arrastrado === null) { return; }
        e.preventDefault();
        var fila = e.target.closest('.pn-prog-fila[data-indice]');
        calendario.querySelectorAll('.destino').forEach(function (otra) { otra.classList.remove('destino'); });
        if (fila) { fila.classList.add('destino'); }
    });
    calendario.addEventListener('drop', function (e) {
        if (arrastrado === null) { return; }
        e.preventDefault();
        var fila = e.target.closest('.pn-prog-fila[data-indice]');
        var desde = arrastrado;
        arrastrado = null;
        if (fila) { mover(desde, parseInt(fila.dataset.indice, 10)); } else { dibujar(); }
    });
    calendario.addEventListener('dragend', function () {
        arrastrado = null;
        dibujarCalendario();
    });
    calendario.addEventListener('keydown', function (e) {
        var asa = e.target.closest('.pn-prog-asa');
        if (!asa || (e.key !== 'ArrowUp' && e.key !== 'ArrowDown')) { return; }
        e.preventDefault();
        var indice = parseInt(asa.closest('.pn-prog-fila').dataset.indice, 10);
        mover(indice, indice + (e.key === 'ArrowUp' ? -1 : 1));
        var nueva = calendario.querySelector('.pn-prog-fila[data-indice="' + Math.max(0, Math.min(cronograma.length - 1, indice + (e.key === 'ArrowUp' ? -1 : 1))) + '"] .pn-prog-asa');
        if (nueva) { nueva.focus(); }
    });

    // ---------------------------------------------------------------- partir de un programa anterior
    function leerCronograma(cadena) {
        return (cadena || '').split(',').filter(Boolean).map(function (par) {
            var partes = par.split(':');
            return { id: parseInt(partes[0], 10), dias: parseInt(partes[1], 10) };
        }).filter(function (fila) { return temasPorId[fila.id]; });
    }

    if (selectOrigen) {
        selectOrigen.addEventListener('change', function () {
            var opcion = selectOrigen.selectedOptions[0];
            origenCronograma = opcion ? opcion.dataset.cronograma || '' : '';
            cronograma = leerCronograma(origenCronograma);
            ajustado = cronograma.length > 0;
            dibujar();
        });
    }

    // ---------------------------------------------------------------- abrir el asistente
    function abrir(programa) {
        edicion = programa;
        cronograma = programa ? leerCronograma(programa.cronograma) : [];
        ajustado = !!programa;
        origenCronograma = '';
        campoNombre.value = programa ? programa.nombre : '';
        campoDescripcion.value = programa ? programa.descripcion : '';
        [campoNombre, campoDescripcion].forEach(function (campo) {
            campo.dispatchEvent(new Event('input', { bubbles: true }));
            marcar(campo, '');
        });
        if (selectOrigen) { selectOrigen.value = ''; selectOrigen.disabled = !!programa; }
        if (contenedorOrigen) { contenedorOrigen.hidden = !!programa; }
        titulo.textContent = programa ? 'Editar programa' : 'Registrar programa';
        formulario.querySelector('[data-paso-guardar]').textContent = programa ? 'Confirmar cambios' : 'Confirmar y guardar';
        buscador.value = '';
        vista = 'calendario';
        formulario.querySelectorAll('[data-prog-vista]').forEach(function (b) { b.classList.toggle('activo', b.dataset.progVista === 'calendario'); });
        dibujar();
        if (formulario.pasos) { formulario.pasos.mostrar(0); }
        if (window.bootstrap) { window.bootstrap.Modal.getOrCreateInstance(ventana).show(); }
    }

    function programaDe(boton) {
        return { id: parseInt(boton.dataset.id, 10), nombre: boton.dataset.nombre || '', descripcion: boton.dataset.descripcion || '', cronograma: boton.dataset.cronograma || '' };
    }

    document.querySelectorAll('[data-programa-nuevo]').forEach(function (boton) {
        boton.addEventListener('click', function () { abrir(null); });
    });
    document.querySelectorAll('[data-programa-editar]').forEach(function (boton) {
        boton.addEventListener('click', function () { if (!boton.disabled) { abrir(programaDe(boton)); } });
    });

    // ---------------------------------------------------------------- resumen
    function crear(etiqueta, clase, html) {
        var e = document.createElement(etiqueta);
        if (clase) { e.className = clase; }
        if (html !== undefined) { e.innerHTML = html; }
        return e;
    }

    function nodo(color, tituloNodo, tipo, estado) {
        var n = crear('div', 'pn-nodo pn-nodo-' + color);
        var fila = crear('div', 'pn-nodo-fila');
        fila.appendChild(crear('span', 'pn-punto'));
        fila.appendChild(crear('span', 'pn-nodo-titulo', tituloNodo));
        if (tipo) { fila.appendChild(crear('span', 'pn-chip pn-chip-' + tipo[0], esc(tipo[1]))); }
        if (estado) { fila.appendChild(crear('span', 'pn-chip pn-chip-estado pn-chip-' + estado[0], esc(estado[1]))); }
        n.appendChild(fila);
        return n;
    }

    function textoCronograma(filas) {
        return filas.length ? filas.map(function (fila, i) {
            return (i + 1) + '. ' + temasPorId[fila.id].titulo + ' (' + texto(fila.dias) + ')';
        }).join(' · ') : 'Sin temas';
    }

    function dibujarResumen() {
        arbol.innerHTML = '';
        var alta = !edicion;
        var nombre = campoNombre.value.trim();
        var descripcion = campoDescripcion.value.trim();
        var origen = selectOrigen && selectOrigen.value ? selectOrigen.selectedOptions[0].textContent.replace(/\s*\(.*\)$/, '') : '';
        var lineas = [];   // { grupo, etiqueta, antes, despues }
        if (alta) {
            lineas.push({ grupo: 'Datos básicos', etiqueta: 'Nombre', despues: nombre });
            if (descripcion) { lineas.push({ grupo: 'Datos básicos', etiqueta: '¿Qué cambia?', despues: descripcion }); }
            if (origen) { lineas.push({ grupo: 'Datos básicos', etiqueta: 'Parte de', despues: origen }); }
            lineas.push({ grupo: 'Cronograma', etiqueta: 'Temas', despues: textoCronograma(cronograma) });
        } else {
            if (nombre !== edicion.nombre) { lineas.push({ grupo: 'Datos básicos', etiqueta: 'Nombre', antes: edicion.nombre, despues: nombre }); }
            if (descripcion !== edicion.descripcion) { lineas.push({ grupo: 'Datos básicos', etiqueta: '¿Qué cambia?', antes: edicion.descripcion, despues: descripcion }); }
            var antesCron = textoCronograma(leerCronograma(edicion.cronograma));
            var despuesCron = textoCronograma(cronograma);
            if (antesCron !== despuesCron) { lineas.push({ grupo: 'Cronograma', etiqueta: 'Temas', antes: antesCron, despues: despuesCron }); }
        }
        var asignado = total();
        lineas.push({ grupo: 'Cronograma', etiqueta: 'Duración asignada', despues: texto(asignado || 0) + ' de ' + texto(datos.duracionDias), solo: true });

        var curso = nodo('oro', esc(formulario.dataset.curso), ['oro', 'Curso']);
        curso.classList.add('pn-nodo-primero');
        arbol.appendChild(curso);
        var cambios = lineas.filter(function (l) { return !l.solo; }).length;
        var programa = nodo('azul', esc(nombre || (edicion ? edicion.nombre : 'Programa')), ['azul', 'Programa'],
            alta ? ['verde', 'Nuevo'] : (cambios ? ['naranja', cambios + (cambios === 1 ? ' cambio' : ' cambios')] : ['gris', 'Sin cambios']));
        programa.classList.add('pn-nodo-tercero');
        curso.appendChild(programa);
        if (!alta && !cambios) {
            programa.appendChild(crear('p', 'pn-arbol-sin-cambios', 'No hay cambios para guardar'));
            return;
        }
        var grupos = {};
        lineas.forEach(function (linea) {
            var grupo = grupos[linea.grupo];
            if (!grupo) {
                grupo = nodo(alta ? 'verde' : 'naranja', esc(linea.grupo));
                grupo.classList.add('pn-nodo-grupo');
                grupos[linea.grupo] = grupo;
                programa.appendChild(grupo);
            }
            if (alta || linea.solo) {
                grupo.appendChild(crear('div', 'pn-arbol-dato' + (alta && !linea.solo ? ' pn-dato-nuevo' : ' pn-dato-igual'),
                    esc(linea.etiqueta) + ': <strong>' + esc(linea.despues || '—') + '</strong>'));
            } else {
                grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-cambio', esc(linea.etiqueta) + ': <span class="pn-arbol-antes">'
                    + esc(linea.antes || '—') + '</span><span class="pn-arbol-flecha">→</span><strong>' + esc(linea.despues || '—') + '</strong>'));
            }
        });
    }

    var pasoResumen = arbol.closest('.pn-paso');
    new MutationObserver(function () {
        if (pasoResumen.classList.contains('activo')) { dibujarResumen(); }
    }).observe(pasoResumen, { attributes: true, attributeFilter: ['class'] });

    // ---------------------------------------------------------------- validación y envío
    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.programa = function (paso) {
        if (paso !== 2) { return true; }
        if (total() > datos.duracionDias) {
            errorCronograma.textContent = 'Los temas suman ' + texto(total()) + ', más que los ' + texto(datos.duracionDias) + ' del curso. Acortá alguno.';
            return false;
        }
        errorCronograma.textContent = '';
        return true;
    };

    /** Deja en el formulario lo que se envía: la acción (registrar o modificar) y los temas con sus días, en orden. */
    function prepararEnvio() {
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

    /** Guarda el programa tal como está y, si sale bien, va al destino (la alerta de salida ofrece esta opción). */
    async function guardarYSalir(destino) {
        if (!campoNombre.value.trim()) {
            if (formulario.pasos) { formulario.pasos.mostrar(0); }
            marcar(campoNombre, 'Debe completar el nombre.');
            return;
        }
        if (!window.PanelValidadores.programa(2)) {
            if (formulario.pasos) { formulario.pasos.mostrar(1); }
            return;
        }
        prepararEnvio();
        try {
            var respuesta = await fetch(formulario.action, {
                method: 'POST',
                body: new FormData(formulario),
                headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var cuerpo = await respuesta.json().catch(function () { return {}; });
            if (respuesta.ok && cuerpo.mensaje) {
                window.location.href = destino;
                return;
            }
            window.mostrarError(cuerpo.error || 'Error! No se pudo guardar el programa.');
        } catch (error) {
            window.mostrarError('Error! No se pudo conectar con el servidor. Intente nuevamente.');
        }
    }

    // Los enlaces que sacan del asistente (Nuevo tema, Registrar…) piden confirmación antes de salir
    ventana.addEventListener('click', function (e) {
        var enlace = e.target.closest('a[data-salida]');
        if (!enlace) { return; }
        e.preventDefault();
        var destino = enlace.href;
        window.confirmarAccion('Vas a salir del asistente', 'El programa todavía no está guardado. ¿Querés guardarlo antes de salir?',
            'Guardar y salir', 'Seguir editando').then(function (guardar) {
            if (guardar) { guardarYSalir(destino); }
        });
    });

    // ---------------------------------------------------------------- apertura desde la dirección
    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && principal.dataset.abrirModal === 'modalPrograma') {
        var boton = principal.dataset.editar ? document.querySelector('[data-programa-editar][data-id="' + principal.dataset.editar + '"]') : null;
        abrir(boton && !boton.disabled ? programaDe(boton) : null);
        var direccion = new URL(window.location.href);
        ['nuevo', 'editar'].forEach(function (nombreParametro) { direccion.searchParams.delete(nombreParametro); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    dibujar();
})();
