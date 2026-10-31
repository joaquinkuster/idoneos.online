/**
 * Duración escrita en días, semanas o meses, o en una combinación de las tres (opción "Mixto"), de fragments/campoDuracion.html.
 *
 * El campo tiene un total oculto (el que se envía al servidor, en días) y, para escribir, una cantidad con su unidad o, en
 * "Mixto", una casilla para meses, otra para semanas y otra para días. Al cambiar de unidad el valor se convierte: con
 * "Mixto" se reparte en meses, semanas y días; al volver a una sola unidad se redondea. Un mes son 30 días y una semana 7.
 * El total guarda también su texto ("1 mes, 2 semanas y 3 días") para el resumen del formulario.
 */
(function () {
    'use strict';

    var DIAS_POR_MES = 30;
    var DIAS_POR_SEMANA = 7;

    function plural(cantidad, singular, plural) {
        return cantidad + ' ' + (cantidad === 1 ? singular : plural);
    }

    /** Mismo formato que DuracionUtilidad.formatear en el servidor: la unidad más grande que expresa la duración exacta. */
    function texto(dias) {
        if (!dias) { return ''; }
        if (dias % DIAS_POR_MES === 0) { return plural(dias / DIAS_POR_MES, 'mes', 'meses'); }
        if (dias % DIAS_POR_SEMANA === 0) { return plural(dias / DIAS_POR_SEMANA, 'semana', 'semanas'); }
        return plural(dias, 'día', 'días');
    }

    /** Una duración escrita en partes ("Mixto"): "1 mes, 2 semanas y 3 días". */
    function textoMixto(dias) {
        if (!dias) { return ''; }
        var meses = Math.floor(dias / DIAS_POR_MES);
        var semanas = Math.floor((dias % DIAS_POR_MES) / DIAS_POR_SEMANA);
        var sueltos = (dias % DIAS_POR_MES) % DIAS_POR_SEMANA;
        var partes = [];
        if (meses) { partes.push(plural(meses, 'mes', 'meses')); }
        if (semanas) { partes.push(plural(semanas, 'semana', 'semanas')); }
        if (sueltos) { partes.push(plural(sueltos, 'día', 'días')); }
        return partes.length > 1 ? partes.slice(0, -1).join(', ') + ' y ' + partes[partes.length - 1] : partes[0];
    }

    function entero(valor) {
        return Math.max(0, parseInt(valor, 10) || 0);
    }

    function iniciar(contenedor) {
        var total = contenedor.querySelector('[data-tiempo-total]');
        var cantidad = contenedor.querySelector('[data-duracion-cantidad]');
        var unidad = contenedor.querySelector('[data-duracion-unidad]');
        var simple = contenedor.querySelector('[data-duracion-simple]');
        var mixto = contenedor.querySelector('[data-duracion-mixto]');
        var partes = Array.prototype.slice.call(contenedor.querySelectorAll('[data-duracion-parte]'));
        var campo = contenedor.closest('.pn-campo');

        function esMixto() { return unidad.value === '0'; }

        function diasActuales() { return entero(total.value); }

        /** Recalcula el total (en días) y su texto a partir de lo escrito. */
        function calcular() {
            var dias;
            if (esMixto()) {
                dias = partes.reduce(function (suma, parte) { return suma + entero(parte.value) * parseInt(parte.dataset.duracionParte, 10); }, 0);
            } else {
                dias = entero(cantidad.value) * parseInt(unidad.value, 10);
            }
            total.value = dias ? String(dias) : '';
            total.dataset.texto = esMixto() ? textoMixto(dias) : texto(dias);
            // Las pantallas que muestran un cálculo con esta duración se enteran por este evento
            total.dispatchEvent(new CustomEvent('duracion:cambio', { bubbles: true }));
        }

        /** Muestra las casillas que corresponden a la unidad elegida; el campo ocupa todo el ancho si es mixto. */
        function mostrar() {
            var m = esMixto();
            simple.hidden = m;
            mixto.hidden = !m;
            if (campo) {
                campo.classList.toggle('col-12', m);
                campo.classList.toggle('col-md-6', !m);
            }
        }

        /** Escribe en las casillas los días que tiene el total, en la unidad elegida. */
        function volcar(dias) {
            if (esMixto()) {
                var restantes = dias;
                partes.forEach(function (parte) {
                    var factor = parseInt(parte.dataset.duracionParte, 10);
                    var valor = Math.floor(restantes / factor);
                    restantes -= valor * factor;
                    parte.value = valor ? String(valor) : '';
                });
            } else {
                var factorUnico = parseInt(unidad.value, 10);
                cantidad.value = dias ? String(Math.max(1, Math.round(dias / factorUnico))) : '';
            }
        }

        // Estado inicial: el total guardado, en la unidad más grande que lo expresa exacto
        var inicial = diasActuales();
        unidad.value = inicial && inicial % DIAS_POR_MES === 0 ? '30' : (inicial && inicial % DIAS_POR_SEMANA === 0 ? '7' : (inicial ? '1' : '7'));
        mostrar();
        volcar(inicial);
        calcular();

        function alEscribir() {
            calcular();
            // Si el campo tenía un error, se limpia al escribir
            if (window.PanelValidacion && total.classList.contains('es-invalido')) { window.PanelValidacion.marcar(total, ''); }
        }

        cantidad.addEventListener('input', alEscribir);
        partes.forEach(function (parte) { parte.addEventListener('input', alEscribir); });
        unidad.addEventListener('change', function () {
            var dias = diasActuales();
            mostrar();
            volcar(dias);
            calcular();
        });
        // Al salir de una casilla, el valor se muestra normalizado (por ejemplo, 9 días mixtos pasan a 1 semana y 2 días)
        contenedor.addEventListener('change', function (e) {
            if (e.target === unidad) { return; }
            if (esMixto()) { volcar(diasActuales()); }
        });
    }

    window.DuracionTexto = texto;
    document.querySelectorAll('[data-duracion]').forEach(iniciar);
})();
