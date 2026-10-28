package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;

/**
 * Todas las vistas con menú lateral (panel del administrador y panel del curso) marcan una, y solo una, opción activa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MenuActivoControladorTest {

    private static final Pattern ENLACE_ACTIVO = Pattern.compile("<a [^>]*class=\"pn-menu-enlace activo\"[^>]*>\\s*<i[^>]*></i>\\s*<span>([^<]*)</span>");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CursoRepositorio cursoRepositorio;
    @Autowired
    private TemaRepositorio temaRepositorio;
    @Autowired
    private MaterialRepositorio materialRepositorio;
    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;
    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private static String abreviado(String nombre) {
        return nombre.length() <= 22 ? nombre : nombre.substring(0, 19) + "...";
    }

    private int curso() {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals("Mercado de Capitales Argentino"))
                .mapToInt(c -> c.getIdCurso()).findFirst().orElseThrow();
    }

    private void marca(String url, String opcion) throws Exception {
        String html = mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Matcher m = ENLACE_ACTIVO.matcher(html);
        StringBuilder activas = new StringBuilder();
        int cantidad = 0;
        while (m.find()) {
            cantidad++;
            activas.append('[').append(m.group(1)).append(']');
        }
        assertEquals(1, cantidad, url + " debe marcar una sola opción, marca: " + activas);
        assertEquals("[" + opcion + "]", activas.toString(), url);
    }

    private void marcaPanelDelCurso(int cursoId) throws Exception {
        int temaId = temaRepositorio.findAll().stream().filter(u -> u.getCurso().getIdCurso() == cursoId)
                .mapToInt(u -> u.getIdTema()).findFirst().orElseThrow();
        // el primer botón del menú lleva el nombre del curso, abreviado a 22 caracteres
        marca("/curso/acceder/" + cursoId, abreviado(cursoRepositorio.findById(cursoId).orElseThrow().getNombre()));
        marca("/curso/acceder/" + cursoId + "?seccion=clases", "Clases en vivo");
        marca("/tema/buscar?cursoId=" + cursoId, "Temas");
        marca("/programa/buscar?cursoId=" + cursoId, "Programas");
        marca("/cronograma/buscar?cursoId=" + cursoId, "Cronograma");
        marca("/participante/buscar?cursoId=" + cursoId, "Participantes");
        marca("/material/buscar?cursoId=" + cursoId, "Materiales");
        marca("/glosario/buscar?cursoId=" + cursoId, "Glosario");
        marca("/glosario/ver/" + temaId, "Glosario");
        marca("/foro/buscar?cursoId=" + cursoId, "Foros");
        marca("/foro/ver/" + temaId, "Foros");
        materialRepositorio.findAll().stream().filter(m -> m.getCurso().getIdCurso() == cursoId).findFirst()
                .ifPresent(m -> {
                    try {
                        marca("/material/ver/" + m.getIdMaterial(), "Materiales");
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
        consultaForoRepositorio.findAll().stream().filter(c -> c.getTema().getCurso().getIdCurso() == cursoId).findFirst()
                .ifPresent(c -> {
                    try {
                        marca("/foro/consulta/" + c.getIdConsulta(), "Foros");
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Panel del administrador y panel del curso como administrador: siempre una opción activa")
    void administrador() throws Exception {
        marca("/curso/buscar", "Cursos");
        marca("/categoria/buscar", "Categorías");
        marca("/cohorte/buscar?cursoId=" + curso(), "Cohortes");
        marcaPanelDelCurso(curso());
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Panel del curso como docente: siempre una opción activa")
    void docente() throws Exception {
        marcaPanelDelCurso(curso());
    }

    @Test
    @WithUserDetails(value = "valentina.ruiz@correo.com", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("Panel del curso como alumno: siempre una opción activa")
    void alumno() throws Exception {
        Inscripcion i = inscripcionRepositorio.findAll().stream()
                .filter(x -> x.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")).findFirst().orElseThrow();
        String base = "/inscripcion/acceder/" + i.getIdInscripcion();
        marca(base, abreviado(i.getCurso().getNombre()));
        marca(base + "?seccion=cronograma", "Cronograma");
        marca(base + "?seccion=clases", "Clases en vivo");
        marca("/participante/ver/" + i.getIdInscripcion(), "Participantes");
    }
}
