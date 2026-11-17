/** Usuarios: pasos del perfil docente según los roles, títulos y matrículas, resumen y validación. */
(function () {
    'use strict';

    var FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    var FORMATO_TELEFONO = /^[0-9+()\s-]{6,20}$/;
    var FORMATO_MATRICULA = /^[A-Za-z0-9]{1,20}$/;
    var FORMATO_MATRICULA_IDONEO = /^[0-9]{1,8}$/;

    function marcar(campo, mensaje) {
        if (window.PanelValidacion && window.PanelValidacion.marcar) { window.PanelValidacion.marcar(campo, mensaje); }
    }

    function recortar(texto, maximo) {
        return texto.length > maximo ? texto.substring(0, maximo - 3).trimEnd() + '...' : texto;
    }

    // ----------------------------------------------------------------------
    // Estado del formulario
    // ----------------------------------------------------------------------

    function rolesMarcados(formulario) {
        var selector = formulario.querySelector('select[name="roles"]');
        return selector ? Array.prototype.map.call(selector.selectedOptions, function (opcion) { return opcion.value; }) : [];
    }

    /** Muestra u oculta los pasos del docente y deja al día los textos del resumen. */
    function actualizar(formulario) {
        var roles = rolesMarcados(formulario);
        formulario.querySelectorAll('[data-paso-docente]').forEach(function (paso) { paso.hidden = roles.indexOf('Docente') < 0; });
        if (formulario.pasos && formulario.pasos.refrescar) { formulario.pasos.refrescar(); }
    }

    function iniciar(contenedor) {
        contenedor.querySelectorAll('form[data-validador="usuario"]').forEach(function (formulario) {
            iniciarTitulos(formulario);
            actualizar(formulario);
        });
    }

    document.addEventListener('change', function (e) {
        var formulario = e.target.closest ? e.target.closest('form[data-validador="usuario"]') : null;
        if (formulario) { actualizar(formulario); }
    });

    // Los formularios de las ventanas que se piden al servidor se inician cuando llegan
    document.addEventListener('ventana-cargada', function (e) { iniciar(e.detail.ventana); });

    // ----------------------------------------------------------------------
    // Títulos y matrículas (mismo diseño que la ventana de temas)
    // ----------------------------------------------------------------------

    function iniciarTitulos(formulario) {
        var contenedorItems = formulario.querySelector('[data-titulos-items]');
        if (!contenedorItems || formulario.dataset.titulosListo) { return; }
        formulario.dataset.titulosListo = '1';

        var vacio = formulario.querySelector('[data-titulos-empty]');
        var cantidad = formulario.querySelector('[data-titulos-cantidad]');
        var ocultos = formulario.querySelector('[data-titulos-ocultos]');
        var cabecera = formulario.querySelector('[data-titulo-formulario]');
        var botonNuevo = formulario.querySelector('[data-titulo-nuevo]');
        var botonAplicar = formulario.querySelector('[data-titulo-aplicar]');
        var botonDescartar = formulario.querySelector('[data-titulo-descartar]');
        var botonQuitar = formulario.querySelector('[data-titulo-quitar]');
        var entradas = {
            titulo: formulario.querySelector('[data-titulo-campo="titulo"]'),
            matricula: formulario.querySelector('[data-titulo-campo="matricula"]')
        };
        var resumen = formulario.querySelector('[data-resumen-titulos]');

        var titulos = []; // { titulo, matricula, original, nuevo, boton }
        var seleccionado = null;

        function cambiado(item) {
            return item.nuevo || item.titulo !== item.original.titulo || item.matricula !== item.original.matricula;
        }

        function leerFormulario() {
            return { titulo: entradas.titulo.value.trim(), matricula: entradas.matricula.value.trim() };
        }

        function escribir(campo, valor) {
            entradas[campo].value = valor || '';
            entradas[campo].dispatchEvent(new Event('input', { bubbles: true }));
            marcar(entradas[campo], '');
        }

        /** Si lo escrito es distinto del título elegido (o, en uno nuevo, si hay algo escrito). */
        function hayCambiosSinAplicar() {
            var escrito = leerFormulario();
            if (!seleccionado) { return escrito.titulo !== '' || escrito.matricula !== ''; }
            return escrito.titulo !== seleccionado.titulo || escrito.matricula !== seleccionado.matricula;
        }

        /** Deja los datos que viajan al servidor y el texto del resumen al día con la lista. */
        function sincronizar() {
            ocultos.innerHTML = '';
            titulos.forEach(function (item) {
                [['titulo', item.titulo], ['matricula', item.matricula]].forEach(function (par) {
                    var campo = document.createElement('input');
                    campo.type = 'hidden';
                    campo.name = par[0];
                    campo.value = par[1];
                    ocultos.appendChild(campo);
                });
            });
            if (resumen) {
                resumen.textContent = titulos.map(function (item) {
                    return item.matricula ? item.titulo + ' (matrícula ' + item.matricula + ')' : item.titulo;
                }).join(', ') || 'sin títulos';
            }
        }

        function dibujarLista() {
            titulos.forEach(function (item) {
                item.boton.classList.toggle('activo', item === seleccionado);
                var chip = item.boton.querySelector('[data-estado]');
                var estado = item.nuevo ? 'Nuevo' : (cambiado(item) ? 'Modificado' : '');
                chip.textContent = estado;
                chip.classList.toggle('d-none', !estado);
                chip.classList.toggle('pn-chip-verde', !!estado);
                var elTitulo = item.boton.querySelector('.pn-tema-item-titulo');
                var elDescripcion = item.boton.querySelector('.pn-tema-item-desc');
                elTitulo.textContent = recortar(item.titulo, 30);
                elTitulo.title = item.titulo;
                elDescripcion.textContent = item.matricula ? 'Matrícula ' + item.matricula : 'Sin matrícula';
            });
            vacio.hidden = titulos.length > 0;
            cantidad.textContent = titulos.length;
            sincronizar();
        }

        function elegir(item) {
            seleccionado = item;
            escribir('titulo', item ? item.titulo : '');
            escribir('matricula', item ? item.matricula : '');
            cabecera.textContent = item ? 'Editar título' : 'Agregar nuevo título';
            botonNuevo.hidden = !item;
            botonAplicar.textContent = item ? 'Editar' : 'Agregar';
            botonDescartar.hidden = !item || item.nuevo || !cambiado(item);
            botonQuitar.hidden = !item;
            dibujarLista();
        }

        function crearBoton(item) {
            var boton = document.createElement('div');
            boton.className = 'pn-tema-item';
            boton.setAttribute('role', 'button');
            boton.tabIndex = 0;
            boton.innerHTML = '<div class="pn-tema-item-cuerpo"><span class="pn-tema-item-titulo"></span><span class="pn-tema-item-desc"></span></div>'
                + '<span class="pn-chip d-none" data-estado></span>';
            conectar(item, boton);
            contenedorItems.appendChild(boton);
        }

        function conectar(item, boton) {
            item.boton = boton;
            boton.addEventListener('click', function () { intentarElegir(item); });
            boton.addEventListener('keydown', function (e) {
                if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); intentarElegir(item); }
            });
        }

        /** Valida lo escrito y lo aplica al título elegido (o lo agrega como nuevo). */
        function aplicar() {
            var escrito = leerFormulario();
            if (!escrito.titulo) {
                marcar(entradas.titulo, 'Debe completar el título.');
                return false;
            }
            var repetido = titulos.some(function (otro) {
                return otro !== seleccionado && otro.titulo.toLowerCase() === escrito.titulo.toLowerCase();
            });
            if (repetido) {
                marcar(entradas.titulo, 'Ya cargaste ese título.');
                return false;
            }
            if (escrito.matricula && !FORMATO_MATRICULA.test(escrito.matricula)) {
                marcar(entradas.matricula, 'No es válida. Formato válido: letras y números, por ejemplo 3591, LE26 o LA200.');
                return false;
            }
            marcar(entradas.titulo, '');
            marcar(entradas.matricula, '');
            if (seleccionado) {
                seleccionado.titulo = escrito.titulo;
                seleccionado.matricula = escrito.matricula;
                elegir(seleccionado);
            } else {
                var nuevo = { titulo: escrito.titulo, matricula: escrito.matricula, original: {}, nuevo: true };
                titulos.push(nuevo);
                crearBoton(nuevo);
                elegir(null);
            }
            return true;
        }

        /** Cambia de título. Si había cambios sin aplicar los aplica antes (y si no son válidos, se queda donde está). */
        function intentarElegir(item) {
            if (hayCambiosSinAplicar() && !aplicar()) { return; }
            elegir(item);
        }

        contenedorItems.querySelectorAll('.pn-tema-item').forEach(function (boton) {
            var item = { titulo: boton.dataset.titulo || '', matricula: boton.dataset.matricula || '', nuevo: false };
            item.original = { titulo: item.titulo, matricula: item.matricula };
            conectar(item, boton);
            titulos.push(item);
        });

        botonNuevo.addEventListener('click', function () { intentarElegir(null); });
        botonAplicar.addEventListener('click', function () { aplicar(); });
        botonDescartar.addEventListener('click', function () {
            if (!seleccionado) { return; }
            seleccionado.titulo = seleccionado.original.titulo;
            seleccionado.matricula = seleccionado.original.matricula;
            elegir(seleccionado);
        });
        botonQuitar.addEventListener('click', function () {
            if (!seleccionado) { return; }
            seleccionado.boton.remove();
            titulos.splice(titulos.indexOf(seleccionado), 1);
            elegir(null);
            actualizar(formulario);
        });
        entradas.titulo.addEventListener('input', function () { marcar(entradas.titulo, ''); });
        entradas.matricula.addEventListener('input', function () { marcar(entradas.matricula, ''); });

        // Las pasos del formulario piden estas dos cosas al validar el paso de títulos
        formulario.titulosApi = {
            aplicarPendiente: function () { return !hayCambiosSinAplicar() || aplicar(); },
            cantidad: function () { return titulos.length; },
            marcarVacio: function (mensaje) { marcar(entradas.titulo, mensaje); }
        };
        elegir(null);
    }

    iniciar(document);

    // ----------------------------------------------------------------------
    // Validación
    // ----------------------------------------------------------------------

    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.usuario = function (paso, formulario) {
        var valido = true;
        var campos = formulario.elements;
        var esDocente = rolesMarcados(formulario).indexOf('Docente') >= 0;

        if (paso === 1) {
            if (campos.correo.value.trim() && !FORMATO_CORREO.test(campos.correo.value.trim())) {
                marcar(campos.correo, 'No es válido. Formato válido: nombre@dominio.com.');
                valido = false;
            }
            if (campos.dni.value.trim() && !/^[0-9]{7,8}$/.test(campos.dni.value.trim())) {
                marcar(campos.dni, 'No es válido. Formato válido: 7 u 8 dígitos.');
                valido = false;
            }
            if (campos.telefono.value.trim() && !FORMATO_TELEFONO.test(campos.telefono.value.trim())) {
                marcar(campos.telefono, 'No es válido. Formato válido: entre 6 y 20 dígitos, con +, espacios o guiones.');
                valido = false;
            }
        }

        if (paso === 3 && esDocente && campos.matriculaCnv.value.trim() && !FORMATO_MATRICULA_IDONEO.test(campos.matriculaCnv.value.trim())) {
            marcar(campos.matriculaCnv, 'No es válida. Formato válido: solo números, por ejemplo 984.');
            valido = false;
        }

        if (paso === 4 && esDocente && formulario.titulosApi) {
            // Lo que quedó escrito y sin agregar se agrega antes de comprobar que haya al menos un título o una matrícula
            if (!formulario.titulosApi.aplicarPendiente()) {
                valido = false;
            } else if (formulario.titulosApi.cantidad() === 0 && !campos.matriculaCnv.value.trim()) {
                formulario.titulosApi.marcarVacio('Declará al menos un título o una matrícula de idóneo.');
                valido = false;
            }
        }
        return valido;
    };
})();
