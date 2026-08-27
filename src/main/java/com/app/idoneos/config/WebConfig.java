package com.app.idoneos.config;
import com.app.idoneos.service.modulo_reportes.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
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
        // Se crea y se devuelve una instancia del filtro
        return new HiddenHttpMethodFilter();
    }
}

