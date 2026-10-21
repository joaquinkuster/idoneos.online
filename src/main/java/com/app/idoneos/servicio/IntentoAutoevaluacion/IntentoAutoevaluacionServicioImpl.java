package com.app.idoneos.servicio.IntentoAutoevaluacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.modelo.RespuestaIntento;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.repositorio.PreguntaIntentoRepositorio;
import com.app.idoneos.repositorio.RespuestaIntentoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link IntentoAutoevaluacion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link IntentoAutoevaluacionServicio}.
 */
@Service
public class IntentoAutoevaluacionServicioImpl implements IntentoAutoevaluacionServicio, CrudServicio<IntentoAutoevaluacion> {

    private static final Logger REGISTRO = LoggerFactory.getLogger(IntentoAutoevaluacionServicioImpl.class);

    private static final DateTimeFormatter FECHA_Y_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoAutoevaluacionRepositorio;

    @Autowired
    private PreguntaIntentoRepositorio preguntaIntentoRepositorio;

    @Autowired
    private RespuestaIntentoRepositorio respuestaIntentoRepositorio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ProgresoServicio progresoServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el intento de autoevaluación por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<IntentoAutoevaluacion> buscarPorId(Integer id) {
        return intentoAutoevaluacionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<IntentoAutoevaluacion> obtenerTodo() {
        return intentoAutoevaluacionRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return intentoAutoevaluacionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Inscripcion indicado
     */
    @Override
    public List<IntentoAutoevaluacion> buscarPorInscripcion(Inscripcion inscripcion) {
        return intentoAutoevaluacionRepositorio.findByInscripcionAndBajaFalse(inscripcion);
    }

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Autoevaluacion indicado
     */
    @Override
    public List<IntentoAutoevaluacion> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion) {
        return intentoAutoevaluacionRepositorio.findByAutoevaluacionAndBajaFalse(autoevaluacion);
    }

    /**
     * Busca los intentos vigentes de una inscripción sobre una autoevaluación, del más antiguo al más reciente.
     */
    @Override
    public List<IntentoAutoevaluacion> buscarPorInscripcionYAutoevaluacion(Inscripcion inscripcion,
            Autoevaluacion autoevaluacion) {
        return intentoAutoevaluacionRepositorio
                .findByInscripcionAndAutoevaluacionAndBajaFalseOrderByIdIntentoAutoevaluacion(inscripcion, autoevaluacion);
    }

    /**
     * Busca un intento del alumno. Si se le agotó el tiempo, lo cierra y lo corrige antes de devolverlo.
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion buscarPropio(Integer idIntento, Alumno alumno) {
        IntentoAutoevaluacion intento = intentoAutoevaluacionRepositorio.findById(idIntento == null ? -1 : idIntento)
                .filter(i -> !i.esInactivo()
                        && i.getInscripcion().getAlumno().getIdAlumno() == alumno.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! El intento no existe."));
        cerrarSiVencido(intento);
        return intento;
    }

    /**
     * Busca el intento que mantiene al alumno dentro del cuestionario.
     */
    @Override
    @Transactional
    public Optional<IntentoAutoevaluacion> buscarEnCursoSinSalida(Alumno alumno) {
        for (IntentoAutoevaluacion intento : intentoAutoevaluacionRepositorio.buscarEnCursoDelAlumno(alumno)) {
            if (cerrarSiVencido(intento)) {
                continue;
            }
            if (!intento.getAutoevaluacion().getPermiteSalir()) {
                return Optional.of(intento);
            }
        }
        return Optional.empty();
    }

    /**
     * Indica por qué el alumno no puede iniciar un intento, o vacío si puede o ya tiene uno en curso.
     */
    @Override
    public Optional<String> motivoNoDisponible(Alumno alumno, Autoevaluacion autoevaluacion) {
        if (!autoevaluacion.esVisible()) {
            return Optional.of("La autoevaluación no se encuentra disponible.");
        }
        Optional<Inscripcion> inscripcion = inscripcionServicio.buscarConAccesoAUnidad(alumno, autoevaluacion.getUnidad());
        if (inscripcion.isEmpty()) {
            return Optional.of("Todavía no podés acceder a esta unidad: completá la anterior o revisá el estado de tu inscripción.");
        }
        List<IntentoAutoevaluacion> intentos = buscarPorInscripcionYAutoevaluacion(inscripcion.get(), autoevaluacion);
        if (intentos.stream().anyMatch(IntentoAutoevaluacion::estaEnCurso)) {
            return Optional.empty(); // puede continuarlo
        }
        return motivoNoSePuedeIniciar(autoevaluacion, intentos.size(), LocalDateTime.now());
    }

    /**
     * Lee la tolerancia de entrega (parámetro del sistema).
     */
    @Override
    public int obtenerGraciaEnSegundos() {
        return parametroServicio.obtenerEntero(Parametro.GRACIA_ENTREGA_AUTOEVALUACION,
                Parametro.GRACIA_ENTREGA_AUTOEVALUACION_POR_DEFECTO);
    }

    /**
     * CU-68: Busca los intentos vigentes del curso.
     */
    @Override
    public List<IntentoAutoevaluacion> buscarDelCurso(Integer idCurso, Docente soloDeDocente) {
        if (idCurso == null) {
            throw new IllegalArgumentException("Error! Debe seleccionar un curso.");
        }
        List<IntentoAutoevaluacion> intentos = intentoAutoevaluacionRepositorio.buscarVigentesDelCurso(idCurso);
        if (soloDeDocente != null && !intentos.isEmpty()) {
            validarDocenteDelCurso(intentos.get(0), soloDeDocente);
        }
        intentos.forEach(this::cerrarSiVencido); // los que se quedaron sin tiempo se muestran ya corregidos
        return intentos;
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el intento de autoevaluación en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public IntentoAutoevaluacion guardar(IntentoAutoevaluacion entidad) {
        return intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-70: Inicia un intento (o devuelve el que el alumno tiene en curso).
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion iniciarIntento(Alumno alumno, Integer idAutoevaluacion) {
        Autoevaluacion autoevaluacion = autoevaluacionRepositorio.findById(idAutoevaluacion == null ? -1 : idAutoevaluacion)
                .filter(Autoevaluacion::esVisible)
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra disponible."));
        Unidad unidad = autoevaluacion.getUnidad();
        Inscripcion inscripcion = inscripcionServicio.buscarConAccesoAUnidad(alumno, unidad).orElseThrow(
                () -> new IllegalArgumentException("Error! Todavía no podés acceder a esta unidad: completá la anterior "
                        + "o revisá el estado de tu inscripción."));
        // Serializa los pedidos simultáneos del mismo alumno: dos clics no registran dos intentos
        inscripcionRepositorio.bloquearPorId(inscripcion.getIdInscripcion());

        List<IntentoAutoevaluacion> intentos = buscarPorInscripcionYAutoevaluacion(inscripcion, autoevaluacion);
        LocalDateTime ahora = LocalDateTime.now();
        for (IntentoAutoevaluacion intento : intentos) {
            if (intento.estaEnCurso() && !cerrarSiVencido(intento)) {
                return intento; // continúa el que tiene en curso, sin sortear de nuevo ni consumir otro intento
            }
        }
        long usados = buscarPorInscripcionYAutoevaluacion(inscripcion, autoevaluacion).size();
        motivoNoSePuedeIniciar(autoevaluacion, usados, ahora).ifPresent(motivo -> {
            throw new IllegalArgumentException("Error! " + motivo);
        });

        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
        intento.setFechaCreacion(ahora);
        intento = intentoAutoevaluacionRepositorio.save(intento);
        List<Pregunta> sorteo = new ArrayList<>(autoevaluacion.getPreguntasDisponibles());
        Collections.shuffle(sorteo);
        int orden = 1;
        for (Pregunta pregunta : sorteo.subList(0, autoevaluacion.getCantidadPreguntas())) {
            PreguntaIntento preguntaIntento = preguntaIntentoRepositorio.save(new PreguntaIntento(intento, pregunta, orden++));
            intento.getPreguntasIntento().add(preguntaIntento);
        }
        autoevaluacion.getIntentosAutoevaluacion().add(intento);
        inscripcion.getIntentosAutoevaluacion().add(intento);
        return intento;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public IntentoAutoevaluacion modificar(IntentoAutoevaluacion entidad) {
        return intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-70 (paso 6): Guarda la elección del alumno para una pregunta.
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion responderPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento,
            List<Integer> idsOpciones) {
        IntentoAutoevaluacion intento = intentoEnCurso(idIntento, alumno);
        PreguntaIntento preguntaIntento = preguntaDelIntento(intento, idPreguntaIntento);
        validarQueEsLaPreguntaActual(intento, preguntaIntento);

        Pregunta pregunta = preguntaIntento.getPregunta();
        Set<Integer> elegidas = new LinkedHashSet<>(idsOpciones == null ? List.<Integer>of() : idsOpciones);
        if (!pregunta.getEsOpcionMultiple() && elegidas.size() > 1) {
            throw new IllegalArgumentException("Error! Esta pregunta admite una sola opción.");
        }
        List<OpcionRespuesta> opciones = new ArrayList<>();
        for (Integer idOpcion : elegidas) {
            opciones.add(pregunta.getOpcionesActivas().stream().filter(o -> idOpcion != null && o.getIdOpcionRespuesta() == idOpcion)
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Error! Una de las opciones no pertenece a la pregunta.")));
        }
        for (RespuestaIntento anterior : new ArrayList<>(preguntaIntento.getRespuestasIntento())) {
            preguntaIntento.getRespuestasIntento().remove(anterior);
            respuestaIntentoRepositorio.delete(anterior);
        }
        respuestaIntentoRepositorio.flush();
        for (OpcionRespuesta opcion : opciones) {
            preguntaIntento.getRespuestasIntento().add(respuestaIntentoRepositorio.save(new RespuestaIntento(preguntaIntento, opcion)));
        }
        intento.setUltimaModificacion(LocalDateTime.now());
        return intentoAutoevaluacionRepositorio.save(intento);
    }

    /**
     * Marca o desmarca una pregunta para revisarla (solo con navegación libre).
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion marcarPregunta(Integer idIntento, Alumno alumno, Integer idPreguntaIntento, boolean marcada) {
        IntentoAutoevaluacion intento = intentoEnCurso(idIntento, alumno);
        if (!intento.getAutoevaluacion().getNavegacionLibre()) {
            throw new IllegalArgumentException("Error! Este cuestionario no permite marcar preguntas para revisar.");
        }
        PreguntaIntento preguntaIntento = preguntaDelIntento(intento, idPreguntaIntento);
        preguntaIntento.setMarcada(marcada);
        preguntaIntentoRepositorio.save(preguntaIntento);
        return intento;
    }

    /**
     * Pasa a la pregunta siguiente en un cuestionario secuencial.
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion avanzarPregunta(Integer idIntento, Alumno alumno) {
        IntentoAutoevaluacion intento = intentoEnCurso(idIntento, alumno);
        if (intento.getAutoevaluacion().getNavegacionLibre()) {
            throw new IllegalArgumentException("Error! Este cuestionario permite desplazarse libremente entre las preguntas.");
        }
        PreguntaIntento actual = intento.getPreguntaActualSecuencial();
        if (actual != null) {
            actual.setPasada(true);
            preguntaIntentoRepositorio.save(actual);
        }
        return intento;
    }

    /**
     * CU-70 (pasos 8 a 12): Entrega y corrige el intento.
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion entregarIntento(Integer idIntento, Alumno alumno) {
        IntentoAutoevaluacion intento = buscarPropio(idIntento, alumno); // si se agotó el tiempo, ya queda cerrado
        if (intento.estaEnCurso()) {
            finalizar(intento);
        }
        return intento;
    }

    /**
     * Cierra y corrige un intento al que se le agotó el tiempo.
     */
    @Override
    @Transactional
    public boolean cerrarSiVencido(IntentoAutoevaluacion intento) {
        if (!intento.estaEnCurso() || !intento.haVencido(LocalDateTime.now(), obtenerGraciaEnSegundos())) {
            return false;
        }
        finalizar(intento);
        return true;
    }

    /**
     * Tarea periódica: cierra los intentos en curso a los que se les agotó el tiempo.
     */
    @Override
    @Transactional
    public int cerrarIntentosVencidos() {
        int cerrados = 0;
        for (IntentoAutoevaluacion intento : intentoAutoevaluacionRepositorio.findByBajaFalseAndFechaEntregaIsNull()) {
            if (cerrarSiVencido(intento)) {
                cerrados++;
            }
        }
        return cerrados;
    }

    /** Cada minuto cierra los intentos vencidos, aunque el alumno no vuelva a entrar. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void cerrarIntentosVencidosPeriodicamente() {
        try {
            int cerrados = cerrarIntentosVencidos();
            if (cerrados > 0) {
                REGISTRO.info("Intentos de autoevaluación cerrados por tiempo agotado: {}", cerrados);
            }
        } catch (RuntimeException e) {
            REGISTRO.warn("No se pudieron cerrar los intentos de autoevaluación vencidos", e);
        }
    }

    /**
     * CU-71: Aprueba manualmente un intento entregado que no alcanzó la nota mínima.
     */
    @Override
    @Transactional
    public IntentoAutoevaluacion aprobarPorDocente(Integer idIntento, Docente docente) {
        IntentoAutoevaluacion intento = intentoVigente(idIntento);
        validarDocenteDelCurso(intento, docente);
        cerrarSiVencido(intento);
        if (intento.estaEnCurso()) {
            throw new IllegalArgumentException("Error! El intento todavía está en curso: esperá a que el alumno lo entregue.");
        }
        if (Boolean.TRUE.equals(intento.getAprobado())) {
            throw new IllegalArgumentException("Error! El intento ya está aprobado.");
        }
        intento.setAprobado(true);
        intento.setUltimaModificacion(LocalDateTime.now());
        intentoAutoevaluacionRepositorio.save(intento);
        progresoServicio.actualizarProgreso(intento.getInscripcion(), intento.getAutoevaluacion().getUnidad());
        return intento;
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(IntentoAutoevaluacion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-72: Da de baja un intento y, si el alumno deja de aprobar la unidad, la revierte y anula su certificado.
     */
    @Override
    @Transactional
    public boolean darDeBajaIntento(Integer idIntento) {
        IntentoAutoevaluacion intento = intentoVigente(idIntento);
        Inscripcion inscripcion = intento.getInscripcion();
        Unidad unidad = intento.getAutoevaluacion().getUnidad();
        intento.marcarInactivo();
        intento.setUltimaModificacion(LocalDateTime.now());
        intentoAutoevaluacionRepositorio.save(intento);
        if (inscripcion.estaUnidadCompletada(unidad) && !unidad.estaAprobadaPor(inscripcion)) {
            return progresoServicio.revertirProgreso(inscripcion, unidad);
        }
        return false;
    }

    // ---------------------------------------------------------------- Auxiliares

    /**
     * Corrige el intento, registra su fecha de entrega y, si lo aprobó, verifica el progreso de la unidad (y del curso).
     */
    private void finalizar(IntentoAutoevaluacion intento) {
        intento.corregir();
        intento.setFechaEntrega(LocalDateTime.now());
        intentoAutoevaluacionRepositorio.save(intento);
        if (intento.getAprobado()) {
            progresoServicio.actualizarProgreso(intento.getInscripcion(), intento.getAutoevaluacion().getUnidad());
        }
    }

    /**
     * Busca un intento del alumno que siga en curso y dentro del tiempo (más la tolerancia).
     */
    private IntentoAutoevaluacion intentoEnCurso(Integer idIntento, Alumno alumno) {
        IntentoAutoevaluacion intento = buscarPropio(idIntento, alumno);
        if (!intento.estaEnCurso()) {
            throw new IllegalArgumentException(intento.estaFinalizado()
                    ? "Error! Se agotó el tiempo o el intento ya fue entregado: tus respuestas quedaron corregidas."
                    : "Error! El intento no está en curso.");
        }
        return intento;
    }

    private PreguntaIntento preguntaDelIntento(IntentoAutoevaluacion intento, Integer idPreguntaIntento) {
        return intento.getPreguntasIntento().stream()
                .filter(p -> idPreguntaIntento != null && p.getIdPreguntaIntento() == idPreguntaIntento).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! La pregunta no pertenece al intento."));
    }

    /**
     * En un cuestionario secuencial solo se responde la pregunta actual.
     */
    private void validarQueEsLaPreguntaActual(IntentoAutoevaluacion intento, PreguntaIntento preguntaIntento) {
        if (intento.getAutoevaluacion().getNavegacionLibre()) {
            return;
        }
        PreguntaIntento actual = intento.getPreguntaActualSecuencial();
        if (actual == null || actual.getIdPreguntaIntento() != preguntaIntento.getIdPreguntaIntento()) {
            throw new IllegalArgumentException("Error! En este cuestionario solo podés responder la pregunta actual.");
        }
    }

    /**
     * Valida el período habilitado, el límite de intentos y las preguntas disponibles.
     *
     * @return el motivo por el que no se puede iniciar, o vacío si se puede
     */
    private Optional<String> motivoNoSePuedeIniciar(Autoevaluacion autoevaluacion, long intentosUsados, LocalDateTime ahora) {
        if (ahora.isBefore(autoevaluacion.getFechaApertura())) {
            return Optional.of("La autoevaluación se abre el " + autoevaluacion.getFechaApertura().format(FECHA_Y_HORA) + ".");
        }
        if (autoevaluacion.getFechaCierre() != null && ahora.isAfter(autoevaluacion.getFechaCierre())) {
            return Optional.of("La autoevaluación cerró el " + autoevaluacion.getFechaCierre().format(FECHA_Y_HORA) + ".");
        }
        Integer permitidos = autoevaluacion.getIntentosPermitidos();
        if (permitidos != null && intentosUsados >= permitidos) {
            return Optional.of("Ya usaste " + MensajeErrorUtilidad.cantidad(permitidos, "intento permitido", "intentos permitidos") + ".");
        }
        if (!autoevaluacion.tienePreguntasSuficientes()) {
            return Optional.of("La autoevaluación todavía no tiene preguntas suficientes: avisale a tu docente.");
        }
        return Optional.empty();
    }
    private IntentoAutoevaluacion intentoVigente(Integer idIntento) {
        return buscarPorId(idIntento == null ? -1 : idIntento)
                .orElseThrow(() -> new IllegalArgumentException("Error! El intento no se encuentra disponible."));
    }

    private void validarDocenteDelCurso(IntentoAutoevaluacion intento, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        if (!intento.getAutoevaluacion().getUnidad().getCurso().tieneDocente(docente)) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
    }
}
