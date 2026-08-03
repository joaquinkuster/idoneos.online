package com.app.ecomisiones.service.Evaluacion;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar intentos de autoevaluación.
 * Sortea preguntas, corrige automáticamente y calcula nota.
 */
@Service
public class IntentoService {

    private static final int PREGUNTAS_POR_INTENTO = 10;
    private static final double UMBRAL_APROBACION = 60.0; // porcentaje

    @Autowired private IntentoAutoevaluacionRepository intentoRepository;
    @Autowired private RespuestaIntentoRepository respuestaRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;

    /**
     * Inicia un nuevo intento: sortea preguntas aleatoriamente del pool.
     */
    public IntentoAutoevaluacion iniciarIntento(Autoevaluacion autoevaluacion, Usuario usuario) {
        long intentosYaRealizados = intentoRepository
                .countByAutoevaluacionAndUsuario(autoevaluacion, usuario);
        if (intentosYaRealizados >= autoevaluacion.getIntentosPermitidos()) {
            throw new IllegalStateException("Ya agotaste los intentos permitidos para esta autoevaluación.");
        }
        return new IntentoAutoevaluacion(autoevaluacion, usuario);
    }

    /**
     * Sortea hasta PREGUNTAS_POR_INTENTO preguntas de todos los pools de la autoevaluación.
     */
    public List<Pregunta> sortearPreguntas(Autoevaluacion autoevaluacion) {
        List<Pregunta> todas = autoevaluacion.getPools().stream()
                .flatMap(pool -> preguntaRepository.findByPoolAndBajaFalse(pool).stream())
                .filter(p -> !p.getBaja())
                .collect(Collectors.toList());

        Collections.shuffle(todas);
        return todas.stream()
                .limit(PREGUNTAS_POR_INTENTO)
                .collect(Collectors.toList());
    }

    /**
     * Corrige el intento, calcula la nota y lo persiste.
     * @param intento    El intento a corregir (sin persistir aún)
     * @param respuestas Mapa preguntaId → opcionRespuestaId elegida por el alumno
     */
    @Transactional
    public IntentoAutoevaluacion corregirYGuardar(
            IntentoAutoevaluacion intento,
            Map<Integer, Integer> respuestas) {

        intento.setFecha(LocalDateTime.now());
        IntentoAutoevaluacion guardado = intentoRepository.save(intento);

        int correctas = 0;
        int total = respuestas.size();

        for (Map.Entry<Integer, Integer> entry : respuestas.entrySet()) {
            Integer opcionId = entry.getValue();
            OpcionRespuesta opcion = opcionRepository.findById(opcionId)
                    .orElseThrow(() -> new IllegalArgumentException("Opción no encontrada: " + opcionId));

            RespuestaIntento ri = new RespuestaIntento(guardado, opcion);
            respuestaRepository.save(ri);

            if (Boolean.TRUE.equals(opcion.getEsCorrecta())) {
                correctas++;
            }
        }

        double nota = total > 0 ? (correctas * 100.0) / total : 0.0;
        guardado.setNota(nota);
        return intentoRepository.save(guardado);
    }

    public boolean estaAprobado(IntentoAutoevaluacion intento) {
        return intento.getNota() != null && intento.getNota() >= UMBRAL_APROBACION;
    }

    public List<IntentoAutoevaluacion> historialPorAlumno(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionAndUsuarioOrderByFechaDesc(ae, usuario);
    }

    public boolean alumnoAproboAutoevaluacion(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionAndUsuarioOrderByFechaDesc(ae, usuario)
                .stream().anyMatch(this::estaAprobado);
    }
}
