package com.app.idoneos.repository;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    List<Progreso> findByInscripcion(Inscripcion inscripcion);

    Optional<Progreso> findByInscripcionAndUnidad(Inscripcion inscripcion, Unidad unidad);
}
