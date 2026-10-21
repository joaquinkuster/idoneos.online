/**
 * Editor de preguntas de los pools (CU-61 y CU-62), dentro del formulario por pasos de cada pool.
 *
 * Cada editor ([data-pool-editor]) se arma a partir de los datos del pool (atributo data-pool): el nombre y las preguntas con
 * sus opciones. Cada pregunta es de opción única, de opción múltiple o de verdadero o falso. Al enviar, las preguntas se
 * serializan como una lista JSON en el campo oculto "preguntas"; el envío lo hace formularios.js y las reglas las valida el
 * servidor. El editor también deja, fuera de la vista, una línea por pregunta (data-resumen-linea) para que el resumen del
 * formulario (hojaDeRuta.js) muestre qué se registra o qué cambió; por eso se dibuja apenas carga la página.
 */
(function () {
    'use strict';

    var MAXIMO_OPCIONES = 8;
    var LARGO_TEXTO = 150;
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

        // ------------------------------------------------------------ Estado

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

        // ------------------------------------------------------------ Resumen para la hoja de ruta

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

        // ------------------------------------------------------------ Dibujo

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

        function icono(clase) {
            return elemento('i', { 'class': clase, 'aria-hidden': 'true' });
        }

        function boton(clase, claseIcono, titulo, accion) {
            var b = elemento('button', { type: 'button', clase: clase, title: titulo, 'aria-label': titulo }, [icono(claseIcono)]);
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
                fila.appendChild(boton('pl-icono-boton', 'fa-solid fa-xmark', 'Quitar opción', function () {
                    pregunta.opciones.splice(indice, 1);
                    dibujar();
                }));
            }
            return fila;
        }

        function dibujarPregunta(pregunta, indice) {
            var tarjeta = elemento('article', { clase: 'pl-pregunta' + (pregunta.abierta ? '' : ' pl-cerrada') });

            var resumenTitulo = elemento('span', { clase: 'pl-resumen' });
            var titulo = elemento('button', { type: 'button', clase: 'pl-titulo', 'aria-expanded': String(pregunta.abierta) }, [
                icono('fa-solid fa-chevron-down pl-flecha'),
                elemento('span', { clase: 'pl-numero', texto: 'Pregunta ' + (indice + 1) }), resumenTitulo]);
            titulo.addEventListener('click', function () {
                pregunta.abierta = !pregunta.abierta;
                tarjeta.classList.toggle('pl-cerrada', !pregunta.abierta);
                titulo.setAttribute('aria-expanded', String(pregunta.abierta));
            });
            function actualizarTitulo() {
                resumenTitulo.textContent = pregunta.texto ? '- ' + pregunta.texto : '';
            }
            actualizarTitulo();

            var selectorTipo = elemento('select', { clase: 'pn-input pl-tipo', 'aria-label': 'Tipo de pregunta',
                'data-sin-busqueda': '' }, [
                elemento('option', { value: 'unica', texto: TIPOS.unica }),
                elemento('option', { value: 'multiple', texto: TIPOS.multiple }),
                elemento('option', { value: 'vf', texto: TIPOS.vf })]);
            selectorTipo.value = pregunta.tipo;
            selectorTipo.addEventListener('change', function () {
                cambiarTipo(pregunta, selectorTipo.value);
                dibujar();
            });

            var cabecera = elemento('div', { clase: 'pl-pregunta-cabecera' }, [titulo, selectorTipo]);
            cabecera.appendChild(boton('pl-icono-boton pl-quitar', 'fa-solid fa-trash', 'Quitar pregunta', function () {
                preguntas.splice(indice, 1);
                dibujar();
            }));
            tarjeta.appendChild(cabecera);

            var cuerpo = elemento('div', { clase: 'pl-pregunta-cuerpo' });
            var contador = elemento('span', { clase: 'pn-contador', texto: pregunta.texto.length + ' / ' + LARGO_TEXTO });
            var enunciado = elemento('textarea', { clase: 'pn-input pl-enunciado', rows: '2', maxlength: LARGO_TEXTO,
                placeholder: 'Enunciado de la pregunta' });
            enunciado.value = pregunta.texto;
            enunciado.addEventListener('input', function () {
                pregunta.texto = enunciado.value;
                contador.textContent = enunciado.value.length + ' / ' + LARGO_TEXTO;
                actualizarTitulo();
                actualizarResumen();
            });
            cuerpo.appendChild(elemento('div', { clase: 'pn-etiqueta-fila' }, [
                elemento('label', { clase: 'pn-etiqueta', texto: 'Enunciado' }), contador]));
            cuerpo.appendChild(enunciado);

            var ayuda = pregunta.tipo === 'multiple'
                ? 'Marcá todas las opciones correctas (al menos una).'
                : 'Marcá la única opción correcta.';
            cuerpo.appendChild(elemento('p', { clase: 'pl-ayuda', texto: 'Opciones de respuesta. ' + ayuda }));
            pregunta.opciones.forEach(function (o, i) { cuerpo.appendChild(dibujarOpcion(pregunta, i)); });

            if (pregunta.tipo !== 'vf' && pregunta.opciones.length < MAXIMO_OPCIONES) {
                var agregar = elemento('button', { type: 'button', clase: 'pn-btn pn-btn-contorno pn-btn-chico' },
                    [icono('fa-solid fa-plus'), document.createTextNode(' Agregar opción')]);
                agregar.addEventListener('click', function () {
                    pregunta.opciones.push(nuevaOpcion());
                    dibujar();
                });
                cuerpo.appendChild(agregar);
            }
            tarjeta.appendChild(cuerpo);
            return tarjeta;
        }

        function dibujar() {
            lista.textContent = '';
            preguntas.forEach(function (pregunta, indice) { lista.appendChild(dibujarPregunta(pregunta, indice)); });
            if (!preguntas.length) {
                lista.appendChild(elemento('p', { clase: 'pl-vacio', texto: 'Todavía no cargaste ninguna pregunta.' }));
            }
            actualizarResumen();
        }

        // ------------------------------------------------------------ Inicio y envío

        (datos.preguntas || []).forEach(function (origen) { preguntas.push(nuevaPregunta(origen)); });
        if (!preguntas.length) {
            preguntas.push(nuevaPregunta());
        }
        dibujar();

        raiz.querySelectorAll('[data-agregar-pregunta]').forEach(function (agregar) {
            agregar.addEventListener('click', function () {
                preguntas.forEach(function (p) { p.abierta = false; });
                preguntas.push(nuevaPregunta());
                dibujar();
                var ultima = lista.lastElementChild;
                if (ultima && ultima.scrollIntoView) {
                    ultima.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
                }
            });
        });

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

    var editores = document.querySelectorAll('[data-pool-editor]');
    Array.prototype.forEach.call(editores, function (raiz, i) {
        raiz.dataset.uid = String(i);
        var formulario = raiz.closest('form');
        if (formulario && !formulario.id) { formulario.id = 'formPool' + i; }
        iniciar(raiz);
    });
})();
