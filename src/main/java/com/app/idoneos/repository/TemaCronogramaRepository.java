package com.app.idoneos.repository;

import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TemaCronograma. */
@Repository
public interface TemaCronogramaRepository extends JpaRepository<TemaCronograma, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<TemaCronograma> findByBajaFalse();

    /** Busca los registros vigentes de temaCronograma asociados a programa. */
    List<TemaCronograma> findByProgramaAndBajaFalse(Programa programa);

    /** Busca los registros vigentes de temaCronograma asociados a tema. */
    List<TemaCronograma> findByTemaAndBajaFalse(Tema tema);

}
