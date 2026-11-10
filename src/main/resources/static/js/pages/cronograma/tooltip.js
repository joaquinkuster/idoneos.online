/** Leyenda flotante del cronograma: al pasar el mouse por una barra de tema o por la línea roja del día actual (cualquier elemento con data-tip) se muestra su texto junto al cursor. */
(function () {
    'use strict';

    var globo = document.createElement('div');
    globo.className = 'cn-tooltip';
    globo.hidden = true;
    globo.setAttribute('role', 'tooltip');
    document.body.appendChild(globo);

    function ocultar() { globo.hidden = true; }

    document.addEventListener('mousemove', function (e) {
        var origen = e.target.closest ? e.target.closest('[data-tip]') : null;
        if (!origen) { ocultar(); return; }
        globo.textContent = origen.dataset.tip;
        globo.hidden = false;
        var x = e.clientX + 14;
        var y = e.clientY + 16;
        var ancho = globo.offsetWidth;
        var alto = globo.offsetHeight;
        if (x + ancho > window.innerWidth - 8) { x = e.clientX - ancho - 14; }
        if (y + alto > window.innerHeight - 8) { y = e.clientY - alto - 12; }
        globo.style.left = Math.max(8, x) + 'px';
        globo.style.top = Math.max(8, y) + 'px';
    });
    document.addEventListener('mouseleave', ocultar);
    document.addEventListener('dragstart', ocultar);
    document.addEventListener('scroll', ocultar, true);
})();
