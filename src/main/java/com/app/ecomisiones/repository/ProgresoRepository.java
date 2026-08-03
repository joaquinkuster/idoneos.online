package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Progreso;
import com.app.ecomisiones.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    List<Progreso> findByInscripcion(Inscripcion inscripcion);

    Optional<Progreso> findByInscripcionAndUnidad(Inscripcion inscripcion, Unidad unidad);
}
