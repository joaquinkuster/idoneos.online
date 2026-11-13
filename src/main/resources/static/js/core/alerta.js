/** Alertas del sistema (SweetAlert2). */
(function () {
    'use strict';

    // Ícono (de Font Awesome) y clase de cada tipo de aviso: todos son el mismo círculo de 80 px, con el color del tipo
    var TIPOS_DE_AVISO = {
        exito: { icon: 'fa-check', clase: 'swal-icon-exito', boton: 'swal-boton-confirmar' },
        error: { icon: 'fa-xmark', clase: 'swal-icon-error', boton: 'swal-boton-peligro' },
        info: { icon: 'fa-circle-info', clase: 'swal-icon-info', boton: 'swal-boton-info' },
        advertencia: { icon: 'fa-triangle-exclamation', clase: 'swal-icon-advertencia', boton: 'swal-boton-advertencia' }
    };

    /** Aviso del sistema, con el formato de todas las alertas: círculo de color con el ícono del tipo. */
    window.mostrarAviso = function (opciones) {
        var tipo = TIPOS_DE_AVISO[opciones.tipo] || TIPOS_DE_AVISO.info;
        return Swal.fire({
            iconHtml: '<i class="fa-solid ' + tipo.icon + '"></i>',
            title: opciones.titulo,
            text: opciones.html ? undefined : opciones.texto,
            html: opciones.html,
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
     * Alerta guía con dos caminos: si el paso trae una alternativa (siguienteAlternativa y siguienteAlternativaUrl), el segundo botón la sigue.
     * Devuelve true si se fue a otra pantalla.
     */
    window.manejarSiguientePaso = async function (datos) {
        var respuesta = await window.mostrarAviso({
            tipo: datos.siguienteTipo || 'info', titulo: datos.siguienteTitulo, texto: datos.siguienteTexto, confirmar: datos.siguienteConfirmar,
            cancelar: datos.siguienteAlternativa || 'Ahora no', bloquear: !!datos.siguienteAlternativa
        });
        if (respuesta.isConfirmed) {
            await window.irAlSiguientePaso(datos);
            return true;
        }
        if (datos.siguienteAlternativaUrl && respuesta.dismiss === Swal.DismissReason.cancel) {
            window.location.href = datos.siguienteAlternativaUrl;
            return true;
        }
        return false;
    };

    /** Alerta guía del flujo: después de crear algo, propone el paso siguiente. */
    window.preguntarSiguientePaso = function (titulo, texto, confirmar, tipo) {
        return window.mostrarAviso({ tipo: tipo || 'info', titulo: titulo, texto: texto, confirmar: confirmar, cancelar: 'Ahora no' })
            .then(function (r) { return r.isConfirmed; });
    };

    /** Sigue el paso que se confirmó: si el paso cambia datos (siguientePost), lo envía por POST antes de ir a la pantalla. */
    window.irAlSiguientePaso = async function (datos) {
        if (!datos.siguientePost) {
            window.location.href = datos.siguienteUrl;
            return;
        }
        try {
            var respuesta = await fetch(datos.siguientePost, {
                method: 'POST', headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' }, credentials: 'same-origin'
            });
            var resultado = await respuesta.json().catch(function () { return {}; });
            if (respuesta.ok && resultado.mensaje) {
                await window.mostrarExito(resultado.mensaje);
                window.location.href = resultado.destino || datos.siguienteUrl;
                return;
            }
            await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
        } catch (e) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        }
    };

    /** Botón "Nueva versión" del diseño del curso: antes de crear la versión pregunta si hay otros temas por registrar. */
    document.addEventListener('click', async function (e) {
        var boton = e.target.closest('[data-nueva-version]');
        if (!boton) { return; }
        var respuesta = await window.mostrarAviso({
            tipo: 'info',
            titulo: '¿Querés registrar otros temas antes?',
            texto: 'Si la nueva versión necesita temas que el curso todavía no tiene, registralos ahora. Cada tema nuevo tiene que tener su contenido completo '
                + 'para poder usarlo en el programa.',
            confirmar: 'Registrar temas',
            cancelar: 'No, crear la versión',
            bloquear: true
        });
        window.location.href = respuesta.isConfirmed ? boton.dataset.temasUrl : boton.dataset.programaUrl;
    });

    /** Botón "Finalizar diseño" del diseño del curso: pregunta y, si el docente acepta, publica la cohorte. */
    document.addEventListener('click', async function (e) {
        var boton = e.target.closest('[data-finalizar-diseno]');
        if (!boton) { return; }
        var respuesta = await window.mostrarAviso({
            tipo: 'advertencia',
            titulo: '¿Deseás finalizar el diseño?',
            texto: 'La cohorte ' + boton.dataset.cohorteNombre + ' quedará publicada en el catálogo. Lo publicado es un compromiso de la empresa con los alumnos '
                + 'que lo consultan o ya se inscribieron, por eso ya no se podrán modificar los datos del curso. '
                + 'Si te equivocás, solicitá al administrador la baja de la cohorte para crearla de nuevo.',
            confirmar: 'Finalizar diseño',
            cancelar: 'Cancelar',
            bloquear: true
        });
        if (respuesta.isConfirmed) {
            await window.irAlSiguientePaso({ siguientePost: '/cohorte/finalizarDiseno/' + boton.dataset.cohorteId, siguienteUrl: '/curso/acceder/' + boton.dataset.cursoId });
        }
    });

    /** Pregunta, al final del alta de un recurso en un curso. */
    window.preguntarPublicarRecurso = async function (idRecurso) {
        var respuesta = await window.mostrarAviso({
            tipo: 'info',
            titulo: '¿Querés publicarlo en la biblioteca digital?',
            texto: 'Otros docentes van a poder ver este recurso. Si preferís no hacerlo, queda privado.',
            confirmar: 'Publicar',
            cancelar: 'Ahora no'
        });
        if (!respuesta.isConfirmed) { return; }
        try {
            var resultado = await fetch('/biblioteca/publicar/' + idRecurso, {
                method: 'POST', headers: { 'X-Requested-With': 'fetch', 'Accept': 'application/json' }, credentials: 'same-origin'
            });
            var datos = await resultado.json();
            if (resultado.ok && datos.mensaje) {
                await window.mostrarExito(datos.mensaje);
            } else {
                await window.mostrarError(datos.error || 'Error! Ocurrió un error inesperado. Intentá nuevamente.');
            }
        } catch (e) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intentá nuevamente.');
        }
    };

    window.mostrarError = function (mensaje) {
        return window.mostrarAviso({ tipo: 'error', titulo: 'Atención', texto: mensaje });
    };

    window.mostrarInformacion = function (titulo, mensaje) {
        return window.mostrarAviso({ tipo: 'info', titulo: titulo, texto: mensaje });
    };

    /** Confirma la baja de varios elementos a la vez, con las mismas dos filas que la baja individual. */
    window.confirmarBajaMasiva = function (datos) {
        var hay = datos.dependencias > 0;
        var termino = datos.dependencias === 1 ? datos.terminoUno : datos.terminoVarios;
        var filaDependencias = hay
            ? '<span class="text-danger fw-bold">' + datos.dependencias + ' ' + termino + '</span>'
            : '<span class="text-success fw-bold">Sin dependencias</span>';
        return Swal.fire({
            iconHtml: '<i class="fa-solid fa-triangle-exclamation"></i>',
            title: datos.titulo || '¿Confirmas dar de baja los ' + datos.elementos + ' seleccionados?',
            html: '<p class="swal-texto-baja">' + (datos.texto || 'Se darán de baja todos o ninguno.') + '</p>'
                + '<div class="pn-baja-detalle">'
                + '<div class="pn-baja-fila"><span class="text-muted">Elementos</span><strong>' + datos.cantidad + (datos.cantidad === 1 ? ' seleccionado' : ' seleccionados') + '</strong></div>'
                + '<div class="pn-baja-fila"><span class="text-muted">Dependencias</span>' + filaDependencias + '</div>'
                + '</div>',
            showCancelButton: true,
            reverseButtons: true,
            confirmButtonText: datos.confirmar || 'Dar de baja',
            cancelButtonText: 'Cancelar',
            didOpen: function () {
                if (hay) { Swal.getConfirmButton().disabled = true; }
            },
            customClass: {
                icon: 'swal-icon-peligro',
                popup: 'swal-confirmacion swal-baja',
                confirmButton: 'swal-boton-peligro',
                cancelButton: 'swal-boton-cancelar'
            },
            buttonsStyling: false
        }).then(function (resultado) { return resultado.isConfirmed; });
    };

    /** Pregunta si se quiere salir de una pantalla con cambios sin guardar. */
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
                icon: 'swal-icon-peligro',
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

    /** Confirmación de una acción que no es una baja: título, texto y el nombre del botón de confirmar. */
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
                icon: 'swal-icon-confirmar',
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
                icon: 'swal-icon-peligro',
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
