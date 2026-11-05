package com.app.idoneos.repository;

import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TemaCronograma} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link TemaCronograma}.
 */
@Repository
public interface TemaCronogramaRepository extends JpaRepository<TemaCronograma, Integer> {

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

}
