package com.app.idoneos.service.modulo_evaluaciones;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TRAZABILIDAD — Servicio para la rendicion, sorteo y correccion de Intentos de Autoevaluacion.
 *
 * MOD-F-04: Modulo de Evaluaciones y Rendicion
 *   CU-61 — Buscar intento de autoevaluacion.
 *   CU-62 — Ver calificaciones.
 *   CU-63 — Realizar intento: sorteo aleatorio de preguntas, captura de respuestas,
 *            calificacion automatica, actualizacion del progreso y emision de certificado.
 *   CU-64 — Dar de baja intento de autoevaluacion.
 */
@Service
@Transactional
public class IntentoService {

    private static final Logger log = LoggerFactory.getLogger(IntentoService.class);

    /** Cantidad maxima de preguntas por intento si la autoevaluacion no especifica otra. */
    private static final int PREGUNTAS_POR_DEFECTO = 10;

    /** Umbral de aprobacion en porcentaje (100% = todas correctas). */
    private static final double UMBRAL_APROBACION = 100.0;

    @Autowired private IntentoAutoevaluacionRepository intentoRepository;
    @Autowired private RespuestaIntentoRepository respuestaRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;
    @Autowired private CronogramaRepository cronogramaRepository;

    // @Lazy para evitar dependencia circular (ProgresoServiceImpl -> IntentoService via CertificadoService)
    @Autowired @Lazy private ProgresoService progresoService;
    @Autowired @Lazy private CertificadoService certificadoService;

    /**
     * CU-63 — Inicia un nuevo intento de autoevaluacion para el alumno.
     * Valida: autoevaluacion activa, limite de intentos, unidad habilitada.
     */
    public IntentoAutoevaluacion iniciarIntento(Autoevaluacion autoevaluacion, Inscripcion inscripcion) {
        if (autoevaluacion == null || autoevaluacion.getBaja()) {
            throw new ExcepcionValidacion("CU-63 Precondicion: La autoevaluacion seleccionada no esta activa.");
        }

        // Verificar ventana de fecha
        LocalDateTime ahora = LocalDateTime.now();
        if (autoevaluacion.getFechaApertura() != null && ahora.isBefore(autoevaluacion.getFechaApertura())) {
            throw new ExcepcionValidacion("CU-63 Excepcion: La autoevaluacion aun no esta disponible. Apertura: " + autoevaluacion.getFechaApertura());
        }
        if (autoevaluacion.getFechaCierre() != null && ahora.isAfter(autoevaluacion.getFechaCierre())) {
            throw new ExcepcionValidacion("CU-63 Excepcion: La autoevaluacion ya no esta disponible. Cerro: " + autoevaluacion.getFechaCierre());
        }

        // Verificar limite de intentos
        if (autoevaluacion.getIntentosPermitidos() != null && autoevaluacion.getIntentosPermitidos() > 0) {
            long intentosUsados = intentoRepository.countByAutoevaluacion(autoevaluacion);
            if (intentosUsados >= autoevaluacion.getIntentosPermitidos()) {
                throw new ExcepcionValidacion("CU-63 Excepcion paso 4: Ha alcanzado el limite maximo de intentos permitidos ("
                        + autoevaluacion.getIntentosPermitidos() + ") para esta evaluacion.");
            }
        }

        // Verificar que la unidad este habilitada para el alumno
        if (inscripcion != null) {
            Unidad unidad = autoevaluacion.getUnidad();
            if (unidad != null && !progresoService.esUnidadHabilitada(inscripcion, unidad)) {
                throw new ExcepcionValidacion("CU-63 Excepcion: La unidad correspondiente aun no esta habilitada. Debes completar la unidad anterior.");
            }
        }

        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(autoevaluacion);
        if (inscripcion != null) intento.setInscripcion(inscripcion);
        return intento;
    }

    /**
     * Alias de compatibilidad con el codigo existente que llama iniciarIntento(ae, usuario).
     */
    public IntentoAutoevaluacion iniciarIntento(Autoevaluacion autoevaluacion, Usuario usuario) {
        return new IntentoAutoevaluacion(autoevaluacion);
    }

    /**
     * CU-63 paso 3 — Sortea preguntas aleatorias de los pools activos de la autoevaluacion.
     * Respeta cantidadPreguntas si esta definida, sino usa PREGUNTAS_POR_DEFECTO.
     */
    @Transactional(readOnly = true)
    public List<Pregunta> sortearPreguntas(Autoevaluacion autoevaluacion) {
        List<Pregunta> todas = autoevaluacion.getPools().stream()
                .map(pa -> pa.getPool())
                .flatMap(pool -> preguntaRepository.findByPoolAndBajaFalse(pool).stream())
                .filter(p -> !p.getBaja())
                .collect(Collectors.toList());

        if (todas.isEmpty()) {
            throw new ExcepcionValidacion("CU-63 Excepcion paso 5: La autoevaluacion no posee preguntas cargadas en sus pools.");
        }

        Collections.shuffle(todas);
        int limite = (autoevaluacion.getCantidadPreguntas() != null && autoevaluacion.getCantidadPreguntas() > 0)
                ? autoevaluacion.getCantidadPreguntas()
                : PREGUNTAS_POR_DEFECTO;

        return todas.stream()
                .limit(Math.min(limite, todas.size()))
                .collect(Collectors.toList());
    }

    /**
     * CU-63 pasos 8-11 — Corrige el intento, calcula la nota, persiste el resultado
     * y actualiza el progreso del alumno. Si aprueba la ultima unidad, emite el certificado.
     *
     * @param intento   El IntentoAutoevaluacion sin persistir (con autoevaluacion e inscripcion seteados)
     * @param respuestas Map<preguntaId, opcionId> con las respuestas del alumno
     * @return El IntentoAutoevaluacion persistido con nota calculada
     */
    @Transactional
    public IntentoAutoevaluacion corregirYGuardar(IntentoAutoevaluacion intento, Map<Integer, Integer> respuestas) {
        if (respuestas == null || respuestas.isEmpty()) {
            throw new ExcepcionValidacion("CU-63 Excepcion: Debe responder al menos una pregunta de la autoevaluacion.");
        }

        intento.setFechaEntrega(LocalDateTime.now());
        IntentoAutoevaluacion guardado = intentoRepository.save(intento);

        int correctas = 0;
        int total = respuestas.size();

        for (Map.Entry<Integer, Integer> entry : respuestas.entrySet()) {
            Integer opcionId = entry.getValue();
            OpcionRespuesta opcion = opcionRepository.findById(opcionId)
                    .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Opcion de Respuesta", "id", opcionId));

            RespuestaIntento ri = new RespuestaIntento(guardado, opcion);
            respuestaRepository.save(ri);

            if (Boolean.TRUE.equals(opcion.getEsCorrecta())) {
                correctas++;
            }
        }

        float nota = total > 0 ? (float) (correctas * 100.0) / total : 0.0f;
        guardado.setNota(nota);
        guardado = intentoRepository.save(guardado);

        log.info("CU-63: Intento #{} corregido. Nota: {}% ({}/{} correctas)", guardado.getId(), nota, correctas, total);

        // CU-63 paso 10-11: Si el alumno aprueba, actualizar progreso y verificar certificado
        if (estaAprobado(guardado)) {
            Autoevaluacion ae = guardado.getAutoevaluacion();
            Inscripcion inscripcion = guardado.getInscripcion();

            if (ae != null && ae.getUnidad() != null && inscripcion != null) {
                Unidad unidadAprobada = ae.getUnidad();

                // Marcar unidad como completada
                progresoService.marcarCompletada(inscripcion, unidadAprobada);
                log.info("CU-63: Unidad '{}' marcada como completada para inscripcion #{}", unidadAprobada.getTitulo(), inscripcion.getId());

                // Habilitar la siguiente unidad del cronograma
                progresoService.habilitarSiguienteUnidad(inscripcion, unidadAprobada);

                // Verificar si es la ultima unidad del cronograma -> emitir certificado
                Programa programa = inscripcion.getCohorte().getPrograma();
                List<Cronograma> cronograma = cronogramaRepository.findByProgramaOrderByNumeroOrden(programa);
                if (!cronograma.isEmpty()) {
                    Unidad ultimaUnidad = cronograma.get(cronograma.size() - 1).getUnidad();
                    if (ultimaUnidad.getId() == unidadAprobada.getId()) {
                        log.info("CU-63: El alumno aprobo la ultima unidad. Emitiendo certificado para inscripcion #{}", inscripcion.getId());
                        certificadoService.emitirCertificado(inscripcion);
                    }
                }
            }
        }

        return guardado;
    }

    /**
     * CU-63 — Determina si el intento esta aprobado segun el umbral de aprobacion.
     */
    @Transactional(readOnly = true)
    public boolean estaAprobado(IntentoAutoevaluacion intento) {
        return intento != null && intento.getNota() != null && intento.getNota() >= UMBRAL_APROBACION;
    }

    /**
     * CU-62 — Historial de intentos de un alumno para una autoevaluacion.
     */
    @Transactional(readOnly = true)
    public List<IntentoAutoevaluacion> historialPorAlumno(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae);
    }

    /**
     * CU-62 — Verifica si el alumno ya aprobo una autoevaluacion.
     */
    @Transactional(readOnly = true)
    public boolean alumnoAproboAutoevaluacion(Autoevaluacion ae, Usuario usuario) {
        return intentoRepository.findByAutoevaluacionOrderByFechaDesc(ae)
                .stream().anyMatch(this::estaAprobado);
    }
}
