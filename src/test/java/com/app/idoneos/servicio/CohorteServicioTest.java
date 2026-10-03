package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

/**
 * Pruebas de las reglas de negocio de las cohortes (CU-11 a CU-14), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class CohorteServicioTest {

    private static final LocalDate INICIO = LocalDate.now().plusMonths(12);

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    private Programa programaDe(String curso) {
        return programaServicio.buscarPorCurso(cursoServicio.buscarPorNombre(curso).orElseThrow()).get(0);
    }

    private Cohorte cohorte(String curso, String estado) {
        return cohorteServicio.buscarConFiltros(programaDe(curso).getIdPrograma(), null, estado, null, null, null).get(0);
    }

    @Test
    @DisplayName("CU-11: busca cohortes por estado calculado a partir de sus fechas")
    void buscarCohortesPorEstado() {
        Integer idPrograma = programaDe("Mercado de Capitales Argentino").getIdPrograma();

        assertEquals(1, cohorteServicio.buscarConFiltros(idPrograma, null, "Abierta", null, null, null).size());
        assertEquals(1, cohorteServicio.buscarConFiltros(idPrograma, null, "En dictado", null, null, null).size());
        assertEquals(1, cohorteServicio.buscarConFiltros(idPrograma, null, "Finalizada", null, null, null).size());
        assertEquals(3, cohorteServicio.buscarConFiltros(idPrograma, null, null, null, null, null).size());
    }

    @Test
    @DisplayName("CU-12: registra una cohorte de un curso grabado")
    void registrarCohorteDeCursoGrabado() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");

        Cohorte cohorte = cohorteServicio.registrarCohorte(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                null, null, programa.getDuracionTotalSemanas(), 25);

        assertEquals(25, cohorte.getCupoMaximo());
        assertNull(cohorte.getFechaInicioDictado());
        assertEquals("Próxima", cohorte.getEstado());
    }

    @Test
    @DisplayName("CU-12: un curso en vivo exige las fechas de dictado")
    void registrarCohorteEnVivoSinFechasDeDictado() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(programa.getIdPrograma(), INICIO, INICIO.plusDays(30), null, null,
                        12, null));
        assertTrue(error.getMessage().contains("dictado"));
    }

    @Test
    @DisplayName("CU-12: registra una cohorte en vivo con dictado posterior a la inscripción")
    void registrarCohorteEnVivo() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        Cohorte cohorte = cohorteServicio.registrarCohorte(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                INICIO.plusDays(40), INICIO.plusDays(120), 12, null);

        assertEquals(INICIO.plusDays(40), cohorte.getFechaInicioDictado().toLocalDate());
    }

    @Test
    @DisplayName("CU-12: el dictado no puede comenzar antes de que termine la inscripción")
    void registrarCohorteConDictadoAntesDelFinDeInscripcion() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                        INICIO.plusDays(10), INICIO.plusDays(120), 12, null));
    }

    @Test
    @DisplayName("CU-12: valida fechas de inscripción, cupo y semanas de acceso")
    void registrarCohorteConDatosInvalidos() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        Integer id = programa.getIdPrograma();
        int duracion = programa.getDuracionTotalSemanas();

        assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(id, INICIO, null, null, null, duracion, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(id, INICIO.plusDays(30), INICIO, null, null, duracion, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(id, INICIO, INICIO.plusDays(30), null, null, duracion - 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteServicio.registrarCohorte(id, INICIO, INICIO.plusDays(30), null, null, duracion, 0));
    }

    @Test
    @DisplayName("CU-12: el programa debe tener el mínimo de unidades con material publicado")
    void registrarCohorteSinMinimoDeUnidadesConMaterial() {
        Programa programa = programaDe("Planificación Fiscal Corporativa");
        assertEquals(1, programa.getCantidadUnidadesConMaterialPublicado());

        assertThrows(IllegalArgumentException.class, () -> cohorteServicio.registrarCohorte(programa.getIdPrograma(),
                INICIO, INICIO.plusDays(30), null, null, programa.getDuracionTotalSemanas(), null));
    }

    @Test
    @DisplayName("CU-13: modifica una cohorte sin inscripciones")
    void modificarCohorteSinInscripciones() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        int duracion = cohorte.getPrograma().getDuracionTotalSemanas();

        Cohorte modificada = cohorteServicio.modificarCohorte(cohorte.getIdCohorte(), INICIO, INICIO.plusDays(20), null,
                null, duracion + 2, 40);

        assertEquals(40, modificada.getCupoMaximo());
        assertEquals(duracion + 2, modificada.getSemanasAcceso());
        assertTrue(modificada.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-13: no modifica una cohorte con inscripciones activas")
    void modificarCohorteConInscripciones() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Abierta");
        assertTrue(cohorte.getCantidadInscriptos() > 0);

        assertThrows(IllegalArgumentException.class, () -> cohorteServicio.modificarCohorte(cohorte.getIdCohorte(),
                INICIO, INICIO.plusDays(20), INICIO.plusDays(30), INICIO.plusDays(90), 12, null));
    }

    @Test
    @DisplayName("CU-14: no da de baja una cohorte con inscripciones activas")
    void darDeBajaCohorteConInscripciones() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Abierta");

        assertThrows(IllegalArgumentException.class, () -> cohorteServicio.darDeBajaCohorte(cohorte.getIdCohorte()));
    }

    @Test
    @DisplayName("CU-14: no da de baja una cohorte con clases en vivo activas")
    void darDeBajaCohorteConClasesEnVivo() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "En dictado");
        assertTrue(cohorte.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()));

        assertThrows(IllegalArgumentException.class, () -> cohorteServicio.darDeBajaCohorte(cohorte.getIdCohorte()));
    }

    @Test
    @DisplayName("CU-14: da de baja una cohorte y la desvincula de los docentes que la tienen como contexto de trabajo")
    void darDeBajaCohorteDesvinculaAlDocente() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Finalizada");
        Docente fausto = docenteServicio.buscarHabilitados().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
        cohorteServicio.cambiarContextoDeTrabajo(cohorte, fausto);

        cohorteServicio.darDeBajaCohorte(cohorte.getIdCohorte());

        assertTrue(cohorte.esInactivo());
        assertTrue(cohorte.getParticipacionesDocente().stream().allMatch(p -> p.getCohortePorDefecto() == null));
        assertFalse(cohorteServicio.buscarPorId(cohorte.getIdCohorte()).isPresent());
    }

    @Test
    @DisplayName("CU-11: el docente cambia su contexto de trabajo solo en los cursos en los que participa")
    void cambiarContextoDeTrabajo() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        List<Docente> docentes = docenteServicio.buscarHabilitados();
        Docente fausto = docentes.stream().filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online"))
                .findFirst().orElseThrow();
        Docente mariano = docentes.stream().filter(d -> d.getUsuario().getCorreo().equals("mariano.otalora@idoneos.online"))
                .findFirst().orElseThrow();

        cohorteServicio.cambiarContextoDeTrabajo(cohorte, fausto);
        assertThrows(IllegalArgumentException.class, () -> cohorteServicio.cambiarContextoDeTrabajo(cohorte, mariano));
    }
}
