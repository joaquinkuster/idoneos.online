package com.app.idoneos.config;

import com.app.idoneos.config.interceptor.BloqueoDeIntentoInterceptor;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuración de Spring MVC. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private BloqueoDeIntentoInterceptor bloqueoDeIntento;

    /** Crea un bean de tipo HiddenHttpMethodFilter. */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    /** Publica las imágenes de la aplicación: /img/cursos/**. */
    /** Mantiene al alumno dentro del cuestionario mientras rinde una autoevaluación que no permite salir. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bloqueoDeIntento).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        CacheControl semana = CacheControl.maxAge(7, TimeUnit.DAYS);
        registry.addResourceHandler("/img/cursos/**")
                .addResourceLocations("classpath:/static/img/cursos/")
                .setCacheControl(semana);
        registry.addResourceHandler("/img/usuarios/**")
                .addResourceLocations("classpath:/static/img/usuarios/")
                .setCacheControl(semana);
        String carpeta = directorioCursos.toUri().toString();
        registry.addResourceHandler("/uploads/curso*/*")
                .addResourceLocations(carpeta.endsWith("/") ? carpeta : carpeta + "/")
                .setCacheControl(semana);
    }
}
