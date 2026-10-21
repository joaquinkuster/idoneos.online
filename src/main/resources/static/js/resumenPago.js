/**
 * Recuadro con el detalle del pago de una inscripción, para los formularios del administrador (registrar inscripción y
 * registrar pago). Muestra el precio del curso, el descuento aplicado, lo ya pagado (si hay) y el total a pagar.
 *
 * El recuadro es el elemento con el atributo data-caja; adentro, los elementos con data-caja-* se completan con los datos.
 */
(function () {
    'use strict';

    function formato(numero) {
        return '$ ' + numero.toLocaleString('es-AR', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
    }

    function elemento(caja, nombre) {
        return caja.querySelector('[data-caja-' + nombre + ']');
    }

    /**
     * Completa y muestra el recuadro.
     * @param caja  El elemento con data-caja.
     * @param datos precio, descuentoNombre, descuentoPorcentaje, descuento (monto), pagado, total y nota (opcional).
     */
    function mostrar(caja, datos) {
        var gratis = datos.precio <= 0;
        elemento(caja, 'precio').textContent = gratis ? 'Gratis' : formato(datos.precio);

        var filaDescuento = elemento(caja, 'descuento-fila');
        filaDescuento.hidden = !(datos.descuento > 0);
        if (datos.descuento > 0) {
            elemento(caja, 'descuento-etiqueta').textContent = 'Descuento' + (datos.descuentoNombre ? ' «' + datos.descuentoNombre + '»' : '')
                + ' (' + datos.descuentoPorcentaje.toLocaleString('es-AR') + '%)';
            elemento(caja, 'descuento').textContent = '- ' + formato(datos.descuento);
        }

        var filaPagado = elemento(caja, 'pagado-fila');
        if (filaPagado) {
            filaPagado.hidden = !(datos.pagado > 0);
            if (datos.pagado > 0) { elemento(caja, 'pagado').textContent = '- ' + formato(datos.pagado); }
        }

        elemento(caja, 'total-fila').hidden = gratis;
        elemento(caja, 'total').textContent = formato(datos.total);

        var nota = elemento(caja, 'nota');
        nota.hidden = !datos.nota;
        nota.textContent = datos.nota || '';
        caja.hidden = false;
    }

    function ocultar(caja) {
        caja.hidden = true;
    }

    window.ResumenPago = { mostrar: mostrar, ocultar: ocultar };

    // Los recuadros que traen sus datos en atributos (por ejemplo, el de Modificar pago) se completan al cargar la página
    document.querySelectorAll('[data-caja][data-precio]').forEach(function (caja) {
        mostrar(caja, {
            precio: parseFloat(caja.getAttribute('data-precio')) || 0,
            descuentoNombre: caja.getAttribute('data-desc-nombre'),
            descuentoPorcentaje: parseFloat(caja.getAttribute('data-desc-pct')) || 0,
            descuento: parseFloat(caja.getAttribute('data-desc-monto')) || 0,
            pagado: parseFloat(caja.getAttribute('data-pagado')) || 0,
            total: parseFloat(caja.getAttribute('data-total')) || 0
        });
    });
})();
