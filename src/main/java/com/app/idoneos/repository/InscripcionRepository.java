package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
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

    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.dictado.programa.curso = :curso AND i.baja = false")
    Optional<Inscripcion> findByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.dictado.programa.curso = :curso AND i.baja = false")
    boolean existsByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT i FROM Inscripcion i WHERE i.dictado.programa.curso = :curso AND i.baja = false")
    List<Inscripcion> findByCursoAndBajaFalse(@Param("curso") Curso curso);

    /** Verificar si un curso tiene inscripciones vigentes — CU-04. */
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.dictado.programa.curso = :curso AND i.baja = false")
    boolean existsByCursoAndBajaFalse(@Param("curso") Curso curso);

    /** Inscriptos de un dictado especifico — CU-18. */
    List<Inscripcion> findByDictado(com.app.idoneos.model.Dictado dictado);

    /**
     * CU-87 — Generar informe de alumnos de un curso.
     * Inscripciones de un curso en un rango de fechas (desde inclusivo, hasta exclusivo a medianoche).
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.dictado.programa.curso = :curso AND i.fecha >= :desde AND i.fecha < :hasta")
    List<Inscripcion> findByCursoAndFechaRange(
            @Param("curso") Curso curso,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-87 — Generar informe de alumnos: todas las inscripciones de todos los cursos en rango
     * para armar la comparación de barras horizontales.
     */
    @Query("SELECT i.dictado.programa.curso.id, COUNT(i) FROM Inscripcion i WHERE i.fecha >= :desde AND i.fecha < :hasta GROUP BY i.dictado.programa.curso.id")
    List<Object[]> contarInscripcionesPorCursoEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-89 — Consultar estadísticas: inscripciones vigentes al momento actual.
     * Vigente = baja=false Y fechaVencimientoAcceso >= ahora.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.fechaVencimientoAcceso >= :ahora")
    long contarInscripcionesVigentes(@Param("ahora") LocalDateTime ahora);

    /**
     * CU-89 — Consultar estadísticas: inscripciones en los últimos 30 días agrupadas por día.
     * Devuelve Object[]{LocalDate truncado, count}.
     */
    @Query("SELECT FUNCTION('DATE', i.fecha), COUNT(i) FROM Inscripcion i WHERE i.fecha >= :desde GROUP BY FUNCTION('DATE', i.fecha) ORDER BY FUNCTION('DATE', i.fecha)")
    List<Object[]> contarInscripcionesPorDiaDesde(@Param("desde") LocalDateTime desde);

    /**
     * CU-89 — Consultar estadísticas: top 5 cursos con más inscripciones (vigentes, sin baja).
     */
    @Query("SELECT i.dictado.programa.curso.nombre, COUNT(i) FROM Inscripcion i WHERE i.baja = false GROUP BY i.dictado.programa.curso.nombre ORDER BY COUNT(i) DESC")
    List<Object[]> top5CursosPorInscriptos();
}
