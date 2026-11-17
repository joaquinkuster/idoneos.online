package com.app.idoneos.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.app.idoneos.service.estadistica.EstadisticaService.Barra;
import com.app.idoneos.service.estadistica.EstadisticaService.Evolucion;
import com.app.idoneos.service.estadistica.EstadisticaService.InformeAlumnos;
import com.app.idoneos.service.estadistica.EstadisticaService.InformeIngresos;
import com.app.idoneos.service.estadistica.EstadisticaService.Serie;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/** Arma los PDF de los informes de alumnos y de ingresos de un curso (2 páginas A4 con encabezado, indicadores y gráficos). */
public final class InformePdfUtil {

    private InformePdfUtil() {
    }

    // ----------------------------------------------------------------------
    // Medidas y colores del diseño
    // ----------------------------------------------------------------------

    private static final float MARGEN = 36;
    private static final float ANCHO = 523;
    private static final float ALTO_PAGINA = 842;

    /** Posición (desde arriba) hasta donde llega el contenido de una página: debajo va el pie. */
    private static final float LIMITE = 778;

    /** Radio de la torta de ingresos por categoría. */
    private static final float RADIO_TORTA = 48;

    private static final Color NAVY = new Color(0x0B, 0x14, 0x24);
    private static final Color PIZARRA = new Color(0x2D, 0x4A, 0x70);
    private static final Color DORADO = PdfUtil.DORADO;
    private static final Color VERDE = new Color(0x05, 0x96, 0x69);
    private static final Color ROJO = new Color(0xDC, 0x26, 0x26);
    private static final Color AZUL = new Color(0x25, 0x63, 0xEB);
    private static final Color NARANJA = new Color(0xD9, 0x77, 0x06);
    private static final Color GRIS = new Color(0x64, 0x74, 0x8B);
    private static final Color GRIS_OSCURO = new Color(0x33, 0x41, 0x55);
    private static final Color BORDE = new Color(0xE2, 0xE8, 0xF0);
    private static final Color PISTA = new Color(0xF1, 0xF5, 0xF9);
    private static final Color FONDO_TARJETA = new Color(0xF8, 0xFA, 0xFC);
    private static final Color EJES = new Color(0xCB, 0xD5, 0xE1);
    private static final Color[] PALETA = { AZUL, VERDE, DORADO, ROJO, new Color(0x7C, 0x3A, 0xED), PIZARRA };
    private static final Color[] PALETA_TORTA = { NAVY, DORADO, new Color(0x64, 0x74, 0x8B), VERDE, AZUL, new Color(0xCB, 0xD5, 0xE1) };

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat ENTERO = new DecimalFormat("#,##0", new DecimalFormatSymbols(new Locale("es", "AR")));

    // ----------------------------------------------------------------------
    // Informe de alumnos
    // ----------------------------------------------------------------------

    /**
     * Genera el PDF del informe de alumnos de un curso.
     *
     * @param datos los datos del informe
     * @param emisor nombre de quien lo genera
     * @param empresa razón social que va en el pie
     * @return el contenido del PDF
     */
    public static byte[] alumnos(InformeAlumnos datos, String emisor, String empresa) {
        return armar((h, pagina) -> {
            String periodo = FECHA.format(datos.desde()) + " al " + FECHA.format(datos.hasta());
            String pie = "INFORME DE ALUMNOS POR CURSO • " + empresa.toUpperCase(Locale.ROOT);
            if (pagina == 1) {
                float top = encabezado(h, "INFORME DE ALUMNOS POR CURSO",
                        "Período: " + periodo + " • Emitido: " + FECHA.format(LocalDate.now()) + " por " + emisor);
                top = cursoYPrograma(h, top, datos.curso().getNombre(), datos.programaVigente());
                top = indicadores(h, top, new String[] { ENTERO.format(datos.total()), ENTERO.format(datos.completadas()), ENTERO.format(datos.vigentes()),
                        ENTERO.format(datos.bajas()) }, new String[] { "TOTAL INSCRIPCIONES", "COMPLETADAS (CERTIF.)", "ACTIVAS / VIGENTES", "DADAS DE BAJA (ABANDONO)" },
                        new Color[] { NAVY, VERDE, AZUL, ROJO });
                top = tarjetaBarras(h, top, MARGEN, ANCHO, "1. COMPARACIÓN DE INSCRIPCIONES ENTRE CURSOS",
                        "Volumen total de inscriptos en el período, contrastando el curso seleccionado con el catálogo.", datos.porCurso(),
                        v -> ENTERO.format((long) v) + " alumnos", v -> ENTERO.format((long) v) + " unid.", NAVY, PIZARRA, " (Seleccionado)", 0);
                tarjetaEvolucion(h, top + 12, LIMITE, "2. EVOLUCIÓN DIARIA DE DEMANDA", "Comportamiento de las inscripciones registradas a lo largo del período seleccionado, con una línea por programa.",
                        datos.evolucion(), false);
            } else {
                float top = encabezado(h, "INFORME DE ALUMNOS (CONT.)", "Curso: " + datos.curso().getNombre() + " • Período: " + periodo);
                top = tarjetaAbandono(h, top + 14, datos);
                double total = datos.porPrograma().stream().mapToDouble(Barra::valor).sum();
                List<Barra> programas = datos.porPrograma().stream().limit(6).toList();
                tarjetaBarras(h, top + 12, MARGEN, ANCHO, "4. EFECTIVIDAD POR PROGRAMA ACADÉMICO",
                        "Comparativa entre versiones de programa para identificar cuál captó mayor volumen de alumnos.", programas,
                        v -> ENTERO.format((long) v) + " alumnos" + (total > 0 ? " (" + Math.round(v * 100 / total) + "%)" : ""),
                        v -> ENTERO.format((long) v) + " unid.", VERDE, PIZARRA, "", LIMITE);
            }
            pie(h, pie, pagina, 2);
        });
    }

    private static float tarjetaAbandono(Hoja h, float top, InformeAlumnos d) {
        float alto = 182;
        float interior = tarjeta(h, MARGEN, top, ANCHO, alto, "3. TASA DE ABANDONO Y RETENCIÓN DE CURSADA",
                "Proporción de alumnos activos/completados frente a bajas o deserciones registradas.");
        double abandono = d.porcentajeAbandono();
        float radio = 56;
        float cx = MARGEN + 15 + radio + 20;
        float centro = interior + 6 + radio;
        float cy = h.y(centro);
        double[] partes = d.total() == 0 ? new double[] { 1, 0 } : new double[] { d.retenidas(), d.bajas() };
        sectores(h, cx, cy, radio, partes, new Color[] { VERDE, ROJO });
        h.circulo(cx, cy, 32, Color.WHITE);
        h.texto(String.format(Locale.US, "%.1f%%", abandono), cx, cy, h.negrita, 14, NAVY, PdfContentByte.ALIGN_CENTER);
        h.texto("ABANDONO", cx, cy - 10, h.negrita, 6, ROJO, PdfContentByte.ALIGN_CENTER);
        float lx = MARGEN + 230;
        leyenda(h, lx, centro - 34, VERDE, String.format(Locale.US, "%.1f%% Retención Exitosa", 100 - abandono),
                ENTERO.format(d.retenidas()) + " alumnos (" + ENTERO.format(d.completadas()) + " completados y el resto cursando o con acceso a vencer)");
        leyenda(h, lx, centro + 10, ROJO, String.format(Locale.US, "%.1f%% Tasa de Abandono / Bajas", abandono),
                ENTERO.format(d.bajas()) + " alumnos dados de baja durante la cursada");
        return top + alto;
    }

    // ----------------------------------------------------------------------
    // Informe de ingresos
    // ----------------------------------------------------------------------

    /**
     * Genera el PDF del informe de ingresos de un curso.
     *
     * @param datos los datos del informe
     * @param emisor nombre de quien lo genera
     * @param empresa razón social que va en el pie
     * @return el contenido del PDF
     */
    public static byte[] ingresos(InformeIngresos datos, String emisor, String empresa) {
        return armar((h, pagina) -> {
            String periodo = FECHA.format(datos.desde()) + " al " + FECHA.format(datos.hasta());
            String pie = "INFORME DE INGRESOS POR CURSO • " + empresa.toUpperCase(Locale.ROOT);
            if (pagina == 1) {
                float top = encabezado(h, "INFORME DE INGRESOS POR CURSO",
                        "Período: " + periodo + " • Emitido: " + FECHA.format(LocalDate.now()) + " por " + emisor);
                top = cursoYPrograma(h, top, datos.curso().getNombre(), datos.programaVigente());
                top = indicadores(h, top, new String[] { dinero(datos.neto()), dinero(datos.bruto()), dinero(datos.descuentos()), ENTERO.format(datos.pagos()) },
                        new String[] { "NETO ACREDITADO", "PRECIO LISTA BRUTO", "DESCUENTOS APLICADOS", "PAGOS ACREDITADOS" }, new Color[] { VERDE, NAVY, NARANJA, NAVY });
                top = tarjetaBarras(h, top, MARGEN, ANCHO, "1. COMPARACIÓN DE FACTURACIÓN ENTRE CURSOS",
                        "Monto total recaudado en el período, contrastando el curso seleccionado frente a la oferta total.", datos.porCurso(),
                        v -> dinero(v) + " ARS", InformePdfUtil::millones, VERDE, PIZARRA, " (Seleccionado)", 0);
                tarjetaEvolucion(h, top + 12, LIMITE, "2. EVOLUCIÓN DIARIA DE FACTURACIÓN", "Comportamiento de los ingresos percibidos a lo largo del período seleccionado, con una línea por programa.",
                        datos.evolucion(), true);
            } else {
                float top = encabezado(h, "INFORME DE INGRESOS (ANÁLISIS DE MÁRGENES)", "Curso: " + datos.curso().getNombre() + " • Período: " + periodo);
                float ancho = (ANCHO - 14) / 2;
                int partesTorta = Math.min(datos.porCategoria().size(), 6);
                float alto = Math.max(200, 43 + 6 + 2 * RADIO_TORTA + 14 + Math.max(1, partesTorta) * 11 + 16);
                float interior = tarjeta(h, MARGEN, top + 14, ancho, alto, "3. INGRESOS POR CATEGORÍA TEMÁTICA", "Participación porcentual en la facturación global.");
                torta(h, MARGEN, interior, ancho, datos.porCategoria());
                float x2 = MARGEN + ancho + 14;
                interior = tarjeta(h, x2, top + 14, ancho, alto, "4. BRUTO VS DESCUENTOS VS NETO", "Relación entre precio de lista y acreditación real.");
                double bruto = datos.bruto();
                String pd = bruto > 0 ? Math.round(datos.descuentos() * 100 / bruto) + "" : "0";
                String pn = bruto > 0 ? Math.round(datos.neto() * 100 / bruto) + "" : "0";
                float paso = Math.min(48, (alto - (interior - top - 14) - 15) / 3);
                float y = interior + 4;
                fila(h, x2 + 15, y, ancho - 30, "Precio Bruto (100%)", millonesTexto(bruto), bruto, bruto, NAVY, millones(bruto), Color.WHITE, false);
                y += paso;
                fila(h, x2 + 15, y, ancho - 30, "Descuentos (-" + pd + "%)", millonesTexto(datos.descuentos()), datos.descuentos(), bruto, DORADO, millones(datos.descuentos()), NAVY, false);
                y += paso;
                fila(h, x2 + 15, y, ancho - 30, "Neto Acreditado (" + pn + "%)", millonesTexto(datos.neto()), datos.neto(), bruto, VERDE, millones(datos.neto()), Color.WHITE, false);
                List<Barra> programas = datos.porPrograma().stream().limit(6).toList();
                tarjetaBarras(h, top + 14 + alto + 12, MARGEN, ANCHO, "5. EFECTIVIDAD POR PROGRAMA ACADÉMICO",
                        "Comparación entre los programas del curso según el ingreso neto generado, para identificar cuál resultó más efectivo facturando.", programas,
                        v -> dinero(v) + " ARS", InformePdfUtil::millones, VERDE, PIZARRA, "", LIMITE);
            }
            pie(h, pie, pagina, 2);
        });
    }

    // ----------------------------------------------------------------------
    // Piezas del diseño
    // ----------------------------------------------------------------------

    /** Dibuja el encabezado: el logo sobre fondo blanco a la izquierda y el título y el subtítulo sobre fondo oscuro a la derecha. Devuelve la posición donde termina. */
    private static float encabezado(Hoja h, String titulo, String subtitulo) {
        float top = 30;
        float alto = 53;
        float izquierda = 196;
        h.rectangulo(MARGEN, top, ANCHO, alto, 4, NAVY, NAVY);
        h.rectanguloIzquierdo(MARGEN, top, izquierda, alto, 4, Color.WHITE);
        h.c.saveState();
        h.c.setLineWidth(0.8f);
        h.c.setColorStroke(NAVY);
        h.c.roundRectangle(MARGEN, h.y(top + alto), ANCHO, alto, 4);
        h.c.stroke();
        h.c.restoreState();
        // Marca: el logo y, a su derecha, el nombre y la leyenda, dentro del bloque blanco
        float x = MARGEN + 16;
        Image logo = PdfUtil.logo();
        if (logo != null) {
            logo.scaleToFit(48, 44);
            logo.setAbsolutePosition(x, h.y(top + alto / 2) - logo.getScaledHeight() / 2);
            try {
                h.c.addImage(logo);
            } catch (DocumentException e) {
                // el logo es decorativo: si falla, el encabezado sigue con el texto
            }
            x += logo.getScaledWidth() + 9;
        }
        h.texto("IDÓNEOS ", x, h.y(top + 25), h.negrita, 12.5f, NAVY, PdfContentByte.ALIGN_LEFT);
        h.texto("ONLINE", x + h.negrita.getWidthPoint("IDÓNEOS ", 12.5f), h.y(top + 25), h.negrita, 12.5f, DORADO, PdfContentByte.ALIGN_LEFT);
        h.texto("FINANZAS & MERCADOS", x, h.y(top + 36), h.negrita, 6.2f, DORADO, PdfContentByte.ALIGN_LEFT);
        float derecha = MARGEN + ANCHO - 16;
        h.texto(titulo, derecha, h.y(top + 22), h.negrita, titulo.length() > 36 ? 10.5f : 13, Color.WHITE, PdfContentByte.ALIGN_RIGHT);
        float y = top + 35;
        for (String linea : envolver(subtitulo, h.normal, 6.8f, ANCHO - izquierda - 28)) {
            h.texto(linea, derecha, h.y(y), h.normal, 6.8f, new Color(0xA8, 0xB3, 0xC4), PdfContentByte.ALIGN_RIGHT);
            y += 9;
        }
        return top + alto;
    }

    /** Dibuja el nombre del curso y, a la derecha, la insignia del programa vigente. Devuelve la posición donde termina. */
    private static float cursoYPrograma(Hoja h, float top, String curso, String programa) {
        float base = top + 26;
        String texto = "Curso: " + ajustar(curso, h.negrita, 9.5f, 330);
        h.texto(texto, MARGEN, h.y(base), h.negrita, 9.5f, NAVY, PdfContentByte.ALIGN_LEFT);
        boolean vigente = programa != null;
        String insignia = vigente ? "CON PROGRAMA VIGENTE (" + ajustar(programa, h.negrita, 6.3f, 110) + ")" : "SIN PROGRAMA VIGENTE";
        float ancho = h.negrita.getWidthPoint(insignia, 6.3f) + 24;
        float x = MARGEN + ANCHO - ancho;
        h.rectangulo(x, base - 12, ancho, 16, 4, vigente ? new Color(0xFE, 0xF3, 0xC7) : new Color(0xF1, 0xF5, 0xF9),
                vigente ? new Color(0xF5, 0xD8, 0x8A) : BORDE);
        h.texto(insignia, x + ancho / 2, h.y(base - 1.5f), h.negrita, 6.3f, vigente ? new Color(0x92, 0x40, 0x0E) : GRIS, PdfContentByte.ALIGN_CENTER);
        return base + 14;
    }

    /** Dibuja las cuatro tarjetas de indicadores. Devuelve la posición donde terminan, más el espacio que las separa de lo que sigue. */
    private static float indicadores(Hoja h, float top, String[] valores, String[] etiquetas, Color[] colores) {
        float separacion = 9;
        float ancho = (ANCHO - 3 * separacion) / 4;
        float alto = 54;
        for (int i = 0; i < 4; i++) {
            float x = MARGEN + i * (ancho + separacion);
            h.rectangulo(x, top, ancho, alto, 5, FONDO_TARJETA, BORDE);
            float tamanio = valores[i].length() > 11 ? 14 : 17;
            h.texto(valores[i], x + ancho / 2, h.y(top + 26), h.negrita, tamanio, colores[i], PdfContentByte.ALIGN_CENTER);
            float y = top + 38;
            for (String linea : envolver(etiquetas[i], h.negrita, 6.2f, ancho - 14)) {
                h.texto(linea, x + ancho / 2, h.y(y), h.negrita, 6.2f, GRIS, PdfContentByte.ALIGN_CENTER);
                y += 7.5f;
            }
        }
        return top + alto + 12;
    }

    /** Dibuja el marco de una sección con su título y subtítulo. Devuelve la posición donde empieza el contenido. */
    private static float tarjeta(Hoja h, float x, float top, float ancho, float alto, String titulo, String subtitulo) {
        h.rectangulo(x, top, ancho, alto, 6, Color.WHITE, BORDE);
        h.texto(titulo, x + 15, h.y(top + 22), h.negrita, 8.2f, NAVY, PdfContentByte.ALIGN_LEFT);
        float y = top + 32;
        for (String linea : envolver(subtitulo, h.normal, 6.6f, ancho - 30)) {
            h.texto(linea, x + 15, h.y(y), h.normal, 6.6f, GRIS, PdfContentByte.ALIGN_LEFT);
            y += 8.5f;
        }
        return y + 3;
    }

    /**
     * Dibuja una sección de barras horizontales. Devuelve la posición donde termina.
     *
     * @param hasta posición hasta donde puede llegar la tarjeta: las filas se separan más para aprovechar el lugar. Con cero, las filas van juntas.
     */
    private static float tarjetaBarras(Hoja h, float top, float x, float ancho, String titulo, String subtitulo, List<Barra> barras,
            java.util.function.DoubleFunction<String> derecha, java.util.function.DoubleFunction<String> interno, Color colorSeleccionada, Color colorResto,
            String marca, float hasta) {
        float inicio = top + 32 + envolver(subtitulo, h.normal, 6.6f, ancho - 30).size() * 8.5f + 6;
        int filas = Math.max(1, barras.size());
        float paso = hasta > 0 ? Math.max(30, Math.min(46, (hasta - inicio - 15) / filas)) : 32;
        double maximo = barras.stream().mapToDouble(Barra::valor).max().orElse(0);
        float alto = inicio - top + (filas - 1) * paso + 22 + 15;
        tarjeta(h, x, top, ancho, alto, titulo, subtitulo);
        float y = inicio;
        if (barras.isEmpty()) {
            h.texto("Sin datos en el período seleccionado.", x + 15, h.y(y + 12), h.normal, 7, GRIS, PdfContentByte.ALIGN_LEFT);
        }
        for (Barra b : barras) {
            Color color = b.seleccionada() ? colorSeleccionada : colorResto;
            fila(h, x + 15, y, ancho - 30, b.etiqueta() + (b.seleccionada() ? marca : ""), derecha.apply(b.valor()), b.valor(), maximo, color,
                    interno.apply(b.valor()), Color.WHITE, b.seleccionada());
            y += paso;
        }
        return top + alto;
    }

    /** Dibuja una fila de barra: etiqueta y valor arriba, y la barra debajo. Devuelve el alto que ocupa. */
    private static float fila(Hoja h, float x, float top, float ancho, String etiqueta, String valor, double cantidad, double maximo, Color color, String interno,
            Color colorInterno, boolean resaltada) {
        BaseFont fuente = resaltada ? h.negrita : h.normal;
        h.texto(ajustar(etiqueta, fuente, 7, ancho - 120), x, h.y(top + 9), fuente, 7, resaltada ? NAVY : GRIS_OSCURO, PdfContentByte.ALIGN_LEFT);
        h.texto(valor, x + ancho, h.y(top + 9), fuente, 7, resaltada ? NAVY : GRIS_OSCURO, PdfContentByte.ALIGN_RIGHT);
        float yBarra = top + 13;
        h.rectangulo(x, yBarra, ancho, 9, 2, PISTA, PISTA);
        float lleno = maximo <= 0 ? 0 : (float) (cantidad / maximo * ancho * 0.93f);
        if (lleno > 0) {
            lleno = Math.max(lleno, 3);
            h.rectangulo(x, yBarra, lleno, 9, 2, color, color);
            if (lleno > h.negrita.getWidthPoint(interno, 5.8f) + 10) {
                h.texto(interno, x + lleno - 5, h.y(yBarra + 6.5f), h.negrita, 5.8f, colorInterno, PdfContentByte.ALIGN_RIGHT);
            }
        }
        return 28;
    }

    /** Dibuja una sección con un gráfico de líneas de evolución. Devuelve la posición donde termina. */
    private static float tarjetaEvolucion(Hoja h, float top, float hasta, String titulo, String subtitulo, Evolucion evolucion, boolean dinero) {
        float alto = Math.min(hasta - top, 330);
        float inicio = tarjeta(h, MARGEN, top, ANCHO, alto, titulo, subtitulo);
        lineas(h, MARGEN + 15, inicio + 4, ANCHO - 30, top + alto - inicio - 4 - 15, evolucion, dinero);
        return top + alto;
    }

    /** Dibuja un gráfico de líneas, una por serie, con ejes, puntos y las etiquetas de los puntos principales. */
    private static void lineas(Hoja h, float x, float top, float ancho, float alto, Evolucion ev, boolean dinero) {
        List<Serie> series = ev.series();
        int n = ev.etiquetas().size();
        if (series.isEmpty() || n == 0) {
            h.texto("Sin datos en el período seleccionado.", x, h.y(top + 20), h.normal, 7, GRIS, PdfContentByte.ALIGN_LEFT);
            return;
        }
        boolean varias = series.size() > 1;
        float cursor = top;
        if (varias) {
            float lx = x;
            for (int s = 0; s < series.size(); s++) {
                String nombre = ajustar(series.get(s).nombre(), h.normal, 6.5f, 140);
                h.rectangulo(lx, cursor, 7, 7, 1.5f, PALETA[s % PALETA.length], PALETA[s % PALETA.length]);
                h.texto(nombre, lx + 11, h.y(cursor + 6.3f), h.normal, 6.5f, GRIS_OSCURO, PdfContentByte.ALIGN_LEFT);
                lx += 11 + h.normal.getWidthPoint(nombre, 6.5f) + 14;
            }
            cursor += 14;
        }
        double maximo = 0;
        for (Serie s : series) {
            for (double v : s.valores()) {
                maximo = Math.max(maximo, v);
            }
        }
        if (maximo <= 0) {
            maximo = 1;
        }
        float izq = x + 14;
        float derecha = x + ancho - 14;
        float arriba = cursor + 12;
        float base = top + alto - 14;
        float areaAlto = base - arriba;
        h.linea(izq - 8, h.y(arriba - 6), izq - 8, h.y(base), EJES, 0.8f);
        h.linea(izq - 8, h.y(base), derecha + 8, h.y(base), EJES, 0.8f);

        // Marcas del eje horizontal: hasta seis, repartidas
        int marcas = Math.min(n, 6);
        for (int m = 0; m < marcas; m++) {
            int i = marcas == 1 ? 0 : Math.round(m * (n - 1f) / (marcas - 1));
            h.texto(ev.etiquetas().get(i), px(izq, derecha, i, n), h.y(base + 9), h.normal, 5.8f, GRIS, PdfContentByte.ALIGN_CENTER);
        }

        for (int s = 0; s < series.size(); s++) {
            Color color = varias ? PALETA[s % PALETA.length] : NAVY;
            double[] v = series.get(s).valores();
            h.c.saveState();
            h.c.setLineWidth(1.6f);
            h.c.setColorStroke(color);
            h.c.setLineJoin(1);
            for (int i = 0; i < n; i++) {
                float px = px(izq, derecha, i, n);
                float py = h.y(base - (float) (v[i] / maximo) * areaAlto);
                if (i == 0) {
                    h.c.moveTo(px, py);
                } else {
                    h.c.lineTo(px, py);
                }
            }
            h.c.stroke();
            h.c.restoreState();
            int pico = 0;
            for (int i = 0; i < n; i++) {
                if (v[i] > v[pico]) {
                    pico = i;
                }
            }
            for (int i = 0; i < n; i++) {
                boolean etiquetado = !varias ? (n <= 8 || i == 0 || i == n - 1 || i == pico) : i == pico;
                if (n > 12 && !etiquetado) {
                    continue;
                }
                float px = px(izq, derecha, i, n);
                float py = h.y(base - (float) (v[i] / maximo) * areaAlto);
                h.c.saveState();
                h.c.setColorFill(varias ? color : DORADO);
                h.c.setColorStroke(NAVY);
                h.c.setLineWidth(0.8f);
                h.c.circle(px, py, 2.4f);
                h.c.fillStroke();
                h.c.restoreState();
                if (etiquetado && v[i] > 0) {
                    String valor = dinero ? millones(v[i]) : ENTERO.format((long) v[i]);
                    h.texto(varias ? valor : ev.etiquetas().get(i) + " (" + valor + ")", px, py + 5, h.normal, 5.8f, GRIS_OSCURO, PdfContentByte.ALIGN_CENTER);
                }
            }
        }
    }

    private static float px(float izq, float der, int i, int n) {
        return n == 1 ? (izq + der) / 2 : izq + i * (der - izq) / (n - 1);
    }

    /** Dibuja un gráfico de torta con su leyenda debajo. */
    private static void torta(Hoja h, float x, float top, float ancho, List<Barra> partes) {
        double total = partes.stream().mapToDouble(Barra::valor).sum();
        if (total <= 0) {
            h.texto("Sin ingresos en el período seleccionado.", x + 15, h.y(top + 12), h.normal, 7, GRIS, PdfContentByte.ALIGN_LEFT);
            return;
        }
        List<Barra> mostradas = new ArrayList<>(partes.stream().limit(5).toList());
        double resto = partes.stream().skip(5).mapToDouble(Barra::valor).sum();
        if (resto > 0) {
            mostradas.add(new Barra("Otras", resto, false));
        }
        double[] valores = mostradas.stream().mapToDouble(Barra::valor).toArray();
        Color[] colores = new Color[valores.length];
        for (int i = 0; i < valores.length; i++) {
            colores[i] = PALETA_TORTA[i % PALETA_TORTA.length];
        }
        float radio = RADIO_TORTA;
        float cx = x + ancho / 2;
        float cy = top + radio + 6;
        sectores(h, cx, h.y(cy), radio, valores, colores);
        float y = cy + radio + 14;
        for (int i = 0; i < valores.length; i++) {
            String texto = ajustar(mostradas.get(i).etiqueta(), h.normal, 7, ancho - 90) + " (" + Math.round(valores[i] * 100 / total) + "%)";
            float lx = cx - (h.normal.getWidthPoint(texto, 7) + 14) / 2;
            h.rectangulo(lx, y - 7, 8, 8, 2, colores[i], colores[i]);
            h.texto(texto, lx + 13, h.y(y), h.normal, 7, GRIS_OSCURO, PdfContentByte.ALIGN_LEFT);
            y += 11;
        }
    }

    /** Dibuja los sectores de un círculo. El centro se da en coordenadas del PDF (y hacia arriba). */
    private static void sectores(Hoja h, float cx, float cy, float radio, double[] valores, Color[] colores) {
        double total = 0;
        for (double v : valores) {
            total += v;
        }
        if (total <= 0) {
            return;
        }
        double inicio = Math.PI / 2;
        for (int i = 0; i < valores.length; i++) {
            double barrido = valores[i] / total * 2 * Math.PI;
            if (barrido <= 0) {
                continue;
            }
            h.c.saveState();
            h.c.setColorFill(colores[i]);
            if (valores[i] >= total) {
                h.c.circle(cx, cy, radio);
            } else {
                h.c.moveTo(cx, cy);
                int pasos = Math.max(2, (int) Math.ceil(barrido / (Math.PI / 90)));
                for (int p = 0; p <= pasos; p++) {
                    double ang = inicio - barrido * p / pasos;
                    h.c.lineTo(cx + (float) (radio * Math.cos(ang)), cy + (float) (radio * Math.sin(ang)));
                }
                h.c.closePath();
            }
            h.c.fill();
            h.c.restoreState();
            inicio -= barrido;
        }
    }

    private static void leyenda(Hoja h, float x, float top, Color color, String titulo, String detalle) {
        h.rectangulo(x, top, 9, 9, 2, color, color);
        h.texto(titulo, x + 16, h.y(top + 7.5f), h.negrita, 8, NAVY, PdfContentByte.ALIGN_LEFT);
        float y = top + 17;
        for (String linea : envolver(detalle, h.normal, 6.6f, 270)) {
            h.texto(linea, x + 16, h.y(y), h.normal, 6.6f, GRIS, PdfContentByte.ALIGN_LEFT);
            y += 8;
        }
    }

    private static void pie(Hoja h, String texto, int pagina, int paginas) {
        float y = ALTO_PAGINA - 794;
        h.linea(MARGEN, y, MARGEN + ANCHO, y, BORDE, 0.6f);
        h.texto(ajustar(texto, h.normal, 6, ANCHO - 90), MARGEN, y - 13, h.normal, 6, new Color(0x94, 0xA3, 0xB8), PdfContentByte.ALIGN_LEFT);
        h.texto("PÁGINA " + pagina + " DE " + paginas, MARGEN + ANCHO, y - 13, h.normal, 6, new Color(0x94, 0xA3, 0xB8), PdfContentByte.ALIGN_RIGHT);
    }

    // ----------------------------------------------------------------------
    // Formatos y texto
    // ----------------------------------------------------------------------

    private static String dinero(double valor) {
        return "$" + ENTERO.format(Math.round(valor));
    }

    /** Monto abreviado en millones, para el interior de las barras y las etiquetas: {@code $100.8M}. */
    private static String millones(double valor) {
        if (Math.abs(valor) >= 1_000_000) {
            return String.format(Locale.US, "$%.1fM", valor / 1_000_000);
        }
        if (Math.abs(valor) >= 1_000) {
            return String.format(Locale.US, "$%.1fK", valor / 1_000);
        }
        return "$" + Math.round(valor);
    }

    private static String millonesTexto(double valor) {
        return millones(valor);
    }

    private static String ajustar(String texto, BaseFont fuente, float tamanio, float maximo) {
        if (texto == null) {
            return "";
        }
        if (fuente.getWidthPoint(texto, tamanio) <= maximo) {
            return texto;
        }
        String corto = texto;
        while (corto.length() > 1 && fuente.getWidthPoint(corto + "…", tamanio) > maximo) {
            corto = corto.substring(0, corto.length() - 1);
        }
        return corto.stripTrailing() + "…";
    }

    private static List<String> envolver(String texto, BaseFont fuente, float tamanio, float maximo) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fuente.getWidthPoint(prueba, tamanio) > maximo && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    // ----------------------------------------------------------------------
    // Documento y primitivas
    // ----------------------------------------------------------------------

    private interface Pagina {
        void dibujar(Hoja hoja, int numero);
    }

    private static byte[] armar(Pagina pagina) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 0, 0, 0, 0);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            documento.open();
            for (int numero = 1; numero <= 2; numero++) {
                if (numero == 2) {
                    documento.newPage();
                }
                pagina.dibujar(new Hoja(escritor.getDirectContent()), numero);
            }
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el informe.", e);
        } finally {
            documento.close();
        }
        return salida.toByteArray();
    }

    /** Lienzo de una página: las coordenadas verticales se dan desde el borde superior. */
    private static final class Hoja {

        final PdfContentByte c;
        final BaseFont normal = PdfUtil.helvetica(false);
        final BaseFont negrita = PdfUtil.helvetica(true);

        Hoja(PdfContentByte c) {
            this.c = c;
        }

        /** Pasa una posición medida desde arriba a la del PDF (desde abajo). */
        float y(float top) {
            return ALTO_PAGINA - top;
        }

        void texto(String texto, float x, float y, BaseFont fuente, float tamanio, Color color, int alineacion) {
            c.beginText();
            c.setFontAndSize(fuente, tamanio);
            c.setColorFill(color);
            c.showTextAligned(alineacion, texto, x, y, 0);
            c.endText();
        }

        void rectangulo(float x, float top, float ancho, float alto, float radio, Color relleno, Color borde) {
            c.saveState();
            c.setColorFill(relleno);
            c.setColorStroke(borde);
            c.setLineWidth(0.7f);
            c.roundRectangle(x, y(top + alto), ancho, alto, radio);
            c.fillStroke();
            c.restoreState();
        }

        /** Rectángulo blanco sobre la parte izquierda de otro redondeado (para el logo del encabezado). */
        void rectanguloIzquierdo(float x, float top, float ancho, float alto, float radio, Color relleno) {
            c.saveState();
            c.setColorFill(relleno);
            c.roundRectangle(x, y(top + alto), ancho, alto, radio);
            c.fill();
            c.rectangle(x + ancho - radio - 1, y(top + alto), radio + 1, alto);
            c.fill();
            c.restoreState();
            // El lado derecho del bloque blanco queda recto, pegado al fondo oscuro
            c.saveState();
            c.setColorFill(relleno);
            c.rectangle(x + ancho - 8, y(top + alto), 8, alto);
            c.fill();
            c.restoreState();
        }

        void circulo(float cx, float cy, float radio, Color color) {
            c.saveState();
            c.setColorFill(color);
            c.circle(cx, cy, radio);
            c.fill();
            c.restoreState();
        }

        void linea(float x1, float y1, float x2, float y2, Color color, float grosor) {
            c.saveState();
            c.setColorStroke(color);
            c.setLineWidth(grosor);
            c.moveTo(x1, y1);
            c.lineTo(x2, y2);
            c.stroke();
            c.restoreState();
        }
    }
}
