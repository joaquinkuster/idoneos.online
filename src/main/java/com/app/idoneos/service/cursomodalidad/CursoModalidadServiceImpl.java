package com.app.idoneos.service.cursomodalidad;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;
import com.app.idoneos.repository.CursoModalidadRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link CursoModalidad}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CursoModalidadService}.
 */
@Service
public class CursoModalidadServiceImpl implements CursoModalidadService {

    @Autowired
    private CursoModalidadRepository cursoModalidadRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la relación entre curso y modalidad por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<CursoModalidad> buscarPorId(Integer id) {
        return cursoModalidadRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<CursoModalidad> obtenerTodo() {
        return cursoModalidadRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cursoModalidadRepository.existsById(id);
    }

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    @Override
    public List<CursoModalidad> buscarPorCurso(Curso curso) {
        return cursoModalidadRepository.findByCurso(curso);
    }



    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la relación entre curso y modalidad en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public CursoModalidad guardar(CursoModalidad entidad) {
        return cursoModalidadRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public CursoModalidad modificar(CursoModalidad entidad) {
        return cursoModalidadRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(CursoModalidad entidad) {
        cursoModalidadRepository.delete(entidad);
    }
}
