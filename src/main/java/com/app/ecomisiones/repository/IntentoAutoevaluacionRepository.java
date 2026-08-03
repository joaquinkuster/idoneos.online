package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Autoevaluacion;
import com.app.ecomisiones.model.IntentoAutoevaluacion;
import com.app.ecomisiones.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {
    List<IntentoAutoevaluacion> findByAutoevaluacionAndUsuarioOrderByFechaDesc(Autoevaluacion ae, Usuario usuario);
    long countByAutoevaluacionAndUsuario(Autoevaluacion ae, Usuario usuario);
}
