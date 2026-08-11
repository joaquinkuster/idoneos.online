package com.app.idoneos.service.Usuario;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
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

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

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

    // ─── Métodos adicionales ──────────────────────────────────────────────────

    /**
     * Cuenta los administradores activos (baja = false).
     * Usado para validar RN-07: debe quedar al menos 1 admin activo.
     */
    public long contarAdministradoresActivos() {
        return usuarioRepository.findByRolAndBajaFalse(RolUsuario.Administrador).size();
    }

    /**
     * Registra un nuevo Docente: crea el Usuario + el registro Docente en una transacción.
     * CU-74: el alta la realiza el Administrador.
     */
    @Transactional
    public Docente registrarDocente(String nombre, String apellido, String correo,
                                    String telefono, String biografia, Integer aniosExperiencia) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo " + correo + " ya está registrado.");
        }
        // Contraseña temporal — el docente la cambia en su primer login
        String contrasenaTmp = passwordEncoder.encode("Idoneos2026!");
        Usuario usuario = new Usuario(nombre, apellido, correo, null, RolUsuario.Docente);
        usuario.setContrasena(contrasenaTmp);
        usuario.setTelefono(telefono);
        usuario.setEmailValidado(true);
        usuario = usuarioRepository.save(usuario);

        Docente docente = new Docente(usuario);
        docente.setBiografia(biografia);
        docente.setAniosExperiencia(aniosExperiencia != null ? aniosExperiencia : 0);
        return docenteRepository.save(docente);
    }

    /**
     * Registra un nuevo Alumno por parte del Administrador.
     * CU-69.
     */
    @Transactional
    public Usuario registrarAlumno(String nombre, String apellido, String correo, String contrasena) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo " + correo + " ya está registrado.");
        }
        Usuario usuario = new Usuario(nombre, apellido, correo, null, RolUsuario.Alumno);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setEmailValidado(true);
        usuario = usuarioRepository.save(usuario);
        alumnoRepository.save(new Alumno(usuario));
        return usuario;
    }

    /**
     * Registra un nuevo Administrador.
     * CU-69 (variante admin).
     */
    @Transactional
    public Usuario registrarAdministrador(String nombre, String apellido, String correo, String contrasena) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RuntimeException("El correo " + correo + " ya está registrado.");
        }
        Usuario usuario = new Usuario(nombre, apellido, correo, null, RolUsuario.Administrador);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setEmailValidado(true);
        usuario = usuarioRepository.save(usuario);
        administradorRepository.save(new Administrador(usuario));
        return usuario;
    }

    @Transactional
    public Usuario crearUsuarioConRol(String nombre, String apellido, String correo, String contrasena, RolUsuario rol) {
        String passHash = (contrasena != null && !contrasena.isBlank()) ? passwordEncoder.encode(contrasena) : passwordEncoder.encode("123456");
        Usuario usuario = new Usuario(nombre, apellido, correo, passHash, rol);
        usuario.setEmailValidado(true);
        usuario = usuarioRepository.save(usuario);

        if (rol == RolUsuario.Alumno) {
            alumnoRepository.save(new Alumno(usuario));
        } else if (rol == RolUsuario.Docente) {
            docenteRepository.save(new Docente(usuario));
        } else if (rol == RolUsuario.Administrador) {
            administradorRepository.save(new Administrador(usuario));
        }
        return usuario;
    }

    @Transactional
    public void darDeBaja(Integer id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setBaja(!u.getBaja());
            usuarioRepository.save(u);
        });
    }
}
