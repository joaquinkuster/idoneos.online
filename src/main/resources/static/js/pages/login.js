/** Pantallas de acceso: fondo con imágenes que se alternan, contraseña visible, atajos entre pantallas y validación. */
(function () {
    'use strict';

    var INTERVALO = 5000;
    var FORMATO_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    // ----------------------------------------------------------------------
    // Fondo
    // ----------------------------------------------------------------------

    var diapositivas = document.querySelectorAll('.auth-fondo-slide');
    if (diapositivas.length > 1) {
        var actual = 0;
        setInterval(function () {
            diapositivas[actual].classList.remove('activo');
            actual = (actual + 1) % diapositivas.length;
            diapositivas[actual].classList.add('activo');
        }, INTERVALO);
    }

    // ----------------------------------------------------------------------
    // Contraseña visible
    // ----------------------------------------------------------------------

    document.querySelectorAll('[data-ver-clave]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            var campo = boton.parentNode.querySelector('input');
            if (!campo) {
                return;
            }
            var visible = campo.type === 'text';
            campo.type = visible ? 'password' : 'text';
            boton.setAttribute('aria-label', visible ? 'Mostrar contraseña' : 'Ocultar contraseña');
            boton.querySelector('i').className = visible ? 'fa-regular fa-eye' : 'fa-regular fa-eye-slash';
        });
    });

    // ----------------------------------------------------------------------
    // Atajos entre pantallas
    // ----------------------------------------------------------------------

    document.querySelectorAll('[data-registro]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            window.location.href = '/registro';
        });
    });
    document.querySelectorAll('[data-login]').forEach(function (boton) {
        boton.addEventListener('click', function () {
            window.location.href = '/login';
        });
    });

    // ----------------------------------------------------------------------
    // Validación de los formularios de acceso
    // ----------------------------------------------------------------------

    window.PanelValidadores = window.PanelValidadores || {};
    window.PanelValidadores.auth = function (paso, formulario) {
        var marcar = window.PanelValidacion.marcar;
        var valido = true;
        var campos = formulario.elements;

        if (campos.correo && campos.correo.value.trim() && !FORMATO_CORREO.test(campos.correo.value.trim())) {
            marcar(campos.correo, 'No es válido. Formato válido: nombre@dominio.com.');
            valido = false;
        }
        if (campos.dni && campos.dni.value.trim() && !/^[0-9]{7,8}$/.test(campos.dni.value.trim())) {
            marcar(campos.dni, 'No es válido. Formato válido: 7 u 8 dígitos.');
            valido = false;
        }
        if (campos.contrasena && campos.contrasena.value && campos.contrasena.value.length < 8) {
            marcar(campos.contrasena, 'Es demasiado corta. Mínimo: 8 caracteres.');
            valido = false;
        }
        if (campos.confirmarContrasena && campos.contrasena && campos.confirmarContrasena.value
                && campos.confirmarContrasena.value !== campos.contrasena.value) {
            marcar(campos.confirmarContrasena, 'Las contraseñas no coinciden.');
            valido = false;
        }
        return valido;
    };
})();
