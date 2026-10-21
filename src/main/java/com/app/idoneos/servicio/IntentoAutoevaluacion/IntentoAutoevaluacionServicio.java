package com.app.idoneos.servicio.IntentoAutoevaluacion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;

/**
 * Servicio para gestionar las operaciones relacionadas con el intento de autoevaluación.
 */
public interface IntentoAutoevaluacionServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Inscripcion indicado
     */
    List<IntentoAutoevaluacion> buscarPorInscripcion(Inscripcion inscripcion);

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Autoevaluacion indicado
     */
    List<IntentoAutoevaluacion> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion);

    /**
     * Busca los intentos vigentes de una inscripción sobre una autoevaluación, del más antiguo al más reciente (el que está
     * en curso, si lo hay, incluido).
     *
     * @param inscripcion    la inscripción del alumno
     * @param autoevaluacion la autoevaluación
     * @return los intentos del alumno sobre la autoevaluación
     */
    List<IntentoAutoevaluacion> buscarPorInscripcionYAutoevaluacion(Inscripcion inscripcion, Autoevaluacion autoevaluacion);

    /**
     * Busca un intento del alumno: debe estar vigente y pertenecerle. Si se le agotó el tiempo (más la tolerancia), lo
     * cierra y lo corrige antes de devolverlo.
     *
     * @param idIntento identificador del intento
     * @param alumno    el alumno autenticado
     * @return el intento
     * @throws IllegalArgumentException si el intento no existe o no es del alumno
     */
    IntentoAutoevaluacion buscarPropio(Integer idIntento, Alumno alumno);

    /**
     * Busca el intento que mantiene al alumno dentro del cuestionario: está en curso, la autoevaluación no permite salir y
     * todavía no se agotó el tiempo. Mientras exista, el alumno solo puede acceder al intento.
     *
     * @param alumno el alumno autenticado
     * @return el intento en curso que no permite salir, o vacío si no hay ninguno
     */
    Optional<IntentoAutoevaluacion> buscarEnCursoSinSalida(Alumno alumno);

    /**
     * Indica por qué el alumno no puede iniciar un intento de la autoevaluación, o vacío si puede (o si ya tiene uno en
     * curso para continuar): acceso a la unidad, período habilitado, límite de intentos y preguntas suficientes.
     *
     * @param alumno         el alumno
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-70 (pasos 2 a 4): Inicia un intento de autoevaluación: valida el acceso, el período, el límite de intentos y las
     * preguntas disponibles, sortea las preguntas de los pools y registra el intento con su orden. Si el alumno ya tiene un
     * intento en curso de esa autoevaluación, lo devuelve sin sortear de nuevo ni consumir otro intento.
     *
     * @param alumno           el alumno autenticado
     * @param idAutoevaluacion identificador de la autoevaluación
     * @return el intento en curso
     * @throws IllegalArgumentException si el alumno no puede rendir la autoevaluación
     */
    IntentoAutoevaluacion iniciarIntento(Alumno alumno, Integer idAutoevaluacion);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-70 (paso 6): Guarda las opciones que el alumno eligió para una pregunta, reemplazando las anteriores; sin opciones,
     * la deja sin responder. En un cuestionario secuencial solo admite la pregunta actual.
     *
     * @param idIntento         identificador del intento
     * @param alumno            el alumno autenticado
     * @param idPreguntaIntento identificador de la pregunta del intento
     * @param idsOpciones       las opciones elegidas (una en las de opción única)
     * @return el intento actualizado
     * @throws IllegalArgumentException si el intento no está en curso, se agotó el tiempo o la elección no es válida
     */
    IntentoAutoevaluacion responderPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento, List<Integer> idsOpciones);

    /**
     * Marca o desmarca una pregunta para revisarla antes de entregar (solo con navegación libre).
     *
     * @param idIntento         identificador del intento
     * @param alumno            el alumno autenticado
     * @param idPreguntaIntento identificador de la pregunta del intento
     * @param marcada           si queda marcada
     * @return el intento actualizado
     */
    IntentoAutoevaluacion marcarPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento, boolean marcada);

    /**
     * En un cuestionario secuencial, pasa a la pregunta siguiente: la actual queda como pasada (respondida o no) y no se
     * puede volver a ella.
     *
     * @param idIntento identificador del intento
     * @param alumno    el alumno autenticado
     * @return el intento actualizado
     */
    IntentoAutoevaluacion avanzarPregunta(Integer idIntento, Alumno alumno);

    /**
     * CU-70 (pasos 8 a 12): Entrega el intento: lo corrige, registra la nota y la fecha de entrega y, si lo aprobó, verifica
     * si completó la unidad (y el curso). Si el intento ya se había cerrado (por tiempo), lo devuelve tal cual.
     *
     * @param idIntento identificador del intento
     * @param alumno    el alumno autenticado
     * @return el intento corregido
     */
    IntentoAutoevaluacion entregarIntento(Integer idIntento, Alumno alumno);

    /**
     * Cierra y corrige un intento al que se le agotó el tiempo (más la tolerancia).
     *
     * @param intento el intento
     * @return {@code true} si se cerró con esta llamada
     */
    boolean cerrarSiVencido(IntentoAutoevaluacion intento);

    /**
     * Tarea periódica: cierra y corrige todos los intentos en curso a los que se les agotó el tiempo, aunque el alumno no
     * vuelva a entrar.
     *
     * @return la cantidad de intentos cerrados
     */
    int cerrarIntentosVencidos();
}
