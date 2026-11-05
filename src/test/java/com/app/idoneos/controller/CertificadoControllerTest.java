package com.app.idoneos.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Certificado;
import com.app.idoneos.repository.CertificadoRepository;

/**
 * Pruebas de "Mis certificados": el alumno ve y descarga los suyos (Martín Gómez tiene uno en la semilla), no los de otro
 * alumno, y el administrador puede descargar cualquiera.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CertificadoControllerTest {

    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";
    private static final String ADMIN = "admin@idoneos.online";
    private static final String FAUSTO = "fausto.spotorno@idoneos.online";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CertificadoRepository certificadoRepository;

    private Certificado certificadoDeMartin() {
        return certificadoRepository.findByAnuladoFalse().stream()
                .filter(c -> c.getInscripcion().getAlumno().getUsuario().getCorreo().equals(MARTIN)).findFirst().orElseThrow();
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno ve sus certificados con su número y la descarga en PDF")
    void alumnoVeSusCertificados() throws Exception {
        Certificado certificado = certificadoDeMartin();
        mockMvc.perform(get("/certificado/buscar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/certificado/buscarPorAlumno"))
                .andExpect(content().string(Matchers.allOf(
                        Matchers.containsString("Mis certificados"),
                        Matchers.containsString(certificado.getNumero()),
                        Matchers.containsString("Análisis Técnico Bursátil"),
                        Matchers.containsString("/certificado/descargar/" + certificado.getIdCertificado()))));
    }

    @Test
    @WithUserDetails(value = VALENTINA, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("Un alumno sin certificados ve el estado vacío y no puede descargar el de otro")
    void alumnoSinCertificados() throws Exception {
        mockMvc.perform(get("/certificado/buscar"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Todavía no tenés certificados")));
        mockMvc.perform(get("/certificado/descargar/" + certificadoDeMartin().getIdCertificado()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithUserDetails(value = MARTIN, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El alumno descarga su certificado como PDF")
    void alumnoDescargaSuCertificado() throws Exception {
        Certificado certificado = certificadoDeMartin();
        byte[] pdf = mockMvc.perform(get("/certificado/descargar/" + certificado.getIdCertificado()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", Matchers.containsString(certificado.getNumero() + ".pdf")))
                .andReturn().getResponse().getContentAsByteArray();
        assertTrue(new String(pdf, 0, 4).equals("%PDF"));
    }

    @Test
    @WithUserDetails(value = ADMIN, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El administrador descarga cualquier certificado, pero no entra a Mis certificados")
    void administradorDescarga() throws Exception {
        mockMvc.perform(get("/certificado/descargar/" + certificadoDeMartin().getIdCertificado()))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
        mockMvc.perform(get("/certificado/buscar")).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = FAUSTO, userDetailsServiceBeanName = "usuarioDetallesService")
    @DisplayName("El docente no accede a los certificados de los alumnos")
    void docenteNoAccede() throws Exception {
        mockMvc.perform(get("/certificado/buscar")).andExpect(status().isForbidden());
        mockMvc.perform(get("/certificado/descargar/" + certificadoDeMartin().getIdCertificado()))
                .andExpect(status().isForbidden());
    }
}
