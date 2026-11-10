package com.app.idoneos.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Parametro;

/** Repositorio de Parametro. */
@Repository
public interface ParametroRepository extends JpaRepository<Parametro, Integer> {

    /** Busca el parámetro por su atributo único 'clave'. */
    Optional<Parametro> findByClave(String clave);
}
