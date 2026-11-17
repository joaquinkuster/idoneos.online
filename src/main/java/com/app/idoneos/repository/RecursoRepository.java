package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Recurso;

/** Repositorio de Recurso. */
@Repository
public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    /** Busca los recursos activos de un docente. */
    List<Recurso> findByDocenteAndBajaFalse(com.app.idoneos.model.Docente docente);

    /** Busca los recursos públicos activos: los que cualquier docente puede ver desde la biblioteca. */
    List<Recurso> findByPrivadoFalseAndBajaFalse();

    /** Busca todos los recursos activos, públicos y privados (lo que ve el administrador). */
    List<Recurso> findByBajaFalse();

    /** Cuenta los materiales (resúmenes y presentaciones) que un docente pidió generar con IA desde una fecha. */
    @org.springframework.data.jpa.repository.Query("select count(r) from Recurso r where r.docente = :docente and r.documento.materialDidactico.estadoGeneracionIa is not null "
            + "and r.fechaCreacion >= :desde")
    long contarGeneradosConIa(@org.springframework.data.repository.query.Param("docente") com.app.idoneos.model.Docente docente,
            @org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde);
}
