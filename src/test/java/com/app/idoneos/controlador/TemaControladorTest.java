package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de temas (CU-19 a CU-22) y de lo que cambia en la vista del curso y
 * en el menú lateral. Fausto Spotorno es titular de "Mercado de Capitales Argentino" (tres temas, con inscripciones) y
 * de "Macroeconomía de Coyuntura" (sin inscripciones).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TemaControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Curso curso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
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

    // ------------------------------------------------------------------ Listado

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve el cronograma de su programa por defecto con todos los datos de cada tema")
    void listadoDelDocente() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de temas")))
                .andExpect(content().string(Matchers.containsString("Cronograma de " + programaDe(macro()).getNombre())))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + tema.getTitulo() + "<")))
                // los datos propios del tema, y cada uno se puede ocultar con "Columnas"
                .andExpect(content().string(Matchers.containsString("data-col=\"Orden\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Título\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Descripción\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Contenido\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Duración\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Última modificación\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Fecha de creación\"")))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar tema")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                // baja masiva sobre el programa del listado
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.containsString("/tema/darDeBajaMasiva?programaId=" + programaId(macro()))))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarTema" + tema.getIdTema() + "\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaTema" + tema.getIdTema() + "\"")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El formulario de alta elige el programa por defecto, marcado como tal, y permite incorporar un tema existente")
    void formularioDeAlta() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("id=\"modalNuevaTema\"")))
                .andExpect(content().string(Matchers.containsString("name=\"programaId\"")))
                .andExpect(content().string(Matchers.containsString("selected=\"selected\">" + programaDe(macro()).getNombre() + " (Por defecto)")))
                .andExpect(content().string(Matchers.containsString("Un tema de otro programa del curso")))
                .andExpect(content().string(Matchers.containsString("name=\"temaExistenteId\"")))
                .andExpect(content().string(Matchers.containsString("data-programas=\"" + programaId(macro()) + "\"")))
                .andExpect(content().string(Matchers.containsString("/js/tema.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Con ?nuevo o ?accion el listado abre solo la ventana correspondiente")
    void abreLaVentana() throws Exception {
        Tema tema = primeraDe(macro());
        String curso = cursoId(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", curso).param("nuevo", ""))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalNuevaTema\"")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", curso).param("accion", "editar")
                .param("temaId", String.valueOf(tema.getIdTema())))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalEditarTema" + tema.getIdTema() + "\"")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", curso).param("accion", "baja")
                .param("temaId", String.valueOf(tema.getIdTema())))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalBajaTema" + tema.getIdTema() + "\"")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", curso))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El título que llega por la URL no recorta el listado: se filtra en la pantalla")
    void filtro() throws Exception {
        Tema tema = primeraDe(mercado());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(mercado())).param("titulo", "renta fija"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Renta fija y bonos<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + tema.getTitulo() + "<")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y solo puede dar de baja")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalEditarTema"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaTema")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/tema/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al ABM de temas")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(mercado()))).andExpect(status().isForbidden());
        mockMvc.perform(post("/tema/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/tema/modificar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/tema/darDeBaja/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Formularios

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-20: agregar un tema responde en JSON y el error de validación en 400")
    void agregarTema() throws Exception {
        mockMvc.perform(post("/tema/registrar").param("programaId", programaId(macro())).param("titulo", "Política monetaria")
                .param("contenido", "Tasas y agregados monetarios"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("registrada con éxito")));
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Política monetaria<")));

        mockMvc.perform(post("/tema/registrar").param("programaId", programaId(macro())).param("titulo", "")
                .param("contenido", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el título y el contenido.")));
        mockMvc.perform(post("/tema/registrar").param("programaId", programaId(macro())).param("titulo", "política monetaria")
                .param("contenido", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Ya existe un tema con ese título")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-20: incorporar un tema existente de otro programa responde que fue incorporada")
    void incorporarTema() throws Exception {
        Tema tema = primeraDe(macro());
        mockMvc.perform(post("/programa/registrar").param("cursoId", cursoId(macro())).param("nombre", "Programa 2027")
                .param("objetivos", "Obj").param("bibliografia", "Bib")).andExpect(status().isOk());

        mockMvc.perform(post("/tema/registrar").param("programaId", programaId(macro()))
                .param("temaExistenteId", String.valueOf(tema.getIdTema())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("incorporada con éxito")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-21: modificar un tema responde en JSON y se rechaza si tiene inscripciones activas")
    void modificarTema() throws Exception {
        Tema sinInscripciones = primeraDe(macro());
        Tema conInscripciones = primeraDe(mercado());

        mockMvc.perform(post("/tema/modificar/" + sinInscripciones.getIdTema()).param("titulo", "Otro título")
                .param("contenido", "Otro contenido"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema modificada correctamente."));
        mockMvc.perform(post("/tema/modificar/" + conInscripciones.getIdTema()).param("titulo", "Otro")
                .param("contenido", "Otro contenido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("tiene inscripciones activas asociadas")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-22: dar de baja un tema responde en JSON y se rechaza si el programa tiene inscripciones activas")
    void darDeBajaTema() throws Exception {
        Tema tema = primeraDe(macro());
        Tema deMercado = primeraDe(mercado());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()).param("programaId", programaId(macro())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema dada de baja con éxito."));
        mockMvc.perform(post("/tema/darDeBaja/" + deMercado.getIdTema()).param("programaId", programaId(mercado())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el tema")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-22: la baja masiva es de todas o ninguna")
    void bajaMasiva() throws Exception {
        Tema uno = primeraDe(macro());
        Tema deMercado = primeraDe(mercado());

        mockMvc.perform(post("/tema/darDeBajaMasiva").param("programaId", programaId(macro()))
                .param("ids", String.valueOf(uno.getIdTema()), String.valueOf(deMercado.getIdTema())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no pertenece al cronograma")));
        mockMvc.perform(post("/tema/darDeBajaMasiva").param("programaId", programaId(macro())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe seleccionar al menos un tema")));
        mockMvc.perform(post("/tema/darDeBajaMasiva").param("programaId", programaId(macro()))
                .param("ids", String.valueOf(uno.getIdTema())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Temas dadas de baja con éxito."));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador da de baja pero no agrega ni modifica")
    void administrador() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()).param("programaId", programaId(macro())))
                .andExpect(status().isOk());
        mockMvc.perform(post("/tema/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/tema/modificar/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Menú lateral y vista del curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.containsString("/tema/buscar?cursoId=" + mercado().getIdCurso())));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve la descripción y el contenido del tema, pero ningún botón de gestión")
    void vistaDelCursoDelAlumno() throws Exception {
        Tema tema = primeraDe(mercado());
        int inscripcion = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();

        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ac-tema-contenido")))
                .andExpect(content().string(Matchers.containsString(tema.getContenido().trim().split("\\s+")[0])))
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-menu-tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/tema/buscar"))));
    }
}
