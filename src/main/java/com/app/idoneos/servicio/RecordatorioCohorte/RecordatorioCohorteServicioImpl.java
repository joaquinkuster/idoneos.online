package com.app.idoneos.servicio.RecordatorioCohorte;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoEnvio;
import com.app.idoneos.modelo.Notificacion;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.RecordatorioCohorte;
import com.app.idoneos.modelo.TipoNotificacion;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.repositorio.EstadoEnvioRepositorio;
import com.app.idoneos.repositorio.NotificacionRepositorio;
import com.app.idoneos.repositorio.RecordatorioCohorteRepositorio;
import com.app.idoneos.repositorio.TipoNotificacionRepositorio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de {@link RecordatorioCohorteServicio}.
 */
@Service
public class RecordatorioCohorteServicioImpl implements RecordatorioCohorteServicio {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private RecordatorioCohorteRepositorio recordatorioRepositorio;

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

    @Autowired
    private NotificacionRepositorio notificacionRepositorio;

    @Autowired
    private TipoNotificacionRepositorio tipoNotificacionRepositorio;

    @Autowired
    private EstadoEnvioRepositorio estadoEnvioRepositorio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Override
    @Transactional
    public void recordar(Alumno alumno, Integer idCohorte) {
        if (alumno == null) {
            throw new IllegalArgumentException("Error! Solo los alumnos pueden pedir un recordatorio.");
        }
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte == null ? -1 : idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        if (!"Próxima".equals(cohorte.getEstado())) {
            throw new IllegalArgumentException("Error! Solo se puede pedir un recordatorio de una cohorte próxima.");
        }
        RecordatorioCohorte recordatorio = recordatorioRepositorio.findByAlumnoAndCohorte(alumno, cohorte)
                .orElseGet(() -> new RecordatorioCohorte(alumno, cohorte));
        recordatorio.reactivar();
        recordatorio.setUltimaModificacion(LocalDateTime.now());
        recordatorioRepositorio.save(recordatorio);
    }

    @Override
    @Transactional
    public void quitar(Alumno alumno, Integer idCohorte) {
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte == null ? -1 : idCohorte)
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra disponible."));
        RecordatorioCohorte recordatorio = alumno == null ? null
                : recordatorioRepositorio.findByAlumnoAndCohorte(alumno, cohorte).filter(r -> !r.esInactivo()).orElse(null);
        if (recordatorio == null) {
            throw new IllegalArgumentException("Error! No tenés un recordatorio para esta cohorte.");
        }
        recordatorio.marcarInactivo();
        recordatorio.setUltimaModificacion(LocalDateTime.now());
        recordatorioRepositorio.save(recordatorio);
    }

    @Override
    public Set<Integer> idsDeCohortesRecordadas(Alumno alumno) {
        Set<Integer> ids = new HashSet<>();
        if (alumno != null) {
            recordatorioRepositorio.findByAlumnoAndBajaFalse(alumno).forEach(r -> ids.add(r.getCohorte().getIdCohorte()));
        }
        return ids;
    }

    @Override
    @Transactional
    public int crearNotificacionesPendientes() {
        int dias = Math.max(0, parametroServicio.obtenerEntero(Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION,
                Parametro.COHORTE_RECORDATORIO_DIAS_ANTICIPACION_POR_DEFECTO));
        TipoNotificacion tipo = tipoNotificacionRepositorio.findByNombre(TipoNotificacion.RECORDATORIO_COHORTE).orElse(null);
        EstadoEnvio pendiente = estadoEnvioRepositorio.findByNombre(EstadoEnvio.PENDIENTE).orElse(null);
        if (tipo == null || pendiente == null) {
            return 0;
        }
        LocalDateTime ahora = LocalDateTime.now();
        int creadas = 0;
        List<RecordatorioCohorte> vigentes = recordatorioRepositorio.findByBajaFalse();
        for (RecordatorioCohorte recordatorio : vigentes) {
            Cohorte cohorte = recordatorio.getCohorte();
            // Avisa solo mientras la cohorte siga próxima (si ya abrió la inscripción, el alumno la ve abierta en el catálogo) y una sola vez
            boolean proxima = !cohorte.esInactivo() && "Próxima".equals(cohorte.getEstado());
            boolean llegoElMomento = !ahora.isBefore(cohorte.getFechaInicioInscripcion().minusDays(dias));
            if (!proxima || !llegoElMomento || notificacionRepositorio.existsByUsuarioAndCohorteAndTipoNotificacion(
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
            notificacionRepositorio.save(notificacion);
            creadas++;
        }
        return creadas;
    }

    /** Cada hora revisa qué recordatorios ya tienen que avisar. */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 3_600_000, initialDelay = 60_000)
    @org.springframework.transaction.annotation.Transactional
    public void crearNotificacionesPendientesPeriodicamente() {
        try {
            int creadas = crearNotificacionesPendientes();
            if (creadas > 0) {
                org.slf4j.LoggerFactory.getLogger(RecordatorioCohorteServicioImpl.class)
                        .info("Recordatorios de cohortes: {} notificaciones creadas", creadas);
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(RecordatorioCohorteServicioImpl.class)
                    .warn("No se pudieron crear los recordatorios de cohortes", e);
        }
    }
}
