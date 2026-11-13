/** ABM de temas de un curso. */
(function () {
    'use strict';

    var ventana = document.getElementById('modalTemas');
    var formulario = ventana && ventana.querySelector('[data-temas-formulario]');
    if (!formulario) { return; }

    var CAMPOS = ['titulo', 'descripcion', 'contenido'];
    var ETIQUETAS = { titulo: 'Título', descripcion: 'Descripción', contenido: 'Contenido' };
    var contenedorItems = formulario.querySelector('[data-temas-items]');
    var empty = formulario.querySelector('[data-temas-empty]');
    var entradas = {};
    CAMPOS.forEach(function (campo) { entradas[campo] = formulario.querySelector('[data-tema-campo="' + campo + '"]'); });
    var tituloFormulario = formulario.querySelector('[data-tema-titulo-formulario]');
    var botonNuevo = formulario.querySelector('[data-tema-nuevo]');
    var cantidad = formulario.querySelector('[data-temas-cantidad]');
    var botonAplicar = formulario.querySelector('[data-tema-aplicar]');
    var botonQuitar = formulario.querySelector('[data-tema-quitar]');
    var campoCambios = formulario.querySelector('[data-temas-cambios]');
    var arbol = formulario.querySelector('[data-temas-arbol]');

    var temas = [];      // { clave, id, titulo, descripcion, contenido, original, nuevo, boton }
    var siguienteNuevo = 1;
    var seleccionado = null;

    function esc(texto) {
        return String(texto == null ? '' : texto).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function valoresDe(origen) {
        var valores = {};
        CAMPOS.forEach(function (campo) { valores[campo] = (origen[campo] || '').trim(); });
        return valores;
    }

    function cambiado(tema) {
        return tema.nuevo || CAMPOS.some(function (campo) { return (tema[campo] || '').trim() !== (tema.original[campo] || '').trim(); });
    }

    function marcar(campo, mensaje) {
        if (window.PanelValidacion && window.PanelValidacion.marcar) { window.PanelValidacion.marcar(campo, mensaje); }
    }

    function escribir(campo, valor) {
        entradas[campo].value = valor || '';
        entradas[campo].dispatchEvent(new Event('input', { bubbles: true }));
        marcar(entradas[campo], '');
    }

    function leerFormulario() {
        var valores = {};
        CAMPOS.forEach(function (campo) { valores[campo] = entradas[campo].value.trim(); });
        return valores;
    }

    /** Si lo escrito en el formulario es distinto del tema elegido (o, en un tema nuevo, si hay algo escrito). */
    function hayCambiosSinAplicar() {
        var escrito = leerFormulario();
        if (!seleccionado) { return CAMPOS.some(function (campo) { return escrito[campo] !== ''; }); }
        return CAMPOS.some(function (campo) { return escrito[campo] !== (seleccionado[campo] || '').trim(); });
    }

    /** Recorta el texto del panel izquierdo con «...» (el texto completo queda en el tooltip). */
    function recortar(texto, maximo) {
        return texto.length > maximo ? texto.substring(0, maximo - 3).trimEnd() + '...' : texto;
    }

    function dibujarLista() {
        temas.forEach(function (tema) {
            tema.boton.classList.toggle('activo', tema === seleccionado);
            var chip = tema.boton.querySelector('[data-estado]');
            var estado = tema.nuevo ? 'Nuevo' : (cambiado(tema) ? 'Modificado' : '');
            chip.textContent = estado;
            chip.classList.toggle('d-none', !estado);
            chip.classList.toggle('pn-chip-verde', !!estado);
            var elTitulo = tema.boton.querySelector('.pn-tema-item-titulo');
            var elDescripcion = tema.boton.querySelector('.pn-tema-item-desc');
            elTitulo.textContent = recortar(tema.titulo || '(sin título)', 30);
            elTitulo.title = tema.titulo || '';
            elDescripcion.textContent = recortar(tema.descripcion || 'Sin descripción', 45);
            elDescripcion.title = tema.descripcion || '';
        });
        empty.hidden = temas.length > 0;
        cantidad.textContent = temas.length;
    }

    function elegir(tema) {
        seleccionado = tema;
        CAMPOS.forEach(function (campo) { escribir(campo, tema ? tema[campo] : ''); });
        tituloFormulario.textContent = tema ? 'Editar tema' : 'Agregar nuevo tema';
        botonNuevo.hidden = !tema;
        botonAplicar.textContent = tema ? 'Editar' : 'Agregar';
        botonQuitar.hidden = !tema || (!tema.nuevo && !cambiado(tema));
        botonQuitar.textContent = tema && tema.nuevo ? 'Quitar de la lista' : 'Descartar cambios';
        dibujarLista();
    }

    function crearBoton(tema) {
        var boton = document.createElement('div');
        boton.className = 'pn-tema-item';
        boton.setAttribute('role', 'button');
        boton.tabIndex = 0;
        boton.innerHTML = '<div class="pn-tema-item-cuerpo"><span class="pn-tema-item-titulo"></span><span class="pn-tema-item-desc"></span></div>'
            + '<span class="pn-chip d-none" data-estado></span>';
        boton.addEventListener('click', function () { intentarElegir(tema); });
        tema.boton = boton;
        contenedorItems.appendChild(boton);
    }

    /** Valida el título del formulario y lo aplica al tema elegido (o lo agrega como nuevo). */
    function aplicar() {
        var escrito = leerFormulario();
        if (!escrito.titulo) {
            marcar(entradas.titulo, 'Debe completar el título.');
            return false;
        }
        var repetido = temas.some(function (otro) {
            return otro !== seleccionado && (otro.titulo || '').trim().toLowerCase() === escrito.titulo.toLowerCase();
        });
        if (repetido) {
            marcar(entradas.titulo, 'Ya existe un tema con ese título en el curso.');
            return false;
        }
        marcar(entradas.titulo, '');
        if (seleccionado) {
            CAMPOS.forEach(function (campo) { seleccionado[campo] = escrito[campo]; });
            elegir(seleccionado);
        } else {
            var nuevo = { clave: 'n' + (siguienteNuevo++), id: null, nuevo: true, original: {} };
            CAMPOS.forEach(function (campo) { nuevo[campo] = escrito[campo]; });
            temas.push(nuevo);
            crearBoton(nuevo);
            elegir(null);
        }
        return true;
    }

    /** Cambia de tema. Si había cambios sin aplicar los aplica antes (y si no son válidos, se queda donde está). */
    function intentarElegir(tema) {
        if (hayCambiosSinAplicar() && !aplicar()) { return; }
        elegir(tema);
    }

    contenedorItems.querySelectorAll('.pn-tema-item').forEach(function (boton) {
        var tema = { clave: 'e' + boton.dataset.id, id: parseInt(boton.dataset.id, 10), nuevo: false, boton: boton };
        CAMPOS.forEach(function (campo) { tema[campo] = boton.dataset[campo] || ''; });
        tema.original = valoresDe(tema);
        boton.addEventListener('click', function () { intentarElegir(tema); });
        boton.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); intentarElegir(tema); } });
        temas.push(tema);
    });

    formulario.querySelector('[data-tema-nuevo]').addEventListener('click', function () { intentarElegir(null); });
    botonAplicar.addEventListener('click', function () { aplicar(); });
    botonQuitar.addEventListener('click', function () {
        if (!seleccionado) { return; }
        if (seleccionado.nuevo) {
            seleccionado.boton.remove();
            temas.splice(temas.indexOf(seleccionado), 1);
            elegir(null);
        } else {
            CAMPOS.forEach(function (campo) { seleccionado[campo] = seleccionado.original[campo]; });
            elegir(seleccionado);
        }
    });
    CAMPOS.forEach(function (campo) {
        entradas[campo].addEventListener('input', function () {
            if (campo === 'titulo') { marcar(entradas.titulo, ''); }
        });
    });

    /** Validación del paso 1: se aplica lo que quedó escrito y tiene que haber al menos un tema nuevo o modificado. */
    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.temas = function (paso) {
        if (paso !== 1) { return true; }
        if (hayCambiosSinAplicar() && !aplicar()) { return false; }
        if (!temas.some(cambiado)) {
            marcar(entradas.titulo, 'Registrá un tema nuevo o modificá alguno de la lista.');
            return false;
        }
        return true;
    };

    // ----------------------------------------------------------------------
    // resumen
    // ----------------------------------------------------------------------

    function crear(etiqueta, clase, html) {
        var e = document.createElement(etiqueta);
        if (clase) { e.className = clase; }
        if (html !== undefined) { e.innerHTML = html; }
        return e;
    }

    function nodo(color, titulo, tipo, estado) {
        var n = crear('div', 'pn-nodo pn-nodo-' + color);
        var fila = crear('div', 'pn-nodo-fila');
        fila.appendChild(crear('span', 'pn-punto'));
        fila.appendChild(crear('span', 'pn-nodo-titulo', titulo));
        if (tipo) { fila.appendChild(crear('span', 'pn-chip pn-chip-' + tipo[0], esc(tipo[1]))); }
        if (estado) { fila.appendChild(crear('span', 'pn-chip pn-chip-estado pn-chip-' + estado[0], esc(estado[1]))); }
        n.appendChild(fila);
        return n;
    }

    function dibujarResumen() {
        arbol.innerHTML = '';
        var curso = nodo('oro', esc(formulario.dataset.curso), ['oro', 'Curso']);
        curso.classList.add('pn-nodo-primero');
        arbol.appendChild(curso);
        temas.filter(cambiado).forEach(function (tema) {
            var estado = tema.nuevo ? ['verde', 'Nuevo'] : ['naranja', 'Modificado'];
            var rama = nodo('azul', esc(tema.titulo), ['azul', 'Tema'], estado);
            rama.classList.add('pn-nodo-tercero');
            var grupo = nodo(tema.nuevo ? 'verde' : 'naranja', 'Datos básicos');
            grupo.classList.add('pn-nodo-grupo');
            CAMPOS.forEach(function (campo) {
                var valor = (tema[campo] || '').trim();
                var antes = (tema.original[campo] || '').trim();
                if (tema.nuevo) {
                    if (valor) { grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-nuevo', ETIQUETAS[campo] + ': <strong>' + esc(valor) + '</strong>')); }
                } else if (valor !== antes) {
                    grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-cambio', ETIQUETAS[campo] + ': <span class="pn-arbol-antes">'
                        + (antes ? esc(antes) : '—') + '</span><span class="pn-arbol-flecha">→</span><strong>' + (valor ? esc(valor) : '—') + '</strong>'));
                }
            });
            rama.appendChild(grupo);
            curso.appendChild(rama);
        });
    }

    var pasoResumen = arbol.closest('.pn-paso');
    new MutationObserver(function () {
        if (pasoResumen.classList.contains('activo')) { dibujarResumen(); }
    }).observe(pasoResumen, { attributes: true, attributeFilter: ['class'] });

    // Antes de enviar (este escucha se ejecuta antes que el de los formularios de la página), los cambios van en el campo oculto
    formulario.addEventListener('submit', function () {
        campoCambios.value = JSON.stringify(temas.filter(cambiado).map(function (tema) {
            return { id: tema.id, titulo: tema.titulo.trim(), descripcion: (tema.descripcion || '').trim(), contenido: (tema.contenido || '').trim() };
        }));
    });

    // ----------------------------------------------------------------------
    // apertura
    // ----------------------------------------------------------------------

    function mostrar() {
        if (window.bootstrap) { window.bootstrap.Modal.getOrCreateInstance(ventana).show(); }
    }

    function temaPorId(id) {
        return temas.filter(function (tema) { return tema.id === id; })[0] || null;
    }

    // Delegado: las filas llegan del servidor al filtrar o cambiar de página
    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-editar-tema]');
        if (!boton) { return; }
        elegir(temaPorId(parseInt(boton.dataset.id, 10)));
        mostrar();
    });
    ventana.addEventListener('show.bs.modal', function () {
        if (formulario.pasos) { formulario.pasos.mostrar(0); }
    });

    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && principal.dataset.abrirModal === 'modalTemas') {
        if (principal.dataset.editar) { elegir(temaPorId(parseInt(principal.dataset.editar, 10))); }
        mostrar();
        var direccion = new URL(window.location.href);
        ['nuevo', 'editar'].forEach(function (nombre) { direccion.searchParams.delete(nombre); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    elegir(seleccionado);
})();
