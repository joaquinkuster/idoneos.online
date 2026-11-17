package com.app.idoneos.repository;

import com.app.idoneos.model.CategoriaMaterialDidactico;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de CategoriaMaterialDidactico. */
@Repository
public interface CategoriaMaterialDidacticoRepository extends JpaRepository<CategoriaMaterialDidactico, Integer> {

    /** Busca el registro por su atributo único 'nombre'. */
    Optional<CategoriaMaterialDidactico> findByNombre(String nombre);
}
