/**
 * Ventana de intereses del alumno (fragments/components/modalIntereses.html): elige las categorías de cursos que le interesan.
 *
 * Modo "ingreso": se abre sola al entrar si todavía no eligió ninguna; al cerrarla sin guardar no se le vuelve a preguntar en esa sesión. Modo "edicion": se abre desde «Mis intereses» del menú del usuario ([data-abrir-intereses]) con lo ya elegido; se puede cancelar sin registrar nada.
 */
(function () {
    'use strict';

    var modal = document.getElementById('modalIntereses');
    if (!modal || !window.bootstrap) { return; }
    document.body.appendChild(modal); // dentro del navbar el fondo de la ventana quedaría recortado
    var instancia = window.bootstrap.Modal.getOrCreateInstance(modal);

    var chips = Array.prototype.slice.call(modal.querySelectorAll('[data-categoria]'));
    var original = chips.filter(function (c) { return c.classList.contains('on'); }).map(function (c) { return c.dataset.categoria; });
    var titulo = modal.querySelector('[data-intereses-titulo]');
    var botonOmitir = modal.querySelector('[data-intereses-omitir]');
    var error = modal.querySelector('[data-intereses-error]');
    var modo = 'ingreso';

    function elegidas() {
        return chips.filter(function (c) { return c.classList.contains('on'); }).map(function (c) { return c.dataset.categoria; });
    }

    function abrir(nuevoModo) {
        modo = nuevoModo;
        error.textContent = '';
        chips.forEach(function (c) {
            c.classList.toggle('on', modo === 'edicion' && original.indexOf(c.dataset.categoria) >= 0);
        });
        titulo.textContent = modo === 'ingreso' ? 'Registrar intereses' : 'Editar intereses';
        // «Ahora no» solo al registrar (la pregunta al ingresar); al editar es «Cancelar»
        botonOmitir.textContent = modo === 'ingreso' ? 'Ahora no' : 'Cancelar';
        instancia.show();
    }

    chips.forEach(function (chip) {
        chip.addEventListener('click', function () {
            chip.classList.toggle('on');
            error.textContent = '';
        });
    });

    function enviar(url, datos) {
        return fetch(url, { method: 'POST', body: datos, headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
            .then(function (respuesta) { return respuesta.json().then(function (cuerpo) { return { ok: respuesta.ok, cuerpo: cuerpo }; }); });
    }

    // La ventana se cierra como todas las del sistema (Cancelar, la X, Esc o un clic afuera). Si se cerró al preguntar al ingresar sin guardar,
    // se avisa al servidor para no volver a preguntar en esta sesión (no guarda nada en la base).
    var guardado = false;
    modal.addEventListener('hidden.bs.modal', function () {
        if (modo === 'ingreso' && !guardado) { enviar('/interes/omitir', new FormData()).catch(function () { /* sin efecto */ }); }
        modo = 'edicion';
    });

    modal.querySelector('[data-intereses-guardar]').addEventListener('click', function (e) {
        var ids = elegidas();
        if (!ids.length) {
            error.textContent = 'Elegí al menos una categoría.';
            return;
        }
        var boton = e.currentTarget;
        var datos = new FormData();
        ids.forEach(function (id) { datos.append('categoriaIds', id); });
        boton.disabled = true;
        enviar('/interes/guardar', datos).then(function (resultado) {
            if (resultado.ok) {
                guardado = true;
                instancia.hide();
                var terminar = function () { window.location.reload(); };
                if (window.mostrarExito) { window.mostrarExito(resultado.cuerpo.mensaje).then(terminar, terminar); } else { terminar(); }
            } else {
                error.textContent = resultado.cuerpo.error || 'Error! Ocurrió un error inesperado. Intente nuevamente.';
                boton.disabled = false;
            }
        }).catch(function () {
            error.textContent = 'Error! Ocurrió un error inesperado. Intente nuevamente.';
            boton.disabled = false;
        });
    });

    document.addEventListener('click', function (e) {
        var disparador = e.target.closest ? e.target.closest('[data-abrir-intereses]') : null;
        if (!disparador) { return; }
        e.preventDefault();
        e.stopPropagation();
        document.querySelectorAll('.um-wrapper.abierto').forEach(function (menu) { menu.classList.remove('abierto'); });
        abrir('edicion');
    }, true);

    // Al ingresar se muestra primero el aviso de inicio de sesión exitoso: la pregunta espera a que la persona lo acepte
    function esperarAlertas(cuando) {
        var intentos = 0;
        (function revisar() {
            var hayAlerta = window.Swal && window.Swal.isVisible && window.Swal.isVisible();
            if (hayAlerta && intentos++ < 400) { setTimeout(revisar, 150); } else { setTimeout(cuando, hayAlerta ? 0 : 250); }
        })();
    }

    if (modal.dataset.preguntar === 'true') { esperarAlertas(function () { abrir('ingreso'); }); }
})();
