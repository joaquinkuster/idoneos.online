package com.app.idoneos.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuración de la carpeta del servidor donde se guardan los archivos subidos por los usuarios. */
@Configuration
public class CargaArchivosConfig {

    /** Define la carpeta base de los archivos de los cursos, como ruta absoluta y normalizada. */
    @Bean
    public Path directorioCursos(@Value("${idoneos.directorio-cursos:./uploads}") String ruta) throws IOException {
        Path directorio = Paths.get(ruta).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
        return directorio;
    }
}
