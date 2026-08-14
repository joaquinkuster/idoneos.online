package com.app.idoneos.service.Reportes;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CU-87 — Generar informe de alumnos de un curso.
 * Genera el PDF ejecutivo de alta calidad estética con 3 gráficos usando OpenPDF + JFreeChart.
 * Paleta corporativa institucional: Deep Navy (#0B1F3A, #12294D) + Elegant Gold (#D4AF37, #E4BE6C).
 */
@Component
public class ReporteAlumnosPdfGenerator {

    // Paleta corporativa Idóneos Online
    private static final Color COLOR_NAVY_DARK = new Color(11, 31, 58);
    private static final Color COLOR_NAVY_LIGHT = new Color(18, 41, 77);
    private static final Color COLOR_GOLD = new Color(212, 175, 55);
    private static final Color COLOR_GOLD_SOFT = new Color(245, 237, 216);
    private static final Color COLOR_SUCCESS = new Color(34, 139, 34);
    private static final Color COLOR_DANGER = new Color(180, 40, 40);
    private static final Color COLOR_MUTED = new Color(100, 115, 135);
    private static final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);

    // Tipografías OpenPDF
    private static final Font FONT_BRAND = new Font(Font.HELVETICA, 16, Font.BOLD, Color.WHITE);
    private static final Font FONT_HEADER_TITLE = new Font(Font.HELVETICA, 14, Font.BOLD, COLOR_GOLD);
    private static final Font FONT_HEADER_SUB = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(210, 220, 235));
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_NAVY_DARK);
    private static final Font FONT_DESC = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_MUTED);
    private static final Font FONT_CARD_LABEL = new Font(Font.HELVETICA, 8, Font.BOLD, COLOR_MUTED);
    private static final Font FONT_CARD_VAL = new Font(Font.HELVETICA, 20, Font.BOLD, COLOR_NAVY_DARK);

    public byte[] generar(DatosInformeAlumnosDTO datos, String adminNombre) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new ModernPdfFooter(
                "Informe de Alumnos — " + datos.getCurso().getNombre(),
                adminNombre,
                LocalDateTime.now()
        ));

        doc.open();

        // 1. Header Banner
        agregarBannerSuperior(doc, "INFORME EJECUTIVO DE ALUMNOS", datos.getCurso().getNombre(),
                datos.getDesde(), datos.getHasta(), adminNombre);

        // 2. Tarjetas KPI
        agregarTarjetasKpi(doc,
                String.valueOf(datos.getTotalInscripcionesCurso()), "TOTAL INSCRIPCIONES",
                String.valueOf(datos.getCompletadas()), "COMPLETADAS (CERTIFICADAS)",
                String.valueOf(datos.getVigentes()), "ACTIVAS / VIGENTES",
                String.valueOf(datos.getBajas()), "DADAS DE BAJA");

        // 3. Vista 1: Gráfico de barras horizontales
        agregarTituloSeccion(doc, "1. Comparación de inscriptos entre cursos",
                "Volumen total de inscripciones registradas en el período seleccionado, comparando este curso contra la oferta académica.");
        doc.add(crearImagenGrafico(
                crearGraficoBarrasHorizontales(datos.getNombresComparacion(), datos.getCantidadesComparacion(), datos.getCurso().getNombre()),
                520, 200));

        // 4. Vista 2: Evolución diaria de inscripciones
        agregarTituloSeccion(doc, "2. Evolución diaria de inscripciones",
                "Comportamiento de la demanda temporal día por día para el curso analizado.");
        doc.add(crearImagenGrafico(
                crearGraficoLinea(datos.getEtiquetasEvolucion(), datos.getValoresEvolucion(), "Inscripciones"),
                520, 175));

        // 5. Vista 3: Estado de las inscripciones
        agregarTituloSeccion(doc, "3. Distribución según estado de cursada",
                "Composición porcentual y cuantitativa de inscripciones completadas con certificación, activas y canceladas.");
        doc.add(crearImagenGrafico(
                crearGraficoEstadoInscripciones(datos.getCompletadas(), datos.getVigentes(), datos.getBajas()),
                520, 130));

        doc.close();
        return baos.toByteArray();
    }

    private void agregarBannerSuperior(Document doc, String tipoReporte, String nombreCurso,
                                        LocalDate desde, LocalDate hasta, String adminNombre) throws Exception {
        PdfPTable banner = new PdfPTable(2);
        banner.setWidthPercentage(100);
        banner.setWidths(new float[]{1.3f, 2.7f});
        banner.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Celda Izquierda: Logo & Marca
        PdfPCell colLogo = new PdfPCell();
        colLogo.setBackgroundColor(COLOR_NAVY_DARK);
        colLogo.setPadding(14);
        colLogo.setBorder(Rectangle.NO_BORDER);

        try {
            com.lowagie.text.Image logoImg = null;
            try {
                ClassPathResource r1 = new ClassPathResource("static/img/logos/image.png");
                logoImg = com.lowagie.text.Image.getInstance(r1.getURL());
            } catch (Exception ignored) {
                ClassPathResource r2 = new ClassPathResource("logo.png");
                logoImg = com.lowagie.text.Image.getInstance(r2.getURL());
            }
            if (logoImg != null) {
                logoImg.scaleToFit(120, 48);
                colLogo.addElement(logoImg);
            }
        } catch (Exception e) {
            Paragraph pMarca = new Paragraph("Idóneos Online", FONT_BRAND);
            colLogo.addElement(pMarca);
        }

        Paragraph pTagline = new Paragraph("EXCELENCIA EN EDUCACIÓN FINANCIERA",
                new Font(Font.HELVETICA, 6.5f, Font.BOLD, COLOR_GOLD));
        pTagline.setSpacingBefore(4);
        colLogo.addElement(pTagline);
        banner.addCell(colLogo);

        // Celda Derecha: Metadatos del informe
        PdfPCell colInfo = new PdfPCell();
        colInfo.setBackgroundColor(COLOR_NAVY_LIGHT);
        colInfo.setPadding(14);
        colInfo.setBorder(Rectangle.NO_BORDER);
        colInfo.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph pTipo = new Paragraph(tipoReporte, FONT_HEADER_TITLE);
        pTipo.setAlignment(Element.ALIGN_RIGHT);
        colInfo.addElement(pTipo);

        Paragraph pCurso = new Paragraph(nombreCurso,
                new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE));
        pCurso.setAlignment(Element.ALIGN_RIGHT);
        pCurso.setSpacingBefore(2);
        colInfo.addElement(pCurso);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph pPeriodo = new Paragraph("Período: " + desde.format(fmt) + " al " + hasta.format(fmt)
                + "  |  Emitido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " por " + adminNombre, FONT_HEADER_SUB);
        pPeriodo.setAlignment(Element.ALIGN_RIGHT);
        pPeriodo.setSpacingBefore(4);
        colInfo.addElement(pPeriodo);

        banner.addCell(colInfo);
        doc.add(banner);
        doc.add(new Paragraph(" "));
    }

    private void agregarTarjetasKpi(Document doc, String v1, String l1, String v2, String l2,
                                     String v3, String l3, String v4, String l4) throws Exception {
        PdfPTable kpis = new PdfPTable(4);
        kpis.setWidthPercentage(100);
        kpis.setSpacingBefore(2);
        kpis.setSpacingAfter(8);

        String[] valores = {v1, v2, v3, v4};
        String[] labels = {l1, l2, l3, l4};
        Color[] colValores = {COLOR_NAVY_DARK, COLOR_SUCCESS, COLOR_GOLD, COLOR_DANGER};

        for (int i = 0; i < 4; i++) {
            PdfPCell card = new PdfPCell();
            card.setBackgroundColor(COLOR_BG_LIGHT);
            card.setBorderColor(COLOR_CARD_BORDER);
            card.setBorderWidth(1f);
            card.setPadding(8);
            card.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph pVal = new Paragraph(valores[i], new Font(Font.HELVETICA, 16, Font.BOLD, colValores[i]));
            pVal.setAlignment(Element.ALIGN_CENTER);
            card.addElement(pVal);

            Paragraph pLbl = new Paragraph(labels[i], FONT_CARD_LABEL);
            pLbl.setAlignment(Element.ALIGN_CENTER);
            pLbl.setSpacingBefore(2);
            card.addElement(pLbl);

            kpis.addCell(card);
        }
        doc.add(kpis);
    }

    private void agregarTituloSeccion(Document doc, String titulo, String descripcion) throws Exception {
        Paragraph pT = new Paragraph(titulo, FONT_SECTION);
        pT.setSpacingBefore(6);
        doc.add(pT);
        Paragraph pD = new Paragraph(descripcion, FONT_DESC);
        pD.setSpacingAfter(4);
        doc.add(pD);
    }

    // ==========================================
    // GRÁFICOS JFREECHART ESTILIZADOS
    // ==========================================
    private JFreeChart crearGraficoBarrasHorizontales(List<String> nombres, List<Long> cantidades, String cursoSeleccionado) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < nombres.size(); i++) {
            dataset.addValue(cantidades.get(i), "Inscriptos", nombres.get(i));
        }
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, "Inscripciones", dataset,
                PlotOrientation.HORIZONTAL, false, false, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_NAVY_DARK);
        renderer.setMaximumBarWidth(0.08);
        renderer.setShadowVisible(false);
        return chart;
    }

    private JFreeChart crearGraficoLinea(List<String> etiquetas, List<Long> valores, String serieLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), serieLabel, etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                null, null, "Inscriptos por día", dataset,
                PlotOrientation.VERTICAL, false, false, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesPaint(0, COLOR_GOLD);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
        plot.setRenderer(renderer);
        return chart;
    }

    private JFreeChart crearGraficoEstadoInscripciones(long completadas, long vigentes, long bajas) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(completadas, "Completadas", "Estado Actual");
        dataset.addValue(vigentes, "Vigentes", "Estado Actual");
        dataset.addValue(bajas, "Dadas de Baja", "Estado Actual");

        JFreeChart chart = ChartFactory.createStackedBarChart(
                null, null, "Alumnos", dataset,
                PlotOrientation.HORIZONTAL, true, false, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_SUCCESS);
        renderer.setSeriesPaint(1, COLOR_GOLD);
        renderer.setSeriesPaint(2, COLOR_DANGER);
        renderer.setMaximumBarWidth(0.18);
        renderer.setShadowVisible(false);
        return chart;
    }

    private void estilizarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(COLOR_BG_LIGHT);
        plot.setOutlinePaint(COLOR_CARD_BORDER);
        plot.setRangeGridlinePaint(new Color(220, 226, 235));
        plot.setDomainGridlinesVisible(false);
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        }
        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        }
    }

    private com.lowagie.text.Image crearImagenGrafico(JFreeChart chart, int ancho, int alto) throws Exception {
        BufferedImage bi = chart.createBufferedImage(ancho, alto);
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bi, null);
        img.setAlignment(Element.ALIGN_CENTER);
        return img;
    }

    // =========================================================
    // PIE DE PÁGINA (Modern)
    // =========================================================
    static class ModernPdfFooter extends PdfPageEventHelper {
        private final String tipoInforme;
        private final String adminNombre;
        private final LocalDateTime fechaGeneracion;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        private final Font fontFooter = new Font(Font.HELVETICA, 7, Font.NORMAL, COLOR_MUTED);

        ModernPdfFooter(String tipoInforme, String adminNombre, LocalDateTime fechaGeneracion) {
            this.tipoInforme = tipoInforme;
            this.adminNombre = adminNombre;
            this.fechaGeneracion = fechaGeneracion;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            int numPagina = writer.getPageNumber();

            canvas.setColorStroke(COLOR_CARD_BORDER);
            canvas.setLineWidth(0.5f);
            canvas.moveTo(36, 35);
            canvas.lineTo(PageSize.A4.getWidth() - 36, 35);
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
