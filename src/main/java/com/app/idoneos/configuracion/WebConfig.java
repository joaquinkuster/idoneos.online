package com.app.idoneos.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Crea un bean de tipo HiddenHttpMethodFilter.
     * Este filtro permite que los formularios HTML envíen métodos HTTP como PUT, DELETE, PATCH,
     * usando un campo oculto en el formulario.
     *
     * @return HiddenHttpMethodFilter configurado.
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}
