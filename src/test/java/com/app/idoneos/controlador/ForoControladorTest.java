package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;

/**
 * Pruebas de las vistas y los formularios del foro (CU-35 a CU-42). En "Mercado de Capitales Argentino", la unidad 1
 * tiene una consulta de Valentina Ruiz con una respuesta de Fausto Spotorno (titular); la unidad 2, una consulta sin
 * responder; la 3 no tiene consultas y está bloqueada para Valentina, alumna de la cohorte en dictado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ForoControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private ConsultaForoRepositorio consultaRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private int primera() {
        return unidad("Mercado de Capitales Argentino", "Introducción al mercado de capitales").getIdUnidad();
    }

    private int segunda() {
        return unidad("Mercado de Capitales Argentino", "Renta fija y bonos").getIdUnidad();
    }

    private int tercera() {
        return unidad("Mercado de Capitales Argentino", "Renta variable y opciones").getIdUnidad();
    }

    private int idCurso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).mapToInt(Curso::getIdCurso)
                .findFirst().orElseThrow();
    }

    /** La consulta de la semilla en la unidad indicada. */
    private ConsultaForo consultaDe(int idUnidad) {
        return unidadRepositorio.findById(idUnidad).orElseThrow().getConsultasForoOrdenadas().get(0);
    }

    private Inscripcion inscripcionDeValentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(VALENTINA)).findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------ Listado de foros

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El listado tiene un foro por unidad, con sus consultas y las que están sin responder")
    void listadoDeForos() throws Exception {
        int curso = idCurso("Mercado de Capitales Argentino");

        mockMvc.perform(get("/foro/buscar").param("cursoId", String.valueOf(curso)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de foros")))
                .andExpect(content().string(Matchers.containsString("Foro 1")))
                .andExpect(content().string(Matchers.containsString("Foro 2")))
                .andExpect(content().string(Matchers.containsString("Foro 3")))
                .andExpect(content().string(Matchers.containsString("1 consulta")))
                .andExpect(content().string(Matchers.containsString("Sin consultas")))
                .andExpect(content().string(Matchers.containsString("/foro/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("Ver detalle")))
                .andExpect(content().string(Matchers.containsString("data-columnas-boton")))
                .andExpect(content().string(Matchers.containsString("data-paginador")));
        // filtrado por unidad
        mockMvc.perform(get("/foro/buscar").param("cursoId", String.valueOf(curso)).param("unidadId",
                String.valueOf(segunda())))
                .andExpect(content().string(Matchers.containsString("Foro 2")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Foro 3<"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador también ve el listado de foros de cualquier curso")
    void listadoParaElAdministrador() throws Exception {
        mockMvc.perform(get("/foro/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isOk()).andExpect(content().string(Matchers.containsString("Foro 1")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no puede listar sus foros: vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/foro/buscar").param("cursoId", String.valueOf(idCurso("Macroeconomía de Coyuntura"))))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al listado de foros del curso")
    void listadoNoEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/foro/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Vista de un foro

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve las consultas del foro, con las que están sin responder, y no puede consultar")
    void vistaDelDocente() throws Exception {
        mockMvc.perform(get("/foro/ver/" + segunda()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Foro 2")))
                .andExpect(content().string(Matchers.containsString("Renta fija y bonos")))
                .andExpect(content().string(Matchers.containsString("duration de un bono con cupones")))
                .andExpect(content().string(Matchers.containsString("Sin responder")))
                .andExpect(content().string(Matchers.containsString("Valentina Ruiz")))
                // avatar con las iniciales de quien comenzó la consulta
                .andExpect(content().string(Matchers.containsString("av av-chico")))
                .andExpect(content().string(Matchers.containsString("VR")))
                // paginación estándar de las tablas
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                .andExpect(content().string(Matchers.containsString("Filas por página")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar consulta"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Dar de baja"))))
                .andExpect(content().string(Matchers.containsString("name=\"desde\"")))
                .andExpect(content().string(Matchers.containsString("name=\"hasta\"")));
        mockMvc.perform(get("/foro/ver/" + primera()))
                .andExpect(content().string(Matchers.containsString("matrícula para operar")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Sin responder"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La búsqueda filtra por texto y por rango de fechas")
    void busqueda() throws Exception {
        mockMvc.perform(get("/foro/ver/" + primera()).param("texto", "matricula"))
                .andExpect(content().string(Matchers.containsString("matrícula para operar")));
        mockMvc.perform(get("/foro/ver/" + primera()).param("texto", "zzz"))
                .andExpect(content().string(Matchers.containsString("No se encontraron consultas")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("matrícula para operar"))));
        // la consulta de la unidad 1 es de hace 3 días
        mockMvc.perform(get("/foro/ver/" + primera()).param("desde", java.time.LocalDate.now().minusDays(1).toString()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("matrícula para operar"))));
        mockMvc.perform(get("/foro/ver/" + primera()).param("desde", "").param("hasta", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("matrícula para operar")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La lista usa la paginación de las tablas: entrega todas las consultas y la tabla las pagina")
    void paginacion() throws Exception {
        Unidad unidad = unidadRepositorio.findById(primera()).orElseThrow();
        for (int i = 1; i <= 11; i++) {
            consultaRepositorio.save(new ConsultaForo(unidad, inscripcionDeValentina(), "Consulta número " + i));
        }
        mockMvc.perform(get("/foro/ver/" + primera()))
                .andExpect(content().string(Matchers.containsString("data-tabla")))
                .andExpect(content().string(Matchers.containsString("data-por-pagina")))
                .andExpect(content().string(Matchers.containsString("data-paginador")))
                .andExpect(content().string(Matchers.containsString("Consulta número 11")))
                .andExpect(content().string(Matchers.containsString("matrícula para operar")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve el foro de una unidad habilitada y puede registrar una consulta")
    void vistaDelAlumno() throws Exception {
        mockMvc.perform(get("/foro/ver/" + segunda()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Registrar consulta")))
                .andExpect(content().string(Matchers.containsString("modalNuevaConsulta")))
                .andExpect(content().string(Matchers.containsString("name=\"unidadId\"")))
                .andExpect(content().string(Matchers.containsString("duration de un bono con cupones")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Dar de baja"))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no ve el foro de una unidad bloqueada ni sus consultas")
    void alumnoConUnidadBloqueada() throws Exception {
        mockMvc.perform(get("/foro/ver/" + tercera())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "martin.gomez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno cuyo dictado todavía no comenzó no ve el foro ni sus consultas")
    void alumnoConDictadoSinComenzar() throws Exception {
        mockMvc.perform(get("/foro/ver/" + primera())).andExpect(status().isForbidden());
        mockMvc.perform(get("/foro/consulta/" + consultaDe(primera()).getIdConsulta())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no ve el foro, y una unidad inexistente responde 404")
    void docenteAjenoEInexistente() throws Exception {
        int ajena = unidad("Macroeconomía de Coyuntura", "Inflación y precios").getIdUnidad();

        mockMvc.perform(get("/foro/ver/" + ajena)).andExpect(status().isForbidden());
        mockMvc.perform(get("/foro/ver/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/foro/consulta/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el foro con la baja de cada consulta, sin poder consultar")
    void vistaDelAdministrador() throws Exception {
        mockMvc.perform(get("/foro/ver/" + primera()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("modalBajaConsulta" + consultaDe(primera()).getIdConsulta())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar consulta"))));
    }

    @Test
    @DisplayName("Sin sesión se pide iniciar sesión")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/foro/ver/" + primera())).andExpect(status().is3xxRedirection());
    }

    // ------------------------------------------------------------------ Detalle de una consulta

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve la consulta con sus respuestas, enlaces permanentes y el botón Responder")
    void detalleParaElDocente() throws Exception {
        ConsultaForo consulta = consultaDe(primera());
        int respuesta = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("matrícula para operar")))
                .andExpect(content().string(Matchers.containsString("Valentina Ruiz")))
                .andExpect(content().string(Matchers.containsString("Lo vemos en la clase 2")))
                .andExpect(content().string(Matchers.containsString("Fausto Spotorno")))
                .andExpect(content().string(Matchers.containsString("Enlace permanente")))
                .andExpect(content().string(Matchers.containsString("respuestaId=" + respuesta)))
                .andExpect(content().string(Matchers.containsString("modalResponder")))
                .andExpect(content().string(Matchers.containsString("fo-enlace-accion")))
                .andExpect(content().string(Matchers.containsString("av av-grande")))
                // la respuesta del seed es de hace 2 días: ya no se puede modificar
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarRespuesta"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalBajaRespuesta"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El enlace permanente de una respuesta muestra solo esa respuesta")
    void unaSolaRespuesta() throws Exception {
        ConsultaForo consulta = consultaDe(primera());
        int otra = respuesta(consulta, "Segunda respuesta aparte");

        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta()).param("respuestaId", String.valueOf(otra)))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Se muestra una sola respuesta.")))
                .andExpect(content().string(Matchers.containsString("Segunda respuesta aparte")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Lo vemos en la clase 2"))));
    }

    private int respuesta(ConsultaForo consulta, String texto) {
        // Fausto responde por la API del servicio para tener una respuesta reciente
        try {
            mockMvc.perform(post("/foro/responder").param("consultaId", String.valueOf(consulta.getIdConsulta()))
                    .param("texto", texto)).andExpect(status().isOk());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return consulta.getRespuestasVigentes().get(consulta.getRespuestasVigentes().size() - 1).getIdRespuesta();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente puede modificar su respuesta reciente")
    void docenteModificaSuRespuestaReciente() throws Exception {
        ConsultaForo consulta = consultaDe(segunda());
        int id = respuesta(consulta, "Texto inicial");

        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta()))
                .andExpect(content().string(Matchers.containsString("modalEditarRespuesta" + id)));
        mockMvc.perform(post("/foro/modificarRespuesta/" + id).param("texto", "Texto corregido"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Respuesta modificada correctamente."));
        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta()))
                .andExpect(content().string(Matchers.containsString("Texto corregido")))
                .andExpect(content().string(Matchers.containsString("editada")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno puede modificar su consulta reciente, no la vencida, y no responde")
    void detalleParaElAlumno() throws Exception {
        ConsultaForo vencida = consultaDe(primera());
        ConsultaForo reciente = consultaDe(segunda());
        reciente.setFechaCreacion(LocalDateTime.now().minusMinutes(2));

        mockMvc.perform(get("/foro/consulta/" + reciente.getIdConsulta()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("modalEditarConsulta")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalResponder"))))
                .andExpect(content().string(Matchers.containsString("Esta consulta todavía no tiene respuestas")));
        mockMvc.perform(get("/foro/consulta/" + vencida.getIdConsulta()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarConsulta"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve las acciones de baja de la consulta y de cada respuesta")
    void detalleParaElAdministrador() throws Exception {
        ConsultaForo consulta = consultaDe(primera());
        int respuesta = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("modalBajaConsulta")))
                .andExpect(content().string(Matchers.containsString("modalBajaRespuesta" + respuesta)))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalResponder"))));
    }

    // ------------------------------------------------------------------ Formularios

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-36 y CU-37: registrar y modificar una consulta responde en JSON, con el error de validación en 400")
    void alumnoRegistraYModifica() throws Exception {
        mockMvc.perform(post("/foro/registrar").param("unidadId", String.valueOf(segunda())).param("texto", "Nueva duda"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value("Consulta registrada con éxito."));
        mockMvc.perform(get("/foro/ver/" + segunda())).andExpect(content().string(Matchers.containsString("Nueva duda")));

        mockMvc.perform(post("/foro/registrar").param("unidadId", String.valueOf(segunda())).param("texto", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el texto de la consulta.")));
        mockMvc.perform(post("/foro/registrar").param("unidadId", String.valueOf(tercera())).param("texto", "Hola"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No tenés una inscripción vigente")));

        int id = consultaDe(segunda()).getIdConsulta();
        mockMvc.perform(post("/foro/modificar/" + id).param("texto", "Duda corregida")).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Consulta modificada correctamente."));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-40: el docente responde; el error de validación responde 400")
    void docenteResponde() throws Exception {
        int id = consultaDe(segunda()).getIdConsulta();

        mockMvc.perform(post("/foro/responder").param("consultaId", String.valueOf(id)).param("texto", "Respuesta"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Respuesta registrada con éxito."));
        mockMvc.perform(post("/foro/responder").param("consultaId", String.valueOf(id)).param("texto", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el texto de la respuesta.")));
        mockMvc.perform(get("/foro/ver/" + segunda()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Sin responder"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-38 y CU-42: el administrador da de baja la respuesta y la consulta, y vuelve al foro")
    void administradorModera() throws Exception {
        ConsultaForo consulta = consultaDe(primera());
        int respuesta = consulta.getRespuestasVigentes().get(0).getIdRespuesta();

        mockMvc.perform(post("/foro/darDeBajaRespuesta/" + respuesta)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Respuesta dada de baja con éxito."));
        mockMvc.perform(post("/foro/darDeBaja/" + consulta.getIdConsulta())).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Consulta dada de baja con éxito."))
                .andExpect(jsonPath("$.destino").value("/foro/ver/" + primera()));
        // la consulta dada de baja ya no tiene página propia
        mockMvc.perform(get("/foro/consulta/" + consulta.getIdConsulta())).andExpect(status().isNotFound());
        mockMvc.perform(post("/foro/darDeBaja/" + consulta.getIdConsulta())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activa")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador no puede consultar, modificar ni responder")
    void administradorNoParticipa() throws Exception {
        mockMvc.perform(post("/foro/registrar").param("unidadId", String.valueOf(segunda())).param("texto", "x"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/modificar/1").param("texto", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/responder").param("consultaId", "1").param("texto", "x"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/modificarRespuesta/1").param("texto", "x")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente no puede consultar ni moderar")
    void docenteNoConsultaNiModera() throws Exception {
        mockMvc.perform(post("/foro/registrar").param("unidadId", String.valueOf(segunda())).param("texto", "x"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/modificar/1").param("texto", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/darDeBaja/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/darDeBajaRespuesta/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no puede responder ni moderar")
    void alumnoNoRespondeNiModera() throws Exception {
        mockMvc.perform(post("/foro/responder").param("consultaId", "1").param("texto", "x"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/modificarRespuesta/1").param("texto", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/darDeBaja/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/foro/darDeBajaRespuesta/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Enlaces desde el resto de la plataforma

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("La vista del curso y la barra lateral del docente enlazan al foro")
    void enlacesDelDocente() throws Exception {
        int curso = idCurso("Mercado de Capitales Argentino");

        mockMvc.perform(get("/curso/acceder/" + curso))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/foro/buscar?cursoId=" + curso)))
                .andExpect(content().string(Matchers.containsString("/foro/buscar?cursoId=" + curso + "&amp;unidadId=" + primera())))
                .andExpect(content().string(Matchers.containsString("/foro/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("Foro de consultas")));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno llega al foro de la unidad desde la vista del curso")
    void enlacesDelAlumno() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDeValentina().getIdInscripcion()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/foro/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("/foro/ver/" + segunda())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/foro/buscar"))));
    }
}
