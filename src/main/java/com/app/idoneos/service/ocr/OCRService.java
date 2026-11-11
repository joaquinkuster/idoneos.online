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
     * @return {@code true} si el archivo quedó en la cola del reconocimiento de texto, o {@code false} si no hace falta o no se puede.
     */
    boolean encolarSiHaceFalta(Recurso recurso);

    /**
     * Progreso estimado del reconocimiento de texto de un recurso que está en la cola o en proceso.
     *
     * @param idRecurso El identificador del recurso.
     * @return El progreso, o {@code null} si el recurso no está en la cola.
     */
    Progreso progreso(int idRecurso);

    /**
     * Empieza el reconocimiento de texto de un PDF apenas se elige en el formulario, antes de guardar el recurso.
     *
     * @param archivo El PDF elegido.
     * @return El identificador para seguir el avance, o {@code null} si el PDF ya tiene texto o no se puede reconocer.
     * @throws java.io.IOException si no se puede guardar el archivo temporal.
     */
    String iniciarPrevio(org.springframework.web.multipart.MultipartFile archivo) throws java.io.IOException;

    /**
     * Estado de un reconocimiento previo.
     *
     * @param token El identificador que devolvió {@link #iniciarPrevio}.
     * @return El estado y, mientras corre, el progreso, o {@code null} si el identificador no existe.
     */
    Previo consultarPrevio(String token);

    /**
     * Archivo ya procesado de un reconocimiento previo.
     *
     * @param token El identificador del reconocimiento.
     * @return La ruta del PDF con texto, o {@code null} si todavía no terminó.
     */
    java.nio.file.Path archivoPrevio(String token);

    /**
     * Cambia el archivo subido por el que ya se procesó, si el reconocimiento previo terminó.
     *
     * @param token El identificador del reconocimiento previo (puede ser vacío).
     * @param original El archivo que llegó con el formulario.
     * @return El archivo procesado o, si no corresponde, el original.
     * @throws IllegalArgumentException si el reconocimiento todavía está en curso: el recurso no se guarda hasta que termine.
     */
    org.springframework.web.multipart.MultipartFile reemplazarPorProcesado(String token, org.springframework.web.multipart.MultipartFile original);

    /**
     * Borra el archivo temporal de un reconocimiento previo, una vez guardado el recurso.
     *
     * @param token El identificador del reconocimiento previo (puede ser vacío).
     */
    void descartarPrevio(String token);

    /**
     * Estado de un reconocimiento previo.
     *
     * @param estado Pendiente, Procesando, Listo o Error.
     * @param progreso El avance estimado, o {@code null} si ya terminó.
     */
    record Previo(String estado, Progreso progreso) {
    }

    /**
     * Progreso estimado de un reconocimiento de texto.
     *
     * @param porcentaje El avance, de 0 a 95 mientras corre (llega a 100 al terminar).
     * @param segundosRestantes Los segundos que faltan para reconocer las páginas, según el ritmo de las ya terminadas, o -1 si no hay estimación.
     * @param fase "ocr" mientras se reconocen las páginas, "optimizando" mientras se optimizan sus imágenes y "guardando" cuando se escribe el PDF final.
     */
    record Progreso(int porcentaje, int segundosRestantes, String fase) {
    }
}
