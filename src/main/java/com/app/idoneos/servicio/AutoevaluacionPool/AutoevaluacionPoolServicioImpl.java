package com.app.idoneos.servicio.AutoevaluacionPool;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.AutoevaluacionPool;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.repositorio.AutoevaluacionPoolRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link AutoevaluacionPool}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AutoevaluacionPoolServicio}.
 */
@Service
public class AutoevaluacionPoolServicioImpl implements AutoevaluacionPoolServicio, CrudServicio<AutoevaluacionPool> {

    @Autowired
    private AutoevaluacionPoolRepositorio autoevaluacionPoolRepositorio;

    /**
     * Guarda la relación entre autoevaluación y pool en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public AutoevaluacionPool guardar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepositorio.save(entidad);
    }

    /**
     * Busca la relación entre autoevaluación y pool por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<AutoevaluacionPool> buscarPorId(Integer id) {
        return autoevaluacionPoolRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<AutoevaluacionPool> obtenerTodo() {
        return autoevaluacionPoolRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public AutoevaluacionPool modificar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(AutoevaluacionPool entidad) {
        autoevaluacionPoolRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionPoolRepositorio.existsById(id);
    }

    /**
     * Busca los registros de autoevaluacionPool asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de AutoevaluacionPool asociados al Pool indicado
     */
    @Override
    public List<AutoevaluacionPool> buscarPorPool(Pool pool) {
        return autoevaluacionPoolRepositorio.findByPool(pool);
    }

    /**
     * Busca los registros de autoevaluacionPool asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de AutoevaluacionPool asociados al Autoevaluacion indicado
     */
    @Override
    public List<AutoevaluacionPool> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion) {
        return autoevaluacionPoolRepositorio.findByAutoevaluacion(autoevaluacion);
    }

    /**
     * Busca la relación entre autoevaluación y pool a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @param pool el registro de Pool asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<AutoevaluacionPool> buscarPorAutoevaluacionYPool(Autoevaluacion autoevaluacion, Pool pool) {
        return autoevaluacionPoolRepositorio.findByAutoevaluacionAndPool(autoevaluacion, pool);
    }
}
