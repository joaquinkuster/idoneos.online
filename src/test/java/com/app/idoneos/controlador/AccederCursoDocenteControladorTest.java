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

import com.app.idoneos.repositorio.CursoRepositorio;

/**
 * Pruebas de la pantalla "Acceder curso" del docente (CU-27, MOD-F-02).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccederCursoDocenteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    private int idCurso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow()
                .getIdCurso();
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve el cajón del cronograma con el estado de las unidades y sin avisos de atraso")
    void docenteVeElCajonDelCronograma() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("id=\"cajonCronograma\"")))
                .andExpect(content().string(Matchers.containsString("ac-cajon-leyenda")))
                .andExpect(content().string(Matchers.containsString("data-cajon-alternar")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("de atraso"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Al día"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el docente ve todas las unidades habilitadas, con su contenido y sin avance")
    void docenteVeLasUnidadesSinBloqueos() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Agregar recurso o actividad")))
                .andExpect(content().string(Matchers.containsString("Agregar un recurso o actividad")))
                // las actividades de la unidad se agrupan con títulos
                .andExpect(content().string(Matchers.containsString("ac-grupo-titulo")))
                .andExpect(content().string(Matchers.containsString(">Autoevaluaciones<")))
                .andExpect(content().string(Matchers.containsString(">Foro<")))
                // cada grupo tiene su enlace "Ver listado"; el de materiales lleva al ABM de la unidad
                .andExpect(content().string(Matchers.containsString("Ver listado")))
                .andExpect(content().string(Matchers.containsString("/material/ver/")))
                .andExpect(content().string(Matchers.containsString("/material/buscar?cursoId=")))
                // el docente ve también el material oculto, marcado como tal
                .andExpect(content().string(Matchers.containsString("Borrador: ejercicios de duration")))
                .andExpect(content().string(Matchers.containsString("Oculto")))
                .andExpect(content().string(Matchers.containsString("Introducción al mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("Renta variable y opciones")))
                .andExpect(content().string(Matchers.containsString("Autoevaluación Unidad 1: Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Este curso")))
                // sin bloqueos, sin avance del alumno ni intentos propios
                .andExpect(content().string(Matchers.not(Matchers.containsString("Completá la unidad anterior"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Bloqueada"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Sin intentos"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Acceso hasta"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el cronograma del docente (en su propia página) muestra la semana de la cohorte pero no el atraso del alumno")
    void docenteVeElCronogramaSinAtraso() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Semana actual de la cohorte: 3 de 9")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Vas al día"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Vas por detrás"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La página principal del curso tiene su portada con la imagen del curso, y las otras secciones no")
    void portadaConImagen() throws Exception {
        int id = idCurso("Mercado de Capitales Argentino");

        mockMvc.perform(get("/curso/acceder/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("class=\"ac-portada\"")))
                .andExpect(content().string(Matchers.containsString("Portada de Mercado de Capitales Argentino")))
                // el botón "Curso" vuelve a ser un botón del menú, sin tarjeta
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-menu-curso"))));
        mockMvc.perform(get("/curso/acceder/" + id).param("seccion", "clases")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-portada"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un curso sin imagen muestra la portada con el degradé, sin imagen")
    void portadaSinImagen() throws Exception {
        com.app.idoneos.modelo.Curso curso = cursoRepositorio.findById(idCurso("Macroeconomía de Coyuntura")).orElseThrow();
        curso.setImagen(null);

        mockMvc.perform(get("/curso/acceder/" + curso.getIdCurso())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("class=\"ac-portada\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Portada de Macroeconomía"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el docente ve las clases en vivo de la cohorte")
    void docenteVeLasClasesEnVivo() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")).param("seccion", "clases"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Clase 1: Panorama del mercado de capitales")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: un docente no puede acceder a un curso en el que no participa")
    void docenteAjenoAlCurso() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Macroeconomía de Coyuntura")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"))
                .andExpect(flash().attribute("error", Matchers.containsString("No participás en este curso")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-27: el alumno no puede usar el acceso del docente")
    void alumnoNoAccedeComoDocente() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + idCurso("Mercado de Capitales Argentino")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El menú del docente ya no tiene la opción Mis cohortes")
    void menuDelDocenteSinMisCohortes() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Mis cohortes"))))
                .andExpect(content().string(Matchers.containsString("/curso/acceder/")));
    }
}
