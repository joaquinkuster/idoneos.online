/**
 * Conectar con mi OBS: desde el estudio, el navegador se conecta al servidor WebSocket de OBS del propio docente (protocolo 5, incluido
 * desde OBS 28), carga el servidor y la clave de la clase como servicio de emisión personalizado y, si el docente lo pide, inicia la
 * transmisión. La clave nunca pasa por otro servidor: va del navegador al OBS de la misma computadora.
 */
(function () {
    'use strict';

    var caja = document.querySelector('[data-obs]');
    if (!caja) { return; }

    var servidor = caja.dataset.servidor;
    var clave = caja.dataset.clave;
    var direccion = caja.querySelector('[data-obs-direccion]');
    var contrasena = caja.querySelector('[data-obs-contrasena]');
    var conectar = caja.querySelector('[data-obs-conectar]');
    var estado = caja.querySelector('[data-obs-estado]');

    // El mismo botón recorre tres pasos: conectar, iniciar la transmisión y, una vez transmitiendo, deja de mostrarse
    var paso = 'conectar';
    var socket = null;
    var pendientes = {};
    var numero = 0;

    function decir(texto, error) {
        estado.textContent = texto;
        estado.classList.toggle('sc-obs-error', !!error);
    }

    /** Resumen SHA-256 en base 64, que es lo que pide la autenticación de OBS. */
    function resumir(texto) {
        return crypto.subtle.digest('SHA-256', new TextEncoder().encode(texto)).then(function (buffer) {
            var binario = '';
            new Uint8Array(buffer).forEach(function (b) { binario += String.fromCharCode(b); });
            return btoa(binario);
        });
    }

    function pedir(tipo, datos) {
        return new Promise(function (resolver, rechazar) {
            var id = 'p' + (++numero);
            pendientes[id] = { resolver: resolver, rechazar: rechazar };
            socket.send(JSON.stringify({ op: 6, d: { requestType: tipo, requestId: id, requestData: datos || {} } }));
        });
    }

    function cerrar() {
        if (socket) {
            socket.onclose = null;
            try { socket.close(); } catch (e) { /* ya estaba cerrado */ }
        }
        socket = null;
        irAlPaso('conectar');
    }

    function irAlPaso(nuevo) {
        paso = nuevo;
        conectar.textContent = nuevo === 'conectar' ? 'Conectar con mi OBS' : 'Iniciar transmisión';
        // Una vez transmitiendo, el botón sigue a la vista pero deshabilitado
        conectar.disabled = nuevo === 'transmitiendo';
    }

    function identificar(hello) {
        var identificacion = { rpcVersion: 1, eventSubscriptions: 0 };
        if (!hello.authentication) {
            socket.send(JSON.stringify({ op: 1, d: identificacion }));
            return;
        }
        resumir(contrasena.value + hello.authentication.salt).then(function (secreto) {
            return resumir(secreto + hello.authentication.challenge);
        }).then(function (autenticacion) {
            identificacion.authentication = autenticacion;
            socket.send(JSON.stringify({ op: 1, d: identificacion }));
        });
    }

    function cargarServicio() {
        decir('Conectado. Cargando los datos de la clase en OBS...');
        pedir('SetStreamServiceSettings', {
            streamServiceType: 'rtmp_custom',
            streamServiceSettings: { server: servidor, key: clave, use_auth: false }
        }).then(function () {
            decir('Listo: OBS ya tiene los datos de la clase. Podés iniciar la transmisión desde acá o desde OBS.');
            irAlPaso('iniciar');
        }).catch(function (e) {
            decir('OBS no aceptó los datos: ' + e.message, true);
            cerrar();
        });
    }

    function alMensaje(evento) {
        var mensaje = JSON.parse(evento.data);
        if (mensaje.op === 0) { identificar(mensaje.d); return; }
        if (mensaje.op === 2) { cargarServicio(); return; }
        if (mensaje.op === 7) {
            var espera = pendientes[mensaje.d.requestId];
            if (!espera) { return; }
            delete pendientes[mensaje.d.requestId];
            if (mensaje.d.requestStatus.result) {
                espera.resolver(mensaje.d.responseData);
            } else {
                espera.rechazar(new Error(mensaje.d.requestStatus.comment || 'error ' + mensaje.d.requestStatus.code));
            }
        }
    }

    function conectarConObs() {
        if (!window.crypto || !crypto.subtle) {
            decir('Este navegador no puede cifrar la contraseña de OBS. Cargá los datos a mano.', true);
            return;
        }
        cerrar();
        conectar.disabled = true;
        decir('Conectando con OBS...');
        var abierto = false;
        try {
            socket = new WebSocket('ws://' + direccion.value.trim(), 'obswebsocket.json');
        } catch (e) {
            decir('La dirección de OBS no es válida.', true);
            irAlPaso('conectar');
            return;
        }
        socket.onopen = function () { abierto = true; };
        socket.onmessage = alMensaje;
        socket.onclose = function (evento) {
            irAlPaso('conectar');
            if (evento.code === 4009) {
                decir('La contraseña de OBS no es correcta.', true);
            } else if (!abierto) {
                decir('No se pudo conectar. Revisá que OBS esté abierto y tenga activado el servidor WebSocket.', true);
            }
        };
    }

    // Alternativa: los datos para cargar a mano
    var abrirManual = document.querySelector('[data-manual-abrir]');
    var panelManual = document.querySelector('[data-manual-panel]');
    if (abrirManual && panelManual) {
        abrirManual.addEventListener('click', function () { panelManual.hidden = !panelManual.hidden; });
    }

    // Si el estudio está conectado con OBS, al finalizar la clase también se detiene la transmisión de OBS
    var formularioFinalizar = document.querySelector('#modalFinalizarClase form');
    if (formularioFinalizar) {
        formularioFinalizar.addEventListener('submit', function () {
            if (socket && socket.readyState === WebSocket.OPEN && paso !== 'conectar') {
                pedir('StopStream').catch(function () { /* OBS no estaba transmitiendo */ });
            }
        }, true);
    }

    conectar.addEventListener('click', function () {
        if (paso !== 'iniciar') {
            conectarConObs();
            return;
        }
        conectar.disabled = true;
        pedir('StartStream').then(function () {
            decir('Transmitiendo desde OBS. En unos segundos el estudio muestra la señal.');
            irAlPaso('transmitiendo');
        }).catch(function (e) {
            decir('OBS no pudo iniciar la transmisión: ' + e.message, true);
            irAlPaso('iniciar');
        });
    });
})();
