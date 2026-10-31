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
    @DisplayName("CU-27: el acordeón muestra los temas completado, en curso y bloqueada con su contenido publicado")
    void acordeonDeTemas() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Introducción al mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Completada")))
                .andExpect(content().string(Matchers.containsString("En curso")))
                .andExpect(content().string(Matchers.containsString("Bloqueada")))
                // contenido del tema habilitado: material, glosario, autoevaluación con intentos y foro
                .andExpect(content().string(Matchers.containsString("Grabación: Estructura del mercado argentino")))
                .andExpect(content().string(Matchers.containsString("Autoevaluación Tema 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Foro de consultas")))
                .andExpect(content().string(Matchers.containsString("Glosario")))
                // el alumno no ve el material oculto ni las opciones del docente
                .andExpect(content().string(Matchers.not(Matchers.containsString("Borrador: ejercicios de duration"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Agregar recurso o actividad"))))
                // el tema bloqueado no entrega su contenido
                .andExpect(content().string(Matchers.containsString("Completá el tema anterior")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: la barra lateral se agrupa por Curso, Programa, Cohorte y contenido, y los enlaces del módulo van en la barra superior")
    void barraLateralYBarraSuperior() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                // "Curso" va arriba; el grupo "Este curso" (Temas y Programas) es de gestión y el alumno no lo ve
                .andExpect(content().string(Matchers.not(Matchers.containsString("Este curso"))))
                .andExpect(content().string(Matchers.containsString("Este programa")))
                .andExpect(content().string(Matchers.containsString("Esta cohorte")))
                .andExpect(content().string(Matchers.containsString("Contenido de los temas")))
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.containsString("Participantes")))
                .andExpect(content().string(Matchers.containsString("Calificaciones")))
                .andExpect(content().string(Matchers.containsString("Cronograma")))
                // el alumno no ve los botones del menú que son de gestión (no puede abrirlos)
                .andExpect(content().string(Matchers.not(Matchers.containsString("/tema/buscar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/programa/buscar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/material/buscar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/glosario/buscar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/foro/buscar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-menu-tema"))))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Inicio<")))
                .andExpect(content().string(Matchers.containsString("data-sin-accion")))
                // sin categoría en el breadcrumb, sin progreso ni pestañas internas
                .andExpect(content().string(Matchers.not(Matchers.containsString("Progreso general"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Clon IA"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-tab"))));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve el cajón del cronograma con la semana actual, su estado y el estado de cada tema")
    void alumnoVeElCajonDelCronograma() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("id=\"cajonCronograma\"")))
                .andExpect(content().string(Matchers.containsString("ac-cajon-semana")))
                .andExpect(content().string(Matchers.containsString("data-cajon-alternar")))
                .andExpect(content().string(Matchers.containsString("ac-cajon-barra")))
                .andExpect(content().string(Matchers.containsString("ac-cajon-leyenda")))
                .andExpect(content().string(Matchers.anyOf(Matchers.containsString("Al día"),
                        Matchers.containsString("de atraso"))));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La alumna atrasada ve en el cajón sus semanas de atraso, en la línea de la semana, y la leyenda de la línea roja")
    void alumnaAtrasadaVeElAtrasoEnElCajon() throws Exception {
        int atrasada = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .filter(i -> i.getCurso().getNombre().equals("Análisis Técnico Bursátil"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();

        mockMvc.perform(get("/inscripcion/acceder/" + atrasada))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("20 días de atraso")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Al día"))))
                .andExpect(content().string(Matchers.containsString("ac-cajon-actual")))
                .andExpect(content().string(Matchers.containsString("Hoy (día actual)")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno también ve la portada del curso en la página principal")
    void alumnoVeLaPortada() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("class=\"ac-portada\"")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el cronograma marca la semana actual y que el alumno va al día")
    void cronograma() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDe("valentina.ruiz@correo.com"))
                .param("seccion", "cronograma"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-dia-actual=\"21\"")))
                .andExpect(content().string(Matchers.containsString("Vas al día")))
                .andExpect(content().string(Matchers.containsString("Hoy (día actual)")))
                // el mismo tablero que ve el docente cuando hay inscripciones: solo lectura (sin paneles ni botones)
                .andExpect(content().string(Matchers.containsString("id=\"cronograma\"")))
                .andExpect(content().string(Matchers.containsString("data-editable=\"false\"")))
                .andExpect(content().string(Matchers.containsString("data-vista=\"temas\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-panel-disponibles"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-modificar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Borrador: ejercicios de duration"))));
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
                // "Mis cursos" está en el menú del usuario, debajo de "Panel administrador"
                .andExpect(content().string(Matchers.containsString("/curso/misCursos")))
                .andExpect(content().string(Matchers.containsString("/cohorte/buscar")))
                .andExpect(content().string(Matchers.containsString("Panel del administrador")));
    }
}
