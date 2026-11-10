package com.app.idoneos.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.DocenteCursoRepository;

/** Pruebas de las vistas y los formularios del ABM de programas y de los selectores de programa y cohorte por defecto de la vista del curso. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProgramaControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DocenteCursoRepository participacionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    private Curso curso(String nombre) {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    private Curso macro() {
        return curso("Macroeconomía de Coyuntura");
    }

    private DocenteCurso participacionDeFausto(Curso curso) {
        return participacionRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getUsuario().getCorreo().equals(FAUSTO)).findFirst().orElseThrow();
    }

    private void registrar(Curso curso, String nombre) throws Exception {
        mockMvc.perform(post("/programa/registrar").param("cursoId", String.valueOf(curso.getIdCurso()))
                .param("nombre", nombre).param("objetivos", "Objetivos"))
                .andExpect(status().isOk());
    }

    // ----------------------------------------------------------------------
    // Listado
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente ve los programas del curso con su estado, objetivos y bibliografía")
    void listadoDelDocente() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(mercado().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de programas")))
                .andExpect(content().string(Matchers.containsString("Programa 2026")))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar programa")))
                .andExpect(content().string(Matchers.containsString("id=\"modalPrograma\"")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                // columnas: los datos propios del programa (sin objetivos, que son del curso, ni bibliografía o dedicación docente)
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Objetivos\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Bibliografía\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Dedicación docente\""))))
                .andExpect(content().string(Matchers.containsString("data-col=\"Descripción\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Última modificación\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Nombre\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Inscriptos\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Temas\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Con material\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Semanas\""))))
                // baja masiva
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.containsString("/programa/darDeBajaMasiva")))
                .andExpect(content().string(Matchers.containsString("data-seleccionar-fila")))
                // las acciones no se bloquean: la regla se informa al intentar la operación
                .andExpect(content().string(Matchers.containsString("data-programa-editar")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPrograma")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("(Por defecto)"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("★"))))
                // el asistente de programas: datos básicos y resumen, sin el cronograma ni el contenido
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-matriz"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Temas disponibles"))))
                .andExpect(content().string(Matchers.containsString("/js/pages/programa/programa.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Los programas anteriores también se pueden editar y dar de baja desde el listado")
    void accionesHabilitadas() throws Exception {
        int anterior = macro().getProgramaVigente().getIdPrograma();
        registrar(macro(), "Programa 2027");
        Programa nuevo = macro().getProgramaVigente();

        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-id=\"" + nuevo.getIdPrograma() + "\"")))
                .andExpect(content().string(Matchers.containsString("/programa/darDeBaja/" + nuevo.getIdPrograma() + "")))
                .andExpect(content().string(Matchers.containsString("Anterior")))
                // el programa anterior (con una cohorte activa) conserva sus acciones
                .andExpect(content().string(Matchers.containsString("data-id=\"" + anterior + "\"")))
                .andExpect(content().string(Matchers.containsString("/programa/darDeBaja/" + anterior + "")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El servidor filtra el listado por las columnas que llegan en la URL y no recorta por lo que no es columna")
    void filtros() throws Exception {
        registrar(macro(), "Programa Avanzado");
        String curso = String.valueOf(macro().getIdCurso());

        mockMvc.perform(get("/programa/buscar").param("cursoId", curso).param("nombre", "avanzado"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa Avanzado<")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-celda-principal pn-truncar\">Programa 2026<"))));
        mockMvc.perform(get("/programa/buscar").param("cursoId", curso).param("estado", "Anterior"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa 2026<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa Avanzado<")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Un programa dado de baja deja de figurar en el listado")
    void noListaLosDadosDeBaja() throws Exception {
        registrar(macro(), "Por error");
        int id = macro().getProgramaVigente().getIdPrograma();
        String curso = String.valueOf(macro().getIdCurso());
        mockMvc.perform(get("/programa/buscar").param("cursoId", curso))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Por error<")));

        mockMvc.perform(post("/programa/darDeBaja/" + id)).andExpect(status().isOk());

        mockMvc.perform(get("/programa/buscar").param("cursoId", curso))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-celda-principal pn-truncar\">Por error<"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("value=\"Dado de baja\""))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador ve el listado y solo puede dar de baja")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Programa 2026")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar programa"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalModificarPrograma"))));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Un docente que no participa en el curso no lista sus programas: vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/programa/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno no accede al ABM de programas")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(mercado().getIdCurso())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/programa/registrar")).andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------------
    // Formularios
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-16: registrar un programa responde en JSON y el error de validación en 400")
    void registrar() throws Exception {
        String curso = String.valueOf(macro().getIdCurso());

        mockMvc.perform(post("/programa/registrar").param("cursoId", curso).param("nombre", "Programa 2027"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Programa 2027")));
        // sin nombre, la versión se llama "Versión N"
        mockMvc.perform(post("/programa/registrar").param("cursoId", curso).param("nombre", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Versión")));
        mockMvc.perform(post("/programa/registrar").param("cursoId", curso).param("nombre", "x".repeat(51)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Máximo: 50 caracteres")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-17: modificar un programa responde en JSON y se rechaza si tiene inscripciones activas")
    void modificar() throws Exception {
        int sinInscripciones = macro().getProgramaVigente().getIdPrograma();
        int conInscripciones = mercado().getProgramaVigente().getIdPrograma();

        mockMvc.perform(post("/programa/modificar/" + sinInscripciones).param("nombre", "Renombrado")
                .param("objetivos", "Obj"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Programa modificado correctamente."));
        mockMvc.perform(post("/programa/modificar/" + conInscripciones).param("nombre", "Otro")
                .param("objetivos", "Obj"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("inscripciones activas asociadas")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("CU-18: dar de baja un programa responde en JSON y se rechaza si tiene cohortes activas")
    void darDeBaja() throws Exception {
        registrar(macro(), "Por error");
        int sinCohortes = macro().getProgramaVigente().getIdPrograma();
        int conCohortes = mercado().getProgramaVigente().getIdPrograma();

        mockMvc.perform(post("/programa/darDeBaja/" + sinCohortes)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Programa dado de baja con éxito."));
        mockMvc.perform(post("/programa/darDeBaja/" + conCohortes)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el programa")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Dar de baja varios programas: todos o ninguno")
    void darDeBajaMasiva() throws Exception {
        registrar(macro(), "Uno");
        int uno = macro().getProgramaVigente().getIdPrograma();
        registrar(macro(), "Dos");
        int dos = macro().getProgramaVigente().getIdPrograma();
        int conCohortes = mercado().getProgramaVigente().getIdPrograma();

        // uno de los programas tiene cohortes activas: no se da de baja ninguno
        mockMvc.perform(post("/programa/darDeBajaMasiva").param("ids", String.valueOf(uno), String.valueOf(conCohortes)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el programa")));
        assertEquals("Anterior", macro().getProgramas().stream().filter(p -> p.getIdPrograma() == uno).findFirst()
                .orElseThrow().getEstado());

        mockMvc.perform(post("/programa/darDeBajaMasiva").param("ids", String.valueOf(uno), String.valueOf(dos)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Programas dados de baja con éxito."));
        mockMvc.perform(post("/programa/darDeBajaMasiva")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe seleccionar al menos un programa")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador da de baja pero no registra ni modifica programas")
    void administrador() throws Exception {
        mockMvc.perform(post("/programa/darDeBaja/" + macro().getProgramaVigente().getIdPrograma()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el programa")));
        mockMvc.perform(post("/programa/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/programa/modificar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/cohorte/establecerPorDefecto/1")).andExpect(status().isForbidden());
    }

    // ----------------------------------------------------------------------
    // Selectores de la vista del curso
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La vista del curso del docente ofrece el selector de cohorte, sin el de programa")
    void selectoresEnLaVistaDelCurso() throws Exception {
        registrar(macro(), "Programa 2027");
        registrar(macro(), "Descartado");
        int descartado = macro().getProgramaVigente().getIdPrograma();
        mockMvc.perform(post("/programa/darDeBaja/" + descartado)).andExpect(status().isOk());

        mockMvc.perform(get("/curso/acceder/" + macro().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("selectProgramaPorDefecto"))))
                .andExpect(content().string(Matchers.containsString("selectCohortePorDefecto")))
                .andExpect(content().string(Matchers.containsString("/js/pages/curso/contexto.js")))
                // el selector lista todas las cohortes del curso, también las pendientes sin programa
                .andExpect(content().string(Matchers.containsString("selectCohortePorDefecto")));
        // el programa 2026 tiene una cohorte: se ve con su estado y la estrella no aparece porque no es la de trabajo
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(content().string(Matchers.containsString(" (En dictado)")))
                .andExpect(content().string(Matchers.containsString(" (Finalizada)")))
                .andExpect(content().string(Matchers.containsString("data-url=\"/cohorte/establecerPorDefecto/{valor}\"")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno no ve los selectores de programa y cohorte")
    void alumnoSinSelectores() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDeValentina()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("selectProgramaPorDefecto"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/js/pages/curso/contexto.js"))));
    }

    private int inscripcionDeValentina() {
        return inscripcionRepository.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Elegir una cohorte guarda la cohorte por defecto en la participación del docente")
    void guardaLaCohortePorDefecto() throws Exception {
        Curso curso = mercado();
        Cohorte finalizada = curso.getProgramaVigente().getCohortesActivas().stream()
                .filter(c -> c.getEstado().equals(com.app.idoneos.model.EstadoCohorte.CURSADA_FINALIZADA)).findFirst().orElseThrow();

        mockMvc.perform(post("/cohorte/establecerPorDefecto/" + finalizada.getIdCohorte()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ahora trabajás sobre la cohorte seleccionada."));

        DocenteCurso participacion = participacionDeFausto(curso);
        assertEquals(finalizada.getIdCohorte(), participacion.getCohortePorDefecto().getIdCohorte());
        assertEquals(finalizada.getPrograma().getIdPrograma(), participacion.getProgramaDeTrabajo().getIdPrograma());
        assertEquals(finalizada.getIdCohorte(), participacion.getCohorteDeTrabajo().getIdCohorte());

        mockMvc.perform(post("/cohorte/establecerPorDefecto/999999")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activa")));
    }

    // ----------------------------------------------------------------------
    // Enlaces
    // ----------------------------------------------------------------------

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("La barra lateral del docente enlaza a los programas del curso")
    void enlaceDelMenu() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(content().string(Matchers.containsString("/programa/buscar?cursoId=" + mercado().getIdCurso())));
    }
}
