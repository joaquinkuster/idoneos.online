package com.app.idoneos.servicio.Autoevaluacion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Autoevaluacion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AutoevaluacionServicio}.
 */
@Service
public class AutoevaluacionServicioImpl implements AutoevaluacionServicio, CrudServicio<Autoevaluacion> {

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    /**
     * Guarda la autoevaluación en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Autoevaluacion guardar(Autoevaluacion entidad) {
        return autoevaluacionRepositorio.save(entidad);
    }

    /**
     * Busca la autoevaluación por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Autoevaluacion> buscarPorId(Integer id) {
        return autoevaluacionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Autoevaluacion> obtenerTodo() {
        return autoevaluacionRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Autoevaluacion modificar(Autoevaluacion entidad) {
        return autoevaluacionRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Autoevaluacion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        autoevaluacionRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de autoevaluacion asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Autoevaluacion asociados al Unidad indicado
     */
    @Override
    public List<Autoevaluacion> buscarPorUnidad(Unidad unidad) {
        return autoevaluacionRepositorio.findByUnidadAndBajaFalse(unidad);
    }
}
