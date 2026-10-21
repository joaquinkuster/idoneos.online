package com.app.idoneos.servicio.Pool;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.OpcionRespuestaRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.PreguntaRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pool}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PoolServicio}.
 */
@Service
public class PoolServicioImpl implements PoolServicio, CrudServicio<Pool> {

    /** Largo máximo del nombre del pool. */
    private static final int LONGITUD_NOMBRE = 50;

    /** Largo máximo del enunciado de una pregunta y del texto de una opción. */
    private static final int LONGITUD_TEXTO = 150;

    @Autowired
    private PoolRepositorio poolRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private PreguntaRepositorio preguntaRepositorio;

    @Autowired
    private OpcionRespuestaRepositorio opcionRespuestaRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el pool por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pool> buscarPorId(Integer id) {
        return poolRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pool> obtenerTodo() {
        return poolRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return poolRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de pool asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Pool asociados al Unidad indicado
     */
    @Override
    public List<Pool> buscarPorUnidad(Unidad unidad) {
        return poolRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * CU-60: Busca los pools de un curso (incluidos los dados de baja) aplicando filtros opcionales. Los dados de baja
     * van siempre al final.
     */
    @Override
    public List<Pool> buscarConFiltros(Integer idCurso, Integer idUnidad, String nombre, Boolean baja,
            Docente soloDeDocente) {
        String criterio = nombre == null ? "" : nombre.trim().toLowerCase();
        return poolRepositorio.findAll().stream()
                .filter(p -> idCurso == null || p.getUnidad().getCurso().getIdCurso() == idCurso)
                .filter(p -> idUnidad == null || p.getUnidad().getIdUnidad() == idUnidad)
                .filter(p -> criterio.isEmpty() || p.getNombre().toLowerCase().contains(criterio))
                .filter(p -> baja == null || p.getBaja().equals(baja))
                .filter(p -> soloDeDocente == null || participaEnElCurso(p.getUnidad().getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(Pool::getBaja)
                        .thenComparingInt((Pool p) -> p.getUnidad().getIdUnidad())
                        .thenComparingInt(Pool::getIdPool))
                .toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el pool en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pool guardar(Pool entidad) {
        return poolRepositorio.save(entidad);
    }

    /**
     * CU-61: Crea un pool en una unidad activa, con sus preguntas y opciones de respuesta.
     */
    @Override
    @Transactional
    public Pool registrarPool(Docente docente, Integer idUnidad, String nombre, List<SolicitudPregunta> preguntas) {
        Unidad unidad = unidadRepositorio.findById(idUnidad == null ? -1 : idUnidad)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        obtenerParticipacion(unidad.getCurso(), docente);
        String nombreLimpio = validarNombre(nombre, unidad, null);
        validarPreguntas(preguntas);

        Pool pool = poolRepositorio.save(new Pool(unidad, nombreLimpio));
        aplicarPreguntas(pool, preguntas, LocalDateTime.now());
        return pool;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pool modificar(Pool entidad) {
        return poolRepositorio.save(entidad);
    }

    /**
     * CU-62: Modifica el nombre de un pool y agrega, edita o elimina sus preguntas y opciones.
     */
    @Override
    @Transactional
    public Pool modificarPool(Integer idPool, Docente docente, String nombre, List<SolicitudPregunta> preguntas) {
        Pool pool = poolRepositorio.findById(idPool == null ? -1 : idPool).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El pool no se encuentra activo."));
        obtenerParticipacion(pool.getUnidad().getCurso(), docente);
        if (pool.tieneIntentosActivos()) {
            throw new IllegalArgumentException("Error! El contenido del pool «" + pool.getNombre()
                    + "» no puede modificarse porque ya tiene intentos de autoevaluación asociados.");
        }
        String nombreLimpio = validarNombre(nombre, pool.getUnidad(), pool);
        validarPreguntas(preguntas);
        validarQueLasAutoevaluacionesPuedanSortear(pool, preguntas.size());

        LocalDateTime ahora = LocalDateTime.now();
        pool.setNombre(nombreLimpio);
        pool.setUltimaModificacion(ahora);
        aplicarPreguntas(pool, preguntas, ahora);
        return poolRepositorio.save(pool);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pool entidad) {
        entidad.marcarInactivo(); // Baja lógica
        poolRepositorio.save(entidad);
    }

    /**
     * CU-63: Da de baja un pool que no está asociado a ninguna autoevaluación activa.
     */
    @Override
    @Transactional
    public void darDeBajaPool(Integer idPool, Docente docente) {
        darDeBaja(validarBaja(idPool, docente));
    }

    /**
     * Da de baja varios pools. Es todo o nada: primero se valida cada uno y recién después se dan de baja.
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un pool.");
        }
        List<Pool> pools = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        pools.forEach(this::darDeBaja);
    }

    /**
     * Verifica que el pool esté activo, que el docente (si se indica) participe en su curso y que ninguna autoevaluación
     * activa lo use.
     */
    private Pool validarBaja(Integer idPool, Docente docente) {
        Pool pool = poolRepositorio.findById(idPool == null ? -1 : idPool).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El pool no se encuentra activo."));
        if (docente != null) {
            obtenerParticipacion(pool.getUnidad().getCurso(), docente);
        }
        List<Autoevaluacion> autoevaluaciones = pool.getAutoevaluacionesActivas();
        if (!autoevaluaciones.isEmpty()) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el pool", pool.getNombre(),
                    "está asociado a " + MensajeErrorUtilidad.cantidad(autoevaluaciones.size(),
                            "autoevaluación activa", "autoevaluaciones activas"),
                    "Primero hay que dar de baja las autoevaluaciones que lo usan."));
        }
        return pool;
    }

    private void darDeBaja(Pool pool) {
        pool.marcarInactivo();
        pool.setUltimaModificacion(LocalDateTime.now());
        poolRepositorio.save(pool);
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
     * Valida el nombre (obligatorio, hasta 50 caracteres y único en la unidad, incluso entre los pools dados de baja,
     * porque la restricción de la tabla lo exige) y lo devuelve sin espacios sobrantes.
     */
    private String validarNombre(String nombre, Unidad unidad, Pool actual) {
        if (nombre == null || nombre.isBlank()) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", "Error! Debe completar el nombre del pool.");
        }
        String limpio = nombre.trim();
        if (limpio.length() > LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre",
                    MensajeErrorUtilidad.demasiadoLargo("El nombre del pool", LONGITUD_NOMBRE, "caracteres"));
        }
        boolean repetido = poolRepositorio.findByUnidad(unidad).stream()
                .anyMatch(p -> (actual == null || p.getIdPool() != actual.getIdPool()) && p.getNombre().equalsIgnoreCase(limpio));
        if (repetido) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", "Error! Ya existe un pool llamado «" + limpio
                    + "» en esta unidad (incluso entre los dados de baja). Elegí otro nombre.");
        }
        return limpio;
    }

    /**
     * Valida que haya al menos una pregunta y que cada una tenga enunciado, al menos dos opciones con texto y la cantidad
     * de opciones correctas que corresponde a su tipo: exactamente una en las de opción única y al menos una en las de
     * opción múltiple.
     */
    private void validarPreguntas(List<SolicitudPregunta> preguntas) {
        if (preguntas == null || preguntas.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe cargar al menos una pregunta.");
        }
        int numero = 0;
        for (SolicitudPregunta pregunta : preguntas) {
            numero++;
            if (pregunta.texto() == null || pregunta.texto().isBlank()) {
                throw new IllegalArgumentException("Error! La pregunta " + numero + " no tiene enunciado.");
            }
            if (pregunta.texto().trim().length() > LONGITUD_TEXTO) {
                throw new IllegalArgumentException(MensajeErrorUtilidad.demasiadoLargo(
                        "El enunciado de la pregunta " + numero, LONGITUD_TEXTO, "caracteres"));
            }
            List<SolicitudOpcion> opciones = pregunta.opciones() == null ? List.of() : pregunta.opciones();
            if (opciones.size() < 2) {
                throw new IllegalArgumentException(
                        "Error! La pregunta " + numero + " debe tener al menos dos opciones de respuesta.");
            }
            for (SolicitudOpcion opcion : opciones) {
                if (opcion.texto() == null || opcion.texto().isBlank()) {
                    throw new IllegalArgumentException("Error! La pregunta " + numero + " tiene una opción sin texto.");
                }
                if (opcion.texto().trim().length() > LONGITUD_TEXTO) {
                    throw new IllegalArgumentException(MensajeErrorUtilidad.demasiadoLargo(
                            "El texto de una opción de la pregunta " + numero, LONGITUD_TEXTO, "caracteres"));
                }
            }
            long correctas = opciones.stream().filter(SolicitudOpcion::correcta).count();
            if (!pregunta.opcionMultiple() && correctas != 1) {
                throw new IllegalArgumentException("Error! La pregunta " + numero
                        + " (opción única) debe tener exactamente una opción correcta.");
            }
            if (pregunta.opcionMultiple() && correctas < 1) {
                throw new IllegalArgumentException("Error! La pregunta " + numero
                        + " (opción múltiple) debe tener al menos una opción correcta.");
            }
        }
    }

    /**
     * Valida que, tras la modificación, el pool conserve en conjunto con los demás pools de cada autoevaluación asociada
     * la cantidad de preguntas que esta necesita para sortear sus intentos.
     */
    private void validarQueLasAutoevaluacionesPuedanSortear(Pool pool, int preguntasNuevas) {
        for (Autoevaluacion autoevaluacion : pool.getAutoevaluacionesActivas()) {
            long deLosOtrosPools = autoevaluacion.getPoolsActivos().stream()
                    .filter(otro -> otro.getIdPool() != pool.getIdPool()).mapToLong(Pool::getCantidadPreguntas).sum();
            if (deLosOtrosPools + preguntasNuevas < autoevaluacion.getCantidadPreguntas()) {
                throw new IllegalArgumentException("Error! El pool no puede quedar con menos preguntas de las que necesita la "
                        + "autoevaluación «" + autoevaluacion.getNombre() + "», que sortea "
                        + autoevaluacion.getCantidadPreguntas() + " (hoy reúne " + (deLosOtrosPools + preguntasNuevas)
                        + " entre todos sus pools).");
            }
        }
    }

    /**
     * Aplica las preguntas de la solicitud al pool: las que ya existen se actualizan, las nuevas se crean y las que no
     * figuran se dan de baja.
     */
    private void aplicarPreguntas(Pool pool, List<SolicitudPregunta> solicitudes, LocalDateTime ahora) {
        Map<Integer, Pregunta> existentes = new LinkedHashMap<>();
        pool.getPreguntasActivas().forEach(p -> existentes.put(p.getIdPregunta(), p));
        Set<Integer> conservadas = new HashSet<>();
        for (SolicitudPregunta solicitud : solicitudes) {
            Pregunta pregunta = null;
            if (solicitud.id() != null) {
                pregunta = existentes.get(solicitud.id());
                if (pregunta == null || !conservadas.add(solicitud.id())) {
                    throw new IllegalArgumentException("Error! Una de las preguntas no pertenece al pool o está repetida.");
                }
            }
            String texto = solicitud.texto().trim();
            if (pregunta == null) {
                pregunta = preguntaRepositorio.save(new Pregunta(pool, texto, solicitud.opcionMultiple()));
                pool.getPreguntas().add(pregunta);
            } else if (!pregunta.getTexto().equals(texto) || !pregunta.getEsOpcionMultiple().equals(solicitud.opcionMultiple())) {
                pregunta.setTexto(texto);
                pregunta.setEsOpcionMultiple(solicitud.opcionMultiple());
                pregunta.setUltimaModificacion(ahora);
                preguntaRepositorio.save(pregunta);
            }
            aplicarOpciones(pregunta, solicitud.opciones(), ahora);
        }
        existentes.values().stream().filter(p -> !conservadas.contains(p.getIdPregunta())).forEach(p -> {
            p.marcarInactivo();
            p.setUltimaModificacion(ahora);
            preguntaRepositorio.save(p);
        });
    }

    /**
     * Aplica las opciones de la solicitud a la pregunta: las que ya existen se actualizan, las nuevas se crean y las que
     * no figuran se dan de baja.
     */
    private void aplicarOpciones(Pregunta pregunta, List<SolicitudOpcion> solicitudes, LocalDateTime ahora) {
        Map<Integer, OpcionRespuesta> existentes = new LinkedHashMap<>();
        pregunta.getOpcionesActivas().forEach(o -> existentes.put(o.getIdOpcionRespuesta(), o));
        Set<Integer> conservadas = new HashSet<>();
        for (SolicitudOpcion solicitud : solicitudes) {
            OpcionRespuesta opcion = null;
            if (solicitud.id() != null) {
                opcion = existentes.get(solicitud.id());
                if (opcion == null || !conservadas.add(solicitud.id())) {
                    throw new IllegalArgumentException("Error! Una de las opciones no pertenece a la pregunta o está repetida.");
                }
            }
            String texto = solicitud.texto().trim();
            if (opcion == null) {
                opcion = opcionRespuestaRepositorio.save(new OpcionRespuesta(pregunta, texto, solicitud.correcta()));
                pregunta.getOpcionesRespuesta().add(opcion);
            } else if (!opcion.getTexto().equals(texto) || !opcion.getEsCorrecta().equals(solicitud.correcta())) {
                opcion.setTexto(texto);
                opcion.setEsCorrecta(solicitud.correcta());
                opcion.setUltimaModificacion(ahora);
                opcionRespuestaRepositorio.save(opcion);
            }
        }
        existentes.values().stream().filter(o -> !conservadas.contains(o.getIdOpcionRespuesta())).forEach(o -> {
            o.marcarInactivo();
            o.setUltimaModificacion(ahora);
            opcionRespuestaRepositorio.save(o);
        });
    }
}
