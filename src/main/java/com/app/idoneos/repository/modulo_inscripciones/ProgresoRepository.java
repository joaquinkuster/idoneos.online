package com.app.idoneos.repository.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    List<Progreso> findByInscripcion(Inscripcion inscripcion);

    Optional<Progreso> findByInscripcionAndUnidad(Inscripcion inscripcion, Unidad unidad);
}

