package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Pool} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Pool}.
 */
@Repository
public interface PoolRepositorio extends JpaRepository<Pool, Integer> {

    /**
     * Busca en la base, con su tema y su curso ya cargados, filtrando por curso, tema y baja (cada filtro es opcional).
     * Antes se traía todo y se filtraba en memoria.
     */
    @org.springframework.data.jpa.repository.Query("""
            select p from Pool p join fetch p.tema t join fetch t.curso c
            where (cast(:idCurso as integer) is null or c.idCurso = :idCurso)
              and (cast(:idTema as integer) is null or t.idTema = :idTema)
              and (cast(:baja as boolean) is null or p.baja = :baja)
            """)
    List<Pool> buscarPorFiltros(@org.springframework.data.repository.query.Param("idCurso") Integer idCurso,
            @org.springframework.data.repository.query.Param("idTema") Integer idTema,
            @org.springframework.data.repository.query.Param("baja") Boolean baja);

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Pool} activos.
     */
    List<Pool> findByBajaFalse();

    /**
     * Busca los registros vigentes de pool asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Pool asociados al Tema indicado
     */
    List<Pool> findByTemaAndBajaFalse(Tema tema);

    /**
     * Busca todos los pools de un tema, incluidos los dados de baja.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Pool del tema
     */
    List<Pool> findByTema(Tema tema);
}
