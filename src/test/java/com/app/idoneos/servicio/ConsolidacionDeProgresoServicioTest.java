package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Pruebas de la consolidación del progreso: completar un tema al aprobar todas sus autoevaluaciones visibles, habilitar la
 * siguiente, emitir el certificado al completar la último tema y revertir (CU-70 a CU-72). Valentina Ruiz cursa
 * "Mercado de Capitales Argentino" (tres temas): tiene completada el tema 1.
 */
@SpringBootTest
@Transactional
class ConsolidacionDeProgresoServicioTest {

    @Autowired
    private ProgresoServicio progresoServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoRepositorio;

    private Inscripcion valentina() {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
    }

    private List<Tema> temas(Inscripcion inscripcion) {
        return inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream().map(c -> c.getTema()).toList();
    }

    private void aprobar(Inscripcion inscripcion, Tema tema) {
        for (Autoevaluacion autoevaluacion : tema.getAutoevaluacionesPublicadas()) {
            IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
            intento.setNota(10f);
            intento.setAprobado(true);
            intento.setFechaEntrega(LocalDateTime.now());
            intento = intentoRepositorio.save(intento);
            autoevaluacion.getIntentosAutoevaluacion().add(intento);
            inscripcion.getIntentosAutoevaluacion().add(intento);
        }
    }

    @Test
    @DisplayName("Completa el tema aprobado, habilita la siguiente y no repite el trabajo")
    void completaLaTema() {
        Inscripcion inscripcion = valentina();
        Tema segunda = temas(inscripcion).get(1);
        var tercera = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2);

        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda)); // todavía no aprobó nada
        assertFalse(inscripcion.estaTemaHabilitada(tercera));

        aprobar(inscripcion, segunda);
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, segunda));
        assertTrue(inscripcion.estaTemaCompletada(segunda));
        assertTrue(inscripcion.estaTemaHabilitada(tercera));
        assertNotNull(progresoServicio.buscarPorInscripcionYTema(inscripcion, segunda).orElseThrow().getFechaCompletada());
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda)); // ya estaba completada
        assertNull(inscripcion.getCertificadoVigente()); // falta la último tema
    }

    @Test
    @DisplayName("Al completar la último tema se emite el certificado y al revertir un tema se anula")
    void certificadoYReversion() {
        Inscripcion inscripcion = valentina();
        List<Tema> temas = temas(inscripcion);
        aprobar(inscripcion, temas.get(1));
        aprobar(inscripcion, temas.get(2));
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, temas.get(1)));
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, temas.get(2)));

        Certificado certificado = inscripcion.getCertificadoVigente();
        assertNotNull(certificado);
        assertTrue(certificado.getNumero().matches("CERT-\\d{4}-\\d{6}"), certificado.getNumero());
        assertEquals("Valentina Ruiz", certificado.getNombreAlumno());
        assertEquals("42345678", certificado.getDniAlumno());
        assertTrue(certificado.getTexto().contains("Mercado de Capitales Argentino"));
        assertFalse(certificado.getEnviado()); // el correo real llega con MOD-NF-01

        // Revertir un tema: el curso deja de estar completo y el certificado se anula
        assertTrue(progresoServicio.revertirProgreso(inscripcion, temas.get(1)));
        assertFalse(inscripcion.estaTemaCompletada(temas.get(1)));
        assertTrue(inscripcion.estaTemaCompletada(temas.get(2))); // las posteriores conservan su estado...
        assertFalse(inscripcion.estaTemaHabilitada(inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2)));
        assertTrue(certificado.estaAnulado());
        assertNull(inscripcion.getCertificadoVigente());
        assertFalse(progresoServicio.revertirProgreso(inscripcion, temas.get(1))); // ya estaba revertida
    }

    @Test
    @DisplayName("No completa temas de una inscripción dada de baja ni de otro programa")
    void noCompleta() {
        Inscripcion inscripcion = valentina();
        Tema segunda = temas(inscripcion).get(1);
        aprobar(inscripcion, segunda);

        Tema ajena = inscripcionRepositorio.findAll().stream().filter(i -> i.getCurso().getNombre().equals("Finanzas Personales e Inversión"))
                .findFirst().orElseThrow().getCohorte().getPrograma().getCronogramaOrdenado().get(0).getTema();
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, ajena));

        inscripcion.marcarInactivo();
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda));
        assertFalse(inscripcion.estaTemaCompletada(segunda));
    }
}
