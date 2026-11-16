package com.app.idoneos.service.usuario;

import com.app.idoneos.service.CrudService;

import java.util.Optional;

import com.app.idoneos.model.Rol;
import com.app.idoneos.model.Usuario;

/** Servicio del usuario. */
public interface UsuarioService extends CrudService<Usuario> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el usuario por su atributo único 'correo'.
     *
     * @param correo el valor de 'correo' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /**
     * Resuelve el ingreso con Google. Solo ingresa quien ya creó su cuenta con Google: un correo que tiene una cuenta propia no
     * se vincula, y debe iniciar sesión con su correo y su contraseña.
     *
     * @param googleId Identificador de la cuenta de Google.
     * @param correo Correo electrónico que informa Google.
     * @param correoVerificado Indica si Google verificó ese correo.
     * @return La cuenta creada con Google, o vacío si el correo todavía no tiene cuenta.
     * @throws CorreoYaRegistradoException Si el correo ya tiene una cuenta propia.
     * @throws IllegalArgumentException Si Google no verificó el correo o la cuenta está dada de baja.
     */
    Optional<Usuario> ingresarConGoogle(String googleId, String correo, boolean correoVerificado);

    /**
     * Obtiene todos los usuarios, incluidos los dados de baja.
     *
     * @return La lista completa de usuarios.
     */
    java.util.List<Usuario> buscarTodos();

    /**
     * Busca el usuario activo al que pertenece un token de un solo uso vigente.
     *
     * @param token El token recibido en el enlace del correo.
     * @return El usuario dueño del token.
     * @throws IllegalArgumentException Si el token no existe, venció o el usuario está dado de baja.
     */
    Usuario buscarPorToken(String token);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Datos profesionales de un docente.
     *
     * @param biografia Biografía del docente.
     * @param aniosExperiencia Años de experiencia profesional.
     * @param matriculaCnv Matrícula de idóneo de la Comisión Nacional de Valores, solo números, o vacío si no aplica.
     * @param habilitado Indica si el docente está habilitado para dictar clases.
     * @param titulos Títulos universitarios o de posgrado.
     * @param matriculas Matrícula profesional de cada título, en el mismo orden que los títulos.
     */
    record DatosDocente(String biografia, Integer aniosExperiencia, String matriculaCnv, boolean habilitado,
            java.util.List<String> titulos, java.util.List<String> matriculas) {
    }

    /**
     * Registra la cuenta de un alumno que ingresó con Google. El correo queda validado y la contraseña es una que nadie conoce.
     *
     * @param googleId Identificador de la cuenta de Google.
     * @param correo Correo electrónico que informa Google.
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param dni Documento Nacional de Identidad, de 7 u 8 dígitos.
     * @return El usuario registrado.
     * @throws IllegalArgumentException Si falta algún dato, alguno no es válido o el correo o el DNI ya están registrados.
     */
    Usuario registrarConGoogle(String googleId, String correo, String nombre, String apellido, String dni);

    /**
     * Registra una cuenta por un administrador. El usuario recibe un enlace para definir su contraseña.
     *
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param correo Correo electrónico, que será su usuario de acceso.
     * @param dni Documento Nacional de Identidad, de 7 u 8 dígitos.
     * @param telefono Teléfono, opcional.
     * @param roles Nombres de los roles de la cuenta (Alumno, Docente y/o Administrador).
     * @param datosDocente Datos profesionales, obligatorios si entre los roles está Docente.
     * @return El usuario registrado.
     * @throws IllegalArgumentException Si falta algún dato, alguno no es válido o el correo o el DNI ya están registrados.
     * @throws IllegalStateException Si no se pudo enviar el correo.
     */
    Usuario registrar(String nombre, String apellido, String correo, String dni, String telefono,
            java.util.List<String> roles, DatosDocente datosDocente);

    /**
     * Registra la cuenta de un alumno que se registra por su cuenta y le envía el enlace para validar su correo.
     *
     * @param nombre Nombre del usuario.
     * @param apellido Apellido del usuario.
     * @param correo Correo electrónico, que será su usuario de acceso.
     * @param dni Documento Nacional de Identidad, de 7 u 8 dígitos.
     * @param contrasena Contraseña elegida.
     * @param confirmacion Repetición de la contraseña.
     * @return El usuario registrado, con el correo sin validar.
     * @throws IllegalArgumentException Si falta algún dato, alguno no es válido o el correo o el DNI ya están registrados.
     * @throws IllegalStateException Si no se pudo enviar el correo de validación.
     */
    Usuario autoregistrar(String nombre, String apellido, String correo, String dni, String contrasena, String confirmacion);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica los datos de la cuenta de un usuario y sus roles. Las cuentas de administrador no se modifican.
     *
     * @param idUsuario Identificador del usuario.
     * @param nombre Nombre nuevo.
     * @param apellido Apellido nuevo.
     * @param correo Correo electrónico nuevo.
     * @param dni DNI nuevo.
     * @param telefono Teléfono nuevo, opcional.
     * @param roles Nombres de los roles que la cuenta debe tener.
     * @param datosDocente Datos profesionales, obligatorios si entre los roles está Docente.
     * @param imagen Foto de perfil nueva, o {@code null} si no cambia.
     * @param quitarImagen Indica si se quita la foto actual, cuando no se sube una nueva.
     * @return El usuario modificado.
     * @throws IllegalArgumentException Si el usuario no está activo, es administrador, algún dato no es válido o un rol quitado
     *         tiene dependencias activas.
     */
    Usuario modificar(int idUsuario, String nombre, String apellido, String correo, String dni, String telefono,
            java.util.List<String> roles, DatosDocente datosDocente, org.springframework.web.multipart.MultipartFile imagen,
            boolean quitarImagen);

    /**
     * Marca como validado el correo del usuario dueño del token y anula el token.
     *
     * @param token El token recibido en el enlace del correo.
     * @throws IllegalArgumentException Si el token no es válido o venció.
     */
    void validarCorreo(String token);

    /**
     * Envía al correo indicado el enlace para crear una contraseña nueva.
     *
     * @param correo Correo electrónico de la cuenta.
     * @throws IllegalArgumentException Si el correo no está registrado o la cuenta no está activa.
     * @throws IllegalStateException Si no se pudo enviar el correo.
     */
    void solicitarRecuperacion(String correo);

    /**
     * Cambia la contraseña del usuario dueño del token, valida su correo y anula el token.
     *
     * @param token El token recibido en el enlace del correo.
     * @param contrasena Contraseña nueva.
     * @param confirmacion Repetición de la contraseña nueva.
     * @throws IllegalArgumentException Si el token no es válido o venció, o la contraseña no es válida.
     */
    void restablecerContrasena(String token, String contrasena, String confirmacion);

    /**
     * Genera un token de un solo uso para el usuario, reemplazando el anterior si lo había. Sirve para validar el correo,
     * recuperar la contraseña o definirla por primera vez.
     *
     * @param idUsuario Identificador del usuario.
     * @param vigencia Tiempo durante el cual el token puede usarse.
     * @return El token generado.
     * @throws IllegalArgumentException Si el usuario no existe o está dado de baja.
     */
    String generarToken(int idUsuario, java.time.Duration vigencia);

    /**
     * Anula el token del usuario, para que no pueda volver a usarse.
     *
     * @param idUsuario Identificador del usuario.
     */
    void anularToken(int idUsuario);

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja la cuenta de un usuario, quitándole el acceso al sistema.
     *
     * @param idUsuario Identificador del usuario.
     * @throws IllegalArgumentException Si el usuario no está activo o tiene dependencias activas.
     */
    void darDeBaja(int idUsuario);

    /**
     * Da de baja varias cuentas a la vez, todas o ninguna.
     *
     * @param ids Identificadores de los usuarios.
     * @throws IllegalArgumentException Si no se seleccionó ninguno o alguno no puede darse de baja.
     */
    void darDeBajaVarios(java.util.List<Integer> ids);

    /**
     * Modifica los datos del perfil que el propio usuario puede editar.
     *
     * @param idUsuario Identificador del usuario.
     * @param nombre Nombre nuevo.
     * @param apellido Apellido nuevo.
     * @param telefono Teléfono nuevo, o vacío para quitarlo.
     * @param imagen Foto de perfil nueva, o {@code null} si no cambia.
     * @param quitarImagen Indica si se quita la foto actual, cuando no se sube una nueva.
     * @return El usuario con los datos actualizados.
     * @throws IllegalArgumentException Si el usuario no está activo, algún dato no es válido o la imagen no se puede guardar.
     */
    Usuario modificarPerfil(int idUsuario, String nombre, String apellido, String telefono,
            org.springframework.web.multipart.MultipartFile imagen, boolean quitarImagen);

    /**
     * Cambia el rol por defecto de un usuario.
     *
     * @param idUsuario Identificador del usuario.
     * @param idRol Identificador del rol que pasa a ser el rol por defecto.
     * @return El rol establecido como rol por defecto.
     * @throws IllegalArgumentException Si el usuario no existe o no tiene ese rol vigente.
     */
    Rol cambiarRolPorDefecto(int idUsuario, Integer idRol);
}
