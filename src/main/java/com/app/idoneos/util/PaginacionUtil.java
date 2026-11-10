package com.app.idoneos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Utilidad de paginación de los listados del panel. */
public final class PaginacionUtil {

    /** Filas por página cuando la solicitud no indica otra cosa. */
    public static final int TAMANIO_POR_DEFECTO = 10;

    private PaginacionUtil() {
    }

    /**
     * Una página de un listado.
     *
     * @param filas las filas de la página pedida.
     * @param total cuántas filas hay en total (todas las páginas), ya filtradas.
     * @param pagina la página devuelta (desde 1).
     * @param paginas cuántas páginas hay.
     * @param tamanio el tamaño de página usado (0 = todo).
     * @param opciones las opciones de las columnas de lista, para los filtros.
     */
    public record Pagina<T>(List<T> filas, int total, int pagina, int paginas, int tamanio, Map<String, List<String>> opciones) {
    }

    /**
     * Recorta la lista a la página que piden los parámetros.
     *
     * @param filtradas la lista ya filtrada y ordenada.
     * @param params los parámetros de la solicitud.
     * @param opciones las opciones de los filtros de lista, que viajan junto con la página.
     * @return la página pedida, acotada a las páginas que existen.
     */
    public static <T> Pagina<T> paginar(List<T> filtradas, Map<String, String[]> params, Map<String, List<String>> opciones) {
        return paginar(filtradas, params, opciones, TAMANIO_POR_DEFECTO);
    }

    /**
     * Igual que paginar, pero con otro tamaño de página por defecto (las vistas de cards muestran 8).
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
     * Deja en el modelo los datos del paginador y de los filtros que necesita la pantalla: tablaTotal.
     *
     * @param model el modelo de la vista.
     * @param pagina lo que devolvió {@link #paginar}.
     */
    public static void cargarEnModelo(org.springframework.ui.Model model, Pagina<?> pagina) {
        model.addAttribute("tablaTotal", pagina.total());
        model.addAttribute("tablaPagina", pagina.pagina());
        model.addAttribute("tablaPaginas", pagina.paginas());
        model.addAttribute("tablaTamanio", pagina.tamanio());
        try {
            model.addAttribute("tablaOpciones", new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(pagina.opciones()));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            model.addAttribute("tablaOpciones", "{}");
        }
    }

    /**
     * Indica si la solicitud pide solo las filas de la tabla.
     *
     * @param request la solicitud HTTP.
     * @return {@code true} si trae el encabezado {@code X-Tabla: filas}.
     */
    public static boolean pideSoloFilas(jakarta.servlet.http.HttpServletRequest request) {
        return "filas".equals(request.getHeader("X-Tabla"));
    }

    private static String texto(Map<String, String[]> params, String nombre) {
        String[] v = params.get(nombre);
        return v == null || v.length == 0 || v[0] == null ? "" : v[0].trim();
    }
}
