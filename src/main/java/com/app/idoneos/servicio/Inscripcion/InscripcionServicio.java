package com.app.idoneos.servicio.Inscripcion;

import java.util.List;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio para gestionar las operaciones relacionadas con la inscripción.
 */
public interface InscripcionServicio {

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    List<Inscripcion> buscarPorCohorte(Cohorte cohorte);

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    List<Inscripcion> buscarPorAlumno(Alumno alumno);

    /**
     * Busca las inscripciones vigentes de un alumno, filtrando opcionalmente por el nombre del curso
     * y por el estado de la inscripción (Pendiente, En Progreso o Finalizado).
     *
     * @param alumno el alumno dueño de las inscripciones
     * @param nombreCurso parte del nombre del curso (opcional)
     * @param estado el estado de la inscripción (opcional)
     * @return una lista de inscripciones que cumplen los criterios, de la más reciente a la más antigua
     */
    List<Inscripcion> buscarMisCursos(Alumno alumno, String nombreCurso, String estado);

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción: la inscripción debe ser suya y estar
     * vigente y habilitada, su acceso no debe haber vencido y, si la cohorte tiene fecha de inicio de dictado,
     * ésta no debe ser futura (CU-27).
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    Inscripcion validarAcceso(int idInscripcion, Alumno alumno);
}
