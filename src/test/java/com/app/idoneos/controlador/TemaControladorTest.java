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
        Curso curso = cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
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

    // ------------------------------------------------------------------ Listado

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve los temas del curso con los programas que los incluyen y si tienen su contenido")
    void listadoDelDocente() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de temas")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + tema.getTitulo() + "<")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Tema\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"En programas\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Contenido\"")))
                .andExpect(content().string(Matchers.containsString(programaDe(macro()).getNombre())))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar tema")))
                .andExpect(content().string(Matchers.containsString("data-editar-tema")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaTema" + tema.getIdTema() + "\"")))
                // el listado ya no es el del cronograma de un programa
                .andExpect(content().string(Matchers.not(Matchers.containsString("Cronograma de "))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-baja-masiva"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
                .andExpect(content().string(Matchers.containsString("/js/temas.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
                .andExpect(content().string(Matchers.containsString("Listado de temas")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar tema"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalTemas\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-editar-tema"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaTema")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Guardar los temas registra los nuevos y modifica los existentes en un solo paso, y propone armar el programa")
    void guardarTemas() throws Exception {
        Tema existente = primeraDe(macro());
        String cambios = "[{\"id\":null,\"titulo\":\"Política monetaria\",\"descripcion\":\"\",\"contenido\":\"Tasas\"},"
                + "{\"id\":" + existente.getIdTema() + ",\"titulo\":\"Otro título\",\"descripcion\":\"\",\"contenido\":\"Otro\"}]";

        mockMvc.perform(post("/tema/guardar").param("cursoId", cursoId(macro())).param("cambios", cambios))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("2 temas guardados correctamente."))
                // el programa vigente no asigna toda la duración del curso: se guía al docente
                .andExpect(jsonPath("$.siguienteUrl").value(Matchers.containsString("/programa/buscar?cursoId=" + cursoId(macro()))));

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
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Dar de baja un tema sin indicar programa lo da de baja en el curso")
    void darDeBajaDelCurso() throws Exception {
        Tema tema = primeraDe(macro());

        mockMvc.perform(post("/tema/darDeBaja/" + tema.getIdTema()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema dada de baja con éxito."));
        mockMvc.perform(get("/tema/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalBajaTema" + tema.getIdTema() + "\""))));
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
    @DisplayName("CU-20: con el cronograma lleno el error se marca en el campo de la duración")
    void agregarTemaConElCronogramaLleno() throws Exception {
        String programaId = programaId(macro());
        Programa programa = programaDe(macro());
        // el curso no tiene lugar: la duración es justo la del cronograma
        programa.getCurso().setDuracionDias(programa.getDuracionTotalDias());

        mockMvc.perform(post("/tema/registrar").param("programaId", programaId).param("titulo", "Sin lugar")
                .param("contenido", "Contenido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("La duración del cronograma ya alcanzó")))
                .andExpect(jsonPath("$.campo").value("duracion"));
    }

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
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el título del tema.")));
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
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Tema modificado correctamente."));
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
