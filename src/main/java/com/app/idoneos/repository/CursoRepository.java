package com.app.idoneos.repository;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Curso. */
@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Curso> findByBajaFalse();

    /** Busca los registros vigentes de curso asociados a categoria. */
    List<Curso> findByCategoriaAndBajaFalse(Categoria categoria);

    /** Busca el curso por su atributo único 'nombre'. */
    Optional<Curso> findByNombre(String nombre);
}
