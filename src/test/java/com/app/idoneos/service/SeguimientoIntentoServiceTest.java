package com.app.idoneos.service;

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

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.repository.AutoevaluacionRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.intentoautoevaluacion.IntentoAutoevaluacionService;

/** Pruebas con los datos de la semilla. */
@SpringBootTest
@Transactional
class SeguimientoIntentoServiceTest {

    @Autowired
    private IntentoAutoevaluacionService intentoService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AutoevaluacionRepository autoevaluacionRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    private Inscripcion valentina() {
        return inscripcionRepository.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private Autoevaluacion autoevaluacionDeLaTema1() {
        return autoevaluacionRepository.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 1: Marco regulatorio")).findFirst().orElseThrow();
    }

    private Docente docenteDelCurso() {
        return docenteRepository.findAll().stream().filter(d -> valentina().getCurso().tieneDocente(d)).findFirst().orElseThrow();
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

        List<IntentoAutoevaluacion> intentos = intentoService.buscarDelCurso(idCurso, null);
        assertTrue(intentos.stream().anyMatch(i -> i.getInscripcion().getIdInscripcion() == valentina.getIdInscripcion()));
        assertTrue(intentos.stream().noneMatch(IntentoAutoevaluacion::esInactivo));
        assertEquals(intentos, intentoService.buscarDelCurso(idCurso, docenteDelCurso()));

        Docente ajeno = new Docente(); // sin identificador: no participa en el curso
        assertThrows(IllegalArgumentException.class, () -> intentoService.buscarDelCurso(idCurso, ajeno));
        assertThrows(IllegalArgumentException.class, () -> intentoService.buscarDelCurso(null, null));
    }

    @Test
    @DisplayName("CU-71: el docente aprueba un intento no aprobado sin cambiar su nota; no uno aprobado ni uno en curso")
    void aprobarPorDocente() {
        IntentoAutoevaluacion reprobado = intentoConNota(4f);
        assertEquals(IntentoAutoevaluacion.RESULTADO_NO_APROBADO, reprobado.getResultado());

        intentoService.aprobarPorDocente(reprobado.getIdIntentoAutoevaluacion(), docenteDelCurso());
        assertTrue(reprobado.getAprobado());
        assertEquals(4f, reprobado.getNota());
        assertEquals(IntentoAutoevaluacion.RESULTADO_APROBADO_POR_DOCENTE, reprobado.getResultado());

        assertThrows(IllegalArgumentException.class,
                () -> intentoService.aprobarPorDocente(reprobado.getIdIntentoAutoevaluacion(), docenteDelCurso()));
        assertThrows(IllegalArgumentException.class,
                () -> intentoService.aprobarPorDocente(intentoConNota(8f).getIdIntentoAutoevaluacion(), docenteDelCurso()));

        // Uno en curso no se puede aprobar
        Autoevaluacion secuencial = autoevaluacionRepository.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 2: Ejercicios de rendimiento")).findFirst().orElseThrow();
        IntentoAutoevaluacion enCurso = intentoService.iniciar(valentina().getAlumno(), secuencial.getIdAutoevaluacion());
        assertThrows(IllegalArgumentException.class,
                () -> intentoService.aprobarPorDocente(enCurso.getIdIntentoAutoevaluacion(), docenteDelCurso()));

        // El docente de otro curso no puede
        Docente ajeno = new Docente(); // sin identificador: no participa en el curso
        IntentoAutoevaluacion otro = intentoConNota(8f);
        assertThrows(IllegalArgumentException.class, () -> intentoService.aprobarPorDocente(otro.getIdIntentoAutoevaluacion(), ajeno));
    }

    @Test
    @DisplayName("CU-72: al dar de baja el intento que aprobaba el tema, ésta vuelve a quedar pendiente; aprobar otro la completa de nuevo")
    void darDeBajaRevierteLaTema() {
        Inscripcion valentina = valentina();
        var tema1 = autoevaluacionDeLaTema1().getTema();
        assertTrue(valentina.estaTemaCompletada(tema1));

        IntentoAutoevaluacion aprobado = intentoConNota(8f);
        assertTrue(intentoService.darDeBaja(aprobado.getIdIntentoAutoevaluacion()));
        assertTrue(aprobado.esInactivo());
        assertFalse(valentina.estaTemaCompletada(tema1));
        assertThrows(IllegalArgumentException.class, () -> intentoService.darDeBaja(aprobado.getIdIntentoAutoevaluacion()));

        // El docente aprueba el que queda: el tema se completa otra vez
        intentoService.aprobarPorDocente(intentoConNota(4f).getIdIntentoAutoevaluacion(), docenteDelCurso());
        assertTrue(valentina.estaTemaCompletada(tema1));
    }

    @Test
    @DisplayName("CU-72: dar de baja un intento no aprobado o en curso no toca el progreso")
    void darDeBajaSinEfectoEnElProgreso() {
        Inscripcion valentina = valentina();
        var tema1 = autoevaluacionDeLaTema1().getTema();

        assertFalse(intentoService.darDeBaja(intentoConNota(4f).getIdIntentoAutoevaluacion()));
        assertTrue(valentina.estaTemaCompletada(tema1));

        Autoevaluacion secuencial = autoevaluacionRepository.findAll().stream()
                .filter(a -> a.getNombre().equals("Autoevaluación Tema 2: Ejercicios de rendimiento")).findFirst().orElseThrow();
        IntentoAutoevaluacion enCurso = intentoService.iniciar(valentina.getAlumno(), secuencial.getIdAutoevaluacion());
        assertTrue(intentoService.buscarEnCursoSinSalida(valentina.getAlumno()).isPresent());
        assertFalse(intentoService.darDeBaja(enCurso.getIdIntentoAutoevaluacion()));
        assertTrue(intentoService.buscarEnCursoSinSalida(valentina.getAlumno()).isEmpty()); // queda libre del bloqueo
    }
}
