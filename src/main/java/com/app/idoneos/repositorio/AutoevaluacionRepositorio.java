package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Autoevaluacion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Autoevaluacion}.
 */
@Repository
public interface AutoevaluacionRepositorio extends JpaRepository<Autoevaluacion, Integer> {

    /**
     * Busca en la base, con su tema y su curso ya cargados, filtrando por curso, tema y baja (cada filtro es opcional).
     * Antes se traía todo y se filtraba en memoria.
     */
    @org.springframework.data.jpa.repository.Query("""
            select a from Autoevaluacion a join fetch a.tema t join fetch t.curso c
            where (cast(:idCurso as integer) is null or c.idCurso = :idCurso)
              and (cast(:idTema as integer) is null or t.idTema = :idTema)
              and (cast(:baja as boolean) is null or a.baja = :baja)
            """)
    List<Autoevaluacion> buscarPorFiltros(@org.springframework.data.repository.query.Param("idCurso") Integer idCurso,
            @org.springframework.data.repository.query.Param("idTema") Integer idTema,
            @org.springframework.data.repository.query.Param("baja") Boolean baja);

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Autoevaluacion} activos.
     */
    List<Autoevaluacion> findByBajaFalse();

    /**
     * Busca los registros vigentes de autoevaluacion asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Autoevaluacion asociados al Tema indicado
     */
    List<Autoevaluacion> findByTemaAndBajaFalse(Tema tema);
}
