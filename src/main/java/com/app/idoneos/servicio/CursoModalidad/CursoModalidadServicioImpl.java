package com.app.idoneos.servicio.CursoModalidad;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.CursoModalidad;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.repositorio.CursoModalidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link CursoModalidad}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CursoModalidadServicio}.
 */
@Service
public class CursoModalidadServicioImpl implements CursoModalidadServicio, CrudServicio<CursoModalidad> {

    @Autowired
    private CursoModalidadRepositorio cursoModalidadRepositorio;

    /**
     * Guarda la relación entre curso y modalidad en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public CursoModalidad guardar(CursoModalidad entidad) {
        return cursoModalidadRepositorio.save(entidad);
    }

    /**
     * Busca la relación entre curso y modalidad por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<CursoModalidad> buscarPorId(Integer id) {
        return cursoModalidadRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<CursoModalidad> obtenerTodo() {
        return cursoModalidadRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public CursoModalidad modificar(CursoModalidad entidad) {
        return cursoModalidadRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(CursoModalidad entidad) {
        cursoModalidadRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cursoModalidadRepositorio.existsById(id);
    }

    /**
     * Busca los registros de cursoModalidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de CursoModalidad asociados al Curso indicado
     */
    @Override
    public List<CursoModalidad> buscarPorCurso(Curso curso) {
        return cursoModalidadRepositorio.findByCurso(curso);
    }

    /**
     * Busca los registros de cursoModalidad asociados a modalidad.
     *
     * @param modalidad el registro de Modalidad asociado
     * @return una lista de CursoModalidad asociados al Modalidad indicado
     */
    @Override
    public List<CursoModalidad> buscarPorModalidad(Modalidad modalidad) {
        return cursoModalidadRepositorio.findByModalidad(modalidad);
    }

    /**
     * Busca la relación entre curso y modalidad a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param curso el registro de Curso asociado
     * @param modalidad el registro de Modalidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<CursoModalidad> buscarPorCursoYModalidad(Curso curso, Modalidad modalidad) {
        return cursoModalidadRepositorio.findByCursoAndModalidad(curso, modalidad);
    }
}
