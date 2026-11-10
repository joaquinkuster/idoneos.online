package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de IntentoAutoevaluacion. */
@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<IntentoAutoevaluacion> findByBajaFalse();

    /** Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion. */
    List<IntentoAutoevaluacion> findByInscripcionAndBajaFalse(Inscripcion inscripcion);

    /** Busca los intentos vigentes de una inscripción sobre una autoevaluación (incluido el que está en curso). */
    List<IntentoAutoevaluacion> findByInscripcionAndAutoevaluacionAndBajaFalseOrderByIdIntentoAutoevaluacion(
            Inscripcion inscripcion, Autoevaluacion autoevaluacion);

    /** Busca los intentos en curso (sin entregar) de todos los alumnos. */
    List<IntentoAutoevaluacion> findByBajaFalseAndFechaEntregaIsNull();

    /** Busca los intentos en curso de un alumno, en cualquiera de sus inscripciones. */
    @Query("select i from IntentoAutoevaluacion i where i.baja = false and i.fechaEntrega is null "
            + "and i.inscripcion.alumno = :alumno")
    List<IntentoAutoevaluacion> buscarPorAlumno(@Param("alumno") Alumno alumno);

    /** Busca los intentos vigentes de las autoevaluaciones de un curso, del más antiguo al más reciente. */
    @Query("select i from IntentoAutoevaluacion i where i.baja = false "
            + "and i.autoevaluacion.tema.curso.idCurso = :idCurso order by i.idIntentoAutoevaluacion")
    List<IntentoAutoevaluacion> buscarPorCurso(@Param("idCurso") Integer idCurso);
}
