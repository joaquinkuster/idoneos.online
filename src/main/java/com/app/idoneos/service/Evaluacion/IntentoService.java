package com.app.idoneos.service.Evaluacion;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de intentos de autoevaluación (CU-59 y CU-60).
 * Sortea preguntas aleatoriamente de los pools asociados, corrige automáticamente y calcula la calificación final.
 */
@Service
@Transactional
public class IntentoService {

    private static final int PREGUNTAS_POR_INTENTO = 10;
    private static final double UMBRAL_APROBACION = 60.0; // porcentaje

    @Autowired private IntentoAutoevaluacionRepository intentoRepository;
    @Autowired private RespuestaIntentoRepository respuestaRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;

    /**
     * CU-60 — Realizar intento de autoevaluación.
     * Regla de negocio: Valida el límite de intentos permitidos antes de iniciar.
     */
    public IntentoAutoevaluacion iniciarIntento(Autoevaluacion autoevaluacion, Usuario usuario) {
        if (autoevaluacion == null || autoevaluacion.getBaja()) {
            throw new ExcepcionValidacion("CU-60 Precondición: La autoevaluación seleccionada no está activa.");
        }

        if (autoevaluacion.getIntentosPermitidos() != null && autoevaluacion.getIntentosPermitidos() > 0) {
            long intentosYaRealizados = intentoRepository.countByAutoevaluacion(autoevaluacion);
            if (intentosYaRealizados >= autoevaluacion.getIntentosPermitidos()) {
                throw new ExcepcionValidacion("CU-60 Excepción paso 4: Ha alcanzado el límite máximo de intentos permitidos (" + autoevaluacion.getIntentosPermitidos() + ") para esta evaluación.");
            }
        }
        return new IntentoAutoevaluacion(autoevaluacion);
    }

    /**
     * CU-60 — Sortea hasta 10 preguntas aleatorias de los pools activos asociados a la autoevaluación.
     */
    @Transactional(readOnly = true)
    public List<Pregunta> sortearPreguntas(Autoevaluacion autoevaluacion) {
        List<Pregunta> todas = autoevaluacion.getPools().stream()
                .map(pa -> pa.getPool())
                .flatMap(pool -> preguntaRepository.findByPoolAndBajaFalse(pool).stream())
                .filter(p -> !p.getBaja())
                .collect(Collectors.toList());

        if (todas.isEmpty()) {
            throw new ExcepcionValidacion("CU-60 Excepción paso 5: La autoevaluación no posee preguntas cargadas en sus pools.");
        }

        Collections.shuffle(todas);
        return todas.stream()
                .limit(PREGUNTAS_POR_INTENTO)
                .collect(Collectors.toList());
    }

    /**
     * CU-60 — Corrige el intento, calcula el porcentaje de aciertos y persiste el resultado.
     */
    @Transactional
    public IntentoAutoevaluacion corregirYGuardar(
            IntentoAutoevaluacion intento,
            Map<Integer, Integer> respuestas) {

        if (respuestas == null || respuestas.isEmpty()) {
            throw new ExcepcionValidacion("CU-60 Excepción: Debe responder al menos una pregunta de la autoevaluación.");
        }

        intento.setFecha(LocalDateTime.now());
        IntentoAutoevaluacion guardado = intentoRepository.save(intento);

        int correctas = 0;
        int total = respuestas.size();

        for (Map.Entry<Integer, Integer> entry : respuestas.entrySet()) {
            Integer opcionId = entry.getValue();
            OpcionRespuesta opcion = opcionRepository.findById(opcionId)
                    .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Opción de Respuesta", "id", opcionId));

            RespuestaIntento ri = new RespuestaIntento(guardado, opcion);
            respuestaRepository.save(ri);

            if (Boolean.TRUE.equals(opcion.getEsCorrecta())) {
                correctas++;
            }
        }

        float nota = total > 0 ? (float)(correctas * 100.0) / total : 0.0f;
        guardado.setNota(nota);
        return intentoRepository.save(guardado);
    }

    @Transactional(readOnly = true)
    public boolean estaAprobado(IntentoAutoevaluacion intento) {
        return intento != null && intento.getNota() >= UMBRAL_APROBACION;
    }

    @Transactional(readOnly = true)
    public List<IntentoAutoevaluacion> historialPorAlumno(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae);
    }

    @Transactional(readOnly = true)
    public boolean alumnoAproboAutoevaluacion(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae)
                .stream().anyMatch(this::estaAprobado);
    }
}
