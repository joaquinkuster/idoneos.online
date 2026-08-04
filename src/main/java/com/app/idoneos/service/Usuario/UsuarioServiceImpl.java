package com.app.idoneos.service.Usuario;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.AlumnoRepository;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Usuario}.
 * Esta clase proporciona las operaciones CRUD básicas (Crear, Leer, Actualizar, Eliminar)
 * para la entidad Usuario, además de funcionalidades adicionales como la verificación de
 * usuarios por correo y la gestión de contraseñas cifradas.
 */
@Service
public class UsuarioServiceImpl implements UsuarioService, CrudService<Usuario> {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuarioRepository.findByCorreoAndBajaFalse(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El usuario con este email ya está registrado");
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .filter(usuario -> !usuario.esInactivo());
    }

    @Override
    public List<Usuario> obtenerTodo() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario modificar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Override
    public void borrar(Usuario usuario) {
        usuario.setBaja(true);
        usuarioRepository.save(usuario);
    }

    @Override
    public boolean existePorId(Integer id) {
        return usuarioRepository.existsById(id) && buscarPorId(id).isPresent();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoAndBajaFalse(correo);
    }

    @Override
    @Transactional
    public Usuario procesarUsuarioOAuth2(String email, String nombre, String apellido, String googleSub) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndBajaFalse(email);
        Usuario usuario;

        if (usuarioOpt.isEmpty()) {
            usuario = new Usuario(nombre, apellido, email, null, RolUsuario.Alumno);
            usuario.setEmailValidado(true);
            usuario.setGoogleId(googleSub);
            usuario = usuarioRepository.save(usuario);

            if (!alumnoRepository.existsById(usuario.getId())) {
                alumnoRepository.save(new Alumno(usuario));
            }
        } else {
            usuario = usuarioOpt.get();
            if (usuario.esAlumno() && !alumnoRepository.existsById(usuario.getId())) {
                alumnoRepository.save(new Alumno(usuario));
            }
        }
        return usuario;
    }
}
