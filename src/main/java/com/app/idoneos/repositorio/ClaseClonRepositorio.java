package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.ClaseClon;
import com.app.idoneos.modelo.EstadoClaseClon;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ClaseClon} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ClaseClon}.
 */
@Repository
public interface ClaseClonRepositorio extends JpaRepository<ClaseClon, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ClaseClon} activos.
     */
    List<ClaseClon> findByBajaFalse();

    /**
     * Busca los registros vigentes de claseClon asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseClon asociados al ParticipacionDocente indicado
     */
    List<ClaseClon> findByParticipacionDocenteAndBajaFalse(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de claseClon asociados a estadoClaseClon.
     *
     * @param estadoClaseClon el registro de EstadoClaseClon asociado
     * @return una lista de ClaseClon asociados al EstadoClaseClon indicado
     */
    List<ClaseClon> findByEstadoClaseClonAndBajaFalse(EstadoClaseClon estadoClaseClon);

    /**
     * Busca los registros vigentes de claseClon asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseClon asociados al Material indicado
     */
    List<ClaseClon> findByMaterialAndBajaFalse(Material material);
}
