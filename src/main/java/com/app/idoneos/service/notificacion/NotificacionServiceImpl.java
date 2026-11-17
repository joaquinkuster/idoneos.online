package com.app.idoneos.service.notificacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.EstadoEnvio;
import com.app.idoneos.model.Notificacion;
import com.app.idoneos.model.TipoNotificacion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.EstadoEnvioRepository;
import com.app.idoneos.repository.NotificacionRepository;
import com.app.idoneos.repository.TipoNotificacionRepository;
import com.app.idoneos.service.correo.CorreoService;

/** Implementación de NotificacionService. */
@Service
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    private static final int LONGITUD_ASUNTO = 150;

    private static final int LONGITUD_MENSAJE = 500;

    private static final int LONGITUD_ERROR = 500;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private TipoNotificacionRepository tipoNotificacionRepository;

    @Autowired
    private EstadoEnvioRepository estadoEnvioRepository;

    @Autowired
    private CorreoService correoService;

    @Autowired
    private com.app.idoneos.repository.RolUsuarioRepository rolUsuarioRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca las notificaciones más recientes de un usuario, para el panel de la campana. */
    @Override
    public List<Notificacion> buscarRecientes(Usuario usuario) {
        return notificacionRepository.findTop10ByUsuarioOrderByFechaCreacionDescIdNotificacionDesc(usuario);
    }

    /** Busca todas las notificaciones de un usuario. */
    @Override
    public List<Notificacion> buscarTodas(Usuario usuario) {
        return notificacionRepository.findByUsuarioOrderByFechaCreacionDescIdNotificacionDesc(usuario);
    }

    /** Cuenta las notificaciones que un usuario todavía no leyó. */
    @Override
    public long contarNoLeidas(Usuario usuario) {
        return notificacionRepository.countByUsuarioAndFechaLecturaIsNull(usuario);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Crea una notificación para un usuario. Aparece en su bandeja y, si se pide, también se le envía por correo. */
    @Override
    @Transactional
    public Notificacion notificar(Usuario usuario, String tipo, String asunto, String mensaje, String url, Cohorte cohorte,
            boolean porCorreo) {
        TipoNotificacion tipoNotificacion = tipoNotificacionRepository.findByNombre(tipo).orElse(null);
        if (tipoNotificacion == null) {
            // Una notificación que no se puede crear no debe impedir la operación que la originó (un pago, una respuesta)
            log.error("No está definido el tipo de notificación {}: el aviso para {} no se creó", tipo, usuario.getCorreo());
            return null;
        }
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipoNotificacion(tipoNotificacion);
        notificacion.setCohorte(cohorte);
        notificacion.setAsunto(recortar(asunto, LONGITUD_ASUNTO));
        notificacion.setMensaje(recortar(mensaje, LONGITUD_MENSAJE));
        notificacion.setUrl(url);
        if (porCorreo) {
            notificacion.setEstadoEnvio(estadoEnvioRepository.findByNombre(EstadoEnvio.PENDIENTE).orElse(null));
        }
        notificacion = notificacionRepository.save(notificacion);
        if (porCorreo) {
            enviarCuandoSeGuarde();
        }
        return notificacion;
    }

    /** Crea una notificación para varios usuarios a la vez. */
    @Override
    @Transactional
    public void notificarATodos(List<Usuario> usuarios, String tipo, String asunto, String mensaje, String url, Cohorte cohorte,
            boolean porCorreo) {
        for (Usuario usuario : usuarios) {
            notificar(usuario, tipo, asunto, mensaje, url, cohorte, porCorreo);
        }
    }

    /** Crea una notificación para todos los administradores activos. */
    @Override
    @Transactional
    public void notificarAAdministradores(String tipo, String asunto, String mensaje, String url, Cohorte cohorte, boolean porCorreo) {
        notificarATodos(rolUsuarioRepository.buscarUsuariosActivosConRol("Administrador"), tipo, asunto, mensaje, url, cohorte, porCorreo);
    }

    /** Indica si el usuario ya recibió una notificación de ese tipo con ese mismo mensaje. */
    @Override
    public boolean yaNotificado(Usuario usuario, String tipo, String mensaje) {
        return notificacionRepository.existsByUsuarioAndTipoNotificacionNombreAndMensaje(usuario, tipo, recortar(mensaje, LONGITUD_MENSAJE));
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Marca una notificación como leída y devuelve la dirección a la que lleva. */
    @Override
    @Transactional
    public String abrir(int idNotificacion, Usuario usuario) {
        Notificacion notificacion = notificacionRepository.findById(idNotificacion)
                .filter(n -> n.getUsuario().getIdUsuario() == usuario.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Error! La notificación no existe."));
        notificacion.marcarLeida();
        notificacion.setUltimaModificacion(LocalDateTime.now());
        notificacionRepository.save(notificacion);
        return notificacion.getUrl();
    }

    /** Marca como leídas todas las notificaciones de un usuario. */
    @Override
    @Transactional
    public void marcarTodasLeidas(Usuario usuario) {
        List<Notificacion> sinLeer = notificacionRepository.findByUsuarioAndFechaLecturaIsNull(usuario);
        sinLeer.forEach(notificacion -> {
            notificacion.marcarLeida();
            notificacion.setUltimaModificacion(LocalDateTime.now());
        });
        notificacionRepository.saveAll(sinLeer);
    }

    /** Envía por correo las notificaciones pendientes y reintenta las que fallaron, cada minuto. */
    @Override
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public synchronized void enviarPendientes() {
        try {
            List<Notificacion> porEnviar = new ArrayList<>(
                    notificacionRepository.findByEstadoEnvioNombreAndIntentosLessThan(EstadoEnvio.PENDIENTE, MAXIMO_INTENTOS));
            porEnviar.addAll(notificacionRepository.findByEstadoEnvioNombreAndIntentosLessThan(EstadoEnvio.ERROR, MAXIMO_INTENTOS));
            if (porEnviar.isEmpty()) {
                return;
            }
            EstadoEnvio enviado = estadoEnvioRepository.findByNombre(EstadoEnvio.ENVIADO).orElse(null);
            EstadoEnvio error = estadoEnvioRepository.findByNombre(EstadoEnvio.ERROR).orElse(null);
            for (Notificacion notificacion : porEnviar) {
                enviar(notificacion, enviado, error);
            }
        } catch (RuntimeException e) {
            log.warn("No se pudieron enviar las notificaciones pendientes", e);
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Envía el correo de una notificación y registra cómo salió. */
    private void enviar(Notificacion notificacion, EstadoEnvio enviado, EstadoEnvio error) {
        notificacion.setIntentos(notificacion.getIntentos() + 1);
        try {
            correoService.enviarNotificacion(notificacion.getUsuario(), notificacion.getAsunto(), notificacion.getMensaje(), notificacion.getUrl());
            notificacion.setEstadoEnvio(enviado);
            notificacion.setFechaEnvio(LocalDateTime.now());
            notificacion.setDetalleError(null);
        } catch (RuntimeException e) {
            notificacion.setEstadoEnvio(error);
            notificacion.setDetalleError(recortar(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage(), LONGITUD_ERROR));
        }
        notificacion.setUltimaModificacion(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }

    /** Pide el envío de los correos pendientes cuando la transacción en curso ya guardó la notificación. */
    private void enviarCuandoSeGuarde() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    CompletableFuture.runAsync(NotificacionServiceImpl.this::enviarPendientes);
                }
            });
        } else {
            CompletableFuture.runAsync(this::enviarPendientes);
        }
    }

    /** Recorta un texto al largo máximo de su columna. */
    private String recortar(String texto, int maximo) {
        String limpio = texto == null ? "" : texto.trim();
        return limpio.length() > maximo ? limpio.substring(0, maximo - 1) + "…" : limpio;
    }
}
