package com.app.idoneos.service.rolusuario;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.repository.RolUsuarioRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de RolUsuarioService. */
@Service
public class RolUsuarioServiceImpl implements RolUsuarioService {

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la asignación de rol por su identificador. */
    @Override
    public Optional<RolUsuario> buscarPorId(Integer id) {
        return rolUsuarioRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<RolUsuario> obtenerTodo() {
        return rolUsuarioRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return rolUsuarioRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la asignación de rol en la base de datos. */
    @Override
    public RolUsuario guardar(RolUsuario entidad) {
        return rolUsuarioRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public RolUsuario modificar(RolUsuario entidad) {
        return rolUsuarioRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(RolUsuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        rolUsuarioRepository.save(entidad);
    }
}
