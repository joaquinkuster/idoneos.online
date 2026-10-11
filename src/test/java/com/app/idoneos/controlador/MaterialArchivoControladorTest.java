package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.Material.MaterialServicio;

/**
 * Pruebas de la entrega del contenido de los materiales ({@code GET /material/archivo/{id}}): solo la reciben quienes
 * tienen acceso y solo cuando el pedido lo hace la propia página del material (nunca por navegación directa, iframe ni
 * desde otro sitio); los videos admiten pedidos por rangos. Valentina Ruiz es alumna de la cohorte en dictado de
 * "Mercado de Capitales Argentino" (con la unidad 2 habilitada y la 3 bloqueada).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MaterialArchivoControladorTest {

    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2', 1, 2, 3, 4 };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MaterialServicio materialServicio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private int idTipo(String nombre) {
        return tipoMaterialRepositorio.findAll().stream().filter(t -> t.getNombre().equals(nombre)).findFirst()
                .orElseThrow().getIdTipoMaterial();
    }

    /** Sube una grabación a la unidad indicada, como lo haría el docente titular del curso. */
    private Material subir(Unidad unidad, boolean oculto) {
        return materialServicio.subirMaterial(docente("fausto.spotorno@idoneos.online"), unidad.getIdUnidad(),
                idTipo(TipoMaterial.GRABACION), "Clase de prueba", oculto, null, null,
                new MockMultipartFile("archivo", "clase.mp4", "video/mp4", MP4));
    }

    private Material subirEnUnidadDos(boolean oculto) {
        return subir(unidad("Mercado de Capitales Argentino", "Renta fija y bonos"), oculto);
    }

    /** El pedido que hace el reproductor o el visor de la propia página del material. */
    private MockHttpServletRequestBuilder pedidoDeLaPagina(int idMaterial) {
        return get("/material/archivo/" + idMaterial).header("Sec-Fetch-Dest", "video").header("Sec-Fetch-Site",
                "same-origin");
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El docente del curso recibe el video en el reproductor, sin nombre de archivo y sin caché")
    void docenteRecibeElContenido() throws Exception {
        Material material = subirEnUnidadDos(true);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().bytes(MP4)).andExpect(content().contentType("video/mp4"))
                .andExpect(header().string("Content-Disposition", "inline"))
                .andExpect(header().string("Cache-Control", Matchers.containsString("no-store")));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El PDF también se entrega solo al visor de la página (pedido de tipo 'empty', como el de PDF.js)")
    void pdfParaElVisor() throws Exception {
        Material pdf = materialServicio.subirMaterial(docente("fausto.spotorno@idoneos.online"),
                unidad("Mercado de Capitales Argentino", "Renta fija y bonos").getIdUnidad(),
                idTipo(TipoMaterial.PRESENTACION), "Diapositivas", false, null, null,
                new MockMultipartFile("archivo", "clase.pdf", "application/pdf", PDF));

        mockMvc.perform(get("/material/archivo/" + pdf.getIdMaterial()).header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Site", "same-origin")).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("No se entrega por navegación directa, en un iframe, desde otro sitio ni sin encabezados del navegador")
    void soloParaLaPropiaPagina() throws Exception {
        int id = subirEnUnidadDos(false).getIdMaterial();

        // abrir la dirección en otra pestaña, o con "guardar enlace como"
        mockMvc.perform(get("/material/archivo/" + id).header("Sec-Fetch-Dest", "document").header("Sec-Fetch-Site",
                "same-origin")).andExpect(status().isForbidden());
        // incrustarla en un iframe, embed u object
        mockMvc.perform(get("/material/archivo/" + id).header("Sec-Fetch-Dest", "iframe").header("Sec-Fetch-Site",
                "same-origin")).andExpect(status().isForbidden());
        // pedirla desde otro sitio
        mockMvc.perform(get("/material/archivo/" + id).header("Sec-Fetch-Dest", "video").header("Sec-Fetch-Site",
                "cross-site")).andExpect(status().isForbidden());
        // sin informar el origen del pedido (herramientas de línea de comandos)
        mockMvc.perform(get("/material/archivo/" + id)).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno con la unidad habilitada recibe el contenido y puede pedir un rango del video")
    void alumnoRecibeElContenidoYPideUnRango() throws Exception {
        Material material = subirEnUnidadDos(false);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isOk())
                .andExpect(content().bytes(MP4));
        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial()).header("Range", "bytes=0-3"))
                .andExpect(status().isPartialContent())
                .andExpect(content().bytes(new byte[] { 0, 0, 0, 0x18 }))
                .andExpect(header().string("Content-Range", "bytes 0-3/" + MP4.length));
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno no recibe el material de una unidad bloqueada ni el material oculto")
    void alumnoSinAccesoABloqueadoNiOculto() throws Exception {
        Material deUnidadBloqueada = subir(unidad("Mercado de Capitales Argentino", "Renta variable y opciones"), false);
        Material oculto = subirEnUnidadDos(true);

        mockMvc.perform(pedidoDeLaPagina(deUnidadBloqueada.getIdMaterial())).andExpect(status().isForbidden());
        mockMvc.perform(pedidoDeLaPagina(oculto.getIdMaterial())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "martin.gomez@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno cuyo dictado todavía no comenzó no recibe el material")
    void alumnoConDictadoSinComenzar() throws Exception {
        Material material = subirEnUnidadDos(false);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "tomas.herrera@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El alumno que no está inscripto en el curso no recibe el material")
    void alumnoSinInscripcion() throws Exception {
        Material material = subirEnUnidadDos(false);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "sebastian.bordato@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un docente que no participa en el curso no recibe el material")
    void docenteAjenoAlCurso() throws Exception {
        Material material = subir(unidad("Macroeconomía de Coyuntura", "Inflación y precios"), false);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador recibe el contenido de cualquier curso")
    void administradorRecibeElContenido() throws Exception {
        Material material = subirEnUnidadDos(true);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Un enlace externo no tiene contenido propio, y un material dado de baja o inexistente responde 404")
    void enlaceBajaOInexistente() throws Exception {
        Material enlace = materialServicio.subirMaterial(docente("fausto.spotorno@idoneos.online"),
                unidad("Mercado de Capitales Argentino", "Renta fija y bonos").getIdUnidad(),
                idTipo(TipoMaterial.BIBLIOGRAFIA), "Libro", false, "Autor", "https://sitio.com/libro.pdf", null);
        Material deBaja = subirEnUnidadDos(false);
        materialServicio.darDeBajaMaterial(deBaja.getIdMaterial(), null);

        mockMvc.perform(pedidoDeLaPagina(enlace.getIdMaterial())).andExpect(status().isNotFound());
        mockMvc.perform(pedidoDeLaPagina(deBaja.getIdMaterial())).andExpect(status().isNotFound());
        mockMvc.perform(pedidoDeLaPagina(999999)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sin sesión no se entrega ningún contenido: se pide iniciar sesión")
    void sinSesion() throws Exception {
        Material material = subirEnUnidadDos(false);

        mockMvc.perform(pedidoDeLaPagina(material.getIdMaterial())).andExpect(status().is3xxRedirection());
    }
}
