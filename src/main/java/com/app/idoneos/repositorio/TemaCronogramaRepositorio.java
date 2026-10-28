package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TemaCronograma} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TemaCronograma}.
 */
@Repository
public interface TemaCronogramaRepositorio extends JpaRepository<TemaCronograma, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link TemaCronograma} activos.
     */
    List<TemaCronograma> findByBajaFalse();

    /**
     * Busca los registros vigentes de temaCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de TemaCronograma asociados al Programa indicado
     */
    List<TemaCronograma> findByProgramaAndBajaFalse(Programa programa);

    /**
     * Busca los registros vigentes de temaCronograma asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TemaCronograma asociados al Tema indicado
     */
    List<TemaCronograma> findByTemaAndBajaFalse(Tema tema);

    /**
     * Busca el tema del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TemaCronograma> findByProgramaAndTema(Programa programa, Tema tema);
}
