package com.app.idoneos.servicio.Programa;

import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.TemaCronogramaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Programa}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgramaServicio}.
 */
@Service
public class ProgramaServicioImpl implements ProgramaServicio, CrudServicio<Programa> {

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private TemaCronogramaRepositorio temaCronogramaRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el programa por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Programa> buscarPorId(Integer id) {
        return programaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Programa> obtenerTodo() {
        return programaRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return programaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de programa asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Programa asociados al Curso indicado
     */
    @Override
    public List<Programa> buscarPorCurso(Curso curso) {
        return programaRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * CU-15: Busca los programas activos de un curso por nombre y estado (los dados de baja no se listan).
     */
    @Override
    public List<Programa> buscarConFiltros(Curso curso, String nombre, String estado) {
        String buscado = TextoUtilidad.normalizar(nombre);
        String estadoBuscado = estado == null ? "" : estado.trim();
        return curso.getProgramas().stream().filter(p -> !p.getBaja())
                .filter(p -> buscado.isEmpty() || TextoUtilidad.normalizar(p.getNombre()).contains(buscado))
                .filter(p -> estadoBuscado.isEmpty() || p.getEstado().equalsIgnoreCase(estadoBuscado))
                .sorted(Comparator.comparingInt(Programa::getIdPrograma).reversed()).toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el programa en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Programa guardar(Programa entidad) {
        return programaRepositorio.save(entidad);
    }

    /**
     * CU-16: Registra un programa para un curso.
     */
    @Override
    @Transactional
    public Programa registrarPrograma(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion, String objetivos, Integer cargaHoraria, String bibliografia) {
        Curso curso = cursoRepositorio.findById(cursoId == null ? -1 : cursoId).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        DocenteCurso participacion = obtenerParticipacion(curso, docente);
        Programa origen = null;
        if (programaOrigenId != null) {
            origen = programaRepositorio.findById(programaOrigenId)
                    .filter(p -> !p.esInactivo() && p.getCurso().getIdCurso() == curso.getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! Debe seleccionar un programa de origen de la lista."));
        }
        Programa programa = new Programa(curso, validarDatos(nombre, objetivos, bibliografia), objetivos.trim(),
                bibliografia.trim());
        programa.setDescripcion(validarDescripcion(descripcion));
        programa.setCargaHorariaTotal(validarCargaHoraria(cargaHoraria));
        programa = programaRepositorio.save(programa);
        curso.getProgramas().add(programa);

        // Se parte del cronograma del programa anterior (temas, orden y semanas), como base editable
        if (origen != null) {
            for (TemaCronograma original : origen.getCronogramaOrdenado()) {
                TemaCronograma copia = temaCronogramaRepositorio.save(new TemaCronograma(programa,
                        original.getTema(), original.getNumeroOrden(), original.getSemanasDuracion()));
                programa.getTemasCronograma().add(copia);
            }
        }
        // Pasa a ser el vigente: el docente que lo registró sigue trabajando sobre él
        participacion.setProgramaPorDefecto(programa);
        participacion.setCohortePorDefecto(null);
        participacion.setUltimaModificacion(LocalDateTime.now());
        docenteCursoRepositorio.save(participacion);
        return programa;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Programa modificar(Programa entidad) {
        return programaRepositorio.save(entidad);
    }

    /**
     * CU-17: Modifica los datos de un programa sin inscripciones activas.
     */
    @Override
    @Transactional
    public Programa modificarPrograma(Integer id, Docente docente, String nombre, String descripcion, String objetivos,
            Integer cargaHoraria, String bibliografia) {
        Programa programa = buscarActivo(id);
        obtenerParticipacion(programa.getCurso(), docente);
        if (programa.getCantidadInscripcionesActivas() > 0) {
            throw new IllegalArgumentException("Error! El programa tiene inscripciones activas: sus datos no pueden "
                    + "modificarse porque son los que el alumno vio y pagó al inscribirse.");
        }
        programa.setNombre(validarDatos(nombre, objetivos, bibliografia));
        programa.setDescripcion(validarDescripcion(descripcion));
        programa.setObjetivos(objetivos.trim());
        programa.setCargaHorariaTotal(validarCargaHoraria(cargaHoraria));
        programa.setBibliografia(bibliografia.trim());
        programa.setUltimaModificacion(LocalDateTime.now());
        return programaRepositorio.save(programa);
    }

    /**
     * Establece el programa por defecto del docente en el curso del programa.
     */
    @Override
    @Transactional
    public void establecerPorDefecto(Docente docente, Integer programaId) {
        Programa programa = buscarActivo(programaId);
        DocenteCurso participacion = obtenerParticipacion(programa.getCurso(), docente);
        participacion.setProgramaPorDefecto(programa);
        participacion.setCohortePorDefecto(null);
        participacion.setUltimaModificacion(LocalDateTime.now());
        docenteCursoRepositorio.save(participacion);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Programa entidad) {
        entidad.marcarInactivo(); // Baja lógica
        programaRepositorio.save(entidad);
    }

    /**
     * CU-18: Da de baja un programa sin cohortes activas.
     */
    @Override
    @Transactional
    public void darDeBajaPrograma(Integer id, Docente docente) {
        Programa programa = validarBaja(id, docente);
        programa.marcarInactivo();
        programa.setUltimaModificacion(LocalDateTime.now());
        programaRepositorio.save(programa);
    }

    /**
     * CU-18: Da de baja varios programas a la vez (todos o ninguno).
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un programa.");
        }
        // Se valida todo antes de dar de baja alguno
        List<Programa> programas = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        LocalDateTime ahora = LocalDateTime.now();
        for (Programa programa : programas) {
            programa.marcarInactivo();
            programa.setUltimaModificacion(ahora);
            programaRepositorio.save(programa);
        }
    }

    /** Verifica que el programa se pueda dar de baja: activo, del curso del docente y sin cohortes activas. */
    private Programa validarBaja(Integer id, Docente docente) {
        Programa programa = buscarActivo(id);
        if (docente != null) {
            obtenerParticipacion(programa.getCurso(), docente);
        }
        if (programa.getCantidadCohortesActivas() > 0) {
            int cohortesActivas = programa.getCantidadCohortesActivas();
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el programa", programa.getNombre(),
                    "tiene " + MensajeErrorUtilidad.cantidad(cohortesActivas, "cohorte activa asociada",
                            "cohortes activas asociadas"),
                    "Dé de baja primero " + (cohortesActivas == 1 ? "esa cohorte." : "esas cohortes.")));
        }
        return programa;
    }

    private Programa buscarActivo(Integer id) {
        return programaRepositorio.findById(id == null ? -1 : id).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
    }

    /** Obtiene la participación del docente en el curso (como titular o ayudante). */
    private DocenteCurso obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida el nombre, los objetivos y la bibliografía, y devuelve el nombre sin espacios sobrantes. */
    private String validarDatos(String nombre, String objetivos, String bibliografia) {
        if (nombre == null || nombre.isBlank() || objetivos == null || objetivos.isBlank() || bibliografia == null
                || bibliografia.isBlank()) {
            throw new IllegalArgumentException("Error! Debe completar el nombre, los objetivos y la bibliografía.");
        }
        if (nombre.trim().length() > Programa.LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", MensajeErrorUtilidad.demasiadoLargo("El nombre", Programa.LONGITUD_NOMBRE, "caracteres"));
        }
        return nombre.trim();
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (limpia != null && limpia.length() > Programa.LONGITUD_DESCRIPCION) {
            throw RespuestaFormularioUtilidad.campoInvalido("descripcion", MensajeErrorUtilidad.demasiadoLargo("La descripción", Programa.LONGITUD_DESCRIPCION, "caracteres"));
        }
        return limpia;
    }

    private Integer validarCargaHoraria(Integer cargaHoraria) {
        if (cargaHoraria != null && cargaHoraria <= 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", MensajeErrorUtilidad.formatoInvalido("La carga horaria", "número entero mayor a cero"));
        }
        return cargaHoraria;
    }
}
