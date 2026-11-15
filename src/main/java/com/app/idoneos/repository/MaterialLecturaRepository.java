package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.MaterialLectura;

/** Repositorio de MaterialLectura. */
@Repository
public interface MaterialLecturaRepository extends JpaRepository<MaterialLectura, Integer> {

    /** Busca los materiales generados con inteligencia artificial que están en alguno de los estados indicados. */
    java.util.List<MaterialLectura> findByEstadoGeneracionIaNombreIn(java.util.Collection<String> estados);
}
