package com.app.idoneos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.SemillaServicio;

/**
 * Clase principal de la aplicación que se ejecuta al iniciar el servicio Spring Boot.
 * Implementa {@link CommandLineRunner} para ejecutar tareas al arrancar la aplicación.
 */
@SpringBootApplication
public class IdoneosApplication implements CommandLineRunner {

    /**
     * Servicio encargado de insertar los datos de semilla al inicio de la aplicación.
     */
    @Autowired
    private SemillaServicio semillaServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    /**
     * Si se insertan los datos de semilla (usuarios, cursos y demás datos de demostración) al arrancar. Está apagado
     * por defecto para que un entorno real no cargue usuarios de prueba: se enciende con {@code idoneos.semilla=true}.
     */
    @Value("${idoneos.semilla:false}")
    private boolean insertarSemilla;

    /**
     * Método principal de la aplicación que lanza el contexto de Spring Boot.
     *
     * @param args Los argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        SpringApplication.run(IdoneosApplication.class, args);
    }

    /**
     * Método ejecutado al iniciar la aplicación. Se utiliza para insertar los datos de semilla (si están habilitados).
     * Este método se ejecuta después de que Spring Boot haya arrancado el contexto.
     *
     * @param args Los argumentos de la línea de comandos (no utilizados en este caso).
     * @throws Exception Si ocurre algún error durante la ejecución del método.
     */
    @Override
    public void run(String... args) throws Exception {
        // La semilla solo se ejecuta si se pidió con la propiedad idoneos.semilla
        if (insertarSemilla) {
            semillaServicio.insertarSemilla();
        }
        // Lleva las imágenes de los cursos a la carpeta de cada curso (datos creados con versiones anteriores)
        cursoServicio.migrarImagenes();
    }
}
