package com.app.idoneos.service.modeloia;

import java.util.List;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.TemaRecurso;

/** Servicio que genera contenido académico con inteligencia artificial a partir de la bibliografía de un tema. */
public interface GeneracionIAService {

    /**
     * Pide el resumen de un tema. Registra el material oculto y en espera, y lo genera en segundo plano.
     *
     * @param docente El docente que lo pide, que debe participar en el curso del tema.
     * @param idTema El identificador del tema.
     * @return El material registrado, todavía sin archivo.
     * @throws IllegalArgumentException Si el tema no está activo, el docente no participa en el curso, el tema no tiene
     *         una bibliografía lista para leer, ya hay un resumen en generación, o la cola de generación está llena.
     */
    default TemaRecurso solicitarResumen(Docente docente, Integer idTema) {
        return solicitarResumen(docente, idTema, null);
    }

    /**
     * Pide el resumen de un tema, a partir de los documentos elegidos como fuente.
     *
     * @param fuentes Los identificadores de los documentos del tema que se usan como fuente, o {@code null} para usar los que no generó la IA.
     * @see #solicitarResumen(Docente, Integer)
     */
    TemaRecurso solicitarResumen(Docente docente, Integer idTema, List<Integer> fuentes);

    /**
     * Pide la presentación de un tema, en PDF con una diapositiva por página. Registra el material oculto y en espera,
     * y lo genera en segundo plano.
     *
     * @param docente El docente que lo pide, que debe participar en el curso del tema.
     * @param idTema El identificador del tema.
     * @return El material registrado, todavía sin archivo.
     * @throws IllegalArgumentException Si el tema no está activo, el docente no participa en el curso, el tema no tiene
     *         una bibliografía lista para leer, ya hay una presentación en generación, o la cola de generación está llena.
     */
    default TemaRecurso solicitarPresentacion(Docente docente, Integer idTema) {
        return solicitarPresentacion(docente, idTema, null);
    }

    /**
     * Pide la presentación de un tema, a partir de los documentos elegidos como fuente.
     *
     * @param fuentes Los identificadores de los documentos del tema que se usan como fuente, o {@code null} para usar los que no generó la IA.
     * @see #solicitarPresentacion(Docente, Integer)
     */
    TemaRecurso solicitarPresentacion(Docente docente, Integer idTema, List<Integer> fuentes);

    /**
     * Pide un banco de preguntas de un tema. Registra el pool en espera y lo genera en segundo plano, con preguntas con opciones
     * y de verdadero o falso, en la cantidad y la proporción que indican los parámetros del sistema.
     *
     * @param docente El docente que lo pide, que debe participar en el curso del tema.
     * @param idTema El identificador del tema.
     * @param guion Un texto opcional para orientar las preguntas. Puede ser nulo o vacío.
     * @return El pool registrado, todavía sin preguntas.
     * @throws IllegalArgumentException Si el tema no está activo, el docente no participa en el curso, el tema no tiene bibliografía
     *         lista ni glosario, ya hay un banco en generación, el guión es muy largo, o la cola de generación está llena.
     */
    default Pool solicitarBanco(Docente docente, Integer idTema, String guion) {
        return solicitarBanco(docente, idTema, guion, null);
    }

    /**
     * Pide un banco de preguntas de un tema, a partir de los documentos elegidos como fuente.
     *
     * @param fuentes Los identificadores de los documentos del tema que se usan como fuente, o {@code null} para usar los que no generó la IA.
     * @see #solicitarBanco(Docente, Integer, String)
     */
    Pool solicitarBanco(Docente docente, Integer idTema, String guion, List<Integer> fuentes);

    /**
     * Pide un resumen o una presentación a partir de documentos de la biblioteca digital, sin tema: queda como un recurso de la biblioteca del docente
     * (privado), generado en segundo plano. Las fuentes pueden ser documentos del docente o de otros docentes que los compartieron.
     *
     * @param presentacion {@code true} para una presentación, {@code false} para un resumen.
     * @param fuentes Los identificadores de los documentos elegidos, o {@code null} para usar los que no generó la IA.
     * @return El recurso registrado, todavía sin archivo.
     */
    com.app.idoneos.model.Recurso solicitarEnBiblioteca(Docente docente, boolean presentacion, List<Integer> fuentes);

    /** Lista los documentos de la biblioteca digital que el docente puede elegir como fuente (los suyos y los que compartieron otros docentes). */
    List<FuenteDisponible> fuentesDeBiblioteca(Docente docente);

    /** Documento del tema que se puede elegir como fuente. */
    record FuenteDisponible(int idDocumento, String titulo, String tipo, java.time.LocalDateTime fechaCreacion, boolean generadaConIa,
            boolean porDefecto) {
    }

    /**
     * Indica si el docente puede editar el contenido de un material generado con IA: es un docente del curso, el material ya está generado y todavía
     * está oculto para los alumnos (una vez publicado, ya no se modifica).
     */
    boolean puedeEditarContenido(Docente docente, TemaRecurso material);

    /** El contenido de un material generado con IA, para editarlo: su título y sus secciones (cada una con su nivel y sus textos). */
    record ContenidoEditable(String titulo, List<com.app.idoneos.service.estructura.EstructuraDocumentoService.Seccion> secciones) {
    }

    /** Lee el contenido de un material generado con IA para editarlo. */
    ContenidoEditable contenido(Docente docente, TemaRecurso material);

    /**
     * Guarda el contenido editado de un material generado con IA (sus secciones y textos), sin regenerar el archivo. Se llama a medida que el docente
     * edita; el archivo se actualiza con {@link #regenerarArchivo}.
     *
     * @throws IllegalArgumentException Si el docente no puede editarlo, el contenido no es válido, o el material es fuente o evidencia de otro contenido
     *         generado con IA.
     */
    void guardarContenido(Docente docente, TemaRecurso material, ContenidoEditable contenido);

    /** Regenera el archivo (PDF), el índice y las páginas de un material generado con IA a partir del contenido guardado. */
    void regenerarArchivo(Docente docente, TemaRecurso material);

    /** Lista los documentos del tema que se pueden elegir como fuente: los que ya tienen su texto listo para leer. */
    List<FuenteDisponible> fuentesDisponibles(Integer idTema);
}
