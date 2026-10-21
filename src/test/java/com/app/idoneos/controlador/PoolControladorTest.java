package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.PoolRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;

/**
 * Pruebas de las vistas y los formularios del ABM de pools (CU-60 a CU-63). Fausto Spotorno es titular de "Mercado de
 * Capitales Argentino": su pool "Marco regulatorio" tiene intentos asociados y "Renta fija" lo usa una autoevaluación.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PoolControladorTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String ADMIN = "admin@idoneos.online";
    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String PREGUNTAS = "[{\"id\":null,\"texto\":\"¿Cuánto es 1+1?\",\"opcionMultiple\":false,"
            + "\"opciones\":[{\"id\":null,\"texto\":\"2\",\"correcta\":true},{\"id\":null,\"texto\":\"3\",\"correcta\":false}]}]";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private PoolRepositorio poolRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    private String curso() {
        return String.valueOf(cursoRepositorio.findAll().stream()
                .filter(c -> c.getNombre().equals("Mercado de Capitales Argentino")).findFirst().orElseThrow().getIdCurso());
    }

    private int pool(String nombre) {
        return poolRepositorio.findAll().stream().filter(p -> p.getNombre().equals(nombre)).mapToInt(Pool::getIdPool)
                .findFirst().orElseThrow();
    }

    private int unidad(String titulo) {
        return unidadRepositorio.findAll().stream().filter(u -> u.getTitulo().equals(titulo)).mapToInt(Unidad::getIdUnidad)
                .findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-60: el docente ve el listado sin 'Ver detalle' ni 'Última modificación', con las ventanas de crear y modificar")
    void listadoDelDocente() throws Exception {
        mockMvc.perform(get("/pool/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Marco regulatorio")))
                .andExpect(content().string(Matchers.containsString("Renta fija")))
                .andExpect(content().string(Matchers.containsString("data-bs-target=\"#modalNuevoPool\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalNuevoPool\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarPool" + pool("Renta fija") + "\"")))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPool")))
                .andExpect(content().string(Matchers.containsString("/pool/darDeBajaMasiva")))
                .andExpect(content().string(Matchers.containsString("data-pool-formulario")))
                .andExpect(content().string(Matchers.containsString("/js/pool.js")))
                .andExpect(content().string(Matchers.containsString("data-col=\"Fecha de creación\"")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Ver detalle"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Última modificación"))));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-62: el pool con intentos no se modifica: su botón está deshabilitado y no tiene ventana de edición")
    void poolConIntentos() throws Exception {
        mockMvc.perform(get("/pool/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Tiene intentos de autoevaluación asociados")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("id=\"modalEditarPool" + pool("Marco regulatorio") + "\""))))
                .andExpect(content().string(Matchers.containsString("id=\"modalEditarPool" + pool("Renta fija") + "\"")));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve el listado y puede dar de baja, pero no crear ni modificar")
    void listadoDelAdministrador() throws Exception {
        mockMvc.perform(get("/pool/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Marco regulatorio")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalNuevoPool"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("modalEditarPool"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-btn-editar"))))
                .andExpect(content().string(Matchers.containsString("id=\"modalBajaPool")));

        mockMvc.perform(post("/pool/registrar").param("unidadId", String.valueOf(unidad("Renta variable y opciones")))
                .param("nombre", "X").param("preguntas", PREGUNTAS)).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no accede a los pools")
    void elAlumnoNoAccede() throws Exception {
        mockMvc.perform(get("/pool/buscar").param("cursoId", curso())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-61: la ventana de alta ofrece las unidades del programa y la de modificación trae las preguntas del pool")
    void ventanasDelFormulario() throws Exception {
        mockMvc.perform(get("/pool/buscar").param("cursoId", curso()))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("action=\"/pool/registrar\"")))
                .andExpect(content().string(Matchers.containsString("name=\"unidadId\"")))
                .andExpect(content().string(Matchers.containsString("3. Renta variable y opciones")))
                .andExpect(content().string(Matchers.containsString("action=\"/pool/modificar/" + pool("Renta fija") + "\"")))
                .andExpect(content().string(Matchers.containsString("El rendimiento anual")));
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-61 y CU-62: registrar y modificar responden en JSON e informan los errores")
    void registrarYModificar() throws Exception {
        String unidad = String.valueOf(unidad("Renta variable y opciones"));
        mockMvc.perform(post("/pool/registrar").param("unidadId", unidad).param("nombre", "Nuevo pool")
                .param("preguntas", PREGUNTAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pool 'Nuevo pool' creado con éxito."));

        mockMvc.perform(post("/pool/registrar").param("unidadId", unidad).param("nombre", "Nuevo pool")
                .param("preguntas", PREGUNTAS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("Ya existe un pool")))
                .andExpect(jsonPath("$.campo").value("nombre"));

        mockMvc.perform(post("/pool/registrar").param("unidadId", unidad).param("nombre", "Roto")
                .param("preguntas", "esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("No se pudieron leer las preguntas")));

        mockMvc.perform(post("/pool/modificar/" + pool("Nuevo pool")).param("nombre", "Pool renombrado")
                .param("preguntas", PREGUNTAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Pool modificado correctamente."));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("CU-63: dar de baja responde en JSON y se rechaza si una autoevaluación activa usa el pool")
    void darDeBaja() throws Exception {
        mockMvc.perform(post("/pool/darDeBaja/" + pool("Renta fija")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("autoevaluación activa")));

        mockMvc.perform(post("/pool/darDeBajaMasiva").param("ids", String.valueOf(pool("Renta fija"))))
                .andExpect(status().isBadRequest());
    }
}
