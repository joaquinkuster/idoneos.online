package com.app.idoneos.service.autoevaluacion;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.AutoevaluacionPool;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.AutoevaluacionPoolRepository;
import com.app.idoneos.repository.AutoevaluacionRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.PoolRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.progreso.ProgresoService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Autoevaluacion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AutoevaluacionService}.
 */
@Service
public class AutoevaluacionServiceImpl implements AutoevaluacionService {

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
    private AutoevaluacionRepository autoevaluacionRepository;

    @Autowired
    private AutoevaluacionPoolRepository autoevaluacionPoolRepository;

    @Autowired
    private PoolRepository poolRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private ProgresoService progresoService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la autoevaluación por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Autoevaluacion> buscarPorId(Integer id) {
        return autoevaluacionRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Autoevaluacion> obtenerTodo() {
        return autoevaluacionRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de autoevaluacion asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Autoevaluacion asociados al Tema indicado
     */
    @Override
    public List<Autoevaluacion> buscarPorTema(Tema tema) {
        return autoevaluacionRepository.findByTemaAndBajaFalse(tema);
    }

    /**
     * CU-64: Busca las autoevaluaciones de un curso (incluidas las dadas de baja) aplicando filtros opcionales. Las dadas
     * de baja van siempre al final.
     */
    @Override
    public List<Autoevaluacion> buscarConFiltros(Integer idCurso, Integer idTema, String nombre, Boolean baja,
            Docente soloDeDocente) {
        String criterio = nombre == null ? "" : nombre.trim().toLowerCase();
        // El curso, el tema y la baja se filtran en la base; el texto y la participación del docente, acá
        return autoevaluacionRepository.buscarPorFiltros(idCurso, idTema, baja).stream()
                .filter(a -> criterio.isEmpty() || a.getNombre().toLowerCase().contains(criterio))
                .filter(a -> soloDeDocente == null || participaEnElCurso(a.getTema().getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(Autoevaluacion::getBaja)
                        .thenComparingInt((Autoevaluacion a) -> a.getTema().getIdTema())
                        .thenComparingInt(Autoevaluacion::getIdAutoevaluacion))
                .toList();
    }

    /**
     * Obtiene los temas cuyos autoevaluaciones pueden usar un pool: la propia y las que vienen después en el cronograma
     * de los programas que la incluyen (una autoevaluación puede combinar pools de su tema y de las anteriores).
     */
    @Override
    public List<Tema> temasQuePuedenUsar(Pool pool) {
        Tema delPool = pool.getTema();
        List<Tema> temas = new ArrayList<>(List.of(delPool));
        for (Programa programa : delPool.getProgramasQueLaIncluyen()) {
            for (TemaCronograma cronograma : programa.getCronogramaOrdenado()) {
                Tema candidata = cronograma.getTema();
                if (temas.stream().noneMatch(u -> u.getIdTema() == candidata.getIdTema())
                        && puedeUsarPoolDe(candidata, delPool)) {
                    temas.add(candidata);
                }
            }
        }
        return temas;
    }

    /**
     * Indica si una autoevaluación de {@code tema} puede usar pools de {@code delPool}: es la mismo tema o viene antes
     * en el cronograma de algún programa que incluye a ambas, y en ningún programa viene después. Nunca se usan pools de
     * temas posteriores.
     */
    private boolean puedeUsarPoolDe(Tema tema, Tema delPool) {
        if (tema.getIdTema() == delPool.getIdTema()) {
            return true;
        }
        boolean vieneAntes = false;
        for (Programa programa : tema.getProgramasQueLaIncluyen()) {
            int ordenDeLaTema = ordenEn(programa, tema);
            int ordenDelPool = ordenEn(programa, delPool);
            if (ordenDelPool < 0) {
                continue;
            }
            if (ordenDelPool > ordenDeLaTema) {
                return false;
            }
            vieneAntes = true;
        }
        return vieneAntes;
    }

    private int ordenEn(Programa programa, Tema tema) {
        return programa.getCronogramaOrdenado().stream().filter(c -> c.getTema().getIdTema() == tema.getIdTema())
                .mapToInt(TemaCronograma::getNumeroOrden).findFirst().orElse(-1);
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
        return autoevaluacionRepository.save(entidad);
    }

    /**
     * CU-65: Crea una autoevaluación en un tema activo, asociada a uno o más pools.
     */
    @Override
    @Transactional
    public Autoevaluacion registrar(Docente docente, Integer idTema, SolicitudAutoevaluacion solicitud) {
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        obtenerParticipacion(tema.getCurso(), docente);
        Datos datos = leer(tema, solicitud);

        Autoevaluacion autoevaluacion = new Autoevaluacion(tema, datos.nombre(), datos.cantidadPreguntas(),
                datos.notaMinima(), datos.fechaApertura());
        aplicarDatosSimples(autoevaluacion, datos);
        autoevaluacion.setNavegacionLibre(datos.navegacionLibre());
        autoevaluacion.setPermiteSalir(datos.permiteSalir());
        autoevaluacion = autoevaluacionRepository.save(autoevaluacion);
        asociarPools(autoevaluacion, datos.pools());
        tema.getAutoevaluaciones().add(autoevaluacion);
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
        return autoevaluacionRepository.save(entidad);
    }

    /**
     * CU-66: Modifica una autoevaluación. Con intentos registrados solo pueden extenderse la fecha de cierre, ocultarse o
     * mostrarse y ampliarse la cantidad de intentos permitidos.
     */
    @Override
    @Transactional
    public Autoevaluacion modificar(Integer idAutoevaluacion, Docente docente,
            SolicitudAutoevaluacion solicitud) {
        Autoevaluacion autoevaluacion = autoevaluacionRepository.findById(idAutoevaluacion == null ? -1 : idAutoevaluacion)
                .filter(a -> !a.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra activa."));
        Tema tema = autoevaluacion.getTema();
        obtenerParticipacion(tema.getCurso(), docente);
        Datos datos = leer(tema, solicitud);
        boolean conIntentos = autoevaluacion.tieneIntentosActivos();
        if (conIntentos) {
            validarModificacionConIntentos(autoevaluacion, datos);
        }
        boolean seOculta = datos.oculto() && !autoevaluacion.getOculto();
        if (seOculta && quedariaSinAutoevaluacionVisible(tema, Set.of(autoevaluacion.getIdAutoevaluacion()))) {
            throw com.app.idoneos.util.RespuestaFormularioUtil.armarCampoInvalido("oculto",
                    com.app.idoneos.util.MensajeErrorUtil.armarAccionBloqueada("ocultar", "la autoevaluación", autoevaluacion.getNombre(),
                            com.app.idoneos.util.MensajeErrorUtil.motivoDisenoIncompleto("es la única autoevaluación visible del tema «" + tema.getTitulo() + "»"),
                            "Registrá otra antes de ocultarla."));
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
        autoevaluacion = autoevaluacionRepository.save(autoevaluacion);
        if (seOculta) {
            progresoService.actualizarPorTema(tema);
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
        autoevaluacionRepository.save(entidad);
    }

    /**
     * CU-67: Da de baja una autoevaluación sin intentos.
     */
    @Override
    @Transactional
    public void darDeBaja(Integer idAutoevaluacion, Docente docente) {
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
            Tema tema = autoevaluacion.getTema();
            if (autoevaluacion.esVisible() && quedariaSinAutoevaluacionVisible(tema, dadasDeBaja)) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la autoevaluación",
                        autoevaluacion.getNombre(),
                        MensajeErrorUtil.motivoDisenoIncompleto("es la única autoevaluación visible del tema «" + tema.getTitulo() + "»"),
                        "Registrá otra antes de darla de baja."));
            }
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (Autoevaluacion autoevaluacion : autoevaluaciones) {
            autoevaluacion.marcarInactivo();
            autoevaluacion.setUltimaModificacion(ahora);
            autoevaluacionRepository.save(autoevaluacion);
        }
        autoevaluaciones.stream().map(Autoevaluacion::getTema).distinct().forEach(progresoService::actualizarPorTema);
    }

    /**
     * Verifica que la autoevaluación esté activa, que el docente (si se indica) participe en su curso y que ningún alumno
     * registre intentos sobre ella.
     */
    private Autoevaluacion validarBaja(Integer id, Docente docente) {
        Autoevaluacion autoevaluacion = autoevaluacionRepository.findById(id == null ? -1 : id)
                .filter(a -> !a.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La autoevaluación no se encuentra activa."));
        if (docente != null) {
            obtenerParticipacion(autoevaluacion.getTema().getCurso(), docente);
        }
        int intentos = autoevaluacion.getIntentosActivos().size();
        if (intentos > 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la autoevaluación",
                    autoevaluacion.getNombre(), "tiene " + MensajeErrorUtil.formatearCantidad(intentos, "intento registrado",
                            "intentos registrados"), ""));
        }
        return autoevaluacion;
    }

    // ---------------------------------------------------------------- Auxiliares

    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
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
     * Indica si, sacando las autoevaluaciones indicadas de las visibles, un tema con cohortes en curso se quedaría sin
     * ninguna. Un tema que ya estaba sin autoevaluaciones visibles no cuenta: no es lo que cambia esta operación.
     */
    private boolean quedariaSinAutoevaluacionVisible(Tema tema, Set<Integer> quitadas) {
        List<Autoevaluacion> visibles = tema.getAutoevaluacionesPublicadas();
        return tema.tieneCohortesActivas() && !visibles.isEmpty()
                && visibles.stream().allMatch(a -> quitadas.contains(a.getIdAutoevaluacion()));
    }

    // ---- Lectura y validación de los datos del formulario

    /**
     * Lee y valida los datos de una solicitud, en el orden de los casos de uso: campos obligatorios y pools, pools
     * vigentes, valores enteros, fechas y cantidad de preguntas que reúnen los pools.
     */
    private Datos leer(Tema tema, SolicitudAutoevaluacion s) {
        if (s.nombre() == null || s.nombre().isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Debe completar el nombre de la autoevaluación.");
        }
        String nombre = s.nombre().trim();
        if (nombre.length() > LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre",
                    MensajeErrorUtil.armarDemasiadoLargo("El nombre de la autoevaluación", LONGITUD_NOMBRE, "caracteres"));
        }
        obligatorio(s.tiempoLimite(), "tiempoLimite", "el tiempo límite");
        obligatorio(s.cantidadPreguntas(), "cantidadPreguntas", "la cantidad de preguntas");
        obligatorio(s.notaMinima(), "notaMinima", "la nota mínima");
        obligatorio(s.fechaApertura(), "fechaApertura", "la fecha de apertura");
        if (s.idsPools() == null || s.idsPools().isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idsPools", "Error! Debe seleccionar al menos un pool.");
        }
        List<Pool> pools = new ArrayList<>();
        for (Integer idPool : new LinkedHashSet<>(s.idsPools())) {
            Pool pool = poolRepository.findById(idPool == null ? -1 : idPool).filter(p -> !p.esInactivo())
                    .orElseThrow(() -> RespuestaFormularioUtil.armarCampoInvalido("idsPools",
                            "Error! Alguno de los pools seleccionados no se encuentra activo."));
            if (!puedeUsarPoolDe(tema, pool.getTema())) {
                throw RespuestaFormularioUtil.armarCampoInvalido("idsPools", "Error! El pool «" + pool.getNombre()
                        + "» (tema «" + pool.getTema().getTitulo() + "») no puede usarse en esta autoevaluación: solo "
                        + "se admiten los pools de su tema y de los temas anteriores del programa, nunca de las posteriores.");
            }
            pools.add(pool);
        }
        int tiempoLimite = entero(s.tiempoLimite(), "tiempoLimite", "El tiempo límite");
        int cantidadPreguntas = entero(s.cantidadPreguntas(), "cantidadPreguntas", "La cantidad de preguntas");
        int notaMinima = entero(s.notaMinima(), "notaMinima", "La nota mínima");
        if (notaMinima > NOTA_MAXIMA) {
            throw RespuestaFormularioUtil.armarCampoInvalido("notaMinima",
                    "Error! La nota mínima no puede ser mayor a " + NOTA_MAXIMA + ".");
        }
        Integer intentos = s.intentosPermitidos() == null || s.intentosPermitidos().isBlank() ? null
                : entero(s.intentosPermitidos(), "intentosPermitidos", "La cantidad de intentos permitidos");
        LocalDateTime apertura = fecha(s.fechaApertura(), "fechaApertura", "La fecha de apertura");
        LocalDateTime cierre = s.fechaCierre() == null || s.fechaCierre().isBlank() ? null
                : fecha(s.fechaCierre(), "fechaCierre", "La fecha de cierre");
        if (cierre != null && !cierre.isAfter(apertura)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaCierre",
                    "Error! La fecha de cierre debe ser posterior a la fecha de apertura.");
        }
        long disponibles = pools.stream().mapToLong(Pool::getCantidadPreguntas).sum();
        if (disponibles < cantidadPreguntas) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idsPools", "Error! Los pools seleccionados reúnen "
                    + MensajeErrorUtil.formatearCantidad(disponibles, "pregunta", "preguntas") + " y la autoevaluación sortea "
                    + cantidadPreguntas + ": seleccioná más pools o reducí la cantidad de preguntas.");
        }
        return new Datos(nombre, tiempoLimite, cantidadPreguntas, notaMinima, intentos, apertura, cierre,
                s.descuentoPorError(), s.navegacionLibre(), s.permiteSalir(), s.oculto(), pools);
    }

    private void obligatorio(String valor, String campo, String etiqueta) {
        if (valor == null || valor.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido(campo, "Error! Debe completar " + etiqueta + ".");
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
        throw RespuestaFormularioUtil.armarCampoInvalido(campo, "Error! " + sujeto + " debe ser un número entero mayor a cero.");
    }

    private LocalDateTime fecha(String valor, String campo, String sujeto) {
        try {
            return LocalDateTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw RespuestaFormularioUtil.armarCampoInvalido(campo, "Error! " + sujeto + " no es válida.");
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
            throw RespuestaFormularioUtil.armarCampoInvalido("intentosPermitidos", "Error! La cantidad de intentos permitidos "
                    + "solo puede ampliarse (o quedar sin límite) porque la autoevaluación ya registra intentos.");
        }
        LocalDateTime cierreActual = actual.getFechaCierre();
        boolean cierreValido = (cierreActual == null && nuevos.fechaCierre() == null)
                || (cierreActual != null && (nuevos.fechaCierre() == null
                        || !nuevos.fechaCierre().truncatedTo(ChronoUnit.MINUTES).isBefore(cierreActual.truncatedTo(ChronoUnit.MINUTES))));
        if (!cierreValido) {
            throw RespuestaFormularioUtil.armarCampoInvalido("fechaCierre", "Error! La fecha de cierre solo puede extenderse "
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
            AutoevaluacionPool asociacion = autoevaluacionPoolRepository.save(new AutoevaluacionPool(pool, autoevaluacion));
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
                autoevaluacionPoolRepository.delete(asociacion);
            }
        }
        asociarPools(autoevaluacion, pools.stream().filter(p -> !actuales.contains(p.getIdPool())).toList());
    }
}
