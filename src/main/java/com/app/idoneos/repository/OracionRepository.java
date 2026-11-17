package com.app.idoneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Documento;
import com.app.idoneos.model.Oracion;

/** Repositorio de Oracion. */
@Repository
public interface OracionRepository extends JpaRepository<Oracion, Integer> {

    /** Busca las oraciones de un documento, en el orden del documento: primero por entrada del índice y después dentro de cada una. */
    @Query("select o from Oracion o where o.indiceDocumento.documento = :documento order by o.indiceDocumento.numeroOrden, o.numeroOrden")
    List<Oracion> buscarDelDocumento(@Param("documento") Documento documento);

    /** Borra todas las oraciones de un documento (se vuelven a armar completas cuando cambia su índice o su archivo). */
    @Modifying(flushAutomatically = true)
    @Query("delete from Oracion o where o.indiceDocumento in (select i from IndiceDocumento i where i.documento = :documento)")
    void borrarDelDocumento(@Param("documento") Documento documento);
}
