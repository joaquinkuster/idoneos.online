package com.app.idoneos.repository.modulo_ia;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoClaseClonIARepository extends JpaRepository<EstadoClaseClonIA, Integer> {
    Optional<EstadoClaseClonIA> findByNombre(String nombre);
}

