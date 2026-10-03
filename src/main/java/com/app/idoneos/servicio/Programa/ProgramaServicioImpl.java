package com.app.idoneos.servicio.Programa;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Programa}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgramaServicio}.
 */
@Service
public class ProgramaServicioImpl implements ProgramaServicio, CrudServicio<Programa> {

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    /**
     * Guarda el programa en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Programa guardar(Programa entidad) {
        return programaRepositorio.save(entidad);
    }

    /**
     * Busca el programa por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Programa> buscarPorId(Integer id) {
        return programaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Programa> obtenerTodo() {
        return programaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Programa modificar(Programa entidad) {
        return programaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Programa entidad) {
        entidad.marcarInactivo(); // Baja lógica
        programaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return programaRepositorio.existsById(id) &&
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
        return programaRepositorio.findByCursoAndBajaFalse(curso);
    }
}
