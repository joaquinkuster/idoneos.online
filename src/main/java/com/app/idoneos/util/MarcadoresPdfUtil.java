package com.app.idoneos.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

/** Utilidad que lee los marcadores de un PDF, que son su índice, para sugerirlos como índice del documento. */
public final class MarcadoresPdfUtil {

    /** Cantidad máxima de niveles de un índice. */
    private static final int NIVEL_MAXIMO = 4;

    /**
     * Marcador de un PDF.
     *
     * @param titulo El título.
     * @param pagina La página donde empieza (la primera es la 1).
     * @param nivel El nivel de anidación: 1 para un título principal.
     */
    public record Marcador(String titulo, int pagina, int nivel) {
    }

    private MarcadoresPdfUtil() {
    }

    /**
     * Lee los marcadores de un PDF, en el orden en que aparecen.
     * Se saltean los que no apuntan a una página y se aplana lo que pasa del cuarto nivel.
     *
     * @param pdf La ruta del archivo.
     * @return Los marcadores, o una lista vacía si el PDF no tiene.
     * @throws IOException Si el archivo no se puede abrir como PDF.
     */
    public static List<Marcador> leer(Path pdf) throws IOException {
        try (PDDocument documento = Loader.loadPDF(pdf.toFile())) {
            PDDocumentOutline raiz = documento.getDocumentCatalog().getDocumentOutline();
            List<Marcador> marcadores = new ArrayList<>();
            if (raiz != null) {
                recorrer(documento, raiz, 1, marcadores);
            }
            return marcadores;
        }
    }

    private static void recorrer(PDDocument documento, PDOutlineNode padre, int nivel, List<Marcador> marcadores) throws IOException {
        for (PDOutlineItem hijo : padre.children()) {
            PDPage pagina = hijo.findDestinationPage(documento);
            String titulo = hijo.getTitle() == null ? "" : hijo.getTitle().strip();
            if (pagina != null && !titulo.isEmpty()) {
                marcadores.add(new Marcador(titulo, documento.getPages().indexOf(pagina) + 1, Math.min(nivel, NIVEL_MAXIMO)));
            }
            recorrer(documento, hijo, nivel + 1, marcadores);
        }
    }
}
