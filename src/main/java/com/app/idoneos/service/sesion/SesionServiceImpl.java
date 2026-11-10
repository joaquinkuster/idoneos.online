package com.app.idoneos.service.sesion;

import com.app.idoneos.model.Sesion;
import com.app.idoneos.repository.SesionRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de SesionService. */
@Service
public class SesionServiceImpl implements SesionService {

    @Autowired
    private SesionRepository sesionRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la sesión por su identificador. */
    @Override
    public Optional<Sesion> buscarPorId(Integer id) {
        return sesionRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Sesion> obtenerTodo() {
        return sesionRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return sesionRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la sesión en la base de datos. */
    @Override
    public Sesion guardar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Sesion modificar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Sesion entidad) {
        sesionRepository.delete(entidad);
    }
}
