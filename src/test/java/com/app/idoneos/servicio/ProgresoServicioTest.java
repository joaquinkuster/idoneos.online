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

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.Progreso.DetalleProgreso;
import com.app.idoneos.servicio.Progreso.ProgresoServicio;

/**
 * Pruebas de la búsqueda de progreso (CU-55, MOD-F-03).
 */
@SpringBootTest
@Transactional
class ProgresoServicioTest {

    @Autowired
    private ProgresoServicio progresoServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    private Inscripcion conProgreso() {
        return inscripcionRepositorio.findByBajaFalse().stream().filter(i -> i.getUnidadesCompletadas() > 0)
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("CU-55: lista las inscripciones activas del curso, de todas sus cohortes")
    void listaLasInscripcionesDelCurso() {
        Inscripcion inscripcion = conProgreso();
        int cursoId = inscripcion.getCurso().getIdCurso();

        List<Inscripcion> resultado = progresoServicio.buscarConFiltros(cursoId, null);

        assertTrue(resultado.contains(inscripcion));
        assertTrue(resultado.stream().allMatch(i -> i.getCurso().getIdCurso() == cursoId && !i.getBaja()));
    }

    @Test
    @DisplayName("CU-55: filtra por alumno (nombre, DNI o correo) sin distinguir mayúsculas ni tildes")
    void filtraPorAlumno() {
        Inscripcion inscripcion = conProgreso();
        int cursoId = inscripcion.getCurso().getIdCurso();
        String apellido = inscripcion.getAlumno().getUsuario().getApellido().toUpperCase();

        List<Inscripcion> porNombre = progresoServicio.buscarConFiltros(cursoId, apellido);
        assertTrue(porNombre.contains(inscripcion));
        assertTrue(porNombre.stream().allMatch(
                i -> i.getAlumno().getUsuario().getNombreCompleto().toUpperCase().contains(apellido)));

        assertTrue(progresoServicio.buscarConFiltros(cursoId, inscripcion.getAlumno().getUsuario().getDni())
                .contains(inscripcion));
        assertTrue(progresoServicio.buscarConFiltros(cursoId, "zzz-no-existe").isEmpty());
    }

    @Test
    @DisplayName("CU-55: el detalle muestra cada unidad del programa de la cohorte, si fue completada y su fecha")
    void detalleDelProgreso() {
        Inscripcion inscripcion = conProgreso();

        DetalleProgreso detalle = progresoServicio.buscarDetalle(inscripcion.getIdInscripcion());

        assertEquals(inscripcion, detalle.inscripcion());
        assertEquals(inscripcion.getCohorte().getPrograma().getCantidadUnidades(), detalle.unidades().size());
        long completadas = detalle.unidades().stream().filter(DetalleProgreso.AvanceUnidad::completada).count();
        assertEquals(inscripcion.getUnidadesCompletadas(), completadas);
        detalle.unidades().forEach(u -> {
            if (u.completada()) {
                assertTrue(u.fechaCompletada() != null);
            } else {
                assertFalse(u.fechaCompletada() != null);
            }
        });
        // en orden de dictado
        for (int i = 1; i < detalle.unidades().size(); i++) {
            assertTrue(detalle.unidades().get(i - 1).unidad().getNumeroOrden() < detalle.unidades().get(i)
                    .unidad().getNumeroOrden());
        }
    }

    @Test
    @DisplayName("CU-55: el detalle de una inscripción inexistente se informa como error")
    void detalleInexistente() {
        assertThrows(IllegalArgumentException.class, () -> progresoServicio.buscarDetalle(-1));
    }
}
