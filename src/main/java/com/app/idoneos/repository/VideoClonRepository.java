package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.VideoClon;

/** Repositorio de VideoClon. */
@Repository
public interface VideoClonRepository extends JpaRepository<VideoClon, Integer> {

    /** Los videos con un estado de generación que ya se pidieron a HeyGen. */
    java.util.List<VideoClon> findByEstadoGeneracionIaNombreAndIdVideoExternoIsNotNull(String estado);

    /** Cuenta los videos con clon que un docente pidió desde una fecha. */
    @org.springframework.data.jpa.repository.Query("select count(v) from VideoClon v where v.recurso.docente = :docente and v.recurso.fechaCreacion >= :desde")
    long contarDelDocenteDesde(@org.springframework.data.repository.query.Param("docente") com.app.idoneos.model.Docente docente,
            @org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde);
}
