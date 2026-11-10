package com.app.idoneos.repository;

import com.app.idoneos.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Categoria. */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Categoria> findByBajaFalse();

    /** Busca la categoría por su atributo único 'nombre'. */
    Optional<Categoria> findByNombre(String nombre);
}
