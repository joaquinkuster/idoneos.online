package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Tema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Autoevaluacion. */
@Repository
public interface AutoevaluacionRepository extends JpaRepository<Autoevaluacion, Integer> {

    /** Busca en la base, con su tema y su curso ya cargados, filtrando por curso. */
    @org.springframework.data.jpa.repository.Query("""
            select a from Autoevaluacion a join fetch a.tema t join fetch t.curso c
            where (cast(:idCurso as integer) is null or c.idCurso = :idCurso)
              and (cast(:idTema as integer) is null or t.idTema = :idTema)
              and (cast(:baja as boolean) is null or a.baja = :baja)
            """)
    List<Autoevaluacion> buscarPorFiltros(@org.springframework.data.repository.query.Param("idCurso") Integer idCurso,
            @org.springframework.data.repository.query.Param("idTema") Integer idTema,
            @org.springframework.data.repository.query.Param("baja") Boolean baja);

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Autoevaluacion> findByBajaFalse();

    /** Busca los registros vigentes de autoevaluacion asociados a tema. */
    List<Autoevaluacion> findByTemaAndBajaFalse(Tema tema);
}
