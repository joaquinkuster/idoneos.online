package com.app.idoneos.servicio.DocenteCurso;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link DocenteCurso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DocenteCursoServicio}.
 */
@Service
public class DocenteCursoServicioImpl implements DocenteCursoServicio, CrudServicio<DocenteCurso> {

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la participación docente por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<DocenteCurso> buscarPorId(Integer id) {
        return docenteCursoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<DocenteCurso> obtenerTodo() {
        return docenteCursoRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return docenteCursoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de docenteCurso asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de DocenteCurso asociados al Curso indicado
     */
    @Override
    public List<DocenteCurso> buscarEquipoPorCurso(Curso curso) {
        return buscarPorCurso(curso).stream().sorted(Comparator.comparing((DocenteCurso d) -> !d.getEsTitular())).toList();
    }

    @Override
    public List<DocenteCurso> buscarPorCurso(Curso curso) {
        return docenteCursoRepositorio.findByCursoAndBajaFalse(curso);
    }

    /**
     * Busca los registros vigentes de docenteCurso asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de DocenteCurso asociados al Docente indicado
     */
    @Override
    public List<DocenteCurso> buscarPorDocente(Docente docente) {
        return docenteCursoRepositorio.findByDocenteAndBajaFalse(docente);
    }

    /**
     * Busca los registros vigentes de docenteCurso asociados a programa.
     *
     * @param programaPorDefecto el registro de Programa asociado
     * @return una lista de DocenteCurso asociados al Programa indicado
     */
    @Override
    public List<DocenteCurso> buscarPorProgramaPorDefecto(Programa programaPorDefecto) {
        return docenteCursoRepositorio.findByProgramaPorDefectoAndBajaFalse(programaPorDefecto);
    }

    /**
     * Busca los registros vigentes de docenteCurso asociados a cohorte.
     *
     * @param cohortePorDefecto el registro de Cohorte asociado
     * @return una lista de DocenteCurso asociados al Cohorte indicado
     */
    @Override
    public List<DocenteCurso> buscarPorCohortePorDefecto(Cohorte cohortePorDefecto) {
        return docenteCursoRepositorio.findByCohortePorDefectoAndBajaFalse(cohortePorDefecto);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la participación docente en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public DocenteCurso guardar(DocenteCurso entidad) {
        return docenteCursoRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public DocenteCurso modificar(DocenteCurso entidad) {
        return docenteCursoRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(DocenteCurso entidad) {
        entidad.marcarInactivo(); // Baja lógica
        docenteCursoRepositorio.save(entidad);
    }
}
