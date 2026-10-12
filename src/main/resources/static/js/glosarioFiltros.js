/**
 * Filtros del glosario de una unidad, en el navegador (sin recargar la página): el texto (en términos o en términos y
 * definiciones), el índice alfabético y la paginación se aplican sobre los términos que entrega el servidor.
 * El servidor entrega todos los términos agrupados por letra; acá se muestran los que cumplen los filtros.
 */
(function () {
    'use strict';

    var raiz = document.querySelector('[data-glosario]');
    if (!raiz) { return; }

    var TERMINOS_POR_PAGINA = 10;
    var ABECEDARIO = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

    var campoTexto = raiz.querySelector('[name="texto"]');
    var casillaDefiniciones = raiz.querySelector('[name="enDefiniciones"]');
    var indice = raiz.querySelector('[data-glosario-indice]');
    var paginas = raiz.querySelector('[data-glosario-paginas]');
    var resultado = raiz.querySelector('[data-glosario-resultado]');
    var vacio = raiz.querySelector('[data-glosario-vacio]');
    var terminos = Array.prototype.slice.call(raiz.querySelectorAll('[data-termino]'));
    var grupos = Array.prototype.slice.call(raiz.querySelectorAll('[data-grupo]'));

    var letra = '';   // '' = todas, '#' = especiales
    var pagina = 1;   // 0 = todos los términos en una sola página

    function normalizar(texto) {
        return (texto || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase().trim();
    }

    function coincidentes() {
        var buscado = normalizar(campoTexto.value);
        if (!buscado) { return terminos.slice(); }
        return terminos.filter(function (t) {
            return normalizar(t.dataset.nombre).indexOf(buscado) >= 0
                || (casillaDefiniciones.checked && normalizar(t.dataset.definicion).indexOf(buscado) >= 0);
        });
    }

    function enlace(texto, activa, alElegir) {
        var a = document.createElement('a');
        a.href = '#';
        a.className = 'gl-indice-letra' + (activa ? ' activa' : '');
        a.textContent = texto;
        a.addEventListener('click', function (e) { e.preventDefault(); alElegir(); });
        return a;
    }

    function separador() {
        var s = document.createElement('span');
        s.className = 'gl-indice-separador';
        s.textContent = '|';
        return s;
    }

    function dibujar() {
        var encontrados = coincidentes();
        var letras = {};
        encontrados.forEach(function (t) { letras[t.dataset.letra] = true; });
        if (letra && !letras[letra]) { letra = ''; }
        var deLaLetra = letra ? encontrados.filter(function (t) { return t.dataset.letra === letra; }) : encontrados;
        var totalPaginas = Math.max(1, Math.ceil(deLaLetra.length / TERMINOS_POR_PAGINA));
        if (pagina > totalPaginas) { pagina = totalPaginas; }
        var visibles = pagina === 0 ? deLaLetra : deLaLetra.slice((pagina - 1) * TERMINOS_POR_PAGINA, pagina * TERMINOS_POR_PAGINA);

        terminos.forEach(function (t) { t.hidden = visibles.indexOf(t) < 0; });
        grupos.forEach(function (g) {
            g.hidden = !Array.prototype.some.call(g.querySelectorAll('[data-termino]'), function (t) { return !t.hidden; });
        });

        // Índice alfabético: las letras sin términos no se pueden elegir
        indice.innerHTML = '';
        function letraDelIndice(valor, texto) {
            if (letras[valor]) {
                indice.appendChild(enlace(texto, letra === valor, function () { letra = valor; pagina = 1; dibujar(); }));
            } else {
                var vacia = document.createElement('span');
                vacia.className = 'gl-indice-letra gl-indice-letra-vacia';
                vacia.textContent = texto;
                indice.appendChild(vacia);
            }
        }
        letraDelIndice('#', 'Especial');
        ABECEDARIO.forEach(function (l) { indice.appendChild(separador()); letraDelIndice(l, l); });
        indice.appendChild(separador());
        indice.appendChild(enlace('TODAS', letra === '', function () { letra = ''; pagina = 1; dibujar(); }));

        // Páginas
        paginas.innerHTML = '';
        paginas.hidden = totalPaginas <= 1;
        if (totalPaginas > 1) {
            var titulo = document.createElement('span');
            titulo.textContent = 'Página:';
            paginas.appendChild(titulo);
            for (var n = 1; n <= totalPaginas; n++) {
                if (n === pagina) {
                    var actual = document.createElement('strong');
                    actual.textContent = n;
                    paginas.appendChild(actual);
                } else {
                    paginas.appendChild(enlace(String(n), false, (function (destino) {
                        return function () { pagina = destino; dibujar(); };
                    })(n)));
                }
            }
            if (pagina === 0) {
                var todas = document.createElement('strong');
                todas.textContent = 'TODAS';
                paginas.appendChild(todas);
            } else {
                paginas.appendChild(enlace('TODAS', false, function () { pagina = 0; dibujar(); }));
            }
        }

        var texto = campoTexto.value.trim();
        resultado.hidden = !texto;
        resultado.textContent = encontrados.length + (encontrados.length === 1 ? ' término encontrado' : ' términos encontrados')
            + ' para «' + texto + '»';
        if (vacio) { vacio.hidden = encontrados.length > 0; }
    }

    campoTexto.addEventListener('input', function () { letra = ''; pagina = 1; dibujar(); });
    casillaDefiniciones.addEventListener('change', function () { letra = ''; pagina = 1; dibujar(); });
    raiz.querySelector('[data-glosario-filtros]').addEventListener('submit', function (e) { e.preventDefault(); });
    raiz.querySelector('[data-glosario-limpiar]').addEventListener('click', function () {
        campoTexto.value = '';
        casillaDefiniciones.checked = true;
        letra = '';
        pagina = 1;
        dibujar();
    });

    dibujar();
})();
