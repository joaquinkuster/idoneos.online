package com.app.idoneos.repository;

import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerminoGlosarioRepository extends JpaRepository<TerminoGlosario, Integer> {
    List<TerminoGlosario> findByUnidadAndBajaFalse(Unidad unidad);
}
