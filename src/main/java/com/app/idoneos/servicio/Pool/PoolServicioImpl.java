package com.app.idoneos.servicio.Pool;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pool}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PoolServicio}.
 */
@Service
public class PoolServicioImpl implements PoolServicio, CrudServicio<Pool> {

    @Autowired
    private PoolRepositorio poolRepositorio;

    /**
     * Guarda el pool en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pool guardar(Pool entidad) {
        return poolRepositorio.save(entidad);
    }

    /**
     * Busca el pool por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pool> buscarPorId(Integer id) {
        return poolRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pool> obtenerTodo() {
        return poolRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pool modificar(Pool entidad) {
        return poolRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pool entidad) {
        entidad.marcarInactivo(); // Baja lógica
        poolRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return poolRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de pool asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Pool asociados al Unidad indicado
     */
    @Override
    public List<Pool> buscarPorUnidad(Unidad unidad) {
        return poolRepositorio.findByUnidadAndBajaFalse(unidad);
    }
}
