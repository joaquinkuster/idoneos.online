/** Perfil del docente: ventana para crear su clon. Paso 1, foto (archivo o cámara); paso 2, muestra de voz (archivo o micrófono). */
(function () {
    'use strict';

    var ventana = document.getElementById('modalClon');
    if (!ventana) { return; }

    var pasos = ventana.querySelectorAll('[data-clon-paso]');
    var puntos = ventana.querySelectorAll('[data-clon-punto]');
    var botonSiguiente = document.getElementById('clonSiguiente');
    var botonConfirmar = document.getElementById('clonConfirmar');
    var estado = document.getElementById('clonEstado');

    var video = document.getElementById('clonVideo');
    var imagen = document.getElementById('clonFoto');
    var fotoVacia = document.getElementById('clonFotoVacia');
    var archivoFoto = document.getElementById('clonArchivoFoto');
    var abrirCamara = document.getElementById('clonAbrirCamara');
    var capturar = document.getElementById('clonCapturar');
    var terminos = document.getElementById('clonTerminos');

    var reproductor = document.getElementById('clonReproductor');
    var audioVacio = document.getElementById('clonAudioVacio');
    var tiempo = document.getElementById('clonTiempo');
    var archivoAudio = document.getElementById('clonArchivoAudio');
    var grabar = document.getElementById('clonGrabar');
    var detener = document.getElementById('clonDetener');

    var foto = null;          // Blob o File con la foto elegida
    var audio = null;         // Blob o File con el audio elegido
    var flujoCamara = null;
    var flujoMicrofono = null;
    var grabadora = null;
    var cronometro = null;
    var urlFoto = null;
    var urlAudio = null;
    var pasoActual = 1;

    // ----------------------------------------------------------------------
    // Pasos
    // ----------------------------------------------------------------------

    function mostrarPaso(numero) {
        pasoActual = numero;
        pasos.forEach(function (p) { p.hidden = Number(p.getAttribute('data-clon-paso')) !== numero; });
        puntos.forEach(function (p) { p.classList.toggle('activo', Number(p.getAttribute('data-clon-punto')) === numero); });
        botonSiguiente.hidden = numero !== 1;
        botonConfirmar.hidden = numero !== 2;
        estado.hidden = true;
    }

    function ocupado(boton, textoEspera, activo) {
        boton.disabled = activo;
        if (activo) {
            boton.dataset.texto = boton.textContent;
            boton.textContent = textoEspera;
            estado.textContent = 'HeyGen está procesando tu archivo: puede tardar hasta un par de minutos. No cierres esta ventana.';
            estado.hidden = false;
        } else {
            boton.textContent = boton.dataset.texto || boton.textContent;
            estado.hidden = true;
        }
    }

    async function enviar(url, datos) {
        var respuesta = await fetch(url, { method: 'POST', body: datos, headers: { 'X-Requested-With': 'fetch' }, credentials: 'same-origin' });
        var cuerpo = await respuesta.json().catch(function () { return {}; });
        if (!respuesta.ok) {
            throw new Error(cuerpo.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
        }
        return cuerpo;
    }

    // ----------------------------------------------------------------------
    // Foto
    // ----------------------------------------------------------------------

    function mostrarFoto(archivo) {
        foto = archivo;
        if (urlFoto) { URL.revokeObjectURL(urlFoto); }
        urlFoto = URL.createObjectURL(archivo);
        imagen.src = urlFoto;
        imagen.classList.remove('d-none');
        fotoVacia.hidden = true;
        video.hidden = true;
        capturar.hidden = true;
    }

    function apagarCamara() {
        if (flujoCamara) { flujoCamara.getTracks().forEach(function (t) { t.stop(); }); flujoCamara = null; }
        video.hidden = true;
        capturar.hidden = true;
    }

    archivoFoto.addEventListener('change', function () {
        if (archivoFoto.files && archivoFoto.files[0]) { apagarCamara(); mostrarFoto(archivoFoto.files[0]); }
    });

    abrirCamara.addEventListener('click', async function () {
        try {
            flujoCamara = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' } });
            video.srcObject = flujoCamara;
            video.hidden = false;
            imagen.classList.add('d-none');
            fotoVacia.hidden = true;
            capturar.hidden = false;
        } catch (e) {
            await window.mostrarError('Error! No se pudo abrir la cámara. Revisá que el navegador tenga permiso para usarla.');
        }
    });

    capturar.addEventListener('click', function () {
        var lienzo = document.createElement('canvas');
        lienzo.width = video.videoWidth;
        lienzo.height = video.videoHeight;
        lienzo.getContext('2d').drawImage(video, 0, 0);
        lienzo.toBlob(function (blob) {
            apagarCamara();
            if (blob) { mostrarFoto(new File([blob], 'foto.jpg', { type: 'image/jpeg' })); }
        }, 'image/jpeg', 0.92);
    });

    botonSiguiente.addEventListener('click', async function () {
        if (!foto) { await window.mostrarError('Error! Elegí una foto o sacate una con la cámara.'); return; }
        if (!terminos.checked) { await window.mostrarError('Error! Aceptá los términos y condiciones para crear tu clon.'); return; }
        var datos = new FormData();
        datos.append('foto', foto, foto.name || 'foto.jpg');
        datos.append('terminos', 'true');
        ocupado(botonSiguiente, 'Creando avatar...', true);
        try {
            await enviar('/clon/avatar', datos);
            mostrarPaso(2);
        } catch (e) {
            await window.mostrarError(e.message);
        } finally {
            ocupado(botonSiguiente, '', false);
        }
    });

    // ----------------------------------------------------------------------
    // Voz
    // ----------------------------------------------------------------------

    function mostrarAudio(archivo) {
        audio = archivo;
        if (urlAudio) { URL.revokeObjectURL(urlAudio); }
        urlAudio = URL.createObjectURL(archivo);
        reproductor.src = urlAudio;
        reproductor.hidden = false;
        audioVacio.hidden = true;
    }

    archivoAudio.addEventListener('change', function () {
        if (archivoAudio.files && archivoAudio.files[0]) { mostrarAudio(archivoAudio.files[0]); }
    });

    /** Pasa lo grabado a un WAV de 16 bits y un solo canal, que HeyGen acepta sin conversión. */
    async function aWav(blob) {
        var contexto = new (window.AudioContext || window.webkitAudioContext)();
        var buffer = await contexto.decodeAudioData(await blob.arrayBuffer());
        contexto.close();
        var muestras = buffer.getChannelData(0);
        var vista = new DataView(new ArrayBuffer(44 + muestras.length * 2));
        function texto(desde, cadena) { for (var i = 0; i < cadena.length; i++) { vista.setUint8(desde + i, cadena.charCodeAt(i)); } }
        texto(0, 'RIFF'); vista.setUint32(4, 36 + muestras.length * 2, true); texto(8, 'WAVE'); texto(12, 'fmt ');
        vista.setUint32(16, 16, true); vista.setUint16(20, 1, true); vista.setUint16(22, 1, true);
        vista.setUint32(24, buffer.sampleRate, true); vista.setUint32(28, buffer.sampleRate * 2, true); vista.setUint16(32, 2, true); vista.setUint16(34, 16, true);
        texto(36, 'data'); vista.setUint32(40, muestras.length * 2, true);
        for (var i = 0; i < muestras.length; i++) {
            var m = Math.max(-1, Math.min(1, muestras[i]));
            vista.setInt16(44 + i * 2, m < 0 ? m * 0x8000 : m * 0x7FFF, true);
        }
        return new File([vista.buffer], 'voz.wav', { type: 'audio/wav' });
    }

    grabar.addEventListener('click', async function () {
        try {
            flujoMicrofono = await navigator.mediaDevices.getUserMedia({ audio: true });
        } catch (e) {
            await window.mostrarError('Error! No se pudo usar el micrófono. Revisá que el navegador tenga permiso para usarlo.');
            return;
        }
        var trozos = [];
        grabadora = new MediaRecorder(flujoMicrofono);
        grabadora.ondataavailable = function (e) { if (e.data.size) { trozos.push(e.data); } };
        grabadora.onstop = async function () {
            flujoMicrofono.getTracks().forEach(function (t) { t.stop(); });
            clearInterval(cronometro);
            tiempo.hidden = true;
            try {
                mostrarAudio(await aWav(new Blob(trozos, { type: grabadora.mimeType })));
            } catch (e) {
                await window.mostrarError('Error! No se pudo procesar la grabación. Probá de nuevo o elegí un archivo.');
            }
        };
        grabadora.start();
        var segundos = 0;
        tiempo.hidden = false;
        tiempo.textContent = 'Grabando... 00:00';
        cronometro = setInterval(function () {
            segundos++;
            tiempo.textContent = 'Grabando... ' + String(Math.floor(segundos / 60)).padStart(2, '0') + ':' + String(segundos % 60).padStart(2, '0');
        }, 1000);
        grabar.hidden = true;
        detener.hidden = false;
        reproductor.hidden = true;
        audioVacio.hidden = true;
    });

    detener.addEventListener('click', function () {
        if (grabadora && grabadora.state !== 'inactive') { grabadora.stop(); }
        detener.hidden = true;
        grabar.hidden = false;
    });

    botonConfirmar.addEventListener('click', async function () {
        if (!audio) { await window.mostrarError('Error! Elegí un audio o grabá tu voz con el micrófono.'); return; }
        var datos = new FormData();
        datos.append('audio', audio, audio.name || 'voz.wav');
        ocupado(botonConfirmar, 'Creando voz...', true);
        try {
            await enviar('/clon/voz', datos);
            await window.mostrarExito('Clon creado con éxito.');
            window.location.reload();
        } catch (e) {
            await window.mostrarError(e.message);
        } finally {
            ocupado(botonConfirmar, '', false);
        }
    });

    // Al cerrar la ventana se apagan la cámara y el micrófono y se vuelve al primer paso
    ventana.addEventListener('hidden.bs.modal', function () {
        apagarCamara();
        if (grabadora && grabadora.state !== 'inactive') { grabadora.stop(); }
        mostrarPaso(1);
    });

    mostrarPaso(1);
})();
