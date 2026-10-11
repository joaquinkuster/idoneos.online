package com.app.idoneos.configuracion;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

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
     * Publica las imágenes de la aplicación:
     * <ul>
     * <li>{@code /img/cursos/**}: las imágenes de ejemplo incluidas en la aplicación ({@code static/img/cursos}).</li>
     * <li>{@code /uploads/curso{id}/{imagen}}: la imagen de cada curso, guardada en su carpeta del servidor. El patrón
     * admite un solo nivel debajo de la carpeta del curso, por lo que las carpetas de unidad
     * ({@code curso{id}/unidad{id}/...}) con sus materiales <b>no</b> se publican: se entregan solo por un
     * endpoint que controla el acceso.</li>
     * </ul>
     * Las imágenes se guardan en la caché del navegador durante una semana (las imágenes de los cursos incluyen
     * la versión en la URL, por lo que un cambio se ve de inmediato).
     *
     * @param registry El registro de manejadores de recursos.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl semana = CacheControl.maxAge(7, TimeUnit.DAYS);
        registry.addResourceHandler("/img/cursos/**")
                .addResourceLocations("classpath:/static/img/cursos/")
                .setCacheControl(semana);
        String carpeta = directorioCursos.toUri().toString();
        registry.addResourceHandler("/uploads/curso*/*")
                .addResourceLocations(carpeta.endsWith("/") ? carpeta : carpeta + "/")
                .setCacheControl(semana);
    }
}
