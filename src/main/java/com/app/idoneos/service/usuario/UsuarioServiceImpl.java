package com.app.idoneos.service.usuario;

import java.time.LocalDateTime;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TituloDocente;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.RolUsuarioRepository;
import com.app.idoneos.service.administrador.AdministradorService;
import com.app.idoneos.service.docente.DocenteService;
import com.app.idoneos.service.titulodocente.TituloDocenteService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import com.app.idoneos.model.Rol;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.service.alumno.AlumnoService;
import com.app.idoneos.service.correo.CorreoService;
import com.app.idoneos.service.rol.RolService;
import com.app.idoneos.service.rolusuario.RolUsuarioService;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de UsuarioService. */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    /** Formato de un correo electrónico. */
    private static final Pattern FORMATO_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Vigencia del enlace para validar el correo. */
    private static final Duration VIGENCIA_VALIDACION = Duration.ofHours(24);

    /** Vigencia del enlace para que un usuario creado por un administrador defina su contraseña. */
    private static final Duration VIGENCIA_DEFINICION = Duration.ofHours(72);

    /** Roles del sistema, del de mayor jerarquía al de menor. */
    private static final List<String> ROLES_POR_JERARQUIA = List.of("Administrador", "Docente", "Alumno");

    /** Formato de la matrícula de idóneo de la Comisión Nacional de Valores, por ejemplo 984. */
    private static final Pattern FORMATO_MATRICULA = Pattern.compile("^[0-9]{1,8}$");

    /** Formato de la matrícula profesional de un título, por ejemplo 3591, LE26 o LA200. */
    private static final Pattern FORMATO_MATRICULA_TITULO = Pattern.compile("^[A-Za-z0-9]{1,20}$");

    /** Vigencia del enlace para recuperar la contraseña. */
    private static final Duration VIGENCIA_RECUPERACION = Duration.ofHours(1);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("directorioCursos")
    private java.nio.file.Path directorioCursos;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolService rolService;

    @Autowired
    private RolUsuarioService rolUsuarioService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private CorreoService correoService;

    @Autowired
    private com.app.idoneos.service.sesion.SesionService sesionService;

    @Autowired
    private DocenteService docenteService;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private TituloDocenteService tituloDocenteService;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private RespuestaForoRepository respuestaForoRepository;

    @Autowired
    private ConsultaForoRepository consultaForoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

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

    /** Obtiene todos los usuarios, incluidos los dados de baja. */
    @Override
    public List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    /** Busca el usuario activo al que pertenece un token de un solo uso vigente. */
    @Override
    public Usuario buscarPorToken(String token) {
        Usuario usuario = token == null || token.isBlank() ? null : usuarioRepository.findByTokenVerificacion(token).orElse(null);
        if (usuario == null || usuario.esInactivo()) {
            throw new IllegalArgumentException("Error! El enlace no es válido.");
        }
        if (usuario.getVencimientoTokenVerificacion() == null || usuario.getVencimientoTokenVerificacion().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Error! El enlace venció. Solicitá uno nuevo.");
        }
        return usuario;
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Registra una cuenta por un administrador. El usuario recibe un enlace para definir su contraseña. */
    @Override
    @Transactional
    public Usuario registrar(String nombre, String apellido, String correo, String dni, String telefono, List<String> roles,
            DatosDocente datosDocente) {
        String nombreLimpio = limpiar(nombre);
        String apellidoLimpio = limpiar(apellido);
        String correoLimpio = limpiar(correo).toLowerCase();
        String dniLimpio = limpiar(dni);
        String telefonoLimpio = limpiar(telefono);
        validarDatosPersonales(nombreLimpio, apellidoLimpio, correoLimpio, dniLimpio, telefonoLimpio);
        Set<String> nombresRoles = validarRoles(roles);
        if (nombresRoles.contains("Docente")) {
            validarDatosDocente(datosDocente, null);
        }
        if (usuarioRepository.findByCorreo(correoLimpio).isPresent()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("correo", "Error! El correo electrónico ya está registrado por otra cuenta.");
        }
        if (usuarioRepository.existsByDni(dniLimpio)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("dni", "Error! El DNI ya está registrado por otra cuenta.");
        }

        // La contraseña la define el propio usuario con el enlace del correo. Mientras tanto, la cuenta tiene una que nadie conoce
        Usuario usuario = new Usuario(nombreLimpio, apellidoLimpio, dniLimpio, correoLimpio, passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setTelefono(telefonoLimpio.isEmpty() ? null : telefonoLimpio);
        usuario.setRolPorDefecto(buscarRol(rolDeMayorJerarquia(nombresRoles)));
        usuario = usuarioRepository.save(usuario);
        for (String nombreRol : ROLES_POR_JERARQUIA) {
            if (nombresRoles.contains(nombreRol)) {
                asignarRol(usuario, buscarRol(nombreRol), datosDocente);
            }
        }

        String token = generarToken(usuario.getIdUsuario(), VIGENCIA_DEFINICION);
        correoService.enviarDefinicionContrasena(usuario, token);
        return usuario;
    }

    /** Registra la cuenta de un alumno que se registra por su cuenta y le envía el enlace para validar su correo. */
    @Override
    @Transactional
    public Usuario autoregistrar(String nombre, String apellido, String correo, String dni, String contrasena, String confirmacion) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String apellidoLimpio = apellido == null ? "" : apellido.trim();
        String correoLimpio = correo == null ? "" : correo.trim().toLowerCase();
        String dniLimpio = dni == null ? "" : dni.trim();
        if (nombreLimpio.isEmpty() || apellidoLimpio.isEmpty() || correoLimpio.isEmpty() || dniLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Completá todos los campos obligatorios.");
        }
        if (nombreLimpio.length() > 50 || apellidoLimpio.length() > 50) {
            throw new IllegalArgumentException("Error! El nombre y el apellido no pueden superar los 50 caracteres.");
        }
        if (correoLimpio.length() > 150 || !FORMATO_CORREO.matcher(correoLimpio).matches()) {
            throw new IllegalArgumentException("Error! El correo electrónico no es válido.");
        }
        if (!dniLimpio.matches("^[0-9]{7,8}$")) {
            throw new IllegalArgumentException("Error! El DNI debe tener 7 u 8 dígitos.");
        }
        validarContrasena(contrasena, confirmacion);
        if (usuarioRepository.findByCorreo(correoLimpio).isPresent()) {
            throw new IllegalArgumentException(
                    "Error! Ya existe una cuenta con ese correo electrónico. Iniciá sesión o recuperá tu contraseña.");
        }
        if (usuarioRepository.existsByDni(dniLimpio)) {
            throw new IllegalArgumentException("Error! Ya existe una cuenta con ese DNI.");
        }

        Rol rolAlumno = rolService.buscarPorNombre("Alumno")
                .orElseThrow(() -> new IllegalStateException("No está definido el rol Alumno."));
        Usuario usuario = new Usuario(nombreLimpio, apellidoLimpio, dniLimpio, correoLimpio, passwordEncoder.encode(contrasena));
        usuario.setRolPorDefecto(rolAlumno);
        usuario = usuarioRepository.save(usuario);
        rolUsuarioService.guardar(new RolUsuario(rolAlumno, usuario));
        alumnoService.guardar(new Alumno(usuario));

        String token = generarToken(usuario.getIdUsuario(), VIGENCIA_VALIDACION);
        correoService.enviarValidacionCuenta(usuario, token);
        return usuario;
    }

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

    /** Modifica los datos de la cuenta de un usuario y sus roles. Las cuentas de administrador no se modifican. */
    @Override
    @Transactional
    public Usuario modificar(int idUsuario, String nombre, String apellido, String correo, String dni, String telefono,
            List<String> roles, DatosDocente datosDocente, org.springframework.web.multipart.MultipartFile imagen, boolean quitarImagen) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(entidad -> !entidad.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario se encuentra dado de baja."));
        if (usuario.esAdmin()) {
            throw new IllegalArgumentException("Error! Las cuentas de administrador no se pueden modificar.");
        }
        String nombreLimpio = limpiar(nombre);
        String apellidoLimpio = limpiar(apellido);
        String correoLimpio = limpiar(correo).toLowerCase();
        String dniLimpio = limpiar(dni);
        String telefonoLimpio = limpiar(telefono);
        validarDatosPersonales(nombreLimpio, apellidoLimpio, correoLimpio, dniLimpio, telefonoLimpio);
        Set<String> nuevos = validarRoles(roles);
        if (nuevos.contains("Docente")) {
            validarDatosDocente(datosDocente, usuario.getDocente());
        }
        if (usuarioRepository.findByCorreo(correoLimpio).filter(otro -> otro.getIdUsuario() != idUsuario).isPresent()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("correo", "Error! El correo electrónico ya está registrado por otra cuenta.");
        }
        if (usuarioRepository.existsByDniAndIdUsuarioNot(dniLimpio, idUsuario)) {
            throw RespuestaFormularioUtil.armarCampoInvalido("dni", "Error! El DNI ya está registrado por otra cuenta.");
        }

        // Un rol solo se quita si no deja trabajo sin dueño
        Set<String> actuales = usuario.getRolesVigentes().stream().map(Rol::getNombre).collect(java.util.stream.Collectors.toSet());
        for (String actual : actuales) {
            if (nuevos.contains(actual)) {
                continue;
            }
            String motivo = "Docente".equals(actual) ? motivoDependenciasDocente(usuario)
                    : "Alumno".equals(actual) ? motivoDependenciasAlumno(usuario) : null;
            if (motivo != null) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("quitar el rol " + actual + " a", "la cuenta",
                        usuario.getNombreCompleto(), motivo.split("\\|")[0], motivo.split("\\|")[1]));
            }
        }

        // Suspender a un docente exige que no esté a cargo de cursos
        Docente docente = usuario.getDocente();
        Boolean nuevaHabilitacion = null;
        if (actuales.contains("Docente") && nuevos.contains("Docente") && docente != null) {
            if (docente.getHabilitado() && !datosDocente.habilitado()) {
                long cursos = contarCursosActivos(docente);
                if (cursos > 0) {
                    throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("suspender", "la habilitación de la cuenta",
                            usuario.getNombreCompleto(), "es titular o ayudante de "
                                    + MensajeErrorUtil.formatearCantidad(cursos, "curso activo", "cursos activos"),
                            "Reemplazalo como titular o ayudante primero."));
                }
                nuevaHabilitacion = false;
            } else if (!docente.getHabilitado() && datosDocente.habilitado()) {
                nuevaHabilitacion = true;
            }
        }

        usuario.setNombre(nombreLimpio);
        usuario.setApellido(apellidoLimpio);
        usuario.setCorreo(correoLimpio);
        usuario.setDni(dniLimpio);
        usuario.setTelefono(telefonoLimpio.isEmpty() ? null : telefonoLimpio);
        usuario.setUltimaModificacion(LocalDateTime.now());
        aplicarFoto(usuario, imagen, quitarImagen);

        // Los roles quitados se dan de baja sin borrar su historial, y los agregados se registran o se reactivan
        for (RolUsuario asignacion : usuario.getRoles()) {
            if (!asignacion.getBaja() && !nuevos.contains(asignacion.getRol().getNombre())) {
                asignacion.marcarInactivo();
                asignacion.setUltimaModificacion(LocalDateTime.now());
                rolUsuarioService.modificar(asignacion);
            }
        }
        for (String nombreRol : ROLES_POR_JERARQUIA) {
            if (nuevos.contains(nombreRol)) {
                asignarRol(usuario, buscarRol(nombreRol), datosDocente);
            }
        }
        if (usuario.getRolPorDefecto() == null || !nuevos.contains(usuario.getRolPorDefecto().getNombre())) {
            usuario.setRolPorDefecto(buscarRol(rolDeMayorJerarquia(nuevos)));
        }
        usuario = usuarioRepository.save(usuario);

        if (nuevaHabilitacion != null) {
            try {
                correoService.enviarCambioHabilitacionDocente(usuario, nuevaHabilitacion);
            } catch (IllegalStateException e) {
                // El aviso es un complemento: si no se puede enviar, el cambio ya hecho se conserva
            }
        }
        return usuario;
    }

    /** Marca como validado el correo del usuario dueño del token y anula el token. */
    @Override
    public void validarCorreo(String token) {
        Usuario usuario = buscarPorToken(token);
        usuario.setEmailValidado(true);
        usuario.setTokenVerificacion(null);
        usuario.setVencimientoTokenVerificacion(null);
        usuario.setUltimaModificacion(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    /** Envía al correo indicado el enlace para crear una contraseña nueva. */
    @Override
    @Transactional
    public void solicitarRecuperacion(String correo) {
        String correoLimpio = correo == null ? "" : correo.trim().toLowerCase();
        if (correoLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Ingresá el correo electrónico de tu cuenta.");
        }
        Usuario usuario = usuarioRepository.findByCorreo(correoLimpio)
                .orElseThrow(() -> new IllegalArgumentException("Error! No existe una cuenta registrada con ese correo electrónico."));
        if (usuario.esInactivo()) {
            throw new IllegalArgumentException("Error! La cuenta no se encuentra activa.");
        }
        String token = generarToken(usuario.getIdUsuario(), VIGENCIA_RECUPERACION);
        correoService.enviarRecuperacionContrasena(usuario, token);
    }

    /** Cambia la contraseña del usuario dueño del token, valida su correo y anula el token. */
    @Override
    public void restablecerContrasena(String token, String contrasena, String confirmacion) {
        Usuario usuario = buscarPorToken(token);
        validarContrasena(contrasena, confirmacion);
        usuario.setContrasena(passwordEncoder.encode(contrasena));
        usuario.setEmailValidado(true); // llegar con el enlace del correo prueba que la casilla es del usuario
        usuario.setTokenVerificacion(null);
        usuario.setVencimientoTokenVerificacion(null);
        usuario.setUltimaModificacion(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);
        sesionService.cerrarTodas(usuario); // con la contraseña nueva se vuelve a ingresar en todos los dispositivos
    }

    /** Modifica los datos del perfil que el propio usuario puede editar. */
    @Override
    public Usuario modificarPerfil(int idUsuario, String nombre, String apellido, String telefono,
            org.springframework.web.multipart.MultipartFile imagen, boolean quitarImagen) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(entidad -> !entidad.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no se encuentra activo."));
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String apellidoLimpio = apellido == null ? "" : apellido.trim();
        String telefonoLimpio = telefono == null ? "" : telefono.trim();
        if (nombreLimpio.isEmpty() || apellidoLimpio.isEmpty()) {
            throw new IllegalArgumentException("Error! Completá todos los campos obligatorios.");
        }
        if (nombreLimpio.length() > 50 || apellidoLimpio.length() > 50) {
            throw new IllegalArgumentException("Error! El nombre y el apellido no pueden superar los 50 caracteres.");
        }
        if (!telefonoLimpio.isEmpty() && !telefonoLimpio.matches("^[0-9+()\\s-]{6,20}$")) {
            throw new IllegalArgumentException("Error! El teléfono no es válido. Formato válido: entre 6 y 20 dígitos, con +, espacios o guiones.");
        }

        aplicarFoto(usuario, imagen, quitarImagen);

        usuario.setNombre(nombreLimpio);
        usuario.setApellido(apellidoLimpio);
        usuario.setTelefono(telefonoLimpio.isEmpty() ? null : telefonoLimpio);
        usuario.setUltimaModificacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
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

    /** Genera un token de un solo uso para el usuario, reemplazando el anterior si lo había. */
    @Override
    public String generarToken(int idUsuario, java.time.Duration vigencia) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(entidad -> !entidad.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario no se encuentra activo."));
        String token = java.util.UUID.randomUUID().toString().replace("-", "") + java.util.UUID.randomUUID().toString().replace("-", "");
        usuario.setTokenVerificacion(token);
        usuario.setVencimientoTokenVerificacion(LocalDateTime.now().plus(vigencia));
        usuario.setUltimaModificacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        return token;
    }

    /** Anula el token del usuario, para que no pueda volver a usarse. */
    @Override
    public void anularToken(int idUsuario) {
        usuarioRepository.findById(idUsuario).ifPresent(usuario -> {
            usuario.setTokenVerificacion(null);
            usuario.setVencimientoTokenVerificacion(null);
            usuarioRepository.save(usuario);
        });
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja la cuenta de un usuario, quitándole el acceso al sistema. */
    @Override
    @Transactional
    public void darDeBaja(int idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .filter(entidad -> !entidad.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El usuario se encuentra dado de baja."));
        String nombre = usuario.getNombreCompleto();
        if (usuario.esAdmin() && rolUsuarioRepository.contarOtrosUsuariosActivosConRol("Administrador", idUsuario) == 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la cuenta", nombre,
                    "es el único administrador activo del sistema", "El sistema no puede quedar sin administradores: registrá otro antes."));
        }
        String motivo = usuario.esDocente() ? motivoDependenciasDocente(usuario) : null;
        if (motivo == null && usuario.esAlumno()) {
            motivo = motivoDependenciasAlumno(usuario);
        }
        if (motivo != null) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la cuenta", nombre, motivo.split("\\|")[0], motivo.split("\\|")[1]));
        }
        usuario.marcarInactivo();
        usuario.setUltimaModificacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        sesionService.cerrarTodas(usuario); // la cuenta pierde el acceso en todos sus dispositivos
    }

    /** Da de baja varias cuentas a la vez, todas o ninguna. */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un registro.");
        }
        for (Integer id : ids) {
            try {
                darDeBaja(id);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Usuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        usuarioRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    /** Quita los espacios sobrantes de un texto, o lo deja vacío si no hay. */
    private String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /** Busca un rol por su nombre. */
    private Rol buscarRol(String nombre) {
        return rolService.buscarPorNombre(nombre).orElseThrow(() -> new IllegalStateException("No está definido el rol " + nombre + "."));
    }

    /** Obtiene el rol de mayor jerarquía entre los indicados. */
    private String rolDeMayorJerarquia(Set<String> nombres) {
        return ROLES_POR_JERARQUIA.stream().filter(nombres::contains).findFirst().orElseThrow();
    }

    /** Verifica los datos personales de una cuenta. */
    private void validarDatosPersonales(String nombre, String apellido, String correo, String dni, String telefono) {
        if (nombre.isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Completá el nombre.");
        }
        if (apellido.isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("apellido", "Error! Completá el apellido.");
        }
        if (nombre.length() > 50 || apellido.length() > 50) {
            throw new IllegalArgumentException("Error! El nombre y el apellido no pueden superar los 50 caracteres.");
        }
        if (correo.isEmpty() || correo.length() > 150 || !FORMATO_CORREO.matcher(correo).matches()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("correo", "Error! El correo electrónico no es válido.");
        }
        if (!dni.matches("^[0-9]{7,8}$")) {
            throw RespuestaFormularioUtil.armarCampoInvalido("dni", "Error! El DNI debe tener 7 u 8 dígitos.");
        }
        if (!telefono.isEmpty() && !telefono.matches("^[0-9+()\\s-]{6,20}$")) {
            throw RespuestaFormularioUtil.armarCampoInvalido("telefono", "Error! El teléfono no es válido. Formato válido: entre 6 y 20 dígitos, con +, espacios o guiones.");
        }
    }

    /** Verifica que haya al menos un rol y que todos existan. */
    private Set<String> validarRoles(List<String> roles) {
        Set<String> nombres = new LinkedHashSet<>();
        if (roles != null) {
            for (String rol : roles) {
                if (rol != null && !rol.isBlank()) {
                    if (!ROLES_POR_JERARQUIA.contains(rol.trim())) {
                        throw new IllegalArgumentException("Error! El rol «" + rol + "» no existe.");
                    }
                    nombres.add(rol.trim());
                }
            }
        }
        if (nombres.isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("roles", "Error! Seleccioná al menos un rol.");
        }
        return nombres;
    }

    /** Verifica los datos profesionales de un docente. El docente actual, si lo hay, puede conservar su matrícula. */
    private void validarDatosDocente(DatosDocente datos, Docente actual) {
        if (datos == null || limpiar(datos.biografia()).isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("biografia", "Error! Completá la biografía del docente.");
        }
        if (datos.biografia().trim().length() > 2000) {
            throw RespuestaFormularioUtil.armarCampoInvalido("biografia", "Error! La biografía no puede superar los 2000 caracteres.");
        }
        if (datos.aniosExperiencia() == null || datos.aniosExperiencia() < 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("aniosExperiencia",
                    "Error! Los años de experiencia deben ser un número entero mayor o igual a cero.");
        }
        String cnv = limpiar(datos.matriculaCnv());
        if (!cnv.isEmpty()) {
            if (!FORMATO_MATRICULA.matcher(cnv).matches()) {
                throw RespuestaFormularioUtil.armarCampoInvalido("matriculaCnv",
                        "Error! La matrícula de idóneo no es válida. Formato válido: solo números, por ejemplo 984.");
            }
            if (docenteRepository.findByMatriculaCnv(cnv).filter(otro -> actual == null || otro.getIdDocente() != actual.getIdDocente()).isPresent()) {
                throw RespuestaFormularioUtil.armarCampoInvalido("matriculaCnv", "Error! La matrícula de idóneo ya está registrada por otro docente.");
            }
        }
        boolean hayTitulo = false;
        List<String> titulos = datos.titulos() == null ? List.of() : datos.titulos();
        for (int i = 0; i < titulos.size(); i++) {
            String titulo = limpiar(titulos.get(i));
            String matricula = datos.matriculas() != null && i < datos.matriculas().size() ? limpiar(datos.matriculas().get(i)) : "";
            if (titulo.isEmpty() && matricula.isEmpty()) {
                continue;
            }
            if (titulo.isEmpty()) {
                throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! Cada matrícula profesional necesita el título al que corresponde.");
            }
            if (titulo.length() > 100) {
                throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! El título admite hasta 100 caracteres.");
            }
            if (!matricula.isEmpty() && !FORMATO_MATRICULA_TITULO.matcher(matricula).matches()) {
                throw RespuestaFormularioUtil.armarCampoInvalido("titulo",
                        "Error! La matrícula de «" + titulo + "» no es válida. Formato válido: letras y números, por ejemplo 3591, LE26 o LA200.");
            }
            hayTitulo = true;
        }
        if (!hayTitulo && cnv.isEmpty()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo",
                    "Error! Declará al menos un título universitario o de posgrado, o una matrícula profesional.");
        }
    }

    /** Da el rol al usuario (lo registra o lo reactiva) y crea o actualiza el perfil que le corresponde. */
    private void asignarRol(Usuario usuario, Rol rol, DatosDocente datosDocente) {
        RolUsuario asignacion = usuario.getRoles().stream()
                .filter(existente -> existente.getRol().getIdRol() == rol.getIdRol()).findFirst().orElse(null);
        if (asignacion == null) {
            asignacion = rolUsuarioService.guardar(new RolUsuario(rol, usuario));
            usuario.getRoles().add(asignacion);
        } else if (asignacion.getBaja()) {
            asignacion.setBaja(false);
            asignacion.setUltimaModificacion(LocalDateTime.now());
            rolUsuarioService.modificar(asignacion);
        }
        switch (rol.getNombre()) {
            case "Alumno" -> {
                if (usuario.getAlumno() == null) {
                    usuario.setAlumno(alumnoService.guardar(new Alumno(usuario)));
                }
            }
            case "Administrador" -> {
                if (usuario.getAdministrador() == null) {
                    usuario.setAdministrador(administradorService.guardar(new Administrador(usuario)));
                }
            }
            case "Docente" -> {
                Docente docente = usuario.getDocente() != null ? usuario.getDocente() : new Docente(usuario);
                String cnv = limpiar(datosDocente.matriculaCnv());
                docente.setBiografia(datosDocente.biografia().trim());
                docente.setAniosExperiencia(datosDocente.aniosExperiencia());
                docente.setMatriculaCnv(cnv.isEmpty() ? null : cnv);
                docente.setHabilitado(datosDocente.habilitado());
                docente = docenteService.guardar(docente);
                usuario.setDocente(docente);
                sincronizarTitulos(docente, datosDocente);
            }
            default -> {
            }
        }
    }

    /** Deja los títulos del docente como indican los datos, reutilizando los existentes y dando de baja los que sobran. */
    private void sincronizarTitulos(Docente docente, DatosDocente datos) {
        List<String> titulos = new ArrayList<>();
        List<String> matriculas = new ArrayList<>();
        for (int i = 0; i < (datos.titulos() == null ? 0 : datos.titulos().size()); i++) {
            String titulo = limpiar(datos.titulos().get(i));
            if (!titulo.isEmpty()) {
                titulos.add(titulo);
                String matricula = datos.matriculas() != null && i < datos.matriculas().size() ? limpiar(datos.matriculas().get(i)) : "";
                matriculas.add(matricula);
            }
        }
        List<TituloDocente> existentes = new ArrayList<>(tituloDocenteService.buscarPorDocente(docente));
        existentes.sort(java.util.Comparator.comparingInt(TituloDocente::getIdTituloDocente));
        for (int i = 0; i < Math.max(existentes.size(), titulos.size()); i++) {
            if (i < titulos.size()) {
                TituloDocente titulo = i < existentes.size() ? existentes.get(i) : new TituloDocente(docente, titulos.get(i));
                titulo.setTitulo(titulos.get(i));
                titulo.setMatricula(matriculas.get(i).isEmpty() ? null : matriculas.get(i));
                titulo.setUltimaModificacion(LocalDateTime.now());
                tituloDocenteService.guardar(titulo);
            } else {
                TituloDocente sobrante = existentes.get(i);
                sobrante.marcarInactivo();
                sobrante.setUltimaModificacion(LocalDateTime.now());
                tituloDocenteService.guardar(sobrante);
            }
        }
    }

    /** Cuenta los cursos activos de los que el docente es titular o ayudante. */
    private long contarCursosActivos(Docente docente) {
        return docenteCursoRepository.findByDocenteAndBajaFalse(docente).stream()
                .filter(participacion -> !participacion.getCurso().esInactivo()).count();
    }

    /** Arma el motivo (y qué hacer) por el que un docente no puede perder su rol, o devuelve null si no hay dependencias. El motivo y el paso a seguir van separados por una barra vertical. */
    private String motivoDependenciasDocente(Usuario usuario) {
        Docente docente = usuario.getDocente();
        if (docente == null) {
            return null;
        }
        long cursos = contarCursosActivos(docente);
        long respuestas = respuestaForoRepository.countByDocenteCurso_DocenteAndBajaFalse(docente);
        List<String> partes = new ArrayList<>();
        if (cursos > 0) {
            partes.add("es titular o ayudante de " + MensajeErrorUtil.formatearCantidad(cursos, "curso activo", "cursos activos"));
        }
        if (respuestas > 0) {
            partes.add("tiene " + MensajeErrorUtil.formatearCantidad(respuestas, "respuesta activa", "respuestas activas") + " en los foros");
        }
        return partes.isEmpty() ? null : String.join(" y ", partes)
                + "|Reemplazalo como titular o ayudante y dá de baja sus respuestas primero.";
    }

    /** Arma el motivo (y qué hacer) por el que un alumno no puede perder su rol, o devuelve null si no hay dependencias. El motivo y el paso a seguir van separados por una barra vertical. */
    private String motivoDependenciasAlumno(Usuario usuario) {
        Alumno alumno = usuario.getAlumno();
        if (alumno == null) {
            return null;
        }
        long inscripciones = inscripcionRepository.findByAlumnoAndBajaFalse(alumno).size();
        long consultas = consultaForoRepository.countByInscripcion_AlumnoAndBajaFalse(alumno);
        List<String> partes = new ArrayList<>();
        if (inscripciones > 0) {
            partes.add("tiene " + MensajeErrorUtil.formatearCantidad(inscripciones, "inscripción activa", "inscripciones activas"));
        }
        if (consultas > 0) {
            partes.add("tiene " + MensajeErrorUtil.formatearCantidad(consultas, "consulta activa", "consultas activas") + " en los foros");
        }
        return partes.isEmpty() ? null : String.join(" y ", partes) + "|Dá de baja sus inscripciones y consultas primero.";
    }

    /** Guarda la foto de perfil nueva, o quita la actual, según se pida. Sin foto nueva ni pedido de quitarla, no cambia nada. */
    private void aplicarFoto(Usuario usuario, org.springframework.web.multipart.MultipartFile imagen, boolean quitarImagen) {
        if (imagen != null && !imagen.isEmpty()) {
            // La foto lleva la hora en el nombre para que el navegador no muestre la anterior guardada en su memoria
            java.nio.file.Path carpeta = com.app.idoneos.util.ManejadorArchivosUtil.resolverCarpetaDePerfil(directorioCursos, usuario.getIdUsuario());
            String nombreNuevo = com.app.idoneos.util.ManejadorArchivosUtil.guardarImagen(imagen, carpeta, "foto-" + System.currentTimeMillis());
            eliminarFoto(usuario);
            usuario.setImagen(nombreNuevo);
        } else if (quitarImagen) {
            eliminarFoto(usuario);
            usuario.setImagen(null);
        }
    }

    /** Borra del servidor la foto de perfil actual del usuario, si la tiene. */
    private void eliminarFoto(Usuario usuario) {
        if (usuario.getImagen() != null) {
            com.app.idoneos.util.ManejadorArchivosUtil.eliminar(usuario.getImagen(),
                    com.app.idoneos.util.ManejadorArchivosUtil.resolverCarpetaDePerfil(directorioCursos, usuario.getIdUsuario()));
        }
    }

    /** Verifica que la contraseña tenga un largo permitido y que coincida con su repetición. */
    private void validarContrasena(String contrasena, String confirmacion) {
        if (contrasena == null || contrasena.isEmpty()) {
            throw new IllegalArgumentException("Error! Ingresá la contraseña.");
        }
        if (contrasena.length() < 8 || contrasena.length() > 72) {
            throw new IllegalArgumentException("Error! La contraseña debe tener entre 8 y 72 caracteres.");
        }
        if (!contrasena.equals(confirmacion)) {
            throw new IllegalArgumentException("Error! Las contraseñas no coinciden.");
        }
    }
}
