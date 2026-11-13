package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.app.idoneos.service.claseenvivo.ChatClaseService;
import com.app.idoneos.service.claseenvivo.ChatClaseService.Autor;

/** Pruebas del chat en memoria de las clases en vivo. */
class ChatClaseServiceTest {

    private final ChatClaseService chat = new ChatClaseService();

    private static Autor alumno(int id, String nombre) {
        return new Autor(id, nombre, null, "AP", false);
    }

    private static Autor docente() {
        return new Autor(99, "Fausto S.", "/img/fausto.png", "FS", true);
    }

    @Test
    @DisplayName("Rechaza el mensaje vacío o de más de 300 caracteres, y deja el texto como lo escribió, sin interpretarlo")
    void validaElMensaje() {
        assertThrows(IllegalArgumentException.class, () -> chat.enviar(1, alumno(10, "Ana P."), "   "));
        assertThrows(IllegalArgumentException.class, () -> chat.enviar(1, alumno(10, "Ana P."), "x".repeat(301)));

        chat.enviar(1, alumno(10, "Ana P."), "<b>hola</b>");

        assertEquals("<b>hola</b>", chat.historial(1).get(0).texto());
    }

    @Test
    @DisplayName("Un alumno no puede escribir un mensaje cada menos de 2 segundos, pero el docente sí")
    void limitaLaFrecuencia() {
        chat.enviar(1, alumno(10, "Ana P."), "uno");
        assertThrows(IllegalArgumentException.class, () -> chat.enviar(1, alumno(10, "Ana P."), "dos"));
        chat.enviar(1, alumno(11, "Beto R."), "otro alumno");
        chat.enviar(1, docente(), "uno");
        chat.enviar(1, docente(), "dos");

        assertEquals(4, chat.historial(1).size());
    }

    @Test
    @DisplayName("El docente elimina mensajes y deshabilita el chat: los alumnos ya no pueden escribir")
    void moderaElChat() {
        chat.enviar(1, alumno(10, "Ana P."), "mensaje");
        long id = chat.historial(1).get(0).id();

        chat.eliminar(1, id);
        chat.habilitar(1, false);

        assertTrue(chat.historial(1).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> chat.enviar(1, alumno(11, "Beto R."), "hola"));
        chat.enviar(1, docente(), "sigo yo");
    }

    @Test
    @DisplayName("Guarda solo los últimos 200 mensajes y borra todo al cerrar la clase")
    void guardaLosUltimosYBorraAlCerrar() {
        for (int i = 0; i < 250; i++) {
            chat.enviar(1, docente(), "mensaje " + i);
        }
        assertEquals(ChatClaseService.MENSAJES_GUARDADOS, chat.historial(1).size());
        assertEquals("mensaje 50", chat.historial(1).get(0).texto());

        chat.cerrar(1);

        assertTrue(chat.historial(1).isEmpty());
    }

    @Test
    @DisplayName("Cuenta solo a los alumnos que avisaron su presencia hace poco, y el mensaje lleva la foto de quien escribe")
    void cuentaLosConectadosYGuardaLaFoto() {
        chat.registrarPresencia(1, 10);
        chat.registrarPresencia(1, 11);
        chat.registrarPresencia(1, 10);
        chat.enviar(1, docente(), "hola");

        assertEquals(2, chat.conectados(1));
        assertEquals(0, chat.conectados(2));
        assertEquals("/img/fausto.png", chat.historial(1).get(0).imagen());
        assertEquals("FS", chat.historial(1).get(0).iniciales());
    }
}
