/**
 * Índice del documento (paso «Índice» de los formularios de recursos): cuando el recurso es un PDF, el navegador arma solo el índice y
 * el docente puede corregirlo antes de guardar. No es obligatorio revisarlo: si no abre el paso, se guarda como se detectó.
 *
 *  - De dónde sale: primero los marcadores del propio PDF (los que Word y Google Docs generan a partir de los títulos). Si no tiene, se
 *    infieren de los títulos del texto: líneas con una letra más grande que el cuerpo, con numeración («1.», «1.1», «Ejercicio 1») o con
 *    una palabra de título («Introducción», «Bibliografía»), sin los textos que se repiten en muchas páginas (encabezados y pies).
 *  - Un PDF escaneado (sin texto) no tiene títulos que leer: se avisa y el índice se arma a mano.
 *  - Al modificar un recurso se muestra el índice guardado; si no tiene, se detecta sobre el archivo actual. Al elegir un archivo
 *    nuevo, se detecta de nuevo.
 *  - Lo que se envía con el formulario es el campo oculto «indiceJson»: [{ titulo, pagina, nivel }]. Sin PDF, el paso se oculta y no se envía.
 *
 * Usa PDF.js (se carga la primera vez que hace falta). El paso aparece y desaparece con data-paso-oculto: ver core/validacion.js.
 */
(function () {
    'use strict';

    var PDFJS = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/';
    var MAXIMO_DE_PAGINAS = 400;      // páginas que se leen para inferir los títulos
    var MAXIMO_DE_ENTRADAS = 300;
    var MAXIMO_DEL_TITULO = 150;

    // ------------------------------------------------------------------ PDF.js
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

    // ------------------------------------------------------------------ detección
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
                var fila = pagina ? [{ titulo: limpiar(item.title), pagina: pagina, nivel: Math.min(nivel, 3) }] : [];
                return aplanar(doc, item.items || [], nivel + 1).then(function (hijos) { return fila.concat(hijos); });
            });
        })).then(function (listas) { return [].concat.apply([], listas); });
    }

    function limpiar(texto) {
        var t = String(texto || '').replace(/\s+/g, ' ').trim();
        return t.length > MAXIMO_DEL_TITULO ? t.substring(0, MAXIMO_DEL_TITULO - 1).trim() + '…' : t;
    }

    /** Las líneas de texto de una página, con su tamaño de letra (agrupa los fragmentos que están a la misma altura). */
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
                lineas.push({ texto: it.texto, y: it.y, finX: it.x + it.ancho, tam: it.tam });
            }
        });
        return lineas;
    }

    var NUMERADO = /^(\d{1,2}(?:\.\d{1,2}){0,3})[.)]?\s+\S/;
    // Un título numerado de una línea con la letra del cuerpo: «1 Introducción», «2.1 Métodos» (no «1.1.2 Texto…» ni «60 funciones…»)
    var NUMERADO_DE_TITULO = /^\d{1,2}(?:\.\d{1,2})?[.)]?\s+\p{Lu}/u;
    var ROMANO = /^[IVXL]{1,6}\.\s+\S/;
    var PALABRA_DE_TITULO = /^(cap[ií]tulo|unidad|parte|secci[oó]n|anexo|ap[eé]ndice|ejercicio|introducci[oó]n|conclusi[oó]n(es)?|bibliograf[ií]a|referencias|resumen|abstract|introduction|conclusions?|references|appendix|chapter)\b/i;

    /** Infiere los títulos de un PDF sin marcadores. Devuelve { entradas, sinTexto }. */
    function inferir(doc) {
        var paginas = Math.min(doc.numPages, MAXIMO_DE_PAGINAS);
        var lineas = [];
        var caracteres = 0;
        var secuencia = Promise.resolve();
        for (var n = 1; n <= paginas; n++) {
            (function (numero) {
                secuencia = secuencia.then(function () {
                    return doc.getPage(numero).then(function (p) { return p.getTextContent(); }).then(function (contenido) {
                        lineasDePagina(contenido).forEach(function (l) {
                            l.pagina = numero;
                            caracteres += l.texto.length;
                            lineas.push(l);
                        });
                    }).catch(function () { /* una página que no se puede leer se salta */ });
                });
            })(n);
        }
        return secuencia.then(function () {
            if (caracteres < 20 * paginas) { return { entradas: [], sinTexto: true }; }

            // El cuerpo del texto: el tamaño de letra con más caracteres
            var pesos = {};
            lineas.forEach(function (l) { var k = Math.round(l.tam * 2) / 2; pesos[k] = (pesos[k] || 0) + l.texto.length; });
            var cuerpo = Object.keys(pesos).reduce(function (m, k) { return pesos[k] > (pesos[m] || 0) ? k : m; }, '0');
            cuerpo = parseFloat(cuerpo) || 10;

            // Los textos que se repiten en muchas páginas son encabezados y pies, no títulos
            var apariciones = {};
            lineas.forEach(function (l) {
                var clave = l.texto.trim().toLowerCase().replace(/\d+/g, '#');
                apariciones[clave] = apariciones[clave] || {};
                apariciones[clave][l.pagina] = true;
            });
            var umbral = Math.max(3, Math.ceil(paginas * 0.25));

            var candidatas = [];
            lineas.forEach(function (l) {
                var t = l.texto.trim().replace(/\s+/g, ' ');
                if (t.length < 3 || t.length > 110 || /^[\d\s.,\-–—]+$/.test(t)) { return; }
                var clave = t.toLowerCase().replace(/\d+/g, '#');
                if (Object.keys(apariciones[clave] || {}).length >= umbral) { return; }
                var coincide = t.match(NUMERADO);
                var numerado = !!coincide;
                var romano = ROMANO.test(t);
                if (/[.,;:]$/.test(t)) { return; }
                if (/^(fig(ura|\.)?|tabla|table|cuadro)\s*\d/i.test(t)) { return; }
                var mayor = l.tam >= cuerpo * 1.18;
                var clave2 = PALABRA_DE_TITULO.test(t) && t.length <= 60;
                var deTitulo = NUMERADO_DE_TITULO.test(t) || romano;
                if (mayor || (deTitulo && l.tam >= cuerpo * 0.95 && t.length <= 70) || (clave2 && l.tam >= cuerpo * 0.95)) {
                    candidatas.push({
                        titulo: limpiar(t), pagina: l.pagina, tam: l.tam, mayor: mayor, clave: clave2 && !numerado,
                        puntos: numerado ? (coincide[1].match(/\./g) || []).length : -1
                    });
                }
            });

            // Niveles: los tamaños de letra distintos, del más grande al más chico; con numeración manda la profundidad del número
            var tamanios = candidatas.filter(function (c) { return c.mayor; }).map(function (c) { return Math.round(c.tam * 2) / 2; })
                .filter(function (v, i, a) { return a.indexOf(v) === i; }).sort(function (a, b) { return b - a; });
            var maximo = tamanios.length ? tamanios[0] : 0;
            var entradas = candidatas.map(function (c) {
                var nivel = c.puntos >= 0 ? c.puntos + 1 : (c.mayor ? Math.min(tamanios.indexOf(Math.round(c.tam * 2) / 2) + 1, 3) : (c.clave ? 1 : 2));
                return { titulo: c.titulo, pagina: c.pagina, nivel: Math.min(Math.max(nivel, 1), 3), titular: c.mayor && Math.round(c.tam * 2) / 2 === maximo && c.puntos < 0 };
            });
            // El título del propio documento (la letra más grande, en la primera página, aunque ocupe varias líneas) no es una entrada del índice
            var delDocumento = entradas.filter(function (e) { return e.titular && e.pagina === 1; });
            if (delDocumento.length && entradas.length - delDocumento.length > 2) {
                entradas = entradas.filter(function (e) { return !(e.titular && e.pagina === 1); });
            }
            // Sin repetidos seguidos y con un tope
            var vistas = [];
            entradas.forEach(function (e) {
                var ultima = vistas[vistas.length - 1];
                if (!ultima || ultima.titulo !== e.titulo || ultima.pagina !== e.pagina) { vistas.push({ titulo: e.titulo, pagina: e.pagina, nivel: e.nivel }); }
            });
            return { entradas: vistas.slice(0, MAXIMO_DE_ENTRADAS), sinTexto: false };
        });
    }

    /** Detecta el índice de un documento: sus marcadores o, si no tiene, los títulos inferidos. */
    function detectar(doc) {
        return doc.getOutline().then(function (arbol) {
            if (arbol && arbol.length) { return aplanar(doc, arbol, 1); }
            return null;
        }).catch(function () { return null; }).then(function (marcadores) {
            if (marcadores && marcadores.length) { return { entradas: marcadores.slice(0, MAXIMO_DE_ENTRADAS), origen: 'marcadores', sinTexto: false }; }
            return inferir(doc).then(function (r) { return { entradas: r.entradas, origen: 'inferido', sinTexto: r.sinTexto }; });
        });
    }

    function abrirDesdeBytes(bytes) {
        return cargarPdfjs().then(function (pdfjs) { return pdfjs.getDocument({ data: bytes }).promise; });
    }

    function abrirDesdeUrl(url) {
        return cargarPdfjs().then(function (pdfjs) { return pdfjs.getDocument({ url: url, withCredentials: true }).promise; });
    }

    // ------------------------------------------------------------------ el paso del formulario
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
        var ocrEstado = paso.dataset.ocrEstado || '';                // el del archivo actual: Pendiente, Procesando, Listo o Error
        var ocrDisponible = paso.dataset.ocrDisponible === 'true';   // si el servidor puede hacer OCR
        var resumen = paso.querySelector('[data-indice-resumen]');

        var estado = { modo: 'ninguno', entradas: [], origen: '', sinTexto: false, editando: -1, turno: 0, base: '' };

        function mostrarPaso(visible) {
            paso.hidden = !visible;
            campo.disabled = !visible;
            if (formulario.pasos && formulario.pasos.refrescar) { formulario.pasos.refrescar(); }
        }

        function sincronizar() {
            var actual = JSON.stringify(estado.entradas.map(function (e) { return { titulo: e.titulo, pagina: e.pagina, nivel: e.nivel }; }));
            campo.value = actual;
            // La línea del resumen: cuántas entradas tiene, de dónde salieron y si se corrigieron
            if (resumen) {
                var n = estado.entradas.length;
                var texto = n + (n === 1 ? ' entrada' : ' entradas');
                if (estado.origen === 'marcadores') { texto += ' · marcadores del PDF'; }
                if (estado.origen === 'inferido') { texto += ' · detectadas automáticamente'; }
                if (estado.base && actual !== estado.base) { texto += ' · corregidas'; }
                resumen.textContent = estado.modo === 'listo' || estado.modo === 'ocr' || estado.modo === 'error' ? (n ? texto : 'sin entradas') : '';
            }
        }

        function fijarBase() {
            estado.base = JSON.stringify(estado.entradas.map(function (e) { return { titulo: e.titulo, pagina: e.pagina, nivel: e.nivel }; }));
        }

        function lista() {
            if (!estado.entradas.length) {
                return '<div class="indice-lista"><div class="indice-vacio">El índice está vacío. Agregá una entrada o volvé a detectar.</div></div>';
            }
            return '<div class="indice-lista">' + estado.entradas.map(function (e, i) {
                var titulo = estado.editando === i
                    ? '<input type="text" class="indice-editar" maxlength="' + MAXIMO_DEL_TITULO + '" value="' + esc(e.titulo) + '" data-indice-editar>'
                    : '<span class="indice-titulo" data-indice-titulo="' + i + '" title="Clic para editar">' + esc(e.titulo) + '</span>';
                return '<div class="indice-fila indice-n' + e.nivel + '">' + titulo
                    + '<input type="number" class="indice-pagina" min="1" value="' + e.pagina + '" data-indice-pagina="' + i + '" aria-label="Página">'
                    + '<button type="button" class="indice-icono" data-indice-nivel="' + i + '" title="' + (e.nivel === 1 ? 'Pasar a subnivel' : 'Pasar a nivel principal')
                    + '"><i class="fa-solid ' + (e.nivel === 1 ? 'fa-indent' : 'fa-outdent') + '" aria-hidden="true"></i></button>'
                    + '<button type="button" class="indice-icono" data-indice-quitar="' + i + '" title="Quitar"><i class="fa-regular fa-trash-can" aria-hidden="true"></i></button></div>';
            }).join('') + '</div>';
        }

        function dibujar() {
            var html;
            if (estado.modo === 'analizando') {
                html = '<div class="pn-alerta pn-alerta-azul">Estamos leyendo el documento para armar el índice…</div>';
            } else if (estado.modo === 'ocr') {
                html = '<div class="pn-alerta pn-alerta-dorado">Este PDF es un escaneo: se está reconociendo su texto (OCR) para poder buscar palabras. '
                    + 'Cuando termine, volvé a abrir este recurso para generar el índice. Mientras tanto podés armarlo a mano.</div>' + herramientas() + lista();
            } else if (estado.modo === 'error') {
                html = '<div class="pn-alerta pn-alerta-dorado">No se pudo leer el documento para armar el índice. Podés agregar las entradas a mano.</div>' + herramientas() + lista();
            } else {
                var aviso;
                if (estado.sinTexto && ocrEstado === 'Error') {
                    aviso = '<div class="pn-alerta pn-alerta-dorado">Este PDF es un escaneo y no se pudo reconocer su texto (OCR). Podés armar el índice a mano.</div>';
                } else if (estado.sinTexto && ocrDisponible) {
                    aviso = '<div class="pn-alerta pn-alerta-dorado">Este PDF parece un escaneo. Al guardar, se le va a reconocer el texto (OCR) en segundo plano para poder '
                        + 'buscar palabras. Después volvé a abrir el recurso para generar el índice; mientras tanto podés armarlo a mano.</div>';
                } else if (estado.sinTexto) {
                    aviso = '<div class="pn-alerta pn-alerta-dorado">Este PDF parece un escaneo: no tiene texto que se pueda leer. Podés armar el índice a mano.</div>';
                } else if (estado.origen === 'marcadores') {
                    aviso = '<div class="pn-alerta pn-alerta-azul">Se importaron ' + estado.entradas.length + ' entradas de los marcadores del propio PDF. Podés corregirlas si hace falta.</div>';
                } else if (estado.origen === 'guardado') {
                    aviso = '<div class="pn-alerta pn-alerta-azul">Este es el índice guardado del documento. Podés corregirlo si hace falta.</div>';
                } else {
                    aviso = '<div class="pn-alerta pn-alerta-dorado">El índice se armó automáticamente a partir de los títulos del documento. Revisalo si hace falta.</div>';
                }
                html = aviso + herramientas() + lista();
            }
            contenido.innerHTML = html;
            var edicion = contenido.querySelector('[data-indice-editar]');
            if (edicion) { edicion.focus(); edicion.setSelectionRange(edicion.value.length, edicion.value.length); }
            sincronizar();
        }

        function herramientas() {
            return '<div class="indice-barra"><button type="button" class="pn-btn pn-btn-contorno pn-btn-chico" data-indice-agregar>+ Agregar entrada</button>'
                + '<button type="button" class="pn-btn pn-btn-contorno pn-btn-chico" data-indice-detectar>Volver a detectar</button></div>';
        }

        /** Lee el documento (sus bytes o su dirección) y arma el índice. */
        function analizar(abrir) {
            var turno = ++estado.turno;
            estado.modo = 'analizando';
            estado.editando = -1;
            mostrarPaso(true);
            dibujar();
            abrir().then(detectar).then(function (r) {
                if (turno !== estado.turno) { return; }
                estado.modo = 'listo';
                estado.entradas = r.entradas;
                estado.origen = r.origen;
                estado.sinTexto = r.sinTexto;
                fijarBase();
                dibujar();
            }).catch(function () {
                if (turno !== estado.turno) { return; }
                estado.modo = 'error';
                estado.entradas = [];
                estado.sinTexto = false;
                dibujar();
            });
        }

        function esPdf(f) {
            return f && (f.type === 'application/pdf' || /\.pdf$/i.test(f.name || ''));
        }

        function analizarArchivoElegido() {
            var f = archivo && archivo.files && archivo.files[0];
            if (esPdf(f)) {
                analizar(function () { return f.arrayBuffer().then(abrirDesdeBytes); });
            } else if (f) {
                estado.turno++;
                mostrarPaso(false);
            } else if (urlArchivoActual) {
                // Se sacó el archivo nuevo: vuelve el del recurso
                cargarGuardado();
            } else {
                estado.turno++;
                mostrarPaso(false);
            }
        }

        /** Al modificar: el índice guardado; si no tiene, el que se detecta en el archivo actual. */
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
                    estado.sinTexto = false;
                    fijarBase();
                    mostrarPaso(true);
                    dibujar();
                    // Lo guardado es el «antes» del resumen: solo lo que se corrija después cuenta como cambio
                    if (formulario.hojaDeRuta) { formulario.hojaDeRuta.fijarOriginal(); }
                } else if (ocrEstado === 'Pendiente' || ocrEstado === 'Procesando') {
                    // El archivo se está procesando: todavía no hay texto del que sacar el índice
                    estado.modo = 'ocr';
                    estado.entradas = [];
                    estado.origen = '';
                    mostrarPaso(true);
                    dibujar();
                } else if (urlArchivoActual) {
                    analizar(function () { return abrirDesdeUrl(urlArchivoActual); });
                } else {
                    mostrarPaso(false);
                }
            });
        }

        // Eventos del paso
        paso.addEventListener('click', function (e) {
            var titulo = e.target.closest('[data-indice-titulo]');
            if (titulo) { estado.editando = parseInt(titulo.dataset.indiceTitulo, 10); dibujar(); return; }
            var nivel = e.target.closest('[data-indice-nivel]');
            if (nivel) { var i = parseInt(nivel.dataset.indiceNivel, 10); estado.entradas[i].nivel = estado.entradas[i].nivel === 1 ? 2 : 1; dibujar(); return; }
            var quitar = e.target.closest('[data-indice-quitar]');
            if (quitar) { estado.entradas.splice(parseInt(quitar.dataset.indiceQuitar, 10), 1); estado.editando = -1; dibujar(); return; }
            if (e.target.closest('[data-indice-agregar]')) {
                var ultima = estado.entradas[estado.entradas.length - 1];
                estado.entradas.push({ titulo: 'Nueva entrada', pagina: ultima ? ultima.pagina : 1, nivel: 1 });
                estado.editando = estado.entradas.length - 1;
                dibujar();
                return;
            }
            if (e.target.closest('[data-indice-detectar]')) { redetectar(); }
        });
        paso.addEventListener('change', function (e) {
            var pagina = e.target.closest('[data-indice-pagina]');
            if (pagina) {
                estado.entradas[parseInt(pagina.dataset.indicePagina, 10)].pagina = Math.max(1, parseInt(pagina.value, 10) || 1);
                sincronizar();
            }
        });
        paso.addEventListener('focusout', function (e) {
            var edicion = e.target.closest('[data-indice-editar]');
            if (edicion && estado.editando >= 0) {
                var texto = limpiar(edicion.value);
                if (texto) { estado.entradas[estado.editando].titulo = texto; }
                estado.editando = -1;
                dibujar();
            }
        });
        paso.addEventListener('keydown', function (e) {
            if (e.target.matches('[data-indice-editar]') && (e.key === 'Enter' || e.key === 'Escape')) {
                e.preventDefault();
                e.stopPropagation();
                e.target.blur();
            }
        });

        function redetectar() {
            var f = archivo && archivo.files && archivo.files[0];
            if (esPdf(f)) { analizar(function () { return f.arrayBuffer().then(abrirDesdeBytes); }); }
            else if (urlArchivoActual) { analizar(function () { return abrirDesdeUrl(urlArchivoActual); }); }
        }

        if (archivo) { archivo.addEventListener('change', analizarArchivoElegido); }
        // En el alta, al cambiar de tipo de recurso el archivo se vacía: el paso se oculta
        var tipo = formulario.querySelector('select[name="tipo"]');
        if (tipo) { tipo.addEventListener('change', function () { setTimeout(analizarArchivoElegido, 0); }); }

        campo.disabled = true;
        paso.hidden = true;
        if (urlGuardado || urlArchivoActual) { cargarGuardado(); }
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
