package com.app.idoneos.servicio.Tema;

import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.TemaCronogramaRepositorio;
import com.app.idoneos.utilidades.TextoUtilidad;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Tema}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TemaServicio}.
 */
@Service
public class TemaServicioImpl implements TemaServicio, CrudServicio<Tema> {

    @Autowired
    private com.app.idoneos.servicio.Programa.MinimoDeContenidoValidador minimoDeContenido;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private TemaCronogramaRepositorio temaCronogramaRepositorio;

    @Autowired
    private CargaHorariaTemas cargaHoraria;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el tema por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Tema> buscarPorId(Integer id) {
        return temaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Tema> obtenerTodo() {
        return temaRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return temaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de tema asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Tema asociados al Curso indicado
     */
    @Override
    public List<Tema> buscarPorCurso(Curso curso) {
        return temaRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * Indica si un usuario puede ver el contenido de un tema.
     */
    @Override
    public boolean puedeVerContenido(Tema tema, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        if (docente != null && docenteCursoRepositorio.findByCursoAndBajaFalse(tema.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente())) {
            return true;
        }
        Alumno alumno = usuario.getAlumno();
        return alumno != null && inscripcionServicio.buscarConAccesoATema(alumno, tema).isPresent();
    }

    /**
     * CU-19: Busca los temas del cronograma de un programa por título.
     */
    @Override
    public List<TemaCronograma> buscarConFiltros(Programa programa, String titulo) {
        String buscado = TextoUtilidad.normalizar(titulo);
        return programa.getCronogramaOrdenado().stream()
                .filter(c -> buscado.isEmpty() || TextoUtilidad.normalizar(c.getTema().getTitulo()).contains(buscado))
                .toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el tema en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Tema guardar(Tema entidad) {
        return temaRepositorio.save(entidad);
    }

    /**
     * CU-20: Agrega un tema (nueva o existente) al final del cronograma de un programa.
     */
    @Override
    @Transactional
    public Tema agregarTema(Docente docente, Integer programaId, Integer temaExistenteId, String titulo,
            String descripcion, String contenido) {
        return agregarTema(docente, programaId, temaExistenteId, titulo, descripcion, contenido, null);
    }

    /**
     * CU-20: Agrega un tema al cronograma de un programa, con los minutos de carga horaria indicados.
     */
    @Override
    @Transactional
    public Tema agregarTema(Docente docente, Integer programaId, Integer temaExistenteId, String titulo,
            String descripcion, String contenido, Integer cargaHorariaMinutos) {
        Programa programa = buscarProgramaActivo(programaId);
        Curso curso = programa.getCurso();
        obtenerParticipacion(curso, docente);
        // El tema entra con una semana, o con los días que queden hasta la duración máxima del curso
        int restante = curso.getDuracionMaximaDias() - programa.getDuracionTotalDias();
        if (restante <= 0) {
            throw new IllegalArgumentException("Error! El cronograma ya alcanzó la duración máxima del curso ("
                    + DuracionUtilidad.formatear(curso.getDuracionMaximaDias()) + "): no se pueden agregar más temas.");
        }

        Tema tema;
        if (temaExistenteId != null) {
            tema = temaRepositorio.findById(temaExistenteId)
                    .filter(u -> !u.esInactivo() && u.getCurso().getIdCurso() == curso.getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! El tema que se quiere incorporar no se encuentra activa en este curso."));
            if (tema.perteneceAlCronograma(programa)) {
                throw RespuestaFormularioUtilidad.campoInvalido("temaExistenteId", "Error! El tema ya forma parte del cronograma de este programa.");
            }
            if (tema.getProgramasQueLaIncluyen().isEmpty()) {
                throw new IllegalArgumentException(
                        "Error! El tema no forma parte del cronograma de ningún otro programa del curso.");
            }
        } else {
            String tituloLimpio = validarDatos(titulo, contenido);
            validarTituloLibre(curso, tituloLimpio, null);
            tema = new Tema(curso, tituloLimpio, contenido.trim());
            tema.setDescripcion(validarDescripcion(descripcion));
            cargaHoraria.asignarATemaNuevo(curso, tema, cargaHorariaMinutos);
            tema = temaRepositorio.save(tema);
        }
        int orden = programa.getTemasCronograma().stream().mapToInt(TemaCronograma::getNumeroOrden).max().orElse(0)
                + 1;
        TemaCronograma cronograma = temaCronogramaRepositorio
                .save(new TemaCronograma(programa, tema, orden, Math.min(DuracionUtilidad.DIAS_POR_SEMANA, restante)));
        programa.getTemasCronograma().add(cronograma);
        tema.getTemasCronograma().add(cronograma);
        return tema;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Tema modificar(Tema entidad) {
        return temaRepositorio.save(entidad);
    }

    /**
     * CU-21: Modifica un tema sin inscripciones activas asociadas.
     */
    @Override
    @Transactional
    public Tema modificarTema(Integer id, Docente docente, String titulo, String descripcion, String contenido) {
        return modificarTema(id, docente, titulo, descripcion, contenido, null);
    }

    /**
     * CU-21: Modifica un tema sin inscripciones activas asociadas, incluidos sus minutos de carga horaria.
     */
    @Override
    @Transactional
    public Tema modificarTema(Integer id, Docente docente, String titulo, String descripcion, String contenido,
            Integer cargaHorariaMinutos) {
        Tema tema = temaRepositorio.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        obtenerParticipacion(tema.getCurso(), docente);
        if (tema.tieneInscripcionesActivas()) {
            throw new IllegalArgumentException("Error! El tema tiene inscripciones activas asociadas: no puede "
                    + "modificarse porque es parte de lo que el alumno vio y pagó al inscribirse.");
        }
        String tituloLimpio = validarDatos(titulo, contenido);
        validarTituloLibre(tema.getCurso(), tituloLimpio, tema);
        tema.setTitulo(tituloLimpio);
        tema.setDescripcion(validarDescripcion(descripcion));
        tema.setContenido(contenido.trim());
        if (cargaHorariaMinutos != null && cargaHorariaMinutos != tema.getCargaHorariaMinutos()) {
            cargaHoraria.cambiarCargaDeTema(tema, cargaHorariaMinutos);
        }
        tema.setUltimaModificacion(LocalDateTime.now());
        return temaRepositorio.save(tema);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Tema entidad) {
        entidad.marcarInactivo(); // Baja lógica
        temaRepositorio.save(entidad);
    }

    /**
     * CU-22: Da de baja un tema del cronograma de un programa.
     */
    @Override
    @Transactional
    public void darDeBajaTema(Integer id, Integer programaId, Docente docente) {
        Programa programa = buscarProgramaActivo(programaId);
        TemaCronograma aQuitar = validarBaja(id, programa, docente);
        validarMinimoAlQuitar(programa, List.of(aQuitar));
        quitarDelCronograma(programa, List.of(aQuitar));
    }

    /**
     * CU-22: Da de baja varios temas del cronograma de un programa (todas o ninguna).
     */
    @Override
    @Transactional
    public void darDeBajaVarias(List<Integer> ids, Integer programaId, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un tema.");
        }
        Programa programa = buscarProgramaActivo(programaId);
        // Se valida todo antes de quitar alguna
        List<TemaCronograma> aQuitar = ids.stream().distinct().map(id -> validarBaja(id, programa, docente)).toList();
        validarMinimoAlQuitar(programa, aQuitar);
        quitarDelCronograma(programa, aQuitar);
    }

    /** Verifica que, sin estos temas, el programa con cohortes en curso conserve el mínimo de contenido publicado. */
    private void validarMinimoAlQuitar(Programa programa, List<TemaCronograma> aQuitar) {
        java.util.Set<Integer> ids = aQuitar.stream().map(c -> c.getTema().getIdTema()).collect(java.util.stream.Collectors.toSet());
        minimoDeContenido.verificarTemas(programa, ids).ifPresent(bloqueo -> {
            Tema primera = aQuitar.get(0).getTema();
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el tema", primera.getTitulo(),
                    (bloqueo.minimo() == 1 ? "es la único tema que debe conservar su programa"
                            : "es una de las " + bloqueo.minimoEnTemas() + " que debe conservar su programa")
                            + " y este tiene cohortes en curso",
                    "Registrá otro tema antes."));
        });
    }

    /** Verifica que el tema se pueda quitar del cronograma del programa y devuelve su lugar en el cronograma. */
    private TemaCronograma validarBaja(Integer id, Programa programa, Docente docente) {
        Tema tema = temaRepositorio.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        if (docente != null) {
            obtenerParticipacion(programa.getCurso(), docente);
        }
        TemaCronograma cronograma = programa.getTemasCronograma().stream()
                .filter(c -> c.getTema().getIdTema() == tema.getIdTema()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! El tema '" + tema.getTitulo() + "' no pertenece al cronograma de este programa."));
        if (programa.getCantidadInscripcionesActivas() > 0) {
            // Las inscripciones no se pueden deshacer: no hay un paso que indicar
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el tema", tema.getTitulo(),
                    "el programa «" + programa.getNombre() + "» tiene "
                            + MensajeErrorUtilidad.cantidad(programa.getCantidadInscripcionesActivas(), "inscripción activa",
                                    "inscripciones activas")
                            + " y su cronograma es lo que los alumnos vieron y pagaron al inscribirse", ""));
        }
        return cronograma;
    }

    /**
     * Quita los temas del cronograma del programa, renumera las que quedan y da de baja los temas que ya no
     * forman parte del cronograma de ningún programa.
     */
    private void quitarDelCronograma(Programa programa, List<TemaCronograma> aQuitar) {
        LocalDateTime ahora = LocalDateTime.now();
        for (TemaCronograma cronograma : aQuitar) {
            Tema tema = cronograma.getTema();
            programa.getTemasCronograma().remove(cronograma);
            tema.getTemasCronograma().remove(cronograma);
            temaCronogramaRepositorio.delete(cronograma);
        }
        temaCronogramaRepositorio.flush();

        // Las restantes se renumeran sin huecos, de a una para no repetir un número de orden
        int orden = 1;
        for (TemaCronograma restante : programa.getTemasCronograma().stream()
                .sorted(Comparator.comparingInt(TemaCronograma::getNumeroOrden)).toList()) {
            if (restante.getNumeroOrden() != orden) {
                restante.setNumeroOrden(orden);
                restante.setUltimaModificacion(ahora);
                temaCronogramaRepositorio.saveAndFlush(restante);
            }
            orden++;
        }
        boolean hayBajas = false;
        for (TemaCronograma cronograma : aQuitar) {
            Tema tema = cronograma.getTema();
            if (tema.getProgramasQueLaIncluyen().isEmpty()) {
                tema.marcarInactivo();
                tema.setUltimaModificacion(ahora);
                temaRepositorio.save(tema);
                hayBajas = true;
            }
        }
        // La carga horaria de los temas dados de baja se vuelve a repartir entre los que quedan
        if (hayBajas) {
            cargaHoraria.redistribuir(programa.getCurso());
        }
    }

    private Programa buscarProgramaActivo(Integer programaId) {
        return programaRepositorio.findById(programaId == null ? -1 : programaId).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
    }

    /** Obtiene la participación del docente en el curso (como titular o ayudante). */
    private void obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
    }

    /** Valida el título y el contenido, y devuelve el título sin espacios sobrantes. */
    private String validarDatos(String titulo, String contenido) {
        if (titulo == null || titulo.isBlank() || contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("Error! Debe completar el título y el contenido.");
        }
        if (titulo.trim().length() > Tema.LONGITUD_TITULO) {
            throw RespuestaFormularioUtilidad.campoInvalido("titulo", MensajeErrorUtilidad.demasiadoLargo("El título", Tema.LONGITUD_TITULO, "caracteres"));
        }
        return titulo.trim();
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (limpia != null && limpia.length() > Tema.LONGITUD_DESCRIPCION) {
            throw RespuestaFormularioUtilidad.campoInvalido("descripcion", MensajeErrorUtilidad.demasiadoLargo("La descripción", Tema.LONGITUD_DESCRIPCION, "caracteres"));
        }
        return limpia;
    }

    /** Verifica que el curso no tenga otro tema con el mismo título (el título es único por curso). */
    private void validarTituloLibre(Curso curso, String titulo, Tema propia) {
        boolean repetido = temaRepositorio.findAll().stream()
                .anyMatch(u -> u.getCurso().getIdCurso() == curso.getIdCurso() && u.getTitulo().equalsIgnoreCase(titulo)
                        && (propia == null || u.getIdTema() != propia.getIdTema()));
        if (repetido) {
            throw RespuestaFormularioUtilidad.campoInvalido("titulo", "Error! Ya existe un tema con ese título en el curso.");
        }
    }
}
