package com.app.idoneos.servicio.Inscripcion;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Inscripcion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link InscripcionServicio}.
 */
@Service
public class InscripcionServicioImpl implements InscripcionServicio, CrudServicio<Inscripcion> {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    /**
     * Guarda la inscripción en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Inscripcion guardar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
    }

    /**
     * Busca la inscripción por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Inscripcion> buscarPorId(Integer id) {
        return inscripcionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Inscripcion> obtenerTodo() {
        return inscripcionRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Inscripcion modificar(Inscripcion entidad) {
        return inscripcionRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Inscripcion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        inscripcionRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return inscripcionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    @Override
    public List<Inscripcion> buscarPorCohorte(Cohorte cohorte) {
        return inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte);
    }

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    @Override
    public List<Inscripcion> buscarPorAlumno(Alumno alumno) {
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno);
    }

    /**
     * Busca las inscripciones vigentes de un alumno, filtrando opcionalmente por el nombre del curso
     * y por el estado de la inscripción (Pendiente, En Progreso o Finalizado).
     *
     * @param alumno el alumno dueño de las inscripciones
     * @param nombreCurso parte del nombre del curso (opcional)
     * @param estado el estado de la inscripción (opcional)
     * @return una lista de inscripciones que cumplen los criterios, de la más reciente a la más antigua
     */
    @Override
    public List<Inscripcion> buscarMisCursos(Alumno alumno, String nombreCurso, String estado) {
        String criterio = nombreCurso == null ? "" : nombreCurso.trim().toLowerCase();
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> criterio.isEmpty()
                        || inscripcion.getCurso().getNombre().toLowerCase().contains(criterio))
                .filter(inscripcion -> estado == null || estado.isBlank()
                        || inscripcion.getEstado().equalsIgnoreCase(estado.trim()))
                .sorted(Comparator.comparingInt(Inscripcion::getIdInscripcion).reversed())
                .toList();
    }

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción (CU-27).
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    @Override
    public Inscripcion validarAcceso(int idInscripcion, Alumno alumno) {
        Inscripcion inscripcion = buscarPorId(idInscripcion)
                .filter(i -> i.getAlumno().getIdAlumno() == alumno.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! No tenés una inscripción vigente a este curso."));
        Cohorte cohorte = inscripcion.getCohorte();
        if (Boolean.FALSE.equals(inscripcion.getHabilitado()) || cohorte.esInactivo()) {
            throw new IllegalArgumentException("Error! Tu inscripción a este curso no está habilitada.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(inscripcion.getFechaVencimientoAcceso())) {
            throw new IllegalArgumentException("Error! Tu acceso a este curso venció el "
                    + inscripcion.getFechaVencimientoAcceso().format(FORMATO_FECHA) + ".");
        }
        if (cohorte.getFechaInicioDictado() != null && ahora.isBefore(cohorte.getFechaInicioDictado())) {
            throw new IllegalArgumentException("Error! El dictado de tu cohorte comienza el "
                    + cohorte.getFechaInicioDictado().format(FORMATO_FECHA) + ".");
        }
        return inscripcion;
    }
}
