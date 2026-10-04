/**
 * Alertas del sistema (SweetAlert2).
 *
 *  - mostrarExito: alerta de éxito con el tilde verde, el título, el mensaje y el botón "Aceptar".
 *  - mostrarError: alerta de error con el botón "Aceptar".
 *  - confirmarBajaMasiva: confirmación de la baja de varios elementos.
 *  - confirmarCerrarSesion: confirmación de cierre de sesión con botón rojo.
 */
(function () {
    'use strict';

    // Todas las alertas comparten tamaño y estilo: título en negrita y mensaje debajo, sin negrita
    var CLASES_INFORMACION = { popup: 'swal-confirmacion', confirmButton: 'swal-boton-confirmar' };

    window.mostrarExito = function (mensaje) {
        return Swal.fire({
            icon: 'success',
            title: '¡Operación exitosa!',
            text: mensaje,
            confirmButtonText: 'Aceptar',
            customClass: CLASES_INFORMACION,
            buttonsStyling: false
        });
    };

    window.mostrarError = function (mensaje) {
        return Swal.fire({
            icon: 'error',
            title: 'Atención',
            text: mensaje,
            confirmButtonText: 'Aceptar',
            customClass: CLASES_INFORMACION,
            buttonsStyling: false
        });
    };

    window.mostrarInformacion = function (titulo, mensaje) {
        return Swal.fire({
            icon: 'info',
            title: titulo,
            text: mensaje,
            confirmButtonText: 'Aceptar',
            customClass: CLASES_INFORMACION,
            buttonsStyling: false
        });
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
                popup: 'swal-confirmacion',
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
