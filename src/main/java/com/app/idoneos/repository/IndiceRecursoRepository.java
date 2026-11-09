package com.app.idoneos.repository;

import com.app.idoneos.model.IndiceRecurso;
import com.app.idoneos.model.Recurso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link IndiceRecurso} en la base de datos.
 */
@Repository
public interface IndiceRecursoRepository extends JpaRepository<IndiceRecurso, Integer> {

    /**
     * Las entradas del índice de un documento, en su orden.
     *
     * @param recurso el documento
     * @return las entradas, de la primera a la última
     */
    List<IndiceRecurso> findByRecursoOrderByNumeroOrden(Recurso recurso);

    /**
     * Borra todas las entradas del índice de un documento (el índice se vuelve a guardar completo cada vez).
     *
     * @param recurso el documento
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from IndiceRecurso i where i.recurso = :recurso")
    void borrarDelRecurso(@Param("recurso") Recurso recurso);
}
