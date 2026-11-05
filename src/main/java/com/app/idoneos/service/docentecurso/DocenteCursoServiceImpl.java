package com.app.idoneos.service.docentecurso;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.service.CrudService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link DocenteCurso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DocenteCursoService}.
 */
@Service
public class DocenteCursoServiceImpl implements DocenteCursoService {

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la participación docente por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<DocenteCurso> buscarPorId(Integer id) {
        return docenteCursoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<DocenteCurso> obtenerTodo() {
        return docenteCursoRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return docenteCursoRepository.existsById(id) &&
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
        return docenteCursoRepository.findByCursoAndBajaFalse(curso);
    }

    /**
     * Busca los registros vigentes de docenteCurso asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de DocenteCurso asociados al Docente indicado
     */
    @Override
    public List<DocenteCurso> buscarPorDocente(Docente docente) {
        return docenteCursoRepository.findByDocenteAndBajaFalse(docente);
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
        return docenteCursoRepository.save(entidad);
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
        return docenteCursoRepository.save(entidad);
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
        docenteCursoRepository.save(entidad);
    }
}
