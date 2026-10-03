package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ConsultaForo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ConsultaForo}.
 */
@Repository
public interface ConsultaForoRepositorio extends JpaRepository<ConsultaForo, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ConsultaForo} activos.
     */
    List<ConsultaForo> findByBajaFalse();

    /**
     * Busca los registros vigentes de consultaForo asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de ConsultaForo asociados al Unidad indicado
     */
    List<ConsultaForo> findByUnidadAndBajaFalse(Unidad unidad);

    /**
     * Busca los registros vigentes de consultaForo asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de ConsultaForo asociados al Inscripcion indicado
     */
    List<ConsultaForo> findByInscripcionAndBajaFalse(Inscripcion inscripcion);
}
