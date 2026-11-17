package com.app.idoneos.repository;

import com.app.idoneos.model.IndiceDocumento;
import com.app.idoneos.model.Documento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de IndiceDocumento. */
@Repository
public interface IndiceDocumentoRepository extends JpaRepository<IndiceDocumento, Integer> {

    /** Busca las entradas del índice de un documento, en su orden. */
    List<IndiceDocumento> findByDocumentoOrderByNumeroOrden(Documento documento);

    /** Borra todas las entradas del índice de un documento (el índice se vuelve a guardar completo cada vez). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from IndiceDocumento i where i.documento = :documento")
    void borrarDelDocumento(@Param("documento") Documento documento);
}
