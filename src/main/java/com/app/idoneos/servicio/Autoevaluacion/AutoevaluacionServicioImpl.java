package com.app.idoneos.servicio.Autoevaluacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.AutoevaluacionPool;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.AutoevaluacionPoolRepositorio;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Autoevaluacion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AutoevaluacionServicio}.
 */
@Service
public class AutoevaluacionServicioImpl implements AutoevaluacionServicio, CrudServicio<Autoevaluacion> {

    /** Largo máximo del nombre de la autoevaluación. */
    private static final int LONGITUD_NOMBRE = 50;

    /** Nota máxima de la escala. */
    private static final int NOTA_MAXIMA = 10;

    /** Datos de una solicitud ya leídos y validados. */
    private record Datos(String nombre, int tiempoLimite, int cantidadPreguntas, int notaMinima, Integer intentosPermitidos,
            LocalDateTime fechaApertura, LocalDateTime fechaCierre, boolean descuentoPorError, boolean navegacionLibre,
            boolean permiteSalir, boolean oculto, List<Pool> pools) {
    }

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private AutoevaluacionPoolRepositorio autoevaluacionPoolRepositorio;

    @Autowired
    private PoolRepositorio poolRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    @Autowired
    private ProgresoServicio progresoServicio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la autoevaluación por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Autoevaluacion> buscarPorId(Integer id) {
        return autoevaluacionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Autoevaluacion> obtenerTodo() {
        return autoevaluacionRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de autoevaluacion asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Autoevaluacion asociados al Unidad indicado
     */
    @Override
    public List<Autoevaluacion> buscarPorUnidad(Unidad unidad) {
        return autoevaluacionRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * CU-64: Busca las autoevaluaciones de un curso (incluidas las dadas de baja) aplicando filtros opcionales. Las dadas
     * de baja van siempre al final.
     */
    @Override
    public List<Autoevaluacion> buscarConFiltros(Integer idCurso, Integer idUnidad, String nombre, Boolean baja,
            Docente soloDeDocente) {
        String criterio = nombre == null ? "" : nombre.trim().toLowerCase();
        return autoevaluacionRepositorio.findAll().stream()
                .filter(a -> idCurso == null || a.getUnidad().getCurso().getIdCurso() == idCurso)
                .filter(a -> idUnidad == null || a.getUnidad().getIdUnidad() == idUnidad)
                .filter(a -> criterio.isEmpty() || a.getNombre().toLowerCase().contains(criterio))
                .filter(a -> baja == null || a.getBaja().equals(baja))
                .filter(a -> soloDeDocente == null || participaEnElCurso(a.getUnidad().getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(Autoevaluacion::getBaja)
                        .thenComparingInt((Autoevaluacion a) -> a.getUnidad().getIdUnidad())
                        .thenComparingInt(Autoevaluacion::getIdAutoevaluacion))
                .toList();
    }

    /**
     * Obtiene las unidades cuyas autoevaluaciones pueden usar un pool: la propia y la última de cada programa que incluye
     * la unidad del pool.
     */
    @Override
    public List<Unidad> unidadesQuePuedenUsar(Pool pool) {
        Unidad propia = pool.getUnidad();
        List<Unidad> unidades = new ArrayList<>(List.of(propia));
        for (Programa programa : propia.getProgramasQueLaIncluyen()) {
            List<UnidadCronograma> cronograma = programa.getCronogramaOrdenado();
            if (cronograma.isEmpty()) {
                continue;
            }
            Unidad ultima = cronograma.get(cronograma.size() - 1).getUnidad();
            if (unidades.stream().noneMatch(u -> u.getIdUnidad() == ultima.getIdUnidad())) {
                unidades.add(ultima);
            }
        }
        return unidades;
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la autoevaluación en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Autoevaluacion guardar(Autoevaluacion entidad) {
        return autoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-65: Crea una autoevaluación en una unidad activa, asociada a uno o más pools.
     */
    @Override
    @Transactional
    public Autoevaluacion registrarAutoevaluacion(Docente docente, Integer idUnidad, SolicitudAutoevaluacion solicitud) {
        Unidad unidad = unidadRepositorio.findById(idUnidad == null ? -1 : idUnidad).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        obtenerParticipacion(unidad.getCurso(), docente);
        Datos datos = leer(unidad, solicitud);

        Autoevaluacion autoevaluacion = new Autoevaluacion(unidad, datos.nombre(), datos.cantidadPreguntas(),
                datos.notaMinima(), datos.fechaApertura());
        aplicarDatosSimples(autoevaluacion, datos);
        autoevaluacion.setNavegacionLibre(datos.navegacionLibre());
        autoevaluacion.setPermiteSalir(datos.permiteSalir());
        autoevaluacion = autoevaluacionRepositorio.save(autoevaluacion);
        asociarPools(autoevaluacion, datos.pools());
        unidad.getAutoevaluaciones().add(autoevaluacion);
        return autoevaluacion;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Autoevaluacion modificar(Autoevaluacion entidad) {
        return autoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-66: Modifica una autoevaluación. Con intentos registrados solo pueden extenderse la fecha de cierre, ocultarse o
     * mostrarse y ampliarse la cantidad de intentos permitidos.
     */
    @Override
    @Transactional
    public Autoevaluacion modificarAutoevaluacion(Integer idAutoevaluacion, Docente docente,
            SolicitudAutoevaluacion solicitud) {
        Autoevaluacion autoevaluacion = autoevaluacionRepositorio.findById(idAutoevaluacion == null ? -1 : idAutoevaluacion)
                .filter(a -> !a.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra activa."));
        Unidad unidad = autoevaluacion.getUnidad();
        obtenerParticipacion(unidad.getCurso(), docente);
        Datos datos = leer(unidad, solicitud);
        boolean conIntentos = autoevaluacion.tieneIntentosActivos();
        if (conIntentos) {
            validarModificacionConIntentos(autoevaluacion, datos);
        }
        boolean seOculta = datos.oculto() && !autoevaluacion.getOculto();
        if (seOculta && quedariaSinAutoevaluacionVisible(unidad, Set.of(autoevaluacion.getIdAutoevaluacion()))) {
            throw new IllegalArgumentException("Error! No se puede ocultar la autoevaluación «" + autoevaluacion.getNombre()
                    + "»: es la única visible de la unidad «" + unidad.getTitulo()
                    + "» y su programa tiene cohortes en curso. Registrá otra autoevaluación antes.");
        }

        autoevaluacion.setIntentosPermitidos(datos.intentosPermitidos());
        autoevaluacion.setFechaCierre(datos.fechaCierre());
        autoevaluacion.setOculto(datos.oculto());
        if (!conIntentos) {
            autoevaluacion.setNombre(datos.nombre());
            autoevaluacion.setTiempoLimite(datos.tiempoLimite());
            autoevaluacion.setCantidadPreguntas(datos.cantidadPreguntas());
            autoevaluacion.setNotaMinima(datos.notaMinima());
            autoevaluacion.setFechaApertura(datos.fechaApertura());
            autoevaluacion.setDescuentoPorError(datos.descuentoPorError());
            autoevaluacion.setNavegacionLibre(datos.navegacionLibre());
            autoevaluacion.setPermiteSalir(datos.permiteSalir());
            reemplazarPools(autoevaluacion, datos.pools());
        }
        autoevaluacion.setUltimaModificacion(LocalDateTime.now());
        autoevaluacion = autoevaluacionRepositorio.save(autoevaluacion);
        if (seOculta) {
            progresoServicio.actualizarProgresoDeLaUnidad(unidad);
        }
        return autoevaluacion;
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Autoevaluacion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        autoevaluacionRepositorio.save(entidad);
    }

    /**
     * CU-67: Da de baja una autoevaluación sin intentos.
     */
    @Override
    @Transactional
    public void darDeBajaAutoevaluacion(Integer idAutoevaluacion, Docente docente) {
        darDeBajaVarias(List.of(idAutoevaluacion == null ? -1 : idAutoevaluacion), docente);
    }

    /**
     * Da de baja varias autoevaluaciones. Es todo o nada: primero se valida el conjunto y recién después se dan de baja.
     */
    @Override
    @Transactional
    public void darDeBajaVarias(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos una autoevaluación.");
        }
        List<Autoevaluacion> autoevaluaciones = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        Set<Integer> dadasDeBaja = autoevaluaciones.stream().map(Autoevaluacion::getIdAutoevaluacion)
                .collect(Collectors.toSet());
        for (Autoevaluacion autoevaluacion : autoevaluaciones) {
            Unidad unidad = autoevaluacion.getUnidad();
            if (autoevaluacion.esVisible() && quedariaSinAutoevaluacionVisible(unidad, dadasDeBaja)) {
                throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("la autoevaluación",
                        autoevaluacion.getNombre(), "es la única visible de la unidad «" + unidad.getTitulo()
                                + "» y su programa tiene cohortes en curso",
                        "Registrá otra autoevaluación antes."));
            }
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (Autoevaluacion autoevaluacion : autoevaluaciones) {
            autoevaluacion.marcarInactivo();
            autoevaluacion.setUltimaModificacion(ahora);
            autoevaluacionRepositorio.save(autoevaluacion);
        }
        autoevaluaciones.stream().map(Autoevaluacion::getUnidad).distinct().forEach(progresoServicio::actualizarProgresoDeLaUnidad);
    }

    /**
     * Verifica que la autoevaluación esté activa, que el docente (si se indica) participe en su curso y que ningún alumno
     * registre intentos sobre ella.
     */
    private Autoevaluacion validarBaja(Integer id, Docente docente) {
        Autoevaluacion autoevaluacion = autoevaluacionRepositorio.findById(id == null ? -1 : id)
                .filter(a -> !a.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra activa."));
        if (docente != null) {
            obtenerParticipacion(autoevaluacion.getUnidad().getCurso(), docente);
        }
        int intentos = autoevaluacion.getIntentosActivos().size();
        if (intentos > 0) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("la autoevaluación",
                    autoevaluacion.getNombre(), "tiene " + MensajeErrorUtilidad.cantidad(intentos, "intento registrado",
                            "intentos registrados"), ""));
        }
        return autoevaluacion;
    }

    // ---------------------------------------------------------------- Auxiliares

    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
    }

    /**
     * Verifica que el docente participe en el curso (como titular o ayudante).
     */
    private void obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        if (!participaEnElCurso(curso, docente)) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
    }

    /**
     * Indica si, sacando las autoevaluaciones indicadas de las visibles, una unidad con cohortes en curso se quedaría sin
     * ninguna. Una unidad que ya estaba sin autoevaluaciones visibles no cuenta: no es lo que cambia esta operación.
     */
    private boolean quedariaSinAutoevaluacionVisible(Unidad unidad, Set<Integer> quitadas) {
        List<Autoevaluacion> visibles = unidad.getAutoevaluacionesPublicadas();
        return unidad.tieneCohortesEnCurso() && !visibles.isEmpty()
                && visibles.stream().allMatch(a -> quitadas.contains(a.getIdAutoevaluacion()));
    }

    // ---- Lectura y validación de los datos del formulario

    /**
     * Lee y valida los datos de una solicitud, en el orden de los casos de uso: campos obligatorios y pools, pools
     * vigentes, valores enteros, fechas y cantidad de preguntas que reúnen los pools.
     */
    private Datos leer(Unidad unidad, SolicitudAutoevaluacion s) {
        if (s.nombre() == null || s.nombre().isBlank()) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", "Error! Debe completar el nombre de la autoevaluación.");
        }
        String nombre = s.nombre().trim();
        if (nombre.length() > LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre",
                    MensajeErrorUtilidad.demasiadoLargo("El nombre de la autoevaluación", LONGITUD_NOMBRE, "caracteres"));
        }
        obligatorio(s.tiempoLimite(), "tiempoLimite", "el tiempo límite");
        obligatorio(s.cantidadPreguntas(), "cantidadPreguntas", "la cantidad de preguntas");
        obligatorio(s.notaMinima(), "notaMinima", "la nota mínima");
        obligatorio(s.fechaApertura(), "fechaApertura", "la fecha de apertura");
        if (s.idsPools() == null || s.idsPools().isEmpty()) {
            throw RespuestaFormularioUtilidad.campoInvalido("idsPools", "Error! Debe seleccionar al menos un pool.");
        }
        List<Pool> pools = new ArrayList<>();
        for (Integer idPool : new LinkedHashSet<>(s.idsPools())) {
            Pool pool = poolRepositorio.findById(idPool == null ? -1 : idPool).filter(p -> !p.esInactivo())
                    .orElseThrow(() -> RespuestaFormularioUtilidad.campoInvalido("idsPools",
                            "Error! Alguno de los pools seleccionados no se encuentra activo."));
            if (unidadesQuePuedenUsar(pool).stream().noneMatch(u -> u.getIdUnidad() == unidad.getIdUnidad())) {
                throw RespuestaFormularioUtilidad.campoInvalido("idsPools", "Error! El pool «" + pool.getNombre()
                        + "» no puede usarse en esta autoevaluación: solo se admiten los pools de su unidad y, si es la última "
                        + "unidad del programa, los de las otras unidades del programa.");
            }
            pools.add(pool);
        }
        int tiempoLimite = entero(s.tiempoLimite(), "tiempoLimite", "El tiempo límite");
        int cantidadPreguntas = entero(s.cantidadPreguntas(), "cantidadPreguntas", "La cantidad de preguntas");
        int notaMinima = entero(s.notaMinima(), "notaMinima", "La nota mínima");
        if (notaMinima > NOTA_MAXIMA) {
            throw RespuestaFormularioUtilidad.campoInvalido("notaMinima",
                    "Error! La nota mínima no puede ser mayor a " + NOTA_MAXIMA + ".");
        }
        Integer intentos = s.intentosPermitidos() == null || s.intentosPermitidos().isBlank() ? null
                : entero(s.intentosPermitidos(), "intentosPermitidos", "La cantidad de intentos permitidos");
        LocalDateTime apertura = fecha(s.fechaApertura(), "fechaApertura", "La fecha de apertura");
        LocalDateTime cierre = s.fechaCierre() == null || s.fechaCierre().isBlank() ? null
                : fecha(s.fechaCierre(), "fechaCierre", "La fecha de cierre");
        if (cierre != null && !cierre.isAfter(apertura)) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaCierre",
                    "Error! La fecha de cierre debe ser posterior a la fecha de apertura.");
        }
        long disponibles = pools.stream().mapToLong(Pool::getCantidadPreguntas).sum();
        if (disponibles < cantidadPreguntas) {
            throw RespuestaFormularioUtilidad.campoInvalido("idsPools", "Error! Los pools seleccionados reúnen "
                    + MensajeErrorUtilidad.cantidad(disponibles, "pregunta", "preguntas") + " y la autoevaluación sortea "
                    + cantidadPreguntas + ": seleccioná más pools o reducí la cantidad de preguntas.");
        }
        return new Datos(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentos, apertura, cierre,
                s.descuentoPorError(), s.navegacionLibre(), s.permiteSalir(), s.oculto(), pools);
    }

    private void obligatorio(String valor, String campo, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw RespuestaFormularioUtilidad.campoInvalido(campo, "Error! Debe completar " + etiqueta + ".");
        }
    }

    private int entero(String valor, String campo, String sujeto) {
        try {
            int numero = Integer.parseInt(valor.trim());
            if (numero > 0) {
                return numero;
            }
        } catch (NumberFormatException e) {
            // se informa abajo
        }
        throw RespuestaFormularioUtilidad.campoInvalido(campo, "Error! " + sujeto + " debe ser un número entero mayor a cero.");
    }

    private LocalDateTime fecha(String valor, String campo, String sujeto) {
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw RespuestaFormularioUtilidad.campoInvalido(campo, "Error! " + sujeto + " no es válida.");
        }
    }

    // ---- Modificación con intentos

    /**
     * Con intentos registrados el contenido queda protegido: solo pueden cambiar la fecha de cierre, la visibilidad y la
     * cantidad de intentos permitidos, y los dos valores se pueden extender pero no acortar.
     */
    private void validarModificacionConIntentos(Autoevaluacion actual, Datos nuevos) {
        boolean cambiaContenido = !actual.getNombre().equals(nuevos.nombre())
                || !Objects.equals(actual.getTiempoLimite(), nuevos.tiempoLimite())
                || actual.getCantidadPreguntas() != nuevos.cantidadPreguntas()
                || actual.getNotaMinima() != nuevos.notaMinima()
                || !mismoMinuto(actual.getFechaApertura(), nuevos.fechaApertura())
                || !actual.getDescuentoPorError().equals(nuevos.descuentoPorError())
                || !actual.getNavegacionLibre().equals(nuevos.navegacionLibre())
                || !actual.getPermiteSalir().equals(nuevos.permiteSalir())
                || !idsDe(actual.getPoolsActivos()).equals(idsDe(nuevos.pools()));
        if (cambiaContenido) {
            throw new IllegalArgumentException("Error! La autoevaluación «" + actual.getNombre() + "» ya registra intentos: "
                    + "solo pueden modificarse la fecha de cierre, la visibilidad y la cantidad de intentos permitidos.");
        }
        Integer intentosActuales = actual.getIntentosPermitidos();
        boolean intentosValidos = Objects.equals(intentosActuales, nuevos.intentosPermitidos())
                || (intentosActuales != null && (nuevos.intentosPermitidos() == null
                        || nuevos.intentosPermitidos() > intentosActuales));
        if (!intentosValidos) {
            throw RespuestaFormularioUtilidad.campoInvalido("intentosPermitidos", "Error! La cantidad de intentos permitidos "
                    + "solo puede ampliarse (o quedar sin límite) porque la autoevaluación ya registra intentos.");
        }
        LocalDateTime cierreActual = actual.getFechaCierre();
        boolean cierreValido = (cierreActual == null && nuevos.fechaCierre() == null)
                || (cierreActual != null && (nuevos.fechaCierre() == null
                        || !nuevos.fechaCierre().truncatedTo(ChronoUnit.MINUTES).isBefore(cierreActual.truncatedTo(ChronoUnit.MINUTES))));
        if (!cierreValido) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaCierre", "Error! La fecha de cierre solo puede extenderse "
                    + "(o eliminarse) porque la autoevaluación ya registra intentos.");
        }
    }

    private boolean mismoMinuto(LocalDateTime a, LocalDateTime b) {
        return a.truncatedTo(ChronoUnit.MINUTES).equals(b.truncatedTo(ChronoUnit.MINUTES));
    }

    private Set<Integer> idsDe(List<Pool> pools) {
        return pools.stream().map(Pool::getIdPool).collect(Collectors.toSet());
    }

    // ---- Aplicación de los datos

    private void aplicarDatosSimples(Autoevaluacion autoevaluacion, Datos datos) {
        autoevaluacion.setTiempoLimite(datos.tiempoLimite());
        autoevaluacion.setIntentosPermitidos(datos.intentosPermitidos());
        autoevaluacion.setFechaCierre(datos.fechaCierre());
        autoevaluacion.setDescuentoPorError(datos.descuentoPorError());
        autoevaluacion.setOculto(datos.oculto());
    }

    private void asociarPools(Autoevaluacion autoevaluacion, List<Pool> pools) {
        for (Pool pool : pools) {
            AutoevaluacionPool asociacion = autoevaluacionPoolRepositorio.save(new AutoevaluacionPool(pool, autoevaluacion));
            autoevaluacion.getAutoevaluacionPools().add(asociacion);
            pool.getAutoevaluacionPools().add(asociacion);
        }
    }

    /**
     * Deja asociados exactamente los pools indicados: quita los que ya no figuran y agrega los nuevos.
     */
    private void reemplazarPools(Autoevaluacion autoevaluacion, List<Pool> pools) {
        Set<Integer> elegidos = idsDe(pools);
        Set<Integer> actuales = new HashSet<>();
        for (AutoevaluacionPool asociacion : new ArrayList<>(autoevaluacion.getAutoevaluacionPools())) {
            if (elegidos.contains(asociacion.getPool().getIdPool())) {
                actuales.add(asociacion.getPool().getIdPool());
            } else {
                autoevaluacion.getAutoevaluacionPools().remove(asociacion);
                asociacion.getPool().getAutoevaluacionPools().remove(asociacion);
                autoevaluacionPoolRepositorio.delete(asociacion);
            }
        }
        asociarPools(autoevaluacion, pools.stream().filter(p -> !actuales.contains(p.getIdPool())).toList());
    }
}
