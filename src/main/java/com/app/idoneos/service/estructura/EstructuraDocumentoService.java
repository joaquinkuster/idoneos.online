package com.app.idoneos.service.estructura;

import com.app.idoneos.model.Documento;

/** Servicio de la estructura de un documento: sus oraciones, repartidas entre las entradas de su índice. */
public interface EstructuraDocumentoService {

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Lee el PDF del documento, saca sus oraciones y las guarda, cada una en la entrada del índice que cubre su página: la última entrada que
     * empieza en esa página o antes, y la primera para las que van antes del primer título. Reemplaza las oraciones que hubiera. Si el archivo no es
     * un PDF, no existe o no tiene texto, el documento queda sin oraciones.
     *
     * @param documento El documento, con su índice ya guardado.
     * @return La cantidad de oraciones guardadas.
     */
    int reconstruir(Documento documento);

    /** Sección de un documento: el título de una entrada del índice y los textos (oraciones o párrafos) que le pertenecen, en orden. */
    record Seccion(String titulo, int nivel, java.util.List<String> textos) {

        /** Una sección de primer nivel. */
        public Seccion(String titulo, java.util.List<String> textos) {
            this(titulo, 1, textos);
        }
    }

    /**
     * Guarda las oraciones de un documento generado, tal como se escribieron, cada sección en la entrada del índice con su mismo título. Las
     * secciones que no coinciden con ninguna entrada quedan en la entrada anterior. Reemplaza las oraciones que hubiera.
     *
     * @param documento El documento, con su índice ya guardado.
     */
    void guardarSecciones(Documento documento, java.util.List<Seccion> secciones);

    /** Lee las secciones de un documento: cada entrada del índice que tiene oraciones, con ellas. */
    java.util.List<Seccion> secciones(Documento documento);
}
