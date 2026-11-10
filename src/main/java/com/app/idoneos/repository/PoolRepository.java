package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Tema;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Pool. */
@Repository
public interface PoolRepository extends JpaRepository<Pool, Integer> {

    /** Busca en la base, con su tema y su curso ya cargados, filtrando por curso. */
    @org.springframework.data.jpa.repository.Query("""
            select p from Pool p join fetch p.tema t join fetch t.curso c
            where (cast(:idCurso as integer) is null or c.idCurso = :idCurso)
              and (cast(:idTema as integer) is null or t.idTema = :idTema)
              and (cast(:baja as boolean) is null or p.baja = :baja)
            """)
    List<Pool> buscarPorFiltros(@org.springframework.data.repository.query.Param("idCurso") Integer idCurso,
            @org.springframework.data.repository.query.Param("idTema") Integer idTema,
            @org.springframework.data.repository.query.Param("baja") Boolean baja);

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Pool> findByBajaFalse();

    /** Busca los registros vigentes de pool asociados a tema. */
    List<Pool> findByTemaAndBajaFalse(Tema tema);

    /** Busca todos los pools de un tema, incluidos los dados de baja. */
    List<Pool> findByTema(Tema tema);
}
