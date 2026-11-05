/**
 * Listados en tabla y en cards: selección de filas, columnas visibles, filas por página, paginador, filtros y orden
 * (en el navegador o, con data-tabla-servidor, pidiéndolos al servidor).
 *
 * Los filtros se generan desde las columnas: cada <th data-filtro="..."> declara su tipo. Se carga después de
 * core/validacion.js, que arma los selectores múltiples de los filtros (window.iniciarMultiselect).
 */
(function () {
    'use strict';

    function guardar(clave, valor) {
        try { window.localStorage.setItem(clave, valor); } catch (e) { /* almacenamiento no disponible */ }
    }
    function leer(clave) {
        try { return window.localStorage.getItem(clave); } catch (e) { return null; }
    }

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
        // Con el listado en el servidor las opciones de las listas llegan en data-opciones (de toda la lista, no solo de la página)
        var servidor = raiz.hasAttribute('data-tabla-servidor');
        var opcionesServidor = {};
        try { opcionesServidor = JSON.parse(raiz.getAttribute('data-opciones') || '{}'); } catch (e) { opcionesServidor = {}; }
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
            if (!servidor && th.getAttribute('data-filtro') !== 'estatico') { derivar(th, th.getAttribute('data-filtro'), th.getAttribute('data-campo')); }
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
                if (servidor) { elegibles = (opcionesServidor[campo] || []).slice(); } else filas.forEach(function (fila) {
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
                if (servidor) { valores = (opcionesServidor[campo] || []).slice(); } else filas.forEach(function (fila) {
                    (fila.getAttribute('data-f-' + campo.toLowerCase()) || '').split('|').forEach(function (v) {
                        if (v && valores.indexOf(v) < 0) { valores.push(v); }
                    });
                });
                valores.sort(function (a, b) { return a.localeCompare(b, 'es', { sensitivity: 'base' }); });
                var l = crearCampo(nombre);
                var sel = document.createElement('select');
                sel.name = campo;
                sel.className = 'pn-input';
                sel.title = nombre;
                // Todos los filtros de lista se pueden buscar, salvo los de opciones fijas (estado, nivel...)
                if (!th.hasAttribute('data-sin-busqueda')) { sel.setAttribute('data-buscable', ''); }
                var todos = document.createElement('option');
                todos.value = '';
                todos.textContent = 'Todos';
                sel.appendChild(todos);
                valores.forEach(function (v) { var o = document.createElement('option'); o.value = v; o.textContent = v; sel.appendChild(o); });
                l.appendChild(sel);
            }
        });
    }

    function iniciarTabla(raiz) {
        var todas = Array.prototype.slice.call(raiz.querySelectorAll('tbody tr[data-fila], [data-card]')); // filas o cards
        var filas = todas.slice(); // las que cumplen los filtros (todas, si no hay)
        var selectPorPagina = raiz.querySelector('[data-por-pagina]');
        var infoRango = raiz.querySelector('[data-rango]');
        var contador = raiz.querySelector('[data-seleccionados]');
        var paginador = raiz.querySelector('[data-paginador]');
        var seleccionarTodos = raiz.querySelector('[data-seleccionar-todos]');
        var empty = raiz.querySelector('[data-empty]');
        var pagina = 1;
        var botonBaja = raiz.querySelector('[data-baja-masiva]');
        var porPagina = selectPorPagina ? parseInt(selectPorPagina.value, 10) : 10;

        // Listado en el servidor (data-tabla-servidor): el filtro, el orden y la página los resuelve el servidor y se piden con
        // fetch; en la pantalla solo está la página actual. Los datos de la página llegan en el <tbody> (data-total, data-paginas...)
        var servidor = raiz.hasAttribute('data-tabla-servidor');
        var meta = { total: 0, paginas: 1 };
        var ordenServidor = null;
        var peticion = 0;
        var temporizador = null;
        var reaplicarColumnas = function () { };
        function refrescar() { if (servidor) { pedir(); } else { dibujar(); } }
        function leerMeta(cuerpoTabla) {
            meta = { total: parseInt(cuerpoTabla.dataset.total, 10) || 0, paginas: parseInt(cuerpoTabla.dataset.paginas, 10) || 1 };
            pagina = parseInt(cuerpoTabla.dataset.pagina, 10) || 1;
        }
        function pedir() {
            var n = ++peticion;
            var params = new URLSearchParams(window.location.search);
            ['pagina', 'tamanio', 'ordenCampo', 'ordenDir'].forEach(function (k) { params.delete(k); });
            (typeof campos !== 'undefined' ? campos : []).forEach(function (campo) {
                params.delete(campo.name);
                if (campo.multiple) {
                    Array.prototype.forEach.call(campo.selectedOptions, function (o) { params.append(campo.name, o.value); });
                } else if ((campo.value || '').trim() !== '') {
                    params.set(campo.name, campo.value.trim());
                }
            });
            params.set('pagina', pagina);
            params.set('tamanio', porPagina);
            if (ordenServidor) { params.set('ordenCampo', ordenServidor.campo); params.set('ordenDir', ordenServidor.dir); }
            if (typeof selectorOrden !== 'undefined' && selectorOrden && selectorOrden.value) { params.set('orden', selectorOrden.value); }
            raiz.classList.add('pn-cargando');
            raiz.setAttribute('aria-busy', 'true');
            var desde = Date.now();
            var urlTabla = raiz.getAttribute('data-tabla-servidor');
            // Lo que ya lleva la dirección del listado (por ejemplo vista=cards) no se repite
            if (urlTabla.indexOf('?') >= 0) {
                new URLSearchParams(urlTabla.slice(urlTabla.indexOf('?') + 1)).forEach(function (v, k) { params.delete(k); });
            }
            fetch(urlTabla + (urlTabla.indexOf('?') >= 0 ? '&' : '?') + params.toString(), {
                headers: { 'X-Tabla': 'filas', 'Accept': 'text/html' }, credentials: 'same-origin'
            }).then(function (r) {
                if (!r.ok) { throw new Error('No se pudo cargar el listado.'); }
                return r.text();
            }).then(function (html) {
                if (n !== peticion) { return; } // llegó una respuesta más nueva
                var plantilla = document.createElement('template');
                // Tabla (<tbody>) o grilla de cards ([data-cards])
                var actual = raiz.querySelector('tbody') || raiz.querySelector('[data-cards]');
                var esTabla = !!raiz.querySelector('tbody');
                plantilla.innerHTML = esTabla ? '<table>' + html + '</table>' : html;
                var nuevo = esTabla ? plantilla.content.querySelector('tbody') : plantilla.content.querySelector('[data-cards]');
                if (!nuevo || !actual) { return; }
                actual.replaceWith(nuevo);
                empty = raiz.querySelector('[data-empty]');
                todas = Array.prototype.slice.call(nuevo.querySelectorAll('tr[data-fila], [data-card]'));
                filas = todas.slice();
                leerMeta(nuevo);
                reaplicarColumnas();
                dibujar();
                // Las pantallas con comportamientos propios sobre las filas los vuelven a enganchar
                document.dispatchEvent(new CustomEvent('tabla-actualizada', { detail: { tabla: raiz } }));
            }).catch(function () {
                if (window.mostrarError) { window.mostrarError('Error! No se pudo cargar el listado. Intente nuevamente.'); }
            }).then(function () {
                // El efecto de carga dura al menos un instante, para que se note aunque el servidor responda enseguida
                setTimeout(function () { if (n === peticion) { raiz.classList.remove('pn-cargando'); raiz.removeAttribute('aria-busy'); } }, Math.max(0, 220 - (Date.now() - desde)));
            });
        }
        if (servidor) {
            var cuerpoInicial = raiz.querySelector('tbody') || raiz.querySelector('[data-cards]');
            if (cuerpoInicial) { leerMeta(cuerpoInicial); }
        }

        // El valor 0 de "filas por página" significa "todo"
        function tamanio() {
            return porPagina > 0 ? porPagina : Math.max(servidor ? meta.total : filas.length, 1);
        }

        function totalPaginas() {
            return servidor ? meta.paginas : Math.max(1, Math.ceil(filas.length / tamanio()));
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
            // En el servidor la pantalla ya trae solo la página: no hay filas que ocultar
            var total = servidor ? meta.total : filas.length;
            if (!servidor) {
                todas.forEach(function (fila) { if (filas.indexOf(fila) < 0) { fila.hidden = true; } });
                filas.forEach(function (fila, i) {
                    fila.hidden = !(i >= inicio && i < inicio + tamanio());
                });
            }
            if (empty) {
                empty.hidden = total > 0;
            }
            raiz.querySelectorAll('[data-total-filtradas]').forEach(function (e) {
                // Con singular y plural el elemento lleva el texto completo ("1 curso encontrado" / "3 cursos encontrados")
                e.textContent = e.dataset.plural ? total + ' ' + (total === 1 ? e.dataset.singular : e.dataset.plural) : total;
            });
            if (infoRango) {
                var desde = total ? inicio + 1 : 0;
                infoRango.textContent = desde + '-' + Math.min(inicio + tamanio(), total) + ' de ' + total;
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
            b.addEventListener('click', function () { pagina = datos[1]; refrescar(); });
            return b;
        }

        if (selectPorPagina) {
            selectPorPagina.addEventListener('change', function () {
                porPagina = parseInt(selectPorPagina.value, 10);
                pagina = 1;
                refrescar();
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
            envoltura.innerHTML = '<button type="button" class="pn-columnas-icon" data-columnas-boton aria-label="Columnas" '
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
            reaplicarColumnas = function () { casillas.forEach(function (c) { aplicarColumna(c.th, c.indice, c.casilla.checked); }); };
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
            var cuerpo = raiz.querySelector('tbody') || raiz.querySelector('[data-cards]');
            construirFiltros(formularioFiltros, raiz);
            window.iniciarMultiselect(formularioFiltros); // los selectores múltiples de los filtros se arman ya, sin esperar a la carga completa
            setTimeout(function () { if (window.SelectBuscable) { window.SelectBuscable.iniciarTodos(formularioFiltros); } }, 0);
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
                if (servidor || !selectorOrden || !selectorOrden.selectedOptions.length) { return; }
                var opcion = selectorOrden.selectedOptions[0];
                var ordenadas = todas.slice().sort(function (a, b) {
                    var bajaA = a.classList.contains('pn-fila-baja') ? 1 : 0;
                    var bajaB = b.classList.contains('pn-fila-baja') ? 1 : 0;
                    return (bajaA - bajaB)
                        || comparar(a, b, 'data-o-' + opcion.value, opcion.dataset.dir)
                        || comparar(a, b, 'data-o2-' + opcion.value, opcion.dataset.dir2);
                });
                ordenadas.forEach(function (fila) { cuerpo.insertBefore(fila, empty && empty.parentNode === cuerpo ? empty : null); });
                todas = ordenadas;
            }

            function actualizarLimpiar() {
                var hay = campos.some(function (campo) { return (campo.value || '').trim() !== ''; });
                formularioFiltros.querySelectorAll('a[href].pn-btn, [data-limpiar]').forEach(function (b) { b.hidden = !hay; });
            }

            function filtrar() {
                actualizarLimpiar();
                if (servidor) {
                    // Al escribir se espera un instante para no pedir en cada tecla
                    pagina = 1;
                    clearTimeout(temporizador);
                    temporizador = setTimeout(pedir, 250);
                    return;
                }
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
            // Un filtro que llega por la dirección (por ejemplo ?estado=Pendiente) se aplica al abrir la página
            new URLSearchParams(window.location.search).forEach(function (valor, nombre) {
                var campo = campos.filter(function (c) { return c.name === nombre; })[0];
                if (campo && valor && campo.tagName === 'SELECT' && Array.prototype.some.call(campo.options, function (o) { return o.value === valor; })) {
                    campo.value = valor;
                }
            });
            // Los campos que llegan completos (por la URL) se aplican apenas carga la página (el servidor ya los aplicó al dibujarla)
            if (servidor) { actualizarLimpiar(); } else { filtrar(); }
        }

        // ------------------------------------------------------------------
        // Orden por columna: un clic en el título ordena de menor a mayor, otro de mayor a menor y otro quita el orden.
        // El criterio depende del tipo de la columna (data-filtro): número, fecha (ISO) o texto sin tildes ni mayúsculas.
        // Un <th data-orden-inicial="asc|desc"> marca el orden con el que arranca el listado. Las filas dadas de baja
        // quedan siempre al final.
        // ------------------------------------------------------------------
        (function () {
            var titulos = Array.prototype.filter.call(raiz.querySelectorAll('thead th[data-col]'), function (th) {
                // En el servidor solo se ordena por las columnas con campo (data-campo)
                return !th.classList.contains('pn-th-acciones') && (!servidor || th.hasAttribute('data-campo'));
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
                if (servidor) {
                    ordenServidor = activo && sentido !== 0 ? { campo: activo.getAttribute('data-campo'), dir: sentido === 1 ? 'asc' : 'desc' } : null;
                    titulos.forEach(function (th) {
                        var marca = th === activo ? sentido : 0;
                        th.setAttribute('aria-sort', marca === 1 ? 'ascending' : (marca === -1 ? 'descending' : 'none'));
                        th.classList.toggle('pn-orden-asc', marca === 1);
                        th.classList.toggle('pn-orden-desc', marca === -1);
                    });
                    pagina = 1;
                    pedir();
                    return;
                }
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
                    var referencia = empty && empty.parentNode === cuerpoTabla ? empty : null;
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
            if (activo && !servidor) { aplicarOrden(); }
            if (activo && servidor) {
                // El servidor ya trajo la página con ese orden inicial: solo se marca la columna
                titulos.forEach(function (th) {
                    var marca = th === activo ? sentido : 0;
                    th.setAttribute('aria-sort', marca === 1 ? 'ascending' : (marca === -1 ? 'descending' : 'none'));
                    th.classList.toggle('pn-orden-asc', marca === 1);
                    th.classList.toggle('pn-orden-desc', marca === -1);
                });
                activo = null; sentido = 0; // el primer clic arranca el orden del usuario
            }
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
})();
