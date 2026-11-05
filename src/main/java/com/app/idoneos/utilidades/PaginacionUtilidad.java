package com.app.idoneos.utilidades;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Paginación de los listados del panel, resuelta en el servidor: recorta la lista a la página pedida y deja en el modelo
 * lo que la pantalla necesita para dibujar el paginador. Los filtros y el orden se aplican antes, con
 * {@link FiltroUtilidad}.
 *
 * <p>Parámetros que entiende: {@code pagina} (desde 1) y {@code tamanio} (filas por página; 0 = todo; por defecto 10).</p>
 */
public final class PaginacionUtilidad {

    /** Filas por página cuando la solicitud no indica otra cosa. */
    public static final int TAMANIO_POR_DEFECTO = 10;

    private PaginacionUtilidad() {
    }

    /**
     * Una página de un listado.
     *
     * @param filas    las filas de la página pedida.
     * @param total    cuántas filas hay en total (todas las páginas), ya filtradas.
     * @param pagina   la página devuelta (desde 1).
     * @param paginas  cuántas páginas hay.
     * @param tamanio  el tamaño de página usado (0 = todo).
     * @param opciones las opciones de las columnas de lista, para los filtros.
     * @param <T>      el tipo de fila.
     */
    public record Pagina<T>(List<T> filas, int total, int pagina, int paginas, int tamanio, Map<String, List<String>> opciones) {
    }

    /**
     * Recorta la lista a la página que piden los parámetros.
     *
     * @param filtradas la lista ya filtrada y ordenada.
     * @param params    los parámetros de la solicitud.
     * @param opciones  las opciones de los filtros de lista, que viajan junto con la página.
     * @param <T>       el tipo de fila.
     * @return la página pedida, acotada a las páginas que existen.
     */
    public static <T> Pagina<T> paginar(List<T> filtradas, Map<String, String[]> params, Map<String, List<String>> opciones) {
        return paginar(filtradas, params, opciones, TAMANIO_POR_DEFECTO);
    }

    /**
     * Igual que {@link #paginar(List, Map, Map)} pero con otro tamaño de página por defecto (las vistas de tarjetas
     * muestran 8).
     *
     * @param tamanioPorDefecto las filas por página cuando la solicitud no indica otro tamaño.
     */
    public static <T> Pagina<T> paginar(List<T> filtradas, Map<String, String[]> params, Map<String, List<String>> opciones,
            int tamanioPorDefecto) {
        int tamanio = tamanioPorDefecto;
        String pedido = texto(params, "tamanio");
        if (!pedido.isEmpty()) {
            try {
                tamanio = Math.max(0, Integer.parseInt(pedido));
            } catch (NumberFormatException e) {
                tamanio = tamanioPorDefecto;
            }
        }
        int total = filtradas.size();
        int efectivo = tamanio > 0 ? tamanio : Math.max(total, 1);
        int paginas = Math.max(1, (int) Math.ceil(total / (double) efectivo));
        int pagina;
        try {
            pagina = Integer.parseInt(texto(params, "pagina"));
        } catch (NumberFormatException e) {
            pagina = 1;
        }
        pagina = Math.min(Math.max(pagina, 1), paginas);
        int desde = (pagina - 1) * efectivo;
        List<T> filas = filtradas.subList(Math.min(desde, total), Math.min(desde + efectivo, total));
        return new Pagina<>(new ArrayList<>(filas), total, pagina, paginas, tamanio, opciones);
    }

    /**
     * Deja en el modelo los datos del paginador y de los filtros que necesita la pantalla: tablaTotal, tablaPagina,
     * tablaPaginas, tablaTamanio y tablaOpciones (las opciones de las listas, en JSON).
     *
     * @param modelo el modelo de la vista.
     * @param pagina lo que devolvió {@link #paginar}.
     */
    public static void aModelo(org.springframework.ui.Model modelo, Pagina<?> pagina) {
        modelo.addAttribute("tablaTotal", pagina.total());
        modelo.addAttribute("tablaPagina", pagina.pagina());
        modelo.addAttribute("tablaPaginas", pagina.paginas());
        modelo.addAttribute("tablaTamanio", pagina.tamanio());
        try {
            modelo.addAttribute("tablaOpciones", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(pagina.opciones()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            modelo.addAttribute("tablaOpciones", "{}");
        }
    }

    /**
     * Indica si la solicitud pide solo las filas de la tabla (la que hace el navegador al filtrar, ordenar o cambiar de página).
     *
     * @param solicitud la solicitud HTTP.
     * @return {@code true} si trae el encabezado {@code X-Tabla: filas}.
     */
    public static boolean pideSoloFilas(jakarta.servlet.http.HttpServletRequest solicitud) {
        return "filas".equals(solicitud.getHeader("X-Tabla"));
    }

    private static String texto(Map<String, String[]> params, String nombre) {
        String[] v = params.get(nombre);
        return v == null || v.length == 0 || v[0] == null ? "" : v[0].trim();
    }
}
