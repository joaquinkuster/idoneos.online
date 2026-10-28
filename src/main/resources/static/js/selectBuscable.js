/**
 * Selectores con búsqueda por texto.
 *
 * Convierte un <select> en un campo donde se escribe para filtrar las opciones (sin tildes ni mayúsculas) y se elige con
 * el mouse o con el teclado (flechas, Enter y Escape). El <select> original sigue siendo el campo del formulario: se
 * oculta, conserva su valor y recibe el evento "change", así que la validación y el envío no cambian.
 *
 * Se aplica a:
 *  - los <select data-buscable> de cualquier página;
 *  - los <select class="pn-input"> dentro de un modal de formulario que tengan MINIMO opciones o más (alumnos, cohortes,
 *    inscripciones, cursos, programas, temas...).
 * Con data-sin-busqueda un <select> queda como está.
 */
(function () {
    'use strict';

    var MINIMO = 6;

    function normalizar(texto) {
        return (texto || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
    }

    function iniciar(select) {
        if (select.dataset.buscableListo) { return; }
        select.dataset.buscableListo = '1';

        var envoltura = document.createElement('div');
        envoltura.className = 'sb';
        var campo = document.createElement('input');
        campo.type = 'text';
        campo.className = 'pn-input sb-campo';
        campo.autocomplete = 'off';
        campo.setAttribute('role', 'combobox');
        campo.setAttribute('aria-expanded', 'false');
        campo.setAttribute('aria-autocomplete', 'list');
        var flecha = document.createElement('i');
        flecha.className = 'fa-solid fa-chevron-down sb-flecha';
        flecha.setAttribute('aria-hidden', 'true');
        var lista = document.createElement('ul');
        lista.className = 'sb-lista';
        lista.setAttribute('role', 'listbox');
        lista.hidden = true;
        select.parentNode.insertBefore(envoltura, select);
        envoltura.appendChild(select);
        envoltura.appendChild(campo);
        envoltura.appendChild(flecha);
        document.body.appendChild(lista);
        select.classList.add('sb-oculto');
        select.tabIndex = -1;
        if (select.id) {
            var rotulo = document.querySelector('label[for="' + select.id + '"]');
            if (rotulo) { campo.id = select.id + 'Buscar'; rotulo.setAttribute('for', campo.id); }
        }

        var abierta = false;
        var activa = -1;
        var visibles = [];

        function opcionVacia() {
            return Array.prototype.find.call(select.options, function (o) { return o.value === ''; });
        }

        function textoMarcador() {
            return 'Escribí para buscar';
        }

        function mostrarSeleccion() {
            var elegida = select.selectedOptions.length ? select.selectedOptions[0] : null;
            campo.value = elegida && elegida.value !== '' ? elegida.textContent.trim() : '';
            campo.placeholder = textoMarcador();
        }

        function sincronizar() {
            campo.disabled = select.disabled;
            envoltura.classList.toggle('sb-deshabilitado', select.disabled);
            campo.classList.toggle('es-invalido', select.classList.contains('es-invalido'));
            mostrarSeleccion();
        }

        function posicionar() {
            var caja = campo.getBoundingClientRect();
            lista.style.left = caja.left + 'px';
            lista.style.width = caja.width + 'px';
            var abajo = window.innerHeight - caja.bottom;
            if (abajo < 200 && caja.top > abajo) {
                lista.style.top = 'auto';
                lista.style.bottom = (window.innerHeight - caja.top + 4) + 'px';
                lista.style.maxHeight = Math.min(260, caja.top - 12) + 'px';
            } else {
                lista.style.bottom = 'auto';
                lista.style.top = (caja.bottom + 4) + 'px';
                lista.style.maxHeight = Math.min(260, abajo - 12) + 'px';
            }
        }

        function dibujar(filtro) {
            var buscado = normalizar(filtro);
            lista.innerHTML = '';
            visibles = [];
            Array.prototype.forEach.call(select.options, function (opcion) {
                if (opcion.value === '') { return; }
                if (buscado && normalizar(opcion.textContent).indexOf(buscado) < 0) { return; }
                var item = document.createElement('li');
                item.className = 'sb-opcion' + (opcion.disabled ? ' sb-opcion-inactiva' : '')
                    + (opcion.selected ? ' sb-opcion-elegida' : '');
                item.setAttribute('role', 'option');
                item.textContent = opcion.textContent.trim();
                if (!opcion.disabled) {
                    item.addEventListener('mousedown', function (e) { e.preventDefault(); elegir(opcion); });
                    item.addEventListener('mousemove', function () { activar(visibles.indexOf(item)); });
                }
                lista.appendChild(item);
                visibles.push(item);
                item._opcion = opcion;
            });
            if (!visibles.length) {
                var vacio = document.createElement('li');
                vacio.className = 'sb-vacio';
                vacio.textContent = 'Sin resultados';
                lista.appendChild(vacio);
            }
            var primera = visibles.findIndex(function (i) { return !i._opcion.disabled; });
            var elegida = visibles.findIndex(function (i) { return i._opcion.selected && !buscado; });
            activar(elegida >= 0 ? elegida : primera);
        }

        function activar(indice) {
            activa = indice;
            visibles.forEach(function (item, i) { item.classList.toggle('sb-activa', i === indice); });
            if (indice >= 0 && visibles[indice]) { visibles[indice].scrollIntoView({ block: 'nearest' }); }
        }

        function abrir() {
            if (abierta || select.disabled) { return; }
            abierta = true;
            campo.setAttribute('aria-expanded', 'true');
            envoltura.classList.add('sb-abierto');
            lista.hidden = false;
            dibujar('');
            posicionar();
            campo.select();
        }

        function cerrar() {
            if (!abierta) { return; }
            abierta = false;
            campo.setAttribute('aria-expanded', 'false');
            envoltura.classList.remove('sb-abierto');
            lista.hidden = true;
            // lo escrito que no se eligió se descarta
            var vacia = opcionVacia();
            if (campo.value.trim() === '' && vacia && select.value !== '') {
                select.value = '';
                select.dispatchEvent(new Event('change', { bubbles: true }));
            }
            mostrarSeleccion();
        }

        function elegir(opcion) {
            if (select.value !== opcion.value) {
                select.value = opcion.value;
                select.dispatchEvent(new Event('change', { bubbles: true }));
            }
            mostrarSeleccion();
            cerrar();
            campo.blur();
        }

        campo.addEventListener('focus', abrir);
        campo.addEventListener('click', abrir);
        flecha.addEventListener('mousedown', function (e) {
            e.preventDefault();
            if (abierta) { cerrar(); campo.blur(); } else { campo.focus(); }
        });
        campo.addEventListener('input', function () {
            if (!abierta) { abrir(); }
            dibujar(campo.value);
        });
        campo.addEventListener('blur', cerrar);
        campo.addEventListener('keydown', function (e) {
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                if (!abierta) { abrir(); return; }
                var paso = e.key === 'ArrowDown' ? 1 : -1;
                var i = activa;
                do { i += paso; } while (i >= 0 && i < visibles.length && visibles[i]._opcion.disabled);
                if (i >= 0 && i < visibles.length) { activar(i); }
            } else if (e.key === 'Enter') {
                if (abierta) {
                    e.preventDefault();
                    if (activa >= 0 && visibles[activa]) { elegir(visibles[activa]._opcion); }
                }
            } else if (e.key === 'Escape') {
                if (abierta) { e.stopPropagation(); cerrar(); campo.blur(); }
            }
        });

        // El <select> lo cambian también otros scripts o el formulario al reiniciarse: el campo lo refleja
        select.addEventListener('change', mostrarSeleccion);
        if (select.form) { select.form.addEventListener('reset', function () { setTimeout(sincronizar, 0); }); }
        new MutationObserver(function () {
            sincronizar();
            if (abierta) { dibujar(campo.value); }
        }).observe(select, { childList: true, subtree: true, attributes: true, attributeFilter: ['disabled', 'class'] });
        window.addEventListener('resize', function () { if (abierta) { posicionar(); } });
        document.addEventListener('scroll', function () { if (abierta) { posicionar(); } }, true);

        sincronizar();
    }

    function iniciarTodos(raiz) {
        raiz.querySelectorAll('select[data-buscable], .pn-modal select.pn-input').forEach(function (select) {
            if (select.multiple || select.hasAttribute('data-sin-busqueda') || select.classList.contains('ts-multiselect')
                    || select.classList.contains('pn-bloqueado')) { return; }
            if (select.hasAttribute('data-buscable') || select.options.length >= MINIMO) { iniciar(select); }
        });
    }

    window.SelectBuscable = { iniciar: iniciar, iniciarTodos: iniciarTodos };

    function arrancar() { iniciarTodos(document); }
    if (document.readyState === 'loading') { document.addEventListener('DOMContentLoaded', arrancar); } else { arrancar(); }
    // Los modales pueden completar sus opciones al abrirse
    document.addEventListener('shown.bs.modal', function (e) { iniciarTodos(e.target); });
})();
