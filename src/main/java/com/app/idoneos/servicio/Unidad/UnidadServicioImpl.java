package com.app.idoneos.servicio.Unidad;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Unidad}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link UnidadServicio}.
 */
@Service
public class UnidadServicioImpl implements UnidadServicio, CrudServicio<Unidad> {

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    /**
     * Guarda la unidad en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Unidad guardar(Unidad entidad) {
        return unidadRepositorio.save(entidad);
    }

    /**
     * Busca la unidad por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Unidad> buscarPorId(Integer id) {
        return unidadRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Unidad> obtenerTodo() {
        return unidadRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Unidad modificar(Unidad entidad) {
        return unidadRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Unidad entidad) {
        entidad.marcarInactivo(); // Baja lógica
        unidadRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return unidadRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de unidad asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Unidad asociados al Curso indicado
     */
    @Override
    public List<Unidad> buscarPorCurso(Curso curso) {
        return unidadRepositorio.findByCursoAndBajaFalse(curso);
    }
}
