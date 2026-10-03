package com.app.idoneos.repositorio;

import java.util.List;
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
}
