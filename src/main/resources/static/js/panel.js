/**
 * Comportamiento del panel del administrador.
 *
 *  1. Menú lateral: contraer/expandir y apertura en pantallas chicas.
 *  2. Listados en tabla: selección de filas, columnas visibles, filas por página y paginador.
 *  3. Filtros: se aplican solos al cambiar un campo.
 *  4. Formularios: validación de campos, formularios por pasos, vista previa de imagen y selectores múltiples.
 *
 * Las páginas pueden registrar validaciones propias de un formulario en window.PanelValidadores[nombre]
 * (el nombre es el atributo data-validador del formulario): reciben el número de paso (desde 1) y el formulario,
 * y devuelven true si el paso es válido.
 */
(function () {
    'use strict';

    var CLAVE_MENU = 'idoneos.panel.menuColapsado';
    var CLAVE_FOCO = 'idoneos.panel.focoFiltro';
    window.PanelValidadores = window.PanelValidadores || {};

    function guardar(clave, valor) {
        try { window.localStorage.setItem(clave, valor); } catch (e) { /* almacenamiento no disponible */ }
    }
    function leer(clave) {
        try { return window.localStorage.getItem(clave); } catch (e) { return null; }
    }

    // ------------------------------------------------------------------
    // 1. Menú lateral
    // ------------------------------------------------------------------
    var app = document.getElementById('pnApp');
    if (app) {
        if (leer(CLAVE_MENU) === '1') {
            app.classList.add('colapsado');
        }
        document.addEventListener('click', function (e) {
            if (e.target.closest('[data-colapsar-menu]')) {
                var colapsado = app.classList.toggle('colapsado');
                guardar(CLAVE_MENU, colapsado ? '1' : '0');
            } else if (e.target.closest('[data-abrir-menu]')) {
                app.classList.add('menu-abierto');
            } else if (e.target.closest('[data-cerrar-menu]')) {
                app.classList.remove('menu-abierto');
            }
        });
    }

    // ------------------------------------------------------------------
    // 2. Listados en tabla
    // ------------------------------------------------------------------
    function iniciarTabla(raiz) {
        var filas = Array.prototype.slice.call(raiz.querySelectorAll('tbody tr[data-fila]'));
        var selectPorPagina = raiz.querySelector('[data-por-pagina]');
        var infoRango = raiz.querySelector('[data-rango]');
        var contador = raiz.querySelector('[data-seleccionados]');
        var paginador = raiz.querySelector('[data-paginador]');
        var seleccionarTodos = raiz.querySelector('[data-seleccionar-todos]');
        var vacio = raiz.querySelector('[data-vacio]');
        var pagina = 1;
        var botonBaja = raiz.querySelector('[data-baja-masiva]');
        var porPagina = selectPorPagina ? parseInt(selectPorPagina.value, 10) : 10;

        // El valor 0 de "filas por página" significa "todo"
        function tamanio() {
            return porPagina > 0 ? porPagina : Math.max(filas.length, 1);
        }

        function totalPaginas() {
            return Math.max(1, Math.ceil(filas.length / tamanio()));
        }

        function actualizarSeleccion() {
            var marcadas = filas.filter(function (f) {
                var c = f.querySelector('[data-seleccionar-fila]');
                return c && c.checked;
            }).length;
            if (contador) {
                contador.textContent = marcadas + ' de ' + filas.length + ' seleccionados';
            }
            if (botonBaja) {
                botonBaja.classList.toggle('d-none', marcadas === 0);
            }
            if (seleccionarTodos) {
                var visibles = filas.filter(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    return !f.hidden && c && !c.disabled;
                });
                seleccionarTodos.checked = visibles.length > 0 && visibles.every(function (f) {
                    return f.querySelector('[data-seleccionar-fila]').checked;
                });
            }
        }

        function dibujar() {
            var paginas = totalPaginas();
            pagina = Math.min(Math.max(pagina, 1), paginas);
            var inicio = (pagina - 1) * tamanio();
            filas.forEach(function (fila, i) {
                fila.hidden = !(i >= inicio && i < inicio + tamanio());
            });
            if (vacio) {
                vacio.hidden = filas.length > 0;
            }
            if (infoRango) {
                var desde = filas.length ? inicio + 1 : 0;
                infoRango.textContent = desde + '-' + Math.min(inicio + tamanio(), filas.length) + ' de ' + filas.length;
            }
            if (paginador) {
                paginador.innerHTML = '';
                [['«', 1, pagina === 1, 'Primera página'], ['‹', pagina - 1, pagina === 1, 'Página anterior']].forEach(function (b) {
                    paginador.appendChild(boton(b));
                });
                var texto = document.createElement('span');
                texto.textContent = 'Página ' + pagina + ' de ' + paginas;
                paginador.appendChild(texto);
                [['›', pagina + 1, pagina === paginas, 'Página siguiente'], ['»', paginas, pagina === paginas, 'Última página']].forEach(function (b) {
                    paginador.appendChild(boton(b));
                });
            }
            actualizarSeleccion();
        }

        function boton(datos) {
            var b = document.createElement('button');
            b.type = 'button';
            b.textContent = datos[0];
            b.disabled = datos[2];
            b.title = datos[3];
            b.setAttribute('aria-label', datos[3]);
            b.addEventListener('click', function () { pagina = datos[1]; dibujar(); });
            return b;
        }

        if (selectPorPagina) {
            selectPorPagina.addEventListener('change', function () {
                porPagina = parseInt(selectPorPagina.value, 10);
                pagina = 1;
                dibujar();
            });
        }

        if (seleccionarTodos) {
            seleccionarTodos.addEventListener('change', function () {
                filas.forEach(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    if (!f.hidden && c && !c.disabled) {
                        c.checked = seleccionarTodos.checked;
                    }
                });
                actualizarSeleccion();
            });
        }
        raiz.addEventListener('change', function (e) {
            if (e.target.matches('[data-seleccionar-fila]')) {
                actualizarSeleccion();
            }
        });

        // Baja masiva de los elementos seleccionados (todos o ninguno)
        if (botonBaja) {
            botonBaja.addEventListener('click', async function () {
                var marcadas = filas.filter(function (f) {
                    var c = f.querySelector('[data-seleccionar-fila]');
                    return c && c.checked;
                });
                var ids = marcadas.map(function (f) { return f.querySelector('[data-seleccionar-fila]').value; });
                var dependencias = marcadas.reduce(function (total, f) { return total + (parseInt(f.dataset.dependencias, 10) || 0); }, 0);
                if (!ids.length || !(await window.confirmarBajaMasiva({
                    cantidad: ids.length,
                    elementos: botonBaja.dataset.elementos,
                    dependencias: dependencias,
                    terminoUno: botonBaja.dataset.terminoUno,
                    terminoVarios: botonBaja.dataset.terminoVarios
                }))) { return; }
                var datos = new FormData();
                ids.forEach(function (id) { datos.append('ids', id); });
                botonBaja.disabled = true;
                try {
                    var respuesta = await fetch(botonBaja.dataset.url, {
                        method: 'POST',
                        body: datos,
                        headers: { 'Accept': 'application/json' },
                        credentials: 'same-origin'
                    });
                    var resultado = await respuesta.json();
                    if (respuesta.ok && resultado.mensaje) {
                        await window.mostrarExito(resultado.mensaje);
                        window.location.reload();
                        return;
                    }
                    await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intente nuevamente.');
                } catch (error) {
                    await window.mostrarError('Error! No se pudo conectar con el servidor. Intente nuevamente.');
                }
                botonBaja.disabled = false;
            });
        }

        // Columnas visibles
        var menuColumnas = raiz.querySelector('[data-columnas-menu]');
        var cabeceras = Array.prototype.slice.call(raiz.querySelectorAll('thead th'));
        if (menuColumnas) {
            cabeceras.forEach(function (th, indice) {
                if (!th.hasAttribute('data-col')) { return; }
                var etiqueta = document.createElement('label');
                var casilla = document.createElement('input');
                casilla.type = 'checkbox';
                casilla.checked = true;
                casilla.addEventListener('change', function () {
                    var visible = casilla.checked;
                    th.style.display = visible ? '' : 'none';
                    raiz.querySelectorAll('tbody tr[data-fila]').forEach(function (fila) {
                        if (fila.children[indice]) { fila.children[indice].style.display = visible ? '' : 'none'; }
                    });
                });
                etiqueta.appendChild(casilla);
                etiqueta.appendChild(document.createTextNode(th.getAttribute('data-col')));
                menuColumnas.appendChild(etiqueta);
            });
        }

        dibujar();
    }

    document.querySelectorAll('[data-tabla]').forEach(iniciarTabla);

    document.addEventListener('click', function (e) {
        var boton = e.target.closest('[data-columnas-boton]');
        document.querySelectorAll('.pn-columnas-wrapper.abierto').forEach(function (w) {
            if (!boton || !w.contains(boton)) { w.classList.remove('abierto'); }
        });
        if (boton) {
            var wrapper = boton.closest('.pn-columnas-wrapper');
            var abierto = wrapper.classList.toggle('abierto');
            if (abierto) {
                // Si el menú se saliera por el borde izquierdo del contenido (tapando el menú lateral), se alinea a la izquierda
                var menu = wrapper.querySelector('.pn-columnas-menu');
                menu.classList.remove('alinear-izquierda');
                var principal = document.querySelector('.pn-principal');
                var limite = principal ? principal.getBoundingClientRect().left : 0;
                if (menu.getBoundingClientRect().left < limite) {
                    menu.classList.add('alinear-izquierda');
                }
            }
        }
    });

    // ------------------------------------------------------------------
    // 3. Filtros automáticos
    // ------------------------------------------------------------------
    document.querySelectorAll('form[data-filtros]').forEach(function (form) {
        var temporizador = null;
        form.addEventListener('change', function (e) {
            if (e.target.matches('select, input[type="date"]')) { form.submit(); }
        });
        form.addEventListener('input', function (e) {
            if (e.target.matches('input[type="text"], input[type="search"]')) {
                clearTimeout(temporizador);
                temporizador = setTimeout(function () {
                    try { window.sessionStorage.setItem(CLAVE_FOCO, e.target.name); } catch (err) { /* sin almacenamiento */ }
                    form.submit();
                }, 450);
            }
        });
        // Devuelve el foco al campo de texto que se estaba escribiendo
        try {
            var nombre = window.sessionStorage.getItem(CLAVE_FOCO);
            if (nombre) {
                window.sessionStorage.removeItem(CLAVE_FOCO);
                var campo = form.querySelector('[name="' + nombre + '"]');
                if (campo) {
                    campo.focus();
                    campo.setSelectionRange(campo.value.length, campo.value.length);
                }
            }
        } catch (err) { /* sin almacenamiento */ }
    });

    // ------------------------------------------------------------------
    // 4. Formularios: validación, pasos, vista previa de imagen y selectores múltiples
    // ------------------------------------------------------------------
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
        if (campo.matches(':disabled') || !campo.name || campo.type === 'hidden' || campo.type === 'checkbox') {
            return true;
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
        if (campo.type === 'file') {
            var archivo = campo.files && campo.files[0];
            if (archivo && IMAGEN_TIPOS.indexOf(archivo.type) === -1) {
                campo.value = '';
                return marcar(campo, 'La imagen debe estar en formato JPG, PNG o WebP.');
            }
            if (archivo && archivo.size > IMAGEN_TAMANIO_MAXIMO) {
                campo.value = '';
                return marcar(campo, 'La imagen no puede superar los 2 MB.');
            }
            return marcar(campo, '');
        }
        if (campo.required && !valor) {
            return marcar(campo, campo.dataset.mensajeObligatorio || 'Este campo es obligatorio.');
        }
        if (campo.maxLength > 0 && valor.length > campo.maxLength) {
            return marcar(campo, 'No puede superar los ' + campo.maxLength + ' caracteres.');
        }
        if (campo.type === 'number' && valor !== '') {
            var numero = parseFloat(valor);
            if (isNaN(numero)) { return marcar(campo, 'Ingrese un número válido.'); }
            if (campo.min !== '' && numero < parseFloat(campo.min)) {
                return marcar(campo, 'Debe ser mayor o igual a ' + campo.min + '.');
            }
            if (campo.max !== '' && numero > parseFloat(campo.max)) {
                return marcar(campo, 'No puede superar ' + parseFloat(campo.max).toLocaleString('es-AR') + '.');
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
                plugins: ['remove_button', 'checkbox_options'],
                placeholder: el.getAttribute('placeholder'),
                closeAfterSelect: false,
                hideSelected: false,
                maxOptions: null
            });
        });
    }
    if ('requestIdleCallback' in window) {
        requestIdleCallback(function () { iniciarMultiselect(document); }, { timeout: 1500 });
    } else {
        setTimeout(function () { iniciarMultiselect(document); }, 300);
    }
    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('show.bs.modal', function () { iniciarMultiselect(modal); });
    });

    // Formularios por pasos
    function iniciarPasos(formulario) {
        var pasos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-paso'));
        var puntos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-punto'));
        var lineas = Array.prototype.slice.call(formulario.querySelectorAll('.pn-pasos-linea'));
        var barra = formulario.querySelector('.pn-progreso-barra');
        var anterior = formulario.querySelector('[data-paso-anterior]');
        var siguiente = formulario.querySelector('[data-paso-siguiente]');
        var guardarBtn = formulario.querySelector('[data-paso-guardar]');
        var actual = 0;

        function mostrar(indice) {
            actual = Math.min(Math.max(indice, 0), pasos.length - 1);
            pasos.forEach(function (p, i) { p.classList.toggle('activo', i === actual); });
            puntos.forEach(function (p, i) {
                p.classList.toggle('activo', i === actual);
                p.classList.toggle('completo', i < actual);
                p.innerHTML = i < actual ? '<i class="fa-solid fa-check"></i>' : String(i + 1);
            });
            lineas.forEach(function (l, i) { l.classList.toggle('completa', i < actual); });
            if (barra) { barra.style.width = ((actual + 1) / pasos.length * 100) + '%'; }
            if (anterior) { anterior.style.visibility = actual === 0 ? 'hidden' : 'visible'; }
            if (siguiente) { siguiente.classList.toggle('d-none', actual === pasos.length - 1); }
            if (guardarBtn) { guardarBtn.classList.toggle('d-none', actual !== pasos.length - 1); }
        }

        formulario.pasos = { mostrar: mostrar, cantidad: pasos.length, actual: function () { return actual; } };

        if (anterior) { anterior.addEventListener('click', function () { mostrar(actual - 1); }); }
        if (siguiente) {
            siguiente.addEventListener('click', function () {
                if (validarContenedor(pasos[actual], formulario, actual + 1)) { mostrar(actual + 1); }
            });
        }
        // Enter dentro de un campo avanza al paso siguiente en lugar de enviar el formulario incompleto
        formulario.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' && actual < pasos.length - 1 && e.target.tagName !== 'TEXTAREA') {
                e.preventDefault();
                if (siguiente) { siguiente.click(); }
            }
        });
        mostrar(0);
    }
    document.querySelectorAll('form[data-pasos]').forEach(iniciarPasos);

    // Al abrir una ventana modal con formulario por pasos, vuelve al primer paso
    document.addEventListener('show.bs.modal', function (e) {
        e.target.querySelectorAll('form[data-pasos]').forEach(function (f) {
            if (f.pasos) { f.pasos.mostrar(0); }
        });
    });

    // Validación al enviar (en captura: se ejecuta antes que el envío por fetch de formularios.js)
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
