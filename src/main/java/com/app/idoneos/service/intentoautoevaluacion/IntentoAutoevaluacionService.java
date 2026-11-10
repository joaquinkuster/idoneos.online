package com.app.idoneos.service.intentoautoevaluacion;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import java.util.List;
import java.util.Optional;

/** Servicio del intento de autoevaluación. */
public interface IntentoAutoevaluacionService extends CrudService<IntentoAutoevaluacion> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los intentos vigentes de una inscripción sobre una autoevaluación.
     *
     * @param inscripcion la inscripción del alumno
     * @param autoevaluacion la autoevaluación
     * @return los intentos del alumno sobre la autoevaluación
     */
    List<IntentoAutoevaluacion> buscarPorInscripcionYAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion);

    /**
     * Busca un intento del alumno: debe estar vigente y pertenecerle.
     *
     * @param idIntento identificador del intento
     * @param alumno el alumno autenticado
     * @return el intento
     * @throws IllegalArgumentException si el intento no existe o no es del alumno
     */
    IntentoAutoevaluacion buscarPropio(Integer idIntento, Alumno alumno);

    /**
     * Busca el intento que mantiene al alumno dentro del cuestionario: está en curso.
     *
     * @param alumno el alumno autenticado
     * @return el intento en curso que no permite salir, o vacío si no hay ninguno
     */
    Optional<IntentoAutoevaluacion> buscarEnCursoSinSalida(Alumno alumno);

    /**
     * Indica por qué el alumno no puede iniciar un intento de la autoevaluación.
     *
     * @param alumno el alumno
     * @param autoevaluacion la autoevaluación
     * @return el motivo, o vacío si puede iniciar o continuar un intento
     */
    Optional<String> motivoNoDisponible(Alumno alumno, Autoevaluacion autoevaluacion);

    /**
     * Calcula cuántos segundos tolera el sistema las respuestas y entregas que llegan después de agotado el tiempo.
     *
     * @return la tolerancia, en segundos
     */
    int obtenerGraciaEnSegundos();

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Inicia un intento de autoevaluación: valida el acceso, el período.
     *
     * @param alumno el alumno autenticado
     * @param idAutoevaluacion identificador de la autoevaluación
     * @return el intento en curso
     * @throws IllegalArgumentException si el alumno no puede rendir la autoevaluación
     */
    IntentoAutoevaluacion iniciar(Alumno alumno, Integer idAutoevaluacion);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Guarda las opciones que el alumno eligió para una pregunta, reemplazando las anteriores.
     *
     * @param idIntento identificador del intento
     * @param alumno el alumno autenticado
     * @param idPreguntaIntento identificador de la pregunta del intento
     * @param idsOpciones las opciones elegidas (una en las de opción única)
     * @return el intento actualizado
     * @throws IllegalArgumentException si el intento no está en curso, se agotó el tiempo o la elección no es válida
     */
    IntentoAutoevaluacion responderPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento, List<Integer> idsOpciones);

    /**
     * Marca o desmarca una pregunta para revisarla antes de entregar (solo con navegación libre).
     *
     * @param idIntento identificador del intento
     * @param alumno el alumno autenticado
     * @param idPreguntaIntento identificador de la pregunta del intento
     * @param marcada si queda marcada
     * @return el intento actualizado
     */
    IntentoAutoevaluacion marcarPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento, boolean marcada);

    /**
     * En un cuestionario secuencial, pasa a la pregunta siguiente.
     *
     * @param idIntento identificador del intento
     * @param alumno el alumno autenticado
     * @return el intento actualizado
     */
    IntentoAutoevaluacion avanzarPregunta(Integer idIntento, Alumno alumno);

    /**
     * Entrega el intento: lo corrige, registra la nota y la fecha de entrega y, si lo aprobó, verifica si completó el tema y el curso.
     *
     * @param idIntento identificador del intento
     * @param alumno el alumno autenticado
     * @return el intento corregido
     */
    IntentoAutoevaluacion entregar(Integer idIntento, Alumno alumno);

    /**
     * Cierra y corrige un intento al que se le agotó el tiempo (más la tolerancia).
     *
     * @param intento el intento
     * @return {@code true} si se cerró con esta llamada
     */
    boolean cerrarSiVencido(IntentoAutoevaluacion intento);

    /**
     * Tarea periódica: cierra y corrige todos los intentos en curso a los que se les agotó el tiempo.
     *
     * @return la cantidad de intentos cerrados
     */
    int cerrarVencidos();

    /**
     * Busca los intentos vigentes de las autoevaluaciones de un curso, del más antiguo al más reciente.
     *
     * @param idCurso el identificador del curso
     * @param soloDeDocente si se indica, el docente debe participar en el curso (si no, se rechaza)
     * @return los intentos del curso
     * @throws IllegalArgumentException si el docente no participa en el curso
     */
    List<IntentoAutoevaluacion> buscarDelCurso(Integer idCurso, Docente soloDeDocente);

    /**
     * El docente aprueba manualmente un intento entregado que no alcanzó la nota mínima.
     *
     * @param idIntento el identificador del intento
     * @param docente el docente, que debe participar en el curso
     * @return el intento aprobado
     * @throws IllegalArgumentException si el intento no existe, está en curso, ya está aprobado o el docente no participa
     */
    IntentoAutoevaluacion aprobarPorDocente(Integer idIntento, Docente docente);

    /**
     * Da de baja un intento (también uno en curso, por ejemplo ante un fraude).
     *
     * @param idIntento el identificador del intento
     * @return {@code true} si la baja hizo retroceder el tema del alumno
     * @throws IllegalArgumentException si el intento no existe o ya está dado de baja
     */
    boolean darDeBaja(Integer idIntento);
}
