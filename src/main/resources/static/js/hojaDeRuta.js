/**
 * Hoja de ruta de los formularios por pasos: el último paso es un resumen estructural en forma de árbol.
 *
 * Un formulario se suma con data-resumen="<nombre de lo que se registra>" y data-modo="alta|modificacion". Sus pasos
 * (.pn-paso) llevan data-grupo con el nombre de la rama del árbol; el último paso lleva [data-resumen-arbol]. Cada campo
 * (.pn-campo) aporta su etiqueta y su valor.
 *
 *  - Alta: el árbol muestra todos los datos cargados, agrupados por paso, con un "Editar" que vuelve a ese paso.
 *  - Modificación: muestra solo lo que cambió, como "antes -> después".
 *
 * Los campos con data-resumen-formato="moneda" se muestran como importe en pesos. Un elemento con
 * data-resumen-linea="Etiqueta" aporta su texto (o, en una casilla, si está confirmada) como una línea más del árbol.
 *
 * Contexto: data-contexto es una lista JSON con lo que está por encima de lo que se registra (por ejemplo, el curso y el
 * programa de una cohorte). Cada entrada lleva su etiqueta y su valor fijo, o bien el campo (select) de donde sale y el
 * atributo de la opción elegida que trae el valor:
 *   [{"etiqueta":"Curso","atributoForm":"data-ctx-curso"}, {"etiqueta":"Programa","campo":"programaId","atributo":"data-programa"}]
 * (atributoForm toma el valor de un atributo del propio formulario).
 * data-resumen-titulo agrega un texto a la línea de lo que se registra (por ejemplo, la fecha de una cohorte).
 */
(function () {
    'use strict';

    var VACIO = 'sin completar';

    function etiquetaDe(campo) {
        if (campo.hasAttribute('data-resumen-etiqueta')) { return campo.getAttribute('data-resumen-etiqueta'); }
        var rotulo = campo.querySelector('.pn-etiqueta');
        if (!rotulo) { return ''; }
        return rotulo.textContent.replace(/\*/g, '').replace(/\(opcional\)/i, '').replace(/\s+/g, ' ').trim();
    }

    function moneda(valor) {
        var n = parseFloat(valor);
        if (isNaN(n)) { return ''; }
        return '$ ' + n.toLocaleString('es-AR', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    }

    /** Lo que muestra un campo, como texto. */
    function valorDe(campo) {
        var control = campo.querySelector('select, input, textarea');
        if (!control) { return ''; }
        if (control.tagName === 'SELECT') {
            var elegidas = Array.prototype.filter.call(control.options, function (o) { return o.selected && o.value !== ''; })
                .map(function (o) { return o.textContent.trim(); }).join(', ');
            return elegidas || control.getAttribute('data-resumen-vacio') || '';
        }
        if (control.type === 'file') {
            if (control.files && control.files.length) { return control.files[0].name; }
            var vista = campo.querySelector('img[data-original]');
            return vista && vista.getAttribute('data-original') ? 'Imagen actual' : '';
        }
        var texto = (control.value || '').trim();
        if (control.type === 'date' && /^\d{4}-\d{2}-\d{2}$/.test(texto)) { return texto.split('-').reverse().join('/'); }
        if (control.type === 'datetime-local' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(texto)) {
            var partes = texto.split('T');
            return partes[0].split('-').reverse().join('/') + ' ' + partes[1].substring(0, 5);
        }
        return control.getAttribute('data-resumen-formato') === 'moneda' ? moneda(texto) : texto;
    }

    /** Una línea suelta del resumen: el texto del elemento o, si es una casilla, si está confirmada. */
    function lineaDe(elemento) {
        if (elemento.type === 'checkbox') { return elemento.checked ? 'confirmados' : 'sin confirmar'; }
        return (elemento.textContent || '').replace(/\s+/g, ' ').trim();
    }

    function leer(formulario) {
        var todos = Array.prototype.slice.call(formulario.querySelectorAll('.pn-paso'));
        return todos.filter(function (paso) { return paso.hasAttribute('data-grupo'); }).map(function (paso) {
            return {
                indice: todos.indexOf(paso),
                grupo: paso.getAttribute('data-grupo'),
                sinRama: paso.hasAttribute('data-sin-rama'),
                campos: Array.prototype.filter.call(paso.querySelectorAll('.pn-campo, [data-resumen-linea]'), function (campo) {
                    return !campo.closest('.d-none, [hidden]') && !campo.hasAttribute('data-resumen-omitir') && !(campo.matches('.pn-campo') && campo.querySelector('[data-resumen-linea]'));
                }).map(function (campo) {
                    if (campo.hasAttribute('data-resumen-linea')) { return { etiqueta: campo.getAttribute('data-resumen-linea'), valor: lineaDe(campo) }; }
                    return { etiqueta: etiquetaDe(campo), valor: valorDe(campo) };
                }).filter(function (c) { return c.etiqueta; })
            };
        });
    }

    function iniciar(formulario) {
        var contenedor = formulario.querySelector('[data-resumen-arbol]');
        var pasoResumen = contenedor && contenedor.closest('.pn-paso');
        if (!contenedor || !pasoResumen) { return; }
        var esAlta = formulario.getAttribute('data-modo') !== 'modificacion';
        var nombre = formulario.getAttribute('data-resumen') || 'Registro';
        var original = leer(formulario);
        var definicionContexto = [];
        try { definicionContexto = JSON.parse(formulario.getAttribute('data-contexto') || '[]'); } catch (e) { definicionContexto = []; }

        function contexto() {
            return definicionContexto.map(function (c) {
                var valor = c.valor || '';
                if (c.atributoForm) { valor = formulario.getAttribute(c.atributoForm) || ''; }
                if (c.campo) {
                    var select = formulario.querySelector('[name="' + c.campo + '"]');
                    var opcion = select && select.selectedOptions.length ? select.selectedOptions[0] : null;
                    valor = opcion && opcion.value !== '' ? (opcion.getAttribute(c.atributo) || '') : '';
                }
                return { etiqueta: c.etiqueta, valor: valor };
            }).filter(function (c) { return c.valor; });
        }

        function esc(t) {
            return String(t).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        }

        function crear(etiqueta, clase, html) {
            var e = document.createElement(etiqueta);
            if (clase) { e.className = clase; }
            if (html !== undefined) { e.innerHTML = html; }
            return e;
        }

        function editar(indice) {
            var boton = crear('button', 'pn-arbol-editar', '<i class="fa-solid fa-pen" aria-hidden="true"></i> Editar');
            boton.type = 'button';
            boton.addEventListener('click', function () { formulario.pasos.mostrar(indice); });
            return boton;
        }

        function fechaYHora(f) {
            function dos(n) { return (n < 10 ? '0' : '') + n; }
            return dos(f.getDate()) + '/' + dos(f.getMonth() + 1) + '/' + f.getFullYear() + ' ' + dos(f.getHours()) + ':' + dos(f.getMinutes());
        }

        function vacio() { return '<span class="pn-arbol-vacio">' + VACIO + '</span>'; }

        // Colores: curso (oro), programa (azul), los niveles siguientes alternan dorado y azul, lo que se registra (verde) y lo que se
        // modifica (naranja). El color de lo que se registra o modifica lo da el nodo, y el de su tipo, su badge.
        // Los niveles del árbol alternan dorado y azul de arriba hacia abajo (curso, programa, cohorte, inscripción...), así que
        // una rama nueva más abajo sigue el mismo patrón
        function colorDeNivel(i) { return i % 2 === 0 ? 'oro' : 'azul'; }


        /** Un nodo del árbol: línea vertical con codo, punto del color del nivel, título y badges. Los hijos van dentro. */
        function nodo(color, titulo, tipo, estado, extra) {
            var n = crear('div', 'pn-nodo pn-nodo-' + color);
            var fila = crear('div', 'pn-nodo-fila');
            fila.appendChild(crear('span', 'pn-punto'));
            fila.appendChild(crear('span', 'pn-nodo-titulo', titulo));
            if (tipo) { fila.appendChild(crear('span', 'pn-chip pn-chip-' + tipo[0], esc(tipo[1]))); }
            if (estado) { fila.appendChild(crear('span', 'pn-chip pn-chip-estado pn-chip-' + estado[0], esc(estado[1]))); }
            if (extra) { fila.appendChild(extra); }
            n.appendChild(fila);
            return n;
        }

        /** La leyenda trae solo los colores que aparecen en este árbol: los niveles de contexto, el de lo que se registra y el del estado. */
        function leyenda(contextos) {
            var items = [];
            contextos.forEach(function (c, i) { items.push([colorDeNivel(i), c.etiqueta]); });
            items.push([colorDeNivel(contextos.length), nombre]);
            items.push(esAlta ? ['verde', 'Lo que se registra'] : ['naranja', 'Lo que se modifica']);
            var l = crear('div', 'pn-arbol-leyenda');
            items.forEach(function (e) {
                l.appendChild(crear('span', 'pn-leyenda-item pn-nodo-' + e[0], '<i></i>' + esc(e[1])));
            });
            return l;
        }

        function dibujar() {
            var actual = leer(formulario);
            contenedor.innerHTML = '';

            // En una modificación se muestran los grupos con cambios y, dentro de ellos, todos sus datos
            var ramas = actual.map(function (grupo, i) {
                if (grupo.sinRama) { return null; }
                var campos = grupo.campos.map(function (c) {
                    var anterior = original[i] ? original[i].campos.filter(function (o) { return o.etiqueta === c.etiqueta; })[0] : null;
                    var antes = anterior ? anterior.valor : '';
                    return { etiqueta: c.etiqueta, valor: c.valor, antes: antes, cambio: c.valor !== antes };
                });
                return { indice: grupo.indice, grupo: grupo.grupo, campos: campos };
            }).filter(function (r) { return r && r.campos.length && (esAlta || r.campos.some(function (c) { return c.cambio; })); });
            var cambios = ramas.reduce(function (total, r) {
                return total + r.campos.filter(function (c) { return c.cambio; }).length;
            }, 0);

            // Contexto: de dónde cuelga lo que se registra (curso, programa), como ramas principales
            var contextos = contexto();
            contenedor.appendChild(leyenda(contextos));
            var raiz = contenedor;
            contextos.forEach(function (c, i) {
                var color = colorDeNivel(i);
                var rama = nodo(color, esc(c.valor), [color, c.etiqueta]);
                rama.classList.add(i === 0 ? 'pn-nodo-primero' : 'pn-nodo-segundo');
                raiz.appendChild(rama);
                raiz = rama;
            });

            // Lo que se registra o se modifica
            // El título de lo que se registra no repite el badge de su tipo: es su nombre (curso, programa, unidad, material,
            // descuento), o, si no tiene, la fecha y hora en que se registra (cohorte, inscripción, pago) o su dato propio
            var sufijo = formulario.getAttribute('data-resumen-titulo');
            var campoNombre = formulario.querySelector('[name="nombre"], [name="titulo"]');
            var titulo;
            if (campoNombre && !campoNombre.disabled && campoNombre.value.trim()) {
                titulo = esc(campoNombre.value.trim());
            } else if (sufijo) {
                titulo = esc(nombre + ' ' + sufijo.replace(/^\(|\)$/g, ''));
            } else {
                titulo = esc(nombre + ' ' + fechaYHora(new Date()));
            }
            var estado = esAlta
                ? ['verde', formulario.getAttribute('data-resumen-nuevo') === 'nueva' ? 'Nueva' : 'Nuevo']
                : (cambios ? ['naranja', cambios + (cambios === 1 ? ' cambio' : ' cambios')] : ['gris', 'Sin cambios']);
            var colorEntidad = colorDeNivel(contextos.length);
            var entidad = nodo(colorEntidad, titulo, [colorEntidad, nombre], estado);
            entidad.classList.add(contextos.length ? 'pn-nodo-tercero' : 'pn-nodo-primero');
            raiz.appendChild(entidad);

            if (!ramas.length) {
                entidad.appendChild(crear('p', 'pn-arbol-sin-cambios', 'No hay cambios para guardar'));
                return;
            }
            ramas.forEach(function (rama) {
                var grupo = nodo(esAlta ? 'verde' : 'naranja', esc(rama.grupo), null, null, esAlta ? editar(rama.indice) : null);
                grupo.classList.add('pn-nodo-grupo');
                rama.campos.forEach(function (c) {
                    var valor = c.valor ? '<strong>' + esc(c.valor) + '</strong>' : vacio();
                    var clase = 'pn-arbol-dato';
                    var texto;
                    if (esAlta) {
                        if (c.valor) { clase += ' pn-dato-nuevo'; }
                        texto = valor;
                    } else if (c.cambio) {
                        clase += ' pn-dato-cambio';
                        texto = '<span class="pn-arbol-antes">' + (c.antes ? esc(c.antes) : VACIO) + '</span><span class="pn-arbol-flecha">→</span>' + valor;
                    } else {
                        clase += ' pn-dato-igual';
                        texto = '<span class="pn-arbol-antes">' + (c.valor ? esc(c.valor) : VACIO) + '</span>';
                    }
                    grupo.appendChild(crear('div', clase, esc(c.etiqueta) + ': ' + texto));
                });
                entidad.appendChild(grupo);
            });
        }

        // El árbol se arma cada vez que el paso de resumen pasa a estar visible
        new MutationObserver(function () {
            if (pasoResumen.classList.contains('activo')) { dibujar(); }
        }).observe(pasoResumen, { attributes: true, attributeFilter: ['class'] });
    }

    function arrancar() { document.querySelectorAll('form[data-resumen]').forEach(iniciar); }
    if (document.readyState === 'loading') { document.addEventListener('DOMContentLoaded', arrancar); } else { arrancar(); }
})();
