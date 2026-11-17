package com.app.idoneos.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.app.idoneos.service.sesion.SesionService;

/** Configuración de pruebas que agrega la sesión registrada a todos los pedidos de MockMvc. */
@Configuration
public class SesionDePruebaConfig {

    /**
     * Crea el personalizador de MockMvc que usa {@link SesionDePrueba}.
     *
     * @param sesionService El servicio de sesiones.
     * @return El personalizador que se aplica a cada MockMvc de las pruebas.
     */
    @Bean
    MockMvcBuilderCustomizer sesionDePruebaCustomizer(SesionService sesionService) {
        SesionDePrueba sesionDePrueba = new SesionDePrueba(sesionService);
        return constructor -> constructor.defaultRequest(get("/").with(sesionDePrueba));
    }
}
