package com.app.idoneos.servicio.ParticipacionDocente;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ParticipacionDocente}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ParticipacionDocenteServicio}.
 */
@Service
public class ParticipacionDocenteServicioImpl implements ParticipacionDocenteServicio, CrudServicio<ParticipacionDocente> {

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    /**
     * Guarda la participación docente en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ParticipacionDocente guardar(ParticipacionDocente entidad) {
        return participacionDocenteRepositorio.save(entidad);
    }

    /**
     * Busca la participación docente por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ParticipacionDocente> buscarPorId(Integer id) {
        return participacionDocenteRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ParticipacionDocente> obtenerTodo() {
        return participacionDocenteRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ParticipacionDocente modificar(ParticipacionDocente entidad) {
        return participacionDocenteRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ParticipacionDocente entidad) {
        entidad.marcarInactivo(); // Baja lógica
        participacionDocenteRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return participacionDocenteRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de participacionDocente asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de ParticipacionDocente asociados al Curso indicado
     */
    @Override
    public List<ParticipacionDocente> buscarPorCurso(Curso curso) {
        return participacionDocenteRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * Busca los registros vigentes de participacionDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de ParticipacionDocente asociados al Docente indicado
     */
    @Override
    public List<ParticipacionDocente> buscarPorDocente(Docente docente) {
        return participacionDocenteRepositorio.findByDocenteAndBajaFalse(docente);
    }

    /**
     * Busca los registros vigentes de participacionDocente asociados a programa.
     *
     * @param programaPorDefecto el registro de Programa asociado
     * @return una lista de ParticipacionDocente asociados al Programa indicado
     */
    @Override
    public List<ParticipacionDocente> buscarPorProgramaPorDefecto(Programa programaPorDefecto) {
        return participacionDocenteRepositorio.findByProgramaPorDefectoAndBajaFalse(programaPorDefecto);
    }

    /**
     * Busca los registros vigentes de participacionDocente asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de ParticipacionDocente asociados al Cohorte indicado
     */
    @Override
    public List<ParticipacionDocente> buscarPorCohortePorDefecto(Cohorte cohortePorDefecto) {
        return participacionDocenteRepositorio.findByCohortePorDefectoAndBajaFalse(cohortePorDefecto);
    }
}
