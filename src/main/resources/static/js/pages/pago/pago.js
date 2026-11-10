/** Pago online de una inscripción con MODO y reintento de un pago rechazado. */
(function () {
    'use strict';

    var INTERVALO_MS = 3000;
    var URL_RETORNO = '/inscripcion/buscar';

    function enviar(url, metodo) {
        return fetch(url, { method: metodo || 'POST', headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
            .then(function (respuesta) {
                return respuesta.json().catch(function () { return {}; }).then(function (datos) {
                    return { ok: respuesta.ok, datos: datos };
                });
            })
            .catch(function () {
                return { ok: false, datos: { error: 'Error! No se pudo conectar con el servidor. Intentá nuevamente.' } };
            });
    }

    /** Mientras se prepara el pago o se espera a que se haga. */
    function mostrarEtiqueta(boton, texto) {
        var etiqueta = boton.parentNode.querySelector('[data-pago-etiqueta]');
        if (!etiqueta) {
            etiqueta = document.createElement('span');
            etiqueta.setAttribute('data-pago-etiqueta', '');
            etiqueta.className = 'pn-celda-secundaria' + (boton.classList.contains('caja-boton') ? ' caja-verificando' : '');
            boton.parentNode.insertBefore(etiqueta, boton.nextSibling);
        }
        etiqueta.innerHTML = texto + '<span class="pago-puntos" aria-hidden="true"><i>.</i><i>.</i><i>.</i></span>';
        boton.hidden = true;
    }

    function esperando(boton, activo) {
        if (!boton) { return; }
        if (activo) {
            mostrarEtiqueta(boton, 'Preparando el pago');
        } else {
            var etiqueta = boton.parentNode.querySelector('[data-pago-etiqueta]');
            if (etiqueta) { etiqueta.remove(); }
            boton.hidden = false;
        }
    }

    function pagoAprobado(sinPago) {
        var mensaje = sinPago
            ? 'Tu descuento cubre todo el precio: no hay nada que pagar. Se habilitó el acceso al curso.'
            : '¡Pago aprobado! Se habilitó el acceso al curso y se emitió tu comprobante.';
        return window.mostrarExito(mensaje).then(function () { window.location.href = URL_RETORNO; });
    }

    /** Espera el pago hecho en la pestaña de MODO. */
    function seguirPago(boton, pestania, idPago) {
        mostrarEtiqueta(boton, 'Esperando el pago');
        var consulta = setInterval(function () {
            if (pestania.closed) {
                // Cerró la pestaña de MODO: puede haber pagado justo antes, así que se consulta una vez más antes de rendirse
                clearInterval(consulta);
                enviar('/pago/estado/' + idPago, 'GET').then(function (r) {
                    var estado = r.ok ? r.datos.estado : null;
                    if (estado === 'Aprobado') { window.location.href = '/pago/exito/' + idPago; } else { esperando(boton, false); }
                });
                return;
            }
            enviar('/pago/estado/' + idPago, 'GET').then(function (r) {
                if (!r.ok) { return; }
                if (r.datos.estado === 'Aprobado' || r.datos.estado === 'Rechazado') {
                    clearInterval(consulta);
                    window.location.href = '/pago/' + (r.datos.estado === 'Aprobado' ? 'exito' : 'error') + '/' + idPago;
                }
            });
        }, INTERVALO_MS);
    }

    function iniciar(boton) {
        var url = boton.getAttribute('data-url');
        var monto = boton.getAttribute('data-monto');
        if (monto) { url += (url.indexOf('?') < 0 ? '?' : '&') + 'monto=' + encodeURIComponent(monto); }
        // La pestaña se abre ya, en el clic: abrirla después de la respuesta del servidor haría que el navegador la bloquee
        var pestania = window.open('', '_blank');
        esperando(boton, true);
        return enviar(url).then(function (r) {
            if (!r.ok) {
                if (pestania) { pestania.close(); }
                esperando(boton, false);
                return window.mostrarAviso({ tipo: 'error', titulo: r.datos.recargar ? 'El monto cambió' : 'No se pudo iniciar el pago',
                    texto: r.datos.error || 'Intentá nuevamente.' }).then(function () {
                    // Con el monto cambiado, la caja se recarga para mostrar el total actual
                    if (r.datos.recargar) { window.location.reload(); }
                });
            }
            var datos = r.datos;
            // Si no hay nada que cobrar, el botón sigue en espera hasta que la página cambia
            if (datos.aprobado) {
                if (pestania) { pestania.close(); }
                return pagoAprobado(datos.idPago === null || datos.idPago === undefined);
            }
            if (!datos.urlCheckout) {
                if (pestania) { pestania.close(); }
                esperando(boton, false);
                return window.mostrarAviso({ tipo: 'error', titulo: 'No se pudo iniciar el pago', texto: 'Intentá nuevamente en unos minutos.' });
            }
            // El pago se completa en el sitio de MODO, en la pestaña nueva
            if (!pestania) {
                // El navegador bloqueó la pestaña: se sigue en esta, como antes
                window.location.href = datos.urlCheckout;
                return;
            }
            pestania.opener = null;
            pestania.location.href = datos.urlCheckout;
            seguirPago(boton, pestania, datos.idPago);
        });
    }

    document.addEventListener('click', function (evento) {
        var boton = evento.target.closest ? evento.target.closest('[data-pagar]') : null;
        if (!boton) { return; }
        evento.preventDefault();
        iniciar(boton);
    });

    // Página de retorno con el pago todavía pendiente: se consulta hasta que se resuelva
    var pendiente = document.querySelector('[data-pago-pendiente]');
    if (pendiente) {
        var idPago = pendiente.getAttribute('data-pago-pendiente');
        var limite = Date.now() + ((parseInt(pendiente.getAttribute('data-plazo-minutos'), 10) || 10) + 1) * 60 * 1000;
        var consulta = setInterval(function () {
            enviar('/pago/estado/' + idPago, 'GET').then(function (r) {
                if (!r.ok) { return; }
                var estado = r.datos.estado;
                if (estado === 'Aprobado' || estado === 'Rechazado' || Date.now() > limite) {
                    clearInterval(consulta);
                    // El servidor decide a qué página corresponde el resultado
                    window.location.href = '/pago/' + (estado === 'Aprobado' ? 'exito' : 'error') + '/' + idPago;
                }
            });
        }, INTERVALO_MS);
    }
})();
