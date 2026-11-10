package com.app.idoneos.repository;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Progreso. */
@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    /** Busca los registros de progreso asociados a tema. */
    List<Progreso> findByTema(Tema tema);

    /** Busca los registros de progreso asociados a inscripcion. */
    List<Progreso> findByInscripcion(Inscripcion inscripcion);

    /** Busca el progreso a partir de la combinación única de sus relaciones. */
    Optional<Progreso> findByInscripcionAndTema(Inscripcion inscripcion, Tema tema);
}
