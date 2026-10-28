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
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;

/**
 * Pruebas de las vistas y los formularios del glosario (CU-32 a CU-35). Fausto Spotorno es titular de "Mercado de
 * Capitales Argentino" (tema 1 con tres términos, tema 2 con dos y tema 3 sin términos); Valentina Ruiz es alumna
 * de la cohorte en dictado, con los temas 1 y 2 habilitadas y la 3 bloqueada.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GlosarioControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private TerminoGlosarioRepositorio terminoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Tema tema(String curso, String titulo) {
        return temaRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private int primera() {
        return tema("Mercado de Capitales Argentino", "Introducción al mercado de capitales").getIdTema();
    }

    private int segunda() {
        return tema("Mercado de Capitales Argentino", "Renta fija y bonos").getIdTema();
    }

    private int tercera() {
        return tema("Mercado de Capitales Argentino", "Renta variable y opciones").getIdTema();
    }

    private int idCurso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).mapToInt(Curso::getIdCurso)
                .findFirst().orElseThrow();
    }

    private int idTermino(int idTema, String termino) {
        return terminoRepositorio.findAll().stream()
                .filter(t -> t.getTema().getIdTema() == idTema && t.getTermino().equals(termino))
                .mapToInt(TerminoGlosario::getIdTermino).findFirst().orElseThrow();
    }

    // ------------------------------------------------------------------ Listado de glosarios

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El listado tiene un glosario por tema, con sus términos, y un solo botón: Ver detalle")
    void listadoDeGlosarios() throws Exception {
        mockMvc.perform(get("/glosario/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Listado de glosarios")))
                .andExpect(content().string(Matchers.containsString("Glosario 1")))
                .andExpect(content().string(Matchers.containsString("Glosario 2")))
                .andExpect(content().string(Matchers.containsString("Glosario 3")))
                .andExpect(content().string(Matchers.containsString("3 términos")))
                .andExpect(content().string(Matchers.containsString("2 términos")))
                // el tema sin términos también figura
                .andExpect(content().string(Matchers.containsString("Sin términos")))
                .andExpect(content().string(Matchers.containsString("/glosario/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("Ver detalle")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Dar de baja"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Editar"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El tema que llega por la URL solo se elige en el campo (el listado sigue completo) y trae filtros, botón Limpiar, columnas y paginación")
    void listadoConFiltros() throws Exception {
        int curso = idCurso("Mercado de Capitales Argentino");

        mockMvc.perform(get("/glosario/buscar").param("cursoId", String.valueOf(curso)).param("temaId", String.valueOf(segunda())))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Glosario 2")))
                .andExpect(content().string(Matchers.containsString("Glosario 1<")))
                .andExpect(content().string(Matchers.containsString("Glosario 3<")))
                .andExpect(content().string(Matchers.containsString("Limpiar")))
                .andExpect(content().string(Matchers.containsString("data-filtro=")))
                .andExpect(content().string(Matchers.containsString("data-paginador")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador también ve el listado de glosarios de cualquier curso")
    void listadoParaElAdministrador() throws Exception {
        mockMvc.perform(get("/glosario/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Glosario 1")))
                .andExpect(content().string(Matchers.containsString("Ver detalle")));
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no puede listar sus glosarios: vuelve a Mis cursos")
    void listadoDeDocenteAjeno() throws Exception {
        mockMvc.perform(get("/glosario/buscar").param("cursoId", String.valueOf(idCurso("Macroeconomía de Coyuntura"))))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/curso/buscar"));
        mockMvc.perform(get("/glosario/buscar")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/curso/buscar"));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede al listado de glosarios del curso")
    void listadoNoEsParaAlumnos() throws Exception {
        mockMvc.perform(get("/glosario/buscar").param("cursoId", String.valueOf(idCurso("Mercado de Capitales Argentino"))))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Vista de un glosario

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente ve el glosario con sus términos por letra y las acciones para gestionarlos")
    void vistaDelDocente() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Glosario 1")))
                .andExpect(content().string(Matchers.containsString("Introducción al mercado de capitales")))
                .andExpect(content().string(Matchers.containsString("3 términos")))
                .andExpect(content().string(Matchers.containsString("BYMA")))
                .andExpect(content().string(Matchers.containsString("Comisión Nacional de Valores")))
                // buscador con la casilla tildada, índice alfabético y las acciones del docente
                .andExpect(content().string(Matchers.containsString("¿Buscar en términos y definiciones?")))
                .andExpect(content().string(Matchers.containsString("checked")))
                .andExpect(content().string(Matchers.containsString("data-glosario-indice")))
                .andExpect(content().string(Matchers.containsString("data-glosario-limpiar")))
                .andExpect(content().string(Matchers.containsString("Registrar término")))
                .andExpect(content().string(Matchers.containsString("modalNuevoTermino")))
                .andExpect(content().string(Matchers.containsString("modalEditarTermino" + idTermino(primera(), "BYMA"))))
                .andExpect(content().string(Matchers.containsString("modalBajaTermino" + idTermino(primera(), "BYMA"))))
                // enlace permanente por término, sin "Añadir entrada" ni "entradas"
                .andExpect(content().string(Matchers.containsString("terminoId=" + idTermino(primera(), "BYMA"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Añadir entrada"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("entradas"))))
                // el tema no se pide en el formulario: es la de este glosario
                .andExpect(content().string(Matchers.containsString("name=\"temaId\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Seleccionar tema"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Con ?nuevo el docente ve abierto el formulario para registrar un término")
    void abreElFormularioDeAlta() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + tercera()).param("nuevo", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-abrir-modal=\"modalNuevoTermino\"")))
                .andExpect(content().string(Matchers.containsString("Este glosario todavía no tiene términos")));
        mockMvc.perform(get("/glosario/ver/" + tercera()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El buscador filtra en el navegador: el servidor entrega todos los términos, sin botón Buscar")
    void busqueda() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-glosario-filtros")))
                .andExpect(content().string(Matchers.containsString("data-nombre=\"BYMA\"")))
                .andExpect(content().string(Matchers.containsString("data-definicion=")))
                .andExpect(content().string(Matchers.containsString("Comisión Nacional de Valores")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("fa-magnifying-glass\"></i> Buscar"))));
        // los parámetros de búsqueda ya no filtran del lado del servidor
        mockMvc.perform(get("/glosario/ver/" + primera()).param("texto", "zzz").param("letra", "C"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Bolsas y Mercados Argentinos")))
                .andExpect(content().string(Matchers.containsString("Comisión Nacional de Valores")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El enlace permanente muestra un solo término, también al alumno")
    void enlacePermanente() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + segunda()).param("terminoId", String.valueOf(idTermino(segunda(), "TIR"))))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Se muestra un solo término.")))
                .andExpect(content().string(Matchers.containsString("Tasa interna de retorno")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Plazo promedio ponderado"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Entrega todos los términos: las páginas de a 10 y el índice se arman en el navegador")
    void paginacion() throws Exception {
        // 3 términos de la semilla + 9 más = 12 términos
        for (int i = 1; i <= 9; i++) {
            terminoRepositorio.save(new TerminoGlosario(tema("Mercado de Capitales Argentino",
                    "Introducción al mercado de capitales"), "Término " + i, "Definición " + i));
        }
        mockMvc.perform(get("/glosario/ver/" + primera()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-glosario-paginas")))
                .andExpect(content().string(Matchers.containsString("Definición 9")))
                .andExpect(content().string(Matchers.containsString("Bolsas y Mercados Argentinos")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno ve el glosario de un tema habilitado en solo lectura")
    void vistaDelAlumno() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + segunda()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Glosario 2")))
                .andExpect(content().string(Matchers.containsString("TIR")))
                .andExpect(content().string(Matchers.containsString("Duration")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar término"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalNuevoTermino"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Editar"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Dar de baja"))));
        // aunque pida el formulario de alta, no se abre
        mockMvc.perform(get("/glosario/ver/" + segunda()).param("nuevo", ""))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-abrir-modal"))));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no ve el glosario de un tema bloqueado")
    void alumnoConTemaBloqueada() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + tercera())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "martin.gomez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno cuyo dictado todavía no comenzó no ve el glosario")
    void alumnoConDictadoSinComenzar() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "tomas.herrera@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno que no está inscripto en el curso no ve el glosario")
    void alumnoSinInscripcion() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no ve el glosario, y un tema inexistente responde 404")
    void docenteAjenoEInexistente() throws Exception {
        int ajena = tema("Macroeconomía de Coyuntura", "Inflación y precios").getIdTema();

        mockMvc.perform(get("/glosario/ver/" + ajena)).andExpect(status().isForbidden());
        mockMvc.perform(get("/glosario/ver/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el glosario con la baja, pero sin registrar ni modificar")
    void vistaDelAdministrador() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("BYMA")))
                .andExpect(content().string(Matchers.containsString("modalBajaTermino" + idTermino(primera(), "BYMA"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Registrar término"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarTermino"))));
    }

    @Test
    @DisplayName("Sin sesión se pide iniciar sesión")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/glosario/ver/" + primera())).andExpect(status().is3xxRedirection());
    }

    // ------------------------------------------------------------------ Formularios (CU-33 a CU-35)

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-33: registrar un término responde en JSON, y un error de validación responde 400")
    void registrar() throws Exception {
        mockMvc.perform(post("/glosario/registrar").param("temaId", String.valueOf(tercera()))
                .param("termino", "Opción").param("definicion", "Derecho a comprar o vender."))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.mensaje").value(Matchers.containsString("Opción")));
        mockMvc.perform(get("/glosario/ver/" + tercera()))
                .andExpect(content().string(Matchers.containsString("Derecho a comprar o vender.")));

        mockMvc.perform(post("/glosario/registrar").param("temaId", String.valueOf(tercera()))
                .param("termino", "opción").param("definicion", "Otra"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("El término ya está registrado")));
        mockMvc.perform(post("/glosario/registrar").param("temaId", String.valueOf(tercera()))
                .param("termino", "").param("definicion", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Debe completar el término y la definición.")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-34: modificar un término responde en JSON y se ve en el glosario")
    void modificar() throws Exception {
        int id = idTermino(segunda(), "TIR");

        mockMvc.perform(post("/glosario/modificar/" + id).param("termino", "TIR").param("definicion", "Rendimiento anual."))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mensaje").value("Término modificado correctamente."));
        mockMvc.perform(get("/glosario/ver/" + segunda()))
                .andExpect(content().string(Matchers.containsString("Rendimiento anual.")));

        mockMvc.perform(post("/glosario/modificar/" + id).param("termino", "Duration").param("definicion", "x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("El término ya está registrado")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-35: dar de baja un término responde en JSON y deja de mostrarse")
    void darDeBaja() throws Exception {
        int id = idTermino(primera(), "CNV");

        mockMvc.perform(post("/glosario/darDeBaja/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Término dado de baja con éxito."));
        mockMvc.perform(get("/glosario/ver/" + primera()))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Comisión Nacional de Valores"))))
                .andExpect(content().string(Matchers.containsString("2 términos")));
        mockMvc.perform(post("/glosario/darDeBaja/" + id)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("no se encuentra activo")));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador da de baja un término pero no puede registrar ni modificar")
    void administradorSoloDaDeBaja() throws Exception {
        int id = idTermino(primera(), "ON");

        mockMvc.perform(post("/glosario/darDeBaja/" + id)).andExpect(status().isOk());
        mockMvc.perform(post("/glosario/registrar").param("temaId", String.valueOf(primera()))
                .param("termino", "Nuevo").param("definicion", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/glosario/modificar/" + idTermino(primera(), "CNV")).param("termino", "CNV")
                .param("definicion", "x")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no puede registrar, modificar ni dar de baja términos")
    void alumnoNoGestiona() throws Exception {
        mockMvc.perform(post("/glosario/registrar").param("temaId", String.valueOf(segunda()))
                .param("termino", "Nuevo").param("definicion", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/glosario/modificar/" + idTermino(segunda(), "TIR")).param("termino", "TIR")
                .param("definicion", "x")).andExpect(status().isForbidden());
        mockMvc.perform(post("/glosario/darDeBaja/" + idTermino(segunda(), "TIR"))).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ Enlaces desde el resto de la plataforma

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("En el selector, Glosario ya no es 'Próximamente' y la vista del curso y la barra lateral enlazan al glosario")
    void enlacesDelDocente() throws Exception {
        int curso = idCurso("Mercado de Capitales Argentino");

        mockMvc.perform(get("/curso/acceder/" + curso))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-glosario=\"true\"")))
                .andExpect(content().string(Matchers.containsString("/glosario/buscar?cursoId=" + curso)))
                // "Ver listado" de cada tema ya viene filtrado por ese tema
                .andExpect(content().string(Matchers.containsString("/glosario/buscar?cursoId=" + curso + "&amp;temaId=" + primera())))
                .andExpect(content().string(Matchers.containsString("/glosario/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("Glosario 1")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno llega al glosario del tema desde la vista del curso")
    void enlacesDelAlumno() throws Exception {
        mockMvc.perform(get("/inscripcion/acceder/" + inscripcionDeValentina()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("/glosario/ver/" + primera())))
                .andExpect(content().string(Matchers.containsString("/glosario/ver/" + segunda())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("/glosario/buscar"))));
    }

    private int inscripcionDeValentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com"))
                .mapToInt(Inscripcion::getIdInscripcion).findFirst().orElseThrow();
    }
}
