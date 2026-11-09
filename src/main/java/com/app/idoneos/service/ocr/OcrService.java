package com.app.idoneos.service.ocr;

import com.app.idoneos.model.Recurso;

/**
 * Reconocimiento de texto (OCR) de los PDF escaneados. Un PDF sin texto no se puede buscar ni se le puede inferir el índice: se le aplica OCR
 * en el servidor con OCRmyPDF (Tesseract), en segundo plano y de a uno por vez, y el archivo con texto reemplaza al original (se ve igual).
 *
 * El OCR es opcional: si OCRmyPDF no está instalado en el servidor (o está apagado con {@code idoneos.ocr.habilitado=false}), no hace nada y el
 * archivo queda como se subió.
 */
public interface OcrService {

    /**
     * Indica si el servidor puede hacer OCR (está habilitado y OCRmyPDF responde).
     *
     * @return {@code true} si se puede aplicar OCR.
     */
    boolean estaDisponible();

    /**
     * Si el recurso es un PDF escaneado (sin texto), lo deja en la cola del OCR («Pendiente») y devuelve enseguida: el reconocimiento corre en
     * segundo plano. Un PDF con texto, otro formato o un servidor sin OCR no hacen nada. Si el PDF supera los límites (páginas o tamaño),
     * queda en estado «Error».
     *
     * @param recurso El recurso cuyo archivo se acaba de guardar.
     */
    void encolarSiHaceFalta(Recurso recurso);
}
