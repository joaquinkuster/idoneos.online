package com.app.idoneos.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/**
 * Pruebas del navbar único: es el mismo en el sitio público y en los paneles, con la marca de la empresa, los tres
 * enlaces del sitio y el menú del usuario con su rol; el menú lateral empieza debajo, sin cabecera propia.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NavbarControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("El visitante ve el navbar único con la marca, los tres enlaces y el botón Ingresar")
    void visitante() throws Exception {
        mockMvc.perform(get("/inicio")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<header class=\"nb\" data-navbar")))
                .andExpect(content().string(Matchers.containsString("/img/logos/image.png")))
                .andExpect(content().string(Matchers.containsString("nb-marca")))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Inicio<")))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Catálogo<")))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Acerca de<")))
                .andExpect(content().string(Matchers.containsString("nb-ingresar\">Ingresar</a>")))
                // el hero del inicio no promete "formación de posgrado"
                .andExpect(content().string(Matchers.not(Matchers.containsString("Formación de posgrado"))))
                .andExpect(content().string(Matchers.containsString("Cursos online de finanzas, economía y mercado de capitales")))
                // sin el lema debajo de la marca ni el navbar anterior
                .andExpect(content().string(Matchers.not(Matchers.containsString("wf-brand-sub"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("wf-top-navbar"))))
                .andExpect(content().string(Matchers.containsString("/css/navbar.css")))
                .andExpect(content().string(Matchers.containsString("/js/navbar.js")));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El usuario autenticado ve su nombre y su rol en el sitio público, igual que en el panel")
    void usuarioEnElSitioPublico() throws Exception {
        mockMvc.perform(get("/inicio")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Fausto Spotorno")))
                .andExpect(content().string(Matchers.containsString("um-rol\">Docente<")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("nb-ingresar"))));
    }

    @Test
    @WithUserDetails(value = "fausto.spotorno@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("En el panel el navbar es el mismo, con el botón del menú lateral, y el menú lateral no lleva marca propia")
    void panel() throws Exception {
        mockMvc.perform(get("/cronograma/buscar").param("cursoId", "2")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<header class=\"nb\" data-navbar")))
                .andExpect(content().string(Matchers.containsString("data-menu-lateral")))
                .andExpect(content().string(Matchers.containsString("nb-enlace\">Inicio<")))
                .andExpect(content().string(Matchers.containsString("um-rol\">Docente<")))
                .andExpect(content().string(Matchers.containsString("pn-menu-nav")))
                // el botón "Curso" arriba del menú (sin tarjeta)
                .andExpect(content().string(Matchers.containsString("fa-house")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-menu-curso"))))
                // el menú lateral se carga contraído, salvo que el usuario lo haya expandido
                .andExpect(content().string(Matchers.containsString("idoneos.panel.menuColapsado")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-menu-cabecera"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-menu-marca"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("data-colapsar-menu"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("pn-barra"))));
    }

    @Test
    @WithUserDetails(value = "admin@idoneos.online", userDetailsServiceBeanName = "usuarioDetallesServicio")
    @DisplayName("El administrador tiene el mismo navbar y un solo acceso a su panel en el menú del usuario")
    void administrador() throws Exception {
        mockMvc.perform(get("/curso/buscar")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("<header class=\"nb\" data-navbar")))
                .andExpect(content().string(Matchers.containsString("Panel Administrador")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Ir al sitio"))));
        mockMvc.perform(get("/inicio")).andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Panel Administrador")));
    }
}
