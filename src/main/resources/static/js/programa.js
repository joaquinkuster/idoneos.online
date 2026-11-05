/**
 * ABM de programas de un curso (MOD-F-02, CU-15 a CU-18).
 *
 *  1. La ventana "Registrar programa" tiene a la izquierda la lista de programas del curso (el vigente lleva su insignia) y
 *     a la derecha el formulario del programa elegido (o de uno nuevo). Lo que se agrega o se modifica queda "en cola" hasta
 *     confirmar: todos los cambios se guardan juntos. El cronograma se arma en su propia vista.
 *  2. Un programa nuevo puede partir de uno anterior (copia su cronograma). Uno con inscripciones no se puede modificar.
 *  3. El resumen del último paso muestra, por cada programa con cambios, lo nuevo o el antes → después.
 *  4. Si la página se abre con ?nuevo o ?editar=<id>, abre sola la ventana (y elige el programa); se quitan esos parámetros
 *     de la dirección para que al recargar no vuelva a abrirse.
 */
(function () {
    'use strict';

    var ventana = document.getElementById('modalPrograma');
    var formulario = ventana && ventana.querySelector('[data-programa-formulario]');
    if (!formulario) { return; }

    var CAMPOS = ['nombre', 'descripcion'];
    var ETIQUETAS = { nombre: 'Nombre', descripcion: 'Descripción' };
    var contenedorItems = formulario.querySelector('[data-programas-items]');
    var vacio = formulario.querySelector('[data-programas-vacio]');
    var entradas = {};
    CAMPOS.forEach(function (campo) { entradas[campo] = formulario.querySelector('[data-programa-campo="' + campo + '"]'); });
    var selectOrigen = formulario.querySelector('[data-programa-origen-select]');
    var contenedorOrigen = formulario.querySelector('[data-programa-origen]');
    var avisoBloqueado = formulario.querySelector('[data-programa-bloqueado]');
    var tituloFormulario = formulario.querySelector('[data-programa-titulo-formulario]');
    var botonNuevo = formulario.querySelector('[data-programa-nuevo-item]');
    var cantidad = formulario.querySelector('[data-programas-cantidad]');
    var botonAplicar = formulario.querySelector('[data-programa-aplicar]');
    var botonQuitar = formulario.querySelector('[data-programa-quitar]');
    var campoCambios = formulario.querySelector('[data-programas-cambios]');
    var arbol = formulario.querySelector('[data-programas-arbol]');

    var programas = [];   // { clave, id, nombre, descripcion, origenId, origenNombre, original, nuevo, vigente, bloqueado, boton }
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

    function cambiado(programa) {
        return programa.nuevo || CAMPOS.some(function (campo) { return (programa[campo] || '').trim() !== (programa.original[campo] || '').trim(); });
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

    /** Si lo escrito en el formulario es distinto del programa elegido (o, en uno nuevo, si hay algo escrito). */
    function hayCambiosSinAplicar() {
        var escrito = leerFormulario();
        if (!seleccionado) { return CAMPOS.some(function (campo) { return escrito[campo] !== ''; }); }
        if (seleccionado.bloqueado) { return false; }
        return CAMPOS.some(function (campo) { return escrito[campo] !== (seleccionado[campo] || '').trim(); });
    }

    function dibujarLista() {
        programas.forEach(function (programa) {
            programa.boton.classList.toggle('activo', programa === seleccionado);
            var chip = programa.boton.querySelector('[data-estado]');
            var estado = programa.nuevo ? 'Nuevo' : (cambiado(programa) ? 'Modificado' : (programa.vigente ? 'Vigente' : ''));
            chip.textContent = estado;
            chip.classList.toggle('d-none', !estado);
            chip.classList.toggle('pn-chip-verde', !!estado);
            programa.boton.querySelector('.pn-tema-item-titulo').textContent = programa.nombre || '(sin nombre)';
            programa.boton.querySelector('.pn-tema-item-desc').textContent = programa.descripcion || 'Sin descripción';
        });
        vacio.hidden = programas.length > 0;
        cantidad.textContent = programas.length;
    }

    function elegir(programa) {
        seleccionado = programa;
        CAMPOS.forEach(function (campo) { escribir(campo, programa ? programa[campo] : ''); });
        var bloqueado = !!(programa && programa.bloqueado);
        CAMPOS.forEach(function (campo) { entradas[campo].disabled = bloqueado; });
        if (avisoBloqueado) { avisoBloqueado.hidden = !bloqueado; }
        if (contenedorOrigen) { contenedorOrigen.hidden = !!programa; }
        if (selectOrigen && !programa) { selectOrigen.value = ''; }
        tituloFormulario.textContent = programa ? 'Editar programa' : 'Agregar nuevo programa';
        botonNuevo.hidden = !programa;
        botonAplicar.textContent = programa ? 'Editar' : 'Agregar';
        botonAplicar.disabled = bloqueado;
        botonQuitar.hidden = !programa || bloqueado || (!programa.nuevo && !cambiado(programa));
        botonQuitar.textContent = programa && programa.nuevo ? 'Quitar de la lista' : 'Descartar cambios';
        dibujarLista();
    }

    function crearBoton(programa) {
        var boton = document.createElement('div');
        boton.className = 'pn-tema-item';
        boton.setAttribute('role', 'button');
        boton.tabIndex = 0;
        boton.innerHTML = '<div class="pn-tema-item-cuerpo"><span class="pn-tema-item-titulo"></span><span class="pn-tema-item-desc"></span></div>'
            + '<span class="pn-chip d-none" data-estado></span>';
        boton.addEventListener('click', function () { intentarElegir(programa); });
        programa.boton = boton;
        contenedorItems.appendChild(boton);
    }

    /** Valida el nombre del formulario y lo aplica al programa elegido (o lo agrega como nuevo). Devuelve false si no es válido. */
    function aplicar() {
        var escrito = leerFormulario();
        if (!escrito.nombre) {
            marcar(entradas.nombre, 'Debe completar el nombre.');
            return false;
        }
        var repetido = programas.some(function (otro) {
            return otro !== seleccionado && (otro.nombre || '').trim().toLowerCase() === escrito.nombre.toLowerCase();
        });
        if (repetido) {
            marcar(entradas.nombre, 'Ya existe un programa con ese nombre en el curso.');
            return false;
        }
        marcar(entradas.nombre, '');
        if (seleccionado) {
            CAMPOS.forEach(function (campo) { seleccionado[campo] = escrito[campo]; });
            elegir(seleccionado);
        } else {
            var nuevo = { clave: 'n' + (siguienteNuevo++), id: null, nuevo: true, vigente: false, bloqueado: false, original: {},
                origenId: selectOrigen && selectOrigen.value ? parseInt(selectOrigen.value, 10) : null,
                origenNombre: selectOrigen && selectOrigen.value ? selectOrigen.selectedOptions[0].dataset.nombre : '' };
            CAMPOS.forEach(function (campo) { nuevo[campo] = escrito[campo]; });
            programas.push(nuevo);
            crearBoton(nuevo);
            elegir(null);
        }
        return true;
    }

    /** Cambia de programa; si había cambios sin aplicar los aplica antes (y si no son válidos, se queda donde está). */
    function intentarElegir(programa) {
        if (hayCambiosSinAplicar() && !aplicar()) { return; }
        elegir(programa);
    }

    contenedorItems.querySelectorAll('.pn-tema-item').forEach(function (boton) {
        var programa = { clave: 'e' + boton.dataset.id, id: parseInt(boton.dataset.id, 10), nuevo: false, boton: boton,
            vigente: boton.dataset.vigente === 'true', bloqueado: boton.dataset.bloqueado === 'true' };
        CAMPOS.forEach(function (campo) { programa[campo] = boton.dataset[campo] || ''; });
        programa.original = valoresDe(programa);
        boton.addEventListener('click', function () { intentarElegir(programa); });
        boton.addEventListener('keydown', function (e) { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); intentarElegir(programa); } });
        programas.push(programa);
    });

    botonNuevo.addEventListener('click', function () { intentarElegir(null); });
    botonAplicar.addEventListener('click', function () { aplicar(); });
    botonQuitar.addEventListener('click', function () {
        if (!seleccionado) { return; }
        if (seleccionado.nuevo) {
            seleccionado.boton.remove();
            programas.splice(programas.indexOf(seleccionado), 1);
            elegir(null);
        } else {
            CAMPOS.forEach(function (campo) { seleccionado[campo] = seleccionado.original[campo]; });
            elegir(seleccionado);
        }
    });
    entradas.nombre.addEventListener('input', function () { marcar(entradas.nombre, ''); });

    /** Validación del paso 1: se aplica lo que quedó escrito y tiene que haber al menos un programa nuevo o modificado. */
    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.programas = function (paso) {
        if (paso !== 1) { return true; }
        if (hayCambiosSinAplicar() && !aplicar()) { return false; }
        if (!programas.some(cambiado)) {
            marcar(entradas.nombre, 'Registrá un programa nuevo o modificá alguno de la lista.');
            return false;
        }
        return true;
    };

    // ---------------------------------------------------------------- resumen
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
        programas.filter(cambiado).forEach(function (programa) {
            var estado = programa.nuevo ? ['verde', 'Nuevo'] : ['naranja', 'Modificado'];
            var rama = nodo('azul', esc(programa.nombre), ['azul', 'Programa'], estado);
            rama.classList.add('pn-nodo-tercero');
            var grupo = nodo(programa.nuevo ? 'verde' : 'naranja', 'Datos básicos');
            grupo.classList.add('pn-nodo-grupo');
            CAMPOS.forEach(function (campo) {
                var valor = (programa[campo] || '').trim();
                var antes = (programa.original[campo] || '').trim();
                if (programa.nuevo) {
                    if (valor) { grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-nuevo', ETIQUETAS[campo] + ': <strong>' + esc(valor) + '</strong>')); }
                } else if (valor !== antes) {
                    grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-cambio', ETIQUETAS[campo] + ': <span class="pn-arbol-antes">'
                        + (antes ? esc(antes) : '—') + '</span><span class="pn-arbol-flecha">→</span><strong>' + (valor ? esc(valor) : '—') + '</strong>'));
                }
            });
            if (programa.nuevo && programa.origenNombre) {
                grupo.appendChild(crear('div', 'pn-arbol-dato pn-dato-nuevo', 'Parte de: <strong>' + esc(programa.origenNombre) + '</strong>'));
            }
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
        campoCambios.value = JSON.stringify(programas.filter(cambiado).map(function (programa) {
            return { id: programa.id, origenId: programa.origenId || null, nombre: programa.nombre.trim(),
                descripcion: (programa.descripcion || '').trim() };
        }));
    });

    // ---------------------------------------------------------------- apertura
    function mostrar() {
        if (window.bootstrap) { window.bootstrap.Modal.getOrCreateInstance(ventana).show(); }
    }

    function programaPorId(id) {
        return programas.filter(function (programa) { return programa.id === id; })[0] || null;
    }

    document.querySelectorAll('[data-programa-nuevo]').forEach(function (boton) {
        boton.addEventListener('click', function () { elegir(null); mostrar(); });
    });
    // Delegado: las filas llegan del servidor al filtrar o cambiar de página
    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-programa-editar]');
        if (!boton || boton.disabled) { return; }
        elegir(programaPorId(parseInt(boton.dataset.id, 10)));
        mostrar();
    });
    ventana.addEventListener('show.bs.modal', function () {
        if (formulario.pasos) { formulario.pasos.mostrar(0); }
    });

    var principal = document.querySelector('[data-abrir-modal]');
    if (principal && principal.dataset.abrirModal === 'modalPrograma') {
        if (principal.dataset.editar) { elegir(programaPorId(parseInt(principal.dataset.editar, 10))); }
        mostrar();
        var direccion = new URL(window.location.href);
        ['nuevo', 'editar'].forEach(function (nombre) { direccion.searchParams.delete(nombre); });
        window.history.replaceState(null, '', direccion.pathname + direccion.search);
    }

    elegir(seleccionado);
})();
