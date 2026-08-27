package com.app.idoneos.service.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Servicio para el seguimiento y cálculo del porcentaje de avance del alumno en la cursada.
 *
 * MOD-F-03: Módulo de Inscripciones y Pagos
 *   CU-48 — Buscar progreso: consulta y registro de unidades pedagógicas completadas por el estudiante.
 */
@Service
@Transactional
public class ProgresoServiceImpl implements ProgresoService {

    @Autowired private ProgresoRepository progresoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Progreso> obtenerTodo() {
        return progresoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion) {
        return progresoRepository.findByInscripcion(inscripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
    }

    /**
     * CU-46 — Marcar unidad como completada e incrementar avance de cursada.
     */
    @Override
    public Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad) {
        Optional<Progreso> existente = progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad);
        if (existente.isPresent()) {
            Progreso p = existente.get();
            if (!p.getCompletada()) {
                p.setCompletada(true);
                p.setFechaCompletado(LocalDate.now());
                return progresoRepository.save(p);
            }
            return p;
        }
        Progreso nuevo = new Progreso(inscripcion, unidad, true);
        return progresoRepository.save(nuevo);
    }

    /**
     * CU-46 — Contar unidades temáticas completadas por la inscripción.
     */
    @Override
    @Transactional(readOnly = true)
    public int contarCompletadas(Inscripcion inscripcion) {
        return (int) progresoRepository.findByInscripcion(inscripcion)
                .stream()
                .filter(Progreso::getCompletada)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepository.findByInscripcionAndUnidad(inscripcion, unidad)
                .map(Progreso::getCompletada)
                .orElse(false);
    }

    @Override
    public Progreso guardar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public Progreso modificar(Progreso progreso) {
        return progresoRepository.save(progreso);
    }

    @Override
    public void borrar(Progreso progreso) {
        progresoRepository.delete(progreso);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return progresoRepository.existsById(id);
    }
}

