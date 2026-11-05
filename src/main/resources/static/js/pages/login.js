/**
 * Pantalla de inicio de sesión: fondo con imágenes que se alternan, botón para mostrar u ocultar la contraseña
 * y aviso de la recuperación de contraseña.
 */
(function () {
    'use strict';

    var INTERVALO = 5000;

    var diapositivas = document.querySelectorAll('.auth-fondo-slide');
    if (diapositivas.length > 1) {
        var actual = 0;
        setInterval(function () {
            diapositivas[actual].classList.remove('activo');
            actual = (actual + 1) % diapositivas.length;
            diapositivas[actual].classList.add('activo');
        }, INTERVALO);
    }

    // La recuperación de contraseña se incorporará con su caso de uso; por ahora se informa cómo proceder
    var olvide = document.getElementById('olvideClave');
    if (olvide) {
        olvide.addEventListener('click', function () {
            window.mostrarInformacion('Recuperar contraseña',
                'La recuperación de contraseña estará disponible próximamente. Mientras tanto, comunicate con la administración de Idóneos Online.');
        });
    }

    // Los botones de registro todavía no realizan ninguna acción (se incorporarán con su caso de uso)

    var boton = document.getElementById('verClave');
    var campo = document.getElementById('contrasena');
    if (boton && campo) {
        boton.addEventListener('click', function () {
            var visible = campo.type === 'text';
            campo.type = visible ? 'password' : 'text';
            boton.setAttribute('aria-label', visible ? 'Mostrar contraseña' : 'Ocultar contraseña');
            boton.querySelector('i').className = visible ? 'fa-regular fa-eye' : 'fa-regular fa-eye-slash';
        });
    }
})();
