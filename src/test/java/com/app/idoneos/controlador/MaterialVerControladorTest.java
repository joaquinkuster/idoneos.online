package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.Material.MaterialServicio;

/**
 * Pruebas de la vista de un material ({@code GET /material/ver/{id}}): reproductor, visor de PDF, descarga de los
 * formatos que no se pueden previsualizar y redirección de los enlaces externos. Valentina Ruiz es alumna de la
 * cohorte en dictado de "Mercado de Capitales Argentino" (tema 2 habilitada, tema 3 bloqueada).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialVerControladorTest {

    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialServicio materialServicio;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Tema tema(String titulo) {
        return temaRepositorio.findAll().stream().filter(u -> u.getTitulo().equals(titulo)
                && u.getCurso().getNombre().equals("Mercado de Capitales Argentino")).findFirst().orElseThrow();
    }

    private Material subir(String tipo, String nombreArchivo, byte[] contenido, String enlace, String tema) {
        Docente fausto = docenteRepositorio.findAll().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
        int idTipo = tipoMaterialRepositorio.findAll().stream().filter(t -> t.getNombre().equals(tipo)).findFirst()
                .orElseThrow().getIdTipoMaterial();
        return materialServicio.subirMaterial(fausto, tema(tema).getIdTema(), idTipo, "Material de prueba", false,
                null, enlace, contenido == null ? null : new MockMultipartFile("archivo", nombreArchivo, "x", contenido));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Una grabación se muestra en un reproductor de video")
    void grabacion() throws Exception {
        Material material = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(view().name("pages/material/ver"))
                .andExpect(content().string(Matchers.containsString("<video")))
                .andExpect(content().string(Matchers.containsString("/material/archivo/" + material.getIdMaterial())))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Volver al curso"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Descargar"))))
                .andExpect(content().string(Matchers.containsString("Contenido de los temas")));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un PDF se muestra en el visor de páginas")
    void pdf() throws Exception {
        Material material = subir(TipoMaterial.PRESENTACION, "clase.pdf", PDF, null, "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("data-visor-pdf")))
                .andExpect(content().string(Matchers.containsString("data-visor-siguiente")))
                // zoom: acercar, alejar y volver al 100%, sobre una zona que se desplaza
                .andExpect(content().string(Matchers.containsString("data-visor-acercar")))
                .andExpect(content().string(Matchers.containsString("data-visor-alejar")))
                .andExpect(content().string(Matchers.containsString("data-visor-zoom")))
                .andExpect(content().string(Matchers.containsString("mv-desplazable")))
                .andExpect(content().string(Matchers.containsString("/js/visor.js")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("<video"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El material no ofrece ninguna forma de descarga y se protege del clic derecho, la impresión y la grabación")
    void sinDescargaYConProteccion() throws Exception {
        Material video = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta fija y bonos");
        Material pdf = subir(TipoMaterial.PRESENTACION, "clase.pdf", PDF, null, "Renta fija y bonos");

        for (Material material : new Material[] { video, pdf }) {
            mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                    .andExpect(content().string(Matchers.not(Matchers.containsString("Descargar"))))
                    .andExpect(content().string(Matchers.not(Matchers.containsString(" download"))))
                    .andExpect(content().string(Matchers.containsString("/js/proteccion.js")))
                    .andExpect(content().string(Matchers.containsString("mv-protegido")))
                    // la marca de agua lleva el nombre y el correo de quien mira
                    .andExpect(content().string(Matchers.containsString("data-marca=\"Fausto Spotorno · fausto.spotorno@idoneos.online\"")));
        }
        mockMvc.perform(get("/material/ver/" + video.getIdMaterial()))
                .andExpect(content().string(Matchers.containsString("nodownload nofullscreen")))
                .andExpect(content().string(Matchers.containsString("disablePictureInPicture")));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un enlace externo redirige directamente a su página")
    void enlaceExterno() throws Exception {
        Material material = subir(TipoMaterial.BIBLIOGRAFIA, null, null, "https://sitio.com/libro", "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://sitio.com/libro"));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un material sin archivo muestra el aviso y su descripción")
    void sinArchivo() throws Exception {
        Material material = materialRepositorio.findAll().stream()
                .filter(m -> m.getTitulo().equals("Guía teórica de renta fija")).findFirst().orElseThrow();

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("todavía no tiene archivo")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno con el tema habilitado ve el material, con su propio menú de gestión académica")
    void alumnoConAcceso() throws Exception {
        Material material = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<video")))
                .andExpect(content().string(Matchers.containsString("/inscripcion/acceder/")));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no ve un material de un tema bloqueado")
    void alumnoSinAcceso() throws Exception {
        Material material = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta variable y opciones");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador ve cualquier material")
    void administrador() throws Exception {
        Material material = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<video")));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un material inexistente responde 404")
    void inexistente() throws Exception {
        mockMvc.perform(get("/material/ver/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sin sesión se pide iniciar sesión")
    void sinSesion() throws Exception {
        Material material = subir(TipoMaterial.GRABACION, "clase.mp4", MP4, null, "Renta fija y bonos");

        mockMvc.perform(get("/material/ver/" + material.getIdMaterial())).andExpect(status().is3xxRedirection());
    }
}
