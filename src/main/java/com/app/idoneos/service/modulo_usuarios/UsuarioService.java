package com.app.idoneos.service.modulo_usuarios;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

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

