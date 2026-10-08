package com.app.idoneos.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.repositorio.InscripcionRepositorio;

/**
 * Pruebas del contenido publicado de una unidad y de las clases en vivo de una cohorte (CU-27), con los datos de la
 * semilla: el alumno Valentina Ruiz está inscripta en la cohorte en dictado de "Mercado de Capitales Argentino".
 */
@SpringBootTest
@Transactional
class ContenidoDeUnidadTest {

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Inscripcion inscripcionDeValentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")).findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("La unidad entrega su material, glosario, autoevaluaciones y consultas del foro publicados")
    void contenidoPublicado() {
        Unidad primera = inscripcionDeValentina().getCohorte().getPrograma().getCronogramaOrdenado().get(0).getUnidad();

        assertEquals(3, primera.getMaterialesPublicados().size());
        assertEquals(3, primera.getTerminosGlosarioOrdenados().size());
        assertEquals("BYMA", primera.getTerminosGlosarioOrdenados().get(0).getTermino());
        assertEquals(1, primera.getAutoevaluacionesPublicadas().size());
        assertEquals(1, primera.getCantidadConsultasForo());
    }

    @Test
    @DisplayName("El material oculto o dado de baja no se publica")
    void materialNoPublicado() {
        Unidad primera = inscripcionDeValentina().getCohorte().getPrograma().getCronogramaOrdenado().get(0).getUnidad();
        Material oculto = primera.getMateriales().iterator().next();
        oculto.setOculto(true);

        assertEquals(2, primera.getMaterialesPublicados().size());

        oculto.setOculto(false);
        oculto.marcarInactivo();

        assertEquals(2, primera.getMaterialesPublicados().size());
    }

    @Test
    @DisplayName("La autoevaluación informa los intentos propios del alumno")
    void intentosDelAlumno() {
        Inscripcion inscripcion = inscripcionDeValentina();
        Autoevaluacion primera = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(0).getUnidad()
                .getAutoevaluacionesPublicadas().get(0);

        assertEquals(2, primera.intentosDe(inscripcion).size());
        assertEquals(1, primera.intentosRestantes(inscripcion));
        assertEquals(8f, primera.mejorNota(inscripcion));
        assertTrue(primera.estaAprobadaPor(inscripcion));

        Autoevaluacion segunda = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(1).getUnidad()
                .getAutoevaluacionesPublicadas().get(0);

        assertTrue(segunda.intentosDe(inscripcion).isEmpty());
        assertNull(segunda.mejorNota(inscripcion));
    }

    @Test
    @DisplayName("La cohorte lista sus clases en vivo y destaca la que se está transmitiendo")
    void clasesEnVivoDeLaCohorte() {
        Cohorte cohorte = inscripcionDeValentina().getCohorte();

        assertEquals(3, cohorte.getClasesEnVivoPublicadas().size());
        assertEquals("Clase 0: Presentación del curso", cohorte.getClasesEnVivoPublicadas().get(0).getTitulo());
        assertNotNull(cohorte.getClaseEnCurso());
        assertEquals("Clase magistral: Operatoria de renta fija en BYMA", cohorte.getClaseEnCurso().getTitulo());
    }

    @Test
    @DisplayName("El programa destaca su cohorte en dictado")
    void cohorteDestacada() {
        Cohorte cohorte = inscripcionDeValentina().getCohorte();

        assertEquals(cohorte.getIdCohorte(), cohorte.getPrograma().getCohorteDestacada().getIdCohorte());
    }
}
