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
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de unidades (CU-19 a CU-22) y de lo que cambia en la vista del curso y
 * en el menú lateral. Fausto Spotorno es titular de "Mercado de Capitales Argentino" (tres unidades, con inscripciones) y
 * de "Macroeconomía de Coyuntura" (sin inscripciones).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UnidadControladorTest {

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

    private Unidad primeraDe(Curso curso) {
        return programaDe(curso).getCronogramaOrdenado().get(0).getUnidad();
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
    @DisplayName("El docente ve el cronograma de su programa por defecto con todos los datos de cada unidad")
    void listadoDelDocente() throws Exception {
        Unidad unidad = primeraDe(macro());

        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de unidades")))
                .andExpect(content().string(Matchers.containsString("Cronograma de " + programaDe(macro()).getNombre())))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + unidad.getTitulo() + "<")))
                // los datos propios de la unidad, y cada uno se puede ocultar con "Columnas"
                .andExpect(content().string(Matchers.containsString("data-col=\"Orden\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Título\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Descripción\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Contenido\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Semanas\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Última modificación\"")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Fecha de creación\"")))
                .andExpect(content().string(Matchers.containsString("fa-plus\"></i> Registrar unidad")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                // baja masiva sobre el programa del listado
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.containsString("/unidad/darDeBajaMasiva?programaId=" + programaId(macro()))))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarUnidad" + unidad.getIdUnidad() + "\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaUnidad" + unidad.getIdUnidad() + "\"")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El formulario de alta elige el programa por defecto, marcado como tal, y permite incorporar una unidad existente")
    void formularioDeAlta() throws Exception {
        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("id=\"modalNuevaUnidad\"")))
                .andExpect(content().string(Matchers.containsString("name=\"programaId\"")))
                .andExpect(content().string(Matchers.containsString("selected=\"selected\">" + programaDe(macro()).getNombre() + " (Por defecto)")))
                .andExpect(content().string(Matchers.containsString("Una unidad de otro programa del curso")))
                .andExpect(content().string(Matchers.containsString("name=\"unidadExistenteId\"")))
                .andExpect(content().string(Matchers.containsString("data-programas=\"" + programaId(macro()) + "\"")))
                .andExpect(content().string(Matchers.containsString("/js/unidad.js")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Con ?nuevo o ?accion el listado abre solo la ventana correspondiente")
    void abreLaVentana() throws Exception {
        Unidad unidad = primeraDe(macro());
        String curso = cursoId(macro());

        mockMvc.perform(get("/unidad/buscar").param("cursoId", curso).param("nuevo", ""))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalNuevaUnidad\"")));
        mockMvc.perform(get("/unidad/buscar").param("cursoId", curso).param("accion", "editar")
                .param("unidadId", String.valueOf(unidad.getIdUnidad())))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalEditarUnidad" + unidad.getIdUnidad() + "\"")));
        mockMvc.perform(get("/unidad/buscar").param("cursoId", curso).param("accion", "baja")
                .param("unidadId", String.valueOf(unidad.getIdUnidad())))
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalBajaUnidad" + unidad.getIdUnidad() + "\"")));
        mockMvc.perform(get("/unidad/buscar").param("cursoId", curso))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El título que llega por la URL no recorta el listado: se filtra en la pantalla")
    void filtro() throws Exception {
        Unidad unidad = primeraDe(mercado());

        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(mercado())).param("titulo", "renta fija"))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Renta fija y bonos<")))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">" + unidad.getTitulo() + "<")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y solo puede dar de baja")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-baja-masiva")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar unidad"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalEditarUnidad"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaUnidad")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(macro())))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/unidad/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al ABM de unidades")
    void noEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(mercado()))).andExpect(status().isForbidden());
        mockMvc.perform(post("/unidad/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/unidad/modificar/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/unidad/darDeBaja/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Formularios

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-20: agregar una unidad responde en JSON y el error de validación en 400")
    void agregarUnidad() throws Exception {
        mockMvc.perform(post("/unidad/registrar").param("programaId", programaId(macro())).param("titulo", "Política monetaria")
                .param("contenido", "Tasas y agregados monetarios"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("registrada con éxito")));
        mockMvc.perform(get("/unidad/buscar").param("cursoId", cursoId(macro())))
                .andExpect(content().string(Matchers.containsString("pn-celda-principal pn-truncar\">Política monetaria<")));

        mockMvc.perform(post("/unidad/registrar").param("programaId", programaId(macro())).param("titulo", "")
                .param("contenido", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el título y el contenido.")));
        mockMvc.perform(post("/unidad/registrar").param("programaId", programaId(macro())).param("titulo", "política monetaria")
                .param("contenido", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Ya existe una unidad con ese título")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-20: incorporar una unidad existente de otro programa responde que fue incorporada")
    void incorporarUnidad() throws Exception {
        Unidad unidad = primeraDe(macro());
        mockMvc.perform(post("/programa/registrar").param("cursoId", cursoId(macro())).param("nombre", "Programa 2027")
                .param("objetivos", "Obj").param("bibliografia", "Bib")).andExpect(status().isOk());

        mockMvc.perform(post("/unidad/registrar").param("programaId", programaId(macro()))
                .param("unidadExistenteId", String.valueOf(unidad.getIdUnidad())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("incorporada con éxito")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-21: modificar una unidad responde en JSON y se rechaza si tiene inscripciones activas")
    void modificarUnidad() throws Exception {
        Unidad sinInscripciones = primeraDe(macro());
        Unidad conInscripciones = primeraDe(mercado());

        mockMvc.perform(post("/unidad/modificar/" + sinInscripciones.getIdUnidad()).param("titulo", "Otro título")
                .param("contenido", "Otro contenido"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Unidad modificada correctamente."));
        mockMvc.perform(post("/unidad/modificar/" + conInscripciones.getIdUnidad()).param("titulo", "Otro")
                .param("contenido", "Otro contenido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("tiene inscripciones activas asociadas")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-22: dar de baja una unidad responde en JSON y se rechaza si el programa tiene inscripciones activas")
    void darDeBajaUnidad() throws Exception {
        Unidad unidad = primeraDe(macro());
        Unidad deMercado = primeraDe(mercado());

        mockMvc.perform(post("/unidad/darDeBaja/" + unidad.getIdUnidad()).param("programaId", programaId(macro())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Unidad dada de baja con éxito."));
        mockMvc.perform(post("/unidad/darDeBaja/" + deMercado.getIdUnidad()).param("programaId", programaId(mercado())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se puede dar de baja la unidad")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-22: la baja masiva es de todas o ninguna")
    void bajaMasiva() throws Exception {
        Unidad uno = primeraDe(macro());
        Unidad deMercado = primeraDe(mercado());

        mockMvc.perform(post("/unidad/darDeBajaMasiva").param("programaId", programaId(macro()))
                .param("ids", String.valueOf(uno.getIdUnidad()), String.valueOf(deMercado.getIdUnidad())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no pertenece al cronograma")));
        mockMvc.perform(post("/unidad/darDeBajaMasiva").param("programaId", programaId(macro())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe seleccionar al menos una unidad")));
        mockMvc.perform(post("/unidad/darDeBajaMasiva").param("programaId", programaId(macro()))
                .param("ids", String.valueOf(uno.getIdUnidad())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Unidades dadas de baja con éxito."));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador da de baja pero no agrega ni modifica")
    void administrador() throws Exception {
        Unidad unidad = primeraDe(macro());

        mockMvc.perform(post("/unidad/darDeBaja/" + unidad.getIdUnidad()).param("programaId", programaId(macro())))
                .andExpect(status().isOk());
        mockMvc.perform(post("/unidad/registrar")).andExpect(status().isForbidden());
        mockMvc.perform(post("/unidad/modificar/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Menú lateral y vista del curso

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La vista del curso del docente tiene Registrar unidad, el menú de cada unidad y muestra su descripción y contenido")
    void vistaDelCursoDelDocente() throws Exception {
        Unidad unidad = primeraDe(mercado());

        mockMvc.perform(get("/curso/acceder/" + mercado().getIdCurso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Registrar unidad</a>")))
                .andExpect(content().string(Matchers.containsString("/unidad/buscar?cursoId=" + mercado().getIdCurso() + "&amp;nuevo=")))
                .andExpect(content().string(Matchers.containsString("ac-menu-unidad")))
                .andExpect(content().string(Matchers.containsString("accion=editar&amp;unidadId=" + unidad.getIdUnidad())))
                .andExpect(content().string(Matchers.containsString("accion=baja&amp;unidadId=" + unidad.getIdUnidad())))
                .andExpect(content().string(Matchers.containsString("ac-unidad-contenido")))
                .andExpect(content().string(Matchers.containsString(unidad.getContenido().trim().split("\\s+")[0])))
                // el menú lateral: Curso arriba, y Unidades como sección aparte
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.containsString("/unidad/buscar?cursoId=" + mercado().getIdCurso())));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve la descripción y el contenido de la unidad, pero ningún botón de gestión")
    void vistaDelCursoDelAlumno() throws Exception {
        Unidad unidad = primeraDe(mercado());
        int inscripcion = inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();

        mockMvc.perform(get("/inscripcion/acceder/" + inscripcion))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("ac-unidad-contenido")))
                .andExpect(content().string(Matchers.containsString(unidad.getContenido().trim().split("\\s+")[0])))
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar unidad"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("ac-menu-unidad"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/unidad/buscar"))));
    }
}
