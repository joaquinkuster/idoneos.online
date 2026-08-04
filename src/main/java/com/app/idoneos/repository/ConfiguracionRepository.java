package com.app.idoneos.repository;

import com.app.idoneos.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Integer> {
    Optional<Configuracion> findByClave(String clave);
}
