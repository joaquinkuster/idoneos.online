package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de la página del cronograma (CU-23 y CU-24). Fausto Spotorno es titular de "Macroeconomía de Coyuntura" (un
 * programa con tres unidades, sin inscripciones: se puede modificar) y de "Mercado de Capitales Argentino" (con
 * inscripciones activas: solo lectura).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CronogramaControladorTest {

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

    private Curso macro() {
        return curso("Macroeconomía de Coyuntura");
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La pantalla recibe el máximo de semanas del cronograma desde el parámetro del sistema")
    void maximoDeSemanasParametrizado() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", String.valueOf(macro().getIdCurso())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-semanas-maximas=\"52\"")));
    }

    private String cursoId(Curso curso) {
        return String.valueOf(curso.getIdCurso());
    }

    private Programa programaDe(Curso curso) {
        return curso.getProgramaVigente();
    }

    private List<Integer> unidadesDe(Curso curso) {
        return programaDe(curso).getCronogramaOrdenado().stream().map(c -> c.getUnidad().getIdUnidad()).toList();
    }

    // ------------------------------------------------------------------ Consulta y edición

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve el cronograma de un programa sin inscripciones listo para modificar")
    void paginaEditable() throws Exception {
        Programa programa = programaDe(macro());

        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<h1 class=\"pn-titulo\">Cronograma</h1>")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Modificar cronograma"))))
                .andExpect(content().string(Matchers.containsString("Programa: " + programa.getNombre())))
                // tarjetas sin íconos de información; la ayuda es un botón junto al título que muestra el texto
                .andExpect(content().string(Matchers.containsString("data-ayuda-texto")))
                .andExpect(content().string(Matchers.containsString("data-ayuda ")))
                // navbar único con los enlaces del sitio, sin "Mis cursos"
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Inicio<")))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Acerca de<")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("nb-enlace\">Mis cursos<"))))
                .andExpect(content().string(Matchers.containsString("data-editable=\"true\"")))
                .andExpect(content().string(Matchers.containsString("data-programa-id=\"" + programa.getIdPrograma() + "\"")))
                // tarjetas: duración total, mínimo requerido y cohortes activas
                .andExpect(content().string(Matchers.containsString("Duración total")))
                .andExpect(content().string(Matchers.containsString("Mínimo requerido")))
                .andExpect(content().string(Matchers.containsString("Cohortes activas")))
                // los dos paneles, las dos vistas y los botones (el de guardar se llama "Modificar")
                .andExpect(content().string(Matchers.containsString("Unidades disponibles")))
                .andExpect(content().string(Matchers.containsString("data-nueva-unidad")))
                .andExpect(content().string(Matchers.containsString("Vista por semanas")))
                .andExpect(content().string(Matchers.containsString("Vista por unidades")))
                .andExpect(content().string(Matchers.containsString("data-zona")))
                .andExpect(content().string(Matchers.containsString("data-cancelar")))
                .andExpect(content().string(Matchers.containsString("data-modificar")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Guardar cambios"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Cronograma en solo lectura"))))
                .andExpect(content().string(Matchers.containsString("/js/cronograma.js")));
        // las unidades del cronograma viajan a la pantalla, en orden y con sus semanas
        for (UnidadCronograma c : programa.getCronogramaOrdenado()) {
            mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(macro())))
                    .andExpect(content().string(Matchers.containsString("data-id=\"" + c.getUnidad().getIdUnidad()
                            + "\" data-semanas=\"" + c.getSemanasDuracion() + "\"")));
        }
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Con inscripciones activas el cronograma se muestra en solo lectura, con su aviso")
    void soloLecturaPorInscripciones() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(mercado())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Cronograma en solo lectura")))
                .andExpect(content().string(Matchers.containsString("tiene inscripciones activas")))
                .andExpect(content().string(Matchers.containsString("data-editable=\"false\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-panel-disponibles"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-modificar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-zona"))))
                // sin edición no hay botón de ayuda ni su texto
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-ayuda"))))
                // la semana actual de la cohorte de trabajo sigue informada, sin el atraso del alumno
                .andExpect(content().string(Matchers.containsString("Semana actual de la cohorte: 3 de 9")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Vas al día"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el cronograma en solo lectura, sin aviso de inscripciones")
    void administrador() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-editable=\"false\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Cronograma en solo lectura"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-modificar"))));
        mockMvc.perform(post("/cronograma/modificar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso vuelve a Mis cursos")
    void docenteAjeno() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/cronograma/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede a la página del cronograma: ve el suyo desde la vista del curso")
    void alumno() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(mercado()))).andExpect(status().isForbidden());
        mockMvc.perform(post("/cronograma/modificar")).andExpect(status().isForbidden());
        int inscripcion = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion).param("seccion", "cronograma"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("id=\"cronograma\"")));
    }

    // ------------------------------------------------------------------ CU-24 Formulario

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-24: modificar el cronograma responde en JSON y la página muestra el nuevo orden y las nuevas semanas")
    void modificar() throws Exception {
        Programa programa = programaDe(macro());
        List<Integer> unidades = unidadesDe(macro());

        mockMvc.perform(post("/cronograma/modificar").param("programaId", String.valueOf(programa.getIdPrograma()))
                .param("ids", String.valueOf(unidades.get(2)), String.valueOf(unidades.get(0)), String.valueOf(unidades.get(1)))
                .param("semanas", "2", "4", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value("Cronograma modificado correctamente."));

        mockMvc.perform(get("/cronograma/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("data-id=\"" + unidades.get(2) + "\" data-semanas=\"2\"")))
                .andExpect(content().string(Matchers.containsString("7 semanas")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-24: las duraciones inválidas y el programa con inscripciones responden 400 con el mensaje del CU")
    void rechazos() throws Exception {
        Programa programa = programaDe(macro());
        List<Integer> unidades = unidadesDe(macro());
        String id = String.valueOf(programa.getIdPrograma());

        mockMvc.perform(post("/cronograma/modificar").param("programaId", id)
                .param("ids", String.valueOf(unidades.get(0))).param("semanas", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("mayor a cero")));
        mockMvc.perform(post("/cronograma/modificar").param("programaId", id)
                .param("ids", String.valueOf(unidades.get(0))).param("semanas", "tres"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("mayor a cero")));

        Programa conInscripciones = programaDe(mercado());
        mockMvc.perform(post("/cronograma/modificar").param("programaId", String.valueOf(conInscripciones.getIdPrograma())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("tiene inscripciones activas")));
    }

    // ------------------------------------------------------------------ Enlaces

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El menú del docente enlaza a su página del cronograma, y la sección vieja de la vista del curso redirige")
    void enlacesDelDocente() throws Exception {
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/cronograma/buscar?cursoId=" + mercado().getIdCurso())));
        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()).param("seccion", "cronograma"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cronograma/buscar?cursoId=" + mercado().getIdCurso()));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El menú del alumno mantiene su cronograma dentro de la vista del curso")
    void enlaceDelAlumno() throws Exception {
        int inscripcion = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();

        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion))
                .andExpect(content().string(Matchers.containsString("?seccion=cronograma")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/cronograma/buscar"))));
    }
}
