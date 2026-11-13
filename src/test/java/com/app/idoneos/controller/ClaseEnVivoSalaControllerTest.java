package com.app.idoneos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.EstadoClaseEnVivo;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.service.streaming.StreamingService;

/** Pruebas de la sala de la clase en vivo, el estudio del docente, el chat y la autorización del servidor de medios. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClaseEnVivoSalaControllerTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String LUCIA = "lucia.fernandez@correo.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    @Autowired
    private StreamingService streamingService;

    private ClaseEnVivo enVivo() {
        return claseEnVivoRepository.findAll().stream()
                .filter(c -> c.getEstadoClaseEnVivo().getNombre().equals(EstadoClaseEnVivo.EN_VIVO)).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno de la cohorte entra a la sala y escribe en el chat, pero no puede escribir otro mensaje enseguida")
    void alumnoEntraYEscribe() throws Exception {
        int id = enVivo().getIdClaseEnVivo();

        mockMvc.perform(get("/claseEnVivo/sala/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Chat de la clase")));
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/mensajes").param("texto", "Hola")).andExpect(status().isOk());
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/mensajes").param("texto", "Otra vez")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/claseEnVivo/chat/" + id + "/mensajes")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Hola")));
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/eliminar/1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/claseEnVivo/estado/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"enVivo\":true")));
    }

    @Test
    @WithUserDetails(value = LUCIA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno de otra cohorte no entra a la sala ni al chat")
    void alumnoDeOtraCohorteNoEntra() throws Exception {
        int id = enVivo().getIdClaseEnVivo();

        mockMvc.perform(get("/claseEnVivo/sala/" + id)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscripcion/buscar"));
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/mensajes").param("texto", "Hola")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/claseEnVivo/estado/" + id)).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente ve su estudio con los datos de conexión y puede deshabilitar el chat")
    void docenteVeElEstudio() throws Exception {
        int id = enVivo().getIdClaseEnVivo();

        mockMvc.perform(get("/claseEnVivo/estudio/" + id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Datos de conexión")))
                .andExpect(content().string(Matchers.containsString("Finalizar clase")));
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/habilitado").param("valor", "false")).andExpect(status().isOk());
        // El chat vive en memoria y lo comparten las pruebas: se deja habilitado como estaba
        mockMvc.perform(post("/claseEnVivo/chat/" + id + "/habilitado").param("valor", "true")).andExpect(status().isOk());
        mockMvc.perform(get("/claseEnVivo/buscar").param("cursoId", String.valueOf(enVivo().getCohorte().getCurso().getIdCurso())))
                .andExpect(status().isOk()).andExpect(content().string(Matchers.containsString("Ingresar")));
    }

    @Test
    @DisplayName("El servidor de medios deja transmitir con la clave de la clase, y mirar con un acceso firmado")
    void autorizaAlServidorDeMedios() throws Exception {
        ClaseEnVivo clase = enVivo();
        clase.setClaveStream(streamingService.resumirClave("secreta"));
        claseEnVivoRepository.save(clase);
        String camino = streamingService.camino(clase);
        String token = streamingService.firmarAcceso(clase, 1);

        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"publish\",\"path\":\"" + camino + "\",\"password\":\"secreta\"}")).andExpect(status().isOk());
        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"publish\",\"path\":\"" + camino + "\",\"password\":\"otra\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"read\",\"path\":\"" + camino + "\",\"query\":\"token=" + token + "\"}")).andExpect(status().isOk());
        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"read\",\"path\":\"clase-999\",\"query\":\"token=" + token + "\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"read\",\"path\":\"" + camino + "\",\"query\":\"\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/streaming/autorizar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"api\",\"password\":\"mala\"}")).andExpect(status().isUnauthorized());
    }
}
