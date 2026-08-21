import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.FileOutputStream;

public class GenerarGuionPdf {

    private static final Color COLOR_NAVY_DARK = new Color(11, 31, 58);
    private static final Color COLOR_NAVY_LIGHT = new Color(18, 41, 77);
    private static final Color COLOR_GOLD = new Color(212, 175, 55);
    private static final Color COLOR_TEXT_MAIN = new Color(33, 37, 41);
    private static final Color COLOR_TEXT_MUTED = new Color(108, 117, 125);
    private static final Color COLOR_BG_BOX = new Color(248, 249, 250);
    private static final Color COLOR_BORDER_BOX = new Color(222, 226, 230);
    private static final Color COLOR_ACCENT_BLUE = new Color(13, 110, 253);

    public static void main(String[] args) {
        String dest = "Guion_Presentacion_MOD_NF_03.pdf";
        Document doc = new Document(PageSize.A4, 40, 40, 45, 45);

        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(dest));
            writer.setPageEvent(new HeaderFooterPageEvent());
            doc.open();

            // Tipografías
            Font fTitle = new Font(Font.HELVETICA, 16, Font.BOLD, COLOR_GOLD);
            Font fSubTitle = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font fH1 = new Font(Font.HELVETICA, 12, Font.BOLD, COLOR_NAVY_DARK);
            Font fH2 = new Font(Font.HELVETICA, 10.5f, Font.BOLD, COLOR_NAVY_LIGHT);
            Font fBody = new Font(Font.HELVETICA, 9.2f, Font.NORMAL, COLOR_TEXT_MAIN);
            Font fBold = new Font(Font.HELVETICA, 9.2f, Font.BOLD, COLOR_TEXT_MAIN);
            Font fQuote = new Font(Font.HELVETICA, 9.2f, Font.ITALIC, new Color(40, 50, 60));

            // Encabezado Principal Banner
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell cellBanner = new PdfPCell();
            cellBanner.setBackgroundColor(COLOR_NAVY_DARK);
            cellBanner.setPadding(14);
            cellBanner.setBorder(Rectangle.NO_BORDER);

            Paragraph pMarca = new Paragraph("IDÓNEOS ONLINE  |  DEFENSA DE PROYECTO FINAL", new Font(Font.HELVETICA, 8.5f, Font.BOLD, new Color(180, 200, 225)));
            Paragraph pTit = new Paragraph("Guión de Presentación: Módulo de Reportes (MOD-NF-03)", fTitle);
            Paragraph pSub = new Paragraph("Trazabilidad 1 a 1 de Casos de Uso (CU-87, CU-88, CU-89), Fuentes de Datos y Estructura Visual", fSubTitle);
            pSub.setSpacingBefore(3);

            cellBanner.addElement(pMarca);
            cellBanner.addElement(pTit);
            cellBanner.addElement(pSub);
            banner.addCell(cellBanner);
            doc.add(banner);

            doc.add(new Paragraph(" "));

            // 1. INTRODUCCIÓN GENERAL
            doc.add(new Paragraph("📌 1. Introducción General (Contexto y Objetivos)", fH1));
            PdfPTable boxIntro = crearCajaDestacada();
            PdfPCell cIntro = new PdfPCell();
            cIntro.setBackgroundColor(COLOR_BG_BOX);
            cIntro.setBorderColor(COLOR_BORDER_BOX);
            cIntro.setPadding(10);
            
            Paragraph pIntroText = new Paragraph();
            pIntroText.add(new Chunk("Objetivo del Módulo: ", fBold));
            pIntroText.add(new Chunk("Cumplir con ", fBody));
            pIntroText.add(new Chunk("OBJ-09: Generar reportes y estadísticas de gestión", fBold));
            pIntroText.add(new Chunk(" y el requisito de información ", fBody));
            pIntroText.add(new Chunk("RI-09", fBold));
            pIntroText.add(new Chunk(".\n\n", fBody));
            
            pIntroText.add(new Chunk("\"Buenas tardes, profesor. En esta sección les voy a presentar el Módulo de Reportes y Estadísticas (MOD-NF-03), cuyo objetivo es OBJ-09: Generar reportes y estadísticas de gestión.\n\n" +
                    "Este módulo fue diseñado para que el Administrador no solo vea cómo está funcionando la plataforma en el día a día, sino que pueda tomar decisiones estratégicas descargando informes ejecutivos en PDF sobre dos pilares clave del negocio: la retención de alumnos y la recaudación económica.\n\n" +
                    "Cada informe que se emite cumple con una regla de negocio fundamental: queda registrado en el historial del sistema (trazabilidad y auditoría en la tabla Reporte) con la fecha exacta, qué tipo de reporte fue y qué administrador lo generó.\"", fQuote));
            cIntro.addElement(pIntroText);
            boxIntro.addCell(cIntro);
            doc.add(boxIntro);

            doc.add(new Paragraph(" "));

            // 2. CU-87: INFORME DE ALUMNOS
            doc.add(new Paragraph("🎓 2. Informe de Alumnos por Curso (CU-87)", fH1));
            
            Paragraph pCu87Objetivo = new Paragraph();
            pCu87Objetivo.add(new Chunk("🔹 ¿De qué va este Caso de Uso y para qué le sirve a la empresa?\n", fH2));
            pCu87Objetivo.add(new Chunk("\"El CU-87 le permite al Administrador evaluar el rendimiento académico y la demanda de un curso en particular dentro de un período de fechas. Le sirve a la empresa para responder tres preguntas esenciales:\n" +
                    "1. ¿Cómo le va a este curso frente a todos los demás de la plataforma?\n" +
                    "2. ¿Las inscripciones vienen en aumento, estancadas o en caída?\n" +
                    "3. ¿Qué tan bien retenemos a los alumnos? (¿Cuántos terminan con certificado, cuántos siguen cursando y cuántos abandonan?).\"", fQuote));
            pCu87Objetivo.setSpacingBefore(4);
            pCu87Objetivo.setSpacingAfter(6);
            doc.add(pCu87Objetivo);

            Paragraph pCu87Datos = new Paragraph();
            pCu87Datos.add(new Chunk("🔹 ¿De dónde salen los datos? (Trazabilidad en Base de Datos)\n", fH2));
            pCu87Datos.add(new Chunk("Para armar este informe, el backend consulta directamente la tabla ", fBody));
            pCu87Datos.add(new Chunk("Inscripcion", fBold));
            pCu87Datos.add(new Chunk(" cruzada con ", fBody));
            pCu87Datos.add(new Chunk("Curso, Usuario (Alumno) y Certificado", fBold));
            pCu87Datos.add(new Chunk(":\n", fBody));
            pCu87Datos.add(new Chunk("• Total de Inscripciones: ", fBold));
            pCu87Datos.add(new Chunk("Suma todas las filas de la tabla Inscripcion del curso creadas entre Desde y Hasta.\n", fBody));
            pCu87Datos.add(new Chunk("• Completadas: ", fBold));
            pCu87Datos.add(new Chunk("Cuenta inscripciones que tienen registro emitido en la tabla Certificado.\n", fBody));
            pCu87Datos.add(new Chunk("• Vigentes: ", fBold));
            pCu87Datos.add(new Chunk("Cuenta inscripciones activas (sin fecha de baja y sin certificado aún).\n", fBody));
            pCu87Datos.add(new Chunk("• Dadas de Baja: ", fBold));
            pCu87Datos.add(new Chunk("Cuenta inscripciones que registran fecha de baja/cancelación.\n", fBody));
            pCu87Datos.setSpacingAfter(6);
            doc.add(pCu87Datos);

            Paragraph pCu87Visual = new Paragraph();
            pCu87Visual.add(new Chunk("🔹 Estructura visual del Informe (2 Páginas en PDF / Wireframe)\n", fH2));
            pCu87Visual.add(new Chunk("📄 Página 1:\n", fBold));
            pCu87Visual.add(new Chunk("  • Encabezado: Logo de Idóneos Online, título 'INFORME DE ALUMNOS POR CURSO', período y firma de auditoría.\n" +
                    "  • 4 KPIs: Total Inscriptos, Completadas, Vigentes y Bajas.\n" +
                    "  • Vista 1 (Barras Horizontales): Comparación de cantidad de inscriptos del curso frente a todos los demás cursos.\n" +
                    "  • Vista 2 (Línea Temporal): Evolución diaria de las inscripciones con el valor numérico en cada nodo.\n", fBody));
            pCu87Visual.add(new Chunk("📄 Página 2:\n", fBold));
            pCu87Visual.add(new Chunk("  • Vista 3 (Barras Verticales): Completadas, Vigentes y Bajas. La leyenda superior incluye etiqueta, cantidad en unidades y porcentaje (ej: Vigentes (999 unid. - 50.0%)).\n" +
                    "  • Pie de página: Sistema, fecha/hora exacta de emisión, usuario administrador y número de página.\n", fBody));
            doc.add(pCu87Visual);

            doc.newPage();

            // 3. CU-88: INFORME DE INGRESOS
            doc.add(new Paragraph("💰 3. Informe de Ingresos por Curso (CU-88)", fH1));

            Paragraph pCu88Objetivo = new Paragraph();
            pCu88Objetivo.add(new Chunk("🔹 ¿De qué va este Caso de Uso y para qué le sirve a la empresa?\n", fH2));
            pCu88Objetivo.add(new Chunk("\"El CU-88 está enfocado en la salud financiera y comercial de un curso. Le sirve a los directivos para saber:\n" +
                    "1. ¿Cuánto dinero real está ingresando por este curso frente al resto?\n" +
                    "2. ¿La facturación viene creciendo o decreciendo en el tiempo?\n" +
                    "3. ¿Qué categorías temáticas son las más rentables?\n" +
                    "4. ¿Cuánto nos está costando en la práctica otorgar cupones de descuento? (Política comercial).\"", fQuote));
            pCu88Objetivo.setSpacingBefore(4);
            pCu88Objetivo.setSpacingAfter(6);
            doc.add(pCu88Objetivo);

            Paragraph pCu88Datos = new Paragraph();
            pCu88Datos.add(new Chunk("🔹 ¿De dónde salen los datos? (Trazabilidad en Base de Datos)\n", fH2));
            pCu88Datos.add(new Chunk("Para este reporte, el sistema consulta la tabla ", fBody));
            pCu88Datos.add(new Chunk("Pago", fBold));
            pCu88Datos.add(new Chunk(" cruzada con ", fBody));
            pCu88Datos.add(new Chunk("EstadoPago, Inscripcion, Curso, Categoria y CuponDescuento", fBold));
            pCu88Datos.add(new Chunk(":\n", fBody));
            pCu88Datos.add(new Chunk("• Condición obligatoria: ", fBold));
            pCu88Datos.add(new Chunk("Solo se procesan transacciones con EstadoPago = 'Acreditado'.\n", fBody));
            pCu88Datos.add(new Chunk("• Precio de Lista Bruto: ", fBold));
            pCu88Datos.add(new Chunk("Precio nominal del curso multiplicado por la cantidad de pagos acreditados.\n", fBody));
            pCu88Datos.add(new Chunk("• Descuentos Aplicados: ", fBold));
            pCu88Datos.add(new Chunk("Suma de deducciones monetarias otorgadas mediante CuponDescuento.\n", fBody));
            pCu88Datos.add(new Chunk("• Monto Neto Acreditado: ", fBold));
            pCu88Datos.add(new Chunk("Recaudación real efectiva que ingresó a la cuenta de la empresa (Bruto - Descuentos).\n", fBody));
            pCu88Datos.add(new Chunk("• Ingresos por Categoría: ", fBold));
            pCu88Datos.add(new Chunk("Montos netos agrupados por la entidad Categoria del curso.\n", fBody));
            pCu88Datos.setSpacingAfter(6);
            doc.add(pCu88Datos);

            Paragraph pCu88Visual = new Paragraph();
            pCu88Visual.add(new Chunk("🔹 Estructura visual del Informe (2 Páginas en PDF / Wireframe)\n", fH2));
            pCu88Visual.add(new Chunk("📄 Página 1:\n", fBold));
            pCu88Visual.add(new Chunk("  • Encabezado: Título 'INFORME DE INGRESOS POR CURSO', fechas y firma de auditoría.\n" +
                    "  • 4 KPIs: Monto Neto Acreditado ($), Precio de Lista Bruto ($), Descuentos Otorgados ($) y Pagos Acreditados.\n" +
                    "  • Vista 1 (Barras Horizontales): Comparación de recaudación en $ ARS del curso frente a la oferta total.\n" +
                    "  • Vista 2 (Línea Temporal): Evolución diaria de facturación con importes en cada nodo.\n", fBody));
            pCu88Visual.add(new Chunk("📄 Página 2:\n", fBold));
            pCu88Visual.add(new Chunk("  • Vista 3 (Torta / Dona): Porcentaje y monto de ingresos aportado por cada categoría temática.\n" +
                    "  • Vista 4 (Barras Verticales Comparativas): Contraste directo entre Monto Bruto, Descuentos y Neto Acreditado.\n" +
                    "  • Pie de página: Auditoría con sistema, fecha/hora, usuario y paginación.\n", fBody));
            doc.add(pCu88Visual);

            doc.add(new Paragraph(" "));

            // 4. CIERRE Y CU-89
            doc.add(new Paragraph("⚡ 4. Cierre y Conexión con el Panel en Vivo (CU-89)", fH1));
            PdfPTable boxCierre = crearCajaDestacada();
            PdfPCell cCierre = new PdfPCell();
            cCierre.setBackgroundColor(COLOR_BG_BOX);
            cCierre.setBorderColor(COLOR_BORDER_BOX);
            cCierre.setPadding(10);
            
            Paragraph pCierre = new Paragraph();
            pCierre.add(new Chunk("\"Para cerrar, profesor, ambos reportes descargables se complementan con el CU-89: Consultar estadísticas, que es el panel ejecutivo en vivo que el Administrador ve en pantalla sin necesidad de descargar un PDF (con KPIs del mes, variación porcentual vs. mes anterior, gráfico interactivo de los últimos 30 días y el Top 5 de cursos más vendidos).\n\n" +
                    "En resumen, los 3 Casos de Uso del MOD-NF-03 están 100% integrados, conectados a la base de datos real, auditados y con una interfaz visual limpia tanto en pantalla como en documentos PDF ejecutivos.\"", fQuote));
            cCierre.addElement(pCierre);
            boxCierre.addCell(cCierre);
            doc.add(boxCierre);

            doc.close();
            System.out.println("PDF generado con éxito: " + dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PdfPTable crearCajaDestacada() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);
        table.setSpacingAfter(4);
        return table;
    }

    static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private final Font fontFooter = new Font(Font.HELVETICA, 8, Font.NORMAL, COLOR_TEXT_MUTED);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            int pageNum = writer.getPageNumber();

            canvas.setColorStroke(COLOR_BORDER_BOX);
            canvas.setLineWidth(0.5f);
            canvas.moveTo(40, 32);
            canvas.lineTo(PageSize.A4.getWidth() - 40, 32);
            canvas.stroke();

            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("Idóneos Online  •  Módulo de Reportes (MOD-NF-03)  •  Guión de Defensa", fontFooter),
                    40, 20, 0);

            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("Página " + pageNum, fontFooter),
                    PageSize.A4.getWidth() - 40, 20, 0);
        }
    }
}
