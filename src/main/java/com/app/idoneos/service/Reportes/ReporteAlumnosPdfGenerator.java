package com.app.idoneos.service.Reportes;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CU-87 — Generar informe de alumnos de un curso.
 * Genera el PDF ejecutivo con 3 gráficos usando OpenPDF + JFreeChart.
 * Logo oficial: /src/main/resources/logo.png
 */
@Component
public class ReporteAlumnosPdfGenerator {

    private static final Color COLOR_PRIMARIO = new Color(26, 115, 232);
    private static final Color COLOR_EXITO = new Color(40, 167, 69);
    private static final Color COLOR_PELIGRO = new Color(220, 53, 69);
    private static final Color COLOR_ADVERTENCIA = new Color(255, 193, 7);
    private static final Color COLOR_GRIS_CLARO = new Color(248, 249, 250);
    private static final Color COLOR_TEXTO = new Color(33, 37, 41);
    private static final Color COLOR_SECUNDARIO = new Color(108, 117, 125);
    private static final Color COLOR_DESTACADO = new Color(255, 150, 0);

    private static final Font FONT_TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(33, 37, 41));
    private static final Font FONT_SUBTITULO = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 10, Font.NORMAL, COLOR_TEXTO);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 10, Font.BOLD, COLOR_TEXTO);
    private static final Font FONT_LABEL = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SECUNDARIO);
    private static final Font FONT_KPI_VALOR = new Font(Font.HELVETICA, 22, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FONT_KPI_LABEL = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SECUNDARIO);
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_SECUNDARIO);

    /**
     * CU-87: Genera el PDF del informe de alumnos.
     *
     * @param datos    DTO con los datos estadísticos calculados por ReportesService.
     * @param adminNombre Nombre completo del administrador que genera el informe.
     * @return bytes del PDF generado.
     */
    public byte[] generar(DatosInformeAlumnosDTO datos, String adminNombre) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        // Pie de página consistente en todas las páginas
        writer.setPageEvent(new FooterEvent(
                "Informe de Alumnos — " + datos.getCurso().getNombre(),
                adminNombre,
                LocalDateTime.now()
        ));

        doc.open();

        // === ENCABEZADO ===
        agregarEncabezado(doc, writer, "INFORME DE ALUMNOS", datos.getCurso().getNombre(),
                datos.getDesde(), datos.getHasta(), adminNombre);

        // === KPIs RESUMEN ===
        agregarKpis(doc,
                String.valueOf(datos.getTotalInscripcionesCurso()), "Inscripciones en el período",
                String.valueOf(datos.getCompletadas()), "Completadas",
                String.valueOf(datos.getVigentes()), "Vigentes",
                String.valueOf(datos.getBajas()), "Dadas de baja");

        // === GRÁFICO 1: Comparación de inscriptos (barras horizontales) ===
        doc.add(new Paragraph(" "));
        Paragraph t1 = new Paragraph("Comparación de inscriptos por curso", FONT_SUBTITULO);
        t1.setSpacingBefore(10);
        doc.add(t1);
        doc.add(new Paragraph("Cantidad de inscripciones en el período seleccionado, comparando todos los cursos.", FONT_LABEL));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoBarrasHorizontales(datos.getNombresComparacion(), datos.getCantidadesComparacion(), datos.getCurso().getNombre()),
                500, 280));

        // === GRÁFICO 2: Evolución de inscripciones (línea) ===
        doc.add(new Paragraph(" "));
        Paragraph t2 = new Paragraph("Evolución de inscripciones en el período", FONT_SUBTITULO);
        t2.setSpacingBefore(10);
        doc.add(t2);
        doc.add(new Paragraph("Inscripciones diarias del curso seleccionado en el rango consultado.", FONT_LABEL));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoLinea(datos.getEtiquetasEvolucion(), datos.getValoresEvolucion(), "Inscripciones/día"),
                500, 240));

        // === GRÁFICO 3: Estado de inscripciones (barras apiladas) ===
        doc.add(new Paragraph(" "));
        Paragraph t3 = new Paragraph("Estado de las inscripciones", FONT_SUBTITULO);
        t3.setSpacingBefore(10);
        doc.add(t3);
        doc.add(new Paragraph("Distribución de inscripciones por estado: completadas, vigentes y dadas de baja.", FONT_LABEL));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoEstadoInscripciones(datos.getCompletadas(), datos.getVigentes(), datos.getBajas()),
                500, 200));

        doc.close();
        return baos.toByteArray();
    }

    // =========================================================
    // ENCABEZADO PROFESIONAL
    // =========================================================
    private void agregarEncabezado(Document doc, PdfWriter writer, String tipoInforme, String nombreCurso,
                                    LocalDate desde, LocalDate hasta, String adminNombre) throws Exception {
        PdfContentByte canvas = writer.getDirectContent();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Línea de color superior
        canvas.setColorFill(COLOR_PRIMARIO);
        canvas.rectangle(40, PageSize.A4.getHeight() - 85, PageSize.A4.getWidth() - 80, 5);
        canvas.fill();

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 2.5f});
        header.setSpacingAfter(8);

        // Logo oficial
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(4);
        try {
            ClassPathResource logoResource = new ClassPathResource("logo.png");
            com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoResource.getURL());
            logo.scaleToFit(100, 60);
            logoCell.addElement(logo);
        } catch (Exception e) {
            logoCell.addElement(new Phrase("Idóneos Online", FONT_TITULO));
        }
        header.addCell(logoCell);

        // Datos del informe
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingLeft(12);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Font fontSistema = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SECUNDARIO);
        Font fontTipoInforme = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_PRIMARIO);
        Font fontCurso = new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_TEXTO);
        Font fontMeta = new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_TEXTO);

        infoCell.addElement(new Phrase("Sistema de Gestión Idóneos Online", fontSistema));
        infoCell.addElement(new Phrase(tipoInforme, fontTipoInforme));
        infoCell.addElement(new Phrase(nombreCurso, fontCurso));
        infoCell.addElement(new Phrase("Período: " + desde.format(fmt) + " — " + hasta.format(fmt), fontMeta));
        infoCell.addElement(new Phrase("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "  |  Por: " + adminNombre, fontMeta));
        header.addCell(infoCell);

        doc.add(header);

        // Línea separadora
        LineSeparator line = new LineSeparator(1f, 100f, COLOR_PRIMARIO, Element.ALIGN_CENTER, -2f);
        doc.add(line);
        doc.add(new Paragraph(" "));
    }

    // =========================================================
    // KPIs / INDICADORES RESUMEN
    // =========================================================
    private void agregarKpis(Document doc,
            String v1, String l1, String v2, String l2, String v3, String l3, String v4, String l4) throws Exception {
        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(6);
        kpiTable.setSpacingAfter(6);

        Color[] colores = {COLOR_PRIMARIO, COLOR_EXITO, COLOR_ADVERTENCIA, COLOR_PELIGRO};
        String[] valores = {v1, v2, v3, v4};
        String[] labels = {l1, l2, l3, l4};

        for (int i = 0; i < 4; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setBorderColor(new Color(220, 220, 220));
            cell.setPadding(12);
            cell.setBackgroundColor(COLOR_GRIS_CLARO);

            Paragraph pValor = new Paragraph(valores[i], new Font(Font.HELVETICA, 24, Font.BOLD, colores[i]));
            pValor.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(pValor);

            Paragraph pLabel = new Paragraph(labels[i], FONT_KPI_LABEL);
            pLabel.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(pLabel);

            kpiTable.addCell(cell);
        }
        doc.add(kpiTable);
    }

    // =========================================================
    // GRÁFICOS
    // =========================================================

    /** Barras horizontales para comparar cursos. El curso seleccionado aparece destacado. */
    private JFreeChart crearGraficoBarrasHorizontales(List<String> nombres, List<Long> cantidades, String cursoSeleccionado) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < nombres.size(); i++) {
            dataset.addValue(cantidades.get(i), "Inscriptos", nombres.get(i));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, "Curso", "Inscripciones", dataset,
                PlotOrientation.HORIZONTAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_PRIMARIO);
        // Destacar el curso seleccionado
        for (int i = 0; i < nombres.size(); i++) {
            if (nombres.get(i).equals(cursoSeleccionado)) {
                renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator());
            }
        }
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setMaximumBarWidth(0.06);
        renderer.setShadowVisible(false);
        plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        return chart;
    }

    /** Línea para evolución temporal de inscripciones. */
    private JFreeChart crearGraficoLinea(List<String> etiquetas, List<Long> valores, String serieLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), serieLabel, etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                null, "Fecha", "Cantidad", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getRenderer().setSeriesPaint(0, COLOR_PRIMARIO);
        plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        // Mostrar solo cada N etiqueta para no saturar el eje
        int n = Math.max(1, etiquetas.size() / 10);
        ((CategoryAxis) plot.getDomainAxis()).setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        return chart;
    }

    /** Barras apiladas para estado de inscripciones: completadas, vigentes, bajas. */
    private JFreeChart crearGraficoEstadoInscripciones(long completadas, long vigentes, long bajas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(completadas, "Completadas", "Estado");
        dataset.addValue(vigentes, "Vigentes", "Estado");
        dataset.addValue(bajas, "Dadas de baja", "Estado");

        JFreeChart chart = ChartFactory.createStackedBarChart(
                null, null, "Inscripciones", dataset,
                PlotOrientation.HORIZONTAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_EXITO);
        renderer.setSeriesPaint(1, COLOR_PRIMARIO);
        renderer.setSeriesPaint(2, COLOR_PELIGRO);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setShadowVisible(false);
        return chart;
    }

    /** Convierte un JFreeChart en un elemento Image de OpenPDF. */
    private com.lowagie.text.Image crearImagenGrafico(JFreeChart chart, int ancho, int alto) throws Exception {
        java.awt.image.BufferedImage buffered = chart.createBufferedImage(ancho * 2, alto * 2);
        ByteArrayOutputStream imgOs = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(buffered, "PNG", imgOs);
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(imgOs.toByteArray());
        img.scaleToFit(ancho, alto);
        img.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);
        return img;
    }

    // =========================================================
    // PIE DE PÁGINA (Event)
    // =========================================================
    static class FooterEvent extends PdfPageEventHelper {
        private final String tipoInforme;
        private final String adminNombre;
        private final LocalDateTime fechaGeneracion;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        private final Font fontFooter = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(108, 117, 125));

        FooterEvent(String tipoInforme, String adminNombre, LocalDateTime fechaGeneracion) {
            this.tipoInforme = tipoInforme;
            this.adminNombre = adminNombre;
            this.fechaGeneracion = fechaGeneracion;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            int numPagina = writer.getPageNumber();

            // Línea separadora del pie
            canvas.setColorStroke(new Color(220, 220, 220));
            canvas.setLineWidth(0.5f);
            canvas.moveTo(40, 40);
            canvas.lineTo(PageSize.A4.getWidth() - 40, 40);
            canvas.stroke();

            // Texto izquierdo: tipo de informe + fecha
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("Sistema de Gestión Idóneos Online  |  " + tipoInforme +
                            "  |  " + fechaGeneracion.format(fmt) + "  |  " + adminNombre, fontFooter),
                    40, 28, 0);

            // Texto derecho: número de página
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + numPagina, fontFooter),
                    PageSize.A4.getWidth() - 40, 28, 0);
        }
    }
}
