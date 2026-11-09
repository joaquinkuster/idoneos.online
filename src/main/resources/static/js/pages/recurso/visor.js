/**
 * Visor de PDF integrado (MOD-F-02): muestra el documento de a una página, con botones de página anterior y
 * siguiente, número de página, zoom, pantalla completa, navegación con el teclado, un panel lateral con las miniaturas y el
 * índice del documento, y un buscador de palabras.
 *
 *  - Zoom: botones de acercar y alejar (de 50% a 300% del ajuste al ancho), clic en el porcentaje para volver al 100%,
 *    Ctrl + rueda del mouse y las teclas + , - y 0. Con la página más grande que el recuadro se desplaza con las
 *    barras o arrastrando con el mouse.
 *  - Teclado: flechas izquierda y derecha para cambiar de página; Ctrl + F lleva al buscador.
 *  - Miniaturas: una por página, que se dibujan al aparecer en el panel; un clic lleva a la página.
 *  - Índice: el guardado del documento (el que armó el sistema y revisó el docente) y, si no tiene, el propio del PDF (sus marcadores),
 *    anidado por niveles; un clic lleva a la página. Si no hay ninguno, se avisa.
 *  - Anotaciones (solo el alumno): lápiz, marcador y goma, con cinco tamaños y diez colores. Los trazos se dibujan sobre la página y se guardan
 *    solos, por página, en la plataforma (son solo del alumno y no tocan el archivo); al volver a abrir el documento siguen ahí.
 *  - Buscador: busca en el texto de todas las páginas (sin distinguir mayúsculas ni tildes), cuenta las coincidencias, las resalta
 *    en la página y salta de una a otra (Enter, Mayús + Enter o las flechas). Sobre la página hay una capa de texto transparente que
 *    solo sirve para ubicar los resaltados: el texto no se puede seleccionar ni copiar (ver proteccion.js).
 *
 * Usa PDF.js (cargado desde un CDN antes de este script). El archivo se pide a la plataforma con la sesión del usuario
 * (/recurso/archivo/{id}), que controla el acceso.
 */
(function () {
    'use strict';

    var visor = document.querySelector('[data-visor-pdf]');
    if (!visor) { return; }

    var estado = visor.querySelector('[data-visor-estado]');
    var desplazable = visor.querySelector('.mv-desplazable');
    var hoja = visor.querySelector('.mv-hoja');
    var lienzo = visor.querySelector('canvas.mv-pagina');
    var contexto = lienzo.getContext('2d');
    var capaTexto = visor.querySelector('[data-visor-texto]');
    var entradaPagina = visor.querySelector('[data-visor-pagina]');
    var etiquetaTotal = visor.querySelector('[data-visor-total]');
    var botonAnterior = visor.querySelector('[data-visor-anterior]');
    var botonSiguiente = visor.querySelector('[data-visor-siguiente]');
    var botonPantalla = visor.querySelector('[data-visor-pantalla-completa]');
    var botonAlejar = visor.querySelector('[data-visor-alejar]');
    var botonAcercar = visor.querySelector('[data-visor-acercar]');
    var botonZoom = visor.querySelector('[data-visor-zoom]');
    var botonLateral = visor.querySelector('[data-visor-lateral-boton]');
    var lateral = visor.querySelector('[data-visor-lateral]');
    var listaMiniaturas = visor.querySelector('[data-visor-miniaturas]');
    var listaIndice = visor.querySelector('[data-visor-indice]');
    var entradaBuscar = visor.querySelector('[data-visor-buscar]');
    var contador = visor.querySelector('[data-visor-contador]');
    var botonResultadoAnterior = visor.querySelector('[data-visor-resultado-anterior]');
    var botonResultadoSiguiente = visor.querySelector('[data-visor-resultado-siguiente]');
    var urlResaltados = visor.dataset.urlResaltados || '';
    var capaTrazos = visor.querySelector('[data-visor-trazos]');
    var botonAnotar = visor.querySelector('[data-visor-anotar]');
    var panelHerramientas = visor.querySelector('[data-visor-herramientas]');

    var ZOOM_MINIMO = 0.5;
    var ZOOM_MAXIMO = 3;
    var PASO_ZOOM = 0.25;
    var MAXIMO_DE_PIXELES = 16000000; // límite del lienzo para no agotar la memoria con zoom alto y pantallas densas
    var ANCHO_MINIATURA = 124;        // ancho (en píxeles de pantalla) de cada miniatura
    var MINIMO_BUSQUEDA = 2;          // letras mínimas para buscar

    function mostrarEstado(texto) {
        estado.classList.remove('mv-estado-skeleton'); // el skeleton es solo mientras llega el documento
        estado.textContent = texto;
        estado.hidden = !texto;
        hoja.hidden = !!texto;
    }

    if (!window.pdfjsLib) {
        mostrarEstado('No se pudo cargar el visor. Intentá nuevamente más tarde.');
        return;
    }
    window.pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';

    var documento = null;
    var pagina = 1;
    var total = 0;
    var zoom = 1; // 1 = la página ajustada al ancho del visor
    var renderizando = false;
    var pendiente = null;
    var reajustesSeguidos = 0; // evita un bucle si el ancho disponible sigue cambiando
    var divsTexto = [];        // los fragmentos de texto de la página dibujada (para resaltar)
    var entradasIndice = [];   // el índice del PDF, en orden: { titulo, pagina, nivel }

    function escapar(texto) {
        return String(texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    function actualizarControles() {
        entradaPagina.value = pagina;
        botonAnterior.disabled = pagina <= 1;
        botonSiguiente.disabled = pagina >= total;
        botonZoom.textContent = Math.round(zoom * 100) + '%';
        botonAlejar.disabled = zoom <= ZOOM_MINIMO;
        botonAcercar.disabled = zoom >= ZOOM_MAXIMO;
        marcarListas();
    }

    // ------------------------------------------------------------------ página
    /** Dibuja una página ajustada al ancho del visor (y al alto, en pantalla completa), multiplicada por el zoom. */
    function dibujar(numero) {
        if (renderizando) { pendiente = numero; return; }
        renderizando = true;
        pagina = numero;

        // Se recuerda qué parte de la página estaba en el centro para mantenerla a la vista después de redibujar
        var centroX = desplazable.scrollWidth ? (desplazable.scrollLeft + desplazable.clientWidth / 2) / desplazable.scrollWidth : 0.5;
        var centroY = desplazable.scrollHeight ? (desplazable.scrollTop + desplazable.clientHeight / 2) / desplazable.scrollHeight : 0.5;

        var anchoUsado = desplazable.clientWidth;
        var paginaPdf = null;
        var vistaTexto = null;
        documento.getPage(numero).then(function (p) {
            paginaPdf = p;
            var tamanioBase = p.getViewport({ scale: 1 });
            var escala = anchoUsado / tamanioBase.width;
            if (document.fullscreenElement === visor) {
                escala = Math.min(escala, desplazable.clientHeight / tamanioBase.height);
            }
            escala *= zoom;
            var densidad = window.devicePixelRatio || 1;
            while (tamanioBase.width * tamanioBase.height * Math.pow(escala * densidad, 2) > MAXIMO_DE_PIXELES && densidad > 1) {
                densidad = Math.max(1, densidad - 0.5);
            }
            var vista = p.getViewport({ scale: escala * densidad });
            vistaTexto = p.getViewport({ scale: escala });
            lienzo.width = vista.width;
            lienzo.height = vista.height;
            lienzo.style.width = (vista.width / densidad) + 'px';
            lienzo.style.height = (vista.height / densidad) + 'px';
            hoja.style.width = vistaTexto.width + 'px';
            hoja.style.height = vistaTexto.height + 'px';
            if (capaTrazos) {
                capaTrazos.width = vista.width;
                capaTrazos.height = vista.height;
            }
            return p.render({ canvasContext: contexto, viewport: vista }).promise;
        }).then(function () {
            return dibujarTexto(paginaPdf, vistaTexto);
        }).then(function () {
            renderizando = false;
            mostrarEstado('');
            actualizarControles();
            dibujarTrazos();
            marcarPagina(false);
            desplazable.scrollLeft = centroX * desplazable.scrollWidth - desplazable.clientWidth / 2;
            desplazable.scrollTop = centroY * desplazable.scrollHeight - desplazable.clientHeight / 2;
            marcarPagina(true);
            if (pendiente !== null) {
                var siguiente = pendiente;
                pendiente = null;
                dibujar(siguiente);
            } else if (Math.abs(desplazable.clientWidth - anchoUsado) > 2 && reajustesSeguidos < 2) {
                // Apareció o desapareció una barra de desplazamiento: cambió el ancho disponible, se vuelve a ajustar
                reajustesSeguidos++;
                dibujar(pagina);
            } else {
                reajustesSeguidos = 0;
            }
        }).catch(function () {
            renderizando = false;
            mostrarEstado('No se pudo mostrar la página.');
        });
    }

    /** La capa de texto transparente sobre la página: sirve para ubicar los resaltados del buscador. Si falla, la página se ve igual. */
    function dibujarTexto(paginaPdf, vista) {
        capaTexto.textContent = '';
        divsTexto = [];
        if (!window.pdfjsLib.renderTextLayer) { return Promise.resolve(); }
        capaTexto.style.setProperty('--scale-factor', String(vista.scale)); // PDF.js 3.x calcula los tamaños del texto con esta variable
        capaTexto.style.width = vista.width + 'px';
        capaTexto.style.height = vista.height + 'px';
        return paginaPdf.getTextContent().then(function (contenido) {
            var tarea = window.pdfjsLib.renderTextLayer({
                textContentSource: contenido, container: capaTexto, viewport: vista, textDivs: divsTexto
            });
            return tarea.promise;
        }).catch(function () { divsTexto = []; });
    }

    function irA(numero) {
        if (!documento) { return; }
        dibujar(Math.min(Math.max(numero, 1), total));
    }

    function cambiarZoom(nuevo) {
        if (!documento) { return; }
        zoom = Math.min(Math.max(Math.round(nuevo * 100) / 100, ZOOM_MINIMO), ZOOM_MAXIMO);
        actualizarControles();
        dibujar(pagina);
    }

    // ------------------------------------------------------------------ miniaturas
    var observador = null;

    function pintarMiniatura(boton) {
        var numero = parseInt(boton.dataset.pagina, 10);
        documento.getPage(numero).then(function (p) {
            var base = p.getViewport({ scale: 1 });
            var densidad = window.devicePixelRatio || 1;
            var vista = p.getViewport({ scale: (ANCHO_MINIATURA / base.width) * densidad });
            var c = boton.querySelector('canvas');
            c.width = vista.width;
            c.height = vista.height;
            c.style.width = ANCHO_MINIATURA + 'px';
            c.style.height = (vista.height / densidad) + 'px';
            return p.render({ canvasContext: c.getContext('2d'), viewport: vista }).promise;
        }).catch(function () { /* una miniatura que falla se queda en blanco */ });
    }

    function crearMiniaturas() {
        documento.getPage(1).then(function (p) {
            var base = p.getViewport({ scale: 1 });
            var alto = Math.round(ANCHO_MINIATURA * base.height / base.width);
            var html = '';
            for (var n = 1; n <= total; n++) {
                html += '<button type="button" class="mv-mini" data-pagina="' + n + '" aria-label="Ir a la página ' + n + '">'
                    + '<canvas style="width:' + ANCHO_MINIATURA + 'px;height:' + alto + 'px"></canvas><span>' + n + '</span></button>';
            }
            listaMiniaturas.innerHTML = html;
            observador = 'IntersectionObserver' in window ? new IntersectionObserver(function (entradas) {
                entradas.forEach(function (e) {
                    if (e.isIntersecting) { observador.unobserve(e.target); pintarMiniatura(e.target); }
                });
            }, { root: listaMiniaturas, rootMargin: '240px' }) : null;
            Array.prototype.forEach.call(listaMiniaturas.querySelectorAll('.mv-mini'), function (boton) {
                if (observador) { observador.observe(boton); } else { pintarMiniatura(boton); }
            });
            marcarListas();
        });
    }

    // ------------------------------------------------------------------ índice
    function paginaDeDestino(destino) {
        if (!destino) { return Promise.resolve(null); }
        var resuelto = typeof destino === 'string' ? documento.getDestination(destino) : Promise.resolve(destino);
        return resuelto.then(function (arreglo) {
            if (!arreglo || !arreglo.length) { return null; }
            var referencia = arreglo[0];
            if (referencia && typeof referencia === 'object') {
                return documento.getPageIndex(referencia).then(function (i) { return i + 1; });
            }
            return Number.isInteger(referencia) ? referencia + 1 : null;
        }).catch(function () { return null; });
    }

    function aplanarIndice(items, nivel) {
        return Promise.all(items.map(function (item) {
            return paginaDeDestino(item.dest).then(function (numero) {
                var fila = [{ titulo: item.title || '', pagina: numero, nivel: Math.min(nivel, 3) }];
                return aplanarIndice(item.items || [], nivel + 1).then(function (hijos) { return fila.concat(hijos); });
            });
        })).then(function (listas) { return [].concat.apply([], listas); });
    }

    function pintarIndice() {
        listaIndice.innerHTML = entradasIndice.map(function (e, i) {
            return '<button type="button" class="mv-ind n' + e.nivel + '" data-indice="' + i + '"' + (e.pagina ? '' : ' disabled')
                + ' style="padding-left:' + (8 + e.nivel * 14) + 'px"><span>' + escapar(e.titulo) + '</span>'
                + (e.pagina ? '<small>' + e.pagina + '</small>' : '') + '</button>';
        }).join('');
        marcarListas();
    }

    /** El índice guardado del documento (el que revisó el docente) y, si no tiene, los marcadores del propio PDF. */
    function cargarIndice() {
        var url = visor.dataset.urlIndice;
        var guardado = url
            ? fetch(url, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
                .then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; })
            : Promise.resolve([]);
        guardado.then(function (lista) {
            if (lista && lista.length) {
                entradasIndice = lista.map(function (e) { return { titulo: e.titulo, pagina: e.pagina, nivel: Math.max(0, (e.nivel || 1) - 1) }; });
                pintarIndice();
                return;
            }
            return cargarMarcadores();
        });
    }

    function cargarMarcadores() {
        return documento.getOutline().then(function (arbol) {
            if (!arbol || !arbol.length) {
                listaIndice.innerHTML = '<p class="mv-vacio-lista">Este documento no tiene índice.</p>';
                return;
            }
            return aplanarIndice(arbol, 0).then(function (lista) { entradasIndice = lista; pintarIndice(); });
        }).catch(function () {
            listaIndice.innerHTML = '<p class="mv-vacio-lista">No se pudo leer el índice del documento.</p>';
        });
    }

    /** Deja a la vista cuál miniatura y cuál entrada del índice corresponden a la página actual. */
    function marcarListas() {
        Array.prototype.forEach.call(listaMiniaturas.querySelectorAll('.mv-mini'), function (boton) {
            var activa = parseInt(boton.dataset.pagina, 10) === pagina;
            boton.classList.toggle('activa', activa);
            if (activa && !lateral.hidden && !listaMiniaturas.hidden) {
                var caja = listaMiniaturas.getBoundingClientRect();
                var b = boton.getBoundingClientRect();
                if (b.top < caja.top || b.bottom > caja.bottom) { listaMiniaturas.scrollTop += b.top - caja.top - 12; }
            }
        });
        var vigente = -1;
        entradasIndice.forEach(function (e, i) { if (e.pagina && e.pagina <= pagina && (vigente < 0 || e.pagina >= entradasIndice[vigente].pagina)) { vigente = i; } });
        Array.prototype.forEach.call(listaIndice.querySelectorAll('.mv-ind'), function (boton) {
            boton.classList.toggle('activa', parseInt(boton.dataset.indice, 10) === vigente);
        });
    }

    visor.querySelector('[data-visor-pestanas]').addEventListener('click', function (e) {
        var boton = e.target.closest('[data-visor-pestana]');
        if (!boton) { return; }
        var indice = boton.dataset.visorPestana === 'indice';
        Array.prototype.forEach.call(visor.querySelectorAll('[data-visor-pestana]'), function (b) { b.classList.toggle('activa', b === boton); });
        listaMiniaturas.hidden = indice;
        listaIndice.hidden = !indice;
        marcarListas();
    });
    listaMiniaturas.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-pagina]');
        if (boton) { irA(parseInt(boton.dataset.pagina, 10)); }
    });
    listaIndice.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-indice]');
        if (boton) { irA(entradasIndice[parseInt(boton.dataset.indice, 10)].pagina); }
    });
    botonLateral.addEventListener('click', function () {
        lateral.hidden = !lateral.hidden;
        botonLateral.classList.toggle('activo', !lateral.hidden);
        botonLateral.setAttribute('aria-expanded', String(!lateral.hidden));
        if (documento) { dibujar(pagina); } // cambió el ancho disponible
    });

    // ------------------------------------------------------------------ buscador
    var textos = {};   // número de página -> los textos de sus fragmentos (se leen una sola vez)
    var busqueda = { consulta: '', resultados: [], actual: -1, turno: 0 };

    /** Letras sin tilde y en minúscula, una por letra (así los índices coinciden con los del texto original). */
    function normalizar(texto) {
        return texto.split('').map(function (c) { return c.normalize('NFD').charAt(0); }).join('').toLowerCase();
    }

    function posiciones(texto, buscada) {
        var normal = normalizar(texto);
        var lista = [];
        var i = normal.indexOf(buscada);
        while (i !== -1) { lista.push(i); i = normal.indexOf(buscada, i + buscada.length); }
        return lista;
    }

    function textoDePagina(numero) {
        if (textos[numero]) { return Promise.resolve(textos[numero]); }
        return documento.getPage(numero).then(function (p) { return p.getTextContent(); }).then(function (contenido) {
            textos[numero] = contenido.items.filter(function (item) { return typeof item.str === 'string'; })
                .map(function (item) { return item.str; });
            return textos[numero];
        }).catch(function () { textos[numero] = []; return textos[numero]; });
    }

    function actualizarContador() {
        var n = busqueda.resultados.length;
        var hay = normalizar(busqueda.consulta.trim()).length >= MINIMO_BUSQUEDA;
        contador.textContent = !hay ? '' : (n ? (busqueda.actual + 1) + ' de ' + n : 'Sin resultados');
        botonResultadoAnterior.disabled = botonResultadoSiguiente.disabled = n < 2;
    }

    function buscar(consulta) {
        var turno = ++busqueda.turno;
        busqueda.consulta = consulta;
        busqueda.resultados = [];
        busqueda.actual = -1;
        var buscada = normalizar(consulta.trim());
        if (buscada.length < MINIMO_BUSQUEDA) { actualizarContador(); marcarPagina(false); return; }
        contador.textContent = 'Buscando…';
        var numero = 1;
        (function siguiente() {
            if (turno !== busqueda.turno) { return; }
            if (numero > total) { terminar(); return; }
            var actualPagina = numero;
            textoDePagina(actualPagina).then(function (fragmentos) {
                if (turno !== busqueda.turno) { return; }
                var orden = 0;
                fragmentos.forEach(function (f) {
                    posiciones(f, buscada).forEach(function () { busqueda.resultados.push({ pagina: actualPagina, orden: orden++ }); });
                });
                numero++;
                siguiente();
            });
        })();
        function terminar() {
            if (busqueda.resultados.length) {
                var primero = busqueda.resultados.findIndex(function (r) { return r.pagina >= pagina; });
                busqueda.actual = primero < 0 ? 0 : primero;
                irAlResultado();
            } else {
                actualizarContador();
                marcarPagina(false);
            }
        }
    }

    function irAlResultado() {
        actualizarContador();
        var r = busqueda.resultados[busqueda.actual];
        if (!r) { return; }
        if (r.pagina !== pagina) { dibujar(r.pagina); } else { marcarPagina(true); }
    }

    function moverResultado(paso) {
        var n = busqueda.resultados.length;
        if (!n) { return; }
        busqueda.actual = (busqueda.actual + paso + n) % n;
        irAlResultado();
    }

    /** Resalta en la página dibujada las coincidencias; la actual, más fuerte. Con desplazar, la lleva al centro de la vista. */
    function marcarPagina(desplazar) {
        divsTexto.forEach(function (d) {
            if (d.dataset.original !== undefined) { d.textContent = d.dataset.original; delete d.dataset.original; }
        });
        var buscada = normalizar(busqueda.consulta.trim());
        if (buscada.length < MINIMO_BUSQUEDA) { return; }
        var r = busqueda.actual >= 0 ? busqueda.resultados[busqueda.actual] : null;
        var objetivo = r && r.pagina === pagina ? r.orden : -1;
        var orden = 0;
        var divActual = null;
        divsTexto.forEach(function (d) {
            var texto = d.textContent;
            var lugares = posiciones(texto, buscada);
            if (!lugares.length) { return; }
            d.dataset.original = texto;
            var html = '';
            var desde = 0;
            lugares.forEach(function (i) {
                html += escapar(texto.substring(desde, i)) + '<mark' + (orden === objetivo ? ' class="mv-actual"' : '') + '>'
                    + escapar(texto.substring(i, i + buscada.length)) + '</mark>';
                if (orden === objetivo) { divActual = d; }
                orden++;
                desde = i + buscada.length;
            });
            d.innerHTML = html + escapar(texto.substring(desde));
        });
        if (desplazar && divActual) {
            var a = divActual.getBoundingClientRect();
            var c = desplazable.getBoundingClientRect();
            desplazable.scrollTop += a.top - c.top - c.height / 2;
            desplazable.scrollLeft += a.left - c.left - c.width / 2;
        }
    }

    var temporizadorBusqueda = null;
    entradaBuscar.addEventListener('input', function () {
        clearTimeout(temporizadorBusqueda);
        temporizadorBusqueda = setTimeout(function () { if (documento) { buscar(entradaBuscar.value); } }, 300);
    });
    entradaBuscar.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            clearTimeout(temporizadorBusqueda);
            if (entradaBuscar.value !== busqueda.consulta) { buscar(entradaBuscar.value); } else { moverResultado(e.shiftKey ? -1 : 1); }
        } else if (e.key === 'Escape') {
            entradaBuscar.value = '';
            buscar('');
            entradaBuscar.blur();
        }
    });
    botonResultadoAnterior.addEventListener('click', function () { moverResultado(-1); });
    botonResultadoSiguiente.addEventListener('click', function () { moverResultado(1); });

    // ------------------------------------------------------------------ anotaciones del alumno
    var TAMANIOS = [2, 4, 8, 14, 22];   // grosor en píxeles de pantalla (el marcador es más ancho)
    var COLORES = ['#FCA5A5', '#FDE68A', '#86EFAC', '#93C5FD', '#FDBA74', '#F87171', '#FACC15', '#4ADE80', '#60A5FA', '#C4B5FD'];
    var trazosPorPagina = {};           // número de página -> lista de trazos { t, c, w, p: [[x, y], ...] } (todo relativo a la página)
    var anotando = false;
    var herramienta = 'marcador';
    var indiceTamanio = 2;
    var color = '#FDE68A';
    var trazoActual = null;
    var borrando = false;
    var cambioBorrando = false;
    var temporizadoresGuardado = {};
    var pendientesDeGuardar = {};
    var textoGuardado = visor.querySelector('[data-visor-guardado]');

    function dibujarTrazos() {
        if (!capaTrazos) { return; }
        var c = capaTrazos.getContext('2d');
        var ancho = capaTrazos.width;
        var alto = capaTrazos.height;
        c.clearRect(0, 0, ancho, alto);
        (trazosPorPagina[pagina] || []).forEach(function (t) {
            c.lineCap = 'round';
            c.lineJoin = 'round';
            c.strokeStyle = t.c;
            c.lineWidth = Math.max(1, t.w * ancho * (t.t === 'marcador' ? 2.2 : 1));
            c.globalAlpha = t.t === 'marcador' ? 0.45 : 1;
            c.globalCompositeOperation = t.t === 'marcador' ? 'multiply' : 'source-over';
            c.beginPath();
            t.p.forEach(function (q, i) { if (i) { c.lineTo(q[0] * ancho, q[1] * alto); } else { c.moveTo(q[0] * ancho, q[1] * alto); } });
            if (t.p.length === 1) { c.lineTo(t.p[0][0] * ancho + 0.1, t.p[0][1] * alto); }
            c.stroke();
        });
        c.globalAlpha = 1;
        c.globalCompositeOperation = 'source-over';
    }

    function mostrarGuardado(texto) { if (textoGuardado) { textoGuardado.textContent = texto; } }

    function guardarPagina(numero) {
        pendientesDeGuardar[numero] = true;
        mostrarGuardado('Guardando…');
        clearTimeout(temporizadoresGuardado[numero]);
        temporizadoresGuardado[numero] = setTimeout(function () { enviarPagina(numero); }, 500);
    }

    function cuerpoDePagina(numero) {
        return new URLSearchParams({ trazos: JSON.stringify(trazosPorPagina[numero] || []) });
    }

    function enviarPagina(numero) {
        delete pendientesDeGuardar[numero];
        fetch(urlResaltados + '/' + numero, { method: 'POST', body: cuerpoDePagina(numero), credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (r) { mostrarGuardado(r.ok ? 'Guardado' : 'No se pudo guardar. Intentá de nuevo.'); })
            .catch(function () { mostrarGuardado('No se pudo guardar. Intentá de nuevo.'); });
    }

    // Si se cierra la pestaña con anotaciones sin enviar, se mandan al salir
    window.addEventListener('pagehide', function () {
        Object.keys(pendientesDeGuardar).forEach(function (n) {
            if (navigator.sendBeacon) { navigator.sendBeacon(urlResaltados + '/' + n, cuerpoDePagina(n)); }
        });
    });

    function cargarResaltados() {
        if (!urlResaltados || !capaTrazos) { return; }
        fetch(urlResaltados, { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
            .then(function (r) { return r.ok ? r.json() : []; }).catch(function () { return []; })
            .then(function (paginas) {
                paginas.forEach(function (p) {
                    try { trazosPorPagina[p.pagina] = JSON.parse(p.trazos); } catch (e) { /* una página con datos dañados se ignora */ }
                });
                botonAnotar.hidden = false;
                dibujarTrazos();
            });
    }

    function punto(e) {
        var r = capaTrazos.getBoundingClientRect();
        return [Math.round((e.clientX - r.left) / r.width * 10000) / 10000, Math.round((e.clientY - r.top) / r.height * 10000) / 10000];
    }

    /** Distancia (en píxeles de pantalla) de un punto a un segmento: sirve para saber qué trazo toca la goma. */
    function distanciaASegmento(px, py, ax, ay, bx, by) {
        var dx = bx - ax;
        var dy = by - ay;
        var largo = dx * dx + dy * dy;
        var t = largo ? Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / largo)) : 0;
        return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
    }

    function borrarEn(p) {
        var lista = trazosPorPagina[pagina] || [];
        if (!lista.length) { return; }
        var r = capaTrazos.getBoundingClientRect();
        var px = p[0] * r.width;
        var py = p[1] * r.height;
        var radio = 6 + TAMANIOS[indiceTamanio] / 2;
        var quedan = lista.filter(function (t) {
            var holgura = t.w * r.width * (t.t === 'marcador' ? 2.2 : 1) / 2;
            for (var i = 0; i < t.p.length; i++) {
                var a = t.p[i];
                var b = t.p[i + 1] || a;
                if (distanciaASegmento(px, py, a[0] * r.width, a[1] * r.height, b[0] * r.width, b[1] * r.height) <= radio + holgura) { return false; }
            }
            return true;
        });
        if (quedan.length !== lista.length) {
            trazosPorPagina[pagina] = quedan;
            cambioBorrando = true;
            dibujarTrazos();
        }
    }

    function pintarOpciones() {
        panelHerramientas.querySelector('[data-visor-tamanios]').innerHTML = TAMANIOS.map(function (t, i) {
            return '<button type="button" class="' + (i === indiceTamanio ? 'activa' : '') + '" data-visor-tamanio="' + i + '" aria-label="Tamaño ' + (i + 1) + '">'
                + '<i style="width:' + (3 + i * 2.4) + 'px;height:' + (3 + i * 2.4) + 'px"></i></button>';
        }).join('');
        panelHerramientas.querySelector('[data-visor-colores]').innerHTML = COLORES.map(function (c) {
            return '<button type="button" class="' + (c === color ? 'activo' : '') + '" data-visor-color="' + c + '" style="background:' + c + '" aria-label="Color"></button>';
        }).join('');
        Array.prototype.forEach.call(panelHerramientas.querySelectorAll('[data-visor-herramienta]'), function (b) {
            b.classList.toggle('activa', b.dataset.visorHerramienta === herramienta);
        });
        hoja.classList.toggle('mv-borrando', herramienta === 'goma');
    }

    if (capaTrazos && botonAnotar && panelHerramientas) {
        pintarOpciones();
        botonAnotar.addEventListener('click', function () {
            anotando = !anotando;
            botonAnotar.classList.toggle('activo', anotando);
            botonAnotar.setAttribute('aria-pressed', String(anotando));
            panelHerramientas.hidden = !anotando;
            hoja.classList.toggle('anotando', anotando);
            if (documento) { dibujar(pagina); } // cambió el ancho disponible
        });
        panelHerramientas.addEventListener('click', function (e) {
            var h = e.target.closest('[data-visor-herramienta]');
            var t = e.target.closest('[data-visor-tamanio]');
            var c = e.target.closest('[data-visor-color]');
            if (h) { herramienta = h.dataset.visorHerramienta; }
            if (t) { indiceTamanio = parseInt(t.dataset.visorTamanio, 10); }
            if (c) { color = c.dataset.visorColor; if (herramienta === 'goma') { herramienta = 'marcador'; } }
            pintarOpciones();
        });

        capaTrazos.addEventListener('pointerdown', function (e) {
            if (!anotando || e.button !== 0) { return; }
            e.preventDefault(); // sin esto el navegador también arrastra la página
            capaTrazos.setPointerCapture(e.pointerId);
            var p = punto(e);
            if (herramienta === 'goma') {
                borrando = true;
                cambioBorrando = false;
                borrarEn(p);
                return;
            }
            var ancho = capaTrazos.getBoundingClientRect().width;
            trazoActual = { t: herramienta, c: color, w: Math.round(TAMANIOS[indiceTamanio] / ancho * 10000) / 10000, p: [p] };
            (trazosPorPagina[pagina] = trazosPorPagina[pagina] || []).push(trazoActual);
            dibujarTrazos();
        });
        capaTrazos.addEventListener('pointermove', function (e) {
            if (borrando) { borrarEn(punto(e)); return; }
            if (!trazoActual) { return; }
            var p = punto(e);
            var ultimo = trazoActual.p[trazoActual.p.length - 1];
            if (Math.abs(p[0] - ultimo[0]) + Math.abs(p[1] - ultimo[1]) < 0.0012) { return; }
            trazoActual.p.push(p);
            dibujarTrazos();
        });
        function soltar() {
            if (trazoActual) { trazoActual = null; guardarPagina(pagina); }
            if (borrando) { borrando = false; if (cambioBorrando) { guardarPagina(pagina); } }
        }
        capaTrazos.addEventListener('pointerup', soltar);
        capaTrazos.addEventListener('pointercancel', soltar);

        // Ctrl + Z: deshace el último trazo de la página
        document.addEventListener('keydown', function (e) {
            if (!anotando || !(e.ctrlKey || e.metaKey) || e.key.toLowerCase() !== 'z' || e.target.tagName === 'INPUT') { return; }
            var lista = trazosPorPagina[pagina] || [];
            if (lista.length) { e.preventDefault(); lista.pop(); dibujarTrazos(); guardarPagina(pagina); }
        });
    }

    // ------------------------------------------------------------------ apertura del documento
    window.pdfjsLib.getDocument({ url: visor.dataset.url, withCredentials: true }).promise.then(function (doc) {
        documento = doc;
        total = doc.numPages;
        etiquetaTotal.textContent = total;
        entradaPagina.max = total;
        // En pantallas anchas el panel lateral arranca abierto
        if (window.innerWidth >= 900 && total > 1) {
            lateral.hidden = false;
            botonLateral.classList.add('activo');
            botonLateral.setAttribute('aria-expanded', 'true');
        }
        dibujar(1);
        crearMiniaturas();
        cargarIndice();
        cargarResaltados();
    }).catch(function () {
        mostrarEstado('No se pudo abrir el archivo. Intentá nuevamente más tarde.');
    });

    botonAnterior.addEventListener('click', function () { irA(pagina - 1); });
    botonSiguiente.addEventListener('click', function () { irA(pagina + 1); });
    entradaPagina.addEventListener('change', function () { irA(parseInt(entradaPagina.value, 10) || pagina); });
    botonAlejar.addEventListener('click', function () { cambiarZoom(zoom - PASO_ZOOM); });
    botonAcercar.addEventListener('click', function () { cambiarZoom(zoom + PASO_ZOOM); });
    botonZoom.addEventListener('click', function () { cambiarZoom(1); });

    document.addEventListener('keydown', function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'f') {
            e.preventDefault();
            entradaBuscar.focus();
            entradaBuscar.select();
            return;
        }
        if (e.target === entradaPagina || e.target === entradaBuscar) { return; }
        if (e.key === 'ArrowLeft') { irA(pagina - 1); }
        if (e.key === 'ArrowRight') { irA(pagina + 1); }
        if (e.key === '+' || e.key === '=') { cambiarZoom(zoom + PASO_ZOOM); }
        if (e.key === '-') { cambiarZoom(zoom - PASO_ZOOM); }
        if (e.key === '0') { cambiarZoom(1); }
    });

    // Ctrl + rueda: zoom (sin Ctrl, la rueda desplaza la página como siempre)
    visor.addEventListener('wheel', function (e) {
        if (!e.ctrlKey) { return; }
        e.preventDefault();
        cambiarZoom(zoom + (e.deltaY < 0 ? PASO_ZOOM : -PASO_ZOOM));
    }, { passive: false });

    // Arrastrar con el mouse para moverse por la página cuando está ampliada
    var arrastre = null;
    desplazable.addEventListener('mousedown', function (e) {
        if (e.button !== 0) { return; }
        arrastre = { x: e.clientX, y: e.clientY, izquierda: desplazable.scrollLeft, arriba: desplazable.scrollTop };
        desplazable.classList.add('arrastrando');
    });
    window.addEventListener('mousemove', function (e) {
        if (!arrastre) { return; }
        desplazable.scrollLeft = arrastre.izquierda - (e.clientX - arrastre.x);
        desplazable.scrollTop = arrastre.arriba - (e.clientY - arrastre.y);
    });
    window.addEventListener('mouseup', function () {
        arrastre = null;
        desplazable.classList.remove('arrastrando');
    });

    botonPantalla.addEventListener('click', function () {
        if (document.fullscreenElement) {
            document.exitFullscreen();
        } else if (visor.requestFullscreen) {
            visor.requestFullscreen();
        }
    });

    // Al cambiar el tamaño de la ventana o entrar y salir de pantalla completa, se vuelve a ajustar la página
    var temporizador = null;
    function reajustar() {
        clearTimeout(temporizador);
        temporizador = setTimeout(function () { if (documento) { dibujar(pagina); } }, 150);
    }
    window.addEventListener('resize', reajustar);
    document.addEventListener('fullscreenchange', reajustar);
})();
