package com.app.idoneos.service.modulo_gestion_academica;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

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

