package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.exception.*;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ForoServiceImpl implements ForoService {

    @Autowired private ConsultaForoRepository consultaForoRepository;
    @Autowired private RespuestaForoRepository respuestaForoRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private DocenteRepository docenteRepository;

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

    @Override
    public ConsultaForo registrarConsulta(ConsultaForo consulta) {
        if (consulta.getTexto() == null || consulta.getTexto().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-36 Excepción: El texto de la consulta es obligatorio.");
        }
        if (consulta.getUnidad() == null) {
            throw new ExcepcionValidacion("CU-36 Excepción: La unidad temática asociada es obligatoria.");
        }
        if (consulta.getAlumno() == null) {
            throw new ExcepcionValidacion("CU-36 Excepción: El usuario autor de la consulta es obligatorio.");
        }

        consulta.setBaja(false);
        consulta.setFecha(LocalDateTime.now());
        return consultaForoRepository.save(consulta);
    }

    @Override
    public ConsultaForo crearConsulta(String texto, Usuario usuario, Unidad unidad) {
        Alumno alumno = alumnoRepository.findByUsuario(usuario)
                .orElseGet(() -> alumnoRepository.save(new Alumno(usuario)));
        return registrarConsulta(new ConsultaForo(texto, alumno, unidad));
    }

    @Override
    public ConsultaForo modificarConsulta(ConsultaForo consulta) {
        return modificarConsulta(consulta.getId(), null, consulta.getTexto(), null);
    }

    @Override
    public ConsultaForo modificarConsulta(int consultaId, String nuevoTitulo, String nuevoContenido, Usuario usuarioSolicitante) {
        ConsultaForo existente = consultaForoRepository.findById(consultaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Consulta de Foro", "id", consultaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-37 Precondición: No se puede modificar una consulta dada de baja.");
        }

        String texto = (nuevoContenido != null && !nuevoContenido.isBlank()) ? nuevoContenido : nuevoTitulo;
        if (texto == null || texto.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-37 Excepción: El texto de la consulta no puede estar vacío.");
        }

        existente.setTexto(texto.trim());
        return consultaForoRepository.save(existente);
    }

    @Override
    public void darDeBajaConsulta(int consultaId) {
        darDeBajaConsulta(consultaId, null);
    }

    @Override
    public void darDeBajaConsulta(int consultaId, Usuario usuarioSolicitante) {
        ConsultaForo existente = consultaForoRepository.findById(consultaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Consulta de Foro", "id", consultaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("La consulta ya se encuentra dada de baja.");
        }

        existente.setBaja(true);
        consultaForoRepository.save(existente);
    }

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

    @Override
    public RespuestaForo registrarRespuesta(RespuestaForo respuesta) {
        if (respuesta.getTexto() == null || respuesta.getTexto().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-40 Excepción: El texto de la respuesta es obligatorio.");
        }
        if (respuesta.getConsulta() == null) {
            throw new ExcepcionValidacion("CU-40 Excepción: La consulta de foro es obligatoria.");
        }

        respuesta.setBaja(false);
        respuesta.setFecha(LocalDateTime.now());
        return respuestaForoRepository.save(respuesta);
    }

    @Override
    public RespuestaForo crearRespuesta(String texto, Usuario usuarioDocente, ConsultaForo consulta) {
        Docente docente = docenteRepository.findById(usuarioDocente.getId())
                .orElseGet(() -> docenteRepository.findAll().isEmpty() ? null : docenteRepository.findAll().get(0));
        return registrarRespuesta(new RespuestaForo(texto, consulta, docente));
    }

    @Override
    public RespuestaForo modificarRespuesta(RespuestaForo respuesta) {
        return modificarRespuesta(respuesta.getId(), respuesta.getTexto(), null);
    }

    @Override
    public RespuestaForo modificarRespuesta(int respuestaId, String nuevoContenido, Usuario usuarioSolicitante) {
        RespuestaForo existente = respuestaForoRepository.findById(respuestaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Respuesta de Foro", "id", respuestaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-41 Precondición: No se puede modificar una respuesta dada de baja.");
        }

        if (nuevoContenido == null || nuevoContenido.trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-41 Excepción: El texto de la respuesta no puede estar vacío.");
        }

        existente.setTexto(nuevoContenido.trim());
        return respuestaForoRepository.save(existente);
    }

    @Override
    public void darDeBajaRespuesta(int respuestaId) {
        darDeBajaRespuesta(respuestaId, null);
    }

    @Override
    public void darDeBajaRespuesta(int respuestaId, Usuario usuarioSolicitante) {
        RespuestaForo existente = respuestaForoRepository.findById(respuestaId)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Respuesta de Foro", "id", respuestaId));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("La respuesta ya se encuentra dada de baja.");
        }

        existente.setBaja(true);
        respuestaForoRepository.save(existente);
    }
}
