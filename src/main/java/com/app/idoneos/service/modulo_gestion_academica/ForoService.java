package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

public interface ForoService {

    Optional<ConsultaForo> buscarConsultaPorId(Integer id);

    List<ConsultaForo> obtenerConsultasPorUnidad(Unidad unidad);

    ConsultaForo registrarConsulta(ConsultaForo consulta);

    ConsultaForo crearConsulta(String texto, Usuario usuario, Unidad unidad);

    ConsultaForo modificarConsulta(ConsultaForo consulta);

    ConsultaForo modificarConsulta(int consultaId, String nuevoTitulo, String nuevoContenido, Usuario usuarioSolicitante);

    void darDeBajaConsulta(int consultaId);

    void darDeBajaConsulta(int consultaId, Usuario usuarioSolicitante);

    Optional<RespuestaForo> buscarRespuestaPorId(Integer id);

    List<RespuestaForo> obtenerRespuestasPorConsulta(ConsultaForo consulta);

    RespuestaForo registrarRespuesta(RespuestaForo respuesta);

    RespuestaForo crearRespuesta(String texto, Usuario docente, ConsultaForo consulta);

    RespuestaForo modificarRespuesta(RespuestaForo respuesta);

    RespuestaForo modificarRespuesta(int respuestaId, String nuevoContenido, Usuario usuarioSolicitante);

    void darDeBajaRespuesta(int respuestaId);

    void darDeBajaRespuesta(int respuestaId, Usuario usuarioSolicitante);
}
