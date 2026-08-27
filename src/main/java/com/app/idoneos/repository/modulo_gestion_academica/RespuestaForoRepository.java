package com.app.idoneos.repository.modulo_gestion_academica;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RespuestaForoRepository extends JpaRepository<RespuestaForo, Integer> {
    java.util.List<RespuestaForo> findByConsulta(com.app.idoneos.model.ConsultaForo consulta);
}

