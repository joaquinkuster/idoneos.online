package com.app.idoneos.service;

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

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.IntentoAutoevaluacionRepository;
import com.app.idoneos.service.progreso.ProgresoService;

/** Pruebas de la consolidación del progreso. */
@SpringBootTest
@Transactional
class ConsolidacionDeProgresoServiceTest {

    @Autowired
    private ProgresoService progresoService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private IntentoAutoevaluacionRepository intentoRepository;

    private Inscripcion valentina() {
        return inscripcionRepository.findAll().stream()
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
            intento = intentoRepository.save(intento);
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

        assertFalse(progresoService.actualizar(inscripcion, segunda)); // todavía no aprobó nada
        assertFalse(inscripcion.estaTemaHabilitada(tercera));

        aprobar(inscripcion, segunda);
        assertTrue(progresoService.actualizar(inscripcion, segunda));
        assertTrue(inscripcion.estaTemaCompletada(segunda));
        assertTrue(inscripcion.estaTemaHabilitada(tercera));
        assertNotNull(progresoService.buscarPorInscripcionYTema(inscripcion, segunda).orElseThrow().getFechaCompletada());
        assertFalse(progresoService.actualizar(inscripcion, segunda)); // ya estaba completada
        assertNull(inscripcion.getCertificadoVigente()); // falta la último tema
    }

    @Test
    @DisplayName("Al completar la último tema se emite el certificado y al revertir un tema se anula")
    void certificadoYReversion() {
        Inscripcion inscripcion = valentina();
        List<Tema> temas = temas(inscripcion);
        aprobar(inscripcion, temas.get(1));
        aprobar(inscripcion, temas.get(2));
        assertTrue(progresoService.actualizar(inscripcion, temas.get(1)));
        assertTrue(progresoService.actualizar(inscripcion, temas.get(2)));

        Certificado certificado = inscripcion.getCertificadoVigente();
        assertNotNull(certificado);
        assertTrue(certificado.getNumero().matches("CERT-\\d{4}-\\d{6}"), certificado.getNumero());
        assertEquals("Valentina Ruiz", certificado.getNombreAlumno());
        assertEquals("42345678", certificado.getDniAlumno());
        assertTrue(certificado.getTexto().contains("Mercado de Capitales Argentino"));
        assertFalse(certificado.getEnviado()); // el correo real llega con MOD-NF-01

        // Revertir un tema: el curso deja de estar completo y el certificado se anula
        assertTrue(progresoService.revertir(inscripcion, temas.get(1)));
        assertFalse(inscripcion.estaTemaCompletada(temas.get(1)));
        assertTrue(inscripcion.estaTemaCompletada(temas.get(2))); // las posteriores conservan su estado...
        assertFalse(inscripcion.estaTemaHabilitada(inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2)));
        assertTrue(certificado.estaAnulado());
        assertNull(inscripcion.getCertificadoVigente());
        assertFalse(progresoService.revertir(inscripcion, temas.get(1))); // ya estaba revertida
    }

    @Test
    @DisplayName("No completa temas de una inscripción dada de baja ni de otro programa")
    void noCompleta() {
        Inscripcion inscripcion = valentina();
        Tema segunda = temas(inscripcion).get(1);
        aprobar(inscripcion, segunda);

        Tema ajena = inscripcionRepository.findAll().stream().filter(i -> i.getCurso().getNombre().equals("Finanzas Personales e Inversión"))
                .findFirst().orElseThrow().getCohorte().getPrograma().getCronogramaOrdenado().get(0).getTema();
        assertFalse(progresoService.actualizar(inscripcion, ajena));

        inscripcion.marcarInactivo();
        assertFalse(progresoService.actualizar(inscripcion, segunda));
        assertFalse(inscripcion.estaTemaCompletada(segunda));
    }
}
