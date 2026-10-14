package com.app.idoneos.servicio.Participante;

import java.util.List;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Participante;

/**
 * Servicio de consulta de los participantes de un curso (CU-25 y la sección de participantes del CU-27).
 */
public interface ParticipanteServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * CU-25: Busca los participantes de un curso: su equipo docente y los alumnos inscriptos en todas sus cohortes.
     *
     * @param curso          el curso
     * @param nombreCompleto parte del nombre o del apellido (opcional)
     * @param rol            "Docente" (titular o ayudante) o "Alumno" (opcional)
     * @return los participantes que cumplen los criterios: primero el equipo docente y luego los alumnos
     */
    List<Participante> buscarParticipantes(Curso curso, String nombreCompleto, String rol);

    /**
     * CU-27: Busca los participantes que ve un alumno: el equipo docente completo del curso y los alumnos de su
     * propia cohorte.
     *
     * @param cohorte        la cohorte del alumno
     * @param nombreCompleto parte del nombre o del apellido (opcional)
     * @param rol            "Docente" (titular o ayudante) o "Alumno" (opcional)
     * @return los participantes que cumplen los criterios: primero el equipo docente y luego los alumnos
     */
    List<Participante> buscarParticipantesPorCohorte(Cohorte cohorte, String nombreCompleto, String rol);
}
