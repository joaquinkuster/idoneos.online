package com.app.idoneos.service.rol;

import com.app.idoneos.model.Rol;
import com.app.idoneos.repository.RolRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de RolService. */
@Service
public class RolServiceImpl implements RolService {

    @Autowired
    private RolRepository rolRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el rol por su identificador. */
    @Override
    public Optional<Rol> buscarPorId(Integer id) {
        return rolRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Rol> obtenerTodo() {
        return rolRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return rolRepository.existsById(id);
    }

    /** Busca el rol por su atributo único 'nombre'. */
    @Override
    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el rol en la base de datos. */
    @Override
    public Rol guardar(Rol entidad) {
        return rolRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Rol modificar(Rol entidad) {
        return rolRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Rol entidad) {
        rolRepository.delete(entidad);
    }
}
