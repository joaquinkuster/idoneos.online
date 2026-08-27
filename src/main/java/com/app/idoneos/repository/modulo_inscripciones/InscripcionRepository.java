package com.app.idoneos.repository.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.baja = false")
    List<Inscripcion> findByUsuarioAndBajaFalse(@Param("usuario") Usuario usuario);

    List<Inscripcion> findByAlumno(com.app.idoneos.model.Alumno alumno);

    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.cohorte.programa.curso = :curso AND i.baja = false")
    Optional<Inscripcion> findByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.cohorte.programa.curso = :curso AND i.baja = false")
    boolean existsByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT i FROM Inscripcion i WHERE i.cohorte.programa.curso = :curso AND i.baja = false")
    List<Inscripcion> findByCursoAndBajaFalse(@Param("curso") Curso curso);

    /** Verificar si un curso tiene inscripciones vigentes — CU-04. */
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.cohorte.programa.curso = :curso AND i.baja = false")
    boolean existsByCursoAndBajaFalse(@Param("curso") Curso curso);

    /** Inscriptos de una cohorte específica — CU-18. */
    List<Inscripcion> findByCohorte(com.app.idoneos.model.Cohorte cohorte);

    /**
     * CU-87 — Generar informe de alumnos de un curso.
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.cohorte.programa.curso = :curso AND i.fecha >= :desde AND i.fecha < :hasta")
    List<Inscripcion> findByCursoAndFechaRange(
            @Param("curso") Curso curso,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-87 — Comparación de inscripciones por curso en rango de fechas.
     */
    @Query("SELECT i.cohorte.programa.curso.idCurso, COUNT(i) FROM Inscripcion i WHERE i.fecha >= :desde AND i.fecha < :hasta GROUP BY i.cohorte.programa.curso.idCurso")
    List<Object[]> contarInscripcionesPorCursoEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-89 — Inscripciones vigentes al momento actual.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.fechaVencimientoAcceso >= :ahora")
    long contarInscripcionesVigentes(@Param("ahora") LocalDateTime ahora);

    /**
     * CU-89 — Inscripciones en los últimos N días agrupadas por día.
     */
    @Query("SELECT FUNCTION('DATE', i.fecha), COUNT(i) FROM Inscripcion i WHERE i.fecha >= :desde GROUP BY FUNCTION('DATE', i.fecha) ORDER BY FUNCTION('DATE', i.fecha)")
    List<Object[]> contarInscripcionesPorDiaDesde(@Param("desde") LocalDateTime desde);

    /**
     * CU-89 — Top 5 cursos con más inscripciones vigentes.
     */
    @Query("SELECT i.cohorte.programa.curso.nombre, COUNT(i) FROM Inscripcion i WHERE i.baja = false GROUP BY i.cohorte.programa.curso.nombre ORDER BY COUNT(i) DESC")
    List<Object[]> top5CursosPorInscriptos();
}

