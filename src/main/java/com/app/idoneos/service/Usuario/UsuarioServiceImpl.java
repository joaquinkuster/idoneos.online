package com.app.idoneos.service.Usuario;

import com.app.idoneos.exception.ExcepcionRecursoNoEncontrado;
import com.app.idoneos.exception.ExcepcionValidacion;
import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import com.app.idoneos.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación de servicios para la gestión de usuarios, perfiles y autenticación (CU-75 a CU-88).
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService, CrudService<Usuario> {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AdministradorRepository administradorRepository;
    @Autowired private DictadoDocenteRepository dictadoDocenteRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id).filter(u -> !u.esInactivo());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoAndBajaFalse(correo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodo() {
        return usuarioRepository.findAll().stream().filter(u -> !u.getBaja()).toList();
    }

    /**
     * CU-75 — Registrarse (Auto-registro de Alumno).
     * Reglas de negocio:
     * - Campos obligatorios: nombre, apellido, correo, contraseña (Excepción CU-75, paso 4).
     * - Correo único entre usuarios activos (Excepción CU-75, paso 5).
     * - Asigna rol Alumno y crea la entidad Alumno correspondiente.
     */
    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-75 Excepción paso 4: El correo electrónico es obligatorio.");
        }
        if (usuarioRepository.findByCorreoAndBajaFalse(usuario.getCorreo().trim()).isPresent()) {
            throw new ExcepcionValidacion("CU-75 Excepción paso 5: El correo electrónico ya se encuentra registrado.");
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new ExcepcionValidacion("CU-75 Excepción paso 4: La contraseña es obligatoria.");
        }

        usuario.setCorreo(usuario.getCorreo().trim());
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setRol(RolUsuario.Alumno);
        usuario.setEmailValidado(true);
        usuario.setBaja(false);
        usuario.setFechaRegistro(LocalDateTime.now());

        Usuario guardado = usuarioRepository.save(usuario);
        alumnoRepository.save(new Alumno(guardado));
        return guardado;
    }

    /**
     * CU-78 — Modificar usuario / CU-81 — Editar perfil.
     */
    @Override
    public Usuario modificar(Usuario usuario) {
        Usuario existente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Usuario", "id", usuario.getId()));

        if (existente.getBaja()) {
            throw new ExcepcionValidacion("CU-78 Precondición: No se puede modificar un usuario dado de baja.");
        }

        if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
            existente.setNombre(usuario.getNombre().trim());
        }
        if (usuario.getApellido() != null && !usuario.getApellido().trim().isEmpty()) {
            existente.setApellido(usuario.getApellido().trim());
        }
        if (usuario.getTelefono() != null) {
            existente.setTelefono(usuario.getTelefono().trim());
        }
        return usuarioRepository.save(existente);
    }

    /**
     * CU-79 — Dar de baja usuario.
     * Reglas de negocio:
     * - RN-07: Impide la baja del único administrador activo.
     * - RN-11: Impide la baja de un docente titular con cursos publicados.
     */
    @Override
    public void borrar(Usuario usuario) {
        darDeBaja(usuario.getId());
    }

    public void darDeBaja(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Usuario", "id", id));

        // RN-07: Al menos 1 admin activo
        if (usuario.getRol() == RolUsuario.Administrador && !usuario.getBaja()) {
            long adminsActivos = contarAdministradoresActivos();
            if (adminsActivos <= 1) {
                throw new ExcepcionValidacion("RN-07: No es posible dar de baja al único administrador activo del sistema.");
            }
        }

        // RN-11: Docente titular con cursos publicados
        if (usuario.getRol() == RolUsuario.Docente && !usuario.getBaja() && usuario.getDocente() != null) {
            boolean tieneCursosPublicados = dictadoDocenteRepository.findByDocente(usuario.getDocente()).stream()
                    .anyMatch(dd -> !dd.isEsSupervisor() && dd.getDictado() != null
                            && dd.getDictado().getPrograma() != null
                            && dd.getDictado().getPrograma().getCurso() != null
                            && Boolean.TRUE.equals(dd.getDictado().getPrograma().getCurso().getPublicado()));
            if (tieneCursosPublicados) {
                throw new ExcepcionValidacion("RN-11: No es posible dar de baja a un docente titular con cursos publicados.");
            }
        }

        usuario.setBaja(true);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarAdministradoresActivos() {
        return usuarioRepository.findAdministradoresActivos().size();
    }

    /**
     * CU-82 — Registrar docente.
     */
    @Override
    public Docente registrarDocente(String nombre, String apellido, String correo,
                                    String telefono, String biografia, Integer aniosExperiencia) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ExcepcionValidacion("CU-82 Excepción paso 5: El correo " + correo + " ya se encuentra registrado.");
        }
        Usuario usuario = new Usuario(nombre, apellido, correo, passwordEncoder.encode("Idoneos2026!"), RolUsuario.Docente);
        usuario.setTelefono(telefono);
        usuario.setEmailValidado(true);
        usuario.setBaja(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        Docente docente = new Docente(usuario);
        docente.setBiografia(biografia);
        docente.setAniosExperiencia(aniosExperiencia != null ? aniosExperiencia : 0);
        return docenteRepository.save(docente);
    }

    @Override
    public Usuario registrarAlumno(String nombre, String apellido, String correo, String contrasena) {
        Usuario usuario = new Usuario(nombre, apellido, correo, contrasena, RolUsuario.Alumno);
        return guardar(usuario);
    }

    @Override
    public Usuario registrarAdministrador(String nombre, String apellido, String correo, String contrasena) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ExcepcionValidacion("CU-77 Excepción paso 5: El correo " + correo + " ya se encuentra registrado.");
        }
        Usuario usuario = new Usuario(nombre, apellido, correo, passwordEncoder.encode(contrasena), RolUsuario.Administrador);
        usuario.setEmailValidado(true);
        usuario.setBaja(false);
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);
        administradorRepository.save(new Administrador(usuario));
        return usuario;
    }

    @Override
    public Usuario crearUsuarioConRol(String nombre, String apellido, String correo, String contrasena, RolUsuario rol) {
        if (rol == RolUsuario.Alumno) return registrarAlumno(nombre, apellido, correo, contrasena);
        if (rol == RolUsuario.Administrador) return registrarAdministrador(nombre, apellido, correo, contrasena);
        if (rol == RolUsuario.Docente) return registrarDocente(nombre, apellido, correo, null, null, 0).getUsuario();
        return guardar(new Usuario(nombre, apellido, correo, contrasena, rol));
    }

    @Override
    public Usuario procesarUsuarioOAuth2(String email, String nombre, String apellido, String googleSub) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoAndBajaFalse(email);
        Usuario usuario;

        if (usuarioOpt.isEmpty()) {
            usuario = new Usuario(nombre, apellido, email, null, RolUsuario.Alumno);
            usuario.setEmailValidado(true);
            usuario.setGoogleId(googleSub);
            usuario.setBaja(false);
            usuario.setFechaRegistro(LocalDateTime.now());
            usuario = usuarioRepository.save(usuario);
            alumnoRepository.save(new Alumno(usuario));
        } else {
            usuario = usuarioOpt.get();
        }
        return usuario;
    }

    /**
     * CU-86 — Recuperar contraseña.
     * Genera un token único de recuperación con expiración a 24 horas.
     */
    public String generarTokenRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoAndBajaFalse(correo)
                .orElseThrow(() -> new ExcepcionRecursoNoEncontrado("Usuario", "correo", correo));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuario.setExpiracionToken(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(usuario);
        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorId(Integer id) {
        return usuarioRepository.existsById(id) && buscarPorId(id).isPresent();
    }
}
