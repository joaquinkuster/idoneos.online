/**
 * Índice del documento (paso "Índice" de los formularios de recursos): cuando el recurso es un PDF.
 * Se importan sus marcadores y, si no tiene, se arma a mano tocando los títulos en una vista previa de sus páginas.
 */
(function () {
    'use strict';

    var PDFJS = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/';
    var MAXIMO_DE_ENTRADAS = 300;
    var MAXIMO_DEL_TITULO = 150;
    var NIVEL_MAXIMO = 4;
    var ZOOMS = [0.75, 1, 1.25, 1.5, 2];
    // Los tipos de recurso que llevan índice: los que tienen un PDF obligatorio
    var TIPOS_CON_INDICE = ['PRESENTACION', 'RESUMEN', 'BIBLIOGRAFIA'];

    // ----------------------------------------------------------------------
    // PDF.js
    // ----------------------------------------------------------------------

    var promesaPdfjs = null;

    function cargarPdfjs() {
        if (window.pdfjsLib) { return Promise.resolve(window.pdfjsLib); }
        if (!promesaPdfjs) {
            promesaPdfjs = new Promise(function (resolver, rechazar) {
                var script = document.createElement('script');
                script.src = PDFJS + 'pdf.min.js';
                script.onload = function () {
                    window.pdfjsLib.GlobalWorkerOptions.workerSrc = PDFJS + 'pdf.worker.min.js';
                    resolver(window.pdfjsLib);
                };
                script.onerror = function () { promesaPdfjs = null; rechazar(new Error('No se pudo cargar PDF.js')); };
                document.head.appendChild(script);
            });
        }
        return promesaPdfjs;
    }

    function abrirDesdeBytes(bytes) {
        return cargarPdfjs().then(function (pdfjs) { return pdfjs.getDocument({ data: bytes }).promise; });
    }

    function abrirDesdeUrl(url) {
        return cargarPdfjs().then(function (pdfjs) { return pdfjs.getDocument({ url: url, withCredentials: true }).promise; });
    }

    // ----------------------------------------------------------------------
    // marcadores del PDF
    // ----------------------------------------------------------------------

    function paginaDeDestino(doc, destino) {
        if (!destino) { return Promise.resolve(null); }
        var resuelto = typeof destino === 'string' ? doc.getDestination(destino) : Promise.resolve(destino);
        return resuelto.then(function (arreglo) {
            if (!arreglo || !arreglo.length) { return null; }
            var referencia = arreglo[0];
            if (referencia && typeof referencia === 'object') { return doc.getPageIndex(referencia).then(function (i) { return i + 1; }); }
            return Number.isInteger(referencia) ? referencia + 1 : null;
        }).catch(function () { return null; });
    }

    function aplanar(doc, items, nivel) {
        return Promise.all(items.map(function (item) {
            return paginaDeDestino(doc, item.dest).then(function (pagina) {
                var fila = pagina ? [{ titulo: limpiar(item.title), pagina: pagina, nivel: Math.min(nivel, NIVEL_MAXIMO) }] : [];
                return aplanar(doc, item.items || [], nivel + 1).then(function (hijos) { return fila.concat(hijos); });
            });
        })).then(function (listas) { return [].concat.apply([], listas); });
    }

    function limpiar(texto) {
        var t = String(texto || '').replace(/\s+/g, ' ').trim();
        return t.length > MAXIMO_DEL_TITULO ? t.substring(0, MAXIMO_DEL_TITULO - 1).trim() + '…' : t;
    }

    /** Detecta el índice de un documento: sus marcadores. Si no tiene, el índice se carga a mano. */
    function detectar(doc) {
        return doc.getOutline().then(function (arbol) {
            if (arbol && arbol.length) { return aplanar(doc, arbol, 1); }
            return null;
        }).catch(function () { return null; }).then(function (marcadores) {
            if (marcadores && marcadores.length) { return { entradas: marcadores.slice(0, MAXIMO_DE_ENTRADAS), origen: 'marcadores' }; }
            return { entradas: [], origen: 'ninguno' };
        });
    }

    /** Indica si el PDF es un escaneo: en una muestra de páginas casi no hay texto que se pueda leer. */
    function esEscaneo(doc) {
        var paginas = doc.numPages;
        var muestra = Math.min(paginas, 8);
        var numeros = [];
        for (var i = 0; i < muestra; i++) {
            numeros.push(muestra === paginas ? i + 1 : 1 + Math.floor(i * (paginas - 1) / (muestra - 1)));
        }
        return Promise.all(numeros.map(function (n) {
            return doc.getPage(n).then(function (p) { return p.getTextContent(); })
                .then(function (c) { return c.items.reduce(function (suma, it) { return suma + (it.str || '').trim().length; }, 0); })
                .catch(function () { return 0; });
        })).then(function (cantidades) {
            return cantidades.reduce(function (a, b) { return a + b; }, 0) < 25 * muestra;
        });
    }

    // ----------------------------------------------------------------------
    // texto de una página: las líneas que se pueden tocar para agregarlas al índice
    // ----------------------------------------------------------------------

    /** Agrupa los fragmentos de texto de una página en líneas, con su posición y su tamaño de letra. */
    function lineasDePagina(contenido) {
        var items = contenido.items.filter(function (it) { return typeof it.str === 'string' && it.str.trim() !== '' && it.transform; })
            .map(function (it) {
                return { texto: it.str, x: it.transform[4], y: it.transform[5], ancho: it.width || 0, tam: Math.abs(it.height || it.transform[3] || 0) };
            });
        items.sort(function (a, b) { return b.y - a.y || a.x - b.x; });
        var lineas = [];
        items.forEach(function (it) {
            var ultima = lineas[lineas.length - 1];
            if (ultima && Math.abs(ultima.y - it.y) <= Math.max(it.tam, ultima.tam) * 0.4) {
                var hueco = it.x - ultima.finX;
                ultima.texto += (hueco > it.tam * 0.15 ? ' ' : '') + it.texto;
                ultima.finX = it.x + it.ancho;
                ultima.tam = Math.max(ultima.tam, it.tam);
            } else {
                lineas.push({ texto: it.texto, x: it.x, y: it.y, finX: it.x + it.ancho, tam: it.tam });
            }
        });
        return lineas;
    }

    // ----------------------------------------------------------------------
    // el paso del formulario
    // ----------------------------------------------------------------------

    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function iniciar(formulario) {
        var paso = formulario.querySelector('[data-paso-indice]');
        if (!paso || paso.dataset.indiceIniciado) { return; }
        paso.dataset.indiceIniciado = '1';
        var contenido = paso.querySelector('[data-indice-contenido]');
        var campo = paso.querySelector('[data-indice-json]');
        var archivo = formulario.querySelector('input[type="file"][name="archivo"]');
        var vistaPrevia = formulario.querySelector('[data-vista-previa][data-formato="pdf"]');
        var urlGuardado = paso.dataset.indiceUrl || '';
        var urlArchivoActual = vistaPrevia ? vistaPrevia.dataset.fuente : '';
        var resumen = paso.querySelector('[data-indice-resumen]');
        var ocrId = paso.dataset.ocrId || '';
        // Reconocimiento de texto que arranca al elegir un PDF escaneado, antes de guardar el recurso
        var previoToken = '';
        var campoToken = document.createElement('input');
        campoToken.type = 'hidden';
        campoToken.name = 'ocrToken';
        paso.appendChild(campoToken);

        var estado = { modo: 'ninguno', entradas: [], origen: '', editando: -1, elegida: -1, turno: 0, base: '', doc: null, pagina: 1, dibujo: 0, esqueleto: '', zoom: 1, sinTexto: false, ocr: { estado: '', porcentaje: null, segundos: null } };

        function mostrarPaso(visible) {
            paso.hidden = !visible;
            campo.disabled = !visible;
            if (formulario.pasos && formulario.pasos.refrescar) { formulario.pasos.refrescar(); }
        }

        function sincronizar() {
            var actual = JSON.stringify(estado.entradas.map(function (e) { return { titulo: e.titulo, pagina: e.pagina, nivel: e.nivel }; }));
            campo.value = actual;
            // La línea del resumen: solo la cantidad de entradas
            if (resumen) {
                var n = estado.entradas.length;
                resumen.textContent = estado.modo === 'listo' || estado.modo === 'error' || estado.modo === 'sinarchivo'
                    ? (n ? n + (n === 1 ? ' entrada' : ' entradas') : 'sin entradas') : '';
            }
        }

        function fijarBase() {
            estado.base = JSON.stringify(estado.entradas.map(function (e) { return { titulo: e.titulo, pagina: e.pagina, nivel: e.nivel }; }));
        }

        // --------------------------------------------------------------
        // la lista de entradas
        // --------------------------------------------------------------

        function filaHtml(e, i) {
            var elegida = i === estado.elegida;
            var titulo = estado.editando === i
                ? '<input type="text" class="indice-editar" maxlength="' + MAXIMO_DEL_TITULO + '" value="' + esc(e.titulo) + '" data-indice-editar>'
                : '<span class="indice-titulo" title="' + esc(e.titulo) + '">' + esc(e.titulo) + '</span><span class="indice-puntos" aria-hidden="true"></span>';
            // Las acciones aparecen al pasar el mouse o al elegir la fila; el número de página se oculta mientras tanto
            var derecha = estado.editando !== i
                ? '<span class="indice-acciones">'
                    + '<button type="button" class="indice-icono-boton indice-lapiz" data-indice-lapiz title="Editar el título" aria-label="Editar el título"><i class="fa-solid fa-pencil"></i></button>'
                    + '<button type="button" class="indice-icono-boton indice-nivel" data-indice-nivel title="Nivel ' + e.nivel + ': pasar al siguiente" aria-label="Cambiar de nivel"><i class="fa-solid fa-indent"></i></button>'
                    + '<button type="button" class="indice-icono-boton indice-quitar" data-indice-quitar title="Quitar entrada" aria-label="Quitar entrada"><i class="fa-solid fa-trash"></i></button>'
                    + '</span><span class="indice-numero">' + e.pagina + '</span>'
                : '<span class="indice-numero">' + e.pagina + '</span>';
            return '<div class="indice-fila indice-n' + e.nivel + (elegida ? ' elegida' : '') + '" data-indice-fila="' + i + '" tabindex="0" role="button"'
                + ' style="padding-left:' + (10 + (e.nivel - 1) * 18) + 'px">' + titulo + derecha + '</div>';
        }

        function pintarLista() {
            var lista = contenido.querySelector('[data-indice-lista]');
            if (!lista) { sincronizar(); return; }
            var desplazamiento = lista.scrollTop;
            lista.innerHTML = estado.entradas.length
                ? estado.entradas.map(filaHtml).join('')
                : '<div class="indice-vacio">Todavía no hay entradas. Navegá el documento y tocá los títulos para agregarlos.</div>';
            lista.scrollTop = desplazamiento;
            var cuenta = contenido.querySelector('[data-indice-cuenta]');
            if (cuenta) { var n = estado.entradas.length; cuenta.textContent = n + (n === 1 ? ' entrada' : ' entradas'); }
            var edicion = lista.querySelector('[data-indice-editar]');
            if (edicion) { edicion.focus(); edicion.select(); }
            marcarLineasUsadas();
            sincronizar();
        }

        // --------------------------------------------------------------
        // la vista previa de la página
        // --------------------------------------------------------------

        function marcarLineasUsadas() {
            contenido.querySelectorAll('[data-indice-linea]').forEach(function (l) {
                var usada = estado.entradas.some(function (e) { return e.pagina === estado.pagina && e.titulo === limpiar(l.dataset.indiceLinea); });
                l.classList.toggle('usada', usada);
            });
        }

        function pintarPagina() {
            var hoja = contenido.querySelector('[data-indice-hoja]');
            if (!hoja || !estado.doc) { return; }
            var turno = ++estado.dibujo;
            var numero = estado.pagina;
            var entrada = contenido.querySelector('[data-indice-pagina]');
            if (entrada) { entrada.value = numero; }
            var anterior = contenido.querySelector('[data-indice-anterior]');
            var siguiente = contenido.querySelector('[data-indice-siguiente]');
            if (anterior) { anterior.disabled = numero <= 1; }
            if (siguiente) { siguiente.disabled = numero >= estado.doc.numPages; }
            var valorZoom = contenido.querySelector('[data-indice-zoom-valor]');
            if (valorZoom) { valorZoom.textContent = Math.round(estado.zoom * 100) + '%'; }
            estado.doc.getPage(numero).then(function (pagina) {
                var base = pagina.getViewport({ scale: 1 });
                // Al 100 % la página entera entra en el visor
                var caja = contenido.querySelector('.indice-lienzo');
                var libreAncho = Math.max(caja ? caja.offsetWidth : 0, 120);
                var libreAlto = Math.max(caja ? caja.offsetHeight : 0, 160);
                var escala = Math.min(libreAncho / base.width, libreAlto / base.height) * estado.zoom;
                var vista = pagina.getViewport({ scale: escala });
                var densidad = window.devicePixelRatio || 1;
                var lienzo = document.createElement('canvas');
                lienzo.width = Math.floor(vista.width * densidad);
                lienzo.height = Math.floor(vista.height * densidad);
                lienzo.style.width = vista.width + 'px';
                lienzo.style.height = vista.height + 'px';
                var dibujo = pagina.render({ canvasContext: lienzo.getContext('2d'), viewport: pagina.getViewport({ scale: escala * densidad }) }).promise;
                return Promise.all([dibujo, pagina.getTextContent()]).then(function (r) {
                    if (turno !== estado.dibujo) { return; }
                    hoja.style.width = vista.width + 'px';
                    hoja.style.height = vista.height + 'px';
                    hoja.innerHTML = '';
                    hoja.appendChild(lienzo);
                    lineasDePagina(r[1]).forEach(function (l) {
                        var punto = vista.convertToViewportPoint(l.x, l.y);
                        var alto = Math.max(l.tam * escala, 6);
                        var linea = document.createElement('span');
                        linea.className = 'indice-linea';
                        linea.dataset.indiceLinea = l.texto.trim();
                        linea.title = 'Agregar al índice: ' + limpiar(l.texto);
                        linea.style.left = (punto[0] - 2) + 'px';
                        linea.style.top = (punto[1] - alto) + 'px';
                        linea.style.width = Math.min(((l.finX - l.x) * escala) + 4, vista.width - punto[0] + 2) + 'px';
                        linea.style.height = (alto * 1.25) + 'px';
                        hoja.appendChild(linea);
                    });
                    marcarLineasUsadas();
                });
            }).catch(function () {
                if (turno === estado.dibujo) { hoja.innerHTML = '<span class="indice-sin-vista">No se pudo mostrar la página.</span>'; }
            });
        }

        function irAPagina(numero) {
            if (!estado.doc) { return; }
            estado.pagina = Math.min(Math.max(1, numero), estado.doc.numPages);
            pintarPagina();
        }

        // --------------------------------------------------------------
        // armado del paso
        // --------------------------------------------------------------

        function aviso() {
            // Si el PDF se está escaneando, el progreso se muestra aparte (cuando no tiene marcadores ya va en el mensaje principal)
            return avisoPrincipal() + (ocrEnCurso() && estado.modo === 'listo' && estado.origen !== 'ninguno' ? '<div class="pn-alerta pn-alerta-dorado" data-indice-ocr>' + textoOcr(false) + '</div>' : '');
        }

        function avisoPrincipal() {
            if (estado.modo === 'analizando') { return '<div class="pn-alerta pn-alerta-azul">Estamos leyendo el documento para armar el índice…</div>'; }
            if (estado.modo === 'sinarchivo') { return '<div class="pn-alerta pn-alerta-azul">Elegí el PDF en el paso anterior para importar sus marcadores o armar el índice sobre sus páginas.</div>'; }
            if (estado.modo === 'error') { return '<div class="pn-alerta pn-alerta-dorado">No se pudo leer el documento. Podés agregar las entradas a mano si tiene páginas para mostrar.</div>'; }
            if (estado.origen === 'marcadores') { return '<div class="pn-alerta pn-alerta-azul">Importamos ' + estado.entradas.length + ' entradas de los marcadores del PDF. Podés corregirlas si hace falta.</div>'; }
            if (estado.origen === 'guardado') { return '<div class="pn-alerta pn-alerta-azul">Este es el índice guardado del documento. Podés corregirlo si hace falta.</div>'; }
            if (ocrEnCurso()) { return '<div class="pn-alerta pn-alerta-dorado" data-indice-ocr>' + textoOcr(true) + '</div>'; }
            if (estado.ocr.estado === 'Listo') { return '<div class="pn-alerta pn-alerta-azul">Terminó el escaneo del PDF. Armá el índice tocando los títulos en la página.</div>'; }
            if (estado.ocr.estado === 'Error') { return '<div class="pn-alerta pn-alerta-dorado">No se pudo reconocer el texto del PDF. Podés agregar las entradas en la página que corresponda.</div>'; }
            if (estado.sinTexto) {
                return '<div class="pn-alerta pn-alerta-dorado">Este PDF es un escaneo y no tiene marcadores. Agregá las entradas en la página que corresponda.</div>';
            }
            return '<div class="pn-alerta pn-alerta-dorado">Este PDF no tiene marcadores. Armá el índice tocando los títulos en la página.</div>';
        }

        // --------------------------------------------------------------
        // escaneo del PDF (OCR): si todavía se está reconociendo su texto, se muestra el progreso
        // --------------------------------------------------------------

        function ocrEnCurso() {
            return (!!ocrId || !!previoToken) && (estado.ocr.estado === 'Pendiente' || estado.ocr.estado === 'Procesando');
        }

        function tiempoRestante(segundos) {
            return segundos <= 90 ? segundos + ' segundos' : Math.ceil(segundos / 60) + ' min';
        }

        /** Segundos que faltan según la última estimación del servidor: baja de a uno, como un contador. */
        function segundosQueFaltan() {
            return estado.ocr.fin == null ? null : Math.max(0, Math.ceil((estado.ocr.fin - Date.now()) / 1000));
        }

        function detalleOcr() {
            if (estado.ocr.estado === 'Pendiente') { return 'En cola. Esperando su turno...'; }
            // Solo el reconocimiento del texto lleva porcentaje y tiempo: las otras etapas dicen qué se está haciendo
            if (estado.ocr.fase === 'optimizando') { return 'Optimizando las imágenes del PDF...'; }
            if (estado.ocr.fase === 'guardando') { return 'Guardando el PDF final...'; }
            var s = segundosQueFaltan();
            return 'Reconociendo el texto ' + (estado.ocr.porcentaje || 0) + '%.' + (s == null ? '' : (s === 0 ? ' Unos segundos más...' : ' Faltan unos ' + tiempoRestante(s) + '...'));
        }

        /** Actualiza el avance sin volver a armar la caja, para que la barra se mueva suave. */
        function refrescarProgreso() {
            var caja = contenido.querySelector('[data-indice-ocr]');
            if (!caja) { return; }
            var barra = caja.querySelector('.indice-progreso i');
            var detalle = caja.querySelector('.indice-progreso-texto');
            if (barra && detalle) {
                barra.style.width = (estado.ocr.porcentaje || 0) + '%';
                detalle.textContent = detalleOcr();
            } else {
                caja.innerHTML = textoOcr(estado.origen === 'ninguno');
            }
        }

        function textoOcr(sinMarcadores) {
            var p = estado.ocr.porcentaje || 0;
            var detalle = detalleOcr();
            var texto = sinMarcadores
                ? 'Este PDF es un escaneo. Estamos reconociendo su texto (OCR).'
                : 'Estamos reconociendo el texto del PDF (OCR).';
            return texto + '<div class="indice-progreso"><i style="width:' + p + '%"></i></div><div class="indice-progreso-texto">' + detalle + '</div>';
        }

        var vigilancia = null;
        var observadorLienzo = null;

        function detenerVigilancia() {
            if (vigilancia) { clearInterval(vigilancia); vigilancia = null; }
        }

        function consultarOcr() {
            if (!paso.isConnected) { detenerVigilancia(); return; }
            var token = previoToken;
            var direccion = token ? '/recurso/ocrPrevio/' + token : '/recurso/estadoOcr?ids=' + ocrId;
            fetch(direccion, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
                .then(function (r) { return r.ok ? r.json() : {}; })
                .then(function (respuesta) {
                    if (token !== previoToken) { return; }
                    var datos = token ? respuesta : respuesta[ocrId];
                    estado.ocr.estado = datos ? datos.estado : '';
                    estado.ocr.porcentaje = datos && datos.porcentaje != null ? datos.porcentaje : null;
                    estado.ocr.segundos = datos && datos.segundos != null ? datos.segundos : null;
                    // El contador solo baja: se corrige con el servidor si da menos tiempo, o si ya llegó a cero y todavía falta
                    var nuevoFin = estado.ocr.segundos != null ? Date.now() + estado.ocr.segundos * 1000 : null;
                    if (nuevoFin == null) {
                        estado.ocr.fin = null;
                    } else if (estado.ocr.fin == null || segundosQueFaltan() === 0 || nuevoFin < estado.ocr.fin) {
                        estado.ocr.fin = nuevoFin;
                    }
                    estado.ocr.fase = datos && datos.fase ? datos.fase : '';
                    if (ocrEnCurso()) { refrescarProgreso(); return; }
                    detenerVigilancia();
                    // Terminó el reconocimiento previo: se abre el PDF con texto sin perder las entradas ya cargadas
                    if (token) {
                        if (estado.ocr.estado === 'Listo') {
                            abrirDesdeUrl('/recurso/ocrPrevio/' + token + '/archivo?t=' + Date.now()).then(function (doc) {
                                if (token !== previoToken) { return; }
                                estado.doc = doc;
                                estado.sinTexto = false;
                                estado.esqueleto = '';
                                dibujar();
                            }).catch(function () { estado.esqueleto = ''; dibujar(); });
                        } else {
                            // Falló: se guarda el PDF tal cual
                            previoToken = '';
                            campoToken.value = '';
                            estado.ocr.estado = 'Error';
                            estado.esqueleto = '';
                            dibujar();
                        }
                        return;
                    }
                    // Terminó: se vuelve a leer el PDF, que ahora tiene texto
                    if (estado.ocr.estado === 'Listo' && urlArchivoActual && estado.origen === 'ninguno') {
                        var url = urlArchivoActual + (urlArchivoActual.indexOf('?') >= 0 ? '&' : '?') + 'ocr=' + Date.now();
                        analizar(function () { return abrirDesdeUrl(url); });
                    } else {
                        estado.esqueleto = '';
                        dibujar();
                    }
                })
                .catch(function () { /* se vuelve a intentar en el próximo ciclo */ });
        }

        function vigilarOcr() {
            var activo = ocrEnCurso() && estado.modo === 'listo';
            if (!activo) { detenerVigilancia(); return; }
            if (!vigilancia) {
                // Cada segundo baja el contador y cada dos se le pregunta al servidor
                var ciclos = 0;
                vigilancia = setInterval(function () {
                    ciclos++;
                    if (ciclos % 2 === 0) { consultarOcr(); } else { refrescarProgreso(); }
                }, 1000);
                consultarOcr();
            }
        }

        function hayPdf() {
            var f = archivo && archivo.files && archivo.files[0];
            return !!(esPdf(f) || urlArchivoActual);
        }

        function armarEsqueleto() {
            if (estado.modo === 'analizando') {
                contenido.innerHTML = aviso();
                estado.esqueleto = 'analizando';
                return;
            }
            var visor = estado.doc
                ? '<div class="indice-visor"><div class="indice-lienzo"><div class="indice-hoja" data-indice-hoja></div></div>'
                    + '<div class="indice-barra-pagina">'
                    + '<button type="button" class="indice-nav" data-indice-anterior aria-label="Página anterior"><i class="fa-solid fa-chevron-left" aria-hidden="true"></i></button>'
                    + '<span class="indice-paginas"><input type="number" class="indice-pagina-actual" min="1" max="' + estado.doc.numPages + '" value="' + estado.pagina + '" data-indice-pagina aria-label="Página"> / ' + estado.doc.numPages + '</span>'
                    + '<button type="button" class="indice-nav" data-indice-siguiente aria-label="Página siguiente"><i class="fa-solid fa-chevron-right" aria-hidden="true"></i></button>'
                    + '<span class="indice-zoom">'
                    + '<button type="button" class="indice-nav" data-indice-zoom="-1" aria-label="Alejar"><i class="fa-solid fa-minus" aria-hidden="true"></i></button>'
                    + '<span class="indice-zoom-valor" data-indice-zoom-valor>' + Math.round(estado.zoom * 100) + '%</span>'
                    + '<button type="button" class="indice-nav" data-indice-zoom="1" aria-label="Acercar"><i class="fa-solid fa-plus" aria-hidden="true"></i></button>'
                    + '</span></div></div><p class="indice-consejo">Tocá un texto de la página para agregarlo al índice.</p>'
                : '<div class="indice-visor indice-visor-vacio"><span>Elegí el PDF en el paso anterior para ver sus páginas.</span></div>';
            // El campo "ocr" no se ve: es donde el formulario muestra el error de guardar con el reconocimiento en curso, debajo del aviso
            contenido.innerHTML = aviso()
                + '<div class="pn-campo indice-campo-ocr"><input type="text" name="ocr" class="indice-control-ocr" tabindex="-1" aria-hidden="true" autocomplete="off"></div>'
                + '<div class="indice-grilla"><div>' + visor + '</div>'
                + '<div class="indice-panel"><div class="indice-cabecera"><b>Índice</b><span data-indice-cuenta></span></div>'
                + '<div class="indice-lista" data-indice-lista></div>'
                + (estado.doc || hayPdf() ? '<div class="indice-botones">'
                    + (estado.doc ? '<button type="button" class="pn-btn pn-btn-contorno pn-prog-boton" data-indice-agregar><i class="fa-solid fa-plus" aria-hidden="true"></i> Agregar entrada</button>' : '')
                    + (hayPdf() ? '<button type="button" class="pn-btn pn-btn-contorno pn-prog-boton" data-indice-detectar>Volver a leer los marcadores</button>' : '')
                    + '</div>' : '')
                + '</div></div>';
            estado.esqueleto = estado.modo + '|' + estado.origen + '|' + (estado.doc ? 'doc' : 'sin');
            // Si el paso estaba oculto cuando se dibujó (por ejemplo al elegir otro archivo en el paso anterior), la página se vuelve a ajustar al mostrarse
            if (observadorLienzo) { observadorLienzo.disconnect(); observadorLienzo = null; }
            var cajaLienzo = contenido.querySelector('.indice-lienzo');
            if (cajaLienzo && window.ResizeObserver) {
                var medida = cajaLienzo.offsetWidth + 'x' + cajaLienzo.offsetHeight;
                observadorLienzo = new ResizeObserver(function () {
                    var actual = cajaLienzo.offsetWidth + 'x' + cajaLienzo.offsetHeight;
                    if (actual !== medida) { medida = actual; pintarPagina(); }
                });
                observadorLienzo.observe(cajaLienzo);
            }
            pintarPagina();
        }

        function dibujar() {
            var clave = estado.modo + '|' + estado.origen + '|' + (estado.doc ? 'doc' : 'sin');
            if (estado.modo === 'analizando' || clave !== estado.esqueleto) { armarEsqueleto(); }
            if (estado.modo !== 'analizando') { pintarLista(); } else { sincronizar(); }
            vigilarOcr();
        }

        // --------------------------------------------------------------
        // lectura del documento
        // --------------------------------------------------------------

        /** Lee el documento (sus bytes o su dirección), importa sus marcadores y deja lista la vista previa. */
        function analizar(abrir, nuevo) {
            var turno = ++estado.turno;
            estado.modo = 'analizando';
            estado.editando = -1;
            estado.elegida = -1;
            mostrarPaso(true);
            dibujar();
            abrir().then(function (doc) {
                if (turno !== estado.turno) { return null; }
                estado.doc = doc;
                estado.pagina = 1;
                return detectar(doc).then(function (r) {
                    // Sin marcadores se mira si el PDF es un escaneo, para avisarlo
                    return r.origen === 'ninguno' ? esEscaneo(doc).then(function (escaneo) { estado.sinTexto = escaneo; return r; }) : (estado.sinTexto = false, r);
                });
            }).then(function (r) {
                if (!r || turno !== estado.turno) { return; }
                estado.modo = 'listo';
                estado.entradas = r.entradas;
                estado.origen = r.origen;
                fijarBase();
                dibujar();
                if (nuevo && estado.sinTexto && r.origen === 'ninguno') { iniciarPrevio(nuevo); }
            }).catch(function () {
                if (turno !== estado.turno) { return; }
                estado.modo = 'error';
                estado.entradas = [];
                estado.doc = null;
                dibujar();
            });
        }

        /** Manda el PDF escaneado al servidor para reconocer su texto desde ya, sin esperar a que se guarde el recurso. */
        function iniciarPrevio(f) {
            var datos = new FormData();
            datos.append('archivo', f);
            fetch('/recurso/ocrPrevio', { method: 'POST', body: datos, credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
                .then(function (r) { return r.ok ? r.json() : {}; })
                .then(function (respuesta) {
                    var actual = archivo && archivo.files && archivo.files[0];
                    if (!respuesta.token || actual !== f) { return; }
                    previoToken = respuesta.token;
                    campoToken.value = previoToken;
                    estado.ocr = { estado: 'Pendiente', porcentaje: 0, segundos: null };
                    estado.esqueleto = '';
                    dibujar();
                })
                .catch(function () { /* sin reconocimiento, el PDF se guarda tal cual */ });
        }

        function esPdf(f) {
            return f && (f.type === 'application/pdf' || /\.pdf$/i.test(f.name || ''));
        }

        /** Indica si el tipo de recurso elegido lleva índice. En la modificación no hay tipo para elegir: lleva el del recurso. */
        function tipoConIndice() {
            var tipo = formulario.elements['tipo'];
            return !tipo || TIPOS_CON_INDICE.indexOf(tipo.value) >= 0;
        }

        function analizarArchivoElegido() {
            var f = archivo && archivo.files && archivo.files[0];
            if (!esPdf(f)) { previoToken = ''; campoToken.value = ''; }
            if (!tipoConIndice()) {
                estado.turno++;
                mostrarPaso(false);
            } else if (esPdf(f)) {
                // Un archivo nuevo parte de cero: se descarta el reconocimiento del anterior
                previoToken = '';
                campoToken.value = '';
                ocrId = '';
                estado.ocr = { estado: '', porcentaje: null, segundos: null };
                analizar(function () { return f.arrayBuffer().then(abrirDesdeBytes); }, f);
            } else if (f) {
                estado.turno++;
                mostrarPaso(false);
            } else if (urlArchivoActual) {
                // Se sacó el archivo nuevo: vuelve el del recurso
                cargarGuardado();
            } else {
                // Con el tipo elegido el paso ya se muestra, aunque todavía no haya PDF del que leer los marcadores
                estado.turno++;
                estado.modo = 'sinarchivo';
                estado.entradas = [];
                estado.origen = '';
                estado.base = '';
                estado.doc = null;
                estado.elegida = -1;
                mostrarPaso(true);
                dibujar();
            }
        }

        /** Al modificar: el índice guardado. Si no tiene, los marcadores del archivo actual. */
        function cargarGuardado() {
            if (!urlGuardado && !urlArchivoActual) { mostrarPaso(false); return; }
            var turno = ++estado.turno;
            var pedido = urlGuardado
                ? fetch(urlGuardado, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } }).then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; })
                : Promise.resolve([]);
            pedido.then(function (guardado) {
                if (turno !== estado.turno) { return; }
                if (guardado && guardado.length) {
                    estado.modo = 'listo';
                    estado.entradas = guardado;
                    estado.origen = 'guardado';
                    estado.doc = null;
                    estado.elegida = -1;
                    fijarBase();
                    mostrarPaso(true);
                    dibujar();
                    // Lo guardado es el "antes" del resumen: solo lo que se corrija después cuenta como cambio
                    if (formulario.hojaDeRuta) { formulario.hojaDeRuta.fijarOriginal(); }
                    // La vista previa se abre aparte: el índice ya se puede corregir mientras tanto
                    if (urlArchivoActual) {
                        abrirDesdeUrl(urlArchivoActual).then(function (doc) {
                            if (turno !== estado.turno) { return; }
                            estado.doc = doc;
                            estado.pagina = 1;
                            dibujar();
                        }).catch(function () { /* sin vista previa, el índice se corrige igual */ });
                    }
                } else if (urlArchivoActual) {
                    analizar(function () { return abrirDesdeUrl(urlArchivoActual); });
                } else {
                    mostrarPaso(false);
                }
            });
        }

        function redetectar() {
            // Con el PDF ya abierto solo se vuelven a leer sus marcadores: la vista previa no se toca
            if (estado.doc) {
                var turno = estado.turno;
                detectar(estado.doc).then(function (r) {
                    if (turno !== estado.turno) { return; }
                    if (r.origen === 'marcadores') {
                        estado.entradas = r.entradas;
                        estado.origen = 'marcadores';
                        estado.elegida = -1;
                        estado.editando = -1;
                        fijarBase();
                    }
                    estado.esqueleto = '';
                    dibujar();
                });
                return;
            }
            var f = archivo && archivo.files && archivo.files[0];
            if (previoToken && estado.ocr.estado === 'Listo') { analizar(function () { return abrirDesdeUrl('/recurso/ocrPrevio/' + previoToken + '/archivo?t=' + Date.now()); }); }
            else if (esPdf(f)) { analizar(function () { return f.arrayBuffer().then(abrirDesdeBytes); }, previoToken ? null : f); }
            else if (urlArchivoActual) { analizar(function () { return abrirDesdeUrl(urlArchivoActual); }); }
        }

        // --------------------------------------------------------------
        // eventos del paso
        // --------------------------------------------------------------

        /** Agrega una entrada con el texto tocado en la página, en el lugar que le corresponde por su página. */
        function agregarDesdeLinea(texto) {
            var titulo = limpiar(texto);
            if (!titulo) { return; }
            var existente = estado.entradas.findIndex(function (e) { return e.pagina === estado.pagina && e.titulo === titulo; });
            if (existente >= 0) { estado.elegida = existente; pintarLista(); return; }
            if (estado.entradas.length >= MAXIMO_DE_ENTRADAS) { return; }
            // Con nada elegido la entrada se registra al final, y no queda elegida para que la siguiente también vaya al final
            var lugar = estado.entradas.length;
            estado.entradas.push({ titulo: titulo, pagina: estado.pagina, nivel: 1 });
            estado.elegida = -1;
            pintarLista();
            var fila = contenido.querySelector('[data-indice-fila="' + lugar + '"]');
            if (fila && fila.scrollIntoView) { fila.scrollIntoView({ block: 'nearest' }); }
        }

        /** Agrega una entrada vacía en la página que se está viendo (para las páginas sin texto que tocar) y la deja en edición. */
        function agregarVacia() {
            if (estado.entradas.length >= MAXIMO_DE_ENTRADAS) { return; }
            var lugar = estado.entradas.length;
            estado.entradas.push({ titulo: 'Nueva entrada', pagina: estado.pagina, nivel: 1 });
            empezarEdicion(lugar);
            var fila = contenido.querySelector('[data-indice-fila="' + lugar + '"]');
            if (fila && fila.scrollIntoView) { fila.scrollIntoView({ block: 'nearest' }); }
        }

        function empezarEdicion(i) {
            estado.editando = i;
            estado.elegida = i;
            pintarLista();
        }

        /** Deja la lista sin ninguna entrada elegida. */
        function soltarSeleccion() {
            if (estado.elegida < 0 || estado.editando >= 0) { return; }
            estado.elegida = -1;
            pintarLista();
        }

        // Tocar cualquier lugar fuera de las entradas suelta la que estaba elegida
        document.addEventListener('click', function (e) {
            if (!paso.isConnected || paso.contains(e.target) || !document.contains(e.target)) { return; }
            soltarSeleccion();
        });

        var ultimoClic = { fila: -1, momento: 0 };
        paso.addEventListener('click', function (e) {
            if (!e.target.closest('[data-indice-fila]')) { soltarSeleccion(); }
            var linea = e.target.closest('[data-indice-linea]');
            if (linea) { agregarDesdeLinea(linea.dataset.indiceLinea); return; }
            if (e.target.closest('[data-indice-anterior]')) { irAPagina(estado.pagina - 1); return; }
            if (e.target.closest('[data-indice-siguiente]')) { irAPagina(estado.pagina + 1); return; }
            if (e.target.closest('[data-indice-detectar]')) { redetectar(); return; }
            var zoom = e.target.closest('[data-indice-zoom]');
            if (zoom) {
                var paso_ = parseInt(zoom.dataset.indiceZoom, 10);
                var actual = Math.max(0, ZOOMS.indexOf(estado.zoom));
                estado.zoom = paso_ === 0 ? 1 : ZOOMS[Math.min(Math.max(actual + paso_, 0), ZOOMS.length - 1)];
                pintarPagina();
                return;
            }
            if (e.target.closest('[data-indice-agregar]')) { agregarVacia(); return; }
            var lapiz = e.target.closest('[data-indice-lapiz]');
            if (lapiz) { empezarEdicion(parseInt(lapiz.closest('[data-indice-fila]').dataset.indiceFila, 10)); return; }
            var nivel = e.target.closest('[data-indice-nivel]');
            if (nivel) {
                var fila = nivel.closest('[data-indice-fila]');
                var a = parseInt(fila.dataset.indiceFila, 10);
                estado.entradas[a].nivel = estado.entradas[a].nivel % NIVEL_MAXIMO + 1;
                pintarLista();
                return;
            }
            var quitar = e.target.closest('[data-indice-quitar]');
            if (quitar) {
                estado.entradas.splice(parseInt(quitar.closest('[data-indice-fila]').dataset.indiceFila, 10), 1);
                estado.elegida = -1;
                estado.editando = -1;
                pintarLista();
                return;
            }
            var filaElegida = e.target.closest('[data-indice-fila]');
            if (filaElegida && !e.target.closest('input')) {
                var i = parseInt(filaElegida.dataset.indiceFila, 10);
                var ahora = Date.now();
                var doble = ultimoClic.fila === i && ahora - ultimoClic.momento < 400;
                ultimoClic = { fila: i, momento: ahora };
                if (doble && !e.target.closest('.indice-acciones')) { ultimoClic = { fila: -1, momento: 0 }; empezarEdicion(i); return; }
                estado.elegida = i;
                pintarLista();
                if (estado.doc && estado.entradas[i]) { irAPagina(estado.entradas[i].pagina); }
            }
        });
        paso.addEventListener('change', function (e) {
            if (e.target.matches('[data-indice-pagina]')) { irAPagina(parseInt(e.target.value, 10) || 1); }
        });
        paso.addEventListener('focusout', function (e) {
            var edicion = e.target.closest('[data-indice-editar]');
            if (edicion && estado.editando >= 0) {
                var texto = limpiar(edicion.value);
                if (texto) { estado.entradas[estado.editando].titulo = texto; }
                estado.editando = -1;
                pintarLista();
            }
        });
        paso.addEventListener('keydown', function (e) {
            if (e.target.matches('[data-indice-editar]') && (e.key === 'Enter' || e.key === 'Escape')) {
                e.preventDefault();
                e.stopPropagation();
                e.target.blur();
                return;
            }
            if (e.target.matches('[data-indice-fila]') && (e.key === 'Enter' || e.key === ' ')) {
                e.preventDefault();
                e.target.click();
            }
        });

        if (archivo) { archivo.addEventListener('change', analizarArchivoElegido); }
        // En el alta, al cambiar de tipo de recurso el archivo se vacía: el paso se oculta
        var tipo = formulario.querySelector('select[name="tipo"]');
        if (tipo) { tipo.addEventListener('change', function () { setTimeout(analizarArchivoElegido, 0); }); }

        estado.ocr.estado = paso.dataset.ocrEstado || '';
        campo.disabled = true;
        paso.hidden = true;
        if (urlGuardado || urlArchivoActual) { cargarGuardado(); } else { analizarArchivoElegido(); }
    }

    window.PanelIndiceDocumento = { iniciar: iniciar };
    function iniciarTodos(raiz) {
        raiz.querySelectorAll('form[data-pasos]').forEach(function (f) { if (f.querySelector('[data-paso-indice]')) { iniciar(f); } });
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () { iniciarTodos(document); });
    } else {
        iniciarTodos(document);
    }
    // Las ventanas de modificar llegan del servidor cuando se las pide (core/modalModificar.js)
    document.addEventListener('ventana-cargada', function (e) { iniciarTodos(e.detail.ventana); });
})();
