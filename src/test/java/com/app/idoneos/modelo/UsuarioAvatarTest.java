package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de lo que el modelo del usuario deriva para mostrar su avatar: las iniciales y la dirección de la foto.
 */
class UsuarioAvatarTest {

    private Usuario usuario(String nombre, String apellido, String imagen) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setImagen(imagen);
        return usuario;
    }

    @Test
    @DisplayName("Las iniciales son las del nombre y el apellido, en mayúscula")
    void iniciales() {
        assertEquals("VR", usuario("Valentina", "Ruiz", null).getIniciales());
        assertEquals("FS", usuario("fausto", "spotorno", null).getIniciales());
        assertEquals("A", usuario("Ana", null, null).getIniciales());
    }

    @Test
    @DisplayName("La foto de perfil solo tiene dirección si el usuario cargó una")
    void urlDeLaFoto() {
        assertNull(usuario("Ana", "Gil", null).getUrlImagen());
        assertNull(usuario("Ana", "Gil", " ").getUrlImagen());
        assertEquals("/img/usuarios/ana.jpg", usuario("Ana", "Gil", "ana.jpg").getUrlImagen());
    }
}
