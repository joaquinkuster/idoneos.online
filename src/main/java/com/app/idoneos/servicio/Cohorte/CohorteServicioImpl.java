package com.app.idoneos.servicio.Cohorte;

import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.modelo.*;
import com.app.idoneos.repositorio.*;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.CohorteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Cohorte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CohorteServicio}.
 */
@Service
public class CohorteServicioImpl implements CohorteServicio, CrudServicio<Cohorte> {

    @Autowired
    private CohorteRepositorio cohorteRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private ParametroRepositorio parametroRepositorio;

    @Autowired
    private EstadoCohorteRepositorio estadoCohorteRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la cohorte por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Cohorte> buscarPorId(Integer id) {
        return cohorteRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Cohorte> obtenerTodo() {
        return cohorteRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cohorteRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    @Override
    public List<Cohorte> buscarPorPrograma(Programa programa) {
        return cohorteRepositorio.findByProgramaAndBajaFalse(programa);
    }

    @Override
    public List<Cohorte> buscarPorCurso(Curso curso) {
        return cohorteRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * Busca cohortes aplicando filtros opcionales. Incluye las dadas de baja, que se ordenan al final.
     *
     * @param idPrograma identificador del programa (opcional)
     * @param texto parte del nombre del curso o del programa (opcional)
     * @param estado estado de la cohorte: Pendiente, Cancelada, Abierta, En dictado, Finalizada, Próxima o Dada de baja (opcional)
     * @param desde fecha desde la que la inscripción debe estar abierta (opcional)
     * @param hasta fecha hasta la que la inscripción debe estar abierta (opcional)
     * @param orden el orden de los resultados: "recientes" (inicio de inscripción más nuevo primero, por defecto)
     *              o "curso" (nombre del curso A–Z); las dadas de baja van siempre al final
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que el docente participa
     * @return una lista de cohortes que cumplen los criterios
     */
    @Override
    public List<Cohorte> buscarConFiltros(Integer idPrograma, String texto, String estado, LocalDate desde,
            LocalDate hasta, String orden, Docente soloDeDocente) {
        String criterio = texto == null ? "" : texto.trim().toLowerCase();
        return cohorteRepositorio.findAll().stream()
                .filter(cohorte -> idPrograma == null
                        || (cohorte.getPrograma() != null && cohorte.getPrograma().getIdPrograma() == idPrograma))
                .filter(cohorte -> criterio.isEmpty()
                        || (cohorte.getPrograma() != null && cohorte.getPrograma().getNombre().toLowerCase().contains(criterio))
                        || cohorte.getCurso().getNombre().toLowerCase().contains(criterio))
                .filter(cohorte -> coincideEstado(cohorte, estado))
                .filter(cohorte -> desde == null || !cohorte.getFechaFinInscripcion().toLocalDate().isBefore(desde))
                .filter(cohorte -> hasta == null || !cohorte.getFechaInicioInscripcion().toLocalDate().isAfter(hasta))
                .filter(cohorte -> soloDeDocente == null || cohorte.getCurso().tieneDocente(soloDeDocente))
                .sorted(Comparator.comparing(Cohorte::esInactivo)
                        .thenComparing("curso".equals(orden)
                                ? Comparator.comparing((Cohorte cohorte) -> cohorte.getCurso().getNombre(),
                                        String.CASE_INSENSITIVE_ORDER)
                                        .thenComparing(Cohorte::getFechaInicioInscripcion, Comparator.reverseOrder())
                                : Comparator.comparing(Cohorte::getFechaInicioInscripcion, Comparator.reverseOrder())))
                .toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la cohorte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Cohorte guardar(Cohorte entidad) {
        return cohorteRepositorio.save(entidad);
    }

    /**
     * Registra una cohorte para un programa activo. Si el contenido del programa está completo, la cohorte nace Activa;
     * si no, nace Pendiente y la inscripción debe empezar con el plazo mínimo para que el docente lo complete.
     *
     * @param idPrograma el identificador del programa
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    @Override
    @Transactional
    public Cohorte registrarCohorte(Integer idPrograma, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer accesoDias, Integer cupoMaximo) {
        Programa programa = programaRepositorio.findById(idPrograma).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        return registrarCohorte(programa.getCurso().getIdCurso(), idPrograma, fechaInicioInscripcion, fechaFinInscripcion,
                fechaInicioDictado, fechaFinDictado, accesoDias, cupoMaximo);
    }

    /**
     * Registra una cohorte de un curso activo, con o sin programa. Sin programa (o con el contenido incompleto) la cohorte
     * nace Pendiente, hasta que el docente complete el contenido; con el contenido completo nace Activa.
     *
     * @param idCurso el identificador del curso
     * @param idPrograma el identificador del programa (opcional; debe ser del curso)
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    @Override
    @Transactional
    public Cohorte registrarCohorte(Integer idCurso, Integer idPrograma, LocalDate fechaInicioInscripcion,
            LocalDate fechaFinInscripcion, LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer accesoDias,
            Integer cupoMaximo) {
        Curso curso = cursoRepositorio.findById(idCurso).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        Programa programa = idPrograma == null ? null : programaDelCurso(curso, idPrograma);
        boolean enVivo = curso.incluyeModalidad(Modalidad.EN_VIVO);
        validarDatosCohorte(programa, enVivo, fechaInicioInscripcion, fechaFinInscripcion, fechaInicioDictado,
                fechaFinDictado, accesoDias, cupoMaximo);

        boolean activa = tieneContenidoCompleto(programa);
        if (!activa) {
            validarPlazoParaCompletarContenido(programa, fechaInicioInscripcion);
        }
        Cohorte cohorte = new Cohorte(curso, programa, inicioDelDia(fechaInicioInscripcion), finDelDia(fechaFinInscripcion),
                accesoDias);
        aplicarDatosOpcionales(cohorte, enVivo, fechaInicioDictado, fechaFinDictado, cupoMaximo);
        cohorte.setEstadoCohorte(estadoCohorteRepositorio
                .findByNombre(activa ? EstadoCohorte.ACTIVA : EstadoCohorte.PENDIENTE).orElseThrow());
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Devuelve qué le falta al contenido del programa para que una cohorte pueda estar Activa. Sin programa, falta elegirlo.
     *
     * @param programa el programa (puede ser {@code null})
     * @return los faltantes; vacío si el contenido está completo
     */
    @Override
    public List<String> faltantesDeContenido(Programa programa) {
        if (programa == null) {
            return List.of("Todavía no tiene un programa: el docente debe elegir o crear uno y cargar su contenido");
        }
        return programa.getFaltantesDeContenido(minimoTemasConMaterial());
    }

    /**
     * Días que como mínimo debe haber entre hoy y el inicio de la inscripción de una cohorte pendiente.
     *
     * @return el plazo mínimo en días
     */
    @Override
    public int plazoMinimoParaCompletarContenidoDias() {
        return parametroRepositorio.findByClave(Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS)
                .map(parametro -> Integer.parseInt(parametro.getValor().trim()))
                .orElse(Parametro.COHORTE_PLAZO_MINIMO_CONTENIDO_DIAS_POR_DEFECTO);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Cohorte modificar(Cohorte entidad) {
        return cohorteRepositorio.save(entidad);
    }

    /**
     * Modifica una cohorte activa sin inscripciones activas asociadas.
     *
     * @param idCohorte el identificador de la cohorte
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param accesoDias los días de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    @Override
    @Transactional
    public Cohorte modificarCohorte(Integer idCohorte, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer accesoDias, Integer cupoMaximo) {
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
        if (cohorte.esCancelada()) {
            throw new IllegalArgumentException("Error! La cohorte está cancelada y no puede modificarse.");
        }
        if (cohorte.getCantidadInscriptos() > 0) {
            throw new IllegalArgumentException("Error! La cohorte tiene inscripciones activas asociadas y no puede modificarse.");
        }
        boolean enVivo = cohorte.getCurso().incluyeModalidad(Modalidad.EN_VIVO);
        validarDatosCohorte(cohorte.getPrograma(), enVivo, fechaInicioInscripcion, fechaFinInscripcion,
                fechaInicioDictado, fechaFinDictado, accesoDias, cupoMaximo);
        if (cohorte.esPendiente()) {
            validarPlazoParaCompletarContenido(cohorte.getPrograma(), fechaInicioInscripcion);
        }

        cohorte.setFechaInicioInscripcion(inicioDelDia(fechaInicioInscripcion));
        cohorte.setFechaFinInscripcion(finDelDia(fechaFinInscripcion));
        cohorte.setAccesoDias(accesoDias);
        aplicarDatosOpcionales(cohorte, enVivo, fechaInicioDictado, fechaFinDictado, cupoMaximo);
        cohorte.setUltimaModificacion(LocalDateTime.now());
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Asigna el programa a una cohorte pendiente. Si el contenido del programa ya está completo, la cohorte pasa a Activa.
     *
     * @param idCohorte el identificador de la cohorte
     * @param idPrograma el identificador del programa (debe ser del curso de la cohorte)
     * @param docente el docente que asigna el programa (debe participar en el curso)
     * @return la cohorte con el programa asignado
     * @throws IllegalArgumentException si la cohorte no está pendiente o el programa no es válido
     */
    @Override
    @Transactional
    public Cohorte asignarPrograma(Integer idCohorte, Integer idPrograma, Docente docente) {
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
        if (docente != null && !cohorte.getCurso().tieneDocente(docente)) {
            throw new IllegalArgumentException("Error! El docente no participa en el curso de la cohorte.");
        }
        if (!cohorte.esPendiente()) {
            throw new IllegalArgumentException("Error! Solo se puede asignar el programa a una cohorte pendiente.");
        }
        if (cohorte.getCantidadInscriptos() > 0) {
            throw new IllegalArgumentException("Error! La cohorte tiene inscripciones activas asociadas y no puede modificarse.");
        }
        Programa programa = programaDelCurso(cohorte.getCurso(), idPrograma);
        int duracion = programa.getDuracionTotalDias();
        if (cohorte.getAccesoDias() < duracion) {
            throw RespuestaFormularioUtilidad.campoInvalido("programaId", "Error! Los días de acceso de la cohorte ("
                    + cohorte.getAccesoDias() + ") no pueden ser menores a la duración total del cronograma del programa ("
                    + duracion + " días).");
        }
        cohorte.setPrograma(programa);
        if (tieneContenidoCompleto(programa)) {
            cohorte.setEstadoCohorte(estadoCohorteRepositorio.findByNombre(EstadoCohorte.ACTIVA).orElseThrow());
        }
        cohorte.setUltimaModificacion(LocalDateTime.now());
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Cambia el contexto de trabajo de un docente: establece la cohorte (y su programa) como la de trabajo
     * por defecto en el curso al que pertenece.
     *
     * @param cohorte la cohorte sobre la que el docente va a trabajar
     * @param docente el docente que cambia de contexto
     * @throws IllegalArgumentException si el docente no participa en el curso de la cohorte
     */
    @Override
    @Transactional
    public void cambiarContextoDeTrabajo(Cohorte cohorte, Docente docente) {
        if (!cohorte.tienePrograma()) {
            throw new IllegalArgumentException("Error! La cohorte todavía no tiene un programa para trabajar.");
        }
        DocenteCurso participacion = docenteCursoRepositorio
                .findByCursoAndBajaFalse(cohorte.getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! El docente no participa en el curso de la cohorte."));
        participacion.setProgramaPorDefecto(cohorte.getPrograma());
        participacion.setCohortePorDefecto(cohorte);
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
    public void borrar(Cohorte entidad) {
        entidad.marcarInactivo(); // Baja lógica
        cohorteRepositorio.save(entidad);
    }

    /**
     * Da de baja una cohorte activa que no tenga inscripciones ni clases en vivo activas, y la desvincula
     * de los docentes que la tienen asignada por defecto.
     *
     * @param idCohorte el identificador de la cohorte
     * @throws IllegalArgumentException si la cohorte no está activa o tiene inscripciones o clases en vivo activas
     */
    @Override
    @Transactional
    public void darDeBajaCohorte(Integer idCohorte) {
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
        long clasesActivas = cohorte.getClasesEnVivo().stream().filter(clase -> !clase.getBaja()).count();
        long inscriptos = cohorte.getCantidadInscriptos();
        if (inscriptos > 0 || clasesActivas > 0) {
            String motivo = (inscriptos > 0
                    ? MensajeErrorUtilidad.cantidad(inscriptos, "alumno inscripto activo", "alumnos inscriptos activos") : "")
                    + (inscriptos > 0 && clasesActivas > 0 ? " y " : "")
                    + (clasesActivas > 0
                            ? MensajeErrorUtilidad.cantidad(clasesActivas, "clase en vivo activa", "clases en vivo activas") : "");
            // Las inscripciones no se pueden deshacer: solo si hay clases en vivo se indica qué hacer
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("la cohorte",
                    cohorte.getCurso().getNombre() + (cohorte.tienePrograma() ? " - " + cohorte.getPrograma().getNombre() : ""),
                    "tiene " + motivo, inscriptos == 0 ? "Dé de baja primero sus clases en vivo." : ""));
        }
        for (DocenteCurso participacion : new ArrayList<>(cohorte.getParticipacionesDocente())) {
            participacion.setCohortePorDefecto(null);
            docenteCursoRepositorio.save(participacion);
        }
        cohorte.marcarInactivo();
        cohorte.setUltimaModificacion(LocalDateTime.now());
        cohorteRepositorio.save(cohorte);
    }

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno y el mensaje indica cuál lo impidió.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un registro.");
        }
        for (Integer id : ids) {
            try {
                darDeBajaCohorte(id);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }

    private boolean coincideEstado(Cohorte cohorte, String estado) {
        if (estado == null || estado.isBlank()) return true;
        String criterio = estado.trim().toLowerCase();
        return switch (criterio) {
            case "baja", "dada de baja", "inactivas" -> cohorte.esInactivo();
            case "activas" -> !cohorte.esInactivo();
            default -> !cohorte.esInactivo() && cohorte.getEstado().toLowerCase().equals(criterio);
        };
    }

    private int minimoTemasConMaterial() {
        return parametroRepositorio.findByClave(Parametro.MINIMO_TEMAS_CON_MATERIAL)
                .map(parametro -> Integer.parseInt(parametro.getValor().trim())).orElse(1);
    }

    private boolean tieneContenidoCompleto(Programa programa) {
        return programa != null && programa.tieneContenidoCompleto(minimoTemasConMaterial());
    }

    private Programa programaDelCurso(Curso curso, Integer idPrograma) {
        Programa programa = programaRepositorio.findById(idPrograma).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        if (programa.getCurso().getIdCurso() != curso.getIdCurso()) {
            throw RespuestaFormularioUtilidad.campoInvalido("programaId", "Error! El programa no pertenece al curso de la cohorte.");
        }
        return programa;
    }

    /** Una cohorte pendiente necesita tiempo para que el docente complete el contenido antes de abrir la inscripción. */
    private void validarPlazoParaCompletarContenido(Programa programa, LocalDate fechaInicioInscripcion) {
        int plazo = plazoMinimoParaCompletarContenidoDias();
        if (fechaInicioInscripcion.isBefore(LocalDate.now().plusDays(plazo))) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaInicioInscripcion",
                    "Error! La fecha de inicio de inscripción debe ser al menos " + MensajeErrorUtilidad.cantidad(plazo, "día", "días")
                            + " posterior a hoy, porque el contenido del programa todavía no está completo.");
        }
    }

    private LocalDateTime inicioDelDia(LocalDate fecha) {
        return fecha.atStartOfDay();
    }

    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(LocalTime.of(23, 59, 59));
    }

    private void aplicarDatosOpcionales(Cohorte cohorte, boolean enVivo, LocalDate fechaInicioDictado,
            LocalDate fechaFinDictado, Integer cupoMaximo) {
        cohorte.setFechaInicioDictado(enVivo ? inicioDelDia(fechaInicioDictado) : null);
        cohorte.setFechaFinDictado(enVivo ? finDelDia(fechaFinDictado) : null);
        cohorte.setCupoMaximo(cupoMaximo);
    }

    private void validarDatosCohorte(Programa programa, boolean enVivo, LocalDate fechaInicioInscripcion,
            LocalDate fechaFinInscripcion, LocalDate fechaInicioDictado, LocalDate fechaFinDictado,
            Integer accesoDias, Integer cupoMaximo) {
        List<String> faltantes = new ArrayList<>();
        if (fechaInicioInscripcion == null) faltantes.add("fecha de inicio de inscripción");
        if (fechaFinInscripcion == null) faltantes.add("fecha de fin de inscripción");
        if (accesoDias == null) faltantes.add("días de acceso");
        if (enVivo && fechaInicioDictado == null) faltantes.add("fecha de inicio de dictado");
        if (enVivo && fechaFinDictado == null) faltantes.add("fecha de fin de dictado");
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Error! Faltan completar los campos obligatorios: "
                    + String.join(", ", faltantes) + ".");
        }
        if (!fechaFinInscripcion.isAfter(fechaInicioInscripcion)) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaFinInscripcion", "Error! La fecha de fin de inscripción debe ser posterior a la fecha de inicio.");
        }
        if (enVivo && !fechaFinDictado.isAfter(fechaInicioDictado)) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaFinDictado", "Error! La fecha de fin de dictado debe ser posterior a la fecha de inicio.");
        }
        // La inscripción cierra al final del día de su fecha de fin: el dictado empieza al menos el día siguiente
        if (enVivo && !fechaInicioDictado.isAfter(fechaFinInscripcion)) {
            throw RespuestaFormularioUtilidad.campoInvalido("fechaInicioDictado", "Error! La fecha de inicio de dictado debe ser posterior a la fecha de fin de inscripción.");
        }
        if (cupoMaximo != null && cupoMaximo <= 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("cupoMaximo", MensajeErrorUtilidad.formatoInvalido("El cupo máximo", "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO));
        }
        if (cupoMaximo != null && cupoMaximo > Cohorte.CUPO_MAXIMO_PERMITIDO) {
            throw RespuestaFormularioUtilidad.campoInvalido("cupoMaximo", MensajeErrorUtilidad.formatoInvalido("El cupo máximo", "número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO));
        }
        if (accesoDias <= 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("accesoDias", MensajeErrorUtilidad.formatoInvalido("Los días de acceso", "número entero de 1 a " + Cohorte.DIAS_ACCESO_MAXIMOS));
        }
        if (accesoDias > Cohorte.DIAS_ACCESO_MAXIMOS) {
            throw RespuestaFormularioUtilidad.campoInvalido("accesoDias", MensajeErrorUtilidad.formatoInvalido("Los días de acceso", "número entero de 1 a " + Cohorte.DIAS_ACCESO_MAXIMOS));
        }
        int duracion = programa == null ? 0 : programa.getDuracionTotalDias();
        if (accesoDias < duracion) {
            throw RespuestaFormularioUtilidad.campoInvalido("accesoDias", "Error! Los días de acceso (" + accesoDias
                    + ") no pueden ser menores a la duración total del cronograma del programa (" + duracion + " días).");
        }
    }
}
