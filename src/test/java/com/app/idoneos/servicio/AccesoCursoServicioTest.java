package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.servicio.AccesoCurso.AccesoCursoServicio;
import com.app.idoneos.servicio.AccesoCurso.CronogramaAcceso;
import com.app.idoneos.servicio.AccesoCurso.UnidadAcceso;

/**
 * Pruebas del servicio del caso de uso CU-27 Acceder curso.
 */
@SpringBootTest
@Transactional
class AccesoCursoServicioTest {

    @Autowired
    private AccesoCursoServicio accesoCursoServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    private Inscripcion inscripcionDe(String correo) {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Las unidades se habilitan según el avance secuencial del alumno")
    void unidadesHabilitadasSegunAvance() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");

        List<UnidadAcceso> unidades = accesoCursoServicio.buscarUnidades(inscripcion);

        assertEquals(3, unidades.size());
        assertTrue(unidades.get(0).completada());
        assertTrue(unidades.get(0).habilitada());
        assertFalse(unidades.get(0).enCurso());
        assertTrue(unidades.get(1).habilitada());
        assertTrue(unidades.get(1).enCurso());
        assertFalse(unidades.get(2).habilitada());
        assertTrue(unidades.get(2).materiales().isEmpty(), "una unidad bloqueada no entrega contenido");
    }

    @Test
    @DisplayName("Sin progreso, sólo la primera unidad está habilitada y en curso")
    void sinProgresoSoloLaPrimera() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        progresoRepositorio.deleteAll(progresoRepositorio.findByInscripcion(inscripcion));
        progresoRepositorio.flush();
        inscripcion.getProgresos().clear();

        List<UnidadAcceso> unidades = accesoCursoServicio.buscarUnidades(inscripcion);

        assertTrue(unidades.get(0).enCurso());
        assertFalse(unidades.get(1).habilitada());
        assertFalse(unidades.get(2).habilitada());
    }

    @Test
    @DisplayName("La unidad habilitada incluye material, glosario, autoevaluaciones con intentos propios y foro")
    void contenidoDeLaUnidadHabilitada() {
        UnidadAcceso primera = accesoCursoServicio.buscarUnidades(inscripcionDe("valentina.ruiz@correo.com")).get(0);

        assertEquals(3, primera.materiales().size());
        assertEquals(3, primera.glosario().size());
        assertEquals(1, primera.autoevaluaciones().size());
        assertEquals(2, primera.autoevaluaciones().get(0).intentosUsados());
        assertEquals(1, primera.autoevaluaciones().get(0).intentosRestantes());
        assertEquals(8f, primera.autoevaluaciones().get(0).mejorNota());
        assertTrue(primera.autoevaluaciones().get(0).aprobada());
        assertEquals(1, primera.cantidadConsultas());
    }

    @Test
    @DisplayName("El cronograma indica que el alumno va al día cuando completó lo esperado")
    void cronogramaAlDia() {
        CronogramaAcceso cronograma = accesoCursoServicio.buscarCronograma(inscripcionDe("valentina.ruiz@correo.com"));

        assertEquals(3, cronograma.filas().size());
        assertEquals(9, cronograma.semanasTotales());
        assertEquals(3, cronograma.semanaEsperada());
        assertFalse(cronograma.atrasado());
        assertTrue(cronograma.desdeDictado());
    }

    @Test
    @DisplayName("El cronograma indica el atraso si el alumno no completó las unidades que ya deberían estar terminadas")
    void cronogramaConAtraso() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        progresoRepositorio.deleteAll(progresoRepositorio.findByInscripcion(inscripcion));
        progresoRepositorio.flush();
        inscripcion.getProgresos().clear();

        CronogramaAcceso cronograma = accesoCursoServicio.buscarCronograma(inscripcion);

        assertTrue(cronograma.atrasado());
        assertNotNull(cronograma.mensajeAtraso());
        assertEquals("Atrasada", cronograma.filas().get(0).estado());
    }

    @Test
    @DisplayName("El acceso se rechaza si venció o si el dictado de la cohorte no comenzó")
    void validacionesDeAcceso() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        Alumno alumno = inscripcion.getAlumno();
        assertEquals(inscripcion, accesoCursoServicio.validarAcceso(inscripcion.getIdInscripcion(), alumno));

        inscripcion.setFechaVencimientoAcceso(LocalDateTime.now().minusDays(1));
        IllegalArgumentException vencida = assertThrows(IllegalArgumentException.class,
                () -> accesoCursoServicio.validarAcceso(inscripcion.getIdInscripcion(), alumno));
        assertTrue(vencida.getMessage().contains("venció"));

        inscripcion.setFechaVencimientoAcceso(LocalDateTime.now().plusDays(10));
        inscripcion.getCohorte().setFechaInicioDictado(LocalDateTime.now().plusDays(5));
        IllegalArgumentException sinComenzar = assertThrows(IllegalArgumentException.class,
                () -> accesoCursoServicio.validarAcceso(inscripcion.getIdInscripcion(), alumno));
        assertTrue(sinComenzar.getMessage().contains("comienza"));
    }

    @Test
    @DisplayName("Una inscripción dada de baja no permite el acceso")
    void inscripcionDeBaja() {
        Inscripcion inscripcion = inscripcionDe("valentina.ruiz@correo.com");
        inscripcion.marcarInactivo();

        assertThrows(IllegalArgumentException.class,
                () -> accesoCursoServicio.validarAcceso(inscripcion.getIdInscripcion(), inscripcion.getAlumno()));
    }
}
