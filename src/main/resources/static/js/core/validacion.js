/**
 * Formularios: validación de campos, formularios por pasos, contadores de caracteres, vista previa de imagen y
 * selectores múltiples.
 *
 * Las páginas pueden registrar validaciones propias de un formulario en window.PanelValidadores[nombre]
 * (el nombre es el atributo data-validador del formulario): reciben el número de paso (desde 1) y el formulario,
 * y devuelven true si el paso es válido.
 */
(function () {
    'use strict';

    window.PanelValidadores = window.PanelValidadores || {};

    var IMAGEN_TIPOS = ['image/jpeg', 'image/png', 'image/webp'];
    var IMAGEN_TAMANIO_MAXIMO = 2 * 1024 * 1024;

    function contenedorDe(campo) {
        return campo.closest('.pn-campo') || campo.parentNode;
    }

    function ayudaDe(campo) {
        var contenedor = contenedorDe(campo);
        var ayuda = contenedor.querySelector('.pn-ayuda-error');
        if (!ayuda) {
            ayuda = document.createElement('div');
            ayuda.className = 'pn-ayuda-error';
            contenedor.appendChild(ayuda);
        }
        return ayuda;
    }

    /** Muestra (o limpia, si el mensaje está vacío) el error de un campo. Devuelve true si el campo es válido. */
    function marcar(campo, mensaje) {
        campo.classList.toggle('es-invalido', !!mensaje);
        ayudaDe(campo).textContent = mensaje || '';
        return !mensaje;
    }
    window.PanelValidacion = { marcar: marcar };

    function validarCampo(campo) {
        if (campo.matches(':disabled') || !campo.name || campo.type === 'hidden') {
            return true;
        }
        // Una casilla obligatoria (por ejemplo, los términos y condiciones) debe estar marcada
        if (campo.type === 'checkbox') {
            if (campo.required && !campo.checked) {
                return marcar(campo, campo.dataset.mensajeObligatorio || 'Este campo es obligatorio.');
            }
            return marcar(campo, '');
        }
        // Los campos ocultos (por ejemplo, las fechas de dictado de un curso grabado) no se validan
        if (campo.closest('[hidden], .d-none')) {
            return marcar(campo, '');
        }
        var valor = (campo.value || '').trim();
        if (campo.tagName === 'SELECT' && campo.multiple) {
            if (campo.required && campo.selectedOptions.length === 0) {
                return marcar(campo, campo.dataset.mensajeObligatorio || 'Debe seleccionar al menos una opción.');
            }
            return marcar(campo, '');
        }
        if (campo.type === 'file' && campo.dataset.extensiones) {
            // Archivo de material: formatos y tamaño máximo vienen del servidor (data-extensiones y data-tamanio-mb)
            var adjunto = campo.files && campo.files[0];
            if (adjunto) {
                var extension = adjunto.name.indexOf('.') >= 0 ? adjunto.name.split('.').pop().toLowerCase() : '';
                var permitidas = campo.dataset.extensiones.split(',');
                if (permitidas.indexOf(extension) === -1) {
                    campo.value = '';
                    return marcar(campo, 'El archivo no es válido. Formato válido: ' + permitidas.join(', ').toUpperCase() + '.');
                }
                if (adjunto.size > parseFloat(campo.dataset.tamanioMb) * 1024 * 1024) {
                    campo.value = '';
                    return marcar(campo, 'El archivo es demasiado grande. Máximo: ' + campo.dataset.tamanioMb + ' MB.');
                }
            } else if (campo.required) {
                return marcar(campo, 'Debe seleccionar un archivo.');
            }
            return marcar(campo, '');
        }
        if (campo.type === 'file') {
            var archivo = campo.files && campo.files[0];
            if (archivo && IMAGEN_TIPOS.indexOf(archivo.type) === -1) {
                campo.value = '';
                return marcar(campo, 'La imagen no es válida. Formato válido: JPG, PNG o WebP.');
            }
            if (archivo && archivo.size > IMAGEN_TAMANIO_MAXIMO) {
                campo.value = '';
                return marcar(campo, 'La imagen es demasiado grande. Máximo: 2 MB.');
            }
            return marcar(campo, '');
        }
        if (campo.required && !valor) {
            return marcar(campo, campo.dataset.mensajeObligatorio || 'Este campo es obligatorio.');
        }
        if (campo.maxLength > 0 && valor.length > campo.maxLength) {
            return marcar(campo, 'Es demasiado largo. Máximo: ' + campo.maxLength + ' caracteres.');
        }
        if (campo.type === 'number' && valor !== '') {
            var numero = parseFloat(valor);
            if (isNaN(numero)) { return marcar(campo, 'No es válido. Formato válido: número.'); }
            if (campo.min !== '' && numero < parseFloat(campo.min)) {
                return marcar(campo, 'No es válido. Formato válido: número mayor o igual a ' + campo.min + '.');
            }
            if (campo.max !== '' && numero > parseFloat(campo.max)) {
                return marcar(campo, 'No es válido. Formato válido: número menor o igual a ' + parseFloat(campo.max).toLocaleString('es-AR') + '.');
            }
        }
        return marcar(campo, '');
    }

    function validarContenedor(contenedor, formulario, paso) {
        var valido = true;
        contenedor.querySelectorAll('input, select, textarea').forEach(function (campo) {
            if (!validarCampo(campo)) { valido = false; }
        });
        var validador = window.PanelValidadores[formulario.dataset.validador];
        if (validador && !validador(paso, formulario)) { valido = false; }
        return valido;
    }

    // Contadores de caracteres
    function actualizarContador(campo) {
        var contador = contenedorDe(campo).querySelector('.pn-contador');
        if (!contador || !campo.maxLength || campo.maxLength < 0) { return; }
        contador.textContent = campo.value.length + ' / ' + campo.maxLength;
    }
    document.addEventListener('input', function (e) {
        if (e.target.matches('[data-contador]')) {
            actualizarContador(e.target);
        }
        if (e.target.closest('form[data-validar]') && e.target.classList.contains('es-invalido')) {
            validarCampo(e.target);
        }
    });
    document.querySelectorAll('[data-contador]').forEach(actualizarContador);
    document.addEventListener('change', function (e) {
        if (e.target.closest('form[data-validar]') && e.target.classList.contains('es-invalido')) {
            validarCampo(e.target);
        }
    });

    // Vista previa de la imagen elegida (si se quita o es inválida, vuelve la original o se oculta)
    document.addEventListener('change', function (e) {
        var entrada = e.target;
        if (!entrada.matches || !entrada.matches('input.imagen-curso')) { return; }
        var valida = validarCampo(entrada);
        var previa = contenedorDe(entrada).querySelector('.pn-vista-previa');
        if (!previa) { return; }
        if (previa.dataset.urlTemporal) {
            URL.revokeObjectURL(previa.dataset.urlTemporal);
            delete previa.dataset.urlTemporal;
        }
        var archivo = valida && entrada.files && entrada.files[0];
        if (archivo) {
            var url = URL.createObjectURL(archivo);
            previa.dataset.urlTemporal = url;
            previa.src = url;
            previa.classList.remove('d-none');
        } else if (previa.dataset.original) {
            previa.src = previa.dataset.original;
            previa.classList.remove('d-none');
        } else {
            previa.removeAttribute('src');
            previa.classList.add('d-none');
        }
    });

    // Selectores múltiples (Tom Select)
    function iniciarMultiselect(contenedor) {
        if (!window.TomSelect) { return; }
        contenedor.querySelectorAll('select.ts-multiselect:not(.tomselected)').forEach(function (el) {
            new TomSelect(el, {
                // En una ventana el menú se dibuja sobre la página y no dentro de ella (así el modal no lo recorta); en los filtros
                // queda pegado al campo
                dropdownParent: el.closest('.pn-modal') ? 'body' : null,
                plugins: ['remove_button', 'checkbox_options'],
                placeholder: el.getAttribute('placeholder'),
                closeAfterSelect: false,
                hideSelected: false,
                maxOptions: null,
                // En las ventanas por pasos el selector puede recibir el foco mientras su paso no se muestra: el menú se
                // abriría sin tamaño ni lugar y después no se podría abrir. Si el campo no está a la vista, no se abre.
                onDropdownOpen: function () {
                    if (!this.control.offsetWidth) { this.close(); }
                }
            });
        });
    }
    window.iniciarMultiselect = iniciarMultiselect;
    // Tom Select se carga después de este script (al final de la página): se arma apenas termina de leerse el documento, no en
    // un momento ocioso, para que los selectores no se vean cargarse tarde
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () { iniciarMultiselect(document); });
    } else {
        iniciarMultiselect(document);
    }
    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('show.bs.modal', function () { iniciarMultiselect(modal); });
    });

    // Formularios por pasos. Un paso puede ocultarse (atributo hidden en el .pn-paso): se salta al avanzar y retroceder, y su punto del
    // indicador desaparece. El que lo oculta o lo muestra llama a formulario.pasos.refrescar().
    function iniciarPasos(formulario) {
        var pasos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-paso'));
        var items = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-item'));
        var puntos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-punto'));
        var lineas = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-linea'));
        var anterior = formulario.querySelector('[data-paso-anterior]');
        var siguiente = formulario.querySelector('[data-paso-siguiente]');
        var guardarBtn = formulario.querySelector('[data-paso-guardar]');
        var actual = 0;

        function visible(i) { return !pasos[i].hidden; }
        function posicion(i) { var n = 0; for (var k = 0; k < i; k++) { if (visible(k)) { n++; } } return n; }
        function ultimoVisible() { for (var i = pasos.length - 1; i >= 0; i--) { if (visible(i)) { return i; } } return 0; }
        function siguienteVisible(i) { for (var k = i + 1; k < pasos.length; k++) { if (visible(k)) { return k; } } return i; }
        function anteriorVisible(i) { for (var k = i - 1; k >= 0; k--) { if (visible(k)) { return k; } } return i; }
        function hayVisibleDespues(i) { return siguienteVisible(i) !== i; }

        function mostrar(indice) {
            actual = Math.min(Math.max(indice, 0), pasos.length - 1);
            if (!visible(actual)) { actual = hayVisibleDespues(actual) ? siguienteVisible(actual) : anteriorVisible(actual); }
            var posActual = posicion(actual);
            pasos.forEach(function (p, i) { p.classList.toggle('activo', i === actual); });
            puntos.forEach(function (p, i) {
                var visibleI = visible(i);
                var completo = visibleI && posicion(i) < posActual;
                if (items[i]) { items[i].classList.toggle('d-none', !visibleI); }
                p.classList.toggle('activo', i === actual);
                p.classList.toggle('completo', completo);
                p.innerHTML = completo ? '<i class="fa-solid fa-check"></i>' : String(posicion(i) + 1);
            });
            lineas.forEach(function (l, i) {
                l.classList.toggle('d-none', !(visible(i) && hayVisibleDespues(i)));
                l.classList.toggle('completa', visible(i) && posicion(i) < posActual);
            });
            // Los menús de los selectores que quedaron abiertos en otro paso se cierran
            formulario.querySelectorAll('select.tomselected').forEach(function (s) { if (s.tomselect) { s.tomselect.close(); } });
            var ultimo = ultimoVisible();
            if (anterior) { anterior.style.visibility = posActual === 0 ? 'hidden' : 'visible'; }
            if (siguiente) { siguiente.classList.toggle('d-none', actual === ultimo); }
            if (guardarBtn) { guardarBtn.classList.toggle('d-none', actual !== ultimo); }
        }

        formulario.pasos = {
            mostrar: mostrar, cantidad: pasos.length, actual: function () { return actual; },
            refrescar: function () { mostrar(actual); }
        };

        if (anterior) { anterior.addEventListener('click', function () { mostrar(anteriorVisible(actual)); }); }
        if (siguiente) {
            siguiente.addEventListener('click', function () {
                if (validarContenedor(pasos[actual], formulario, actual + 1)) { mostrar(siguienteVisible(actual)); }
            });
        }
        // Enter dentro de un campo avanza al paso siguiente en lugar de enviar el formulario incompleto
        formulario.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && actual < ultimoVisible() && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                if (siguiente) { siguiente.click(); }
            }
        });
        mostrar(0);
    }
    document.querySelectorAll('form[data-pasos]').forEach(iniciarPasos);
    window.PanelIniciarPasos = iniciarPasos; // para formularios que llegan después de la carga (core/modalModificar.js)

    // Cuando el servidor rechaza un formulario por pasos, se vuelve al paso del campo al que se refiere el error y se
    // marca ese campo. El servidor responde con un mensaje, así que el campo se deduce de las palabras de su etiqueta.
    var PALABRAS_VACIAS = ['de', 'del', 'la', 'el', 'los', 'las', 'y', 'o', 'en', 'con', 'al', 'por', 'para', 'una', 'que', 'sus', 'debe', 'error'];

    function palabras(texto) {
        return (texto || '').toString().normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
            .split(/[^a-z0-9]+/).filter(function (p) { return p.length >= 3 && PALABRAS_VACIAS.indexOf(p) < 0; });
    }

    /**
     * Lleva al paso del campo al que se refiere un error del servidor y lo marca con el mensaje. El servidor indica el
     * campo por su nombre; si no lo indica, se deduce de las palabras de las etiquetas. Devuelve true si marcó un campo.
     */
    window.PanelValidacion.irAlError = function (formulario, mensaje, nombreCampo) {
        var mejor = null;
        if (nombreCampo) {
            var elegido = formulario.querySelector('[name="' + nombreCampo + '"]');
            if (elegido && elegido.type !== 'hidden' && !elegido.disabled) { mejor = elegido; }
        }
        if (!mejor) {
            var enMensaje = palabras(mensaje);
            var mejorPuntaje = 0;
            formulario.querySelectorAll('.pn-campo').forEach(function (campo) {
                var control = campo.querySelector('input:not([type="hidden"]), select, textarea');
                var etiqueta = campo.querySelector('.pn-etiqueta');
                if (!control || !etiqueta || control.disabled || campo.closest('[hidden], .d-none')) { return; }
                var clave = palabras(etiqueta.textContent.replace(/\(.*?\)/g, ''));
                if (!clave.length) { return; }
                var coinciden = clave.filter(function (p) { return enMensaje.indexOf(p) >= 0; });
                // todas las palabras de la etiqueta (la coincidencia más precisa) o, si no, la palabra más larga que coincida
                var puntaje = coinciden.length === clave.length ? 100 + clave.length
                    : coinciden.reduce(function (m, p) { return p.length >= 5 ? Math.max(m, p.length) : m; }, 0);
                // a igual puntaje gana el campo cuyas palabras aparecen antes en el mensaje (el sujeto de la frase)
                var posicion = coinciden.length ? Math.min.apply(null, coinciden.map(function (p) { return enMensaje.indexOf(p); })) : 999;
                puntaje = puntaje * 1000 - posicion;
                if (puntaje > mejorPuntaje) { mejorPuntaje = puntaje; mejor = control; }
            });
        }
        if (!mejor) { return false; }
        var paso = mejor.closest('.pn-paso');
        if (formulario.pasos && paso) {
            var indice = Array.prototype.indexOf.call(formulario.querySelectorAll('.pn-paso'), paso);
            if (indice >= 0) { formulario.pasos.mostrar(indice); }
        }
        window.PanelValidacion.marcar(mejor, (mensaje || '').replace(/^Error!\s*/, ''));
        if (mejor.focus && mejor.offsetParent !== null) { mejor.focus(); }
        return true;
    };

    // Al abrir una ventana modal con formulario por pasos, vuelve al primer paso
    document.addEventListener('show.bs.modal', function (e) {
        e.target.querySelectorAll('form[data-pasos]').forEach(function (f) {
            if (f.pasos) { f.pasos.mostrar(0); }
        });
    });

    // Validación al enviar (en captura: se ejecuta antes que el envío por fetch de core/formulario.js)
    document.addEventListener('submit', function (e) {
        var formulario = e.target;
        if (!formulario.matches || !formulario.matches('form[data-validar]')) { return; }
        var pasos = formulario.querySelectorAll('.pn-paso');
        var valido = true;
        if (pasos.length) {
            for (var i = 0; i < pasos.length; i++) {
                if (!validarContenedor(pasos[i], formulario, i + 1)) {
                    if (valido) { formulario.pasos.mostrar(i); }
                    valido = false;
                }
            }
        } else {
            valido = validarContenedor(formulario, formulario, 1);
        }
        if (!valido) {
            e.preventDefault();
            e.stopImmediatePropagation();
            var primero = formulario.querySelector('.es-invalido');
            if (primero && primero.focus && primero.offsetParent !== null) { primero.focus(); }
        }
    }, true);
})();
