package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.RolRepositorio;
import com.app.idoneos.repositorio.RolUsuarioRepositorio;
import com.app.idoneos.servicio.Usuario.UsuarioServicioImpl;

/**
 * Pruebas del rol por defecto del usuario, sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class UsuarioServicioTest {

    @Autowired
    private UsuarioServicioImpl usuarioServicio;

    @Autowired
    private RolRepositorio rolRepositorio;

    @Autowired
    private RolUsuarioRepositorio rolUsuarioRepositorio;

    private Usuario docente() {
        return usuarioServicio.buscarPorCorreo("fausto.spotorno@idoneos.online").orElseThrow();
    }

    private Rol rol(String nombre) {
        return rolRepositorio.findAll().stream().filter(r -> r.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("El rol activo es el rol por defecto del usuario")
    void rolActivoEsElRolPorDefecto() {
        Usuario usuario = docente();

        assertEquals("Docente", usuario.getRolActivo().getNombre());
        assertTrue(usuario.esDocenteActivo());
    }

    @Test
    @DisplayName("Cambia el rol por defecto a otro de los roles vigentes del usuario")
    void cambiarRolPorDefecto() {
        Usuario usuario = docente();
        usuario.getRoles().add(rolUsuarioRepositorio.save(new RolUsuario(rol("Alumno"), usuario)));

        Rol nuevo = usuarioServicio.cambiarRolPorDefecto(usuario.getIdUsuario(), rol("Alumno").getIdRol());

        assertEquals("Alumno", nuevo.getNombre());
        assertTrue(usuario.esAlumnoActivo());
    }

    @Test
    @DisplayName("No cambia el rol por defecto a un rol que el usuario no tiene")
    void cambiarRolPorDefectoDeUnRolAjeno() {
        Usuario usuario = docente();

        assertThrows(IllegalArgumentException.class,
                () -> usuarioServicio.cambiarRolPorDefecto(usuario.getIdUsuario(), rol("Administrador").getIdRol()));
        assertThrows(IllegalArgumentException.class, () -> usuarioServicio.cambiarRolPorDefecto(usuario.getIdUsuario(), null));
    }
}
