package com.app.idoneos.servicio.Cohorte;

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
    private ParametroRepositorio parametroRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

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

    /**
     * Busca cohortes aplicando filtros opcionales. Incluye las dadas de baja, que se ordenan al final.
     *
     * @param idPrograma identificador del programa (opcional)
     * @param texto parte del nombre del curso o del programa (opcional)
     * @param estado estado de la cohorte: Abierta, En dictado, Finalizada, Próxima o Dada de baja (opcional)
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
                .filter(cohorte -> idPrograma == null || cohorte.getPrograma().getIdPrograma() == idPrograma)
                .filter(cohorte -> criterio.isEmpty()
                        || cohorte.getPrograma().getNombre().toLowerCase().contains(criterio)
                        || cohorte.getPrograma().getCurso().getNombre().toLowerCase().contains(criterio))
                .filter(cohorte -> coincideEstado(cohorte, estado))
                .filter(cohorte -> desde == null || !cohorte.getFechaFinInscripcion().toLocalDate().isBefore(desde))
                .filter(cohorte -> hasta == null || !cohorte.getFechaInicioInscripcion().toLocalDate().isAfter(hasta))
                .filter(cohorte -> soloDeDocente == null || cohorte.getPrograma().getCurso().getEquipoDocente().stream()
                        .anyMatch(p -> p.getDocente().getIdDocente() == soloDeDocente.getIdDocente()))
                .sorted(Comparator.comparing(Cohorte::esInactivo)
                        .thenComparing("curso".equals(orden)
                                ? Comparator.comparing((Cohorte cohorte) -> cohorte.getPrograma().getCurso().getNombre(),
                                        String.CASE_INSENSITIVE_ORDER)
                                        .thenComparing(Cohorte::getFechaInicioInscripcion, Comparator.reverseOrder())
                                : Comparator.comparing(Cohorte::getFechaInicioInscripcion, Comparator.reverseOrder())))
                .toList();
    }

    /**
     * Registra una cohorte para un programa activo.
     *
     * @param idPrograma el identificador del programa
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param semanasAcceso las semanas de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte registrada
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    @Override
    @Transactional
    public Cohorte registrarCohorte(Integer idPrograma, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer semanasAcceso, Integer cupoMaximo) {
        Programa programa = programaRepositorio.findById(idPrograma).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        int minimo = parametroRepositorio.findByClave(Parametro.MINIMO_UNIDADES_CON_MATERIAL)
                .map(parametro -> Integer.parseInt(parametro.getValor().trim())).orElse(1);
        int conMaterial = programa.getCantidadUnidadesConMaterialPublicado();
        if (conMaterial < minimo) {
            throw new IllegalArgumentException("Error! El programa debe tener al menos " + minimo
                    + " unidad(es) con material publicado en su cronograma (tiene " + conMaterial + ").");
        }
        boolean enVivo = programa.getCurso().incluyeModalidad(Modalidad.EN_VIVO);
        validarDatosCohorte(programa, enVivo, fechaInicioInscripcion, fechaFinInscripcion, fechaInicioDictado,
                fechaFinDictado, semanasAcceso, cupoMaximo);

        Cohorte cohorte = new Cohorte(programa, inicioDelDia(fechaInicioInscripcion), finDelDia(fechaFinInscripcion),
                semanasAcceso);
        aplicarDatosOpcionales(cohorte, enVivo, fechaInicioDictado, fechaFinDictado, cupoMaximo);
        return cohorteRepositorio.save(cohorte);
    }

    /**
     * Modifica una cohorte activa sin inscripciones activas asociadas.
     *
     * @param idCohorte el identificador de la cohorte
     * @param fechaInicioInscripcion la fecha de inicio de la inscripción
     * @param fechaFinInscripcion la fecha de fin de la inscripción
     * @param fechaInicioDictado la fecha de inicio del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param fechaFinDictado la fecha de fin del dictado (obligatoria si el curso tiene modalidad En vivo)
     * @param semanasAcceso las semanas de acceso al contenido desde la inscripción
     * @param cupoMaximo el cupo máximo de inscriptos (opcional)
     * @return la cohorte modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    @Override
    @Transactional
    public Cohorte modificarCohorte(Integer idCohorte, LocalDate fechaInicioInscripcion, LocalDate fechaFinInscripcion,
            LocalDate fechaInicioDictado, LocalDate fechaFinDictado, Integer semanasAcceso, Integer cupoMaximo) {
        Cohorte cohorte = cohorteRepositorio.findById(idCohorte).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La cohorte no se encuentra activa."));
        if (cohorte.getCantidadInscriptos() > 0) {
            throw new IllegalArgumentException("Error! La cohorte tiene inscripciones activas asociadas y no puede modificarse.");
        }
        boolean enVivo = cohorte.getPrograma().getCurso().incluyeModalidad(Modalidad.EN_VIVO);
        validarDatosCohorte(cohorte.getPrograma(), enVivo, fechaInicioInscripcion, fechaFinInscripcion,
                fechaInicioDictado, fechaFinDictado, semanasAcceso, cupoMaximo);

        cohorte.setFechaInicioInscripcion(inicioDelDia(fechaInicioInscripcion));
        cohorte.setFechaFinInscripcion(finDelDia(fechaFinInscripcion));
        cohorte.setSemanasAcceso(semanasAcceso);
        aplicarDatosOpcionales(cohorte, enVivo, fechaInicioDictado, fechaFinDictado, cupoMaximo);
        cohorte.setUltimaModificacion(LocalDateTime.now());
        return cohorteRepositorio.save(cohorte);
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
        boolean tieneClases = cohorte.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja());
        if (cohorte.getCantidadInscriptos() > 0 || tieneClases) {
            throw new IllegalArgumentException("Error! La cohorte tiene inscripciones y/o clases en vivo activas asociadas. "
                    + "No puede darse de baja.");
        }
        for (ParticipacionDocente participacion : new ArrayList<>(cohorte.getParticipacionesDocente())) {
            participacion.setCohortePorDefecto(null);
            participacionDocenteRepositorio.save(participacion);
        }
        cohorte.marcarInactivo();
        cohorte.setUltimaModificacion(LocalDateTime.now());
        cohorteRepositorio.save(cohorte);
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
        ParticipacionDocente participacion = participacionDocenteRepositorio
                .findByCursoAndBajaFalse(cohorte.getPrograma().getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! El docente no participa en el curso de la cohorte."));
        participacion.setProgramaPorDefecto(cohorte.getPrograma());
        participacion.setCohortePorDefecto(cohorte);
        participacion.setUltimaModificacion(LocalDateTime.now());
        participacionDocenteRepositorio.save(participacion);
    }

    // ---------------------------------------------------------------- métodos auxiliares

    private boolean coincideEstado(Cohorte cohorte, String estado) {
        if (estado == null || estado.isBlank()) return true;
        String criterio = estado.trim().toLowerCase();
        return switch (criterio) {
            case "baja", "dada de baja", "canceladas", "inactivas" -> cohorte.esInactivo();
            case "activas" -> !cohorte.esInactivo();
            default -> !cohorte.esInactivo() && cohorte.getEstado().toLowerCase().equals(criterio);
        };
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
            Integer semanasAcceso, Integer cupoMaximo) {
        List<String> faltantes = new ArrayList<>();
        if (fechaInicioInscripcion == null) faltantes.add("fecha de inicio de inscripción");
        if (fechaFinInscripcion == null) faltantes.add("fecha de fin de inscripción");
        if (semanasAcceso == null) faltantes.add("semanas de acceso");
        if (enVivo && fechaInicioDictado == null) faltantes.add("fecha de inicio de dictado");
        if (enVivo && fechaFinDictado == null) faltantes.add("fecha de fin de dictado");
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Error! Faltan completar los campos obligatorios: "
                    + String.join(", ", faltantes) + ".");
        }
        if (!fechaFinInscripcion.isAfter(fechaInicioInscripcion)) {
            throw new IllegalArgumentException("Error! La fecha de fin de inscripción debe ser posterior a la fecha de inicio.");
        }
        if (enVivo && !fechaFinDictado.isAfter(fechaInicioDictado)) {
            throw new IllegalArgumentException("Error! La fecha de fin de dictado debe ser posterior a la fecha de inicio.");
        }
        if (enVivo && fechaInicioDictado.isBefore(fechaFinInscripcion)) {
            throw new IllegalArgumentException("Error! La fecha de inicio de dictado no puede ser anterior a la fecha de fin de inscripción.");
        }
        if (cupoMaximo != null && cupoMaximo <= 0) {
            throw new IllegalArgumentException("Error! El cupo máximo debe ser un número entero mayor a cero.");
        }
        int duracion = programa.getDuracionTotalSemanas();
        if (semanasAcceso < duracion) {
            throw new IllegalArgumentException("Error! Las semanas de acceso (" + semanasAcceso
                    + ") no pueden ser menores a la duración total del cronograma del programa (" + duracion + " semanas).");
        }
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
                String nombre = cohorteRepositorio.findById(id).map(c -> c.getPrograma().getCurso().getNombre() + " — " + c.getPrograma().getNombre()).orElse("#" + id);
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. «" + nombre + "»: "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }
}
