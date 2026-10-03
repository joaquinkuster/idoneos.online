package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link UnidadCronograma} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link UnidadCronograma}.
 */
@Repository
public interface UnidadCronogramaRepositorio extends JpaRepository<UnidadCronograma, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link UnidadCronograma} activos.
     */
    List<UnidadCronograma> findByBajaFalse();

    /**
     * Busca los registros vigentes de unidadCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de UnidadCronograma asociados al Programa indicado
     */
    List<UnidadCronograma> findByProgramaAndBajaFalse(Programa programa);

    /**
     * Busca los registros vigentes de unidadCronograma asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de UnidadCronograma asociados al Unidad indicado
     */
    List<UnidadCronograma> findByUnidadAndBajaFalse(Unidad unidad);

    /**
     * Busca la unidad del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<UnidadCronograma> findByProgramaAndUnidad(Programa programa, Unidad unidad);
}
