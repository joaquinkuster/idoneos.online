package com.app.idoneos.servicio.Curso;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Programa;

/**
 * Datos de un conjunto de cursos que necesita el listado para validar la baja y la modificación de cada uno.
 *
 * @param inscripcionesPorCurso       Las inscripciones activas de cada curso, por identificador del curso.
 * @param programasPorCurso           Los programas activos de cada curso, por identificador del curso.
 * @param cursosConInscripcionAbierta Los identificadores de los cursos que tienen alguna cohorte con inscripción abierta.
 */
public record ResumenCursos(Map<Integer, List<Inscripcion>> inscripcionesPorCurso,
        Map<Integer, List<Programa>> programasPorCurso, Set<Integer> cursosConInscripcionAbierta) {
}
