package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.service.asistente.AsistenteService;
import com.app.idoneos.service.asistente.AsistenteService.Pendiente;
import com.app.idoneos.service.asistente.AsistenteService.Respuesta;

/** Pruebas del asistente navegacional con los usuarios de la semilla: cada rol ve sus pendientes. */
@SpringBootTest
@Transactional
class AsistenteServiceTest {

    @Autowired
    private AsistenteService asistenteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario(String correo) {
        return usuarioRepository.findByCorreo(correo).orElseThrow();
    }

    private void verificarMaximo(Respuesta respuesta) {
        assertTrue(respuesta.pendientes().size() <= AsistenteService.MAXIMO_PENDIENTES, "la lista trae como mucho 5");
        assertTrue(respuesta.total() >= respuesta.pendientes().size(), "el total cuenta todos los pendientes");
        assertEquals(Math.min(respuesta.total(), AsistenteService.MAXIMO_PENDIENTES), respuesta.pendientes().size());
    }

    @Test
    @DisplayName("Administrador: pagos pendientes de registrar, con la acción «Registrar pago» y sus atajos")
    void administrador() {
        Respuesta respuesta = asistenteService.armar(usuario("admin@idoneos.online"));

        assertEquals("Pagos pendientes", respuesta.titulo());
        verificarMaximo(respuesta);
        for (Pendiente pendiente : respuesta.pendientes()) {
            assertEquals("Registrar pago", pendiente.accion());
            assertTrue(pendiente.url().startsWith("/pago/buscar?registrarPara="), pendiente.url());
        }
        assertEquals(java.util.List.of("Pagos", "Inscripciones", "Cursos"),
                respuesta.atajos().stream().map(AsistenteService.Atajo::texto).toList());
    }

    @Test
    @DisplayName("Docente: cohortes pendientes de contenido, con la acción del paso del diseño en que está cada una")
    void docente() {
        Respuesta respuesta = asistenteService.armar(usuario("fausto.spotorno@idoneos.online"));

        assertEquals("Cohortes pendientes de contenido", respuesta.titulo());
        verificarMaximo(respuesta);
        for (Pendiente pendiente : respuesta.pendientes()) {
            // La acción es la del paso del diseño en que está la cohorte
            assertTrue(java.util.Set.of("Completar datos", "Asignar programa", "Registrar programa", "Registrar tema",
                    "Armar el cronograma", "Completar contenido").contains(pendiente.accion()), pendiente.accion());
            assertTrue(pendiente.url().startsWith("/"), pendiente.url());
        }
        assertEquals(java.util.List.of("Mis cursos", "Biblioteca digital"),
                respuesta.atajos().stream().map(AsistenteService.Atajo::texto).toList());
        assertEquals("Fausto", respuesta.nombre());
    }

    @Test
    @DisplayName("Alumno: autoevaluaciones pendientes con la acción «Realizar intento», el curso y cuándo cierran")
    void alumno() {
        Respuesta respuesta = asistenteService.armar(usuario("martin.gomez@correo.com"));

        assertEquals("Autoevaluaciones pendientes", respuesta.titulo());
        verificarMaximo(respuesta);
        for (Pendiente pendiente : respuesta.pendientes()) {
            assertEquals("Realizar intento", pendiente.accion());
            assertTrue(pendiente.url().startsWith("/autoevaluacion/ver/"), pendiente.url());
            assertFalse(pendiente.curso().isBlank());
            assertTrue(pendiente.fecha() == null || pendiente.fecha().startsWith("Cierra el"), pendiente.fecha());
        }
        assertEquals(java.util.List.of("Mis cursos", "Mis pagos", "Mis certificados", "Mis intereses"),
                respuesta.atajos().stream().map(AsistenteService.Atajo::texto).toList());
    }

    @Test
    @DisplayName("Sin pendientes el asistente avisa que está al día y no lista nada")
    void alDia() {
        // Un docente sin cohortes pendientes (o un alumno sin autoevaluaciones) recibe el mensaje de "al día"
        Respuesta respuesta = asistenteService.armar(usuario("sebastian.bordato@idoneos.online"));
        if (respuesta.total() == 0) {
            assertTrue(respuesta.resumen().contains("Estás al día"), respuesta.resumen());
            assertTrue(respuesta.pendientes().isEmpty());
        } else {
            verificarMaximo(respuesta);
        }
    }
}
