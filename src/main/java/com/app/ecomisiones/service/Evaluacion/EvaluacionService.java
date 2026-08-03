package com.app.ecomisiones.service.Evaluacion;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar pools de preguntas, preguntas, opciones y autoevaluaciones.
 */
@Service
public class EvaluacionService {

    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRespuestaRepository;
    @Autowired private AutoevaluacionRepository autoevaluacionRepository;

    // ─── Pool ─────────────────────────────────────────────────────────────────

    public Pool guardarPool(Pool pool) { return poolRepository.save(pool); }

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

    public Pregunta guardarPregunta(Pregunta pregunta) { return preguntaRepository.save(pregunta); }

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

    public OpcionRespuesta guardarOpcion(OpcionRespuesta opcion) { return opcionRespuestaRepository.save(opcion); }

    public Optional<OpcionRespuesta> buscarOpcionPorId(Integer id) {
        return opcionRespuestaRepository.findById(id).filter(o -> !o.getBaja());
    }

    public void borrarOpcion(OpcionRespuesta opcion) {
        opcion.setBaja(true);
        opcionRespuestaRepository.save(opcion);
    }

    // ─── Autoevaluacion ───────────────────────────────────────────────────────

    public Autoevaluacion guardarAutoevaluacion(Autoevaluacion ae) { return autoevaluacionRepository.save(ae); }

    public Optional<Autoevaluacion> buscarAutoevaluacionPorId(Integer id) {
        return autoevaluacionRepository.findById(id).filter(a -> !a.getBaja());
    }

    public List<Autoevaluacion> autoevaluacionesPorPool(Pool pool) {
        return autoevaluacionRepository.findByPoolsContainingAndBajaFalse(pool);
    }

    public void borrarAutoevaluacion(Autoevaluacion ae) {
        ae.setBaja(true);
        autoevaluacionRepository.save(ae);
    }
}
