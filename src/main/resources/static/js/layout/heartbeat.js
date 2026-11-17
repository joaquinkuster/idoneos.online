/** Latido de la sesión: avisa al servidor cada minuto que hay una pantalla abierta, para que la sesión no venza. */
(function () {
    'use strict';

    var INTERVALO = 60 * 1000;
    var temporizador = null;

    function detener() {
        if (temporizador) {
            clearInterval(temporizador);
            temporizador = null;
        }
    }

    function latir() {
        // Con redirect manual, una sesión vencida (el servidor redirige al inicio de sesión) se reconoce sin seguir la redirección
        fetch('/sesion/heartbeat', { method: 'POST', credentials: 'same-origin', redirect: 'manual', keepalive: true })
            .then(function (respuesta) {
                if (respuesta.type === 'opaqueredirect' || respuesta.status === 401 || respuesta.status === 403) {
                    detener();
                    return null;
                }
                return respuesta.ok ? respuesta.json() : null;
            })
            .then(function (datos) {
                // El latido también trae la cantidad de notificaciones sin leer, para la campana del navbar
                if (datos && typeof datos.noLeidas === 'number' && window.actualizarNotificaciones) {
                    window.actualizarNotificaciones(datos.noLeidas);
                }
            })
            .catch(function () { /* sin conexión: se vuelve a intentar en el próximo latido */ });
    }

    temporizador = setInterval(latir, INTERVALO);
    // Al volver a la pestaña o recuperar la conexión se late de inmediato, sin esperar al próximo minuto
    document.addEventListener('visibilitychange', function () { if (!document.hidden && temporizador) { latir(); } });
    window.addEventListener('online', function () { if (temporizador) { latir(); } });
})();
