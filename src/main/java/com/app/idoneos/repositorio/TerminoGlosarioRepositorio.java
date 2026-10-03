package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TerminoGlosario} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TerminoGlosario}.
 */
@Repository
public interface TerminoGlosarioRepositorio extends JpaRepository<TerminoGlosario, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link TerminoGlosario} activos.
     */
    List<TerminoGlosario> findByBajaFalse();

    /**
     * Busca los registros vigentes de terminoGlosario asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de TerminoGlosario asociados al Unidad indicado
     */
    List<TerminoGlosario> findByUnidadAndBajaFalse(Unidad unidad);
}
