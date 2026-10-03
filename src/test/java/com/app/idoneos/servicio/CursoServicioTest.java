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

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.servicio.Categoria.CategoriaServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Docente.DocenteServicioImpl;
import com.app.idoneos.servicio.Modalidad.ModalidadServicioImpl;
import com.app.idoneos.servicio.Nivel.NivelServicioImpl;

/**
 * Pruebas de las reglas de negocio de los cursos (CU-01 a CU-06), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class CursoServicioTest {

    private static final String FAUSTO = "fausto.spotorno@idoneos.online";
    private static final String SEBASTIAN = "sebastian.bordato@idoneos.online";
    private static final String MARIANO = "mariano.otalora@idoneos.online";

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private CategoriaServicioImpl categoriaServicio;

    @Autowired
    private NivelServicioImpl nivelServicio;

    @Autowired
    private ModalidadServicioImpl modalidadServicio;

    @Autowired
    private DocenteServicioImpl docenteServicio;

    private Integer idDocente(String correo) {
        return docenteServicio.buscarHabilitados().stream()
                .filter(docente -> docente.getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow()
                .getIdDocente();
    }

    private Integer idModalidad(String nombre) {
        return modalidadServicio.obtenerTodo().stream().filter(m -> m.getNombre().equals(nombre)).findFirst()
                .orElseThrow().getIdModalidad();
    }

    private Integer idCategoria(String nombre) {
        return categoriaServicio.obtenerTodo().stream().filter(c -> c.getNombre().equals(nombre)).findFirst()
                .orElseThrow().getIdCategoria();
    }

    private Integer idNivel(String nombre) {
        return nivelServicio.obtenerTodo().stream().filter(n -> n.getNombre().equals(nombre)).findFirst().orElseThrow()
                .getIdNivel();
    }

    private Curso curso(String nombre) {
        return cursoServicio.buscarPorNombre(nombre).orElseThrow();
    }

    @Test
    @DisplayName("CU-03: registra un curso con sus modalidades y su equipo docente")
    void registrarCursoConDatosValidos() {
        Curso curso = cursoServicio.registrarCurso("Curso de Prueba", "Descripción", 1000f, null,
                idCategoria("Mercado de Capitales"), idNivel("Básico"), true,
                List.of(idModalidad(Modalidad.GRABADA), idModalidad(Modalidad.EN_VIVO)), idDocente(FAUSTO),
                idDocente(SEBASTIAN));

        assertEquals("Curso de Prueba", curso.getNombre());
        assertEquals(2, curso.getModalidades().size());
        assertEquals(FAUSTO, curso.getDocenteTitular().getUsuario().getCorreo());
        assertEquals(SEBASTIAN, curso.getDocenteAyudante().getUsuario().getCorreo());
        assertFalse(curso.esInactivo());
    }

    @Test
    @DisplayName("CU-03: informa los campos obligatorios que faltan")
    void registrarCursoSinCamposObligatorios() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("", null, null, null, null, null, false, null, null, null));
        assertTrue(error.getMessage().contains("nombre"));
        assertTrue(error.getMessage().contains("docente titular"));
    }

    @Test
    @DisplayName("CU-03: rechaza un precio negativo")
    void registrarCursoConPrecioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, -1f, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO), null));
    }

    @Test
    @DisplayName("CU-03: el docente titular no puede ser también ayudante")
    void registrarCursoConTitularYAyudanteIguales() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.registrarCurso("Curso Nuevo", null, 10f, null, idCategoria("Macroeconomía"),
                        idNivel("Básico"), false, List.of(idModalidad(Modalidad.GRABADA)), idDocente(FAUSTO),
                        idDocente(FAUSTO)));
        assertTrue(error.getMessage().contains("titular"));
    }

    @Test
    @DisplayName("CU-04: con inscripciones activas solo se puede modificar el precio, el equipo docente y la imagen")
    void modificarCursoConInscripcionesNoPermiteCambiarElNombre() {
        Curso curso = curso("Mercado de Capitales Argentino");
        assertTrue(cursoServicio.tieneInscripcionesActivas(curso));

        assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), "Otro nombre", curso.getDescripcion(), 1f, null,
                        curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                        curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(), idDocente(FAUSTO),
                        idDocente(SEBASTIAN)));
    }

    @Test
    @DisplayName("CU-04: con inscripciones activas se puede modificar el precio")
    void modificarCursoConInscripcionesPermiteCambiarElPrecio() {
        Curso curso = curso("Mercado de Capitales Argentino");

        Curso modificado = cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(),
                175000f, null, curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                curso.getEmiteCertificado(), curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(),
                idDocente(FAUSTO), idDocente(SEBASTIAN));

        assertEquals(175000f, modificado.getPrecio());
        assertTrue(modificado.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-04: no se puede desvincular a un docente con clases o material activo en el curso")
    void modificarCursoNoPermiteDesvincularDocenteConMaterial() {
        Curso curso = curso("Mercado de Capitales Argentino");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cursoServicio.modificarCurso(curso.getIdCurso(), curso.getNombre(), curso.getDescripcion(), 1f,
                        null, curso.getCategoria().getIdCategoria(), curso.getNivel().getIdNivel(),
                        curso.getEmiteCertificado(),
                        curso.getModalidades().stream().map(Modalidad::getIdModalidad).toList(), idDocente(SEBASTIAN),
                        null));
        assertTrue(error.getMessage().contains("Fausto"));
    }

    @Test
    @DisplayName("CU-04: sin inscripciones se pueden modificar todos los datos y el equipo docente")
    void modificarCursoSinInscripciones() {
        Curso curso = curso("Introducción a las Finanzas");

        Curso modificado = cursoServicio.modificarCurso(curso.getIdCurso(), "Introducción a las Finanzas II", "Nueva",
                50000f, null, idCategoria("Macroeconomía"), idNivel("Avanzado"), true,
                List.of(idModalidad(Modalidad.EN_VIVO)), idDocente(MARIANO), idDocente(FAUSTO));

        assertEquals("Introducción a las Finanzas II", modificado.getNombre());
        assertEquals(MARIANO, modificado.getDocenteTitular().getUsuario().getCorreo());
        assertEquals(FAUSTO, modificado.getDocenteAyudante().getUsuario().getCorreo());
        assertTrue(modificado.incluyeModalidad(Modalidad.EN_VIVO));
        assertFalse(modificado.incluyeModalidad(Modalidad.GRABADA));
    }

    @Test
    @DisplayName("CU-05: no se puede dar de baja un curso con programas o unidades activas")
    void darDeBajaCursoConProgramasActivos() {
        Curso curso = curso("Macroeconomía de Coyuntura");

        assertThrows(IllegalArgumentException.class, () -> cursoServicio.darDeBajaCurso(curso.getIdCurso()));
    }

    @Test
    @DisplayName("CU-05: da de baja un curso sin dependencias y deja de estar activo")
    void darDeBajaCursoSinDependencias() {
        Curso curso = curso("Introducción a las Finanzas");

        cursoServicio.darDeBajaCurso(curso.getIdCurso());

        assertTrue(curso(curso.getNombre()).esInactivo());
        assertThrows(IllegalArgumentException.class, () -> cursoServicio.darDeBajaCurso(curso.getIdCurso()));
    }

    @Test
    @DisplayName("CU-06: el catálogo solo incluye cursos activos con cohortes con inscripción abierta")
    void catalogoSoloConCohortesAbiertas() {
        List<String> nombres = cursoServicio.buscarEnCatalogo(null, null, null, null, null).stream()
                .map(Curso::getNombre).toList();

        assertTrue(nombres.contains("Mercado de Capitales Argentino"));
        assertTrue(nombres.contains("Macroeconomía de Coyuntura"));
        assertFalse(nombres.contains("Análisis Técnico Bursátil"), "su cohorte todavía no abrió la inscripción");
        assertFalse(nombres.contains("Introducción a las Finanzas"), "no tiene cohortes");
        assertFalse(nombres.contains("Economía Argentina 2024"), "está dado de baja");
    }

    @Test
    @DisplayName("CU-01: el docente solo ve los cursos en los que participa")
    void buscarCursosRestringidosAlDocente() {
        var mariano = docenteServicio.buscarHabilitados().stream()
                .filter(d -> d.getUsuario().getCorreo().equals(MARIANO)).findFirst().orElseThrow();

        List<String> nombres = cursoServicio.buscarConFiltros(null, null, null, null, null, false, mariano).stream()
                .map(Curso::getNombre).toList();

        assertTrue(nombres.contains("Finanzas Personales e Inversión"));
        assertTrue(nombres.contains("Análisis Técnico Bursátil"));
        assertFalse(nombres.contains("Macroeconomía de Coyuntura"));
    }
}
