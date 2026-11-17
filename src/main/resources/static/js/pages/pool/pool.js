/** Editor de preguntas de los pools, dentro del formulario por pasos de cada pool. */
(function () {
    'use strict';

    var MAXIMO_OPCIONES = 8;
    var LARGO_TEXTO = 255;
    var TIPOS = { unica: 'Opción única', multiple: 'Opción múltiple', vf: 'Verdadero o falso' };

    function iniciar(raiz) {
        var datos = {};
        try {
            datos = JSON.parse(raiz.getAttribute('data-pool') || '{}');
        } catch (error) {
            datos = {};
        }
        var formulario = raiz.closest('form');
        var lista = raiz.querySelector('[data-pl-lista]');
        var campoPreguntas = raiz.querySelector('[data-pl-preguntas]');
        var total = raiz.querySelector('[data-total-preguntas]');
        var resumen = raiz.querySelector('[data-pl-resumen]');
        var siguienteUid = 1;
        var preguntas = [];
        var seleccionada = null;

        // ----------------------------------------------------------------------
        // Estado
        // ----------------------------------------------------------------------

        function tipoDe(pregunta) {
            if (pregunta.opcionMultiple) {
                return 'multiple';
            }
            var o = pregunta.opciones;
            return o.length === 2 && o[0].texto === 'Verdadero' && o[1].texto === 'Falso' ? 'vf' : 'unica';
        }

        function nuevaOpcion(texto, correcta, id) {
            return { id: id || null, texto: texto || '', correcta: !!correcta };
        }

        function nuevaPregunta(origen) {
            var pregunta = {
                uid: siguienteUid++,
                id: origen ? origen.id : null,
                texto: origen ? origen.texto : '',
                tipo: 'unica',
                abierta: !origen,
                opciones: []
            };
            if (origen) {
                pregunta.opciones = origen.opciones.map(function (o) { return nuevaOpcion(o.texto, o.correcta, o.id); });
                pregunta.tipo = tipoDe(origen);
            } else {
                pregunta.opciones = [nuevaOpcion(), nuevaOpcion()];
            }
            return pregunta;
        }

        function cambiarTipo(pregunta, tipo) {
            var anterior = pregunta.tipo;
            pregunta.tipo = tipo;
            if (tipo === 'vf') {
                var verdadera = pregunta.opciones.length && pregunta.opciones[0].correcta;
                pregunta.opciones = [nuevaOpcion('Verdadero', true), nuevaOpcion('Falso', false)];
                if (!verdadera && anterior !== 'vf') {
                    pregunta.opciones[0].correcta = true;
                }
            } else if (anterior === 'vf') {
                // Vuelve a ser editable: se conservan las dos opciones
                pregunta.opciones.forEach(function (o) { o.id = null; });
            }
            if (tipo === 'unica') {
                var yaHayUna = false;
                pregunta.opciones.forEach(function (o) {
                    if (o.correcta && yaHayUna) {
                        o.correcta = false;
                    }
                    yaHayUna = yaHayUna || o.correcta;
                });
            }
        }

        // ----------------------------------------------------------------------
        // Resumen para la hoja de ruta
        // ----------------------------------------------------------------------

        function describir(pregunta) {
            var opciones = pregunta.opciones.map(function (o) { return (o.texto || '(sin texto)') + (o.correcta ? ' ✓' : ''); });
            return (pregunta.texto || '(sin enunciado)') + ' - ' + TIPOS[pregunta.tipo] + ': ' + opciones.join(' | ');
        }

        function linea(etiqueta, texto) {
            var e = document.createElement('span');
            e.setAttribute('data-resumen-linea', etiqueta);
            e.textContent = texto;
            return e;
        }

        function actualizarResumen() {
            total.textContent = preguntas.length + (preguntas.length === 1 ? ' pregunta' : ' preguntas');
            resumen.textContent = '';
            resumen.appendChild(linea('Cantidad de preguntas', String(preguntas.length)));
            preguntas.forEach(function (p, i) { resumen.appendChild(linea('Pregunta ' + (i + 1), describir(p))); });
        }

        // ----------------------------------------------------------------------
        // Dibujo
        // ----------------------------------------------------------------------

        function elemento(etiqueta, atributos, hijos) {
            var nodo = document.createElement(etiqueta);
            Object.keys(atributos || {}).forEach(function (clave) {
                if (clave === 'texto') {
                    nodo.textContent = atributos[clave];
                } else if (clave === 'clase') {
                    nodo.className = atributos[clave];
                } else {
                    nodo.setAttribute(clave, atributos[clave]);
                }
            });
            (hijos || []).forEach(function (hijo) { nodo.appendChild(hijo); });
            return nodo;
        }

        function icon(clase) {
            return elemento('i', { 'class': clase, 'aria-hidden': 'true' });
        }

        function boton(clase, claseIcon, titulo, accion) {
            var b = elemento('button', { type: 'button', clase: clase, title: titulo, 'aria-label': titulo }, [icon(claseIcon)]);
            b.addEventListener('click', accion);
            return b;
        }

        function dibujarOpcion(pregunta, indice) {
            var opcion = pregunta.opciones[indice];
            var esVf = pregunta.tipo === 'vf';
            var marcador = elemento('input', {
                type: pregunta.tipo === 'multiple' ? 'checkbox' : 'radio',
                clase: 'pl-marcador',
                'aria-label': 'Opción correcta'
            });
            marcador.checked = opcion.correcta;
            marcador.addEventListener('change', function () {
                if (pregunta.tipo === 'multiple') {
                    opcion.correcta = marcador.checked;
                } else {
                    pregunta.opciones.forEach(function (o) { o.correcta = false; });
                    opcion.correcta = true;
                }
                actualizarResumen();
            });
            // Los radios de una misma pregunta comparten grupo sin chocar con los de otros formularios de la página
            marcador.name = 'correcta-' + formulario.id + '-' + raiz.dataset.uid + '-' + pregunta.uid;

            var texto = elemento('input', { type: 'text', clase: 'pn-input pl-opcion-texto', maxlength: LARGO_TEXTO,
                placeholder: 'Texto de la opción' });
            texto.value = opcion.texto;
            texto.readOnly = esVf;
            texto.addEventListener('input', function () { opcion.texto = texto.value; actualizarResumen(); });

            var fila = elemento('div', { clase: 'pl-opcion' }, [marcador, texto]);
            if (!esVf && pregunta.opciones.length > 2) {
                fila.appendChild(boton('pl-icon-boton', 'fa-solid fa-xmark', 'Quitar opción', function () {
                    pregunta.opciones.splice(indice, 1);
                    dibujar();
                }));
            }
            return fila;
        }

        /** Lista de preguntas a la izquierda y detalle de la elegida a la derecha, como en la ventana de temas. */
        function dibujarDetalle(pregunta, indice) {
            var detalle = elemento('div', {});
            var cabecera = elemento('div', { clase: 'pn-temas-formulario-cabecera' }, [
                elemento('h5', { clase: 'pn-temas-subtitulo', texto: 'Pregunta ' + (indice + 1) })]);
            var nueva = elemento('button', { type: 'button', clase: 'pn-btn pn-btn-contorno pn-btn-chico' },
                [icon('fa-solid fa-plus'), document.createTextNode(' Nueva pregunta')]);
            nueva.addEventListener('click', agregarPregunta);
            cabecera.appendChild(nueva);
            detalle.appendChild(cabecera);

            var campos = elemento('div', { clase: 'row g-3' });

            var colTipo = elemento('div', { clase: 'col-12 pn-campo' });
            colTipo.appendChild(elemento('label', { clase: 'pn-etiqueta', texto: 'Tipo de pregunta' }));
            var selectorTipo = elemento('select', { clase: 'pn-input', 'aria-label': 'Tipo de pregunta', 'data-sin-busqueda': '' }, [
                elemento('option', { value: 'unica', texto: TIPOS.unica }),
                elemento('option', { value: 'multiple', texto: TIPOS.multiple }),
                elemento('option', { value: 'vf', texto: TIPOS.vf })]);
            selectorTipo.value = pregunta.tipo;
            selectorTipo.addEventListener('change', function () {
                cambiarTipo(pregunta, selectorTipo.value);
                dibujar();
            });
            colTipo.appendChild(selectorTipo);
            campos.appendChild(colTipo);

            var colEnunciado = elemento('div', { clase: 'col-12 pn-campo' });
            var contador = elemento('span', { clase: 'pn-contador', texto: pregunta.texto.length + ' / ' + LARGO_TEXTO });
            var enunciado = elemento('textarea', { clase: 'pn-input pl-enunciado', rows: '3', maxlength: LARGO_TEXTO,
                placeholder: 'Enunciado de la pregunta' });
            enunciado.value = pregunta.texto;
            enunciado.addEventListener('input', function () {
                pregunta.texto = enunciado.value;
                contador.textContent = enunciado.value.length + ' / ' + LARGO_TEXTO;
                var item = lista.querySelector('.pn-tema-item.activo .pn-tema-item-titulo');
                if (item) { item.textContent = tituloDeItem(pregunta, indice); }
                actualizarResumen();
            });
            colEnunciado.appendChild(elemento('div', { clase: 'pn-etiqueta-fila' }, [
                elemento('label', { clase: 'pn-etiqueta', texto: 'Enunciado' }), contador]));
            colEnunciado.appendChild(enunciado);
            campos.appendChild(colEnunciado);

            var colOpciones = elemento('div', { clase: 'col-12 pn-campo' });
            colOpciones.appendChild(elemento('label', { clase: 'pn-etiqueta', texto: 'Opciones de respuesta' }));
            colOpciones.appendChild(elemento('p', { clase: 'pl-ayuda', texto: pregunta.tipo === 'multiple'
                ? 'Marcá todas las opciones correctas (al menos una).' : 'Marcá la única opción correcta.' }));
            pregunta.opciones.forEach(function (o, i) { colOpciones.appendChild(dibujarOpcion(pregunta, i)); });
            if (pregunta.tipo !== 'vf' && pregunta.opciones.length < MAXIMO_OPCIONES) {
                var agregar = elemento('button', { type: 'button', clase: 'pn-btn pn-btn-contorno pn-btn-chico' },
                    [icon('fa-solid fa-plus'), document.createTextNode(' Agregar opción')]);
                agregar.style.marginTop = '12px';
                agregar.addEventListener('click', function () {
                    pregunta.opciones.push(nuevaOpcion());
                    dibujar();
                });
                colOpciones.appendChild(agregar);
            }
            campos.appendChild(colOpciones);
            detalle.appendChild(campos);
            return detalle;
        }

        function tituloDeItem(pregunta, indice) {
            var texto = pregunta.texto || 'Sin enunciado';
            return (indice + 1) + '. ' + (texto.length > 38 ? texto.substring(0, 37) + '…' : texto);
        }

        function agregarPregunta() {
            var nueva = nuevaPregunta();
            preguntas.push(nueva);
            seleccionada = nueva.uid;
            dibujar();
            var campo = lista.querySelector('.pl-enunciado');
            if (campo) { campo.focus(); }
        }

        function dibujar() {
            lista.textContent = '';
            lista.className = 'pn-temas-doble pl-doble';
            if (!preguntas.some(function (p) { return p.uid === seleccionada; })) {
                seleccionada = preguntas.length ? preguntas[0].uid : null;
            }

            var izquierda = elemento('div', {});
            izquierda.appendChild(elemento('h5', { clase: 'pn-temas-subtitulo',
                texto: 'Preguntas agregadas (' + preguntas.length + ')' }));
            var caja = elemento('div', { clase: 'pn-temas-caja' });
            var items = elemento('div', { clase: 'pn-temas-items' });
            preguntas.forEach(function (pregunta, indice) {
                var item = elemento('div', { clase: 'pn-tema-item' + (pregunta.uid === seleccionada ? ' activo' : ''), role: 'button', tabindex: '0' });
                var cuerpo = elemento('div', { clase: 'pn-tema-item-cuerpo' }, [
                    elemento('span', { clase: 'pn-tema-item-titulo', texto: tituloDeItem(pregunta, indice) }),
                    elemento('span', { clase: 'pn-tema-item-desc', texto: TIPOS[pregunta.tipo] + ' · ' + pregunta.opciones.length + ' opciones' })]);
                item.appendChild(cuerpo);
                item.appendChild(boton('pn-tema-item-quitar', 'fa-solid fa-trash', 'Quitar pregunta', function (e) {
                    e.stopPropagation();
                    preguntas.splice(indice, 1);
                    dibujar();
                }));
                item.addEventListener('click', function () { seleccionada = pregunta.uid; dibujar(); });
                item.addEventListener('keydown', function (e) {
                    if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); seleccionada = pregunta.uid; dibujar(); }
                });
                items.appendChild(item);
            });
            caja.appendChild(items);
            if (!preguntas.length) {
                caja.appendChild(elemento('div', { clase: 'pn-temas-empty' }, [
                    elemento('p', { texto: 'No hay preguntas agregadas aún' }), elemento('small', { texto: 'Agregá la primera →' })]));
            }
            izquierda.appendChild(caja);
            lista.appendChild(izquierda);

            var derecha = elemento('div', {});
            var elegida = preguntas.filter(function (p) { return p.uid === seleccionada; })[0];
            if (elegida) {
                derecha.appendChild(dibujarDetalle(elegida, preguntas.indexOf(elegida)));
            } else {
                var nuevaVacia = elemento('button', { type: 'button', clase: 'pn-btn pn-btn-contorno pn-btn-chico' },
                    [icon('fa-solid fa-plus'), document.createTextNode(' Nueva pregunta')]);
                nuevaVacia.addEventListener('click', agregarPregunta);
                derecha.appendChild(nuevaVacia);
            }
            lista.appendChild(derecha);
            actualizarResumen();
        }

        // ----------------------------------------------------------------------
        // Inicio y envío
        // ----------------------------------------------------------------------

        (datos.preguntas || []).forEach(function (origen) { preguntas.push(nuevaPregunta(origen)); });
        if (!preguntas.length) {
            preguntas.push(nuevaPregunta());
        }
        dibujar();

        raiz.querySelectorAll('[data-agregar-pregunta]').forEach(function (agregar) { agregar.addEventListener('click', agregarPregunta); });

        formulario.addEventListener('submit', function () {
            campoPreguntas.value = JSON.stringify(preguntas.map(function (p) {
                return {
                    id: p.id,
                    texto: p.texto,
                    opcionMultiple: p.tipo === 'multiple',
                    opciones: p.opciones.map(function (o) { return { id: o.id, texto: o.texto, correcta: o.correcta }; })
                };
            }));
        });
    }

    var contador = 0;
    function iniciarEditores(raizDocumento) {
        Array.prototype.forEach.call(raizDocumento.querySelectorAll('[data-pool-editor]'), function (raiz) {
            var i = contador++;
            raiz.dataset.uid = String(i);
            var formulario = raiz.closest('form');
            if (formulario && !formulario.id) { formulario.id = 'formPool' + i; }
            iniciar(raiz);
        });
    }
    iniciarEditores(document);
    // Las ventanas de modificación llegan después de la carga (core/modalModificar.js)
    document.addEventListener('ventana-cargada', function (e) { iniciarEditores(e.detail.ventana); });
})();
