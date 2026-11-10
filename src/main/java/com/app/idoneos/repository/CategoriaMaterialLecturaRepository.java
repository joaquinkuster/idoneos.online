package com.app.idoneos.repository;

import com.app.idoneos.model.CategoriaMaterialLectura;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de CategoriaMaterialLectura. */
@Repository
public interface CategoriaMaterialLecturaRepository extends JpaRepository<CategoriaMaterialLectura, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<CategoriaMaterialLectura> findByNombre(String nombre);
}
