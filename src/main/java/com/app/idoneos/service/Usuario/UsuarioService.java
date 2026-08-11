package com.app.idoneos.service.Usuario;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.model.Usuario;

import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con los usuarios (CU-75 a CU-88).
 */
public interface UsuarioService {

    Optional<Usuario> buscarPorCorreo(String correo);

    Usuario procesarUsuarioOAuth2(String email, String nombre, String apellido, String googleSub);

    long contarAdministradoresActivos();

    Docente registrarDocente(String nombre, String apellido, String correo, String telefono, String biografia, Integer aniosExperiencia);

    Usuario registrarAlumno(String nombre, String apellido, String correo, String contrasena);

    Usuario registrarAdministrador(String nombre, String apellido, String correo, String contrasena);

    Usuario crearUsuarioConRol(String nombre, String apellido, String correo, String contrasena, RolUsuario rol);

    String generarTokenRecuperacion(String correo);
}
