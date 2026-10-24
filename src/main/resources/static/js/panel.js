/**
 * Comportamiento del panel del administrador.
 *
 *  1. Menú lateral: contraer/expandir y apertura en pantallas chicas.
 *  2. Listados en tabla: selección de filas, columnas visibles, filas por página y paginador.
 *  3. Filtros: se aplican solos al cambiar un campo.
 *  4. Formularios: validación de campos, formularios por pasos, vista previa de imagen y selectores múltiples.
 *
 * Las páginas pueden registrar validaciones propias de un formulario en window.PanelValidadores[nombre]
 * (el nombre es el atributo data-validador del formulario): reciben el número de paso (desde 1) y el formulario,
 * y devuelven true si el paso es válido.
 */
(function () {
    'use strict';

    var CLAVE_FOCO = 'idoneos.panel.focoFiltro';
    window.PanelValidadores = window.PanelValidadores || {};

    function guardar(clave, valor) {
        try { window.localStorage.setItem(clave, valor); } catch (e) { /* almacenamiento no disponible */ }
    }
    function leer(clave) {
        try { return window.localStorage.getItem(clave); } catch (e) { return null; }
    }

    // ------------------------------------------------------------------
    // 1. Menú lateral
    // ------------------------------------------------------------------
    var app = document.getElementById('pnApp');
    if (app) {
        // El menú siempre se carga contraído (sidebar.html lo aplica antes de dibujar)
        app.classList.add('colapsado');
        document.addEventListener('click', function (e) {
            if (e.target.closest('[data-cerrar-menu]')) {
                app.classList.remove('menu-abierto');
            }
        });
    }

    // Textos truncados con "...": el texto completo queda disponible al pasar el mouse
    document.querySelectorAll('.pn-truncar').forEach(function (e) {
        if (!e.title) { e.title = e.textContent.trim(); }
    });

    // ------------------------------------------------------------------
    // 2. Listados en tabla
    // ------------------------------------------------------------------
    // ------------------------------------------------------------------
    // Filtros generados desde las columnas. Cada <th data-filtro="..."> declara su tipo y el filtro se dibuja con el
    // nombre de la columna y en su mismo orden:
    //   texto  -> campo "contiene"                      (fila: data-f-<campo>)
    //   numero -> desde / hasta                         (fila: data-f-<campo> con el número)
    //   fecha  -> desde / hasta                         (fila: data-f-<campo> con la fecha ISO)
    //   lista  -> desplegable (o campo con sugerencias si hay muchas opciones) con los valores que hay en la tabla
    //   multilista -> selector múltiple con búsqueda (Tom Select) con los valores que hay en la tabla; la fila cumple si
    //                 tiene alguno de los elegidos (fila: data-f-<campo> con los valores separados por "|")
    // ------------------------------------------------------------------
    function construirFiltros(formulario, raiz) {
        var cabeceras = raiz.querySelectorAll('thead th[data-filtro]');
        if (!cabeceras.length) { return; }
        var filas = raiz.querySelectorAll('tbody tr[data-fila]');
        var inicio = formulario.firstChild;
        var sufijo = 0;

        // Si la fila no trae el dato del filtro (data-f-<campo>), se toma de lo que muestra su celda:
        //   fecha -> la primera dd/mm/aaaa pasada a aaaa-mm-dd; numero -> el primer número ("Gratis" es 0);
        //   texto y lista -> el texto de la celda (con data-separador en el <th>, la celda trae varios valores).
        function derivar(th, tipo, campo) {
            var atributo = 'data-f-' + campo.toLowerCase();
            var indice = Array.prototype.indexOf.call(th.parentNode.children, th);
            var separador = th.getAttribute('data-separador');
            filas.forEach(function (fila) {
                if (fila.hasAttribute(atributo)) { return; }
                var celda = fila.children[indice];
                var texto = celda ? celda.textContent.replace(/\s+/g, ' ').trim() : '';
                var valor = '';
                if (tipo === 'fecha') {
                    var f = /(\d{2})\/(\d{2})\/(\d{4})(?:\s+(\d{2}:\d{2}))?/.exec(texto);
                    valor = f ? f[3] + '-' + f[2] + '-' + f[1] + (f[4] ? ' ' + f[4] : '') : '';
                } else if (tipo === 'numero') {
                    if (/gratis/i.test(texto)) {
                        valor = '0';
                    } else {
                        var n = /\d[\d.]*(?:,\d+)?/.exec(texto);
                        valor = n ? n[0].replace(/\./g, '').replace(',', '.') : '';
                    }
                } else if (separador) {
                    valor = texto.split(separador).map(function (v) { return v.trim(); }).filter(Boolean).join('|');
                } else {
                    valor = texto === '—' ? '' : texto;
                }
                fila.setAttribute(atributo, valor);
            });
        }
        cabeceras.forEach(function (th) {
            if (th.getAttribute('data-filtro') !== 'estatico') { derivar(th, th.getAttribute('data-filtro'), th.getAttribute('data-campo')); }
        });
        function crearCampo(etiqueta, ancho) {
            var contenedor = document.createElement('div');
            contenedor.className = 'pn-filtros-campo' + (ancho ? ' ' + ancho : '');
            var rotulo = document.createElement('label');
            rotulo.className = 'pn-etiqueta';
            rotulo.textContent = etiqueta;
            contenedor.appendChild(rotulo);
            formulario.insertBefore(contenedor, inicio);
            return contenedor;
        }
        function entrada(tipo, nombre, titulo, extra) {
            var input = document.createElement('input');
            input.type = tipo;
            input.name = nombre;
            input.className = 'pn-input';
            input.title = titulo;
            input.autocomplete = 'off';
            if (extra) { Object.keys(extra).forEach(function (k) { input.setAttribute(k, extra[k]); }); }
            return input;
        }
        cabeceras.forEach(function (th) {
            var tipo = th.getAttribute('data-filtro');
            var campo = th.getAttribute('data-campo');
            var nombre = th.getAttribute('data-col') || th.textContent.trim();
            if (tipo === 'estatico') {
                // Filtro escrito en la página (por ejemplo, el que llega elegido por el servidor): se respeta su lugar
                var bloque = formulario.querySelector('[data-filtro-estatico="' + campo + '"]');
                if (bloque) { inicio = bloque.nextSibling; }
                return;
            }
            if (tipo === 'texto') {
                var c = crearCampo(nombre, 'pn-filtros-campo-grande');
                c.appendChild(entrada('text', campo, nombre, { placeholder: 'Buscar' }));
            } else if (tipo === 'numero' || tipo === 'fecha') {
                var r = crearCampo(nombre, 'pn-filtros-rango');
                var par = document.createElement('div');
                par.className = 'pn-filtros-par';
                var t = tipo === 'numero' ? 'number' : 'date';
                var extraDesde = tipo === 'numero' ? { step: 'any', min: '0', placeholder: 'Desde' } : null;
                var extraHasta = tipo === 'numero' ? { step: 'any', min: '0', placeholder: 'Hasta' } : null;
                par.appendChild(entrada(t, campo + 'Desde', nombre + ' desde', extraDesde));
                par.appendChild(entrada(t, campo + 'Hasta', nombre + ' hasta', extraHasta));
                r.appendChild(par);
            } else if (tipo === 'multilista') {
                var elegibles = [];
                filas.forEach(function (fila) {
                    (fila.getAttribute('data-f-' + campo.toLowerCase()) || '').split('|').forEach(function (v) {
                        if (v && elegibles.indexOf(v) < 0) { elegibles.push(v); }
                    });
                });
                elegibles.sort(function (a, b) { return a.localeCompare(b, 'es', { sensitivity: 'base' }); });
                var m = crearCampo(nombre, 'pn-filtros-campo-grande');
                var multiple = document.createElement('select');
                multiple.name = campo;
                multiple.multiple = true;
                multiple.className = 'ts-multiselect';
                multiple.title = nombre;
                multiple.setAttribute('placeholder', 'Todos');
                elegibles.forEach(function (v) { var o = document.createElement('option'); o.value = v; o.textContent = v; multiple.appendChild(o); });
                m.appendChild(multiple);
            } else if (tipo === 'lista') {
                var valores = [];
                filas.forEach(function (fila) {
                    (fila.getAttribute('data-f-' + campo.toLowerCase()) || '').split('|').forEach(function (v) {
                        if (v && valores.indexOf(v) < 0) { valores.push(v); }
                    });
                });
                valores.sort(function (a, b) { return a.localeCompare(b, 'es', { sensitivity: 'base' }); });
                var l = crearCampo(nombre);
                if (valores.length > 8) {
                    var id = 'sugerencias-' + campo + '-' + (sufijo++);
                    var texto = entrada('text', campo, nombre, { placeholder: 'Escribí para buscar', list: id });
                    var lista = document.createElement('datalist');
                    lista.id = id;
                    valores.forEach(function (v) { var o = document.createElement('option'); o.value = v; lista.appendChild(o); });
                    l.appendChild(texto);
                    l.appendChild(lista);
                } else {
                    var sel = document.createElement('select');
                    sel.name = campo;
                    sel.className = 'pn-input';
                    sel.title = nombre;
                    var todos = document.createElement('option');
                    todos.value = '';
                    todos.textContent = 'Todos';
                    sel.appendChild(todos);
                    valores.forEach(function (v) { var o = document.createElement('option'); o.value = v; o.textContent = v; sel.appendChild(o); });
                    l.appendChild(sel);
                }
            }
        });
    }

    function iniciarTabla(raiz) {
        var todas = Array.prototype.slice.call(raiz.querySelectorAll('tbody tr[data-fila], [data-tarjeta]')); // filas o tarjetas
        var filas = todas.slice(); // las que cumplen los filtros (todas, si no hay)
        var selectPorPagina = raiz.querySelector('[data-por-pagina]');
        var infoRango = raiz.querySelector('[data-rango]');
        var contador = raiz.querySelector('[data-seleccionados]');
        var paginador = raiz.querySelector('[data-paginador]');
        var seleccionarTodos = raiz.querySelector('[data-seleccionar-todos]');
        var vacio = raiz.querySelector('[data-vacio]');
        var pagina = 1;
        var botonBaja = raiz.querySelector('[data-baja-masiva]');
        var porPagina = selectPorPagina ? parseInt(selectPorPagina.value, 10) : 10;

        // El valor 0 de "filas por página" significa "todo"
        function tamanio() {
            return porPagina > 0 ? porPagina : Math.max(filas.length, 1);
        }

        function totalPaginas() {
            return Math.max(1, Math.ceil(filas.length / tamanio()));
        }

        function actualizarSeleccion() {
            var marcadas = filas.filter(function (f) {
                var c = f.querySelector('[data-seleccionar-fila]');
                return c && c.checked;
            }).length;
            if (contador) {
                contador.textContent = marcadas + ' de ' + filas.length + ' seleccionados';
            }
            if (botonBaja) {
                botonBaja.classList.toggle('d-none', marcadas === 0);
            }
            if (seleccionarTodos) {
                var visibles = filas.filter(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    return !f.hidden && c && !c.disabled;
                });
                seleccionarTodos.checked = visibles.length > 0 && visibles.every(function (f) {
                    return f.querySelector('[data-seleccionar-fila]').checked;
                });
            }
        }

        function dibujar() {
            var paginas = totalPaginas();
            pagina = Math.min(Math.max(pagina, 1), paginas);
            var inicio = (pagina - 1) * tamanio();
            todas.forEach(function (fila) { if (filas.indexOf(fila) < 0) { fila.hidden = true; } });
            filas.forEach(function (fila, i) {
                fila.hidden = !(i >= inicio && i < inicio + tamanio());
            });
            if (vacio) {
                vacio.hidden = filas.length > 0;
            }
            raiz.querySelectorAll('[data-total-filtradas]').forEach(function (e) {
                // Con singular y plural el elemento lleva el texto completo ("1 curso encontrado" / "3 cursos encontrados")
                e.textContent = e.dataset.plural ? filas.length + ' ' + (filas.length === 1 ? e.dataset.singular : e.dataset.plural) : filas.length;
            });
            if (infoRango) {
                var desde = filas.length ? inicio + 1 : 0;
                infoRango.textContent = desde + '-' + Math.min(inicio + tamanio(), filas.length) + ' de ' + filas.length;
            }
            if (paginador) {
                paginador.innerHTML = '';
                [['«', 1, pagina === 1, 'Primera página'], ['‹', pagina - 1, pagina === 1, 'Página anterior']].forEach(function (b) {
                    paginador.appendChild(boton(b));
                });
                var texto = document.createElement('span');
                texto.textContent = 'Página ' + pagina + ' de ' + paginas;
                paginador.appendChild(texto);
                [['›', pagina + 1, pagina === paginas, 'Página siguiente'], ['»', paginas, pagina === paginas, 'Última página']].forEach(function (b) {
                    paginador.appendChild(boton(b));
                });
            }
            actualizarSeleccion();
        }

        function boton(datos) {
            var b = document.createElement('button');
            b.type = 'button';
            b.textContent = datos[0];
            b.disabled = datos[2];
            b.title = datos[3];
            b.setAttribute('aria-label', datos[3]);
            b.addEventListener('click', function () { pagina = datos[1]; dibujar(); });
            return b;
        }

        if (selectPorPagina) {
            selectPorPagina.addEventListener('change', function () {
                porPagina = parseInt(selectPorPagina.value, 10);
                pagina = 1;
                dibujar();
            });
        }

        if (seleccionarTodos) {
            seleccionarTodos.addEventListener('change', function () {
                filas.forEach(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    if (!f.hidden && c && !c.disabled) {
                        c.checked = seleccionarTodos.checked;
                    }
                });
                actualizarSeleccion();
            });
        }
        raiz.addEventListener('change', function (e) {
            if (e.target.matches('[data-seleccionar-fila]')) {
                actualizarSeleccion();
            }
        });

        // Baja masiva de los elementos seleccionados (todos o ninguno)
        if (botonBaja) {
            botonBaja.addEventListener('click', async function () {
                var marcadas = filas.filter(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    return c && c.checked;
                });
                var ids = marcadas.map(function (f) { return f.querySelector('[data-seleccionar-fila]').value; });
                var dependencias = marcadas.reduce(function (total, f) { return total + (parseInt(f.dataset.dependencias, 10) || 0); }, 0);
                if (!ids.length || !(await window.confirmarBajaMasiva({
                    cantidad: ids.length,
                    elementos: botonBaja.dataset.elementos,
                    dependencias: dependencias,
                    terminoUno: botonBaja.dataset.terminoUno,
                    terminoVarios: botonBaja.dataset.terminoVarios
                }))) { return; }
                var datos = new FormData();
                ids.forEach(function (id) { datos.append('ids', id); });
                botonBaja.disabled = true;
                try {
                    var respuesta = await fetch(botonBaja.dataset.url, {
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
                botonBaja.disabled = false;
            });
        }

        // Columnas visibles: un ícono en el encabezado de la última columna abre el menú. La elección se recuerda por listado.
        var cabeceras = Array.prototype.slice.call(raiz.querySelectorAll('thead th'));
        var cabeceraAcciones = cabeceras[cabeceras.length - 1];
        var menuColumnas = raiz.querySelector('[data-columnas-menu]');
        if (!menuColumnas && cabeceraAcciones && !cabeceraAcciones.hasAttribute('data-col')
                && cabeceras.some(function (th) { return th.hasAttribute('data-col'); })) {
            cabeceraAcciones.classList.add('pn-th-acciones');
            cabeceraAcciones.closest('table').classList.add('pn-con-columnas');
            var envoltura = document.createElement('div');
            envoltura.className = 'pn-columnas-wrapper';
            envoltura.innerHTML = '<button type="button" class="pn-columnas-icono" data-columnas-boton aria-label="Columnas" '
                + 'title="Columnas"><i class="fa-solid fa-table-columns"></i></button>'
                + '<div class="pn-columnas-menu" data-columnas-menu></div>';
            cabeceraAcciones.appendChild(envoltura);
            menuColumnas = envoltura.querySelector('[data-columnas-menu]');
        }
        if (menuColumnas) {
            var claveColumnas = 'idoneos.columnas.' + window.location.pathname;
            var ocultas = [];
            try { ocultas = JSON.parse(leer(claveColumnas) || '[]'); } catch (e) { ocultas = []; }
            var casillas = [];
            var aplicarColumna = function (th, indice, visible) {
                th.style.display = visible ? '' : 'none';
                raiz.querySelectorAll('tbody tr[data-fila]').forEach(function (fila) {
                    if (fila.children[indice]) { fila.children[indice].style.display = visible ? '' : 'none'; }
                });
            };
            cabeceras.forEach(function (th, indice) {
                if (!th.hasAttribute('data-col')) { return; }
                var nombreColumna = th.getAttribute('data-col');
                var etiqueta = document.createElement('label');
                var casilla = document.createElement('input');
                casilla.type = 'checkbox';
                casilla.checked = ocultas.indexOf(nombreColumna) < 0;
                aplicarColumna(th, indice, casilla.checked);
                casilla.addEventListener('change', function () {
                    aplicarColumna(th, indice, casilla.checked);
                    var ocultasAhora = casillas.filter(function (c) { return !c.casilla.checked; })
                        .map(function (c) { return c.nombre; });
                    guardar(claveColumnas, JSON.stringify(ocultasAhora));
                });
                casillas.push({ casilla: casilla, nombre: nombreColumna, th: th, indice: indice });
                etiqueta.appendChild(casilla);
                etiqueta.appendChild(document.createTextNode(nombreColumna));
                menuColumnas.appendChild(etiqueta);
            });
            var restablecer = document.createElement('button');
            restablecer.type = 'button';
            restablecer.className = 'pn-columnas-restablecer';
            restablecer.textContent = 'Restablecer';
            restablecer.addEventListener('click', function () {
                casillas.forEach(function (c) { c.casilla.checked = true; aplicarColumna(c.th, c.indice, true); });
                guardar(claveColumnas, '[]');
            });
            menuColumnas.appendChild(restablecer);
        }

        // Vuelve a calcular las filas a mostrar. Con filtros, lo reemplaza la función de filtrado.
        var reaplicar = function () { filas = todas.slice(); pagina = 1; dibujar(); };

        // ------------------------------------------------------------------
        // Filtros y orden en el navegador: sin recargar la página, al escribir o elegir una opción.
        // Cada campo del formulario (por su name) se compara con el atributo data-f-<name> de la fila:
        //   texto -> contiene (sin tildes ni mayúsculas); lista desplegable -> igual (la fila puede traer varios valores
        //   separados por "|"); "desde" / "hasta" -> la fila tiene fecha ISO en data-f-desde / data-f-hasta.
        // El campo "orden" reordena las filas con data-o-<valor> (y data-o2-<valor> de desempate); la opción indica la
        // dirección en data-dir / data-dir2 ("desc" para descendente). Las filas dadas de baja van siempre al final.
        // ------------------------------------------------------------------
        var formularioFiltros = raiz.querySelector('form[data-filtro-front]');
        if (formularioFiltros) {
            var cuerpo = raiz.querySelector('tbody') || raiz.querySelector('[data-tarjetas]');
            construirFiltros(formularioFiltros, raiz);
            var campos = Array.prototype.filter.call(formularioFiltros.elements, function (c) {
                return c.name && c.type !== 'hidden' && c.type !== 'button' && c.type !== 'submit' && c.name !== 'orden';
            });
            var selectorOrden = formularioFiltros.querySelector('[name="orden"]');

            function normalizar(texto) {
                return (texto || '').toString().normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
            }

            function cumple(fila) {
                return campos.every(function (campo) {
                    var valor = (campo.value || '').trim();
                    if (!valor) { return true; }
                    var dato = fila.getAttribute('data-f-' + campo.name.toLowerCase());
                    var rango = /^(.+)(Desde|Hasta)$/.exec(campo.name);
                    if (rango) {
                        var datoRango = fila.getAttribute('data-f-' + rango[1].toLowerCase());
                        if (datoRango === null) { return true; }
                        if (datoRango === '') { return false; }
                        var esDesde = rango[2] === 'Desde';
                        if (campo.type === 'number') {
                            return esDesde ? parseFloat(datoRango) >= parseFloat(valor) : parseFloat(datoRango) <= parseFloat(valor);
                        }
                        // las fechas con hora se comparan por el día; la hora solo sirve para ordenar
                        var dia = campo.type === 'date' ? datoRango.substring(0, 10) : datoRango;
                        return esDesde ? dia >= valor : dia <= valor;
                    }
                    if (dato === null) { return true; }
                    if (campo.type === 'date') {
                        return campo.name === 'desde' ? dato >= valor : dato <= valor;
                    }
                    if (campo.multiple) {
                        var elegidos = Array.prototype.map.call(campo.selectedOptions, function (o) { return o.value.toLowerCase(); });
                        var presentes = dato.toLowerCase().split('|');
                        return elegidos.some(function (v) { return presentes.indexOf(v) >= 0; });
                    }
                    if (campo.tagName === 'SELECT') {
                        return dato.toLowerCase().split('|').indexOf(valor.toLowerCase()) >= 0;
                    }
                    return normalizar(dato).indexOf(normalizar(valor)) >= 0;
                });
            }

            function comparar(a, b, clave, direccion) {
                var x = a.getAttribute(clave) || '';
                var y = b.getAttribute(clave) || '';
                var resultado = x.localeCompare(y, 'es', { numeric: true, sensitivity: 'base' });
                return direccion === 'desc' ? -resultado : resultado;
            }

            function ordenar() {
                if (!selectorOrden || !selectorOrden.selectedOptions.length) { return; }
                var opcion = selectorOrden.selectedOptions[0];
                var ordenadas = todas.slice().sort(function (a, b) {
                    var bajaA = a.classList.contains('pn-fila-baja') ? 1 : 0;
                    var bajaB = b.classList.contains('pn-fila-baja') ? 1 : 0;
                    return (bajaA - bajaB)
                        || comparar(a, b, 'data-o-' + opcion.value, opcion.dataset.dir)
                        || comparar(a, b, 'data-o2-' + opcion.value, opcion.dataset.dir2);
                });
                ordenadas.forEach(function (fila) { cuerpo.insertBefore(fila, vacio && vacio.parentNode === cuerpo ? vacio : null); });
                todas = ordenadas;
            }

            function actualizarLimpiar() {
                var hay = campos.some(function (campo) { return (campo.value || '').trim() !== ''; });
                formularioFiltros.querySelectorAll('a[href].pn-btn, [data-limpiar]').forEach(function (b) { b.hidden = !hay; });
            }

            function filtrar() {
                actualizarLimpiar();
                filas = todas.filter(cumple);
                // Lo que queda fuera del filtro deja de estar seleccionado
                todas.forEach(function (fila) {
                    var casilla = fila.querySelector('[data-seleccionar-fila]');
                    if (casilla && filas.indexOf(fila) < 0) { casilla.checked = false; }
                });
                pagina = 1;
                dibujar();
            }

            formularioFiltros.addEventListener('submit', function (e) { e.preventDefault(); });
            formularioFiltros.addEventListener('input', function (e) {
                if (e.target.matches('input[type="text"], input[type="search"], input[type="number"]')) { filtrar(); }
            });
            formularioFiltros.addEventListener('change', function (e) {
                if (e.target === selectorOrden) { ordenar(); }
                if (e.target.matches('select, input[type="date"], input[type="number"]')) { filtrar(); }
            });
            // "Limpiar" restablece los campos sin recargar
            formularioFiltros.addEventListener('click', function (e) {
                var enlace = e.target.closest('a[href], [data-limpiar]');
                if (!enlace) { return; }
                e.preventDefault();
                // Se vacían todos los campos (reset() devolvería los valores con que llegó la página, por ejemplo un filtro
                // que vino elegido por la URL); el orden vuelve a su opción inicial
                campos.forEach(function (campo) {
                    if (campo.multiple) {
                        if (campo.tomselect) { campo.tomselect.clear(true); } else {
                            Array.prototype.forEach.call(campo.options, function (o) { o.selected = false; });
                        }
                    } else if (campo.tagName === 'SELECT') { campo.selectedIndex = 0; } else { campo.value = ''; }
                });
                if (selectorOrden) {
                    Array.prototype.forEach.call(selectorOrden.options, function (op) { op.selected = op.defaultSelected; });
                }
                ordenar();
                filtrar();
            });

            reaplicar = filtrar;
            // Los campos que llegan completos (por la URL) se aplican apenas carga la página
            filtrar();
        }

        // ------------------------------------------------------------------
        // Orden por columna: un clic en el título ordena de menor a mayor, otro de mayor a menor y otro quita el orden.
        // El criterio depende del tipo de la columna (data-filtro): número, fecha (ISO) o texto sin tildes ni mayúsculas.
        // Un <th data-orden-inicial="asc|desc"> marca el orden con el que arranca el listado. Las filas dadas de baja
        // quedan siempre al final.
        // ------------------------------------------------------------------
        (function () {
            var titulos = Array.prototype.filter.call(raiz.querySelectorAll('thead th[data-col]'), function (th) {
                return !th.classList.contains('pn-th-acciones');
            });
            if (!titulos.length || !cuerpoOrden()) { return; }
            var cuerpoTabla = cuerpoOrden();
            var ordenOriginal = todas.slice();
            // Las filas con detalle desplegable (progreso) se mueven junto con su fila de detalle
            var detalles = new Map();
            ordenOriginal.forEach(function (fila) {
                var siguiente = fila.nextElementSibling;
                if (siguiente && siguiente.classList.contains('pn-fila-detalle')) { detalles.set(fila, siguiente); }
            });
            var activo = null;
            var sentido = 0;

            function cuerpoOrden() { return raiz.querySelector('tbody'); }

            function clave(fila, th) {
                var indice = Array.prototype.indexOf.call(th.parentNode.children, th);
                var tipo = th.getAttribute('data-filtro');
                var campo = th.getAttribute('data-campo');
                var dato = campo ? fila.getAttribute('data-f-' + campo.toLowerCase()) : null;
                var texto = dato !== null ? dato : (fila.children[indice] ? fila.children[indice].textContent.replace(/\s+/g, ' ').trim() : '');
                if (tipo === 'numero') {
                    var n = parseFloat(texto);
                    return isNaN(n) ? null : n;
                }
                if (tipo === 'fecha') { return texto === '' ? null : texto; }
                var normalizado = texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
                return normalizado === '' || normalizado === '—' ? null : normalizado;
            }

            function aplicarOrden() {
                var base = ordenOriginal.slice();
                if (activo && sentido !== 0) {
                    var claves = new Map();
                    base.forEach(function (f) { claves.set(f, clave(f, activo)); });
                    base.sort(function (a, b) {
                        var bajaA = a.classList.contains('pn-fila-baja') ? 1 : 0;
                        var bajaB = b.classList.contains('pn-fila-baja') ? 1 : 0;
                        if (bajaA !== bajaB) { return bajaA - bajaB; }
                        var x = claves.get(a);
                        var y = claves.get(b);
                        if (x === null && y === null) { return 0; }
                        if (x === null) { return 1; } // lo vacío va siempre al final
                        if (y === null) { return -1; }
                        var r = typeof x === 'number' ? x - y : x.localeCompare(y, 'es', { numeric: true });
                        return sentido * r;
                    });
                }
                base.forEach(function (fila) {
                    var referencia = vacio && vacio.parentNode === cuerpoTabla ? vacio : null;
                    cuerpoTabla.insertBefore(fila, referencia);
                    if (detalles.has(fila)) { cuerpoTabla.insertBefore(detalles.get(fila), referencia); }
                });
                todas = base;
                titulos.forEach(function (th) {
                    var marca = th === activo ? sentido : 0;
                    th.setAttribute('aria-sort', marca === 1 ? 'ascending' : (marca === -1 ? 'descending' : 'none'));
                    th.classList.toggle('pn-orden-asc', marca === 1);
                    th.classList.toggle('pn-orden-desc', marca === -1);
                });
                reaplicar();
            }

            titulos.forEach(function (th) {
                th.classList.add('pn-th-ordenable');
                th.tabIndex = 0;
                var flechas = document.createElement('span');
                flechas.className = 'pn-orden-flechas';
                flechas.setAttribute('aria-hidden', 'true');
                flechas.innerHTML = '<i class="fa-solid fa-caret-up"></i><i class="fa-solid fa-caret-down"></i>';
                th.appendChild(flechas);
                function alternar() {
                    if (activo !== th) { activo = th; sentido = 1; } else if (sentido === 1) { sentido = -1; } else { sentido = 0; activo = null; }
                    aplicarOrden();
                }
                th.addEventListener('click', function (e) {
                    if (e.target.closest('[data-columnas-boton], .pn-columnas-wrapper')) { return; }
                    alternar();
                });
                th.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); alternar(); }
                });
                var inicial = th.getAttribute('data-orden-inicial');
                if (inicial) { activo = th; sentido = inicial === 'desc' ? -1 : 1; }
            });
            if (activo) { aplicarOrden(); }
        })();

        dibujar();
    }

    document.querySelectorAll('[data-tabla]').forEach(iniciarTabla);

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-columnas-boton]');
        // Marcar o desmarcar una columna no cierra el menú
        if (!boton && e.target.closest('.pn-columnas-menu')) { return; }
        document.querySelectorAll('.pn-columnas-wrapper.abierto').forEach(function (w) {
            if (!boton || !w.contains(boton)) { w.classList.remove('abierto'); }
        });
        if (boton) {
            var wrapper = boton.closest('.pn-columnas-wrapper');
            var abierto = wrapper.classList.toggle('abierto');
            if (abierto) {
                // Si el menú se saliera por el borde izquierdo del contenido (tapando el menú lateral), se alinea a la izquierda
                var menu = wrapper.querySelector('.pn-columnas-menu');
                menu.classList.remove('alinear-izquierda');
                if (wrapper.closest('.pn-th-acciones')) {
                    var caja = boton.getBoundingClientRect();
                    menu.style.top = (caja.bottom + 6) + 'px';
                    menu.style.left = Math.max(8, caja.right - menu.offsetWidth) + 'px';
                    return;
                }
                var principal = document.querySelector('.pn-principal');
                var limite = principal ? principal.getBoundingClientRect().left : 0;
                if (menu.getBoundingClientRect().left < limite) {
                    menu.classList.add('alinear-izquierda');
                }
            }
        }
    });

    // ------------------------------------------------------------------
    // 3. Filtros automáticos
    // ------------------------------------------------------------------
    document.querySelectorAll('form[data-filtros]').forEach(function (form) {
        var temporizador = null;
        form.addEventListener('change', function (e) {
            if (e.target.matches('select, input[type="date"]')) { form.submit(); }
        });
        form.addEventListener('input', function (e) {
            if (e.target.matches('input[type="text"], input[type="search"]')) {
                clearTimeout(temporizador);
                temporizador = setTimeout(function () {
                    try { window.sessionStorage.setItem(CLAVE_FOCO, e.target.name); } catch (err) { /* sin almacenamiento */ }
                    form.submit();
                }, 450);
            }
        });
        // Devuelve el foco al campo de texto que se estaba escribiendo
        try {
            var nombre = window.sessionStorage.getItem(CLAVE_FOCO);
            if (nombre) {
                window.sessionStorage.removeItem(CLAVE_FOCO);
                var campo = form.querySelector('[name="' + nombre + '"]');
                if (campo) {
                    campo.focus();
                    campo.setSelectionRange(campo.value.length, campo.value.length);
                }
            }
        } catch (err) { /* sin almacenamiento */ }
    });

    // ------------------------------------------------------------------
    // 4. Formularios: validación, pasos, vista previa de imagen y selectores múltiples
    // ------------------------------------------------------------------
    var IMAGEN_TIPOS = ['image/jpeg', 'image/png', 'image/webp'];
    var IMAGEN_TAMANIO_MAXIMO = 2 * 1024 * 1024;

    function contenedorDe(campo) {
        return campo.closest('.pn-campo') || campo.parentNode;
    }

    function ayudaDe(campo) {
        var contenedor = contenedorDe(campo);
        var ayuda = contenedor.querySelector('.pn-ayuda-error');
        if (!ayuda) {
            ayuda = document.createElement('div');
            ayuda.className = 'pn-ayuda-error';
            contenedor.appendChild(ayuda);
        }
        return ayuda;
    }

    /** Muestra (o limpia, si el mensaje está vacío) el error de un campo. Devuelve true si el campo es válido. */
    function marcar(campo, mensaje) {
        campo.classList.toggle('es-invalido', !!mensaje);
        ayudaDe(campo).textContent = mensaje || '';
        return !mensaje;
    }
    window.PanelValidacion = { marcar: marcar };

    function validarCampo(campo) {
        if (campo.matches(':disabled') || !campo.name || campo.type === 'hidden') {
            return true;
        }
        // Una casilla obligatoria (por ejemplo, los términos y condiciones) debe estar marcada
        if (campo.type === 'checkbox') {
            if (campo.required && !campo.checked) {
                return marcar(campo, campo.dataset.mensajeObligatorio || 'Este campo es obligatorio.');
            }
            return marcar(campo, '');
        }
        // Los campos ocultos (por ejemplo, las fechas de dictado de un curso grabado) no se validan
        if (campo.closest('[hidden], .d-none')) {
            return marcar(campo, '');
        }
        var valor = (campo.value || '').trim();
        if (campo.tagName === 'SELECT' && campo.multiple) {
            if (campo.required && campo.selectedOptions.length === 0) {
                return marcar(campo, campo.dataset.mensajeObligatorio || 'Debe seleccionar al menos una opción.');
            }
            return marcar(campo, '');
        }
        if (campo.type === 'file' && campo.dataset.extensiones) {
            // Archivo de material: formatos y tamaño máximo vienen del servidor (data-extensiones y data-tamanio-mb)
            var adjunto = campo.files && campo.files[0];
            if (adjunto) {
                var extension = adjunto.name.indexOf('.') >= 0 ? adjunto.name.split('.').pop().toLowerCase() : '';
                var permitidas = campo.dataset.extensiones.split(',');
                if (permitidas.indexOf(extension) === -1) {
                    campo.value = '';
                    return marcar(campo, 'El archivo no es válido. Formato válido: ' + permitidas.join(', ').toUpperCase() + '.');
                }
                if (adjunto.size > parseFloat(campo.dataset.tamanioMb) * 1024 * 1024) {
                    campo.value = '';
                    return marcar(campo, 'El archivo es demasiado grande. Máximo: ' + campo.dataset.tamanioMb + ' MB.');
                }
            } else if (campo.required) {
                return marcar(campo, 'Debe seleccionar un archivo.');
            }
            return marcar(campo, '');
        }
        if (campo.type === 'file') {
            var archivo = campo.files && campo.files[0];
            if (archivo && IMAGEN_TIPOS.indexOf(archivo.type) === -1) {
                campo.value = '';
                return marcar(campo, 'La imagen no es válida. Formato válido: JPG, PNG o WebP.');
            }
            if (archivo && archivo.size > IMAGEN_TAMANIO_MAXIMO) {
                campo.value = '';
                return marcar(campo, 'La imagen es demasiado grande. Máximo: 2 MB.');
            }
            return marcar(campo, '');
        }
        if (campo.required && !valor) {
            return marcar(campo, campo.dataset.mensajeObligatorio || 'Este campo es obligatorio.');
        }
        if (campo.maxLength > 0 && valor.length > campo.maxLength) {
            return marcar(campo, 'Es demasiado largo. Máximo: ' + campo.maxLength + ' caracteres.');
        }
        if (campo.type === 'number' && valor !== '') {
            var numero = parseFloat(valor);
            if (isNaN(numero)) { return marcar(campo, 'No es válido. Formato válido: número.'); }
            if (campo.min !== '' && numero < parseFloat(campo.min)) {
                return marcar(campo, 'No es válido. Formato válido: número mayor o igual a ' + campo.min + '.');
            }
            if (campo.max !== '' && numero > parseFloat(campo.max)) {
                return marcar(campo, 'No es válido. Formato válido: número menor o igual a ' + parseFloat(campo.max).toLocaleString('es-AR') + '.');
            }
        }
        return marcar(campo, '');
    }

    function validarContenedor(contenedor, formulario, paso) {
        var valido = true;
        contenedor.querySelectorAll('input, select, textarea').forEach(function (campo) {
            if (!validarCampo(campo)) { valido = false; }
        });
        var validador = window.PanelValidadores[formulario.dataset.validador];
        if (validador && !validador(paso, formulario)) { valido = false; }
        return valido;
    }

    // Contadores de caracteres
    function actualizarContador(campo) {
        var contador = contenedorDe(campo).querySelector('.pn-contador');
        if (!contador || !campo.maxLength || campo.maxLength < 0) { return; }
        contador.textContent = campo.value.length + ' / ' + campo.maxLength;
    }
    document.addEventListener('input', function (e) {
        if (e.target.matches('[data-contador]')) {
            actualizarContador(e.target);
        }
        if (e.target.closest('form[data-validar]') && e.target.classList.contains('es-invalido')) {
            validarCampo(e.target);
        }
    });
    document.querySelectorAll('[data-contador]').forEach(actualizarContador);
    document.addEventListener('change', function (e) {
        if (e.target.closest('form[data-validar]') && e.target.classList.contains('es-invalido')) {
            validarCampo(e.target);
        }
    });

    // Vista previa de la imagen elegida (si se quita o es inválida, vuelve la original o se oculta)
    document.addEventListener('change', function (e) {
        var entrada = e.target;
        if (!entrada.matches || !entrada.matches('input.imagen-curso')) { return; }
        var valida = validarCampo(entrada);
        var previa = contenedorDe(entrada).querySelector('.pn-vista-previa');
        if (!previa) { return; }
        if (previa.dataset.urlTemporal) {
            URL.revokeObjectURL(previa.dataset.urlTemporal);
            delete previa.dataset.urlTemporal;
        }
        var archivo = valida && entrada.files && entrada.files[0];
        if (archivo) {
            var url = URL.createObjectURL(archivo);
            previa.dataset.urlTemporal = url;
            previa.src = url;
            previa.classList.remove('d-none');
        } else if (previa.dataset.original) {
            previa.src = previa.dataset.original;
            previa.classList.remove('d-none');
        } else {
            previa.removeAttribute('src');
            previa.classList.add('d-none');
        }
    });

    // Selectores múltiples (Tom Select)
    function iniciarMultiselect(contenedor) {
        if (!window.TomSelect) { return; }
        contenedor.querySelectorAll('select.ts-multiselect:not(.tomselected)').forEach(function (el) {
            new TomSelect(el, {
                // El menú se dibuja sobre la página y no dentro de la ventana: así el modal no lo recorta
                dropdownParent: 'body',
                plugins: ['remove_button', 'checkbox_options'],
                placeholder: el.getAttribute('placeholder'),
                closeAfterSelect: false,
                hideSelected: false,
                maxOptions: null,
                // En las ventanas por pasos el selector puede recibir el foco mientras su paso no se muestra: el menú se
                // abriría sin tamaño ni lugar y después no se podría abrir. Si el campo no está a la vista, no se abre.
                onDropdownOpen: function () {
                    if (!this.control.offsetWidth) { this.close(); }
                }
            });
        });
    }
    if ('requestIdleCallback' in window) {
        requestIdleCallback(function () { iniciarMultiselect(document); }, { timeout: 1500 });
    } else {
        setTimeout(function () { iniciarMultiselect(document); }, 300);
    }
    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('show.bs.modal', function () { iniciarMultiselect(modal); });
    });

    // Formularios por pasos
    function iniciarPasos(formulario) {
        var pasos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-paso'));
        var puntos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-punto'));
        var lineas = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-linea'));
        var anterior = formulario.querySelector('[data-paso-anterior]');
        var siguiente = formulario.querySelector('[data-paso-siguiente]');
        var guardarBtn = formulario.querySelector('[data-paso-guardar]');
        var actual = 0;

        function mostrar(indice) {
            actual = Math.min(Math.max(indice, 0), pasos.length - 1);
            pasos.forEach(function (p, i) { p.classList.toggle('activo', i === actual); });
            puntos.forEach(function (p, i) {
                p.classList.toggle('activo', i === actual);
                p.classList.toggle('completo', i < actual);
                p.innerHTML = i < actual ? '<i class="fa-solid fa-check"></i>' : String(i + 1);
            });
            lineas.forEach(function (l, i) { l.classList.toggle('completa', i < actual); });
            // Los menús de los selectores que quedaron abiertos en otro paso se cierran
            formulario.querySelectorAll('select.tomselected').forEach(function (s) { if (s.tomselect) { s.tomselect.close(); } });
            if (anterior) { anterior.style.visibility = actual === 0 ? 'hidden' : 'visible'; }
            if (siguiente) { siguiente.classList.toggle('d-none', actual === pasos.length - 1); }
            if (guardarBtn) { guardarBtn.classList.toggle('d-none', actual !== pasos.length - 1); }
        }

        formulario.pasos = { mostrar: mostrar, cantidad: pasos.length, actual: function () { return actual; } };

        if (anterior) { anterior.addEventListener('click', function () { mostrar(actual - 1); }); }
        if (siguiente) {
            siguiente.addEventListener('click', function () {
                if (validarContenedor(pasos[actual], formulario, actual + 1)) { mostrar(actual + 1); }
            });
        }
        // Enter dentro de un campo avanza al paso siguiente en lugar de enviar el formulario incompleto
        formulario.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && actual < pasos.length - 1 && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                if (siguiente) { siguiente.click(); }
            }
        });
        mostrar(0);
    }
    document.querySelectorAll('form[data-pasos]').forEach(iniciarPasos);

    // Cuando el servidor rechaza un formulario por pasos, se vuelve al paso del campo al que se refiere el error y se
    // marca ese campo. El servidor responde con un mensaje, así que el campo se deduce de las palabras de su etiqueta.
    var PALABRAS_VACIAS = ['de', 'del', 'la', 'el', 'los', 'las', 'y', 'o', 'en', 'con', 'al', 'por', 'para', 'una', 'que', 'sus', 'debe', 'error'];

    function palabras(texto) {
        return (texto || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
            .split(/[^a-z0-9]+/).filter(function (p) { return p.length >= 3 && PALABRAS_VACIAS.indexOf(p) < 0; });
    }

    /**
     * Lleva al paso del campo al que se refiere un error del servidor y lo marca con el mensaje. El servidor indica el
     * campo por su nombre; si no lo indica, se deduce de las palabras de las etiquetas. Devuelve true si marcó un campo.
     */
    window.PanelValidacion.irAlError = function (formulario, mensaje, nombreCampo) {
        var mejor = null;
        if (nombreCampo) {
            var elegido = formulario.querySelector('[name="' + nombreCampo + '"]');
            if (elegido && elegido.type !== 'hidden' && !elegido.disabled) { mejor = elegido; }
        }
        if (!mejor) {
            var enMensaje = palabras(mensaje);
            var mejorPuntaje = 0;
            formulario.querySelectorAll('.pn-campo').forEach(function (campo) {
                var control = campo.querySelector('input:not([type="hidden"]), select, textarea');
                var etiqueta = campo.querySelector('.pn-etiqueta');
                if (!control || !etiqueta || control.disabled || campo.closest('[hidden], .d-none')) { return; }
                var clave = palabras(etiqueta.textContent.replace(/\(.*?\)/g, ''));
                if (!clave.length) { return; }
                var coinciden = clave.filter(function (p) { return enMensaje.indexOf(p) >= 0; });
                // todas las palabras de la etiqueta (la coincidencia más precisa) o, si no, la palabra más larga que coincida
                var puntaje = coinciden.length === clave.length ? 100 + clave.length
                    : coinciden.reduce(function (m, p) { return p.length >= 5 ? Math.max(m, p.length) : m; }, 0);
                // a igual puntaje gana el campo cuyas palabras aparecen antes en el mensaje (el sujeto de la frase)
                var posicion = coinciden.length ? Math.min.apply(null, coinciden.map(function (p) { return enMensaje.indexOf(p); })) : 999;
                puntaje = puntaje * 1000 - posicion;
                if (puntaje > mejorPuntaje) { mejorPuntaje = puntaje; mejor = control; }
            });
        }
        if (!mejor) { return false; }
        var paso = mejor.closest('.pn-paso');
        if (formulario.pasos && paso) {
            var indice = Array.prototype.indexOf.call(formulario.querySelectorAll('.pn-paso'), paso);
            if (indice >= 0) { formulario.pasos.mostrar(indice); }
        }
        window.PanelValidacion.marcar(mejor, (mensaje || '').replace(/^Error!\s*/, ''));
        if (mejor.focus && mejor.offsetParent !== null) { mejor.focus(); }
        return true;
    };

    // Al abrir una ventana modal con formulario por pasos, vuelve al primer paso
    document.addEventListener('show.bs.modal', function (e) {
        e.target.querySelectorAll('form[data-pasos]').forEach(function (f) {
            if (f.pasos) { f.pasos.mostrar(0); }
        });
    });

    // Validación al enviar (en captura: se ejecuta antes que el envío por fetch de formularios.js)
    document.addEventListener('submit', function (e) {
        var formulario = e.target;
        if (!formulario.matches || !formulario.matches('form[data-validar]')) { return; }
        var pasos = formulario.querySelectorAll('.pn-paso');
        var valido = true;
        if (pasos.length) {
            for (var i = 0; i < pasos.length; i++) {
                if (!validarContenedor(pasos[i], formulario, i + 1)) {
                    if (valido) { formulario.pasos.mostrar(i); }
                    valido = false;
                }
            }
        } else {
            valido = validarContenedor(formulario, formulario, 1);
        }
        if (!valido) {
            e.preventDefault();
            e.stopImmediatePropagation();
            var primero = formulario.querySelector('.es-invalido');
            if (primero && primero.focus && primero.offsetParent !== null) { primero.focus(); }
        }
    }, true);
})();
