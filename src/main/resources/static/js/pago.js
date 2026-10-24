/**
 * Pago online de una inscripción con MODO (CU-51) y reintento de un pago rechazado (CU-52).
 *
 * Los botones con el atributo data-pagar inician el pago:
 *  - data-url: la ruta que lo inicia ("/pago/pagar/{idInscripcion}" o "/pago/reintentar/{idPago}").
 *  - data-inscripcion: la inscripción que se paga (el modal de MODO la usa para generar un QR nuevo si el pago falla).
 *  - data-monto: el monto que el alumno vio en la caja. Se envía al servidor, que no cobra si el monto cambió (por ejemplo,
 *    porque el descuento aplicado dejó de estar disponible) y pide recargar la caja.
 *
 * El servidor registra el pago como pendiente y crea la solicitud en MODO. Con MODO se muestra su modal (QR en la
 * computadora, selector de apps en el celular); Mientras tanto se
 * consulta el estado del pago cada pocos segundos: MODO avisa el resultado por su cuenta y el servidor también lo
 * consulta, así que el pago se confirma aunque el aviso no llegue.
 */
(function () {
    'use strict';

    var INTERVALO_MS = 3000;
    var URL_RETORNO = '/inscripcion/misCursos';
    var consulta = null; // temporizador de la consulta del estado

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

    function cargarScript(origen) {
        return new Promise(function (resolver, rechazar) {
            if (window.ModoSDK) { resolver(); return; }
            var script = document.createElement('script');
            script.src = origen;
            script.onload = resolver;
            script.onerror = function () { rechazar(new Error('No se pudo cargar el modal de MODO')); };
            document.head.appendChild(script);
        });
    }

    function detenerConsulta() {
        if (consulta) { clearInterval(consulta); consulta = null; }
    }

    /** Consulta el estado del pago hasta que se resuelva o venza el plazo; luego llama a alResolver con el resultado. */
    function esperarResultado(idPago, plazoMinutos, alResolver) {
        detenerConsulta();
        var limite = Date.now() + (plazoMinutos + 1) * 60 * 1000;
        consulta = setInterval(function () {
            enviar('/pago/estado/' + idPago, 'GET').then(function (r) {
                if (!r.ok) { return; }
                var estado = r.datos.estado;
                if (estado === 'Aprobado' || estado === 'Rechazado') {
                    detenerConsulta();
                    alResolver(r.datos);
                } else if (Date.now() > limite) {
                    detenerConsulta();
                    alResolver({ estado: 'Rechazado', motivo: 'El pago no se confirmó dentro del plazo.' });
                }
            });
        }, INTERVALO_MS);
    }

    function pagoAprobado(sinPago) {
        if (window.Swal) { window.Swal.close(); }
        var mensaje = sinPago
            ? 'Tu descuento cubre todo el precio: no hay nada que pagar. Se habilitó el acceso al curso.'
            : '¡Pago aprobado! Se habilitó el acceso al curso y se emitió tu comprobante.';
        return window.mostrarExito(mensaje)
            .then(function () { window.location.href = URL_RETORNO; });
    }

    function pagoRechazado(datos, reintentar) {
        return window.mostrarAviso({
            tipo: 'error',
            titulo: 'El pago fue rechazado',
            texto: (datos.motivo || 'No se pudo completar el pago.') + ' Podés reintentarlo.',
            confirmar: 'Reintentar',
            cancelar: 'Cerrar'
        }).then(function (resultado) {
            if (resultado.isConfirmed) { reintentar(); } else { window.location.reload(); }
        });
    }

    /** Muestra el modal de MODO con el QR de la solicitud. */
    function mostrarModalDeModo(datos, urlNueva) {
        var resolver = function (resultado) {
            if (resultado.estado === 'Aprobado') { pagoAprobado(); } else {
                pagoRechazado(resultado, function () { iniciar(urlNueva, urlNueva); });
            }
        };
        return cargarScript(datos.scriptModal).then(function () {
            var retorno = window.location.origin + URL_RETORNO;
            var actual = datos;
            window.ModoSDK.modoInitPayment({
                version: '2',
                qrString: datos.qr,
                checkoutId: datos.idSolicitud,
                deeplink: { url: datos.deeplink, callbackURL: retorno, callbackURLSuccess: retorno },
                callbackURL: retorno,
                // Si el pago falla, el modal pide un QR nuevo: se genera una intención de cobro nueva (con otro identificador)
                refreshData: function () {
                    return enviar(urlNueva).then(function (r) {
                        if (!r.ok) { throw new Error(r.datos.error || 'No se pudo generar un nuevo QR'); }
                        actual = r.datos;
                        esperarResultado(actual.idPago, actual.plazoMinutos, resolver);
                        return { checkoutId: actual.idSolicitud, qrString: actual.qr, deeplink: actual.deeplink };
                    });
                },
                onSuccess: function () { /* el resultado lo confirma la consulta del estado */ },
                onFailure: function () { /* idem: el modal ofrece un QR nuevo */ },
                onCancel: detenerConsulta,
                onClose: detenerConsulta
            });
            esperarResultado(datos.idPago, datos.plazoMinutos, resolver);
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

    function iniciar(url, urlNueva, boton) {
        // Un reintento que sale del aviso de pago rechazado no tiene botón propio: se usa el de la página
        boton = boton || document.querySelector('[data-pagar]');
        esperando(boton, true);
        return enviar(url).then(function (r) {
            if (!r.ok) {
                esperando(boton, false);
                return window.mostrarAviso({ tipo: 'error', titulo: r.datos.recargar ? 'El monto cambió' : 'No se pudo iniciar el pago',
                    texto: r.datos.error || 'Intente nuevamente.' }).then(function () {
                    // Con el monto cambiado, la caja se recarga para mostrar el total actual
                    if (r.datos.recargar) { window.location.reload(); }
                });
            }
            var datos = r.datos;
            // Si no hay nada que cobrar, el botón sigue en espera hasta que la página cambia
            if (datos.aprobado) { return pagoAprobado(datos.idPago === null || datos.idPago === undefined); }
            return mostrarModalDeModo(datos, urlNueva).then(function () {
                esperando(boton, false);
            }).catch(function (error) {
                esperando(boton, false);
                return window.mostrarAviso({ tipo: 'error', titulo: 'No se pudo abrir el pago con MODO', texto: error.message });
            });
        });
    }

    document.addEventListener('click', function (evento) {
        var boton = evento.target.closest ? evento.target.closest('[data-pagar]') : null;
        if (!boton) { return; }
        evento.preventDefault();
        var url = boton.getAttribute('data-url');
        var inscripcion = boton.getAttribute('data-inscripcion');
        // Un QR nuevo se pide siempre por la inscripción: el servidor decide si reutiliza el pago rechazado
        var urlNueva = inscripcion ? '/pago/pagar/' + inscripcion : url;
        var monto = boton.getAttribute('data-monto');
        if (monto) {
            url += (url.indexOf('?') < 0 ? '?' : '&') + 'monto=' + encodeURIComponent(monto);
            urlNueva += (urlNueva.indexOf('?') < 0 ? '?' : '&') + 'monto=' + encodeURIComponent(monto);
        }
        iniciar(url, urlNueva, boton);
    });
})();
