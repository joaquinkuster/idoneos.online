package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;

/**
 * Pruebas de la validación del acceso del alumno a su curso (CU-27).
 */
@SpringBootTest
@Transactional
class InscripcionServicioTest {

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Inscripcion inscripcionDe(String correo) {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("El alumno accede a su inscripción vigente")
    void accedeAsuInscripcion() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");

        assertEquals(inscripcion,
                inscripcionServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));
    }

    @Test
    @DisplayName("Un alumno no puede acceder a la inscripción de otro")
    void inscripcionAjena() {
        Inscripcion ajena = inscripcionDe("valentina.ruiz@correo.com");
        Alumno otro = inscripcionDe("martin.gomez@correo.com").getAlumno();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(ajena.getIdInscripcion(), otro));
        assertTrue(error.getMessage().contains("No tenés una inscripción vigente"));
    }

    @Test
    @DisplayName("El acceso se rechaza si venció")
    void accesoVencido() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        inscripcion.setFechaVencimientoAcceso(LocalDateTime.now().minusDays(1));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));
        assertTrue(error.getMessage().contains("venció"));
    }

    @Test
    @DisplayName("El acceso se rechaza si el dictado de la cohorte todavía no comenzó")
    void dictadoSinComenzar() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        inscripcion.getCohorte().setFechaInicioDictado(LocalDateTime.now().plusDays(5));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));
        assertTrue(error.getMessage().contains("comienza"));
    }

    @Test
    @DisplayName("Una inscripción dada de baja o no habilitada no permite el acceso")
    void inscripcionDeBajaONoHabilitada() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        inscripcion.setHabilitado(false);
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));

        inscripcion.setHabilitado(true);
        inscripcion.marcarInactivo();
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));
    }
}
