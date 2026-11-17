package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.idoneos.model.MaterialBibliografico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de MaterialBibliografico. */
@Repository
public interface MaterialBibliograficoRepository extends JpaRepository<MaterialBibliografico, Integer> {

    /** Indica si otra bibliografía vigente (de un recurso distinto al indicado) tiene el mismo identificador. */
    @org.springframework.data.jpa.repository.Query("""
            select count(b) > 0 from MaterialBibliografico b
            where b.documento.recurso.baja = false and b.documento.recurso.idRecurso <> :idRecurso
              and upper(replace(replace(b.identificador, '-', ''), ' ', '')) = :identificadorNormalizado
            """)
    boolean existePorIdentificador(@org.springframework.data.repository.query.Param("identificadorNormalizado") String identificadorNormalizado,
            @org.springframework.data.repository.query.Param("idRecurso") int idRecurso);
}
