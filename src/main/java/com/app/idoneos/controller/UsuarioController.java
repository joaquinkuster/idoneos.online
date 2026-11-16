package com.app.idoneos.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.idoneos.config.AccesoComponent;
import com.app.idoneos.model.Rol;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.titulodocente.TituloDocenteService;
import com.app.idoneos.service.usuario.UsuarioService;
import com.app.idoneos.util.FiltroUtil;
import com.app.idoneos.util.PaginacionUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;

/** Controlador del usuario. */
@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TituloDocenteService tituloDocenteService;

    @Autowired
    private AccesoComponent accesoComponent;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** GET /usuario/buscar: Busca usuarios. */
    @GetMapping("/buscar")
    public String buscar(Model model, jakarta.servlet.http.HttpServletRequest request, Authentication authentication) {
        List<Usuario> usuarios = usuarioService.buscarTodos();
        // El filtro, el orden por columna y la página los resuelve el servidor (motor común de listados)
        PaginacionUtil.Pagina<Usuario> pagina = Filtros.tabla.paginar(usuarios, request.getParameterMap());
        PaginacionUtil.cargarEnModelo(model, pagina);
        model.addAttribute("usuarios", pagina.filas());
        if (PaginacionUtil.pideSoloFilas(request)) {
            return "pages/usuario/buscar :: filas"; // solo las filas de la página pedida
        }
        model.addAttribute("idActual", ((Usuario) authentication.getPrincipal()).getIdUsuario());
        model.addAttribute("titulo", "Usuarios | Idóneos Online");
        model.addAttribute("menuActivo", "usuarios");
        return "pages/usuario/buscar";
    }

    /** GET /usuario/modificar/{id}: Ventana de modificación de un usuario. */
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable("id") Integer id, Model model) {
        Usuario u = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario se encuentra dado de baja."));
        if (u.esAdmin()) {
            throw new IllegalArgumentException("Error! Las cuentas de administrador no se pueden modificar.");
        }
        model.addAttribute("u", u);
        model.addAttribute("titulosDocente", u.getDocente() == null ? List.of() : tituloDocenteService.buscarPorDocente(u.getDocente()));
        return "pages/usuario/modificar :: modal(u=${u})";
    }

    /** GET /usuario/perfil: Muestra el perfil del usuario autenticado, con sus datos editables. */
    @GetMapping("/perfil")
    public String perfil(Model model, Authentication authentication) {
        Usuario actual = (Usuario) authentication.getPrincipal();
        Usuario perfil = usuarioService.buscarPorId(actual.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no se encuentra activo."));
        model.addAttribute("perfil", perfil);
        if (perfil.getDocente() != null && perfil.esDocente()) {
            model.addAttribute("titulos", tituloDocenteService.buscarPorDocente(perfil.getDocente()).stream()
                    .filter(titulo -> !titulo.getBaja()).toList());
        }
        model.addAttribute("titulo", "Mi perfil | Idóneos Online");
        return "pages/usuario/perfil";
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** POST /usuario/registrar: Registra una cuenta, y el usuario recibe un enlace para definir su contraseña. */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, String>> registrar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "correo", required = false) String correo,
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "roles", required = false) List<String> roles,
            @RequestParam(value = "biografia", required = false) String biografia,
            @RequestParam(value = "aniosExperiencia", required = false) Integer aniosExperiencia,
            @RequestParam(value = "matriculaCnv", required = false) String matriculaCnv,
            @RequestParam(value = "habilitado", defaultValue = "false") boolean habilitado,
            @RequestParam(value = "titulo", required = false) List<String> titulos,
            @RequestParam(value = "matricula", required = false) List<String> matriculas) {
        try {
            Usuario usuario = usuarioService.registrar(nombre, apellido, correo, dni, telefono, roles,
                    new UsuarioService.DatosDocente(biografia, aniosExperiencia, matriculaCnv, habilitado, titulos, matriculas));
            return RespuestaFormularioUtil.armarRespuestaExitosa("Usuario '" + usuario.getNombreCompleto()
                    + "' registrado con éxito. Le enviamos un correo para que defina su contraseña.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/autoregistrar: Registra la cuenta de un alumno que se registra por su cuenta y le envía el correo para validarla. */
    @PostMapping("/autoregistrar")
    public ResponseEntity<Map<String, String>> autoregistrar(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "correo", required = false) String correo,
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "contrasena", required = false) String contrasena,
            @RequestParam(value = "confirmarContrasena", required = false) String confirmarContrasena) {
        try {
            usuarioService.autoregistrar(nombre, apellido, correo, dni, contrasena, confirmarContrasena);
            return RespuestaFormularioUtil.armarRespuestaExitosa(
                    "Cuenta creada. Te enviamos un correo para validarla antes de iniciar sesión.", "/login");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** POST /usuario/modificar/{id}: Modifica los datos de una cuenta y sus roles. */
    @PostMapping("/modificar/{id}")
    public ResponseEntity<Map<String, String>> modificar(@PathVariable("id") Integer id,
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "correo", required = false) String correo,
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "roles", required = false) List<String> roles,
            @RequestParam(value = "biografia", required = false) String biografia,
            @RequestParam(value = "aniosExperiencia", required = false) Integer aniosExperiencia,
            @RequestParam(value = "matriculaCnv", required = false) String matriculaCnv,
            @RequestParam(value = "habilitado", defaultValue = "false") boolean habilitado,
            @RequestParam(value = "titulo", required = false) List<String> titulos,
            @RequestParam(value = "matricula", required = false) List<String> matriculas,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "quitarImagen", defaultValue = "false") boolean quitarImagen) {
        try {
            usuarioService.modificar(id, nombre, apellido, correo, dni, telefono, roles,
                    new UsuarioService.DatosDocente(biografia, aniosExperiencia, matriculaCnv, habilitado, titulos, matriculas),
                    imagen, quitarImagen);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Usuario actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/modificarPerfil: Modifica los datos del perfil del usuario autenticado. */
    @PostMapping("/modificarPerfil")
    public ResponseEntity<Map<String, String>> modificarPerfil(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "quitarImagen", defaultValue = "false") boolean quitarImagen, Authentication authentication) {
        try {
            Usuario actual = (Usuario) authentication.getPrincipal();
            Usuario modificado = usuarioService.modificarPerfil(actual.getIdUsuario(), nombre, apellido, telefono, imagen, quitarImagen);
            // Mantiene actualizado el usuario de la sesión, que es el que muestran el menú y las pantallas
            actual.setNombre(modificado.getNombre());
            actual.setApellido(modificado.getApellido());
            actual.setTelefono(modificado.getTelefono());
            actual.setImagen(modificado.getImagen());
            return RespuestaFormularioUtil.armarRespuestaExitosa("Perfil actualizado con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/completarGoogle: Crea la cuenta de quien ingresó con Google, con el DNI que informa, e inicia su sesión. */
    @PostMapping("/completarGoogle")
    public ResponseEntity<Map<String, String>> completarGoogle(
            @RequestParam(value = "nombre", required = false) String nombre,
            @RequestParam(value = "apellido", required = false) String apellido,
            @RequestParam(value = "dni", required = false) String dni,
            jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        try {
            jakarta.servlet.http.HttpSession sesion = request.getSession(false);
            AccesoComponent.DatosGoogle datos = sesion != null && sesion.getAttribute(AccesoComponent.GOOGLE_PENDIENTE)
                    instanceof AccesoComponent.DatosGoogle pendiente ? pendiente : null;
            if (datos == null || datos.vencido()) {
                throw new IllegalArgumentException("Error! La autorización de Google venció. Volvé a ingresar con Google.");
            }
            Usuario usuario = usuarioService.registrarConGoogle(datos.googleId(), datos.correo(), nombre, apellido, dni);
            sesion.removeAttribute(AccesoComponent.GOOGLE_PENDIENTE);
            accesoComponent.autenticar(request, response, usuario);
            try {
                return RespuestaFormularioUtil.armarRespuestaDeRedireccion(accesoComponent.iniciarSesion(request, response, usuario, false));
            } catch (IllegalStateException e) {
                accesoComponent.descartarAutenticacion(request);
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/solicitarRecuperacion: Envía al correo indicado el enlace para crear una contraseña nueva. */
    @PostMapping("/solicitarRecuperacion")
    public ResponseEntity<Map<String, String>> solicitarRecuperacion(
            @RequestParam(value = "correo", required = false) String correo) {
        try {
            usuarioService.solicitarRecuperacion(correo);
            return RespuestaFormularioUtil.armarRespuestaExitosa(
                    "Te enviamos un correo con el enlace para crear una contraseña nueva. Vence en 1 hora.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/restablecerContrasena: Cambia la contraseña con el enlace recibido por correo. */
    @PostMapping("/restablecerContrasena")
    public ResponseEntity<Map<String, String>> restablecerContrasena(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "contrasena", required = false) String contrasena,
            @RequestParam(value = "confirmarContrasena", required = false) String confirmarContrasena) {
        try {
            usuarioService.restablecerContrasena(token, contrasena, confirmarContrasena);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Contraseña guardada. Ya podés iniciar sesión.", "/login");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/cambiarRolPorDefecto: Cambia el rol por defecto del usuario autenticado (el rol con el que trabaja). */
    @PostMapping("/cambiarRolPorDefecto")
    public ResponseEntity<Map<String, String>> cambiarRolPorDefecto(
            @RequestParam(value = "idRol", required = false) Integer idRol, Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Rol rol = usuarioService.cambiarRolPorDefecto(usuario.getIdUsuario(), idRol);
            usuario.setRolPorDefecto(rol); // mantiene actualizado el usuario de la sesión
            String destino = usuario.esAdministradorActivo() ? "/curso/buscar"
                    : usuario.esAlumnoActivo() ? "/inscripcion/buscar" : "/inicio";
            return RespuestaFormularioUtil.armarRespuestaExitosa("Ahora trabajás con el rol " + rol.getNombre() + ".", destino);
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** POST /usuario/darDeBaja/{id}: Da de baja la cuenta de un usuario. */
    @PostMapping("/darDeBaja/{id}")
    public ResponseEntity<Map<String, String>> darDeBaja(@PathVariable("id") Integer id, Authentication authentication) {
        try {
            impedirBajaPropia(List.of(id), authentication);
            usuarioService.darDeBaja(id);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Usuario dado de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    /** POST /usuario/darDeBajaMasiva: Da de baja varias cuentas a la vez, todas o ninguna. */
    @PostMapping("/darDeBajaMasiva")
    public ResponseEntity<Map<String, String>> darDeBajaVarios(
            @RequestParam(value = "ids", required = false) List<Integer> ids, Authentication authentication) {
        try {
            impedirBajaPropia(ids, authentication);
            usuarioService.darDeBajaVarios(ids);
            return RespuestaFormularioUtil.armarRespuestaExitosa("Usuarios dados de baja con éxito.");
        } catch (Exception e) {
            return RespuestaFormularioUtil.armarRespuestaConError(e);
        }
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Impide que un administrador se dé de baja a sí mismo, porque se quedaría sin acceso. */
    private void impedirBajaPropia(List<Integer> ids, Authentication authentication) {
        int idActual = ((Usuario) authentication.getPrincipal()).getIdUsuario();
        if (ids != null && ids.contains(idActual)) {
            throw new IllegalArgumentException("Error! No podés dar de baja tu propia cuenta.");
        }
    }

    // ----------------------------------------------------------------------
    // Listado (motor común)
    // ----------------------------------------------------------------------

    /** Columnas de filtro, orden y página de los listados (las aplica el servidor). */
    public static final class Filtros {

        private Filtros() {
        }

        /** Define las columnas del listado, que se filtran y ordenan en el servidor. */
        public static final FiltroUtil<Usuario> tabla = new FiltroUtil<>(List.of(
                new FiltroUtil.Columna<Usuario>("nombre", FiltroUtil.Tipo.TEXTO, u -> u.getApellido() + ", " + u.getNombre()),
                new FiltroUtil.Columna<Usuario>("correo", FiltroUtil.Tipo.TEXTO, Usuario::getCorreo),
                new FiltroUtil.Columna<Usuario>("dni", FiltroUtil.Tipo.TEXTO, Usuario::getDni),
                new FiltroUtil.Columna<Usuario>("roles", FiltroUtil.Tipo.LISTA, Usuario::getNombresDeRoles),
                new FiltroUtil.Columna<Usuario>("estado", FiltroUtil.Tipo.LISTA, Usuario::getEstado),
                new FiltroUtil.Columna<Usuario>("fechaCreacion", FiltroUtil.Tipo.FECHA,
                        u -> FiltroUtil.formatearFechaHora(u.getFechaCreacion()))),
                Usuario::getBaja);
    }
}
