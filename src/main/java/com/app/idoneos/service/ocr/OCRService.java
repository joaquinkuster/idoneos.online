package com.app.idoneos.service.ocr;

import com.app.idoneos.model.Recurso;

/** Servicio de reconocimiento de texto (OCR) de los PDF escaneados. */
public interface OCRService {

    /**
     * Indica si el servidor puede hacer OCR (está habilitado y OCRmyPDF responde).
     *
     * @return {@code true} si se puede aplicar OCR.
     */
    boolean estaDisponible();

    /**
     * Si el recurso es un PDF escaneado (sin texto).
     *
     * @param recurso El recurso cuyo archivo se acaba de guardar.
     */
    void encolarSiHaceFalta(Recurso recurso);
}
