package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Recurso;
import com.app.idoneos.modelo.TemaRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link TemaRecurso} en la base de datos.
 */
@Repository
public interface TemaRecursoRepositorio extends JpaRepository<TemaRecurso, Integer> {

    /**
     * Busca en la base, con su tema y su curso ya cargados, filtrando por curso, tema y baja (cada filtro es opcional).
     * Antes se traía todo y se filtraba en memoria.
     */
    @org.springframework.data.jpa.repository.Query("""
            select m from TemaRecurso m join fetch m.tema t join fetch t.curso c
            where (cast(:idCurso as integer) is null or c.idCurso = :idCurso)
              and (cast(:idTema as integer) is null or t.idTema = :idTema)
              and (cast(:baja as boolean) is null or m.baja = :baja)
            """)
    List<TemaRecurso> buscarPorFiltros(@org.springframework.data.repository.query.Param("idCurso") Integer idCurso,
            @org.springframework.data.repository.query.Param("idTema") Integer idTema,
            @org.springframework.data.repository.query.Param("baja") Boolean baja);

    /**
     * Busca los recursos activos de un tema.
     *
     * @param tema el tema
     * @return los recursos activos del tema
     */
    List<TemaRecurso> findByTemaAndBajaFalse(com.app.idoneos.modelo.Tema tema);

    /**
     * Busca los recursos que incorporó una participación docente y siguen activos.
     *
     * @param docenteCurso la participación
     * @return los recursos activos
     */
    List<TemaRecurso> findByDocenteCursoAndBajaFalse(com.app.idoneos.modelo.DocenteCurso docenteCurso);

    /**
     * Busca los usos activos de un recurso.
     *
     * @param recurso el recurso
     * @return los usos activos
     */
    List<TemaRecurso> findByRecursoAndBajaFalse(Recurso recurso);

    /**
     * Busca los registros activos.
     *
     * @return los registros activos
     */
    List<TemaRecurso> findByBajaFalse();
}
