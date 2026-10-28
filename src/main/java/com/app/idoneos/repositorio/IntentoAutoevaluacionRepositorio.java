package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Alumno;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link IntentoAutoevaluacion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link IntentoAutoevaluacion}.
 */
@Repository
public interface IntentoAutoevaluacionRepositorio extends JpaRepository<IntentoAutoevaluacion, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link IntentoAutoevaluacion} activos.
     */
    List<IntentoAutoevaluacion> findByBajaFalse();

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Inscripcion indicado
     */
    List<IntentoAutoevaluacion> findByInscripcionAndBajaFalse(Inscripcion inscripcion);

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Autoevaluacion indicado
     */
    List<IntentoAutoevaluacion> findByAutoevaluacionAndBajaFalse(Autoevaluacion autoevaluacion);

    /**
     * Busca los intentos vigentes de una inscripción sobre una autoevaluación (incluido el que está en curso).
     *
     * @param inscripcion la inscripción del alumno
     * @param autoevaluacion la autoevaluación
     * @return los intentos, del más antiguo al más reciente
     */
    List<IntentoAutoevaluacion> findByInscripcionAndAutoevaluacionAndBajaFalseOrderByIdIntentoAutoevaluacion(
            Inscripcion inscripcion, Autoevaluacion autoevaluacion);

    /**
     * Busca los intentos en curso (sin entregar) de todos los alumnos.
     *
     * @return los intentos vigentes sin fecha de entrega
     */
    List<IntentoAutoevaluacion> findByBajaFalseAndFechaEntregaIsNull();

    /**
     * Busca los intentos en curso de un alumno, en cualquiera de sus inscripciones.
     *
     * @param alumno el alumno
     * @return los intentos vigentes sin fecha de entrega
     */
    @Query("select i from IntentoAutoevaluacion i where i.baja = false and i.fechaEntrega is null "
            + "and i.inscripcion.alumno = :alumno")
    List<IntentoAutoevaluacion> buscarEnCursoDelAlumno(@Param("alumno") Alumno alumno);

    /**
     * Busca los intentos vigentes de las autoevaluaciones de un curso, del más antiguo al más reciente.
     *
     * @param idCurso el identificador del curso
     * @return los intentos vigentes
     */
    @Query("select i from IntentoAutoevaluacion i where i.baja = false "
            + "and i.autoevaluacion.tema.curso.idCurso = :idCurso order by i.idIntentoAutoevaluacion")
    List<IntentoAutoevaluacion> buscarVigentesDelCurso(@Param("idCurso") Integer idCurso);
}
