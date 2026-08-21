package com.app.idoneos.service.Foro;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de servicios para la gestión del foro de consultas (CU-33 a CU-40).
 */
@Service
@Transactional
public class ForoServiceImpl implements ForoService {

    @Autowired private ConsultaForoRepository consultaForoRepository;
    @Autowired private RespuestaForoRepository respuestaForoRepository;

    // ─── Consultas de Foro (CU-33 a CU-36) ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultaForo> buscarConsultaPorId(Integer id) {
        return consultaForoRepository.findById(id).filter(c -> !c.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultaForo> obtenerConsultasPorUnidad(Unidad unidad) {
        return consultaForoRepository.findByUnidad(unidad).stream().filter(c -> !c.getBaja()).toList();
    }

    /**
     * CU-34 — Registrar consulta de foro.
     */
    @Override
    public ConsultaForo registrarConsulta(ConsultaForo consulta) {
        if (consulta.getTexto() == null || consulta.getTexto().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-34 Excepción paso 4: El texto de la consulta es obligatorio.");
        }
        if (consulta.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-34 Excepción paso 4: La unidad temática asociada es obligatoria.");
        }
        if (consulta.getAlumno() == null) {
            throw new ExcepcionValidacion("CU-34 Excepción paso 4: El usuario autor de la consulta es obligatorio.");
        }

        consulta.setBaja(false);
        consulta.setFecha(LocalDateTime.now());
        return consultaForoRepository.save(consulta);
    }

    /**
     * CU-35 — Modificar consulta de foro.
     */
    @Override
    public ConsultaForo modificarConsulta(int consultaId, String nuevoTexto, String ignorado, Usuario usuarioSolicitante) {
        ConsultaForo existente = consultaForoRepository.findById(consultaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Consulta de Foro", "id", consultaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-35 Precondición: No se puede modificar una consulta dada de baja.");
        }

        if (usuarioSolicitante != null && usuarioSolicitante.getRolUsuario() != RolUsuario.Administrador &&
            existente.getAlumno().getUsuario().getId() != usuarioSolicitante.getId()) {
            throw new ExcepcionValidacion("CU-35 Autorización: Solo el autor original puede editar su consulta.");
        }

        if (nuevoTexto == null || nuevoTexto.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-35 Excepción paso 4: El texto no puede quedar vacío.");
        }

        existente.setTexto(nuevoTexto.trim());
        return consultaForoRepository.save(existente);
    }

    /**
     * CU-36 — Eliminar consulta de foro (Baja Lógica).
     */
    @Override
    public void darDeBajaConsulta(int consultaId, Usuario usuarioSolicitante) {
        ConsultaForo consulta = consultaForoRepository.findById(consultaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Consulta de Foro", "id", consultaId));

        if (consulta.getBaja()) {
            throw new ExcepcionValidacion("CU-36 Excepción: La consulta ya se encuentra dada de baja.");
        }

        if (usuarioSolicitante != null && usuarioSolicitante.getRolUsuario() != RolUsuario.Administrador &&
            consulta.getAlumno().getUsuario().getId() != usuarioSolicitante.getId()) {
            throw new ExcepcionValidacion("CU-36 Autorización: Solo el autor original o un Administrador pueden eliminar la consulta.");
        }

        consulta.setBaja(true);
        consultaForoRepository.save(consulta);
    }

    // ─── Respuestas de Foro (CU-37 a CU-40) ───────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<RespuestaForo> buscarRespuestaPorId(Integer id) {
        return respuestaForoRepository.findById(id).filter(r -> !r.getBaja());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaForo> obtenerRespuestasPorConsulta(ConsultaForo consulta) {
        return respuestaForoRepository.findByConsulta(consulta).stream().filter(r -> !r.getBaja()).toList();
    }

    /**
     * CU-38 — Registrar respuesta de foro.
     */
    @Override
    public RespuestaForo registrarRespuesta(RespuestaForo respuesta) {
        if (respuesta.getTexto() == null || respuesta.getTexto().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-38 Excepción paso 4: El texto de la respuesta es obligatorio.");
        }
        if (respuesta.getConsulta() == null || respuesta.getConsulta().getBaja()) {
            throw new ExcepcionValidacion("CU-38 Excepción paso 5: La consulta de foro asociada debe estar activa.");
        }

        respuesta.setBaja(false);
        respuesta.setFecha(LocalDateTime.now());
        return respuestaForoRepository.save(respuesta);
    }

    /**
     * CU-39 — Modificar respuesta de foro.
     */
    @Override
    public RespuestaForo modificarRespuesta(int respuestaId, String nuevoTexto, Usuario usuarioSolicitante) {
        RespuestaForo existente = respuestaForoRepository.findById(respuestaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Respuesta de Foro", "id", respuestaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-39 Precondición: No se puede modificar una respuesta dada de baja.");
        }

        if (nuevoTexto == null || nuevoTexto.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-39 Excepción paso 4: El texto de la respuesta no puede quedar vacío.");
        }

        existente.setTexto(nuevoTexto.trim());
        return respuestaForoRepository.save(existente);
    }

    /**
     * CU-40 — Eliminar respuesta de foro (Baja Lógica).
     */
    @Override
    public void darDeBajaRespuesta(int respuestaId, Usuario usuarioSolicitante) {
        RespuestaForo respuesta = respuestaForoRepository.findById(respuestaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Respuesta de Foro", "id", respuestaId));

        if (respuesta.getBaja()) {
            throw new ExcepcionValidacion("CU-40 Excepción: La respuesta ya se encuentra dada de baja.");
        }

        respuesta.setBaja(true);
        respuestaForoRepository.save(respuesta);
    }
}
