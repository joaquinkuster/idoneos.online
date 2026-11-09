package com.app.idoneos.service.programa;

import java.util.Set;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Tema;
import java.util.Collection;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.repository.TemaCronogramaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.temacronograma.TemaCronogramaService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Programa}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgramaService}.
 */
@Service
public class ProgramaServiceImpl implements ProgramaService {

    @Autowired
    private com.app.idoneos.service.parametro.ParametroService parametroService;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private TemaCronogramaRepository temaCronogramaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.app.idoneos.service.temacronograma.TemaCronogramaService temaCronogramaService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.app.idoneos.service.cohorte.CohorteService cohorteService;

    @Autowired
    private com.app.idoneos.repository.CohorteRepository cohorteRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el programa por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Programa> buscarPorId(Integer id) {
        return programaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Programa> obtenerTodo() {
        return programaRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return programaRepository.existsById(id) &&
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
        return programaRepository.findByCursoAndBajaFalse(curso);
    }

    /**
     * CU-15: Busca los programas activos de un curso por nombre y estado (los dados de baja no se listan).
     */
    @Override
    public List<Programa> buscarConFiltros(Curso curso, String nombre, String estado) {
        String buscado = TextoUtil.normalizar(nombre);
        String estadoBuscado = estado == null ? "" : estado.trim();
        return curso.getProgramas().stream().filter(p -> !p.getBaja())
                .filter(p -> buscado.isEmpty() || TextoUtil.normalizar(p.getNombre()).contains(buscado))
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
        return programaRepository.save(entidad);
    }

    /**
     * CU-16: Registra un programa para un curso.
     */
    @Override
    @Transactional
    public Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion) {
        return registrar(docente, cursoId, programaOrigenId, nombre, descripcion, null, null);
    }

    /**
     * CU-16: Registra un programa con su cronograma de una vez.
     */
    @Override
    @Transactional
    public Programa registrar(Docente docente, Integer cursoId, Integer programaOrigenId, String nombre,
            String descripcion, List<Integer> temaIds, List<String> dias) {
        Curso curso = cursoRepository.findById(cursoId == null ? -1 : cursoId).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        obtenerParticipacion(curso, docente);
        Programa origen = null;
        if (programaOrigenId != null) {
            origen = programaRepository.findById(programaOrigenId)
                    .filter(p -> !p.esInactivo() && p.getCurso().getIdCurso() == curso.getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! Debe seleccionar un programa de origen de la lista."));
        }
        // Sin nombre, la versión se llama "Versión N": las que tuvo el curso (incluidas las dadas de baja) más una
        String nombreFinal = nombre == null || nombre.isBlank() ? "Versión " + (curso.getProgramas().size() + 1) : validarNombre(nombre);
        // Un programa nunca está incompleto: nace con su cronograma entero (o copiando el de un programa, que ya lo tiene) y con el curso listo
        if (!curso.getFaltantesDeDatosAcademicos().isEmpty()) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("registrar", "el programa", nombreFinal,
                    "el curso todavía no tiene completos sus datos (" + String.join(", ", curso.getFaltantesDeDatosAcademicos()).toLowerCase() + ")",
                    "Completá los datos del curso antes."));
        }
        if (temaIds == null && origen == null) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("registrar", "el programa", nombreFinal,
                    "no tiene cronograma", "Elegí los temas y repartí entre ellos la duración del curso."));
        }
        Programa programa = new Programa(curso, nombreFinal);
        programa.setDescripcion(validarDescripcion(descripcion));
        programa.setProgramaAnterior(origen);
        programa = programaRepository.save(programa);
        curso.getProgramas().add(programa);

        // Se parte del cronograma del programa anterior (temas, orden y días), como base editable
        if (origen != null && temaIds == null) {
            for (TemaCronograma original : origen.getCronogramaOrdenado()) {
                TemaCronograma copia = temaCronogramaRepository.save(new TemaCronograma(programa,
                        original.getTema(), original.getNumeroOrden(), original.getDuracionDias()));
                programa.getTemasCronograma().add(copia);
            }
        }
        if (temaIds != null) {
            temaCronogramaService.definirDeProgramaNuevo(docente, programa.getIdPrograma(), temaIds, dias);
        }
        asignarALasCohortesPendientes(programa, docente);
        return programa;
    }

    /**
     * Una cohorte nunca tiene un programa incompleto, así que las cohortes pendientes de contenido del curso no tienen programa: el que
     * acaba de registrarse (que siempre está completo) queda asignado a todas ellas y pasan a inscripción próxima.
     */
    private void asignarALasCohortesPendientes(Programa programa, Docente docente) {
        for (Cohorte cohorte : cohorteRepository.findByCursoAndBajaFalse(programa.getCurso())) {
            if (cohorte.esPendiente() && !cohorte.tienePrograma() && cohorte.getCantidadInscriptos() == 0) {
                cohorteService.asignarPrograma(cohorte.getIdCohorte(), programa.getIdPrograma(), docente);
            }
        }
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-17: Modifica los datos y el cronograma de un programa sin inscripciones activas.
     */
    @Override
    @Transactional
    public Programa modificar(Integer id, Docente docente, String nombre, String descripcion, List<Integer> temaIds,
            List<String> dias) {
        Programa programa = modificar(id, docente, nombre, descripcion);
        if (temaIds != null) {
            temaCronogramaService.modificar(docente, programa.getIdPrograma(), temaIds, dias);
        }
        return programa;
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Programa modificar(Programa entidad) {
        return programaRepository.save(entidad);
    }

    /**
     * CU-17: Modifica los datos de un programa sin inscripciones activas.
     */
    @Override
    @Transactional
    public Programa modificar(Integer id, Docente docente, String nombre, String descripcion) {
        Programa programa = buscarActivo(id);
        obtenerParticipacion(programa.getCurso(), docente);
        if (programa.estaCongelado()) {
            throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("el programa", programa.getNombre(),
                    programa.getMotivoDeCongelado(), MensajeErrorUtil.CIERRE_DISENO));
        }
        programa.setNombre(validarNombre(nombre));
        programa.setDescripcion(validarDescripcion(descripcion));
        programa.setUltimaModificacion(LocalDateTime.now());
        return programaRepository.save(programa);
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
        programaRepository.save(entidad);
    }

    /**
     * CU-18: Da de baja un programa sin cohortes activas.
     */
    @Override
    @Transactional
    public void darDeBaja(Integer id, Docente docente) {
        Programa programa = validarBaja(id, docente);
        programa.marcarInactivo();
        programa.setUltimaModificacion(LocalDateTime.now());
        programaRepository.save(programa);
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
            programaRepository.save(programa);
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
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el programa", programa.getNombre(),
                    "tiene " + MensajeErrorUtil.formatearCantidad(cohortesActivas, "cohorte activa asociada",
                            "cohortes activas asociadas"),
                    "Dé de baja primero " + (cohortesActivas == 1 ? "esa cohorte." : "esas cohortes.")));
        }
        return programa;
    }

    private Programa buscarActivo(Integer id) {
        return programaRepository.findById(id == null ? -1 : id).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
    }

    /** Obtiene la participación del docente en el curso (como titular o ayudante). */
    private DocenteCurso obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida el nombre y lo devuelve sin espacios sobrantes. */
    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Debe completar el nombre del programa.");
        }
        if (nombre.trim().length() > Programa.LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", MensajeErrorUtil.armarDemasiadoLargo("El nombre", Programa.LONGITUD_NOMBRE, "caracteres"));
        }
        return nombre.trim();
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (limpia != null && limpia.length() > Programa.LONGITUD_DESCRIPCION) {
            throw RespuestaFormularioUtil.armarCampoInvalido("descripcion", MensajeErrorUtil.armarDemasiadoLargo("La descripción", Programa.LONGITUD_DESCRIPCION, "caracteres"));
        }
        return limpia;
    }

    private static final int MINIMO_POR_DEFECTO = 1;

    /** El mínimo de temas con material publicado y autoevaluación que exige el sistema. */
    @Override
    public int buscarMinimoDeContenido() {
        return parametroService.obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO);
    }

    /**
     * Indica si una operación dejaría al programa por debajo del mínimo. Solo cuenta si el programa tiene cohortes en
     * curso y si la operación es la que le quita contenido: un programa que ya estaba por debajo y no empeora no se bloquea.
     *
     * @param programa           El programa que se verifica.
     * @param materialesQuitados Los materiales que dejarían de estar publicados.
     * @param temasQuitadas   Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo, o vacío si el programa se mantiene por encima del mínimo.
     */
    @Override
    public Optional<Bloqueo> verificarMinimoDeContenido(Programa programa, Collection<Integer> materialesQuitados,
            Collection<Integer> temasQuitadas) {
        int minimo = buscarMinimoDeContenido();
        int antes = programa.getCantidadTemasConMaterialPublicado();
        int despues = programa.cantidadTemasConMaterialPublicadoSin(materialesQuitados, temasQuitadas);
        if (despues >= antes || despues >= minimo) {
            return Optional.empty();
        }
        return Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Indica si quitar temas del cronograma dejaría al programa con cohortes en curso con menos temas que el mínimo.
     * Cuenta solo temas del cronograma, sin mirar su contenido. Si el programa ya estaba por debajo y la operación no
     * empeora, no se bloquea.
     *
     * @param programa         El programa que se verifica.
     * @param temasQuitadas Los temas que dejarían de formar parte del cronograma.
     * @return El bloqueo (con la cantidad de temas que quedarían), o vacío si el programa conserva el mínimo.
     */
    @Override
    public Optional<Bloqueo> verificarMinimoDeTemas(Programa programa, Collection<Integer> temasQuitadas) {
        int minimo = buscarMinimoDeContenido();
        int antes = programa.getCantidadTemas();
        int despues = (int) programa.getCronogramaOrdenado().stream()
                .filter(cronograma -> !temasQuitadas.contains(cronograma.getTema().getIdTema())).count();
        return despues >= antes || despues >= minimo ? Optional.empty() : Optional.of(new Bloqueo(programa, minimo, despues));
    }

    /**
     * Motivo (sin punto final) por el que no se puede quitar el material de un tema, con la misma oración que usan las
     * autoevaluaciones: es el único visible del tema y su programa tiene cohortes activas.
     *
     * @param tema             El tema del material.
     * @param materialesQuitados Los materiales que dejarían de estar publicados en la operación.
     * @return El motivo, o vacío si el programa se mantiene por encima del mínimo.
     */
    @Override
    public Optional<String> buscarMotivoDeMinimoPorRecurso(Tema tema, Collection<Integer> materialesQuitados) {
        for (Programa programa : tema.getProgramasQueLaIncluyen()) {
            Optional<Bloqueo> bloqueo = verificarMinimoDeContenido(programa, materialesQuitados, java.util.Set.of());
            if (bloqueo.isPresent()) {
                Tema afectada = programa.temasQueDejanDeContarSin(materialesQuitados).stream().findFirst().orElse(tema);
                return Optional.of(MensajeErrorUtil.motivoDisenoIncompleto("es el único material publicado del tema «"
                        + afectada.getTitulo() + "»"));
            }
        }
        return Optional.empty();
    }

    /**
     * Motivo por el que quitar material dejaría incompleto el diseño: el tema pierde su único material publicado, su única clase
     * grabada o su único video clon generado, y algún programa que lo incluye está en curso o tiene inscripciones activas.
     */
    @Override
    public Optional<String> buscarMotivoDeDisenoIncompleto(Tema tema, Collection<Integer> materialesQuitados) {
        // Un programa nunca queda incompleto: alcanza con que algún programa incluya el tema
        if (tema.getProgramasQueLaIncluyen().isEmpty()) {
            return Optional.empty();
        }
        var curso = tema.getCurso();
        var antes = tema.getMaterialesPublicados();
        var despues = antes.stream().filter(m -> !materialesQuitados.contains(m.getIdTemaRecurso())).toList();
        java.util.function.BiPredicate<java.util.List<com.app.idoneos.model.TemaRecurso>, String> tiene = (lista, celda) -> lista.stream().anyMatch(m -> switch (celda) {
            case "clon" -> m.getRecurso().getVideoClon() != null && com.app.idoneos.model.EstadoGeneracion.GENERADO
                    .equals(m.getRecurso().getVideoClon().getEstadoGeneracion().getNombre());
            case "grabada" -> com.app.idoneos.model.Modalidad.GRABADA.equals(m.getModalidadAsociada());
            default -> m.getModalidadAsociada() == null;
        });
        String[][] celdas = {
            {"clon", "video clon generado", "el único video clon generado"},
            {"grabada", "clase grabada", "la única clase grabada publicada"},
            {"material", "material", "el único material publicado"}};
        for (String[] celda : celdas) {
            boolean aplica = switch (celda[0]) {
                case "clon" -> curso.incluyeModalidad(com.app.idoneos.model.Modalidad.CLON_IA);
                case "grabada" -> curso.incluyeModalidad(com.app.idoneos.model.Modalidad.GRABADA);
                default -> true;
            };
            if (aplica && tiene.test(antes, celda[0]) && !tiene.test(despues, celda[0])) {
                return Optional.of(MensajeErrorUtil.motivoDisenoIncompleto("es " + celda[2] + " del tema «" + tema.getTitulo() + "»"));
            }
        }
        return Optional.empty();
    }
}
