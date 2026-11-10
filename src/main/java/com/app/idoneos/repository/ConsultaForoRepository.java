package com.app.idoneos.repository;

import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Tema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de ConsultaForo. */
@Repository
public interface ConsultaForoRepository extends JpaRepository<ConsultaForo, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<ConsultaForo> findByBajaFalse();

    /** Busca los registros vigentes de consultaForo asociados a tema. */
    List<ConsultaForo> findByTemaAndBajaFalse(Tema tema);

    /** Busca los registros vigentes de consultaForo asociados a inscripcion. */
    List<ConsultaForo> findByInscripcionAndBajaFalse(Inscripcion inscripcion);
}
