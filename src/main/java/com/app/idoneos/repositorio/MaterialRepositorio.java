package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Tema;
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
     * Busca los registros vigentes de material asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de Material asociados al DocenteCurso indicado
     */
    List<Material> findByDocenteCursoAndBajaFalse(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    List<Material> findByTipoMaterialAndBajaFalse(TipoMaterial tipoMaterial);

    /**
     * Busca los registros vigentes de material asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Material asociados al Tema indicado
     */
    List<Material> findByTemaAndBajaFalse(Tema tema);
}
