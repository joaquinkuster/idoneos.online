/**
 * Alertas del sistema (SweetAlert2).
 *
 *  - mostrarAviso: el aviso base, con el círculo de color del tipo (éxito, error, información o advertencia). Todos los
 *    avisos y confirmaciones tienen el mismo formato: círculo de 80 px, título, texto, 460 px de ancho y sin animación.
 *  - mostrarExito: alerta de éxito con el título, el mensaje y el botón "Aceptar".
 *  - mostrarError: alerta de error con el botón "Aceptar".
 *  - confirmarBajaMasiva: confirmación de la baja de varios elementos.
 *  - mostrarCursoFinalizado: aviso de curso finalizado con éxito, con acceso a "Mis certificados".
 *  - confirmarAccion: confirmación de una acción que no es una baja (por ejemplo, aprobar un intento), con botón azul.
 *  - confirmarCerrarSesion: confirmación de cierre de sesión con botón rojo.
 *  - confirmarCambiosSinGuardar: confirmación para salir de una pantalla con cambios sin guardar.
 */
(function () {
    'use strict';

    // Ícono (de Font Awesome) y clase de cada tipo de aviso: todos son el mismo círculo de 80 px, con el color del tipo
    var TIPOS_DE_AVISO = {
        exito: { icono: 'fa-check', clase: 'swal-icono-exito', boton: 'swal-boton-confirmar' },
        error: { icono: 'fa-xmark', clase: 'swal-icono-error', boton: 'swal-boton-peligro' },
        info: { icono: 'fa-circle-info', clase: 'swal-icono-info', boton: 'swal-boton-info' },
        advertencia: { icono: 'fa-triangle-exclamation', clase: 'swal-icono-advertencia', boton: 'swal-boton-advertencia' }
    };

    /**
     * Aviso del sistema, con el formato de todas las alertas: círculo de color con el ícono del tipo, título en negrita y
     * texto debajo, sin animación. opciones: tipo ('exito', 'error', 'info' o 'advertencia'), titulo, texto, confirmar
     * (texto del botón, "Aceptar" por defecto), cancelar (si se indica, muestra también un botón para cerrar) y bloquear
     * (si es true, no se cierra al hacer clic afuera) y claseBoton (para cambiar el estilo del botón principal). Devuelve
     * la promesa de SweetAlert (con isConfirmed).
     */
    window.mostrarAviso = function (opciones) {
        var tipo = TIPOS_DE_AVISO[opciones.tipo] || TIPOS_DE_AVISO.info;
        return Swal.fire({
            iconHtml: '<i class="fa-solid ' + tipo.icono + '"></i>',
            title: opciones.titulo,
            text: opciones.texto,
            showCancelButton: !!opciones.cancelar,
            reverseButtons: true,
            confirmButtonText: opciones.confirmar || 'Aceptar',
            cancelButtonText: opciones.cancelar || 'Cancelar',
            showClass: { icon: '' },
            allowOutsideClick: !opciones.bloquear,
            customClass: {
                icon: tipo.clase,
                popup: 'swal-confirmacion swal-baja',
                confirmButton: opciones.claseBoton || tipo.boton,
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        });
    };

    window.mostrarExito = function (mensaje) {
        return window.mostrarAviso({ tipo: 'exito', titulo: '¡Operación exitosa!', texto: mensaje });
    };

    /**
     * Alerta guía del flujo: después de crear algo, propone el paso siguiente. Devuelve una promesa con true si la persona
     * acepta seguir (el que llama navega al paso siguiente) y false si prefiere quedarse.
     */
    window.preguntarSiguientePaso = function (titulo, texto, confirmar) {
        return window.mostrarAviso({ tipo: 'info', titulo: titulo, texto: texto, confirmar: confirmar, cancelar: 'Ahora no' })
            .then(function (r) { return r.isConfirmed; });
    };

    window.mostrarError = function (mensaje) {
        return window.mostrarAviso({ tipo: 'error', titulo: 'Atención', texto: mensaje });
    };

    window.mostrarInformacion = function (titulo, mensaje) {
        return window.mostrarAviso({ tipo: 'info', titulo: titulo, texto: mensaje });
    };

    /**
     * Confirma la baja de varios elementos a la vez, con las mismas dos filas que la baja individual:
     * los elementos seleccionados y sus dependencias (en rojo con la cantidad si existen, y entonces no se puede
     * confirmar; en verde si no existen). Devuelve una promesa con true si el usuario confirma.
     */
    window.confirmarBajaMasiva = function (datos) {
        var hay = datos.dependencias > 0;
        var termino = datos.dependencias === 1 ? datos.terminoUno : datos.terminoVarios;
        var filaDependencias = hay
            ? '<span class="text-danger fw-bold">' + datos.dependencias + ' ' + termino + '</span>'
            : '<span class="text-success fw-bold">Sin dependencias</span>';
        return Swal.fire({
            iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
            title: '¿Confirmas dar de baja los ' + datos.elementos + ' seleccionados?',
            html: '<p class="swal-texto-baja">Se darán de baja todos o ninguno.</p>'
                + '<div class="pn-baja-detalle">'
                + '<div class="pn-baja-fila"><span class="text-muted">Elementos</span><strong>' + datos.cantidad + (datos.cantidad === 1 ? ' seleccionado' : ' seleccionados') + '</strong></div>'
                + '<div class="pn-baja-fila"><span class="text-muted">Dependencias</span>' + filaDependencias + '</div>'
                + '</div>',
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: 'Dar de baja',
            cancelButtonText: 'Cancelar',
            didOpen: function () {
                if (hay) { Swal.getConfirmButton().disabled = true; }
            },
            customClass: {
                icon: 'swal-icono-peligro',
                popup: 'swal-confirmacion swal-baja',
                confirmButton: 'swal-boton-peligro',
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        }).then(function (resultado) { return resultado.isConfirmed; });
    };

    /**
     * Pregunta si se quiere salir de una pantalla con cambios sin guardar. Devuelve una promesa con true si el usuario
     * confirma que quiere salir (y perder los cambios).
     */
    window.confirmarCambiosSinGuardar = function (mensaje) {
        return Swal.fire({
            iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
            title: 'Hay cambios sin guardar',
            text: mensaje,
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: 'Salir sin guardar',
            cancelButtonText: 'Seguir editando',
            customClass: {
                icon: 'swal-icono-peligro',
                popup: 'swal-confirmacion swal-baja',
                confirmButton: 'swal-boton-peligro',
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        }).then(function (resultado) { return resultado.isConfirmed; });
    };

    window.mostrarCursoFinalizado = function (urlCertificados) {
        return window.mostrarAviso({
            tipo: 'exito',
            titulo: '¡Finalizaste el curso con éxito!',
            texto: 'Tu certificado ya está disponible para descargar en «Mis certificados».',
            confirmar: 'Ir a Mis certificados',
            cancelar: 'Cerrar'
        }).then(function (resultado) {
            if (resultado.isConfirmed) {
                window.location.href = urlCertificados;
            }
        });
    };

    /**
     * Confirmación de una acción que no es una baja: título, texto y el nombre del botón de confirmar. Lleva el mismo ícono
     * redondeado que las de baja, en azul, y aparece sin animación. Devuelve una promesa con true si el usuario confirma.
     */
    window.confirmarAccion = function (titulo, texto, textoConfirmar, textoCancelar) {
        return Swal.fire({
            iconHtml: '<i class="fa-solid fa-circle-question"></i>',
            title: titulo,
            text: texto,
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: textoConfirmar,
            cancelButtonText: textoCancelar || 'Cancelar',
            showClass: { icon: '' },
            customClass: {
                icon: 'swal-icono-confirmar',
                popup: 'swal-confirmacion swal-baja', // mismo ancho (460 px) que la confirmación de baja masiva
                confirmButton: 'swal-boton-info',
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        }).then(function (resultado) { return resultado.isConfirmed; });
    };

    window.confirmarCerrarSesion = function () {
        Swal.fire({
            iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
            title: 'Cerrar sesión',
            text: '¿Estás seguro de que deseas cerrar sesión?',
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: 'Cerrar sesión',
            cancelButtonText: 'Cancelar',
            customClass: {
                icon: 'swal-icono-peligro',
                popup: 'swal-confirmacion swal-baja',
                confirmButton: 'swal-boton-peligro',
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        }).then(function (resultado) {
            if (resultado.isConfirmed) {
                document.getElementById('formularioCerrarSesion').submit();
            }
        });
    };
})();
