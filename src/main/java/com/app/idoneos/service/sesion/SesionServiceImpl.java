package com.app.idoneos.service.sesion;

import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Sesion;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.SesionRepository;
import com.app.idoneos.service.parametro.ParametroService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de SesionService. */
@Service
public class SesionServiceImpl implements SesionService {

    /** Vigencia de una sesión común desde su último latido o actividad. Al cerrar el navegador los latidos se cortan y la sesión vence sola. */
    private static final Duration VIGENCIA_COMUN = Duration.ofMinutes(10);

    /** Vigencia de una sesión recordada desde que se inicia. */
    private static final Duration VIGENCIA_RECORDADA = Duration.ofDays(30);

    /** Tiempo mínimo entre una renovación y la siguiente, para no escribir en la base en cada pedido. Es menor que el intervalo del latido. */
    private static final Duration INTERVALO_DE_RENOVACION = Duration.ofSeconds(30);

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private ParametroService parametroService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la sesión por su identificador. */
    @Override
    public Optional<Sesion> buscarPorId(Integer id) {
        return sesionRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Sesion> obtenerTodo() {
        return sesionRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return sesionRepository.existsById(id);
    }

    /** Busca una sesión vigente por su token. */
    @Override
    public Optional<Sesion> buscarVigentePorToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return sesionRepository.findByToken(token).filter(sesion -> sesion.getFechaFin().isAfter(LocalDateTime.now()));
    }

    /** Busca las sesiones vigentes de un usuario, de la más reciente a la más antigua. */
    @Override
    public List<Sesion> buscarVigentes(Usuario usuario) {
        return sesionRepository.findByUsuarioAndFechaFinAfterOrderByFechaInicioDesc(usuario, LocalDateTime.now());
    }

    /** Busca las sesiones vigentes de todos los usuarios, de la más reciente a la más antigua. */
    @Override
    public List<Sesion> buscarTodasVigentes() {
        return sesionRepository.findByFechaFinAfterOrderByFechaInicioDesc(LocalDateTime.now());
    }

    /** Obtiene la cantidad de sesiones simultáneas que puede tener una cuenta. */
    @Override
    public int obtenerLimite() {
        return Math.max(1, parametroService.obtenerEntero(Parametro.LIMITE_SESIONES_CONCURRENTES,
                Parametro.LIMITE_SESIONES_CONCURRENTES_POR_DEFECTO));
    }

    /** Indica si una sesión es recordada. */
    @Override
    public boolean esRecordada(Sesion sesion) {
        return sesion.getToken() != null && sesion.getToken().startsWith(PREFIJO_RECORDADA);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la sesión en la base de datos. */
    @Override
    public Sesion guardar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    /** Registra una sesión nueva del usuario, si no alcanzó el límite de sesiones simultáneas. */
    @Override
    @Transactional
    public Sesion iniciar(Usuario usuario, boolean recordar, String ip, String dispositivo) {
        LocalDateTime ahora = LocalDateTime.now();
        int limite = obtenerLimite();
        if (sesionRepository.countByUsuarioAndFechaFinAfter(usuario, ahora) >= limite) {
            throw new IllegalStateException("Error! Alcanzaste el límite de " + limite + (limite == 1 ? " sesión activa" : " sesiones activas")
                    + " en esta cuenta. Cerrá una desde otro dispositivo o esperá a que venza.");
        }
        String token = (recordar ? PREFIJO_RECORDADA : PREFIJO_COMUN)
                + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        Sesion sesion = new Sesion(usuario, token, ahora, ahora.plus(recordar ? VIGENCIA_RECORDADA : VIGENCIA_COMUN), ip, dispositivo);
        return sesionRepository.save(sesion);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Sesion modificar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    /** Extiende la vigencia de una sesión común por su actividad. Las sesiones recordadas no se renuevan. */
    @Override
    @Transactional
    public void renovar(Sesion sesion) {
        if (esRecordada(sesion)) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime nuevoFin = ahora.plus(VIGENCIA_COMUN);
        if (sesion.getFechaFin().isBefore(nuevoFin.minus(INTERVALO_DE_RENOVACION))) {
            sesion.setFechaFin(nuevoFin);
            sesionRepository.save(sesion);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Sesion entidad) {
        sesionRepository.delete(entidad);
    }

    /** Cierra de forma forzada una sesión distinta de la actual. */
    @Override
    @Transactional
    public void cerrar(int idSesion, int idActor, boolean esAdministrador, String tokenActual) {
        Sesion sesion = sesionRepository.findById(idSesion)
                .orElseThrow(() -> new IllegalArgumentException("Error! La sesión ya no existe."));
        if (!esAdministrador && sesion.getUsuario().getIdUsuario() != idActor) {
            throw new IllegalArgumentException("Error! No podés cerrar la sesión de otro usuario.");
        }
        if (sesion.getToken().equals(tokenActual)) {
            throw new IllegalArgumentException("Error! Esa es la sesión que estás usando. Para salir, usá «Cerrar sesión» del menú.");
        }
        sesionRepository.delete(sesion);
    }

    /** Cierra la sesión que tiene el token indicado, si existe. */
    @Override
    @Transactional
    public void cerrarPorToken(String token) {
        if (token != null && !token.isBlank()) {
            sesionRepository.findByToken(token).ifPresent(sesionRepository::delete);
        }
    }

    /** Cierra todas las sesiones de un usuario. */
    @Override
    @Transactional
    public void cerrarTodas(Usuario usuario) {
        sesionRepository.deleteByUsuario(usuario);
    }

    /** Borra las sesiones vencidas, una vez por hora. */
    @Override
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 120_000)
    @Transactional
    public void eliminarVencidas() {
        sesionRepository.deleteByFechaFinBefore(LocalDateTime.now());
    }
}
