package com.app.idoneos.service.modeloia;

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
    TemaRecurso solicitarResumen(Docente docente, Integer idTema);

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
    TemaRecurso solicitarPresentacion(Docente docente, Integer idTema);

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
    Pool solicitarBanco(Docente docente, Integer idTema, String guion);
}
