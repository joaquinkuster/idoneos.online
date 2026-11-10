package com.app.idoneos.repository;

import com.app.idoneos.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Rol. */
@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /** Busca el rol por su atributo único 'nombre'. */
    Optional<Rol> findByNombre(String nombre);
}
