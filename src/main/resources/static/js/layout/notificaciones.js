/** Campana de notificaciones del navbar: abre el panel, lo carga, marca todas como leídas y mantiene al día el contador. */
(function () {
    'use strict';

    var envoltorio = document.querySelector('.nt-wrapper');
    if (!envoltorio) {
        return;
    }
    var boton = envoltorio.querySelector('[data-notificaciones]');
    var contador = envoltorio.querySelector('[data-nt-contador]');
    var lista = envoltorio.querySelector('[data-nt-lista]');

    function abierto() {
        return envoltorio.classList.contains('abierto');
    }

    var verTodas = envoltorio.querySelector('[data-nt-ver-todas]');
    var expandido = false;

    /** Alterna entre las últimas notificaciones y todas, con la bandeja más alta y su propio desplazamiento. */
    function expandir(todas) {
        expandido = todas;
        envoltorio.classList.toggle('expandido', todas);
        verTodas.textContent = todas ? 'Ver menos' : 'Ver todas';
        cargar();
    }

    function cargar() {
        fetch('/notificacion/panel' + (expandido ? '?todas=true' : ''), { headers: { 'X-Requested-With': 'XMLHttpRequest' }, credentials: 'same-origin' })
            .then(function (respuesta) { return respuesta.ok ? respuesta.text() : Promise.reject(); })
            .then(function (html) { lista.innerHTML = html; })
            .catch(function () {
                lista.innerHTML = '<div class="nt-vacio"><i class="fa-regular fa-bell-slash"></i><p>No se pudieron cargar las notificaciones.</p></div>';
            });
    }

    function cerrar() {
        envoltorio.classList.remove('abierto');
        boton.setAttribute('aria-expanded', 'false');
        // La próxima vez que se abra vuelve a mostrar solo las últimas
        expandido = false;
        envoltorio.classList.remove('expandido');
        verTodas.textContent = 'Ver todas';
    }

    verTodas.addEventListener('click', function () { expandir(!expandido); });

    boton.addEventListener('click', function (e) {
        e.stopPropagation();
        var abrir = !abierto();
        envoltorio.classList.toggle('abierto', abrir);
        boton.setAttribute('aria-expanded', String(abrir));
        if (abrir) { cargar(); }
    });

    document.addEventListener('click', function (e) {
        if (abierto() && !envoltorio.contains(e.target)) { cerrar(); }
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && abierto()) { cerrar(); boton.focus(); }
    });

    /** Muestra la cantidad de notificaciones sin leer. La llama el latido de la sesión cada minuto. */
    window.actualizarNotificaciones = function (cantidad) {
        var anterior = parseInt(contador.dataset.cantidad || '0', 10);
        contador.dataset.cantidad = String(cantidad);
        contador.textContent = cantidad > 99 ? '99+' : String(cantidad);
        contador.classList.toggle('d-none', !cantidad);
        // Si llegaron avisos nuevos con el panel abierto, la lista se actualiza sola
        if (abierto() && cantidad > anterior) { cargar(); }
    };
    contador.dataset.cantidad = String(parseInt(contador.textContent, 10) || 0);

    // Marcar todas como leídas, desde el panel
    document.addEventListener('click', function (e) {
        var marcar = e.target.closest('[data-nt-marcar-todas]');
        if (!marcar) { return; }
        e.preventDefault();
        fetch('/notificacion/leerTodas', { method: 'POST', credentials: 'same-origin' })
            .then(function () {
                window.actualizarNotificaciones(0);
                if (abierto()) { cargar(); }
            });
    });
})();
