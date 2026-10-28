package com.app.idoneos.configuracion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la carpeta del servidor donde se guardan los archivos subidos por los usuarios.
 *
 * La carpeta base se configura con la propiedad {@code idoneos.directorio-cursos} (por defecto {@code ./uploads}) y
 * contiene una carpeta por curso: {@code curso{id}/imagen.{ext}} y {@code curso{id}/tema{id}/material{id}.{ext}}.
 */
@Configuration
public class CargaArchivosConfiguracion {

    /**
     * Carpeta base de los archivos de los cursos, como ruta absoluta y normalizada.
     *
     * @param ruta La ruta configurada (relativa o absoluta).
     * @return La carpeta base de los archivos de los cursos.
     */
    @Bean
    public Path directorioCursos(@Value("${idoneos.directorio-cursos:./uploads}") String ruta) throws IOException {
        Path directorio = Paths.get(ruta).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
        return directorio;
    }
}
