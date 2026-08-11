package com.app.idoneos.service.Foro;

import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión del foro de consultas (CU-33 a CU-40).
 */
public interface ForoService {

    // ─── Consultas de Foro (CU-33 a CU-36) ───────────────────────────────────

    Optional<ConsultaForo> buscarConsultaPorId(Integer id);

    List<ConsultaForo> obtenerConsultasPorUnidad(Unidad unidad);

    ConsultaForo registrarConsulta(ConsultaForo consulta);

    ConsultaForo modificarConsulta(int consultaId, String nuevoTitulo, String nuevoContenido, Usuario usuarioSolicitante);

    void darDeBajaConsulta(int consultaId, Usuario usuarioSolicitante);

    // ─── Respuestas de Foro (CU-37 a CU-40) ───────────────────────────────────

    Optional<RespuestaForo> buscarRespuestaPorId(Integer id);

    List<RespuestaForo> obtenerRespuestasPorConsulta(ConsultaForo consulta);

    RespuestaForo registrarRespuesta(RespuestaForo respuesta);

    RespuestaForo modificarRespuesta(int respuestaId, String nuevoContenido, Usuario usuarioSolicitante);

    void darDeBajaRespuesta(int respuestaId, Usuario usuarioSolicitante);
}
