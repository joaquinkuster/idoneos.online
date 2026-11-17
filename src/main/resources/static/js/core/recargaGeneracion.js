/**
 * Mientras algo se genera con IA (una insignia con data-generando en el listado), la página mira cada tanto si ya terminó y, solo entonces, se recarga.
 * Mientras sigue pendiente no se recarga: se pediría la página entera cada vez sin que cambie nada.
 */
(function () {
    'use strict';

    function contar(texto) { return (texto.match(/data-generando/g) || []).length; }

    var pendientes = document.querySelectorAll('[data-generando]').length;
    if (!pendientes) { return; }

    var consultando = false;
    var reloj = setInterval(function () {
        if (consultando) { return; }
        consultando = true;
        fetch(window.location.href, { credentials: 'same-origin', headers: { 'Accept': 'text/html' } })
            .then(function (r) { return r.ok ? r.text() : null; })
            .then(function (html) {
                consultando = false;
                if (html === null || contar(html) >= pendientes) { return; }
                // Terminó alguno: se espera a que no haya una ventana abierta para no perder lo que el docente escribe
                clearInterval(reloj);
                var esperar = setInterval(function () {
                    if (!document.querySelector('.modal.show')) { clearInterval(esperar); window.location.reload(); }
                }, 1000);
            })
            .catch(function () { consultando = false; });
    }, 10000);
})();
