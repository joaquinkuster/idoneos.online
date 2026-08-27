package com.app.idoneos.service.modulo_evaluaciones;

import com.app.idoneos.repository.modulo_evaluaciones.*;

import com.app.idoneos.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Servicio para la administración de Pools de Preguntas y
 * Configuración de Autoevaluaciones.
 *
 * MOD-F-04: Módulo de Evaluaciones y Rendición
 * CU-53 — Buscar pool: listado y filtrado de bancos de preguntas por unidad.
 * CU-54 — Crear pool: alta de banco con preguntas y opciones de respuesta
 * múltiple / V-F.
 * CU-55 — Modificar pool: edición del pool, agregado y baja de preguntas y
 * opciones.
 * CU-56 — Dar de baja pool: baja lógica de bancos de evaluación.
 * CU-57 — Buscar autoevaluación: consulta de exámenes por unidad y nombre.
 * CU-58 — Crear autoevaluación: configuración de tiempo límite, fechas y
 * asociación de pools.
 * CU-59 — Modificar autoevaluación: edición general con control de intentos
 * activos.
 * CU-60 — Dar de baja autoevaluación: baja lógica de la instancia evaluativa.
 */
@Service
public class EvaluacionService {

    @Autowired
    private PoolRepository poolRepository;
    @Autowired
    private PreguntaRepository preguntaRepository;
    @Autowired
    private OpcionRespuestaRepository opcionRespuestaRepository;
    @Autowired
    private AutoevaluacionRepository autoevaluacionRepository;

    // ─── Pool ─────────────────────────────────────────────────────────────────

    public Pool guardarPool(Pool pool) {
        return poolRepository.save(pool);
    }

    public Optional<Pool> buscarPoolPorId(Integer id) {
        return poolRepository.findById(id).filter(p -> !p.getBaja());
    }

    public Optional<Pool> buscarPoolPorUnidad(Unidad unidad) {
        return poolRepository.findByUnidadAndBajaFalse(unidad);
    }

    public void borrarPool(Pool pool) {
        pool.setBaja(true);
        poolRepository.save(pool);
    }

    // ─── Pregunta ─────────────────────────────────────────────────────────────

    public Pregunta guardarPregunta(Pregunta pregunta) {
        return preguntaRepository.save(pregunta);
    }

    public Optional<Pregunta> buscarPreguntaPorId(Integer id) {
        return preguntaRepository.findById(id).filter(p -> !p.getBaja());
    }

    public List<Pregunta> preguntasPorPool(Pool pool) {
        return preguntaRepository.findByPoolAndBajaFalse(pool);
    }

    public void borrarPregunta(Pregunta pregunta) {
        pregunta.setBaja(true);
        preguntaRepository.save(pregunta);
    }

    // ─── OpcionRespuesta ──────────────────────────────────────────────────────

    public OpcionRespuesta guardarOpcion(OpcionRespuesta opcion) {
        return opcionRespuestaRepository.save(opcion);
    }

    public Optional<OpcionRespuesta> buscarOpcionPorId(Integer id) {
        return opcionRespuestaRepository.findById(id).filter(o -> !o.getBaja());
    }

    public void borrarOpcion(OpcionRespuesta opcion) {
        opcion.setBaja(true);
        opcionRespuestaRepository.save(opcion);
    }

    // ─── Autoevaluacion ───────────────────────────────────────────────────────

    public Autoevaluacion guardarAutoevaluacion(Autoevaluacion ae) {
        return autoevaluacionRepository.save(ae);
    }

    public Optional<Autoevaluacion> buscarAutoevaluacionPorId(Integer id) {
        return autoevaluacionRepository.findById(id).filter(a -> !a.getBaja());
    }

    public List<Autoevaluacion> autoevaluacionesPorPool(Pool pool) {
        return autoevaluacionRepository.findByPoolsContainingAndBajaFalse(pool);
    }

    public List<Autoevaluacion> buscarAutoevaluacionesPorUnidad(Unidad unidad) {
        return autoevaluacionRepository.findByUnidadAndBajaFalse(unidad);
    }

    public void borrarAutoevaluacion(Autoevaluacion ae) {
        ae.setBaja(true);
        autoevaluacionRepository.save(ae);
    }
}
