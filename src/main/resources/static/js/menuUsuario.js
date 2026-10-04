/**
 * Menú desplegable del usuario: abre y cierra el menú y cambia el rol por defecto.
 */
(function () {
    'use strict';

    document.addEventListener('click', function (evento) {
        var disparador = evento.target.closest('[data-menu-usuario]');
        // Se cierran los menús abiertos cuando el clic fue fuera de ellos (un clic dentro del menú, por ejemplo
        // en el selector del rol, no lo cierra)
        document.querySelectorAll('.um-wrapper.abierto').forEach(function (menu) {
            if (!menu.contains(evento.target)) {
                menu.classList.remove('abierto');
                menu.querySelector('[data-menu-usuario]').setAttribute('aria-expanded', 'false');
            }
        });
        if (disparador) {
            var wrapper = disparador.closest('.um-wrapper');
            var abierto = wrapper.classList.toggle('abierto');
            disparador.setAttribute('aria-expanded', abierto ? 'true' : 'false');
        }
    });

    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape') {
            document.querySelectorAll('.um-wrapper.abierto').forEach(function (menu) {
                menu.classList.remove('abierto');
            });
        }
    });

    // Cambio del rol por defecto: se guarda y se va a la pantalla del nuevo rol
    document.addEventListener('change', async function (evento) {
        var selector = evento.target.closest('[data-selector-rol]');
        if (!selector) {
            return;
        }
        selector.disabled = true;
        try {
            var datos = new FormData();
            datos.append('idRol', selector.value);
            var respuesta = await fetch('/usuario/cambiarRolPorDefecto', {
                method: 'POST',
                body: datos,
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var resultado = await respuesta.json();
            if (respuesta.ok && resultado.mensaje) {
                await window.mostrarExito(resultado.mensaje);
                window.location.href = resultado.destino || '/inicio';
                return;
            }
            await window.mostrarError(resultado.error || 'Error! Ocurrió un error inesperado. Intente nuevamente.');
        } catch (error) {
            await window.mostrarError('Error! No se pudo conectar con el servidor. Intente nuevamente.');
        }
        window.location.reload();
    });
})();
