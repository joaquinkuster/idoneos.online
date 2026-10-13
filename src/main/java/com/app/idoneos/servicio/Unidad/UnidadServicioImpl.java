package com.app.idoneos.servicio.Unidad;

import com.app.idoneos.utilidades.MensajeUtilidad;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.UnidadCronogramaRepositorio;
import com.app.idoneos.utilidades.TextoUtilidad;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Unidad}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link UnidadServicio}.
 */
@Service
public class UnidadServicioImpl implements UnidadServicio, CrudServicio<Unidad> {

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private UnidadCronogramaRepositorio unidadCronogramaRepositorio;

    /**
     * Guarda la unidad en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Unidad guardar(Unidad entidad) {
        return unidadRepositorio.save(entidad);
    }

    /**
     * Busca la unidad por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Unidad> buscarPorId(Integer id) {
        return unidadRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Unidad> obtenerTodo() {
        return unidadRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Unidad modificar(Unidad entidad) {
        return unidadRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Unidad entidad) {
        entidad.marcarInactivo(); // Baja lógica
        unidadRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return unidadRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    @Override
    public List<Unidad> buscarPorCurso(Curso curso) {
        return unidadRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * Indica si un usuario puede ver el contenido de una unidad.
     */
    @Override
    public boolean puedeVerContenido(Unidad unidad, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        if (docente != null && participacionDocenteRepositorio.findByCursoAndBajaFalse(unidad.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente())) {
            return true;
        }
        Alumno alumno = usuario.getAlumno();
        return alumno != null && inscripcionServicio.buscarConAccesoAUnidad(alumno, unidad).isPresent();
    }

    /**
     * CU-19: Busca las unidades del cronograma de un programa por título.
     */
    @Override
    public List<UnidadCronograma> buscarConFiltros(Programa programa, String titulo) {
        String buscado = TextoUtilidad.normalizar(titulo);
        return programa.getCronogramaOrdenado().stream()
                .filter(c -> buscado.isEmpty() || TextoUtilidad.normalizar(c.getUnidad().getTitulo()).contains(buscado))
                .toList();
    }

    /**
     * CU-20: Agrega una unidad (nueva o existente) al final del cronograma de un programa.
     */
    @Override
    @Transactional
    public Unidad agregarUnidad(Docente docente, Integer programaId, Integer unidadExistenteId, String titulo,
            String descripcion, String contenido) {
        Programa programa = buscarProgramaActivo(programaId);
        Curso curso = programa.getCurso();
        obtenerParticipacion(curso, docente);

        Unidad unidad;
        if (unidadExistenteId != null) {
            unidad = unidadRepositorio.findById(unidadExistenteId)
                    .filter(u -> !u.esInactivo() && u.getCurso().getIdCurso() == curso.getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! La unidad que se quiere incorporar no se encuentra activa en este curso."));
            if (unidad.perteneceAlCronograma(programa)) {
                throw new IllegalArgumentException("Error! La unidad ya forma parte del cronograma de este programa.");
            }
            if (unidad.getProgramasQueLaIncluyen().isEmpty()) {
                throw new IllegalArgumentException(
                        "Error! La unidad no forma parte del cronograma de ningún otro programa del curso.");
            }
        } else {
            String tituloLimpio = validarDatos(titulo, contenido);
            validarTituloLibre(curso, tituloLimpio, null);
            unidad = new Unidad(curso, tituloLimpio, contenido.trim());
            unidad.setDescripcion(validarDescripcion(descripcion));
            unidad = unidadRepositorio.save(unidad);
        }
        int orden = programa.getUnidadesCronograma().stream().mapToInt(UnidadCronograma::getNumeroOrden).max().orElse(0)
                + 1;
        UnidadCronograma cronograma = unidadCronogramaRepositorio
                .save(new UnidadCronograma(programa, unidad, orden, 1));
        programa.getUnidadesCronograma().add(cronograma);
        unidad.getUnidadesCronograma().add(cronograma);
        return unidad;
    }

    /**
     * CU-21: Modifica una unidad sin inscripciones activas asociadas.
     */
    @Override
    @Transactional
    public Unidad modificarUnidad(Integer id, Docente docente, String titulo, String descripcion, String contenido) {
        Unidad unidad = unidadRepositorio.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        obtenerParticipacion(unidad.getCurso(), docente);
        if (unidad.tieneInscripcionesActivas()) {
            throw new IllegalArgumentException("Error! La unidad tiene inscripciones activas asociadas: no puede "
                    + "modificarse porque es parte de lo que el alumno vio y pagó al inscribirse.");
        }
        String tituloLimpio = validarDatos(titulo, contenido);
        validarTituloLibre(unidad.getCurso(), tituloLimpio, unidad);
        unidad.setTitulo(tituloLimpio);
        unidad.setDescripcion(validarDescripcion(descripcion));
        unidad.setContenido(contenido.trim());
        unidad.setUltimaModificacion(LocalDateTime.now());
        return unidadRepositorio.save(unidad);
    }

    /**
     * CU-22: Da de baja una unidad del cronograma de un programa.
     */
    @Override
    @Transactional
    public void darDeBajaUnidad(Integer id, Integer programaId, Docente docente) {
        Programa programa = buscarProgramaActivo(programaId);
        quitarDelCronograma(programa, List.of(validarBaja(id, programa, docente)));
    }

    /**
     * CU-22: Da de baja varias unidades del cronograma de un programa (todas o ninguna).
     */
    @Override
    @Transactional
    public void darDeBajaVarias(List<Integer> ids, Integer programaId, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos una unidad.");
        }
        Programa programa = buscarProgramaActivo(programaId);
        // Se valida todo antes de quitar alguna
        List<UnidadCronograma> aQuitar = ids.stream().distinct().map(id -> validarBaja(id, programa, docente)).toList();
        quitarDelCronograma(programa, aQuitar);
    }

    /** Verifica que la unidad se pueda quitar del cronograma del programa y devuelve su lugar en el cronograma. */
    private UnidadCronograma validarBaja(Integer id, Programa programa, Docente docente) {
        Unidad unidad = unidadRepositorio.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        if (docente != null) {
            obtenerParticipacion(programa.getCurso(), docente);
        }
        UnidadCronograma cronograma = programa.getUnidadesCronograma().stream()
                .filter(c -> c.getUnidad().getIdUnidad() == unidad.getIdUnidad()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! La unidad '" + unidad.getTitulo() + "' no pertenece al cronograma de este programa."));
        if (programa.getCantidadInscripcionesActivas() > 0) {
            // Las inscripciones no se pueden deshacer: no hay un paso que indicar
            throw new IllegalArgumentException(MensajeUtilidad.bajaBloqueada("la unidad", unidad.getTitulo(),
                    "el programa «" + programa.getNombre() + "» tiene "
                            + MensajeUtilidad.cantidad(programa.getCantidadInscripcionesActivas(), "inscripción activa",
                                    "inscripciones activas")
                            + " y su cronograma es lo que los alumnos vieron y pagaron al inscribirse", ""));
        }
        return cronograma;
    }

    /**
     * Quita las unidades del cronograma del programa, renumera las que quedan y da de baja las unidades que ya no
     * forman parte del cronograma de ningún programa.
     */
    private void quitarDelCronograma(Programa programa, List<UnidadCronograma> aQuitar) {
        LocalDateTime ahora = LocalDateTime.now();
        for (UnidadCronograma cronograma : aQuitar) {
            Unidad unidad = cronograma.getUnidad();
            programa.getUnidadesCronograma().remove(cronograma);
            unidad.getUnidadesCronograma().remove(cronograma);
            unidadCronogramaRepositorio.delete(cronograma);
        }
        unidadCronogramaRepositorio.flush();

        // Las restantes se renumeran sin huecos, de a una para no repetir un número de orden
        int orden = 1;
        for (UnidadCronograma restante : programa.getUnidadesCronograma().stream()
                .sorted(Comparator.comparingInt(UnidadCronograma::getNumeroOrden)).toList()) {
            if (restante.getNumeroOrden() != orden) {
                restante.setNumeroOrden(orden);
                restante.setUltimaModificacion(ahora);
                unidadCronogramaRepositorio.saveAndFlush(restante);
            }
            orden++;
        }
        for (UnidadCronograma cronograma : aQuitar) {
            Unidad unidad = cronograma.getUnidad();
            if (unidad.getProgramasQueLaIncluyen().isEmpty()) {
                unidad.marcarInactivo();
                unidad.setUltimaModificacion(ahora);
                unidadRepositorio.save(unidad);
            }
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
        boolean participa = participacionDocenteRepositorio.findByCursoAndBajaFalse(curso).stream()
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
        if (titulo.trim().length() > Unidad.LONGITUD_TITULO) {
            throw new IllegalArgumentException(
                    "Error! El título no puede superar los " + Unidad.LONGITUD_TITULO + " caracteres.");
        }
        return titulo.trim();
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (limpia != null && limpia.length() > Unidad.LONGITUD_DESCRIPCION) {
            throw new IllegalArgumentException(
                    "Error! La descripción no puede superar los " + Unidad.LONGITUD_DESCRIPCION + " caracteres.");
        }
        return limpia;
    }

    /** Verifica que el curso no tenga otra unidad con el mismo título (el título es único por curso). */
    private void validarTituloLibre(Curso curso, String titulo, Unidad propia) {
        boolean repetido = unidadRepositorio.findAll().stream()
                .anyMatch(u -> u.getCurso().getIdCurso() == curso.getIdCurso() && u.getTitulo().equalsIgnoreCase(titulo)
                        && (propia == null || u.getIdUnidad() != propia.getIdUnidad()));
        if (repetido) {
            throw new IllegalArgumentException("Error! Ya existe una unidad con ese título en el curso.");
        }
    }
}
