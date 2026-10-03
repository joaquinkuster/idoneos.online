package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Material} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Material}.
 */
@Repository
public interface MaterialRepositorio extends JpaRepository<Material, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Material} activos.
     */
    List<Material> findByBajaFalse();

    /**
     * Busca los registros vigentes de material asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de Material asociados al ParticipacionDocente indicado
     */
    List<Material> findByParticipacionDocenteAndBajaFalse(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    List<Material> findByTipoMaterialAndBajaFalse(TipoMaterial tipoMaterial);

    /**
     * Busca los registros vigentes de material asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Material asociados al Unidad indicado
     */
    List<Material> findByUnidadAndBajaFalse(Unidad unidad);
}
