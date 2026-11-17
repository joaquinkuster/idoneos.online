/**
 * Fuentes de la generación con IA (paso "Fuentes"): al elegir el tema, lista los documentos del tema para que el docente marque cuáles se leen.
 * Vienen marcados los que no generó la IA; los que generó la IA, sin marcar. Se pueden ordenar por recientes, título o tipo y marcar todos a la vez.
 * Se usa dentro de un formulario con un select "temaId" y un contenedor con el atributo data-fuentes-ia.
 */
(function () {
    'use strict';

    var ICONOS = {
        'Presentación': ['naranja', 'fa-file-powerpoint'],
        'Material bibliográfico': ['violeta', 'fa-book'],
        'Apunte de Cátedra': ['rojo', 'fa-file-lines'],
        'Guía de Estudio': ['turquesa', 'fa-book-open']
    };
    var ORDENES = [['recientes', 'Recientes'], ['titulo', 'Título'], ['tipo', 'Tipo']];

    function escapar(texto) {
        var div = document.createElement('div');
        div.textContent = texto == null ? '' : texto;
        return div.innerHTML;
    }

    function icono(tipo) {
        var i = ICONOS[tipo] || ['gris', 'fa-file'];
        return '<span class="ac-recurso-icon ac-recurso-icon-' + i[0] + ' ac-recurso-icon-chico"><i class="fa-solid ' + i[1] + '"></i></span>';
    }

    function comparar(orden) {
        if (orden === 'titulo') { return function (a, b) { return a.titulo.localeCompare(b.titulo, 'es'); }; }
        if (orden === 'tipo') { return function (a, b) { return a.tipo.localeCompare(b.tipo, 'es') || a.titulo.localeCompare(b.titulo, 'es'); }; }
        return function (a, b) { return String(b.fecha).localeCompare(String(a.fecha)); };
    }

    function iniciar(contenedor) {
        var formulario = contenedor.closest('form');
        var tema = formulario && formulario.querySelector('select[name="temaId"]');
        // Sin tema (biblioteca digital): las fuentes salen de una dirección fija
        var direccionFija = contenedor.getAttribute('data-fuentes-url');
        if (!tema && !direccionFija) { return; }
        var pedido = 0;
        var fuentes = [];
        var marcadas = {};
        var orden = 'titulo';

        function vaciar(mensaje) {
            contenedor.innerHTML = mensaje ? '<div class="fi-vacio">' + escapar(mensaje) + '</div>' : '';
        }

        function actualizarTodos() {
            var todos = contenedor.querySelector('[data-fi-todos]');
            if (!todos) { return; }
            var cuantas = fuentes.filter(function (f) { return marcadas[f.id]; }).length;
            todos.checked = cuantas === fuentes.length;
            todos.indeterminate = cuantas > 0 && cuantas < fuentes.length;
        }

        function pintarLista() {
            var lista = contenedor.querySelector('.fi-lista');
            lista.innerHTML = fuentes.slice().sort(comparar(orden)).map(function (f) {
                return '<label class="fi-fila">' + icono(f.tipo)
                    + '<span class="fi-titulo" title="' + escapar(f.titulo) + '">' + escapar(f.titulo)
                    + (f.generadaConIa ? '<span class="fi-ia">(generado con IA)</span>' : '') + '</span>'
                    + '<input type="checkbox" name="fuenteIds" value="' + f.id + '"' + (marcadas[f.id] ? ' checked' : '') + '></label>';
            }).join('');
            actualizarTodos();
        }

        function pintar() {
            var html = '<input type="hidden" name="fuentesIndicadas" value="true">'
                + '<div class="fi-cabecera">'
                + '<button type="button" class="fi-orden-boton" data-fi-orden aria-label="Ordenar fuentes" title="Ordenar fuentes"><i class="fa-solid fa-arrow-down-wide-short"></i> Ordenar</button>'
                + '<div class="fi-orden-menu" hidden>'
                + ORDENES.map(function (o) {
                    return '<button type="button" class="fi-orden-opcion" data-orden="' + o[0] + '">' + o[1] + '<i class="fa-solid fa-check"></i></button>';
                }).join('')
                + '</div>'
                + '<label class="fi-todos">Seleccionar todo <input type="checkbox" data-fi-todos></label></div>'
                + '<div class="fi-lista"></div>';
            contenedor.innerHTML = html;
            marcarOrden();
            pintarLista();
        }

        function marcarOrden() {
            contenedor.querySelectorAll('.fi-orden-opcion').forEach(function (b) { b.classList.toggle('on', b.dataset.orden === orden); });
        }

        function cargar() {
            var numero = ++pedido;
            if (!direccionFija && !tema.value) { fuentes = []; vaciar('Elegí un tema para ver sus documentos.'); return; }
            vaciar(direccionFija ? 'Buscando los documentos de la biblioteca...' : 'Buscando los documentos del tema...');
            fetch(direccionFija || '/recurso/fuentes?temaId=' + encodeURIComponent(tema.value), {
                headers: { 'Accept': 'application/json' }, credentials: 'same-origin'
            }).then(function (r) { return r.ok ? r.json() : []; }).then(function (lista) {
                if (numero !== pedido) { return; }
                fuentes = lista;
                marcadas = {};
                fuentes.forEach(function (f) { marcadas[f.id] = !!f.porDefecto; });
                if (!fuentes.length) { vaciar(direccionFija ? 'No hay documentos listos para leer en la biblioteca.' : 'El tema no tiene documentos listos para leer.'); return; }
                pintar();
            }).catch(function () {
                if (numero === pedido) { vaciar('No se pudieron cargar los documentos del tema.'); }
            });
        }

        contenedor.addEventListener('click', function (e) {
            var boton = e.target.closest('[data-fi-orden]');
            var menu = contenedor.querySelector('.fi-orden-menu');
            if (boton && menu) { menu.hidden = !menu.hidden; return; }
            var opcion = e.target.closest('.fi-orden-opcion');
            if (opcion) {
                orden = opcion.dataset.orden;
                menu.hidden = true;
                marcarOrden();
                pintarLista();
                return;
            }
            if (menu && !menu.hidden && !e.target.closest('.fi-orden-menu')) { menu.hidden = true; }
        });
        contenedor.addEventListener('change', function (e) {
            if (e.target.matches('[data-fi-todos]')) {
                fuentes.forEach(function (f) { marcadas[f.id] = e.target.checked; });
                pintarLista();
            } else if (e.target.matches('input[name="fuenteIds"]')) {
                marcadas[e.target.value] = e.target.checked;
                actualizarTodos();
            }
        });

        if (tema) { tema.addEventListener('change', cargar); }
        // Si el tema ya viene elegido, o el select se completa por otro lado (select buscable), se carga también al abrir el modal
        var modal = contenedor.closest('.modal');
        if (modal) { modal.addEventListener('shown.bs.modal', cargar); }
        cargar();
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('[data-fuentes-ia]').forEach(iniciar);
    });
})();
