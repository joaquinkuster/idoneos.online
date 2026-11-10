package com.app.idoneos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.SemillaService;

/** Clase principal de la aplicación que se ejecuta al iniciar el servicio Spring Boot. */
@SpringBootApplication
public class IdoneosApplication implements CommandLineRunner {

    /** Servicio encargado de insertar los datos de semilla al inicio de la aplicación. */
    @Autowired
    private SemillaService semillaService;

    @Autowired
    private CursoServiceImpl cursoService;

    /** Si se insertan los datos de semilla (usuarios, cursos y demás datos de demostración) al arrancar. */
    @Value("${idoneos.semilla:false}")
    private boolean insertarSemilla;

    /** Inicia la aplicación y lanza el contexto de Spring Boot. */
    public static void main(String[] args) {
        SpringApplication.run(IdoneosApplication.class, args);
    }

    /** Se ejecuta al iniciar la aplicación. */
    @Override
    public void run(String... args) throws Exception {
        // La semilla solo se ejecuta si se pidió con la propiedad idoneos.semilla
        if (insertarSemilla) {
            semillaService.insertarSemilla();
        }
        // Lleva las imágenes de los cursos a la carpeta de cada curso (datos creados con versiones anteriores)
        cursoService.migrarImagenes();
    }
}
