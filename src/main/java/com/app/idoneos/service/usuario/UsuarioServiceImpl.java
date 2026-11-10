package com.app.idoneos.service.usuario;

import java.time.LocalDateTime;

import com.app.idoneos.model.Rol;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de UsuarioService. */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el usuario por su identificador. */
    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Usuario> obtenerTodo() {
        return usuarioRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return usuarioRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca el usuario por su atributo único 'correo'. */
    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el usuario en la base de datos. */
    @Override
    public Usuario guardar(Usuario entidad) {
        return usuarioRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Usuario modificar(Usuario entidad) {
        return usuarioRepository.save(entidad);
    }

    /** Cambia el rol por defecto de un usuario. */
    @Override
    public Rol cambiarRolPorDefecto(int idUsuario, Integer idRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(entidad -> !entidad.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no se encuentra activo."));
        Rol rol = usuario.getRolesVigentes().stream()
                .filter(vigente -> idRol != null && vigente.getIdRol() == idRol)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no tiene el rol seleccionado."));
        usuario.setRolPorDefecto(rol);
        usuario.setUltimaModificacion(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        return rol;
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Usuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        usuarioRepository.save(entidad);
    }
}
