package com.app.idoneos.service.videoclon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.EstadoGeneracionIA;
import com.app.idoneos.model.IntegranteEquipo;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.model.VideoClon;
import com.app.idoneos.repository.EstadoGeneracionIARepository;
import com.app.idoneos.repository.IntegranteEquipoRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.repository.VideoClonRepository;
import com.app.idoneos.service.heygen.HeygenService;
import com.app.idoneos.service.notificacion.NotificacionService;
import com.app.idoneos.service.parametro.ParametroService;

/** Implementación de VideoClonService. */
@Service
public class VideoClonServiceImpl implements VideoClonService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(VideoClonServiceImpl.class);

    private static final int LONGITUD_TITULO = 150;
    private static final int LONGITUD_GUION = 500;

    @Autowired
    private HeygenService heygenService;

    @Autowired
    private VideoClonRepository videoClonRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private TemaRecursoRepository temaRecursoRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private IntegranteEquipoRepository integranteEquipoRepository;

    @Autowired
    private EstadoGeneracionIARepository estadoGeneracionRepository;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Obtiene los videos con clon activos de un curso, del más reciente al más antiguo. */
    @Override
    @Transactional(readOnly = true)
    public List<TemaRecurso> buscarPorCurso(Curso curso, Docente docente) {
        return temaRecursoRepository.buscarPorFiltros(curso.getIdCurso(), null, false).stream()
                .filter(m -> !m.getRecurso().getBaja() && m.getRecurso().getVideoClon() != null)
                .filter(m -> docente == null || m.getRecurso().getDocente().getIdDocente() == docente.getIdDocente())
                .sorted(Comparator.comparing((TemaRecurso m) -> m.getRecurso().getFechaCreacion()).reversed()).toList();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Pide un video con el clon del docente. */
    @Override
    @Transactional
    public TemaRecurso solicitar(Docente docente, Integer idTema, String titulo, String guion) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        if (docente.getAvatarId() == null || docente.getVoiceId() == null) {
            throw new IllegalArgumentException("Error! Primero creá tu clon desde tu perfil: hace falta tu foto y tu voz.");
        }
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activo."));
        IntegranteEquipo participacion = integranteEquipoRepository.findByCursoAndBajaFalse(tema.getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
        String tituloLimpio = titulo == null ? "" : titulo.trim();
        String guionLimpio = guion == null ? "" : guion.trim();
        if (tituloLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Ingresá el título del video.");
        }
        if (tituloLimpio.length() > LONGITUD_TITULO) {
            throw new IllegalArgumentException("Error! El título puede tener hasta " + LONGITUD_TITULO + " caracteres.");
        }
        if (guionLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Ingresá el guion: lo que dice tu clon en el video.");
        }
        if (guionLimpio.length() > LONGITUD_GUION) {
            throw new IllegalArgumentException("Error! El guion puede tener hasta " + LONGITUD_GUION + " caracteres.");
        }
        verificarTopeSemanal(docente);

        // Primero se pide a HeyGen: si lo rechaza no queda nada registrado
        String idVideo = heygenService.crearVideo(docente.getAvatarId(), docente.getVoiceId(), tituloLimpio, guionLimpio);

        Recurso recurso = recursoRepository.saveAndFlush(new Recurso(docente, tituloLimpio));
        VideoClon video = new VideoClon(recurso, estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.PENDIENTE).orElseThrow(), guionLimpio);
        video.setIdVideoExterno(idVideo);
        recurso.setVideoClon(videoClonRepository.save(video));
        int orden = temaRecursoRepository.findByTemaAndBajaFalse(tema).stream().mapToInt(TemaRecurso::getNumeroOrden).max().orElse(0) + 1;
        TemaRecurso material = new TemaRecurso(tema, recurso, participacion, orden);
        // Queda oculto para los alumnos hasta que el docente lo revise y lo publique
        material.setOculto(true);
        material = temaRecursoRepository.save(material);
        tema.getTemasRecurso().add(material);
        recurso.getTemasRecurso().add(material);
        return material;
    }

    /** Valida que el docente no haya superado, esta semana, la cantidad de videos con clon que puede pedir. */
    private void verificarTopeSemanal(Docente docente) {
        int tope = parametroService.obtenerEntero(Parametro.TOPE_SEMANAL_CLONES, Parametro.TOPE_SEMANAL_CLONES_POR_DEFECTO);
        LocalDateTime desde = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        if (videoClonRepository.contarDelDocenteDesde(docente, desde) >= tope) {
            throw new IllegalArgumentException("Error! Alcanzaste el máximo de " + tope + " videos con clon por semana. Podés pedir más desde el lunes.");
        }
    }

    // ----------------------------------------------------------------------
    // Seguimiento
    // ----------------------------------------------------------------------

    /** Consulta a HeyGen los videos pendientes. Se revisa cada 15 segundos. */
    @Override
    @Scheduled(fixedDelay = 15_000, initialDelay = 60_000)
    @Transactional
    public void actualizarPendientes() {
        List<VideoClon> pendientes;
        try {
            pendientes = videoClonRepository.findByEstadoGeneracionIaNombreAndIdVideoExternoIsNotNull(EstadoGeneracionIA.PENDIENTE);
        } catch (RuntimeException e) {
            return;
        }
        for (VideoClon video : pendientes) {
            try {
                actualizar(video.getIdVideoClon());
            } catch (RuntimeException e) {
                // Un video con problemas no frena a los demás: se reintenta en la próxima vuelta
                REGISTRO.warn("No se pudo actualizar el video con clon {}: {}", video.getIdVideoClon(), e.getMessage());
            }
        }
    }

    private void actualizar(int idVideoClon) {
        VideoClon video = videoClonRepository.findById(idVideoClon).orElse(null);
        if (video == null || video.getIdVideoExterno() == null || !EstadoGeneracionIA.PENDIENTE.equals(video.getEstadoGeneracionIa().getNombre())) {
            return;
        }
        HeygenService.Video estado = heygenService.consultarVideo(video.getIdVideoExterno());
        if (estado.estaListo() && estado.url() != null) {
            guardarArchivo(video, estado);
        } else if (estado.fallo()) {
            video.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.FALLIDO).orElseThrow());
            String motivo = estado.error() == null || estado.error().isBlank() ? "HeyGen no pudo generar el video." : estado.error();
            video.setDetalleError(motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
            videoClonRepository.save(video);
            avisar(video, false, motivo);
        }
    }

    /** Descarga el video terminado, lo guarda en la biblioteca del docente y lo marca como listo. */
    private void guardarArchivo(VideoClon video, HeygenService.Video estado) {
        Recurso recurso = video.getRecurso();
        String relativa = "biblioteca/docente" + recurso.getDocente().getIdDocente() + "/videoclon" + recurso.getIdRecurso() + ".mp4";
        byte[] contenido = heygenService.descargar(estado.url());
        try {
            Path destino = directorioCursos.resolve(relativa);
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el video con clon " + relativa, e);
        }
        video.setRutaArchivo(relativa);
        video.setTamanioBytes((long) contenido.length);
        if (estado.duracionSegundos() != null) {
            video.setDuracionMinutos(Math.max(1, (int) Math.ceil(estado.duracionSegundos() / 60.0)));
        }
        video.setDetalleError(null);
        video.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.LISTO).orElseThrow());
        videoClonRepository.save(video);
        avisar(video, true, null);
    }

    /** Avisa al docente que el video está listo para revisar, o que falló. */
    private void avisar(VideoClon video, boolean listo, String motivo) {
        Recurso recurso = video.getRecurso();
        TemaRecurso material = recurso.getTemasRecurso().stream().findFirst().orElse(null);
        String url = material == null ? "/inicio" : "/videoClon/buscar?cursoId=" + material.getTema().getCurso().getIdCurso();
        String tema = material == null ? "" : " del tema «" + material.getTema().getTitulo() + "»";
        if (listo) {
            notificacionService.notificar(recurso.getDocente().getUsuario(), TipoNotificacion.MATERIAL_PARA_REVISAR, "Un video con clon está listo para revisar",
                    "El video «" + recurso.getTitulo() + "»" + tema + " ya se generó. Revisalo antes de publicarlo.", url, null, false);
        } else {
            notificacionService.notificar(recurso.getDocente().getUsuario(), TipoNotificacion.GENERACION_FALLIDA, "Falló la generación de un video con clon",
                    "No se pudo generar el video «" + recurso.getTitulo() + "»" + tema + ": " + motivo + " Podés intentarlo de nuevo.", url, null, false);
        }
    }
}
