package com.app.idoneos.repository.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TituloDocenteRepository extends JpaRepository<TituloDocente, Integer> {
    List<TituloDocente> findByDocente(Docente docente);
}

