package com.app.idoneos.service.administrador;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.repository.AdministradorRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de AdministradorService. */
@Service
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el administrador por su identificador. */
    @Override
    public Optional<Administrador> buscarPorId(Integer id) {
        return administradorRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Administrador> obtenerTodo() {
        return administradorRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return administradorRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el administrador en la base de datos. */
    @Override
    public Administrador guardar(Administrador entidad) {
        return administradorRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Administrador modificar(Administrador entidad) {
        return administradorRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Administrador entidad) {
        administradorRepository.delete(entidad);
    }
}
