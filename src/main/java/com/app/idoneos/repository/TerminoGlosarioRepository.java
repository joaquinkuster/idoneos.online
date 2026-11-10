package com.app.idoneos.repository;

import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TerminoGlosario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de TerminoGlosario. */
@Repository
public interface TerminoGlosarioRepository extends JpaRepository<TerminoGlosario, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<TerminoGlosario> findByBajaFalse();

    /** Busca los registros vigentes de terminoGlosario asociados a tema. */
    List<TerminoGlosario> findByTemaAndBajaFalse(Tema tema);

    /** Busca todos los términos de un tema, incluidos los dados de baja. */
    List<TerminoGlosario> findByTema(Tema tema);
}
