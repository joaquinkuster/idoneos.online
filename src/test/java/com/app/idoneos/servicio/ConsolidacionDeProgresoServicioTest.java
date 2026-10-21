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
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Pruebas de la consolidación del progreso: completar una unidad al aprobar todas sus autoevaluaciones visibles, habilitar la
 * siguiente, emitir el certificado al completar la última unidad y revertir (CU-70 a CU-72). Valentina Ruiz cursa
 * "Mercado de Capitales Argentino" (tres unidades): tiene completada la unidad 1.
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

    private List<Unidad> unidades(Inscripcion inscripcion) {
        return inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream().map(c -> c.getUnidad()).toList();
    }

    private void aprobar(Inscripcion inscripcion, Unidad unidad) {
        for (Autoevaluacion autoevaluacion : unidad.getAutoevaluacionesPublicadas()) {
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
    @DisplayName("Completa la unidad aprobada, habilita la siguiente y no repite el trabajo")
    void completaLaUnidad() {
        Inscripcion inscripcion = valentina();
        Unidad segunda = unidades(inscripcion).get(1);
        var tercera = inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2);

        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda)); // todavía no aprobó nada
        assertFalse(inscripcion.estaUnidadHabilitada(tercera));

        aprobar(inscripcion, segunda);
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, segunda));
        assertTrue(inscripcion.estaUnidadCompletada(segunda));
        assertTrue(inscripcion.estaUnidadHabilitada(tercera));
        assertNotNull(progresoServicio.buscarPorInscripcionYUnidad(inscripcion, segunda).orElseThrow().getFechaCompletada());
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda)); // ya estaba completada
        assertNull(inscripcion.getCertificadoVigente()); // falta la última unidad
    }

    @Test
    @DisplayName("Al completar la última unidad se emite el certificado y al revertir una unidad se anula")
    void certificadoYReversion() {
        Inscripcion inscripcion = valentina();
        List<Unidad> unidades = unidades(inscripcion);
        aprobar(inscripcion, unidades.get(1));
        aprobar(inscripcion, unidades.get(2));
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, unidades.get(1)));
        assertTrue(progresoServicio.actualizarProgreso(inscripcion, unidades.get(2)));

        Certificado certificado = inscripcion.getCertificadoVigente();
        assertNotNull(certificado);
        assertTrue(certificado.getNumero().matches("CERT-\\d{4}-\\d{6}"), certificado.getNumero());
        assertEquals("Valentina Ruiz", certificado.getNombreAlumno());
        assertEquals("42345678", certificado.getDniAlumno());
        assertTrue(certificado.getTexto().contains("Mercado de Capitales Argentino"));
        assertFalse(certificado.getEnviado()); // el correo real llega con MOD-NF-01

        // Revertir una unidad: el curso deja de estar completo y el certificado se anula
        assertTrue(progresoServicio.revertirProgreso(inscripcion, unidades.get(1)));
        assertFalse(inscripcion.estaUnidadCompletada(unidades.get(1)));
        assertTrue(inscripcion.estaUnidadCompletada(unidades.get(2))); // las posteriores conservan su estado...
        assertFalse(inscripcion.estaUnidadHabilitada(inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().get(2)));
        assertTrue(certificado.estaAnulado());
        assertNull(inscripcion.getCertificadoVigente());
        assertFalse(progresoServicio.revertirProgreso(inscripcion, unidades.get(1))); // ya estaba revertida
    }

    @Test
    @DisplayName("No completa unidades de una inscripción dada de baja ni de otro programa")
    void noCompleta() {
        Inscripcion inscripcion = valentina();
        Unidad segunda = unidades(inscripcion).get(1);
        aprobar(inscripcion, segunda);

        Unidad ajena = inscripcionRepositorio.findAll().stream().filter(i -> i.getCurso().getNombre().equals("Finanzas Personales e Inversión"))
                .findFirst().orElseThrow().getCohorte().getPrograma().getCronogramaOrdenado().get(0).getUnidad();
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, ajena));

        inscripcion.marcarInactivo();
        assertFalse(progresoServicio.actualizarProgreso(inscripcion, segunda));
        assertFalse(inscripcion.estaUnidadCompletada(segunda));
    }
}
