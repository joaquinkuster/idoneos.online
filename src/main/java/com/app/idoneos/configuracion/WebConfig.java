package com.app.idoneos.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.app.idoneos.servicio.Almacenamiento.AlmacenamientoImagenServicio;

/**
 * Configuración de Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AlmacenamientoImagenServicio almacenamientoImagenServicio;

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

    /**
     * Publica las imágenes de los cursos en la URL {@code /img/cursos/**}. Se buscan primero en la carpeta
     * del servidor donde se guardan las imágenes subidas por los usuarios y, si no están, en las imágenes
     * de ejemplo incluidas en la aplicación ({@code static/img/cursos}). Las imágenes se guardan en la caché del
     * navegador durante una semana.
     *
     * @param registry El registro de manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/cursos/**")
                .addResourceLocations(almacenamientoImagenServicio.getDirectorio().toUri().toString(),
                        "classpath:/static/img/cursos/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS));
    }
}
