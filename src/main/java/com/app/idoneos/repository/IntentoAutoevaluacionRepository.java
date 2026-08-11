package com.app.idoneos.repository;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IntentoAutoevaluacionRepository extends JpaRepository<IntentoAutoevaluacion, Integer> {

    @Query("SELECT i FROM IntentoAutoevaluacion i WHERE i.autoevaluacion = :ae AND i.alumno.usuario = :usuario ORDER BY i.fecha DESC")
    List<IntentoAutoevaluacion> findByAutoevaluacionAndUsuarioOrderByFechaDesc(@Param("ae") Autoevaluacion ae, @Param("usuario") Usuario usuario);

    @Query("SELECT COUNT(i) FROM IntentoAutoevaluacion i WHERE i.autoevaluacion = :ae AND i.alumno.usuario = :usuario")
    long countByAutoevaluacionAndUsuario(@Param("ae") Autoevaluacion ae, @Param("usuario") Usuario usuario);
}
