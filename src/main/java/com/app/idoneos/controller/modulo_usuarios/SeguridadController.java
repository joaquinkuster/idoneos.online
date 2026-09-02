package com.app.idoneos.controller.modulo_usuarios;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.service.modulo_usuarios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

/**
 * TRAZABILIDAD — Controller para el Módulo de Usuarios y Seguridad (MOD-F-07).
 *
 * Mapea y conecta directamente las 14 pantallas de Seguridad y Usuarios:
 *   CU-81 a CU-94
 */
@Controller
@RequestMapping("/seguridad")
public class SeguridadController {

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DocenteRepository docenteRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private SesionRepository sesionRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private void agregarUsuarioAlModelo(Model model, Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            model.addAttribute("usuario", (Usuario) auth.getPrincipal());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-81, CU-90, CU-91, CU-92: AUTENTICACIÓN Y ACCESO PÚBLICO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("titulo", "CU-81 - Registrarse | Idóneos Online");
        return "pages/seguridad/cu-81-registrarse";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre,
                                   @RequestParam String apellido,
                                   @RequestParam String email,
                                   @RequestParam(defaultValue = "00000000") String dni,
                                   @RequestParam String contrasena,
                                   RedirectAttributes ra) {
        try {
            if (usuarioRepository.existsByCorreo(email)) {
                ra.addFlashAttribute("error", "El correo electrónico ya se encuentra registrado.");
                return "redirect:/seguridad/registro";
            }
            Rol rolAlumno = rolRepository.findByNombre("Alumno").orElseGet(() -> rolRepository.save(new Rol("Alumno")));
            Usuario u = new Usuario(nombre, apellido, email, passwordEncoder.encode(contrasena), rolAlumno);
            u.setDni(dni);
            Usuario guardado = usuarioRepository.save(u);
            alumnoRepository.save(new Alumno(guardado));

            ra.addFlashAttribute("mensaje", "¡Registro completado exitosamente! Ahora podés iniciar sesión.");
            return "redirect:/seguridad/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/registro";
        }
    }

    @GetMapping("/login")
    public String loginView(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Correo electrónico o contraseña incorrectos.");
        if (logout != null) model.addAttribute("mensaje", "Has cerrado sesión correctamente.");
        model.addAttribute("titulo", "CU-90 - Iniciar sesión | Idóneos Online");
        return "pages/seguridad/cu-90-iniciar-sesion";
    }

    @GetMapping("/logout")
    public String logoutView(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("titulo", "CU-91 - Cerrar sesión | Idóneos Online");
        return "pages/seguridad/cu-91-cerrar-sesion";
    }

    @Autowired private EmailService emailService;

    @Value("${idoneos.app.base-url:http://localhost:8080}")
    private String baseUrl;

    @GetMapping("/recuperar-contrasena")
    public String recuperarContrasenaForm(Model model) {
        model.addAttribute("titulo", "CU-92 - Recuperar contraseña | Idóneos Online");
        return "pages/seguridad/cu-92-recuperar-contrasena";
    }

    @PostMapping("/recuperar-contrasena")
    public String procesarRecuperarContrasena(@RequestParam String email, RedirectAttributes ra) {
        Optional<Usuario> uOpt = usuarioRepository.findByCorreoAndBajaFalse(email);
        if (uOpt.isPresent()) {
            Usuario u = uOpt.get();
            String token = UUID.randomUUID().toString();
            u.setTokenRecuperacion(token);
            u.setExpiracionToken(LocalDateTime.now().plusHours(2));
            usuarioRepository.save(u);

            String linkReset = baseUrl + "/seguridad/reset-contrasena?token=" + token;
            String htmlBody = "<html><body style=\"font-family: Arial, sans-serif; padding: 20px;\">"
                    + "<h3>Recuperación de Contraseña — Idóneos Online</h3>"
                    + "<p>Hola <strong>" + u.getNombre() + "</strong>,</p>"
                    + "<p>Recibimos una solicitud para restablecer tu contraseña. Para crear una nueva clave, hacé clic en el botón:</p>"
                    + "<p><a href=\"" + linkReset + "\" style=\"background:#1a56db;color:#fff;padding:12px 24px;text-decoration:none;border-radius:6px;font-weight:bold;display:inline-block;\">Restablecer Contraseña</a></p>"
                    + "<p style=\"color:#666;font-size:13px;\">Este enlace tiene una validez de 2 horas. Si no solicitaste este cambio, podés ignorar este correo.</p>"
                    + "</body></html>";

            emailService.enviar(u.getCorreo(), "Recuperá tu contraseña — Idóneos Online", htmlBody);
            ra.addFlashAttribute("mensaje", "Se ha enviado un enlace de recuperación a tu casilla de correo.");
        } else {
            ra.addFlashAttribute("mensaje", "Si el correo existe en el sistema, recibirás instrucciones para restablecer tu contraseña.");
        }
        return "redirect:/seguridad/login";
    }

    @GetMapping("/reset-contrasena")
    public String resetContrasenaForm(@RequestParam String token, Model model) {
        Optional<Usuario> uOpt = usuarioRepository.findByTokenRecuperacion(token);
        if (uOpt.isEmpty() || uOpt.get().getExpiracionToken() == null || uOpt.get().getExpiracionToken().isBefore(LocalDateTime.now())) {
            model.addAttribute("tokenInvalido", true);
        } else {
            model.addAttribute("token", token);
            model.addAttribute("tokenInvalido", false);
        }
        model.addAttribute("titulo", "Restablecer Contraseña | Idóneos Online");
        return "pages/seguridad/reset-contrasena";
    }

    @PostMapping("/reset-contrasena")
    public String procesarResetContrasena(@RequestParam String token,
                                          @RequestParam String contrasena,
                                          RedirectAttributes ra) {
        Optional<Usuario> uOpt = usuarioRepository.findByTokenRecuperacion(token);
        if (uOpt.isEmpty() || uOpt.get().getExpiracionToken() == null || uOpt.get().getExpiracionToken().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("error", "El enlace de recuperación es inválido o ha expirado.");
            return "redirect:/seguridad/recuperar-contrasena";
        }

        Usuario u = uOpt.get();
        u.setContrasena(passwordEncoder.encode(contrasena));
        u.setTokenRecuperacion(null);
        u.setExpiracionToken(null);
        usuarioRepository.save(u);

        ra.addFlashAttribute("mensaje", "¡Tu contraseña se actualizó correctamente! Ya podés iniciar sesión.");
        return "redirect:/seguridad/login";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-82 a CU-85: GESTIÓN DE USUARIOS (ADMINISTRADOR)
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/usuarios")
    public String buscarUsuarios(@RequestParam(value = "busqueda", required = false) String busqueda,
                                 Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        List<Usuario> todos = usuarioRepository.findAll();
        List<Usuario> usuarios = (busqueda != null && !busqueda.isBlank())
                ? todos.stream().filter(u -> u.getNombreCompleto().toLowerCase().contains(busqueda.toLowerCase()) || u.getCorreo().toLowerCase().contains(busqueda.toLowerCase())).toList()
                : todos;

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("titulo", "Buscar usuario | Idóneos Online");
        return "pages/seguridad/cu-82-buscar-usuario";
    }

    @GetMapping("/usuarios/nuevo")
    public String registrarUsuarioForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("titulo", "CU-83 - Registrar usuario | Idóneos Online");
        return "pages/seguridad/cu-83-registrar-usuario";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@RequestParam String nombre,
                                 @RequestParam String apellido,
                                 @RequestParam String email,
                                 @RequestParam(defaultValue = "00000000") String dni,
                                 @RequestParam String contrasena,
                                 @RequestParam Integer rolId,
                                 RedirectAttributes ra) {
        try {
            if (usuarioRepository.existsByCorreo(email)) {
                ra.addFlashAttribute("error", "El correo ya está en uso.");
                return "redirect:/seguridad/usuarios/nuevo";
            }
            Rol rol = rolRepository.findById(rolId).orElseGet(() -> rolRepository.findAll().get(0));
            Usuario u = new Usuario(nombre, apellido, email, passwordEncoder.encode(contrasena), rol);
            u.setDni(dni);
            Usuario guardado = usuarioRepository.save(u);

            if ("Docente".equalsIgnoreCase(rol.getNombre())) {
                docenteRepository.save(new Docente(guardado));
            } else if ("Alumno".equalsIgnoreCase(rol.getNombre())) {
                alumnoRepository.save(new Alumno(guardado));
            }

            ra.addFlashAttribute("mensaje", "Usuario registrado exitosamente.");
            return "redirect:/seguridad/usuarios";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/usuarios/nuevo";
        }
    }

    @GetMapping("/usuarios/{id}/editar")
    public String modificarUsuarioForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) return "redirect:/seguridad/usuarios";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("usuarioEditar", uOpt.get());
        model.addAttribute("roles", rolRepository.findAll());
        model.addAttribute("titulo", "CU-84 - Modificar usuario | Idóneos Online");
        return "pages/seguridad/cu-84-modificar-usuario";
    }

    @PostMapping("/usuarios/{id}/editar")
    public String actualizarUsuario(@PathVariable Integer id,
                                    @RequestParam String nombre,
                                    @RequestParam String apellido,
                                    @RequestParam(defaultValue = "00000000") String dni,
                                    @RequestParam(required = false) Integer rolId,
                                    RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setDni(dni);
            if (rolId != null) {
                Rol rol = rolRepository.findById(rolId).orElse(null);
                if (rol != null) u.setRol(rol);
            }
            usuarioRepository.save(u);
            ra.addFlashAttribute("mensaje", "Usuario actualizado con éxito.");
            return "redirect:/seguridad/usuarios";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/usuarios/" + id + "/editar";
        }
    }

    @GetMapping("/usuarios/{id}/baja")
    public String darDeBajaUsuarioView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Usuario> uOpt = usuarioRepository.findById(id);
        if (uOpt.isEmpty()) return "redirect:/seguridad/usuarios";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("usuarioBaja", uOpt.get());
        model.addAttribute("titulo", "CU-85 - Dar de baja usuario | Idóneos Online");
        return "pages/seguridad/cu-85-dar-de-baja-usuario";
    }

    @PostMapping("/usuarios/{id}/baja")
    public String eliminarUsuario(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepository.findById(id).orElse(null);
            if (u != null) {
                u.setBaja(true);
                usuarioRepository.save(u);
            }
            ra.addFlashAttribute("mensaje", "Usuario dado de baja exitosamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seguridad/usuarios";
    }

    // ─────────────────────────────────────────────────────────────
    // CU-86 & CU-87: PERFIL DEL USUARIO
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/perfil")
    public String verPerfil(Model model, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/seguridad/login";
        Usuario u = (Usuario) auth.getPrincipal();
        model.addAttribute("usuario", u);
        model.addAttribute("titulo", "CU-86 - Mi Perfil | Idóneos Online");
        return "pages/seguridad/cu-86-ver-perfil";
    }

    @GetMapping("/perfil/editar")
    public String editarPerfilForm(Model model, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/seguridad/login";
        Usuario u = (Usuario) auth.getPrincipal();
        model.addAttribute("usuario", u);
        model.addAttribute("titulo", "CU-87 - Editar perfil | Idóneos Online");
        return "pages/seguridad/cu-87-editar-perfil";
    }

    @PostMapping("/perfil/editar")
    public String actualizarPerfil(@RequestParam String nombre,
                                   @RequestParam String apellido,
                                   @RequestParam(required = false) String telefono,
                                   Authentication auth, RedirectAttributes ra) {
        if (auth == null || !(auth.getPrincipal() instanceof Usuario)) return "redirect:/seguridad/login";
        Usuario u = (Usuario) auth.getPrincipal();
        try {
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setTelefono(telefono);
            usuarioRepository.save(u);
            ra.addFlashAttribute("mensaje", "Perfil actualizado con éxito.");
            return "redirect:/seguridad/perfil";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/perfil/editar";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-88 & CU-89: GESTIÓN DE DOCENTES
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/docentes/nuevo")
    public String registrarDocenteForm(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("usuarios", usuarioRepository.findByBajaFalse().stream().filter(u -> !u.esDocente()).toList());
        model.addAttribute("titulo", "CU-88 - Registrar docente | Idóneos Online");
        return "pages/seguridad/cu-88-registrar-docente";
    }

    @PostMapping("/docentes/guardar")
    public String guardarDocente(@RequestParam Integer usuarioId,
                                 @RequestParam int aniosExperiencia,
                                 @RequestParam(required = false) String matriculaCnv,
                                 @RequestParam(required = false) String biografia,
                                 RedirectAttributes ra) {
        try {
            Usuario u = usuarioRepository.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            Rol rolDocente = rolRepository.findByNombre("Docente").orElseGet(() -> rolRepository.save(new Rol("Docente")));
            u.setRol(rolDocente);
            usuarioRepository.save(u);

            Docente d = new Docente(u, aniosExperiencia);
            d.setMatriculaCnv(matriculaCnv);
            d.setBiografia(biografia);
            d.setHabilitado(true);
            docenteRepository.save(d);

            ra.addFlashAttribute("mensaje", "Docente registrado con éxito.");
            return "redirect:/seguridad/usuarios";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/docentes/nuevo";
        }
    }

    @GetMapping("/docentes/{id}/editar")
    public String modificarDocenteForm(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Docente> dOpt = docenteRepository.findById(id);
        if (dOpt.isEmpty()) return "redirect:/seguridad/usuarios";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("docente", dOpt.get());
        model.addAttribute("titulo", "CU-89 - Modificar docente | Idóneos Online");
        return "pages/seguridad/cu-89-modificar-docente";
    }

    @PostMapping("/docentes/{id}/editar")
    public String actualizarDocente(@PathVariable Integer id,
                                    @RequestParam int aniosExperiencia,
                                    @RequestParam(required = false) String matriculaCnv,
                                    @RequestParam(required = false) String biografia,
                                    @RequestParam(defaultValue = "false") boolean habilitado,
                                    RedirectAttributes ra) {
        try {
            Docente d = docenteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Docente no encontrado"));
            d.setAniosExperiencia(aniosExperiencia);
            d.setMatriculaCnv(matriculaCnv);
            d.setBiografia(biografia);
            d.setHabilitado(habilitado);
            docenteRepository.save(d);

            ra.addFlashAttribute("mensaje", "Docente modificado correctamente.");
            return "redirect:/seguridad/usuarios";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/seguridad/docentes/" + id + "/editar";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CU-93 & CU-94: SESIONES ACTIVAS
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/sesiones")
    public String buscarSesiones(Model model, Authentication auth) {
        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("sesiones", sesionRepository.findAll());
        model.addAttribute("titulo", "CU-93 - Buscar sesión | Idóneos Online");
        return "pages/seguridad/cu-93-buscar-sesion";
    }

    @GetMapping("/sesiones/{id}/eliminar")
    public String eliminarSesionView(@PathVariable Integer id, Model model, Authentication auth) {
        Optional<Sesion> sOpt = sesionRepository.findById(id);
        if (sOpt.isEmpty()) return "redirect:/seguridad/sesiones";

        agregarUsuarioAlModelo(model, auth);
        model.addAttribute("sesion", sOpt.get());
        model.addAttribute("titulo", "CU-94 - Eliminar sesión | Idóneos Online");
        return "pages/seguridad/cu-94-eliminar-sesion";
    }

    @PostMapping("/sesiones/{id}/eliminar")
    public String procesarEliminarSesion(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            Optional<Sesion> sOpt = sesionRepository.findById(id);
            if (sOpt.isPresent()) {
                Sesion s = sOpt.get();
                s.setFechaFin(LocalDateTime.now());
                sesionRepository.save(s);
            }
            ra.addFlashAttribute("mensaje", "Sesión revocada y cerrada con éxito.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seguridad/sesiones";
    }
}
