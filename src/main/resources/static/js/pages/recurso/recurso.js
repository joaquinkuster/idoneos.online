/** Comportamiento del ABM de materiales. */
(function () {
    'use strict';

    var selector = document.getElementById('modalSelectorRecurso');
    var modalNuevo = document.getElementById('modalNuevoMaterial');

    function modal(elemento) {
        return window.bootstrap.Modal.getOrCreateInstance(elemento);
    }

    // ----------------------------------------------------------------------
    // 1. Selector de recurso o actividad
    // ----------------------------------------------------------------------

    function opciones() {
        return Array.prototype.slice.call(selector.querySelectorAll('.ac-selector-opcion:not(.ac-selector-opcion-deshabilitada)'));
    }

    function opcionElegida() {
        var marcada = selector.querySelector('.ac-selector-opcion input:checked');
        return marcada ? marcada.closest('.ac-selector-opcion') : null;
    }

    // Qué se hace en la pestaña "Desde cero" de cada tipo de recurso
    var DESDE_CERO = {
        RESUMEN: 'Registrás un nuevo resumen: subís el resumen de un tema en un archivo PDF y elegís el tema al que va. Se abre el listado de recursos del curso con el formulario de alta.',
        PRESENTACION: 'Registrás una nueva presentación: subís las diapositivas de una clase en un archivo PDF y elegís el tema al que va. Se abre el listado de recursos del curso con el formulario de alta.',
        BIBLIOGRAFIA: 'Registrás una nueva bibliografía: cargás un texto o material de lectura con su autor y su ISBN (el archivo PDF es obligatorio) y elegís el tema al que va. Se abre el listado de recursos del curso con el formulario de alta.',
        GRABACION: 'Registrás una nueva grabación: subís el video de una clase dictada y elegís el tema al que va. Se abre el listado de recursos del curso con el formulario de alta.',
        ENLACE: 'Registrás un nuevo enlace: cargás la dirección web de un material que está fuera de la plataforma y elegís el tema al que va. Se abre el listado de recursos del curso con el formulario de alta.'
    };

    function mostrarDetalle() {
        var elegida = opcionElegida();
        opciones().forEach(function (o) { o.classList.toggle('elegida', o === elegida); });
        selector.querySelector('[data-selector-titulo]').textContent = elegida ? elegida.dataset.nombre : '';
        selector.querySelector('[data-selector-descripcion]').textContent = elegida ? (elegida.dataset.biblioteca && DESDE_CERO[elegida.dataset.tipoId]) || elegida.dataset.descripcion : '';
        // El aviso (por ejemplo, registrar un pool primero) acompaña solo a la opción para la que se dio
        var aviso = selector.querySelector('[data-selector-aviso]');
        if (aviso) { aviso.hidden = !aviso.dataset.opcion || !elegida || aviso.dataset.opcion !== elegida.querySelector('input').value; }
        prepararPestanas(elegida);
    }

    // ----------------------------------------------------------------------
    // Pestañas "Nuevo" / "De la biblioteca": reutilizar un recurso de la biblioteca digital en un tema del curso
    // ----------------------------------------------------------------------

    var biblioteca = { pestana: 'nuevo', tipo: null, recursos: [], elegidos: [], pedido: 0, cerrados: {}, pagina: 1, tamanio: 5, irAlElegido: false };

    function elementoDe(atributo) { return selector.querySelector('[' + atributo + ']'); }

    function cambiarPestana(nombre) {
        biblioteca.pestana = nombre;
        selector.querySelectorAll('[data-pestana]').forEach(function (b) { b.classList.toggle('on', b.dataset.pestana === nombre); });
        selector.querySelectorAll('[data-panel]').forEach(function (p) { p.hidden = p.dataset.panel !== nombre; });
        elementoDe('data-selector-agregar').textContent = nombre === 'biblioteca' ? 'Agregar' : 'Siguiente';
    }

    /** Mientras llega la lista se dibujan bloques grises que pulsan, como en los listados. */
    function mostrarEsqueleto() {
        var contenedor = elementoDe('data-selector-biblioteca');
        contenedor.textContent = '';
        contenedor.classList.add('pn-sk-lista');
        for (var i = 0; i < 4; i++) {
            var bloque = document.createElement('span');
            bloque.className = 'pn-sk';
            contenedor.appendChild(bloque);
        }
        elementoDe('data-selector-pie').hidden = true;
    }

    /** Cambiar de página o de filas por página se muestra con el mismo esqueleto que la carga. */
    function pintarConEsqueleto() {
        var turno = ++biblioteca.pedido;
        mostrarEsqueleto();
        setTimeout(function () { if (turno === biblioteca.pedido) { pintarBiblioteca(); } }, 260);
    }

    function boton(texto, pagina, deshabilitado, titulo) {
        var b = document.createElement('button');
        b.type = 'button';
        b.textContent = texto;
        b.disabled = deshabilitado;
        b.title = titulo;
        b.setAttribute('aria-label', titulo);
        b.addEventListener('click', function () { biblioteca.pagina = pagina; pintarConEsqueleto(); });
        return b;
    }

    function pintarBiblioteca() {
        var contenedor = elementoDe('data-selector-biblioteca');
        var texto = (elementoDe('data-selector-buscar').value || '').trim().toLowerCase();
        contenedor.classList.remove('pn-sk-lista');
        contenedor.textContent = '';
        // Los del docente primero y luego los de otros docentes. La página se arma sobre ese orden
        var visibles = biblioteca.recursos.filter(function (r) {
            return !texto || [r.titulo, r.autor, r.isbn].some(function (v) { return v && v.toLowerCase().indexOf(texto) >= 0; });
        });
        var total = visibles.length;
        var tamanio = biblioteca.tamanio || total || 1;
        var paginas = Math.max(1, Math.ceil(total / tamanio));
        if (biblioteca.irAlElegido) {
            var posicion = visibles.findIndex(function (r) { return biblioteca.elegidos.indexOf(r.id) >= 0; });
            if (posicion >= 0) { biblioteca.pagina = Math.floor(posicion / tamanio) + 1; }
            biblioteca.irAlElegido = false;
        }
        biblioteca.pagina = Math.min(Math.max(biblioteca.pagina, 1), paginas);
        var inicio = (biblioteca.pagina - 1) * tamanio;
        var pagina = visibles.slice(inicio, inicio + tamanio);
        [{ propio: true, titulo: 'Tuyos' }, { propio: false, titulo: 'Otros docentes' }].forEach(function (grupo) {
            var filas = pagina.filter(function (r) { return r.propio === grupo.propio; });
            if (!filas.length) { return; }
            // La cantidad del divisor es la de todo el listado filtrado, no solo la de la página
            var enTotal = visibles.filter(function (r) { return r.propio === grupo.propio; }).length;
            // Divisor con el mismo estilo que los de tipo de recurso de la biblioteca: flecha, nombre y cantidad. Al hacerle clic se colapsa el grupo
            var cabecera = document.createElement('div');
            cabecera.className = 'ac-bib-grupo' + (biblioteca.cerrados[grupo.titulo] ? ' cerrado' : '');
            cabecera.setAttribute('role', 'button');
            cabecera.tabIndex = 0;
            var flecha = document.createElement('i');
            flecha.className = 'fa-solid fa-chevron-down ac-bib-flecha';
            var nombre = document.createElement('span');
            nombre.textContent = grupo.titulo;
            var conteo = document.createElement('span');
            conteo.className = 'ac-bib-conteo';
            conteo.textContent = enTotal + (enTotal === 1 ? ' recurso' : ' recursos');
            cabecera.appendChild(flecha); cabecera.appendChild(nombre); cabecera.appendChild(conteo);
            var cuerpoGrupo = document.createElement('div');
            cuerpoGrupo.className = 'ac-bib-cuerpo';
            cuerpoGrupo.hidden = !!biblioteca.cerrados[grupo.titulo];
            cabecera.addEventListener('click', function () {
                biblioteca.cerrados[grupo.titulo] = !biblioteca.cerrados[grupo.titulo];
                cabecera.classList.toggle('cerrado', biblioteca.cerrados[grupo.titulo]);
                cuerpoGrupo.hidden = biblioteca.cerrados[grupo.titulo];
            });
            contenedor.appendChild(cabecera);
            contenedor.appendChild(cuerpoGrupo);
            filas.forEach(function (r) {
                var fila = document.createElement('label');
                var marcado = biblioteca.elegidos.indexOf(r.id) >= 0;
                fila.className = 'ac-bib-fila' + (marcado ? ' elegida' : '');
                var radio = document.createElement('input');
                radio.type = 'checkbox'; radio.name = 'recursoBiblioteca'; radio.value = r.id; radio.checked = marcado;
                var datos = document.createElement('span');
                datos.className = 'ac-bib-datos';
                var titulo = document.createElement('span');
                titulo.className = 'ac-bib-titulo'; titulo.textContent = r.titulo;
                datos.appendChild(titulo);
                var detalle = r.detalle;
                if (detalle) {
                    var linea = document.createElement('small');
                    linea.textContent = detalle;
                    datos.appendChild(linea);
                }
                var quien = document.createElement('span');
                quien.className = 'ac-bib-quien';
                quien.textContent = r.propio ? '' : r.docente;
                fila.appendChild(radio); fila.appendChild(datos); fila.appendChild(quien);
                cuerpoGrupo.appendChild(fila);
            });
        });
        if (!total) {
            var vacio = document.createElement('div');
            vacio.className = 'ac-bib-vacio';
            vacio.textContent = 'Ningún recurso coincide con la búsqueda.';
            contenedor.appendChild(vacio);
        }
        // Lo que llega entra con el mismo fundido escalonado de los listados (movimiento.css)
        if (!(window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches)) {
            var indice = 0;
            Array.prototype.forEach.call(contenedor.querySelectorAll('.ac-bib-grupo, .ac-bib-fila'), function (el) {
                el.style.setProperty('--i', Math.min(indice++, 10));
                el.classList.add('pn-entra');
                setTimeout(function () { el.classList.remove('pn-entra'); el.style.removeProperty('--i'); }, 900 + Math.min(indice, 10) * 45);
            });
        }
        // Filas por página y paginador, como los de los listados
        elementoDe('data-selector-pie').hidden = false;
        elementoDe('data-selector-rango').textContent = (total ? inicio + 1 : 0) + '-' + Math.min(inicio + tamanio, total) + ' de ' + total
            + (biblioteca.elegidos.length ? ' · ' + biblioteca.elegidos.length + (biblioteca.elegidos.length === 1 ? ' seleccionado' : ' seleccionados') : '');
        var paginador = elementoDe('data-selector-paginador');
        paginador.textContent = '';
        paginador.appendChild(boton('‹', biblioteca.pagina - 1, biblioteca.pagina === 1, 'Página anterior'));
        var etiqueta = document.createElement('span');
        etiqueta.textContent = biblioteca.pagina + ' de ' + paginas;
        paginador.appendChild(etiqueta);
        paginador.appendChild(boton('›', biblioteca.pagina + 1, biblioteca.pagina === paginas, 'Página siguiente'));
    }

    /** Muestra las pestañas solo para los tipos que se registran a mano y pide a la biblioteca los recursos del tipo elegido. */
    function prepararPestanas(elegida) {
        var pestanas = elementoDe('data-selector-pestanas');
        var admiteBiblioteca = !!(elegida && elegida.dataset.biblioteca);
        pestanas.hidden = !admiteBiblioteca;
        if (!admiteBiblioteca) {
            biblioteca.tipo = null;
            cambiarPestana('nuevo');
            return Promise.resolve();
        }
        if (biblioteca.tipo === elegida.dataset.tipoId) { return Promise.resolve(); }
        biblioteca.tipo = elegida.dataset.tipoId;
        biblioteca.recursos = [];
        biblioteca.elegidos = [];
        cambiarPestana('nuevo');
        var boton = selector.querySelector('[data-pestana="biblioteca"]');
        boton.disabled = true;
        elementoDe('data-selector-cuenta').textContent = '…';
        var turno = ++biblioteca.pedido;
        biblioteca.pagina = 1;
        mostrarEsqueleto();
        var desde = Date.now();
        return fetch('/recurso/reutilizables?tipo=' + encodeURIComponent(biblioteca.tipo), {
            headers: { 'Accept': 'application/json' }, credentials: 'same-origin'
        }).then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; }).then(function (lista) {
            // El esqueleto se ve un instante como mínimo, para que no parpadee
            return new Promise(function (resolver) { setTimeout(function () { resolver(lista); }, Math.max(0, 260 - (Date.now() - desde))); });
        }).then(function (lista) {
            if (turno !== biblioteca.pedido) { return; }
            biblioteca.recursos = lista;
            elementoDe('data-selector-cuenta').textContent = lista.length;
            boton.disabled = !lista.length;
            boton.title = lista.length ? '' : 'No hay recursos para reutilizar';
            pintarBiblioteca();
        });
    }

    /** Registra en el tema elegido el recurso de la biblioteca que se eligió. */
    async function agregarDeLaBiblioteca() {
        var error = elementoDe('data-selector-error');
        var tema = elementoDe('data-selector-tema');
        error.textContent = '';
        tema.classList.remove('es-invalido');
        if (!tema.value) {
            tema.classList.add('es-invalido');
            error.textContent = 'Debe seleccionar el tema.';
            return;
        }
        if (!biblioteca.elegidos.length) {
            error.textContent = 'Debe elegir al menos un recurso de la biblioteca.';
            return;
        }
        var boton = elementoDe('data-selector-agregar');
        boton.disabled = true;
        try {
            var cuerpo = new FormData();
            cuerpo.append('temaId', tema.value);
            biblioteca.elegidos.forEach(function (id) { cuerpo.append('recursoIds', id); });
            var respuesta = await fetch('/recurso/reutilizar', {
                method: 'POST', body: cuerpo, credentials: 'same-origin',
                headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' }
            });
            var datos = await respuesta.json().catch(function () { return {}; });
            if (respuesta.ok && datos.mensaje) {
                modal(selector).hide();
                await window.mostrarExito(datos.mensaje);
                if (datos.siguienteUrl && window.manejarSiguientePaso && await window.manejarSiguientePaso(datos)) {
                    return;
                }
                window.location.href = datos.destino || window.location.href;
                return;
            }
            error.textContent = datos.error || 'Error! No se pudo registrar el recurso. Intentá nuevamente.';
        } finally {
            boton.disabled = false;
        }
    }

    /** Abre el selector en la pestaña de la biblioteca con ese recurso elegido (viene de "Registrar en un tema" de la biblioteca digital). */
    window.abrirSelectorConRecurso = function (idRecurso) {
        if (!selector) { return; }
        fetch('/recurso/reutilizables', { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
            .then(function (r) { return r.ok ? r.json() : []; })
            .then(function (lista) {
                var recurso = lista.filter(function (r) { return r.id === Number(idRecurso); })[0];
                if (!recurso) { return; }
                var radio = selector.querySelector('.ac-selector-opcion[data-tipo-id="' + recurso.tipo + '"] input');
                if (!radio) { return; }
                radio.checked = true;
                mostrarDetalle();
                var listo = prepararPestanas(radio.closest('.ac-selector-opcion')) || Promise.resolve();
                modal(selector).show();
                listo.then(function () {
                    biblioteca.elegidos = [recurso.id];
                    biblioteca.irAlElegido = true;
                    cambiarPestana('biblioteca');
                    pintarBiblioteca();
                });
            });
    };

    if (selector) {
        selector.addEventListener('change', function (e) {
            if (e.target.name === 'recursoBiblioteca') {
                var id = Number(e.target.value);
                var posicion = biblioteca.elegidos.indexOf(id);
                if (e.target.checked && posicion < 0) { biblioteca.elegidos.push(id); }
                if (!e.target.checked && posicion >= 0) { biblioteca.elegidos.splice(posicion, 1); }
                e.target.closest('.ac-bib-fila').classList.toggle('elegida', e.target.checked);
                var rango = elementoDe('data-selector-rango');
                rango.textContent = rango.textContent.split(' · ')[0]
                    + (biblioteca.elegidos.length ? ' · ' + biblioteca.elegidos.length + (biblioteca.elegidos.length === 1 ? ' seleccionado' : ' seleccionados') : '');
                elementoDe('data-selector-error').textContent = '';
                return;
            }
            mostrarDetalle();
        });
        selector.addEventListener('input', function (e) { if (e.target.matches('[data-selector-buscar]')) { biblioteca.pagina = 1; pintarBiblioteca(); } });
        selector.addEventListener('change', function (e) {
            if (e.target.matches('[data-selector-por-pagina]')) { biblioteca.tamanio = Number(e.target.value); biblioteca.pagina = 1; pintarConEsqueleto(); }
        });
        selector.addEventListener('click', function (e) {
            var pestana = e.target.closest('[data-pestana]');
            if (pestana && !pestana.disabled) { cambiarPestana(pestana.dataset.pestana); }
        });
        selector.addEventListener('show.bs.modal', function () {
            if (!opcionElegida() && opciones().length) {
                opciones()[0].querySelector('input').checked = true;
            }
            mostrarDetalle();
            // Si se abrió desde el botón de un tema, ese tema queda elegido para el recurso de la biblioteca
            var temaDelBoton = new URLSearchParams((selector.dataset.destino || '').split('?')[1] || '').get('temaId');
            var campoTema = elementoDe('data-selector-tema');
            if (campoTema && biblioteca.pestana !== 'biblioteca') {
                campoTema.value = temaDelBoton || '';
                campoTema.classList.remove('es-invalido');
                campoTema.dispatchEvent(new Event('change', { bubbles: true }));
            }
            elementoDe('data-selector-error').textContent = '';
        });
        selector.addEventListener('hidden.bs.modal', function () { cambiarPestana('nuevo'); });
        selector.addEventListener('click', function (e) {
            if (!e.target.closest('[data-selector-agregar]')) { return; }
            if (biblioteca.pestana === 'biblioteca') { agregarDeLaBiblioteca(); return; }
            var elegida = opcionElegida();
            if (!elegida) { return; }
            var destino = selector.dataset.destino;
            if (elegida.dataset.ruta && destino) {
                var cursoDeDestino = new URLSearchParams(destino.substring(destino.indexOf('?') + 1)).get('cursoId');
                // Pool y autoevaluación: se abre el listado con la ventana de alta ya abierta y el tema elegido
                var temaDelDestino = new URLSearchParams(destino.substring(destino.indexOf('?') + 1)).get('temaId');
                window.location.href = elegida.dataset.ruta + '?cursoId=' + cursoDeDestino + '&nuevo'
                    + (temaDelDestino ? '&temaId=' + temaDelDestino : '');
            } else if (elegida.dataset.glosario && destino) {
                // El glosario es uno por tema: se abre el del tema elegido con el formulario de alta abierto
                var consulta = new URLSearchParams(destino.substring(destino.indexOf('?') + 1));
                window.location.href = consulta.get('temaId')
                    ? '/glosario/buscarPorTema/' + consulta.get('temaId') + '?nuevo'
                    : '/glosario/buscar?cursoId=' + consulta.get('cursoId');
            } else if (destino) {
                window.location.href = destino + (destino.indexOf('?') >= 0 ? '&' : '?') + 'nuevo=' + elegida.dataset.tipoId;
            }
        });
    }

    // Botón "Agregar recurso o actividad" de cada tema: el selector lleva al ABM con ese tema elegido
    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-agregar-recurso]');
        if (!boton || !selector) { return; }
        var base = selector.dataset.destinoBase || selector.dataset.destino;
        selector.dataset.destinoBase = base;
        selector.dataset.destino = base + (base.indexOf('?') >= 0 ? '&' : '?') + 'temaId=' + boton.dataset.temaId;
        var aviso = selector.querySelector('[data-selector-aviso]');
        if (aviso) { aviso.dataset.opcion = ''; aviso.textContent = ''; }
        modal(selector).show();
    });

    // ----------------------------------------------------------------------
    // 2 y 3. Formulario de alta según el tipo (y apertura automática desde ?nuevo={tipo})
    // ----------------------------------------------------------------------

    /** Qué pide cada tipo de material (los formatos y tamaños vienen del servidor, en la opción elegida). */
    var TIPOS = {
        PRESENTACION: { archivo: true, archivoObligatorio: true, campos: ['autor', 'paginas'], obligatorios: ['autor'], etiqueta: 'Archivo de la presentación' },
        RESUMEN: { archivo: true, archivoObligatorio: true, campos: ['autor', 'paginas'], obligatorios: ['autor'], etiqueta: 'Archivo del resumen' },
        BIBLIOGRAFIA: { archivo: true, archivoObligatorio: true, campos: ['autor', 'isbn', 'editorial', 'paginas', 'fechaPublicacion'], obligatorios: ['autor', 'isbn'], etiqueta: 'Archivo de la bibliografía' },
        GRABACION: { archivo: true, archivoObligatorio: true, campos: ['duracionMinutos'], obligatorios: [], etiqueta: 'Archivo de video' },
        ENLACE: { archivo: false, archivoObligatorio: false, campos: ['enlace'], obligatorios: ['enlace'], etiqueta: 'Archivo' }
    };
    var CAMPOS = ['enlace', 'autor', 'isbn', 'editorial', 'paginas', 'fechaPublicacion', 'duracionMinutos'];

    /** Adapta el formulario de alta al tipo de material elegido. */
    function aplicarTipo(opcion) {
        var formulario = modalNuevo.querySelector('form');
        var tipo = TIPOS[opcion.value] || TIPOS.PRESENTACION;
        var extensiones = opcion.dataset.extensiones;
        var tamanio = opcion.dataset.tamanioMb;

        formulario.querySelectorAll('.es-invalido').forEach(function (campo) { window.PanelValidacion.marcar(campo, ''); });
        formulario.dataset.tipo = opcion.value;
        var vistaPrevia = formulario.querySelector('[data-vista-previa]');
        if (vistaPrevia) { vistaPrevia.hidden = true; }
        // Las bibliografías son siempre públicas: la biblioteca digital está pensada para eso
        var campoBiblioteca = formulario.elements['privado'];
        if (campoBiblioteca) {
            var esBibliografia = opcion.value === 'BIBLIOGRAFIA';
            if (esBibliografia) { campoBiblioteca.value = 'false'; }
            campoBiblioteca.disabled = esBibliografia;
        }
        var ayudaTipo = modalNuevo.querySelector('[data-material-ayuda]');
        if (ayudaTipo) { ayudaTipo.textContent = opcion.dataset.descripcion; }

        var bloqueArchivo = modalNuevo.querySelector('[data-bloque="archivo"]');
        var archivo = formulario.elements['archivo'];
        archivo.value = '';
        bloqueArchivo.hidden = !tipo.archivo;
        if (tipo.archivo) {
            archivo.accept = extensiones.split(',').map(function (e) { return '.' + e; }).join(',');
            archivo.dataset.extensiones = extensiones;
            archivo.dataset.tamanioMb = tamanio;
            archivo.required = tipo.archivoObligatorio;
            modalNuevo.querySelector('[data-archivo-etiqueta]').textContent = tipo.etiqueta;
            modalNuevo.querySelector('[data-archivo-obligatorio]').hidden = !tipo.archivoObligatorio;
            modalNuevo.querySelector('[data-archivo-ayuda]').textContent =
                'Formatos: ' + extensiones.toUpperCase().split(',').join(', ') + ' · máximo ' + tamanio + ' MB';
        } else {
            archivo.required = false;
        }
        CAMPOS.forEach(function (nombre) {
            var bloque = modalNuevo.querySelector('[data-bloque="' + nombre + '"]');
            var campo = formulario.elements[nombre];
            var visible = tipo.campos.indexOf(nombre) >= 0;
            bloque.hidden = !visible;
            campo.required = visible && tipo.obligatorios.indexOf(nombre) >= 0;
            if (campo.required) { campo.dataset.mensajeObligatorio = 'Debe completar este dato.'; }
            if (!visible) { campo.value = ''; }
        });
    }

    if (modalNuevo) {
        var campoTipo = modalNuevo.querySelector('select[name="tipo"]');
        var grupoTipo = modalNuevo.querySelector('[data-tipo-grupo]');
        var categoriaMaterial = modalNuevo.querySelector('[data-categoria-material]');
        var bloqueCategoria = modalNuevo.querySelector('[data-bloque="categoria-material"]');
        var lineaTipo = modalNuevo.querySelector('[data-tipo-linea]');
        var campoDelTipo = modalNuevo.querySelector('[data-campo-tipo]');

        /** El tipo ocupa todo el ancho cuando no hay categoría de material al lado. */
        function ajustarAncho(esMaterial) {
            campoDelTipo.classList.toggle('col-md-6', esMaterial);
            campoDelTipo.classList.toggle('col-12', !esMaterial);
        }

        // Solo se ofrecen los tipos que el servidor permite en esta pantalla
        [grupoTipo, categoriaMaterial].forEach(function (select) {
            Array.prototype.slice.call(select.options).forEach(function (opcion) {
                var existe = opcion.value === 'MATERIAL_LECTURA'
                    ? campoTipo.querySelector('option[value="PRESENTACION"], option[value="RESUMEN"]') : campoTipo.querySelector('option[value="' + opcion.value + '"]');
                if (!existe) { opcion.remove(); }
            });
        });

        /** Arma el tipo que recibe el servidor: Material de lectura se concreta con su clase (Presentación o Resumen). */
        function sincronizarTipo() {
            var esMaterial = grupoTipo.value === 'MATERIAL_LECTURA';
            bloqueCategoria.hidden = !esMaterial;
            ajustarAncho(esMaterial);
            campoTipo.value = esMaterial ? categoriaMaterial.value : grupoTipo.value;
            if (lineaTipo) {
                lineaTipo.textContent = esMaterial ? 'Material de lectura · ' + categoriaMaterial.selectedOptions[0].textContent : grupoTipo.selectedOptions[0].textContent;
            }
            campoTipo.dispatchEvent(new Event('change', { bubbles: true }));
        }

        /** Deja los selectores como corresponde a un tipo concreto (por ejemplo, al abrir desde ?nuevo=RESUMEN). */
        function mostrarTipo(valor) {
            var esMaterial = valor === 'PRESENTACION' || valor === 'RESUMEN';
            grupoTipo.value = esMaterial ? 'MATERIAL_LECTURA' : valor;
            if (esMaterial) { categoriaMaterial.value = valor; }
            bloqueCategoria.hidden = !esMaterial;
            ajustarAncho(esMaterial);
            campoTipo.value = valor;
            if (lineaTipo) {
                lineaTipo.textContent = esMaterial ? 'Material de lectura · ' + categoriaMaterial.selectedOptions[0].textContent : grupoTipo.selectedOptions[0].textContent;
            }
        }

        grupoTipo.addEventListener('change', sincronizarTipo);
        categoriaMaterial.addEventListener('change', sincronizarTipo);
        mostrarTipo(campoTipo.value);
        campoTipo.addEventListener('change', function () { aplicarTipo(campoTipo.selectedOptions[0]); });
        modalNuevo.addEventListener('show.bs.modal', function () { aplicarTipo(campoTipo.selectedOptions[0]); });

        // Apertura automática desde ?nuevo={tipo}, con el tema elegido
        var tipoInicial = modalNuevo.dataset.abrirTipo;
        if (tipoInicial && campoTipo.querySelector('option[value="' + tipoInicial + '"]')) {
            mostrarTipo(tipoInicial);
            if (modalNuevo.dataset.tema) {
                modalNuevo.querySelector('select[name="temaId"]').value = modalNuevo.dataset.tema;
            }
            modal(modalNuevo).show();
            // Se quita "nuevo" de la dirección: al recargar después de subir, el formulario no vuelve a abrirse
            var direccion = new URL(window.location.href);
            direccion.searchParams.delete('nuevo');
            window.history.replaceState(null, '', direccion.pathname + direccion.search);
        }
    }

    // Apertura automática de la ventana de un material (?accion=editar|baja&materialId=…)
    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && document.getElementById(principal.dataset.abrirModal)) {
        modal(document.getElementById(principal.dataset.abrirModal)).show();
    }

    // ----------------------------------------------------------------------
    // 4. Validación del archivo y aviso de subida
    // ----------------------------------------------------------------------

    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.material = function (paso, formulario) {
        if (paso !== 2) { return true; } // el archivo y los datos de la obra se piden en el segundo paso
        var enlace = formulario.elements['enlace'];
        if (formulario.dataset.tipo === 'ENLACE' && enlace.value.trim() !== '' && !/^https?:\/\//i.test(enlace.value.trim())) {
            window.PanelValidacion.marcar(enlace, 'El enlace no es válido. Formato válido: comienza con http:// o https://.');
            return false;
        }
        return true;
    };

    // Cuenta las páginas de un PDF con PDF.js (se carga la primera vez que hace falta).
    var PDFJS = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/';
    var cargaPdfjs = null;
    function cargarPdfjs() {
        if (window.pdfjsLib) { return Promise.resolve(window.pdfjsLib); }
        if (!cargaPdfjs) {
            cargaPdfjs = new Promise(function (resolver, rechazar) {
                var script = document.createElement('script');
                script.src = PDFJS + 'pdf.min.js';
                script.onload = function () {
                    window.pdfjsLib.GlobalWorkerOptions.workerSrc = PDFJS + 'pdf.worker.min.js';
                    resolver(window.pdfjsLib);
                };
                script.onerror = rechazar;
                document.head.appendChild(script);
            });
        }
        return cargaPdfjs;
    }

    function contarPaginasAlMenos(archivo) {
        return new Promise(function (resolver) {
            var lector = new FileReader();
            lector.onload = function () { resolver((String(lector.result).match(/\/Type\s*\/Page[^s]/g) || []).length); };
            lector.onerror = function () { resolver(0); };
            lector.readAsBinaryString(archivo);
        });
    }

    function contarPaginas(archivo) {
        return cargarPdfjs().then(function (pdfjs) {
            return archivo.arrayBuffer().then(function (datos) { return pdfjs.getDocument({ data: datos }).promise; });
        }).then(function (pdf) { return pdf.numPages; }).catch(function () { return contarPaginasAlMenos(archivo); });
    }

    // ----------------------------------------------------------------------
    // 5. Vista previa: la primera página de un PDF o un cuadro de un video, en una miniatura
    // ----------------------------------------------------------------------

    var ANCHO_VISTA_PREVIA = 180;

    var turnoVistaPrevia = 0;

    function mostrarVistaPrevia(contenedor, mostrar) { contenedor.hidden = !mostrar; }

    /** Reemplaza el lienzo por uno nuevo (así dos pedidos seguidos no usan el mismo) y devuelve el turno de este pedido. */
    function prepararLienzo(contenedor) {
        var nuevo = document.createElement('canvas');
        nuevo.setAttribute('aria-label', 'Vista previa del archivo');
        contenedor.replaceChild(nuevo, contenedor.querySelector('canvas'));
        contenedor.dataset.turno = String(++turnoVistaPrevia);
        return { lienzo: nuevo, turno: contenedor.dataset.turno };
    }

    function vistaPreviaDePdf(contenedor, datos) {
        var preparado = prepararLienzo(contenedor);
        var lienzo = preparado.lienzo;
        var vigente = function () { return contenedor.dataset.turno === preparado.turno; };
        return cargarPdfjs().then(function (pdfjs) { return pdfjs.getDocument({ data: datos }).promise; })
            .then(function (pdf) { return pdf.getPage(1); })
            .then(function (pagina) {
                var escala = ANCHO_VISTA_PREVIA / pagina.getViewport({ scale: 1 }).width;
                var vista = pagina.getViewport({ scale: escala });
                lienzo.width = vista.width;
                lienzo.height = vista.height;
                return pagina.render({ canvasContext: lienzo.getContext('2d'), viewport: vista }).promise;
            })
            .then(function () { if (vigente()) { mostrarVistaPrevia(contenedor, true); } })
            .catch(function () { if (vigente()) { mostrarVistaPrevia(contenedor, false); } });
    }

    function vistaPreviaDeVideo(contenedor, url, liberar) {
        var preparado = prepararLienzo(contenedor);
        var lienzo = preparado.lienzo;
        var video = document.createElement('video');
        video.preload = 'auto';
        video.muted = true;
        video.onloadeddata = function () { video.currentTime = Math.min(1, (isFinite(video.duration) ? video.duration : 2) / 2); };
        video.onseeked = function () {
            var alto = Math.round(ANCHO_VISTA_PREVIA * (video.videoHeight || 9) / (video.videoWidth || 16));
            lienzo.width = ANCHO_VISTA_PREVIA;
            lienzo.height = alto;
            lienzo.getContext('2d').drawImage(video, 0, 0, ANCHO_VISTA_PREVIA, alto);
            if (contenedor.dataset.turno === preparado.turno) { mostrarVistaPrevia(contenedor, true); }
            if (liberar) { URL.revokeObjectURL(url); }
        };
        video.onerror = function () { mostrarVistaPrevia(contenedor, false); };
        video.src = url;
    }

    /** Dibuja la vista previa del archivo que se acaba de elegir en el formulario. */
    function vistaPreviaDelArchivo(formulario, archivo) {
        var contenedor = formulario.querySelector('[data-vista-previa]');
        if (!contenedor) { return; }
        mostrarVistaPrevia(contenedor, false);
        if (/\.pdf$/i.test(archivo.name)) {
            archivo.arrayBuffer().then(function (datos) { vistaPreviaDePdf(contenedor, datos); });
        } else if (/\.(mp4|webm)$/i.test(archivo.name)) {
            vistaPreviaDeVideo(contenedor, URL.createObjectURL(archivo), true);
        }
    }

    // Ventana de modificación: la vista previa del archivo que el recurso ya tiene
    document.addEventListener('shown.bs.modal', function (e) {
        var contenedor = e.target.querySelector ? e.target.querySelector('[data-vista-previa][data-fuente]') : null;
        if (!contenedor || contenedor.dataset.cargada) { return; }
        contenedor.dataset.cargada = 'true';
        if (contenedor.dataset.formato === 'pdf') {
            fetch(contenedor.dataset.fuente, { credentials: 'same-origin' })
                .then(function (respuesta) { return respuesta.ok ? respuesta.arrayBuffer() : Promise.reject(); })
                .then(function (datos) { vistaPreviaDePdf(contenedor, datos); })
                .catch(function () { mostrarVistaPrevia(contenedor, false); });
        } else if (contenedor.dataset.formato === 'video') {
            vistaPreviaDeVideo(contenedor, contenedor.dataset.fuente, false);
        }
    });

    // Páginas y duración: los calcula el navegador al elegir el archivo (un PDF cuenta sus páginas. Un video lee su duración)
    document.addEventListener('change', function (e) {
        var entrada = e.target;
        if (!entrada.matches || !entrada.matches('input[type="file"][name="archivo"]') || !entrada.files || !entrada.files[0]) { return; }
        var formulario = entrada.form;
        var archivo = entrada.files[0];
        vistaPreviaDelArchivo(formulario, archivo);
        var paginas = formulario.elements['paginas'];
        var duracion = formulario.elements['duracionMinutos'];
        var duracionMin = formulario.querySelector('[data-duracion-minutos]');
        var duracionSeg = formulario.querySelector('[data-duracion-segundos]');
        var duracionLinea = formulario.querySelector('[data-duracion-linea]');
        if (/\.pdf$/i.test(archivo.name) && paginas) {
            contarPaginas(archivo).then(function (cuenta) { paginas.value = cuenta > 0 ? cuenta : ''; });
        } else if (/\.(mp4|webm)$/i.test(archivo.name) && duracion) {
            var video = document.createElement('video');
            video.preload = 'metadata';
            video.onloadedmetadata = function () {
                URL.revokeObjectURL(video.src);
                // El servidor guarda los minutos (redondeados hacia arriba). Los segundos se muestran para que se vea lo que dura el video
                var total = isFinite(video.duration) ? Math.max(1, Math.round(video.duration)) : 0;
                duracion.value = total ? Math.ceil(total / 60) : '';
                if (duracionMin) { duracionMin.value = total ? Math.floor(total / 60) : ''; }
                if (duracionSeg) { duracionSeg.value = total ? total % 60 : ''; }
                if (duracionLinea) { duracionLinea.textContent = total ? Math.floor(total / 60) + ' min ' + (total % 60) + ' s' : ''; }
            };
            video.src = URL.createObjectURL(archivo);
        }
    });

    document.addEventListener('submit', function (e) {
        var formulario = e.target;
        if (e.defaultPrevented || !formulario.matches || !formulario.matches('form[enctype="multipart/form-data"]')) { return; }
        var archivo = formulario.elements['archivo'];
        var boton = formulario.querySelector('button[type="submit"]');
        if (!archivo || !archivo.files || !archivo.files.length || !boton) { return; }
        var original = boton.textContent;
        boton.textContent = 'Subiendo…';
        // core/formulario.js vuelve a habilitar el botón si hubo un error: ahí se restaura el texto
        new MutationObserver(function (cambios, observador) {
            if (!boton.disabled) {
                boton.textContent = original;
                observador.disconnect();
            }
        }).observe(boton, { attributes: true, attributeFilter: ['disabled'] });
    });
})();
