/**
 * Pago online de una inscripción con MODO (CU-51) y reintento de un pago rechazado (CU-52).
 *
 * Los botones con el atributo data-pagar inician el pago:
 *  - data-url: la ruta que lo inicia ("/pago/pagar/{idInscripcion}" o "/pago/reintentar/{idPago}").
 *  - data-monto: el monto que el alumno vio en la caja. Se envía al servidor, que no cobra si el monto cambió (por ejemplo,
 *    porque el descuento aplicado dejó de estar disponible) y pide recargar la caja.
 *
 * El servidor registra el pago como pendiente, crea la solicitud en MODO y responde con la dirección del sitio de pagos de
 * MODO, que se abre en una pestaña nueva (se abre en el clic, antes de pedir el pago, para que el navegador no la bloquee).
 * El pago se hace en el sitio de MODO y MODO lo devuelve a /pago/exito/{id} o /pago/error/{id}. Mientras tanto, la pestaña
 * original consulta el estado del pago y avanza sola cuando se resuelve; si se cierra la pestaña de MODO sin pagar, el botón
 * se habilita de nuevo.
 *
 * La página de retorno con un pago que todavía figura pendiente (data-pago-pendiente) consulta su estado cada pocos
 * segundos: MODO avisa el resultado por su cuenta y el servidor también lo consulta, así que el pago se confirma aunque el
 * aviso demore.
 */
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
                return { ok: false, datos: { error: 'Error! No se pudo conectar con el servidor. Intente nuevamente.' } };
            });
    }

    /** Mientras se prepara el pago, el propio botón muestra la espera (sin cartel encima) y no admite otro clic. */
    function esperando(boton, activo) {
        if (!boton) { return; }
        if (activo) {
            boton.dataset.textoOriginal = boton.textContent;
            boton.disabled = true;
            boton.classList.add('cargando');
            boton.innerHTML = '<span class="pago-giro" aria-hidden="true"></span>Preparando el pago…';
        } else {
            boton.disabled = false;
            boton.classList.remove('cargando');
            if (boton.dataset.textoOriginal !== undefined) { boton.textContent = boton.dataset.textoOriginal; }
        }
    }

    function pagoAprobado(sinPago) {
        var mensaje = sinPago
            ? 'Tu descuento cubre todo el precio: no hay nada que pagar. Se habilitó el acceso al curso.'
            : '¡Pago aprobado! Se habilitó el acceso al curso y se emitió tu comprobante.';
        return window.mostrarExito(mensaje).then(function () { window.location.href = URL_RETORNO; });
    }

    /** Espera el pago hecho en la pestaña de MODO: cuando se resuelve (o se cierra esa pestaña) la página original reacciona. */
    function seguirPago(boton, pestania, idPago) {
        boton.dataset.textoOriginal = boton.dataset.textoOriginal || boton.textContent;
        boton.disabled = true;
        boton.classList.add('cargando');
        boton.innerHTML = '<span class="pago-giro" aria-hidden="true"></span>Esperando el pago en MODO…';
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
                    texto: r.datos.error || 'Intente nuevamente.' }).then(function () {
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
                return window.mostrarAviso({ tipo: 'error', titulo: 'No se pudo iniciar el pago', texto: 'Intente nuevamente en unos minutos.' });
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
