/**
 * Comportamiento del ABM de materiales (MOD-F-02).
 *
 *  1. Selector "Agregar un recurso o actividad" (vista del curso): muestra la descripción de lo elegido y, al
 *     confirmar, redirige al ABM de materiales con el tema y el tipo elegidos o, si se elige Glosario, al
 *     glosario del tema con el formulario de alta abierto.
 *  2. Formulario de alta: el tipo de material es un campo del formulario y los datos que pide (formatos y tamaño del
 *     archivo, enlace externo y autor) se adaptan al tipo.
 *  3. Si la página se abre con ?nuevo={tipo}, abre sola el formulario con ese tipo elegido.
 *  4. Validación del archivo (formato y tamaño) y aviso "Subiendo…" mientras se envía.
 *
 * Los formatos, tamaños y reglas de cada tipo vienen del servidor (atributos data- de las opciones).
 */
(function () {
    'use strict';

    var selector = document.getElementById('modalSelectorRecurso');
    var modalNuevo = document.getElementById('modalNuevoMaterial');

    function modal(elemento) {
        return window.bootstrap.Modal.getOrCreateInstance(elemento);
    }

    // ------------------------------------------------------------------
    // 1. Selector de recurso o actividad
    // ------------------------------------------------------------------
    function opciones() {
        return Array.prototype.slice.call(selector.querySelectorAll('.ac-selector-opcion:not(.ac-selector-opcion-deshabilitada)'));
    }

    function opcionElegida() {
        var marcada = selector.querySelector('.ac-selector-opcion input:checked');
        return marcada ? marcada.closest('.ac-selector-opcion') : null;
    }

    function mostrarDetalle() {
        var elegida = opcionElegida();
        opciones().forEach(function (o) { o.classList.toggle('elegida', o === elegida); });
        selector.querySelector('[data-selector-titulo]').textContent = elegida ? elegida.dataset.nombre : '';
        selector.querySelector('[data-selector-descripcion]').textContent = elegida ? elegida.dataset.descripcion : '';
        // El aviso (por ejemplo, registrar un pool primero) acompaña solo a la opción para la que se dio
        var aviso = selector.querySelector('[data-selector-aviso]');
        if (aviso) { aviso.hidden = !aviso.dataset.opcion || !elegida || aviso.dataset.opcion !== elegida.querySelector('input').value; }
    }

    if (selector) {
        selector.addEventListener('change', mostrarDetalle);
        selector.addEventListener('show.bs.modal', function () {
            if (!opcionElegida() && opciones().length) {
                opciones()[0].querySelector('input').checked = true;
            }
            mostrarDetalle();
        });
        selector.addEventListener('click', function (e) {
            if (!e.target.closest('[data-selector-agregar]')) { return; }
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
                    ? '/glosario/ver/' + consulta.get('temaId') + '?nuevo'
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

    // ------------------------------------------------------------------
    // 2 y 3. Formulario de alta según el tipo (y apertura automática desde ?nuevo={tipo})
    // ------------------------------------------------------------------
    /**
     * Qué pide cada tipo de material (los formatos y tamaños vienen del servidor, en la opción elegida): si lleva
     * archivo y si es obligatorio, y los demás campos que muestra con los que son obligatorios.
     */
    var TIPOS = {
        PRESENTACION: { archivo: true, archivoObligatorio: true, campos: ['autor', 'paginas'], obligatorios: ['autor'], etiqueta: 'Archivo de la presentación' },
        BIBLIOGRAFIA: { archivo: true, archivoObligatorio: false, campos: ['autor', 'isbn', 'editorial', 'paginas', 'fechaPublicacion'], obligatorios: ['autor', 'isbn'], etiqueta: 'Archivo (opcional)' },
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
        campoTipo.addEventListener('change', function () { aplicarTipo(campoTipo.selectedOptions[0]); });
        modalNuevo.addEventListener('show.bs.modal', function () { aplicarTipo(campoTipo.selectedOptions[0]); });

        // Apertura automática desde ?nuevo={tipo}, con el tema elegido
        var tipoInicial = modalNuevo.dataset.abrirTipo;
        if (tipoInicial && campoTipo.querySelector('option[value="' + tipoInicial + '"]')) {
            campoTipo.value = tipoInicial;
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

    // ------------------------------------------------------------------
    // 4. Validación del archivo y aviso de subida
    // ------------------------------------------------------------------
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

    // Cuenta las páginas de un PDF con PDF.js (se carga la primera vez que hace falta): leer el texto del archivo no alcanza porque los
    // PDF modernos guardan sus páginas dentro de objetos comprimidos. Si PDF.js no se puede cargar, se cuenta como antes.
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

    // Páginas y duración: los calcula el navegador al elegir el archivo (un PDF cuenta sus páginas; un video lee su duración)
    document.addEventListener('change', function (e) {
        var entrada = e.target;
        if (!entrada.matches || !entrada.matches('input[type="file"][name="archivo"]') || !entrada.files || !entrada.files[0]) { return; }
        var formulario = entrada.form;
        var archivo = entrada.files[0];
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
                // El servidor guarda los minutos (redondeados hacia arriba); los segundos se muestran para que se vea lo que dura el video
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
        // formularios.js vuelve a habilitar el botón si hubo un error: ahí se restaura el texto
        new MutationObserver(function (cambios, observador) {
            if (!boton.disabled) {
                boton.textContent = original;
                observador.disconnect();
            }
        }).observe(boton, { attributes: true, attributeFilter: ['disabled'] });
    });
})();
