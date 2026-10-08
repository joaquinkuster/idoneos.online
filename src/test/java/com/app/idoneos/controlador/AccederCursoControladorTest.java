package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de la pantalla "Acceder curso" del alumno (CU-27, MOD-F-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccederCursoControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    /** Identificador de la inscripción de un alumno de la semilla, según su correo. */
    private int inscripcionDe(String correo) {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el acordeón muestra las unidades completada, en curso y bloqueada con su contenido publicado")
    void acordeonDeUnidades() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Introducción al mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Completada")))
                .andExpect(content().string(Matchers.containsString("En curso")))
                .andExpect(content().string(Matchers.containsString("Bloqueada")))
                // contenido de la unidad habilitada: material, glosario, autoevaluación con intentos y foro
                .andExpect(content().string(Matchers.containsString("Grabación: Estructura del mercado argentino")))
                .andExpect(content().string(Matchers.containsString("Comisión Nacional de Valores")))
                .andExpect(content().string(Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Intento 2")))
                .andExpect(content().string(Matchers.containsString("Foro de consultas")))
                // la unidad bloqueada no entrega su contenido
                .andExpect(content().string(Matchers.containsString("Completá la unidad anterior")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: la barra lateral se agrupa por Curso, Programa, Cohorte y contenido, y los enlaces del módulo van en la barra superior")
    void barraLateralYBarraSuperior() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Este curso")))
                .andExpect(content().string(Matchers.containsString("Este programa")))
                .andExpect(content().string(Matchers.containsString("Esta cohorte")))
                .andExpect(content().string(Matchers.containsString("Contenido de las unidades")))
                .andExpect(content().string(Matchers.containsString("Programas")))
                .andExpect(content().string(Matchers.containsString("Participantes")))
                .andExpect(content().string(Matchers.containsString("Calificaciones")))
                .andExpect(content().string(Matchers.containsString("Glosario")))
                .andExpect(content().string(Matchers.containsString("Foros")))
                .andExpect(content().string(Matchers.containsString("pn-barra-enlace")))
                .andExpect(content().string(Matchers.containsString("data-sin-accion")))
                .andExpect(content().string(Matchers.containsString("En directo ahora")))
                // sin categoría en el breadcrumb, sin progreso ni pestañas internas
                .andExpect(content().string(Matchers.not(Matchers.containsString("Progreso general"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Clon IA"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-tab"))));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el cronograma indica la semana esperada y que el alumno va al día")
    void cronograma() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com"))
                .param("seccion", "cronograma"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Cronograma del programa")))
                .andExpect(content().string(Matchers.containsString("Semana esperada: 3 de 9")))
                .andExpect(content().string(Matchers.containsString("Vas al día con el cronograma.")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: las clases en vivo de la cohorte se listan con su estado")
    void clasesEnVivo() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com"))
                .param("seccion", "clases"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Clase 1: Panorama del mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Programada")))
                .andExpect(content().string(Matchers.containsString("Finalizada")));
    }

    @Test
    @WithUserDetails(value = "lucia.fernandez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: si el dictado de la cohorte todavía no comenzó, se avisa y se vuelve a Mis cursos")
    void dictadoSinComenzar() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("lucia.fernandez@correo.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripcion/misCursos"))
                .andExpect(flash().attribute("error", Matchers.containsString("El dictado de tu cohorte comienza el")));
    }

    @Test
    @WithUserDetails(value = "martin.gomez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: un alumno no puede acceder a la inscripción de otro alumno")
    void inscripcionAjena() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripcion/misCursos"))
                .andExpect(flash().attribute("error", Matchers.containsString("No tenés una inscripción vigente")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La barra lateral reutilizable sigue mostrando el menú del panel del administrador")
    void barraLateralDelAdministrador() throws Exception {
        mockMvc.perform(get("/categoria/buscar")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("pn-menu-enlace")))
                .andExpect(content().string(Matchers.containsString("Cohortes")))
                .andExpect(content().string(Matchers.containsString("Panel del administrador")));
    }
}
