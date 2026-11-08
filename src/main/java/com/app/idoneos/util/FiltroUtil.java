package com.app.idoneos.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Filtros y orden de los listados del panel, resueltos en el servidor. Cada listado declara sus columnas una vez; esta
 * utilidad aplica a una lista los filtros y el orden que llegan en los parámetros de la solicitud. La página se arma
 * aparte, con {@link PaginacionUtil}.
 *
 * <p>Cada columna da, para una fila, el mismo valor que la pantalla mostraba como {@code data-f-<campo>}:
 * texto para las columnas de texto y de lista, el número como texto para las numéricas, la fecha ISO
 * ({@code aaaa-mm-dd}, con hora opcional) para las de fecha, y los valores separados por {@code |} cuando una fila tiene
 * varios. Vacío significa "sin dato".</p>
 *
 * <p>Parámetros que entiende: un parámetro por filtro con el nombre de la columna ({@code campo} para texto y listas,
 * {@code campoDesde} y {@code campoHasta} para números y fechas; las listas múltiples pueden repetirlo), y
 * {@code ordenCampo} y {@code ordenDir} ({@code asc} o {@code desc}) para el orden.</p>
 *
 * @param <T> el tipo de fila.
 */
public final class FiltroUtil<T> {

    /** Cómo se filtra una columna. */
    public enum Tipo {
        /** Contiene el texto (sin distinguir tildes ni mayúsculas). */
        TEXTO,
        /** Rango desde/hasta de números. */
        NUMERO,
        /** Rango desde/hasta de fechas ISO (se compara el día). */
        FECHA,
        /** Igual a una opción (la fila puede tener varios valores separados por {@code |}). */
        LISTA,
        /** Alguna de las opciones elegidas. */
        MULTILISTA
    }

    /**
     * Una columna filtrable y ordenable.
     *
     * @param campo nombre de la columna (el {@code data-campo} del encabezado).
     * @param tipo  cómo se filtra.
     * @param valor el valor de la fila para esta columna.
     * @param <T>   el tipo de fila.
     */
    public record Columna<T>(String campo, Tipo tipo, Function<T, String> valor) {
    }

    private final List<Columna<T>> columnas;
    private final Predicate<T> esBaja;

    /**
     * @param columnas las columnas del listado.
     * @param esBaja   indica las filas dadas de baja, que siempre van al final al ordenar.
     */
    public FiltroUtil(List<Columna<T>> columnas, Predicate<T> esBaja) {
        this.columnas = columnas;
        this.esBaja = esBaja;
    }

    // ---------------------------------------------------------------- Filtro

    /**
     * Deja solo las filas que cumplen todos los filtros que llegan en los parámetros.
     *
     * @param todas  la lista completa.
     * @param params los parámetros de la solicitud.
     * @return una lista nueva con las filas que cumplen.
     */
    public List<T> filtrar(List<T> todas, Map<String, String[]> params) {
        List<T> filtradas = new ArrayList<>();
        for (T fila : todas) {
            if (cumple(fila, params)) {
                filtradas.add(fila);
            }
        }
        return filtradas;
    }

    private boolean cumple(T fila, Map<String, String[]> params) {
        for (Columna<T> columna : columnas) {
            String dato = valor(columna, fila);
            switch (columna.tipo()) {
                case TEXTO -> {
                    String buscado = texto(params, columna.campo());
                    if (!buscado.isEmpty() && !normalizar(dato).contains(normalizar(buscado))) {
                        return false;
                    }
                }
                case LISTA -> {
                    String elegido = texto(params, columna.campo());
                    if (!elegido.isEmpty() && !contiene(dato, elegido)) {
                        return false;
                    }
                }
                case MULTILISTA -> {
                    List<String> elegidos = valores(params, columna.campo());
                    if (!elegidos.isEmpty() && elegidos.stream().noneMatch(e -> contiene(dato, e))) {
                        return false;
                    }
                }
                case NUMERO -> {
                    if (!enRangoNumerico(dato, texto(params, columna.campo() + "Desde"), texto(params, columna.campo() + "Hasta"))) {
                        return false;
                    }
                }
                case FECHA -> {
                    if (!enRangoDeFechas(dato, texto(params, columna.campo() + "Desde"), texto(params, columna.campo() + "Hasta"))) {
                        return false;
                    }
                }
                default -> {
                }
            }
        }
        return true;
    }

    private static boolean contiene(String dato, String elegido) {
        return Arrays.stream(dato.toLowerCase(Locale.ROOT).split("\\|")).anyMatch(v -> v.equals(elegido.toLowerCase(Locale.ROOT)));
    }

    private static boolean enRangoNumerico(String dato, String desde, String hasta) {
        if (desde.isEmpty() && hasta.isEmpty()) {
            return true;
        }
        if (dato.isEmpty()) {
            return false;
        }
        try {
            double numero = Double.parseDouble(dato);
            if (!desde.isEmpty() && numero < Double.parseDouble(desde)) {
                return false;
            }
            return hasta.isEmpty() || numero <= Double.parseDouble(hasta);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean enRangoDeFechas(String dato, String desde, String hasta) {
        if (desde.isEmpty() && hasta.isEmpty()) {
            return true;
        }
        if (dato.isEmpty()) {
            return false;
        }
        // Se compara el día: la hora solo sirve para ordenar
        String dia = dato.length() > 10 ? dato.substring(0, 10) : dato;
        return (desde.isEmpty() || dia.compareTo(desde) >= 0) && (hasta.isEmpty() || dia.compareTo(hasta) <= 0);
    }

    // ---------------------------------------------------------------- Orden

    /**
     * Ordena las filas por la columna indicada en los parámetros. Lo vacío y las filas dadas de baja van siempre al final.
     * Sin {@code ordenCampo} se respeta el orden de la lista, pero las filas dadas de baja igual van al final (el orden es
     * estable: entre sí conservan el orden que traían).
     *
     * @param filas  la lista a ordenar (se modifica).
     * @param params los parámetros de la solicitud.
     */
    public void ordenar(List<T> filas, Map<String, String[]> params) {
        String campo = texto(params, "ordenCampo");
        Columna<T> columna = campo.isEmpty() ? null
                : columnas.stream().filter(c -> c.campo().equals(campo)).findFirst().orElse(null);
        if (columna == null) {
            filas.sort((a, b) -> Boolean.compare(esBaja.test(a), esBaja.test(b)));
            return;
        }
        int sentido = "desc".equals(texto(params, "ordenDir")) ? -1 : 1;
        filas.sort((a, b) -> {
            boolean bajaA = esBaja.test(a);
            boolean bajaB = esBaja.test(b);
            if (bajaA != bajaB) {
                return bajaA ? 1 : -1;
            }
            String x = valor(columna, a);
            String y = valor(columna, b);
            if (x.isEmpty() && y.isEmpty()) {
                return 0;
            }
            if (x.isEmpty()) {
                return 1;
            }
            if (y.isEmpty()) {
                return -1;
            }
            return sentido * comparar(columna.tipo(), x, y);
        });
    }

    private static int comparar(Tipo tipo, String x, String y) {
        if (tipo == Tipo.NUMERO) {
            try {
                return Double.compare(Double.parseDouble(x), Double.parseDouble(y));
            } catch (NumberFormatException e) {
                return x.compareTo(y);
            }
        }
        if (tipo == Tipo.FECHA) {
            return x.compareTo(y);
        }
        return normalizar(x).compareTo(normalizar(y));
    }

    // ---------------------------------------------------------------- Opciones

    /**
     * Las opciones de cada columna de lista, sobre toda la lista (sin filtrar), para armar los desplegables.
     *
     * @param todas la lista completa.
     * @return las opciones ordenadas de cada columna de lista, por nombre de columna.
     */
    public Map<String, List<String>> opciones(List<T> todas) {
        Map<String, List<String>> resultado = new LinkedHashMap<>();
        for (Columna<T> columna : columnas) {
            if (columna.tipo() != Tipo.LISTA && columna.tipo() != Tipo.MULTILISTA) {
                continue;
            }
            TreeSet<String> valores = new TreeSet<>(java.text.Collator.getInstance(Locale.forLanguageTag("es")));
            for (T fila : todas) {
                for (String v : valor(columna, fila).split("\\|")) {
                    if (!v.isBlank()) {
                        valores.add(v.trim());
                    }
                }
            }
            resultado.put(columna.campo(), new ArrayList<>(valores));
        }
        return resultado;
    }

    // ---------------------------------------------------------------- Listado completo

    /**
     * Resuelve un listado: filtra, ordena según los parámetros y pagina, con las opciones de los filtros de lista.
     *
     * @param items  Todas las filas del listado.
     * @param params Los parámetros de la petición (filtros, orden y página).
     * @return La página pedida, con el total y las opciones de los filtros.
     */
    public PaginacionUtil.Pagina<T> paginar(List<T> items, Map<String, String[]> params) {
        List<T> filtradas = filtrar(items, params);
        ordenar(filtradas, params);
        return PaginacionUtil.paginar(filtradas, params, opciones(items));
    }

    /**
     * Igual que {@link #paginar(List, Map)}, para las vistas de cards: tamaño de página propio y sin opciones de filtro.
     *
     * @param items    Todas las filas del listado.
     * @param params   Los parámetros de la petición (filtros, orden y página).
     * @param tamanio  Cuántas cards van en cada página.
     * @return La página pedida.
     */
    public PaginacionUtil.Pagina<T> paginar(List<T> items, Map<String, String[]> params, int tamanio) {
        List<T> filtradas = filtrar(items, params);
        ordenar(filtradas, params);
        return PaginacionUtil.paginar(filtradas, params, Map.of(), tamanio);
    }

    /**
     * Igual que {@link #paginar(List, Map)}, pero después de ordenar agrupa las filas con el comparador dado (por ejemplo, por
     * categoría). El orden previo se conserva dentro de cada grupo, porque la ordenación es estable.
     *
     * @param items      Todas las filas del listado.
     * @param params     Los parámetros de la petición.
     * @param agrupacion Cómo se agrupan las filas, o {@code null} para no reagrupar.
     * @return La página pedida.
     */
    public PaginacionUtil.Pagina<T> paginar(List<T> items, Map<String, String[]> params, java.util.Comparator<? super T> agrupacion) {
        List<T> filtradas = filtrar(items, params);
        ordenar(filtradas, params);
        if (agrupacion != null) {
            filtradas.sort(agrupacion);
        }
        return PaginacionUtil.paginar(filtradas, params, opciones(items));
    }

    /**
     * Igual que {@link #paginar(List, Map)}, pero si se busca una fila concreta (por ejemplo, la que se va a modificar o dar de
     * baja al llegar desde otra pantalla) devuelve la página que la contiene, para que su botón esté en pantalla.
     *
     * @param items   Todas las filas del listado.
     * @param params  Los parámetros de la petición (filtros, orden y página).
     * @param buscada Identifica la fila buscada (nunca verdadero si no se busca ninguna).
     * @return La página pedida, o la que contiene la fila buscada si no está en ella.
     */
    public PaginacionUtil.Pagina<T> paginarMostrando(List<T> items, Map<String, String[]> params,
            java.util.function.Predicate<T> buscada) {
        PaginacionUtil.Pagina<T> pagina = paginar(items, params);
        if (pagina.filas().stream().anyMatch(buscada)) {
            return pagina;
        }
        for (int n = 1; n <= pagina.paginas(); n++) {
            Map<String, String[]> otros = new java.util.HashMap<>(params);
            otros.put("pagina", new String[] { String.valueOf(n) });
            PaginacionUtil.Pagina<T> candidata = paginar(items, otros);
            if (candidata.filas().stream().anyMatch(buscada)) {
                return candidata;
            }
        }
        return pagina;
    }

    /**
     * Igual que {@link #paginar(List, Map, int)} (cards), pero agrupando como {@link #paginar(List, Map, java.util.Comparator)}.
     *
     * @param items      Todas las filas del listado.
     * @param params     Los parámetros de la petición.
     * @param tamanio    Cuántas cards van en cada página.
     * @param agrupacion Cómo se agrupan las filas, o {@code null} para no reagrupar.
     * @return La página pedida.
     */
    public PaginacionUtil.Pagina<T> paginar(List<T> items, Map<String, String[]> params, int tamanio, java.util.Comparator<? super T> agrupacion) {
        List<T> filtradas = filtrar(items, params);
        ordenar(filtradas, params);
        if (agrupacion != null) {
            filtradas.sort(agrupacion);
        }
        return PaginacionUtil.paginar(filtradas, params, Map.of(), tamanio);
    }

    /**
     * Cuenta las filas que cumplen los filtros, por grupo (en todo el listado, no solo en la página pedida).
     *
     * @param items  Todas las filas del listado.
     * @param params Los parámetros de la petición.
     * @param clave  El grupo de cada fila.
     * @param <K>    El tipo de la clave del grupo.
     * @return La cantidad de filas filtradas de cada grupo.
     */
    public <K> Map<K, Long> contar(List<T> items, Map<String, String[]> params, java.util.function.Function<T, K> clave) {
        return filtrar(items, params).stream().collect(java.util.stream.Collectors.groupingBy(clave, java.util.LinkedHashMap::new,
                java.util.stream.Collectors.counting()));
    }

    /**
     * Arma los chips de categoría de un listado, en JSON: para cada valor del campo, cuántas filas hay con los demás filtros
     * aplicados (sin el del propio campo, para que cada chip muestre lo que daría elegirlo) y cuál está elegido.
     *
     * @param items  Todas las filas del listado.
     * @param params Los parámetros de la petición.
     * @param campo  El parámetro de filtro que elige el chip (por ejemplo {@code categoriaId}).
     * @param valor  El valor de la fila para ese campo (lo que se envía al elegir el chip).
     * @param nombre El texto del chip para esa fila.
     * @return El JSON {@code {campo, activa, total, items:[{valor, nombre, total}]}}.
     */
    public String armarChips(List<T> items, Map<String, String[]> params, String campo, java.util.function.Function<T, String> valor,
            java.util.function.Function<T, String> nombre) {
        Map<String, String[]> sinCampo = new java.util.HashMap<>(params);
        sinCampo.remove(campo);
        List<T> filtradas = filtrar(items, sinCampo);
        Map<String, Long> cuentas = new LinkedHashMap<>();
        Map<String, String> nombres = new LinkedHashMap<>();
        for (T fila : filtradas) {
            String v = valor.apply(fila);
            cuentas.merge(v, 1L, Long::sum);
            nombres.putIfAbsent(v, nombre.apply(fila));
        }
        java.text.Collator colador = java.text.Collator.getInstance(Locale.forLanguageTag("es"));
        List<Map<String, Object>> chips = cuentas.keySet().stream()
                .sorted((a, b) -> colador.compare(nombres.get(a), nombres.get(b)))
                .map(v -> Map.<String, Object>of("valor", v, "nombre", nombres.get(v), "total", cuentas.get(v))).toList();
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("campo", campo);
        cuerpo.put("activa", texto(params, campo));
        cuerpo.put("total", (long) filtradas.size());
        cuerpo.put("items", chips);
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(cuerpo);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return "{}";
        }
    }

    // ---------------------------------------------------------------- Fechas para las columnas

    /**
     * Da el día de una fecha como lo esperan las columnas de tipo fecha: {@code aaaa-mm-dd}.
     *
     * @param fecha la fecha, o {@code null}.
     * @return el día, o vacío si no hay fecha.
     */
    public static String formatearFecha(java.time.LocalDateTime fecha) {
        return fecha == null ? "" : fecha.toLocalDate().toString();
    }

    /**
     * Da la fecha con hora como la esperan las columnas de tipo fecha: {@code aaaa-mm-dd hh:mm}.
     *
     * @param fecha la fecha, o {@code null}.
     * @return la fecha con hora, o vacío si no hay fecha.
     */
    public static String formatearFechaHora(java.time.LocalDateTime fecha) {
        return fecha == null ? "" : fecha.toString().replace('T', ' ');
    }

    // ---------------------------------------------------------------- Utilidades

    private static String valor(Columna<?> columna, Object fila) {
        @SuppressWarnings("unchecked")
        String v = ((Function<Object, String>) columna.valor()).apply(fila);
        return v == null ? "" : v.trim();
    }

    private static String texto(Map<String, String[]> params, String nombre) {
        String[] v = params.get(nombre);
        return v == null || v.length == 0 || v[0] == null ? "" : v[0].trim();
    }

    private static List<String> valores(Map<String, String[]> params, String nombre) {
        String[] v = params.get(nombre);
        if (v == null) {
            return List.of();
        }
        return Arrays.stream(v).filter(s -> s != null && !s.isBlank()).map(String::trim).toList();
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim();
    }
}
