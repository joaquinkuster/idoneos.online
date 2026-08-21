package com.app.idoneos.service.Reportes;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
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
 * Genera un PDF ejecutivo de calidad editorial (Retina 300 DPI, tipografía nítida, 2 páginas balanceadas).
 */
@Component
public class ReporteAlumnosPdfGenerator {

    private static final Color COLOR_NAVY_DARK = new Color(11, 31, 58);
    private static final Color COLOR_NAVY_LIGHT = new Color(18, 41, 77);
    private static final Color COLOR_GOLD = new Color(212, 175, 55);
    private static final Color COLOR_SUCCESS = new Color(34, 139, 34);
    private static final Color COLOR_DANGER = new Color(180, 40, 40);
    private static final Color COLOR_MUTED = new Color(100, 115, 135);
    private static final Color COLOR_BG_LIGHT = new Color(248, 250, 252);
    private static final Color COLOR_CARD_BORDER = new Color(226, 232, 240);

    private static final Font FONT_BRAND = new Font(Font.HELVETICA, 15, Font.BOLD, Color.WHITE);
    private static final Font FONT_HEADER_TITLE = new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_GOLD);
    private static final Font FONT_HEADER_SUB = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, new Color(210, 220, 235));
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_NAVY_DARK);
    private static final Font FONT_DESC = new Font(Font.HELVETICA, 8.5f, Font.NORMAL, COLOR_MUTED);
    private static final Font FONT_CARD_LABEL = new Font(Font.HELVETICA, 7.5f, Font.BOLD, COLOR_MUTED);

    public byte[] generar(DatosInformeAlumnosDTO datos, String adminNombre) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 45);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new ModernPdfFooter(
                "Sistema de informe de alumnos por curso",
                adminNombre,
                LocalDateTime.now()
        ));

        doc.open();

        // ==========================================
        // PÁGINA 1: Encabezado, KPIs y Gráficos 1 y 2
        // ==========================================
        agregarBannerSuperior(doc, "INFORME DE ALUMNOS POR CURSO",
                datos.getDesde(), datos.getHasta(), adminNombre);

        agregarTarjetasKpi(doc,
                String.valueOf(datos.getTotalInscripcionesCurso()), "TOTAL INSCRIPCIONES",
                String.valueOf(datos.getCompletadas()), "COMPLETADAS (CERTIF.)",
                String.valueOf(datos.getVigentes()), "ACTIVAS / VIGENTES",
                String.valueOf(datos.getBajas()), "DADAS DE BAJA");

        // Vista 1: Gráfico de barras horizontales
        agregarTituloSeccion(doc, "1. Comparación de inscripciones entre cursos",
                "Volumen total de inscriptos en el período, contrastando el curso seleccionado con el catálogo.");
        doc.add(crearImagenGraficoAltaResolucion(
                crearGraficoBarrasHorizontales(datos.getNombresComparacion(), datos.getCantidadesComparacion(), datos.getCurso().getNombre()),
                522, 190));

        // Vista 2: Evolución temporal
        agregarTituloSeccion(doc, "2. Evolución diaria de demanda",
                "Comportamiento diario de las inscripciones registradas a lo largo del período seleccionado.");
        doc.add(crearImagenGraficoAltaResolucion(
                crearGraficoLinea(datos.getEtiquetasEvolucion(), datos.getValoresEvolucion(), "Inscripciones"),
                522, 180));

        // ==========================================
        // PÁGINA 2: Vista 3
        // ==========================================
        doc.newPage();

        agregarTituloSeccion(doc, "3. Distribución según estado de cursada",
                "Composición porcentual y cuantitativa de inscripciones completadas con certificación, activas y canceladas.");
        doc.add(crearImagenGraficoAltaResolucion(
                crearGraficoEstadoInscripciones(datos.getCompletadas(), datos.getVigentes(), datos.getBajas()),
                522, 220));

        doc.close();
        return baos.toByteArray();
    }

    private void agregarBannerSuperior(Document doc, String tipoReporte,
                                        LocalDate desde, LocalDate hasta, String adminNombre) throws Exception {
        PdfPTable banner = new PdfPTable(2);
        banner.setWidthPercentage(100);
        banner.setWidths(new float[]{1.3f, 2.7f});
        banner.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell colLogo = new PdfPCell();
        colLogo.setBackgroundColor(COLOR_NAVY_DARK);
        colLogo.setPadding(12);
        colLogo.setBorder(Rectangle.NO_BORDER);
        colLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);

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
                logoImg.scaleToFit(120, 50);
                colLogo.addElement(logoImg);
            }
        } catch (Exception e) {
            Paragraph pMarca = new Paragraph("Idóneos Online", FONT_BRAND);
            colLogo.addElement(pMarca);
        }

        banner.addCell(colLogo);

        PdfPCell colInfo = new PdfPCell();
        colInfo.setBackgroundColor(COLOR_NAVY_LIGHT);
        colInfo.setPadding(12);
        colInfo.setBorder(Rectangle.NO_BORDER);
        colInfo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        colInfo.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph pTipo = new Paragraph(tipoReporte, FONT_HEADER_TITLE);
        pTipo.setAlignment(Element.ALIGN_RIGHT);
        colInfo.addElement(pTipo);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Paragraph pPeriodo = new Paragraph("Período: " + desde.format(fmt) + " al " + hasta.format(fmt),
                new Font(Font.HELVETICA, 9.5f, Font.BOLD, Color.WHITE));
        pPeriodo.setAlignment(Element.ALIGN_RIGHT);
        pPeriodo.setSpacingBefore(4);
        colInfo.addElement(pPeriodo);

        Paragraph pMeta = new Paragraph("Emitido: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                + " por " + adminNombre, FONT_HEADER_SUB);
        pMeta.setAlignment(Element.ALIGN_RIGHT);
        pMeta.setSpacingBefore(2);
        colInfo.addElement(pMeta);

        banner.addCell(colInfo);
        doc.add(banner);
        doc.add(new Paragraph(" "));
    }

    private void agregarTarjetasKpi(Document doc, String v1, String l1, String v2, String l2,
                                     String v3, String l3, String v4, String l4) throws Exception {
        PdfPTable kpis = new PdfPTable(4);
        kpis.setWidthPercentage(100);
        kpis.setSpacingBefore(0);
        kpis.setSpacingAfter(6);

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

            Paragraph pVal = new Paragraph(valores[i], new Font(Font.HELVETICA, 15, Font.BOLD, colValores[i]));
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

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    private JFreeChart crearGraficoLinea(List<String> etiquetas, List<Long> valores, String serieLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int step = Math.max(1, etiquetas.size() / 6);
        for (int i = 0; i < etiquetas.size(); i++) {
            String etiquetaVisible = (i % step == 0 || i == etiquetas.size() - 1) ? etiquetas.get(i) : "";
            dataset.addValue(valores.get(i), serieLabel, etiquetaVisible);
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

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return chart;
    }

    private JFreeChart crearGraficoEstadoInscripciones(long completadas, long vigentes, long bajas) {
        long total = Math.max(1, completadas + vigentes + bajas);
        String labelCompletadas = String.format("Completadas (%d unid. - %.1f%%)", completadas, (completadas * 100.0) / total);
        String labelVigentes = String.format("Vigentes (%d unid. - %.1f%%)", vigentes, (vigentes * 100.0) / total);
        String labelBajas = String.format("Dadas de Baja (%d unid. - %.1f%%)", bajas, (bajas * 100.0) / total);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(completadas, labelCompletadas, "Completadas");
        dataset.addValue(vigentes, labelVigentes, "Vigentes");
        dataset.addValue(bajas, labelBajas, "Dadas de Baja");

        JFreeChart chart = ChartFactory.createBarChart(
                null, null, "Alumnos", dataset,
                PlotOrientation.VERTICAL, true, false, false);
        estilizarChart(chart);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, COLOR_SUCCESS);
        renderer.setSeriesPaint(1, COLOR_GOLD);
        renderer.setSeriesPaint(2, COLOR_DANGER);
        renderer.setMaximumBarWidth(0.14);
        renderer.setShadowVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));
        }
        return chart;
    }

    private void estilizarChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setAntiAlias(true);
        chart.setTextAntiAlias(true);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(COLOR_BG_LIGHT);
        plot.setOutlinePaint(COLOR_CARD_BORDER);
        plot.setRangeGridlinePaint(new Color(220, 226, 235));
        plot.setDomainGridlinesVisible(false);

        java.awt.Font fontEjes = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9);
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setTickLabelFont(fontEjes);
            plot.getDomainAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
        }
        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setTickLabelFont(fontEjes);
            plot.getRangeAxis().setLabelFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
        }
    }

    private com.lowagie.text.Image crearImagenGraficoAltaResolucion(JFreeChart chart, int anchoPt, int altoPt) throws Exception {
        int factorEscala = 2;
        BufferedImage bi = new BufferedImage(anchoPt * factorEscala, altoPt * factorEscala, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.scale(factorEscala, factorEscala);
        chart.draw(g2, new java.awt.geom.Rectangle2D.Double(0, 0, anchoPt, altoPt));
        g2.dispose();

        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bi, null);
        img.scaleToFit(anchoPt, altoPt);
        img.setAlignment(Element.ALIGN_CENTER);
        return img;
    }

    static class ModernPdfFooter extends PdfPageEventHelper {
        private final String tipoInforme;
        private final String adminNombre;
        private final LocalDateTime fechaGeneracion;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        private final Font fontFooter = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, COLOR_MUTED);

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

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("Sistema de Gestión Idóneos Online  |  " + tipoInforme +
                            "  |  " + fechaGeneracion.format(fmt) + "  |  " + adminNombre, fontFooter),
                    40, 25, 0);

            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + numPagina, fontFooter),
                    PageSize.A4.getWidth() - 40, 25, 0);
        }
    }
}

