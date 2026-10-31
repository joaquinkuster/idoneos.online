package com.app.idoneos.controlador;

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

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de programas (CU-15 a CU-18) y de los selectores de programa y cohorte
 * por defecto de la vista del curso. Fausto Spotorno es titular de "Mercado de Capitales Argentino" (un programa con tres
 * cohortes e inscripciones) y de "Macroeconomía de Coyuntura" (un programa con una cohorte sin inscriptos).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProgramaControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteCursoRepositorio participacionRepositorio;

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

    private DocenteCurso participacionDeFausto(Curso curso) {
        return participacionRepositorio.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getUsuario().getCorreo().equals(FAUSTO)).findFirst().orElseThrow();
    }

    private void registrar(Curso curso, String nombre) throws Exception {
        mockMvc.perform(post("/programa/registrar").param("cursoId", String.valueOf(curso.getIdCurso()))
                .param("nombre", nombre).param("objetivos", "Objetivos"))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ Listado

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve los programas del curso con su estado, objetivos y bibliografía")
    void listadoDelDocente() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(mercado().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de programas")))
                .andExpect(content().string(Matchers.containsString("Programa 2026")))
                .andExpect(content().string(Matchers.containsString("Vigente")))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar programa")))
                .andExpect(content().string(Matchers.containsString("modalNuevoPrograma")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                // columnas: objetivos y última modificación (sin conteos, sin bibliografía ni dedicación docente, que ya no son del programa)
                .andExpect(content().string(Matchers.containsString("data-col=\"Objetivos\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Bibliografía\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Dedicación docente\""))))
                .andExpect(content().string(Matchers.containsString("data-col=\"Última modificación\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Temas\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Con material\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Semanas\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Cohortes\""))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-col=\"Inscriptos\""))))
                // baja masiva
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.containsString("/programa/darDeBajaMasiva")))
                .andExpect(content().string(Matchers.containsString("data-seleccionar-fila")))
                // las acciones no se bloquean: la regla se informa al intentar la operación
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarPrograma")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPrograma")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("disabled title"))))
                // el programa por defecto se marca en el listado
                .andExpect(content().string(Matchers.containsString("(Por defecto)")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("★"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Los programas anteriores también se pueden editar y dar de baja desde el listado")
    void accionesHabilitadas() throws Exception {
        int anterior = macro().getProgramaVigente().getIdPrograma();
        registrar(macro(), "Programa 2027");
        Programa nuevo = macro().getProgramaVigente();

        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarPrograma" + nuevo.getIdPrograma() + "\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPrograma" + nuevo.getIdPrograma() + "\"")))
                .andExpect(content().string(Matchers.containsString("Anterior")))
                // el programa anterior (con una cohorte activa) conserva sus acciones
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarPrograma" + anterior + "\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPrograma" + anterior + "\"")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Los filtros de la URL no recortan el listado: se aplican en la pantalla")
    void filtros() throws Exception {
        registrar(macro(), "Programa Avanzado");
        String curso = String.valueOf(macro().getIdCurso());

        mockMvc.perform(get("/programa/buscar").param("cursoId", curso).param("nombre", "avanzado"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa Avanzado<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa 2026<")));
        mockMvc.perform(get("/programa/buscar").param("cursoId", curso).param("estado", "Anterior"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa 2026<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Programa Avanzado<")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y solo puede dar de baja")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Programa 2026")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar programa"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalEditarPrograma"))));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no lista sus programas: vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/programa/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al ABM de programas")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/programa/buscar").param("cursoId", String.valueOf(mercado().getIdCurso())))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/programa/registrar")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Formularios

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-16: registrar un programa responde en JSON y el error de validación en 400")
    void registrarPrograma() throws Exception {
        String curso = String.valueOf(macro().getIdCurso());

        mockMvc.perform(post("/programa/registrar").param("cursoId", curso).param("nombre", "Programa 2027")
                .param("objetivos", "Obj"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Programa 2027")));
        mockMvc.perform(post("/programa/registrar").param("cursoId", curso).param("nombre", "")
                .param("objetivos", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el nombre y los objetivos")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-17: modificar un programa responde en JSON y se rechaza si tiene inscripciones activas")
    void modificarPrograma() throws Exception {
        int sinInscripciones = macro().getProgramaVigente().getIdPrograma();
        int conInscripciones = mercado().getProgramaVigente().getIdPrograma();

        mockMvc.perform(post("/programa/modificar/" + sinInscripciones).param("nombre", "Renombrado")
                .param("objetivos", "Obj"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Programa modificado correctamente."));
        mockMvc.perform(post("/programa/modificar/" + conInscripciones).param("nombre", "Otro")
                .param("objetivos", "Obj"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("tiene inscripciones activas")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-18: dar de baja un programa responde en JSON y se rechaza si tiene cohortes activas")
    void darDeBajaPrograma() throws Exception {
        registrar(macro(), "Por error");
        int sinCohortes = macro().getProgramaVigente().getIdPrograma();
        int conCohortes = mercado().getProgramaVigente().getIdPrograma();

        mockMvc.perform(post("/programa/darDeBaja/" + sinCohortes)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Programa dado de baja con éxito."));
        mockMvc.perform(post("/programa/darDeBaja/" + conCohortes)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el programa")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
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
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador da de baja pero no registra, modifica ni establece programas por defecto")
    void administrador() throws Exception {
        mockMvc.perform(post("/programa/darDeBaja/" + macro().getProgramaVigente().getIdPrograma()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja el programa")));
        mockMvc.perform(post("/programa/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/programa/modificar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/programa/establecerPorDefecto").param("programaId", "1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/cohorte/establecerPorDefecto/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Selectores de la vista del curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La vista del curso del docente ofrece los selectores de programa y cohorte, sin programas dados de baja")
    void selectoresEnLaVistaDelCurso() throws Exception {
        registrar(macro(), "Programa 2027");
        registrar(macro(), "Descartado");
        int descartado = macro().getProgramaVigente().getIdPrograma();
        mockMvc.perform(post("/programa/darDeBaja/" + descartado)).andExpect(status().isOk());

        mockMvc.perform(get("/curso/acceder/" + macro().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("selectProgramaPorDefecto")))
                .andExpect(content().string(Matchers.containsString("selectCohortePorDefecto")))
                .andExpect(content().string(Matchers.containsString("/js/contexto.js")))
                .andExpect(content().string(Matchers.containsString("Programa 2027 (Vigente)")))
                .andExpect(content().string(Matchers.containsString("Programa 2026 (Anterior)")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Descartado"))))
                // el programa vigente recién registrado no tiene cohortes
                .andExpect(content().string(Matchers.containsString("Sin cohortes")));
        // el programa 2026 tiene una cohorte: se ve con su estado y la estrella no aparece porque no es la de trabajo
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(content().string(Matchers.containsString(" (En dictado)")))
                .andExpect(content().string(Matchers.containsString(" (Finalizada)")))
                .andExpect(content().string(Matchers.containsString("data-url=\"/cohorte/establecerPorDefecto/{valor}\"")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no ve los selectores de programa y cohorte")
    void alumnoSinSelectores() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDeValentina()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("selectProgramaPorDefecto"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/js/contexto.js"))));
    }

    private int inscripcionDeValentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Elegir un programa guarda el programa por defecto en la participación del docente y la vista lo usa")
    void guardaElProgramaPorDefecto() throws Exception {
        registrar(macro(), "Programa 2027");
        int anterior = macro().getProgramas().stream().filter(p -> p.getNombre().equals("Programa 2026")).findFirst()
                .orElseThrow().getIdPrograma();

        mockMvc.perform(post("/programa/establecerPorDefecto").param("programaId", String.valueOf(anterior)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ahora trabajás sobre el programa seleccionado."));

        DocenteCurso participacion = participacionDeFausto(macro());
        assertEquals(anterior, participacion.getProgramaPorDefecto().getIdPrograma());
        assertNull(participacion.getCohortePorDefecto());
        // la vista del curso muestra ahora ese programa como el de trabajo
        mockMvc.perform(get("/curso/acceder/" + macro().getIdCurso()))
                .andExpect(content().string(Matchers.containsString("selected=\"selected\">Programa 2026 (Anterior)")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("selected=\"selected\">Programa 2027"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("★"))));

        mockMvc.perform(post("/programa/establecerPorDefecto").param("programaId", "999999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activo")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Elegir una cohorte guarda la cohorte por defecto en la participación del docente")
    void guardaLaCohortePorDefecto() throws Exception {
        Curso curso = mercado();
        Cohorte finalizada = curso.getProgramaVigente().getCohortesActivas().stream()
                .filter(c -> c.getEstado().equals("Finalizada")).findFirst().orElseThrow();

        mockMvc.perform(post("/cohorte/establecerPorDefecto/" + finalizada.getIdCohorte()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Ahora trabajás sobre la cohorte seleccionada."));

        DocenteCurso participacion = participacionDeFausto(curso);
        assertEquals(finalizada.getIdCohorte(), participacion.getCohortePorDefecto().getIdCohorte());
        assertEquals(finalizada.getPrograma().getIdPrograma(), participacion.getProgramaPorDefecto().getIdPrograma());
        assertEquals(finalizada.getIdCohorte(), participacion.getCohorteDeTrabajo().getIdCohorte());

        mockMvc.perform(post("/cohorte/establecerPorDefecto/999999")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activa")));
    }

    // ------------------------------------------------------------------ Enlaces

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La barra lateral del docente enlaza a los programas del curso")
    void enlaceDelMenu() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(content().string(Matchers.containsString("/programa/buscar?cursoId=" + mercado().getIdCurso())));
    }
}
