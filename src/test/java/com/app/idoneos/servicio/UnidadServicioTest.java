package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.Programa.ProgramaServicio;
import com.app.idoneos.servicio.Unidad.UnidadServicio;

/**
 * Pruebas del ABM de unidades (CU-19 a CU-22) con los datos de la semilla: "Mercado de Capitales Argentino" tiene un
 * programa con tres unidades e inscripciones activas; "Macroeconomía de Coyuntura" tiene un programa con una cohorte sin
 * inscriptos. Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el segundo.
 */
@SpringBootTest
@Transactional
class UnidadServicioTest {

    @Autowired
    private UnidadServicio unidadServicio;

    @Autowired
    private ProgramaServicio programaServicio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Curso curso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    private Curso macro() {
        return curso("Macroeconomía de Coyuntura");
    }

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Docente sebastian() {
        return docente("sebastian.bordato@idoneos.online");
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    private Unidad agregar(Programa programa, String titulo) {
        return unidadServicio.agregarUnidad(fausto(), programa.getIdPrograma(), null, titulo, null, "Contenido de " + titulo);
    }

    private List<Integer> ordenes(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(UnidadCronograma::getNumeroOrden).toList();
    }

    // ------------------------------------------------------------------ CU-19 Buscar unidad

    @Test
    @DisplayName("CU-19: se buscan las unidades del cronograma por título, en orden de dictado")
    void busca() {
        Programa programa = mercado().getProgramaVigente();

        List<UnidadCronograma> todas = unidadServicio.buscarConFiltros(programa, null);
        assertEquals(3, todas.size());
        assertEquals(List.of(1, 2, 3), todas.stream().map(UnidadCronograma::getNumeroOrden).toList());

        assertEquals(1, unidadServicio.buscarConFiltros(programa, "RENTA FIJA").size());
        assertEquals(1, unidadServicio.buscarConFiltros(programa, "introduccion").size());
        assertTrue(unidadServicio.buscarConFiltros(programa, "zzz").isEmpty());
    }

    // ------------------------------------------------------------------ CU-20 Agregar unidad

    @Test
    @DisplayName("CU-20: una unidad nueva queda al final del cronograma con una semana de duración")
    void agregaUnidadNueva() {
        Programa programa = macro().getProgramaVigente();
        int unidades = programa.getCantidadUnidades();
        int semanas = programa.getDuracionTotalSemanas();

        Unidad nueva = unidadServicio.agregarUnidad(fausto(), programa.getIdPrograma(), null, "  Política monetaria avanzada  ", "  ",
                "  Regímenes cambiarios  ");

        assertTrue(nueva.getIdUnidad() > 0);
        assertEquals("Política monetaria avanzada", nueva.getTitulo());
        assertNull(nueva.getDescripcion());
        assertEquals("Regímenes cambiarios", nueva.getContenido());
        assertEquals(unidades + 1, programa.getCantidadUnidades());
        assertEquals(semanas + 1, programa.getDuracionTotalSemanas());
        UnidadCronograma ultima = programa.getCronogramaOrdenado().get(unidades);
        assertEquals(nueva.getIdUnidad(), ultima.getUnidad().getIdUnidad());
        assertEquals(unidades + 1, ultima.getNumeroOrden());
        assertEquals(1, ultima.getSemanasDuracion());
    }

    @Test
    @DisplayName("CU-20: se puede agregar una unidad a un programa con inscripciones: solo suma a lo que se pagó")
    void agregaEnProgramaConInscripciones() {
        Programa programa = mercado().getProgramaVigente();
        assertTrue(programa.getCantidadInscripcionesActivas() > 0);

        Unidad nueva = agregar(programa, "Unidad extra");

        assertEquals(4, programa.getCantidadUnidades());
        assertEquals(4, programa.getCronogramaOrdenado().get(3).getNumeroOrden());
        assertEquals("Unidad extra", nueva.getTitulo());
    }

    @Test
    @DisplayName("CU-20: faltan datos, superan el máximo, el título se repite, el programa no está activo o el docente no participa")
    void validaAlAgregar() {
        Programa programa = macro().getProgramaVigente();
        int id = programa.getIdPrograma();
        String repetido = programa.getCronogramaOrdenado().get(0).getUnidad().getTitulo();

        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), id, null, "", null, "Contenido"))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), id, null, "Título", null, " "))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), id, null, "x".repeat(51), null, "Contenido"))
                .contains("no puede superar los 50 caracteres"));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), id, null, "Título", "x".repeat(151), "Contenido"))
                .contains("no puede superar los 150 caracteres"));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), id, null, repetido.toUpperCase(), null, "Contenido"))
                .contains("Ya existe una unidad con ese título en el curso"));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), 999999, null, "Título", null, "Contenido"))
                .contains("El programa no se encuentra activo"));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(sebastian(), id, null, "Título", null, "Contenido"))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(null, id, null, "Título", null, "Contenido"))
                .contains("rol de docente"));
        assertNotNull(unidadServicio.agregarUnidad(fausto(), id, null, "x".repeat(50), "y".repeat(150), "Contenido"));
    }

    @Test
    @DisplayName("CU-20: se incorpora una unidad de otro programa del curso, al final y con una semana")
    void incorporaUnaUnidadExistente() {
        Programa original = macro().getProgramaVigente();
        Unidad unidad = original.getCronogramaOrdenado().get(0).getUnidad();
        Programa nuevo = programaServicio.registrarPrograma(fausto(), macro().getIdCurso(), null, "Programa 2027", null,
                "Objetivos", null, "Bibliografía");
        assertEquals(0, nuevo.getCantidadUnidades());

        unidadServicio.agregarUnidad(fausto(), nuevo.getIdPrograma(), unidad.getIdUnidad(), null, null, null);

        assertEquals(1, nuevo.getCantidadUnidades());
        assertEquals(unidad.getIdUnidad(), nuevo.getCronogramaOrdenado().get(0).getUnidad().getIdUnidad());
        assertEquals(1, nuevo.getCronogramaOrdenado().get(0).getNumeroOrden());
        // la unidad sigue en el programa anterior y ahora forma parte de los dos
        assertEquals(2, unidad.getProgramasQueLaIncluyen().size());
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), nuevo.getIdPrograma(), unidad.getIdUnidad(), null,
                null, null)).contains("ya forma parte del cronograma"));
        // una unidad de otro curso no se puede incorporar
        Unidad ajena = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getUnidad();
        assertTrue(mensaje(() -> unidadServicio.agregarUnidad(fausto(), nuevo.getIdPrograma(), ajena.getIdUnidad(), null,
                null, null)).contains("no se encuentra activa en este curso"));
    }

    // ------------------------------------------------------------------ CU-21 Modificar unidad

    @Test
    @DisplayName("CU-21: se modifica una unidad sin inscripciones activas asociadas")
    void modifica() {
        Unidad unidad = macro().getProgramaVigente().getCronogramaOrdenado().get(0).getUnidad();

        Unidad modificada = unidadServicio.modificarUnidad(unidad.getIdUnidad(), fausto(), " Nuevo título ", "",
                "Nuevo contenido");

        assertEquals("Nuevo título", modificada.getTitulo());
        assertNull(modificada.getDescripcion());
        assertEquals("Nuevo contenido", modificada.getContenido());
        assertNotNull(modificada.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-21: no se modifica con inscripciones activas, con datos vacíos, con título repetido o sin participar")
    void validaAlModificar() {
        Unidad conInscripciones = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getUnidad();
        assertTrue(conInscripciones.tieneInscripcionesActivas());
        assertTrue(mensaje(() -> unidadServicio.modificarUnidad(conInscripciones.getIdUnidad(), fausto(), "Otro", null,
                "Contenido")).contains("tiene inscripciones activas asociadas"));

        List<UnidadCronograma> cronograma = macro().getProgramaVigente().getCronogramaOrdenado();
        Unidad unidad = cronograma.get(0).getUnidad();
        int id = unidad.getIdUnidad();
        assertFalse(unidad.tieneInscripcionesActivas());
        assertTrue(mensaje(() -> unidadServicio.modificarUnidad(id, fausto(), " ", null, "Contenido"))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> unidadServicio.modificarUnidad(id, sebastian(), "Otro", null, "Contenido"))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> unidadServicio.modificarUnidad(999999, fausto(), "Otro", null, "Contenido"))
                .contains("La unidad no se encuentra activa"));
        // conservar su propio título no es un repetido
        assertNotNull(unidadServicio.modificarUnidad(id, fausto(), unidad.getTitulo(), null, "Otro contenido"));
    }

    // ------------------------------------------------------------------ CU-22 Dar de baja unidad

    @Test
    @DisplayName("CU-22: dar de baja una unidad la quita del cronograma, renumera las demás y la da de baja si no está en otro programa")
    void daDeBajaYRenumera() {
        Programa programa = macro().getProgramaVigente();
        Unidad uno = agregar(programa, "Uno");
        Unidad dos = agregar(programa, "Dos");
        int antes = programa.getCantidadUnidades();

        unidadServicio.darDeBajaUnidad(uno.getIdUnidad(), programa.getIdPrograma(), fausto());

        assertEquals(antes - 1, programa.getCantidadUnidades());
        assertTrue(uno.getBaja());
        assertFalse(dos.getBaja());
        List<Integer> ordenes = ordenes(programa);
        assertEquals(java.util.stream.IntStream.rangeClosed(1, antes - 1).boxed().toList(), ordenes);
        assertEquals(dos.getIdUnidad(), programa.getCronogramaOrdenado().get(antes - 2).getUnidad().getIdUnidad());
    }

    @Test
    @DisplayName("CU-22: si la unidad sigue en el cronograma de otro programa, solo se quita de este")
    void conservaLaUnidadSiEstaEnOtroPrograma() {
        Programa original = macro().getProgramaVigente();
        Unidad unidad = original.getCronogramaOrdenado().get(0).getUnidad();
        Programa nuevo = programaServicio.registrarPrograma(fausto(), macro().getIdCurso(), null, "Programa 2027", null,
                "Objetivos", null, "Bibliografía");
        unidadServicio.agregarUnidad(fausto(), nuevo.getIdPrograma(), unidad.getIdUnidad(), null, null, null);

        unidadServicio.darDeBajaUnidad(unidad.getIdUnidad(), nuevo.getIdPrograma(), fausto());

        assertFalse(unidad.getBaja());
        assertEquals(0, nuevo.getCantidadUnidades());
        assertTrue(unidad.perteneceAlCronograma(original));
    }

    @Test
    @DisplayName("CU-22: no se quita si el programa tiene inscripciones activas, si no pertenece al cronograma o sin participar")
    void validaAlDarDeBaja() {
        Programa conInscripciones = mercado().getProgramaVigente();
        Unidad deMercado = conInscripciones.getCronogramaOrdenado().get(0).getUnidad();
        assertTrue(mensaje(() -> unidadServicio.darDeBajaUnidad(deMercado.getIdUnidad(), conInscripciones.getIdPrograma(),
                fausto())).contains("tiene inscripciones activas"));
        assertFalse(deMercado.getBaja());

        Programa programa = macro().getProgramaVigente();
        Unidad unidad = programa.getCronogramaOrdenado().get(0).getUnidad();
        assertTrue(mensaje(() -> unidadServicio.darDeBajaUnidad(deMercado.getIdUnidad(), programa.getIdPrograma(), fausto()))
                .contains("no pertenece al cronograma de este programa"));
        assertTrue(mensaje(() -> unidadServicio.darDeBajaUnidad(unidad.getIdUnidad(), programa.getIdPrograma(), sebastian()))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> unidadServicio.darDeBajaUnidad(unidad.getIdUnidad(), 999999, fausto()))
                .contains("El programa no se encuentra activo"));
        assertTrue(mensaje(() -> unidadServicio.darDeBajaUnidad(999999, programa.getIdPrograma(), fausto()))
                .contains("La unidad no se encuentra activa"));
    }

    @Test
    @DisplayName("CU-22: la baja masiva da de baja todas las unidades o ninguna, también para el administrador")
    void daDeBajaVarias() {
        Programa programa = macro().getProgramaVigente();
        Unidad uno = agregar(programa, "Uno");
        Unidad dos = agregar(programa, "Dos");
        Unidad deMercado = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getUnidad();
        int id = programa.getIdPrograma();

        // una de las unidades no está en este programa: no se da de baja ninguna
        assertTrue(mensaje(() -> unidadServicio.darDeBajaVarias(List.of(uno.getIdUnidad(), deMercado.getIdUnidad()), id,
                fausto())).contains("no pertenece al cronograma"));
        assertFalse(uno.getBaja());
        assertTrue(mensaje(() -> unidadServicio.darDeBajaVarias(List.of(), id, fausto()))
                .contains("Debe seleccionar al menos una unidad"));
        assertTrue(mensaje(() -> unidadServicio.darDeBajaVarias(List.of(uno.getIdUnidad()), id, sebastian()))
                .contains("No participás en este curso"));

        unidadServicio.darDeBajaVarias(List.of(uno.getIdUnidad(), dos.getIdUnidad(), dos.getIdUnidad()), id, null);

        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
    }
}
