package com.app.idoneos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Documento;
import com.app.idoneos.model.DocumentoFuente;

/** Repositorio de DocumentoFuente. */
@Repository
public interface DocumentoFuenteRepository extends JpaRepository<DocumentoFuente, DocumentoFuente.Clave> {

    /** Busca las fuentes de un material generado. */
    List<DocumentoFuente> findByDocumento(Documento documento);

    /** Indica si el documento es fuente o evidencia de contenido generado con inteligencia artificial que sigue vigente. */
    @org.springframework.data.jpa.repository.Query(value = "select fn_documento_en_uso_por_ia(:id)", nativeQuery = true)
    boolean enUsoPorIa(@org.springframework.data.repository.query.Param("id") int idDocumento);
}
