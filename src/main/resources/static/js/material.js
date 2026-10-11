/**
 * Comportamiento del ABM de materiales (MOD-F-02).
 *
 *  1. Selector "Agregar un recurso o actividad" (vista del curso): muestra la descripción de lo elegido y, al
 *     confirmar, redirige al ABM de materiales con la unidad y el tipo elegidos o, si se elige Glosario, al
 *     glosario de la unidad con el formulario de alta abierto.
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
            if (elegida.dataset.glosario && destino) {
                // El glosario es uno por unidad: se abre el de la unidad elegida con el formulario de alta abierto
                var consulta = new URLSearchParams(destino.substring(destino.indexOf('?') + 1));
                window.location.href = consulta.get('unidadId')
                    ? '/glosario/ver/' + consulta.get('unidadId') + '?nuevo'
                    : '/glosario/buscar?cursoId=' + consulta.get('cursoId');
            } else if (destino) {
                window.location.href = destino + (destino.indexOf('?') >= 0 ? '&' : '?') + 'nuevo=' + elegida.dataset.tipoId;
            }
        });
    }

    // Botón "Agregar recurso o actividad" de cada unidad: el selector lleva al ABM con esa unidad elegida
    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-agregar-recurso]');
        if (!boton || !selector) { return; }
        var base = selector.dataset.destinoBase || selector.dataset.destino;
        selector.dataset.destinoBase = base;
        selector.dataset.destino = base + (base.indexOf('?') >= 0 ? '&' : '?') + 'unidadId=' + boton.dataset.unidadId;
        modal(selector).show();
    });

    // ------------------------------------------------------------------
    // 2 y 3. Formulario de alta según el tipo (y apertura automática desde ?nuevo={tipo})
    // ------------------------------------------------------------------
    /** Adapta el formulario de alta al tipo de material elegido. */
    function aplicarTipo(opcion) {
        var formulario = modalNuevo.querySelector('form');
        var nombre = opcion.dataset.nombre;
        var permiteEnlace = opcion.dataset.permiteEnlace === 'true';
        var extensiones = opcion.dataset.extensiones;
        var tamanio = opcion.dataset.tamanioMb;

        formulario.querySelectorAll('.es-invalido').forEach(function (campo) { window.PanelValidacion.marcar(campo, ''); });
        formulario.dataset.permiteEnlace = String(permiteEnlace);
        modalNuevo.querySelector('[data-material-ayuda]').textContent = opcion.dataset.descripcion;

        var archivo = formulario.elements['archivo'];
        archivo.value = '';
        archivo.accept = extensiones.split(',').map(function (e) { return '.' + e; }).join(',');
        archivo.dataset.extensiones = extensiones;
        archivo.dataset.tamanioMb = tamanio;
        archivo.required = !permiteEnlace;
        modalNuevo.querySelector('[data-archivo-etiqueta]').textContent = nombre === 'Grabación' ? 'Archivo de video'
            : nombre === 'Presentación' ? 'Archivo de la presentación' : 'Archivo';
        modalNuevo.querySelector('[data-archivo-obligatorio]').hidden = permiteEnlace;
        modalNuevo.querySelector('[data-archivo-ayuda]').textContent =
            'Formatos: ' + extensiones.toUpperCase().split(',').join(', ') + ' · máximo ' + tamanio + ' MB';
        var bloqueArchivo = modalNuevo.querySelector('[data-bloque="archivo"]');
        bloqueArchivo.classList.toggle('col-md-6', permiteEnlace); // comparte la fila con el enlace externo
        bloqueArchivo.classList.toggle('col-12', !permiteEnlace);
        modalNuevo.querySelector('[data-bloque="enlace"]').hidden = !permiteEnlace;
        modalNuevo.querySelector('[data-bloque="autor"]').hidden = !permiteEnlace;
        if (!permiteEnlace) {
            formulario.elements['enlace'].value = '';
            formulario.elements['autor'].value = '';
        }
    }

    if (modalNuevo) {
        var campoTipo = modalNuevo.querySelector('select[name="tipoMaterialId"]');
        campoTipo.addEventListener('change', function () { aplicarTipo(campoTipo.selectedOptions[0]); });
        modalNuevo.addEventListener('show.bs.modal', function () { aplicarTipo(campoTipo.selectedOptions[0]); });

        // Apertura automática desde ?nuevo={tipo}, con la unidad elegida
        var tipoInicial = modalNuevo.dataset.abrirTipo;
        if (tipoInicial && campoTipo.querySelector('option[value="' + tipoInicial + '"]')) {
            campoTipo.value = tipoInicial;
            if (modalNuevo.dataset.unidad) {
                modalNuevo.querySelector('select[name="unidadId"]').value = modalNuevo.dataset.unidad;
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
        var archivo = formulario.elements['archivo'];
        var enlace = formulario.elements['enlace'];
        if (formulario.dataset.permiteEnlace !== 'true') { return true; }
        var hayArchivo = archivo.files && archivo.files.length > 0;
        var hayEnlace = enlace.value.trim() !== '';
        if (!hayArchivo && !hayEnlace) {
            window.PanelValidacion.marcar(archivo, 'Debe seleccionar un archivo o ingresar un enlace externo.');
            return false;
        }
        if (hayArchivo && hayEnlace) {
            window.PanelValidacion.marcar(enlace, 'Indique un archivo o un enlace externo, no ambos.');
            return false;
        }
        if (hayEnlace && !/^https?:\/\//i.test(enlace.value.trim())) {
            window.PanelValidacion.marcar(enlace, 'El enlace debe comenzar con http:// o https://.');
            return false;
        }
        return true;
    };

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
