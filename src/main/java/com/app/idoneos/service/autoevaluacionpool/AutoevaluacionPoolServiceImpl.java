package com.app.idoneos.service.autoevaluacionpool;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.AutoevaluacionPool;
import com.app.idoneos.repository.AutoevaluacionPoolRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de AutoevaluacionPoolService. */
@Service
public class AutoevaluacionPoolServiceImpl implements AutoevaluacionPoolService {

    @Autowired
    private AutoevaluacionPoolRepository autoevaluacionPoolRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la relación entre autoevaluación y pool por su identificador. */
    @Override
    public Optional<AutoevaluacionPool> buscarPorId(Integer id) {
        return autoevaluacionPoolRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<AutoevaluacionPool> obtenerTodo() {
        return autoevaluacionPoolRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return autoevaluacionPoolRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la relación entre autoevaluación y pool en la base de datos. */
    @Override
    public AutoevaluacionPool guardar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public AutoevaluacionPool modificar(AutoevaluacionPool entidad) {
        return autoevaluacionPoolRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(AutoevaluacionPool entidad) {
        autoevaluacionPoolRepository.delete(entidad);
    }
}
