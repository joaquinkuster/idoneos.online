/**
 * Asistente navegacional (fragments/components/asistente.html): botón flotante (una brújula) en la esquina inferior derecha con la cantidad de
 * pendientes del usuario y una carta con la «ruta» de lo que tiene pendiente según su rol. Lo arma el servidor (GET /asistente/pendientes); no
 * hay inteligencia artificial.
 *
 *  - Al cargar la página se pide la cantidad para el contador del botón.
 *  - Al abrir la carta se piden de nuevo los datos con el esqueleto de carga de los listados, y los pasos entran escalonados.
 *  - Las cartas siguen el orden de cercanía. Cada paso es un enlace a la pantalla donde se
 *    resuelve. Solo las cartas urgentes (faltan 7 días o menos) llevan la marca y el círculo relleno: dorado, o rojo si faltan 3 o menos.
 *  - Las acciones rápidas quedan fijas al pie; los pasos hacen scroll por encima.
 *  - Se cierra con la X, con un clic en el encabezado, con Esc o con un clic afuera.
 */
(function () {
    'use strict';

    var raiz = document.getElementById('asistente');
    if (!raiz) { return; }
    document.body.appendChild(raiz); // dentro del navbar quedaría recortado y no flotaría sobre la página

    var boton = raiz.querySelector('[data-asistente-abrir]');
    var panel = raiz.querySelector('.as-panel');
    var cuerpo = raiz.querySelector('[data-asistente-cuerpo]');
    var pie = raiz.querySelector('[data-asistente-pie]');
    var contador = raiz.querySelector('[data-asistente-contador]');
    var titulo = raiz.querySelector('[data-asistente-titulo]');
    var SIN_MOVIMIENTO = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var abierto = false;
    var MAX_TITULO = 18; // los textos largos se cortan con «…» (el completo queda en el title)
    var MAX_CURSO = 24;
    var pedido = 0;

    function elemento(etiqueta, clase, texto) {
        var e = document.createElement(etiqueta);
        if (clase) { e.className = clase; }
        if (texto !== undefined) { e.textContent = texto; }
        return e;
    }

    function recortar(texto, maximo) {
        return texto.length > maximo ? texto.substring(0, maximo - 1).trimEnd() + '…' : texto;
    }

    function pedirPendientes() {
        return fetch('/asistente/pendientes', { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
            .then(function (r) { return r.ok ? r.json() : null; }).catch(function () { return null; });
    }

    function pintarContador(datos) {
        var total = datos.total;
        contador.hidden = !total;
        contador.textContent = total > 99 ? '99+' : total;
        boton.setAttribute('aria-label', total ? 'Abrir el asistente: ' + datos.resumen : 'Abrir el asistente');
    }

    /** Bloques grises que pulsan mientras llegan los datos, como en los listados. */
    function pintarEsqueleto() {
        titulo.textContent = 'Asistente';
        pie.hidden = true;
        cuerpo.textContent = '';
        var ruta = elemento('div', 'as-ruta');
        for (var i = 0; i < 3; i++) {
            var nodo = elemento('div', 'as-nodo');
            nodo.appendChild(elemento('span', 'as-punto'));
            var texto = elemento('span', 'as-nodo-texto');
            texto.appendChild(elemento('span', 'pn-sk as-sk-linea'));
            texto.appendChild(elemento('span', 'pn-sk as-sk-linea corta'));
            nodo.appendChild(texto);
            ruta.appendChild(nodo);
        }
        cuerpo.appendChild(ruta);
    }

    function pintarPie(atajos) {
        pie.textContent = '';
        pie.hidden = !atajos.length;
        if (!atajos.length) { return; }
        var chips = elemento('div', 'as-chips');
        atajos.forEach(function (a) {
            var chip;
            if (a.url === '#intereses') {
                // No es otra pantalla: abre la ventana de intereses del alumno, que ya maneja layout/intereses.js
                chip = elemento('button', 'as-chip', a.texto);
                chip.type = 'button';
                chip.setAttribute('data-abrir-intereses', '');
                chip.addEventListener('click', function () { alternar(false); });
            } else {
                chip = elemento('a', 'as-chip', a.texto);
                chip.href = a.url;
            }
            chips.appendChild(chip);
        });
        pie.appendChild(chips);
    }

    function pintar(datos) {
        cuerpo.textContent = '';
        if (!datos) {
            titulo.textContent = 'Asistente';
            pie.hidden = true;
            cuerpo.appendChild(elemento('div', 'as-vacio', 'Error! No se pudieron consultar tus pendientes. Intentá nuevamente en unos minutos.'));
            return;
        }
        var pendientes = datos.pendientes;
        titulo.textContent = pendientes.length ? '¿Por dónde seguimos, ' + datos.nombre + '?' : 'Todo en orden, ' + datos.nombre;

        if (pendientes.length) {
            // El contexto: qué son los pendientes de abajo (el aviso, si hay, sale al dejar el mouse encima) y cuántos hay
            var contexto = elemento('div', 'as-ctx');
            if (datos.aviso) { contexto.title = datos.aviso; }
            var iconoCtx = elemento('i', 'fa-solid fa-hourglass-half');
            iconoCtx.setAttribute('aria-hidden', 'true');
            contexto.appendChild(iconoCtx);
            contexto.appendChild(elemento('b', '', datos.titulo));
            contexto.appendChild(elemento('span', 'as-ctx-n', String(datos.total)));
            cuerpo.appendChild(contexto);

            // El primer pendiente es la tarjeta «Ahora»: neutra, dorada (faltan 7 días o menos) o roja (3 o menos), con un botón de acción
            var primero = pendientes[0];
            var nivel = primero.urgencia === 'alta' ? ' alta' : (primero.urgencia === 'media' ? ' media' : '');
            var hero = elemento('a', 'as-hero' + nivel);
            hero.href = primero.url;
            hero.setAttribute('aria-label', primero.accion + ': ' + primero.titulo + '. ' + primero.curso + (primero.fecha ? '. ' + primero.fecha : ''));
            if (primero.fecha) {
                var cuando = elemento('div', 'as-hero-cuando');
                var reloj = elemento('i', 'fa-regular ' + (nivel ? 'fa-clock' : 'fa-calendar'));
                reloj.setAttribute('aria-hidden', 'true');
                cuando.appendChild(reloj);
                cuando.appendChild(document.createTextNode(primero.fecha));
                hero.appendChild(cuando);
            }
            var tituloHero = elemento('div', 'as-hero-titulo', recortar(primero.titulo, 32));
            tituloHero.title = primero.titulo;
            hero.appendChild(tituloHero);
            var cursoHero = elemento('div', 'as-hero-curso', recortar(primero.curso, 40));
            cursoHero.title = primero.curso;
            hero.appendChild(cursoHero);
            var boton = elemento('div', 'as-go');
            boton.appendChild(elemento('span', '', primero.accion));
            var flechaHero = elemento('i', 'fa-solid fa-arrow-right');
            flechaHero.setAttribute('aria-hidden', 'true');
            boton.appendChild(flechaHero);
            hero.appendChild(boton);
            cuerpo.appendChild(hero);

            // Los pasos que siguen, numerados desde el 2
            var resto = pendientes.slice(1);
            if (resto.length || datos.total > pendientes.length) {
                var ruta = elemento('div', 'as-ruta');
                ruta.setAttribute('role', 'list');
                ruta.setAttribute('aria-label', datos.titulo);
                resto.forEach(function (p, i) {
                    var nodo = elemento('a', 'as-nodo');
                    nodo.href = p.url;
                    nodo.setAttribute('role', 'listitem');
                    nodo.setAttribute('aria-label', p.accion + ': ' + p.titulo + '. ' + p.curso + (p.fecha ? '. ' + p.fecha : ''));
                    if (p.fecha) { nodo.title = p.fecha; }
                    var punto = elemento('span', 'as-punto');
                    punto.appendChild(elemento('span', 'as-num', String(i + 2)));
                    nodo.appendChild(punto);
                    var texto = elemento('span', 'as-nodo-texto');
                    var tituloCarta = elemento('span', 'as-nodo-titulo', recortar(p.titulo, MAX_TITULO + 8));
                    tituloCarta.title = p.titulo;
                    texto.appendChild(tituloCarta);
                    var cursoCarta = elemento('span', 'as-nodo-curso', recortar(p.curso, MAX_CURSO + 8));
                    cursoCarta.title = p.curso;
                    texto.appendChild(cursoCarta);
                    nodo.appendChild(texto);
                    nodo.appendChild(elemento('span', 'as-nodo-accion', p.accion));
                    ruta.appendChild(nodo);
                });
                if (datos.total > pendientes.length) {
                    var mas = elemento('div', 'as-nodo mas');
                    mas.appendChild(elemento('span', 'as-punto'));
                    mas.appendChild(elemento('span', 'as-nodo-texto', 'y ' + (datos.total - pendientes.length) + ' más…'));
                    ruta.appendChild(mas);
                }
                cuerpo.appendChild(ruta);
            }
        } else {
            var ok = elemento('div', 'as-ok');
            var check = elemento('i', 'fa-solid fa-check');
            check.setAttribute('aria-hidden', 'true');
            ok.appendChild(check);
            cuerpo.appendChild(ok);
            cuerpo.appendChild(elemento('div', 'as-vacio', 'No tenés nada pendiente. Cuando haya algo, te lo muestro acá.'));
        }
        pintarPie(datos.atajos);

        // Los pasos de la ruta entran con el fundido escalonado de los listados (movimiento.css)
        if (!SIN_MOVIMIENTO) {
            var indice = 0;
            Array.prototype.forEach.call(cuerpo.querySelectorAll('.as-hero, .as-nodo'), function (el) {
                el.style.setProperty('--i', Math.min(indice++, 10));
                el.classList.add('pn-entra');
                setTimeout(function () { el.classList.remove('pn-entra'); el.style.removeProperty('--i'); }, 900 + Math.min(indice, 10) * 45);
            });
        }
    }

    function cargar(conEsqueleto) {
        var turno = ++pedido;
        var desde = Date.now();
        if (conEsqueleto) { pintarEsqueleto(); }
        return pedirPendientes().then(function (datos) {
            // El esqueleto se ve un instante como mínimo, para que no parpadee
            return new Promise(function (resolver) { setTimeout(function () { resolver(datos); }, conEsqueleto ? Math.max(0, 260 - (Date.now() - desde)) : 0); });
        }).then(function (datos) {
            if (turno !== pedido) { return; }
            if (datos) { pintarContador(datos); }
            if (conEsqueleto) { pintar(datos); }
        });
    }

    function alternar(mostrar) {
        abierto = mostrar;
        panel.hidden = !mostrar;
        boton.setAttribute('aria-expanded', mostrar ? 'true' : 'false');
        raiz.classList.toggle('abierto', mostrar);
        if (mostrar) { cargar(true); }
    }

    boton.addEventListener('click', function () { alternar(!abierto); });
    raiz.querySelector('[data-asistente-cabecera]').addEventListener('click', function () { alternar(false); boton.focus(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape' && abierto) { alternar(false); boton.focus(); } });
    document.addEventListener('click', function (e) { if (abierto && !raiz.contains(e.target)) { alternar(false); } });

    cargar(false); // el contador del botón
})();
