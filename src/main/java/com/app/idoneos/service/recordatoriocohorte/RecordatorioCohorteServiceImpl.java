package com.app.idoneos.service.recordatoriocohorte;

import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.EstadoEnvio;
import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.RecordatorioCohorte;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.repository.CohorteRepository;
import com.app.idoneos.repository.EstadoEnvioRepository;
import com.app.idoneos.repository.NotificacionRepository;
import com.app.idoneos.repository.RecordatorioCohorteRepository;
import com.app.idoneos.repository.TipoNotificacionRepository;
import com.app.idoneos.service.parametro.ParametroService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de RecordatorioCohorteService. */
@Service
public class RecordatorioCohorteServiceImpl implements RecordatorioCohorteService {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private RecordatorioCohorteRepository recordatorioRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private TipoNotificacionRepository tipoNotificacionRepository;

    @Autowired
    private EstadoEnvioRepository estadoEnvioRepository;

    @Autowired
    private ParametroService parametroService;

    @Override
    @Transactional
    public void recordar(Alumno alumno, Integer idCohorte) {
        if (alumno == null) {
            throw new IllegalArgumentException("Error! Solo los alumnos pueden pedir un recordatorio.");
        }
        Cohorte cohorte = cohorteRepository.findById(idCohorte == null ? -1 : idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        if (!cohorte.estaProxima()) {
            throw new IllegalArgumentException("Error! Solo se puede pedir un recordatorio de una cohorte próxima.");
        }
        RecordatorioCohorte recordatorio = recordatorioRepository.findByAlumnoAndCohorte(alumno, cohorte)
                .orElseGet(() -> new RecordatorioCohorte(alumno, cohorte));
        recordatorio.reactivar();
        recordatorio.setUltimaModificacion(LocalDateTime.now());
        recordatorioRepository.save(recordatorio);
    }

    @Override
    @Transactional
    public void quitar(Alumno alumno, Integer idCohorte) {
        Cohorte cohorte = cohorteRepository.findById(idCohorte == null ? -1 : idCohorte)
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        RecordatorioCohorte recordatorio = alumno == null ? null
                : recordatorioRepository.findByAlumnoAndCohorte(alumno, cohorte).filter(r -> !r.esInactivo()).orElse(null);
        if (recordatorio == null) {
            throw new IllegalArgumentException("Error! No tenés un recordatorio para esta cohorte.");
        }
        recordatorio.marcarInactivo();
        recordatorio.setUltimaModificacion(LocalDateTime.now());
        recordatorioRepository.save(recordatorio);
    }

    @Override
    public Set<Integer> idsDeCohortesRecordadas(Alumno alumno) {
        Set<Integer> ids = new HashSet<>();
        if (alumno != null) {
            recordatorioRepository.findByAlumnoAndBajaFalse(alumno).forEach(r -> ids.add(r.getCohorte().getIdCohorte()));
        }
        return ids;
    }

    @Override
    @Transactional
    public int crearNotificacionesPendientes() {
        int dias = Math.max(0, parametroService.obtenerEntero(Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION,
                Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION_POR_DEFECTO));
        TipoNotificacion tipo = tipoNotificacionRepository.findByNombre(TipoNotificacion.RECORDATORIO_COHORTE).orElse(null);
        EstadoEnvio pendiente = estadoEnvioRepository.findByNombre(EstadoEnvio.PENDIENTE).orElse(null);
        if (tipo == null || pendiente == null) {
            return 0;
        }
        LocalDateTime ahora = LocalDateTime.now();
        int creadas = 0;
        List<RecordatorioCohorte> vigentes = recordatorioRepository.findByBajaFalse();
        for (RecordatorioCohorte recordatorio : vigentes) {
            Cohorte cohorte = recordatorio.getCohorte();
            // Avisa solo mientras la cohorte siga próxima (si ya abrió la inscripción, el alumno la ve abierta en el catálogo) y una sola vez
            boolean proxima = !cohorte.esInactivo() && cohorte.estaProxima();
            boolean llegoElMomento = !ahora.isBefore(cohorte.getFechaInicioInscripcion().minusDays(dias));
            if (!proxima || !llegoElMomento || notificacionRepository.existsByUsuarioAndCohorteAndTipoNotificacion(
                    recordatorio.getAlumno().getUsuario(), cohorte, tipo)) {
                continue;
            }
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuario(recordatorio.getAlumno().getUsuario());
            notificacion.setTipoNotificacion(tipo);
            notificacion.setCohorte(cohorte);
            notificacion.setEstadoEnvio(pendiente);
            String asunto = "Abre la inscripción de «" + cohorte.getCurso().getNombre() + "» el "
                    + cohorte.getFechaInicioInscripcion().format(FECHA);
            notificacion.setAsunto(asunto.length() > 150 ? asunto.substring(0, 149) + "…" : asunto);
            notificacionRepository.save(notificacion);
            creadas++;
        }
        return creadas;
    }

    /** Revisa cada hora qué recordatorios ya tienen que avisar y crea sus notificaciones. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    @org.springframework.transaction.annotation.Transactional
    public void crearNotificacionesPendientesPeriodicamente() {
        try {
            int creadas = crearNotificacionesPendientes();
            if (creadas > 0) {
                org.slf4j.LoggerFactory.getLogger(RecordatorioCohorteServiceImpl.class)
                        .info("Recordatorios de cohortes: {} notificaciones creadas", creadas);
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(RecordatorioCohorteServiceImpl.class)
                    .warn("No se pudieron crear los recordatorios de cohortes", e);
        }
    }
}
