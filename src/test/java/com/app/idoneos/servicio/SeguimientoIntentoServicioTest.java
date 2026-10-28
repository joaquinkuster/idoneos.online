package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.IntentoAutoevaluacion.IntentoAutoevaluacionServicio;

/**
 * Pruebas de CU-68 (buscar intentos), CU-71 (aprobar intento) y CU-72 (dar de baja intento) con los datos de la semilla.
 * Valentina Ruiz tiene en la autoevaluación del tema 1 un intento de 4,00 (no aprobado) y otro de 8,00 (aprobado, con el
 * que completó el tema); fausto.spotorno es docente del curso.
 */
@SpringBootTest
@Transactional
class SeguimientoIntentoServicioTest {

    @Autowired
    private IntentoAutoevaluacionServicio intentoServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Inscripcion valentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private Autoevaluacion autoevaluacionDeLaTema1() {
        return autoevaluacionRepositorio.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 1: Marco regulatorio")).findFirst().orElseThrow();
    }

    private Docente docenteDelCurso() {
        return docenteRepositorio.findAll().stream().filter(d -> valentina().getCurso().tieneDocente(d)).findFirst().orElseThrow();
    }

    private IntentoAutoevaluacion intentoConNota(float nota) {
        return autoevaluacionDeLaTema1().intentosDe(valentina()).stream().filter(i -> i.getNota() != null && i.getNota() == nota)
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("CU-68: busca los intentos vigentes del curso y rechaza al docente que no participa en él")
    void buscarDelCurso() {
        Inscripcion valentina = valentina();
        int idCurso = valentina.getCurso().getIdCurso();

        List<IntentoAutoevaluacion> intentos = intentoServicio.buscarDelCurso(idCurso, null);
        assertTrue(intentos.stream().anyMatch(i -> i.getInscripcion().getIdInscripcion() == valentina.getIdInscripcion()));
        assertTrue(intentos.stream().noneMatch(IntentoAutoevaluacion::esInactivo));
        assertEquals(intentos, intentoServicio.buscarDelCurso(idCurso, docenteDelCurso()));

        Docente ajeno = new Docente(); // sin identificador: no participa en el curso
        assertThrows(IllegalArgumentException.class, () -> intentoServicio.buscarDelCurso(idCurso, ajeno));
        assertThrows(IllegalArgumentException.class, () -> intentoServicio.buscarDelCurso(null, null));
    }

    @Test
    @DisplayName("CU-71: el docente aprueba un intento no aprobado sin cambiar su nota; no uno aprobado ni uno en curso")
    void aprobarPorDocente() {
        IntentoAutoevaluacion reprobado = intentoConNota(4f);
        assertEquals(IntentoAutoevaluacion.RESULTADO_NO_APROBADO, reprobado.getResultado());

        intentoServicio.aprobarPorDocente(reprobado.getIdIntentoAutoevaluacion(), docenteDelCurso());
        assertTrue(reprobado.getAprobado());
        assertEquals(4f, reprobado.getNota());
        assertEquals(IntentoAutoevaluacion.RESULTADO_APROBADO_POR_DOCENTE, reprobado.getResultado());

        assertThrows(IllegalArgumentException.class,
                () -> intentoServicio.aprobarPorDocente(reprobado.getIdIntentoAutoevaluacion(), docenteDelCurso()));
        assertThrows(IllegalArgumentException.class,
                () -> intentoServicio.aprobarPorDocente(intentoConNota(8f).getIdIntentoAutoevaluacion(), docenteDelCurso()));

        // Uno en curso no se puede aprobar
        Autoevaluacion secuencial = autoevaluacionRepositorio.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 2: Ejercicios de rendimiento")).findFirst().orElseThrow();
        IntentoAutoevaluacion enCurso = intentoServicio.iniciarIntento(valentina().getAlumno(), secuencial.getIdAutoevaluacion());
        assertThrows(IllegalArgumentException.class,
                () -> intentoServicio.aprobarPorDocente(enCurso.getIdIntentoAutoevaluacion(), docenteDelCurso()));

        // El docente de otro curso no puede
        Docente ajeno = new Docente(); // sin identificador: no participa en el curso
        IntentoAutoevaluacion otro = intentoConNota(8f);
        assertThrows(IllegalArgumentException.class, () -> intentoServicio.aprobarPorDocente(otro.getIdIntentoAutoevaluacion(), ajeno));
    }

    @Test
    @DisplayName("CU-72: al dar de baja el intento que aprobaba el tema, ésta vuelve a quedar pendiente; aprobar otro la completa de nuevo")
    void darDeBajaRevierteLaTema() {
        Inscripcion valentina = valentina();
        var tema1 = autoevaluacionDeLaTema1().getTema();
        assertTrue(valentina.estaTemaCompletada(tema1));

        IntentoAutoevaluacion aprobado = intentoConNota(8f);
        assertTrue(intentoServicio.darDeBajaIntento(aprobado.getIdIntentoAutoevaluacion()));
        assertTrue(aprobado.esInactivo());
        assertFalse(valentina.estaTemaCompletada(tema1));
        assertThrows(IllegalArgumentException.class, () -> intentoServicio.darDeBajaIntento(aprobado.getIdIntentoAutoevaluacion()));

        // El docente aprueba el que queda: el tema se completa otra vez
        intentoServicio.aprobarPorDocente(intentoConNota(4f).getIdIntentoAutoevaluacion(), docenteDelCurso());
        assertTrue(valentina.estaTemaCompletada(tema1));
    }

    @Test
    @DisplayName("CU-72: dar de baja un intento no aprobado o en curso no toca el progreso")
    void darDeBajaSinEfectoEnElProgreso() {
        Inscripcion valentina = valentina();
        var tema1 = autoevaluacionDeLaTema1().getTema();

        assertFalse(intentoServicio.darDeBajaIntento(intentoConNota(4f).getIdIntentoAutoevaluacion()));
        assertTrue(valentina.estaTemaCompletada(tema1));

        Autoevaluacion secuencial = autoevaluacionRepositorio.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 2: Ejercicios de rendimiento")).findFirst().orElseThrow();
        IntentoAutoevaluacion enCurso = intentoServicio.iniciarIntento(valentina.getAlumno(), secuencial.getIdAutoevaluacion());
        assertTrue(intentoServicio.buscarEnCursoSinSalida(valentina.getAlumno()).isPresent());
        assertFalse(intentoServicio.darDeBajaIntento(enCurso.getIdIntentoAutoevaluacion()));
        assertTrue(intentoServicio.buscarEnCursoSinSalida(valentina.getAlumno()).isEmpty()); // queda libre del bloqueo
    }
}
