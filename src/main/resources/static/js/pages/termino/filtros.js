/**
 * Búsqueda del glosario de un tema. El texto, el índice alfabético y las páginas los resuelve el servidor: este script
 * pide por fetch la zona de resultados (términos, índice y páginas) con lo que se escribió o eligió, y la reemplaza.
 */
(function () {
    'use strict';

    var raiz = document.querySelector('[data-glosario]');
    if (!raiz) { return; }

    var url = raiz.getAttribute('data-glosario-url');
    var campoTexto = raiz.querySelector('[name="texto"]');
    var casillaDefiniciones = raiz.querySelector('[name="enDefiniciones"]');

    var letra = '';   // '' = todas, '#' = especiales
    var pagina = 1;   // 0 = todos los términos en una sola página
    var peticion = 0;
    var temporizador = null;

    function pedir() {
        var n = ++peticion;
        // Se conservan los parámetros de la dirección (por ejemplo, el enlace permanente a un término)
        var params = new URLSearchParams(window.location.search);
        ['texto', 'buscar', 'enDefiniciones', 'letra', 'pagina'].forEach(function (k) { params.delete(k); });
        params.set('buscar', '1');
        if (campoTexto.value.trim()) { params.set('texto', campoTexto.value.trim()); }
        if (casillaDefiniciones.checked) { params.set('enDefiniciones', 'true'); }
        if (letra) { params.set('letra', letra); }
        params.set('pagina', pagina);
        var desde = Date.now();
        raiz.classList.add('pn-cargando');
        raiz.setAttribute('aria-busy', 'true');
        fetch(url + '?' + params.toString(), {
            headers: { 'X-Tabla': 'filas', 'Accept': 'text/html' }, credentials: 'same-origin'
        }).then(function (r) {
            if (!r.ok) { throw new Error('No se pudo cargar el glosario.'); }
            return r.text();
        }).then(function (html) {
            if (n !== peticion) { return; }
            var plantilla = document.createElement('template');
            plantilla.innerHTML = html;
            var nuevo = plantilla.content.querySelector('[data-glosario-contenido]');
            var actual = raiz.querySelector('[data-glosario-contenido]');
            if (nuevo && actual) { actual.replaceWith(nuevo); }
        }).catch(function () {
            if (window.mostrarError) { window.mostrarError('Error! No se pudo cargar el glosario. Intente nuevamente.'); }
        }).then(function () {
            // El efecto de carga dura al menos un instante, para que se note aunque el servidor responda enseguida
            setTimeout(function () { if (n === peticion) { raiz.classList.remove('pn-cargando'); raiz.removeAttribute('aria-busy'); } },
                Math.max(0, 220 - (Date.now() - desde)));
        });
    }

    function pedirConEspera() {
        clearTimeout(temporizador);
        temporizador = setTimeout(pedir, 250);
    }

    campoTexto.addEventListener('input', function () { letra = ''; pagina = 1; pedirConEspera(); });
    casillaDefiniciones.addEventListener('change', function () { letra = ''; pagina = 1; pedir(); });
    raiz.querySelector('[data-glosario-filtros]').addEventListener('submit', function (e) { e.preventDefault(); });
    raiz.querySelector('[data-glosario-limpiar]').addEventListener('click', function () {
        campoTexto.value = '';
        casillaDefiniciones.checked = true;
        letra = '';
        pagina = 1;
        pedir();
    });

    // El índice y las páginas llegan del servidor con cada respuesta: se atienden por delegación
    raiz.addEventListener('click', function (e) {
        var enlaceLetra = e.target.closest('[data-glosario-letra]');
        var enlacePagina = e.target.closest('[data-glosario-pagina]');
        if (enlaceLetra) {
            e.preventDefault();
            letra = enlaceLetra.getAttribute('data-glosario-letra');
            pagina = 1;
            pedir();
        } else if (enlacePagina) {
            e.preventDefault();
            pagina = parseInt(enlacePagina.getAttribute('data-glosario-pagina'), 10);
            pedir();
        }
    });
})();
