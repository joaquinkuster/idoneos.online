package com.app.idoneos.service.Reportes;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * CU-88 — Generar informe de ingresos de un curso.
 * Genera el PDF ejecutivo con 4 gráficos usando OpenPDF + JFreeChart.
 * Solo considera pagos con EstadoPago.nombre = "Acreditado".
 */
@Component
public class ReporteIngresosPdfGenerator {

    private static final Color COLOR_PRIMARIO = new Color(26, 115, 232);
    private static final Color COLOR_EXITO = new Color(40, 167, 69);
    private static final Color COLOR_PELIGRO = new Color(220, 53, 69);
    private static final Color COLOR_ADVERTENCIA = new Color(255, 193, 7);
    private static final Color COLOR_GRIS_CLARO = new Color(248, 249, 250);
    private static final Color COLOR_TEXTO = new Color(33, 37, 41);
    private static final Color COLOR_SECUNDARIO = new Color(108, 117, 125);

    // Paleta de colores para el gráfico de torta
    private static final Color[] COLORES_TORTA = {
            new Color(26, 115, 232), new Color(40, 167, 69), new Color(255, 193, 7),
            new Color(220, 53, 69), new Color(108, 117, 125), new Color(23, 162, 184)
    };

    /**
     * CU-88: Genera el PDF del informe de ingresos.
     *
     * @param datos       DTO con los datos estadísticos calculados por ReportesService.
     * @param adminNombre Nombre completo del administrador que genera el informe.
     * @return bytes del PDF generado.
     */
    public byte[] generar(DatosInformeIngresosDTO datos, String adminNombre) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        writer.setPageEvent(new FooterEvent(
                "Informe de Ingresos — " + datos.getCurso().getNombre(),
                adminNombre,
                LocalDateTime.now()
        ));

        doc.open();

        NumberFormat moneda = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));

        // === ENCABEZADO ===
        agregarEncabezado(doc, writer, "INFORME DE INGRESOS", datos.getCurso().getNombre(),
                datos.getDesde(), datos.getHasta(), adminNombre);

        // === KPIs RESUMEN ===
        agregarKpis(doc,
                moneda.format(datos.getMontoNeto()), "Ingresos netos acreditados",
                moneda.format(datos.getMontoBruto()), "Ingresos brutos (precio lista)",
                moneda.format(datos.getDescuentoAplicado()), "Descuentos aplicados",
                String.valueOf(datos.getTotalPagosAcreditados()), "Pagos acreditados");

        // === GRÁFICO 1: Comparación de ingresos (barras horizontales) ===
        doc.add(new Paragraph(" "));
        Paragraph t1 = new Paragraph("Comparación de ingresos por curso", subtituloFont());
        t1.setSpacingBefore(10);
        doc.add(t1);
        doc.add(new Paragraph("Ingresos acreditados en el período, comparando todos los cursos.", labelFont()));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoBarrasHorizontales(datos.getNombresComparacion(), datos.getIngresosComparacion(), datos.getCurso().getNombre()),
                500, 280));

        // === GRÁFICO 2: Evolución de ingresos (línea) ===
        doc.add(new Paragraph(" "));
        Paragraph t2 = new Paragraph("Evolución de ingresos en el período", subtituloFont());
        t2.setSpacingBefore(10);
        doc.add(t2);
        doc.add(new Paragraph("Ingresos acreditados diarios del curso seleccionado.", labelFont()));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoLinea(datos.getEtiquetasEvolucion(), datos.getValoresEvolucion()),
                500, 240));

        // === GRÁFICO 3: Ingresos por categoría (torta) ===
        if (!datos.getNombresCategoria().isEmpty()) {
            doc.newPage();
            Paragraph t3 = new Paragraph("Ingresos por categoría de curso", subtituloFont());
            t3.setSpacingBefore(10);
            doc.add(t3);
            doc.add(new Paragraph("Distribución porcentual de ingresos acreditados según la categoría del curso.", labelFont()));
            doc.add(new Paragraph(" "));
            doc.add(crearImagenGrafico(
                    crearGraficoTorta(datos.getNombresCategoria(), datos.getIngresosCategoria()),
                    420, 300));
        }

        // === GRÁFICO 4: Bruto vs. Neto (barras comparativas) ===
        doc.add(new Paragraph(" "));
        Paragraph t4 = new Paragraph("Ingresos brutos vs. netos", subtituloFont());
        t4.setSpacingBefore(10);
        doc.add(t4);
        doc.add(new Paragraph("Comparación entre el precio de lista (bruto) y el monto efectivamente cobrado (neto), con descuentos.", labelFont()));
        doc.add(new Paragraph(" "));
        doc.add(crearImagenGrafico(
                crearGraficoBrutoNeto(datos.getMontoBruto(), datos.getMontoNeto(), datos.getDescuentoAplicado()),
                500, 220));

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

        canvas.setColorFill(COLOR_EXITO);
        canvas.rectangle(40, PageSize.A4.getHeight() - 85, PageSize.A4.getWidth() - 80, 5);
        canvas.fill();

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 2.5f});
        header.setSpacingAfter(8);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPadding(4);
        try {
            ClassPathResource logoResource = new ClassPathResource("logo.png");
            com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(logoResource.getURL());
            logo.scaleToFit(100, 60);
            logoCell.addElement(logo);
        } catch (Exception e) {
            logoCell.addElement(new Phrase("Idóneos Online", new Font(Font.HELVETICA, 18, Font.BOLD)));
        }
        header.addCell(logoCell);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setPaddingLeft(12);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.addElement(new Phrase("Sistema de Gestión Idóneos Online", new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SECUNDARIO)));
        infoCell.addElement(new Phrase(tipoInforme, new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_EXITO)));
        infoCell.addElement(new Phrase(nombreCurso, new Font(Font.HELVETICA, 11, Font.BOLD, COLOR_TEXTO)));
        infoCell.addElement(new Phrase("Período: " + desde.format(fmt) + " — " + hasta.format(fmt), new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_TEXTO)));
        infoCell.addElement(new Phrase("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "  |  Por: " + adminNombre, new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_TEXTO)));
        header.addCell(infoCell);

        doc.add(header);
        LineSeparator line = new LineSeparator(1f, 100f, COLOR_EXITO, Element.ALIGN_CENTER, -2f);
        doc.add(line);
        doc.add(new Paragraph(" "));
    }

    // =========================================================
    // KPIs
    // =========================================================
    private void agregarKpis(Document doc, String v1, String l1, String v2, String l2, String v3, String l3, String v4, String l4) throws Exception {
        PdfPTable kpiTable = new PdfPTable(4);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingBefore(6);
        kpiTable.setSpacingAfter(6);
        Color[] colores = {COLOR_EXITO, COLOR_PRIMARIO, COLOR_PELIGRO, COLOR_ADVERTENCIA};
        String[] valores = {v1, v2, v3, v4};
        String[] labels = {l1, l2, l3, l4};
        for (int i = 0; i < 4; i++) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(10);
            cell.setBackgroundColor(COLOR_GRIS_CLARO);
            Paragraph pv = new Paragraph(valores[i], new Font(Font.HELVETICA, 14, Font.BOLD, colores[i]));
            pv.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(pv);
            Paragraph pl = new Paragraph(labels[i], new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_SECUNDARIO));
            pl.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(pl);
            kpiTable.addCell(cell);
        }
        doc.add(kpiTable);
    }

    // =========================================================
    // GRÁFICOS
    // =========================================================
    private JFreeChart crearGraficoBarrasHorizontales(List<String> nombres, List<Double> valores, String cursoSeleccionado) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < nombres.size(); i++) {
            dataset.addValue(valores.get(i), "Ingresos (ARS)", nombres.get(i));
        }
        JFreeChart chart = ChartFactory.createBarChart(null, "Curso", "ARS", dataset, PlotOrientation.HORIZONTAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, COLOR_EXITO);
        r.setShadowVisible(false);
        r.setMaximumBarWidth(0.06);
        plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        return chart;
    }

    private JFreeChart crearGraficoLinea(List<String> etiquetas, List<Double> valores) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(valores.get(i), "Ingresos/día", etiquetas.get(i));
        }
        JFreeChart chart = ChartFactory.createLineChart(null, "Fecha", "ARS", dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        plot.getRenderer().setSeriesPaint(0, COLOR_EXITO);
        plot.getDomainAxis().setTickLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
        return chart;
    }

    @SuppressWarnings("unchecked")
    private JFreeChart crearGraficoTorta(List<String> nombres, List<Double> valores) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (int i = 0; i < nombres.size(); i++) {
            dataset.setValue(nombres.get(i), valores.get(i));
        }
        JFreeChart chart = ChartFactory.createPieChart(null, dataset, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setShadowXOffset(0);
        plot.setShadowYOffset(0);
        for (int i = 0; i < nombres.size(); i++) {
            plot.setSectionPaint(nombres.get(i), COLORES_TORTA[i % COLORES_TORTA.length]);
        }
        return chart;
    }

    private JFreeChart crearGraficoBrutoNeto(double bruto, double neto, double descuento) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(bruto, "Monto bruto", "Ingresos");
        dataset.addValue(descuento, "Descuentos aplicados", "Ingresos");
        dataset.addValue(neto, "Monto neto", "Ingresos");
        JFreeChart chart = ChartFactory.createBarChart(null, null, "ARS", dataset, PlotOrientation.HORIZONTAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(248, 249, 250));
        plot.setOutlinePaint(null);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, COLOR_PRIMARIO);
        r.setSeriesPaint(1, COLOR_PELIGRO);
        r.setSeriesPaint(2, COLOR_EXITO);
        r.setShadowVisible(false);
        r.setMaximumBarWidth(0.15);
        return chart;
    }

    private com.lowagie.text.Image crearImagenGrafico(JFreeChart chart, int ancho, int alto) throws Exception {
        java.awt.image.BufferedImage buffered = chart.createBufferedImage(ancho * 2, alto * 2);
        ByteArrayOutputStream imgOs = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(buffered, "PNG", imgOs);
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(imgOs.toByteArray());
        img.scaleToFit(ancho, alto);
        img.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);
        return img;
    }

    private Font subtituloFont() { return new Font(Font.HELVETICA, 13, Font.BOLD, COLOR_PRIMARIO); }
    private Font labelFont() { return new Font(Font.HELVETICA, 9, Font.NORMAL, COLOR_SECUNDARIO); }

    // =========================================================
    // PIE DE PÁGINA
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
            canvas.setColorStroke(new Color(220, 220, 220));
            canvas.setLineWidth(0.5f);
            canvas.moveTo(40, 40);
            canvas.lineTo(PageSize.A4.getWidth() - 40, 40);
            canvas.stroke();

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("Sistema de Gestión Idóneos Online  |  " + tipoInforme +
                            "  |  " + fechaGeneracion.format(fmt) + "  |  " + adminNombre, fontFooter),
                    40, 28, 0);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), fontFooter),
                    PageSize.A4.getWidth() - 40, 28, 0);
        }
    }
}
