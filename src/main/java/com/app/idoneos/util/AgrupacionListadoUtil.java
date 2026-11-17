package com.app.idoneos.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Agrupa las filas de un listado por un valor (por ejemplo, las cohortes por curso): ordena por el grupo, deja en el modelo la clave de grupo de cada fila de
 * la página y la cantidad de filas de cada grupo en todo el listado filtrado. La plantilla dibuja un encabezado cuando cambia el grupo.
 * Si el usuario ordena por otra columna, el listado se desagrupa.
 */
public final class AgrupacionListadoUtil {

    private AgrupacionListadoUtil() {
    }

    /**
     * Pide la página de un listado agrupado.
     *
     * @param filtro El motor de filtro y orden del listado.
     * @param items Todas las filas.
     * @param params Los parámetros de la petición (con los filtros que se apliquen).
     * @param request La petición, de la que se lee la columna por la que se ordena.
     * @param model El modelo: recibe {@code agrupar}, {@code clavesGrupo} y {@code conteoGrupos}.
     * @param clave El grupo de cada fila.
     * @param dentro Cómo se ordenan las filas dentro de cada grupo (después del orden del usuario), o {@code null}.
     * @param buscada Para saltar a la página que contiene una fila, o {@code null}.
     * @param columnasQueMantienen Las columnas por las que se puede ordenar sin desagrupar (la del grupo y la de orden inicial).
     */
    public static <T> PaginacionUtil.Pagina<T> paginar(FiltroUtil<T> filtro, List<T> items, Map<String, String[]> params, HttpServletRequest request,
            Model model, Function<T, String> clave, Comparator<T> dentro, Predicate<T> buscada, String... columnasQueMantienen) {
        return paginar(filtro, items, params, request, model, clave, null, dentro, buscada, columnasQueMantienen);
    }

    /**
     * Igual que la otra variante, pero los grupos que cumplen {@code primeros} van antes que los demás (por ejemplo, el equipo docente antes que las
     * cohortes de alumnos).
     */
    public static <T> PaginacionUtil.Pagina<T> paginar(FiltroUtil<T> filtro, List<T> items, Map<String, String[]> params, HttpServletRequest request,
            Model model, Function<T, String> clave, Predicate<T> primeros, Comparator<T> dentro, Predicate<T> buscada, String... columnasQueMantienen) {
        String campo = request.getParameter("ordenCampo");
        boolean agrupar = campo == null || campo.isBlank() || Arrays.asList(columnasQueMantienen).contains(campo);
        Function<T, String> grupo = fila -> {
            String valor = clave.apply(fila);
            return valor == null || valor.isBlank() ? "Sin dato" : valor;
        };
        Comparator<T> orden = Comparator.comparing(grupo, String.CASE_INSENSITIVE_ORDER);
        if (primeros != null) {
            orden = Comparator.comparing((T fila) -> !primeros.test(fila)).thenComparing(orden);
        }
        if (dentro != null) {
            orden = orden.thenComparing(dentro);
        }
        Comparator<T> agrupacion = agrupar ? orden : null;
        PaginacionUtil.Pagina<T> pagina = buscada == null ? filtro.paginar(items, params, agrupacion)
                : filtro.paginarMostrando(items, params, buscada, agrupacion);
        model.addAttribute("agrupar", agrupar);
        model.addAttribute("conteoGrupos", agrupar ? filtro.contar(items, params, grupo) : Map.of());
        model.addAttribute("clavesGrupo", agrupar ? pagina.filas().stream().map(grupo).toList() : List.of());
        return pagina;
    }
}
