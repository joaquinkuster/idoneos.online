package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.MaterialDidactico;

/** Repositorio de MaterialDidactico. */
@Repository
public interface MaterialDidacticoRepository extends JpaRepository<MaterialDidactico, Integer> {

    /** Busca los materiales generados con inteligencia artificial que están en alguno de los estados indicados. */
    java.util.List<MaterialDidactico> findByEstadoGeneracionIaNombreIn(java.util.Collection<String> estados);
}
