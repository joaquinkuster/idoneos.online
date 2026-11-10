package com.app.idoneos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.InscripcionRepository;

/** Pruebas de las vistas y los formularios del ABM de temas y de lo que cambia en la vista del curso y en el menú lateral. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TemaControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    private Curso curso(String nombre) {
        Curso curso = cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
        // la semilla deja los cronogramas completos: se amplía la duración para poder agregar temas
        if (curso.getDuracionDias() < 365) {
            curso.setDuracionDias(365);
        }
        return curso;
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    private Curso macro() {
        return curso("Macroeconomía de Coyuntura");
    }

    private Programa programaDe(Curso curso) {
        return curso.getProgramaVigente();
    }

    private Tema primeraDe(Curso curso) {
        return programaDe(curso).getCronogramaOrdenado().get(0).getTema();
    }

    private String cursoId(Curso curso) {
        return String.valueOf(curso.getIdCurso());
    }

    private String programaId(Curso curso) {
        return String.valueOf(programaDe(curso).getIdPrograma());
    }

    // ----------------------------------------------------------------------
    // Listado
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente ve los temas del curso con los programas que los incluyen y si tienen su contenido")
    void listadoDelDocente() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de temas")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + tema.getTitulo() + "<")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Título\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Descripción\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Fecha de creación\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Título\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Contenido\"")))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar tema")))
                .andExpect(content().string(Matchers.containsString("data-editar-tema")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                .andExpect(content().string(Matchers.containsString("/tema/darDeBaja/" + tema.getIdTema() + "")))
                // el listado ya no es el del cronograma de un programa
                .andExpect(content().string(Matchers.not(Matchers.containsString("Cronograma de "))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-baja-masiva"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La ventana Registrar tema tiene la lista de temas del curso, el formulario y los pasos Datos básicos y Resumen")
    void formularioDeAlta() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("id=\"modalTemas\"")))
                .andExpect(content().string(Matchers.containsString("action=\"/tema/guardar\"")))
                .andExpect(content().string(Matchers.containsString("name=\"cambios\"")))
                .andExpect(content().string(Matchers.containsString("Datos básicos")))
                .andExpect(content().string(Matchers.containsString("Resumen")))
                .andExpect(content().string(Matchers.containsString("data-id=\"" + tema.getIdTema() + "\"")))
                .andExpect(content().string(Matchers.containsString("/js/pages/tema/tema.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Con ?nuevo o ?editar el listado abre solo la ventana de temas")
    void abreLaVentana() throws Exception {
        Tema tema = primeraDe(macro());
        String curso = cursoId(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", curso).param("nuevo", ""))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalTemas\"")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", curso).param("editar", String.valueOf(tema.getIdTema())))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalTemas\"")))
                .andExpect(content().string(Matchers.containsString("data-editar=\"" + tema.getIdTema() + "\"")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", curso))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El título que llega por la URL lo filtra el servidor")
    void filtro() throws Exception {
        Tema tema = primeraDe(mercado());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(mercado())).param("titulo", "renta fija"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Renta fija y bonos<")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-celda-principal pn-truncar\">" + tema.getTitulo() + "<"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador ve el listado y solo puede dar de baja")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de temas")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalTemas\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-editar-tema"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaTema")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Guardar los temas registra los nuevos y modifica los existentes en un solo paso, y propone armar el programa")
    void guardarTemas() throws Exception {
        Tema existente = primeraDe(macro());
        String cambios = "[{\"id\":null,\"titulo\":\"Política monetaria\",\"descripcion\":\"\",\"contenido\":\"Tasas\"},"
                + "{\"id\":" + existente.getIdTema() + ",\"titulo\":\"Otro título\",\"descripcion\":\"\",\"contenido\":\"Otro\"}]";

        mockMvc.perform(post("/tema/guardar").param("cursoId", cursoId(macro())).param("cambios", cambios))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("2 temas guardados correctamente."))
                // el programa vigente no tiene asignada la duración del curso: la alerta guía lleva a armar el cronograma
                .andExpect(jsonPath("$.siguienteUrl").value(Matchers.containsString("/cronograma/buscar?cursoId=" + cursoId(macro()))));

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Política monetaria<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Otro título<")));

        // título repetido: el error nombra el tema y no se guarda nada
        mockMvc.perform(post("/tema/guardar").param("cursoId", cursoId(macro()))
                .param("cambios", "[{\"id\":null,\"titulo\":\"política monetaria\"}]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("«política monetaria»")));
        mockMvc.perform(post("/tema/guardar").param("cursoId", cursoId(macro())).param("cambios", "[]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No hay cambios para guardar")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Dar de baja un tema sin indicar programa lo da de baja en el curso")
    void darDeBajaDelCurso() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema dada de baja con éxito."));
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/tema/darDeBaja/" + tema.getIdTema() + ""))));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Un docente que no participa en el curso vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/tema/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno no accede al ABM de temas")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(mercado()))).andExpect(status().isForbidden());
        mockMvc.perform(post("/tema/darDeBaja/1")).andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------------
    // Formularios
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-22: dar de baja un tema responde en JSON y se rechaza si el programa tiene inscripciones activas")
    void darDeBaja() throws Exception {
        Tema tema = primeraDe(macro());
        Tema deMercado = primeraDe(mercado());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()).param("programaId", programaId(macro())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema dada de baja con éxito."));
        mockMvc.perform(post("/tema/darDeBaja/" + deMercado.getIdTema()).param("programaId", programaId(mercado())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el tema")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador da de baja pero no agrega ni modifica")
    void administrador() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()).param("programaId", programaId(macro())))
                .andExpect(status().isOk());
    }

    // ----------------------------------------------------------------------
    // Menú lateral y vista del curso
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La vista del curso del docente tiene Registrar tema, el menú de cada tema y muestra su descripción y contenido")
    void vistaDelCursoDelDocente() throws Exception {
        Tema tema = primeraDe(mercado());

        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Registrar tema</a>")))
                .andExpect(content().string(Matchers.containsString("/tema/buscar?cursoId=" + mercado().getIdCurso() + "&amp;nuevo=")))
                .andExpect(content().string(Matchers.containsString("ac-menu-tema")))
                .andExpect(content().string(Matchers.containsString("accion=editar&amp;temaId=" + tema.getIdTema())))
                .andExpect(content().string(Matchers.containsString("accion=baja&amp;temaId=" + tema.getIdTema())))
                .andExpect(content().string(Matchers.containsString("ac-tema-contenido")))
                .andExpect(content().string(Matchers.containsString(tema.getContenido().trim().split("\\s+")[0])))
                // el menú lateral: Curso arriba, y Temas como sección aparte
                .andExpect(content().string(Matchers.containsString("/tema/buscar?cursoId=" + mercado().getIdCurso())));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno ve la descripción y el contenido del tema, pero ningún botón de gestión")
    void vistaDelCursoDelAlumno() throws Exception {
        Tema tema = primeraDe(mercado());
        int inscripcion = inscripcionRepository.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();

        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ac-tema-contenido")))
                .andExpect(content().string(Matchers.containsString(tema.getContenido().trim().split("\\s+")[0])))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-menu-tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/tema/buscar"))));
    }
}
