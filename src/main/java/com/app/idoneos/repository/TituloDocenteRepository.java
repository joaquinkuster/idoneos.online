package com.app.idoneos.repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TituloDocente. */
@Repository
public interface TituloDocenteRepository extends JpaRepository<TituloDocente, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<TituloDocente> findByBajaFalse();

    /** Busca los registros vigentes de tituloDocente asociados a docente. */
    List<TituloDocente> findByDocenteAndBajaFalse(Docente docente);
}
