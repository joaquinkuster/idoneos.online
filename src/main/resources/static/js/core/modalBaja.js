/**
 * Ventana de baja compartida de los listados: una sola por pantalla (fragments/modalBaja.html) en lugar de una por fila.
 *
 * El botón "Dar de baja" de cada fila lleva data-bs-target (el id de la ventana), data-baja-accion (la ruta de la baja),
 * data-baja-pregunta (la confirmación, con el nombre del registro) y data-baja-nota (la aclaración). Al abrirse, la ventana
 * toma esos datos del botón que la abrió.
 */
(function () {
    'use strict';

    document.addEventListener('show.bs.modal', function (e) {
        var boton = e.relatedTarget;
        if (!boton || !boton.dataset || !boton.dataset.bajaAccion) { return; }
        var modal = e.target;
        var formulario = modal.querySelector('[data-baja-formulario]');
        if (!formulario) { return; }
        // Los campos (por ejemplo el motivo) arrancan vacíos para cada registro
        formulario.reset();
        // El error de la baja anterior (por ejemplo "tiene inscripciones activas") no vale para este registro
        formulario.querySelectorAll('.alert-formulario').forEach(function (alerta) {
            alerta.textContent = '';
            alerta.classList.add('d-none');
        });
        formulario.querySelectorAll('.es-invalido').forEach(function (campo) { campo.classList.remove('es-invalido'); });
        formulario.querySelectorAll('.pn-ayuda-error').forEach(function (ayuda) { ayuda.textContent = ''; });
        var enviar = formulario.querySelector('button[type="submit"]');
        if (enviar) { enviar.disabled = false; }
        formulario.setAttribute('action', boton.dataset.bajaAccion);
        var pregunta = modal.querySelector('[data-baja-pregunta]');
        if (pregunta && boton.dataset.bajaPregunta) { pregunta.textContent = boton.dataset.bajaPregunta; }
        var nota = modal.querySelector('[data-baja-nota]');
        if (nota && boton.dataset.bajaNota !== undefined) { nota.textContent = boton.dataset.bajaNota; }
    });
})();
