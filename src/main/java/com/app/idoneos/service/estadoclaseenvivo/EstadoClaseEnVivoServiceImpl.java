package com.app.idoneos.service.estadoclaseenvivo;

import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.repository.EstadoClaseEnVivoRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de EstadoClaseEnVivoService. */
@Service
public class EstadoClaseEnVivoServiceImpl implements EstadoClaseEnVivoService {

    @Autowired
    private EstadoClaseEnVivoRepository estadoClaseEnVivoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el estado de la clase en vivo por su identificador. */
    @Override
    public Optional<EstadoClaseEnVivo> buscarPorId(Integer id) {
        return estadoClaseEnVivoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<EstadoClaseEnVivo> obtenerTodo() {
        return estadoClaseEnVivoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return estadoClaseEnVivoRepository.existsById(id);
    }

    /** Busca el estado de la clase en vivo por su atributo único 'nombre'. */
    @Override
    public Optional<EstadoClaseEnVivo> buscarPorNombre(String nombre) {
        return estadoClaseEnVivoRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el estado de la clase en vivo en la base de datos. */
    @Override
    public EstadoClaseEnVivo guardar(EstadoClaseEnVivo entidad) {
        return estadoClaseEnVivoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public EstadoClaseEnVivo modificar(EstadoClaseEnVivo entidad) {
        return estadoClaseEnVivoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(EstadoClaseEnVivo entidad) {
        estadoClaseEnVivoRepository.delete(entidad);
    }
}
