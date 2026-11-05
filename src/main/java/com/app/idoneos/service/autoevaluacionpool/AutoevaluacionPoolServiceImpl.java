package com.app.idoneos.service.autoevaluacionpool;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.AutoevaluacionPool;
import com.app.idoneos.repository.AutoevaluacionPoolRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link AutoevaluacionPool}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AutoevaluacionPoolService}.
 */
@Service
public class AutoevaluacionPoolServiceImpl implements AutoevaluacionPoolService {

    @Autowired
    private AutoevaluacionPoolRepository autoevaluacionPoolRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la relación entre autoevaluación y pool por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<AutoevaluacionPool> buscarPorId(Integer id) {
        return autoevaluacionPoolRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<AutoevaluacionPool> obtenerTodo() {
        return autoevaluacionPoolRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionPoolRepository.existsById(id);
    }




    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la relación entre autoevaluación y pool en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public AutoevaluacionPool guardar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public AutoevaluacionPool modificar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(AutoevaluacionPool entidad) {
        autoevaluacionPoolRepository.delete(entidad);
    }
}
