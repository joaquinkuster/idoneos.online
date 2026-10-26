package com.app.idoneos.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.app.idoneos.servicio.UnidadCronograma.UnidadCronogramaServicio;

/**
 * Pruebas del cronograma (CU-23 y CU-24) con los datos de la semilla: "Macroeconomía de Coyuntura" tiene un programa con
 * tres unidades, sin inscripciones; "Mercado de Capitales Argentino" tiene un programa con inscripciones activas.
 * Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el primero.
 */
@SpringBootTest
@Transactional
class CronogramaServicioTest {

    @Autowired
    private UnidadCronogramaServicio cronogramaServicio;

    @Autowired
    private ProgramaServicio programaServicio;

    @Autowired
    private UnidadServicio unidadServicio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Curso curso(String nombre) {
        return cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Programa macro() {
        return curso("Macroeconomía de Coyuntura").getProgramaVigente();
    }

    private Programa mercado() {
        return curso("Mercado de Capitales Argentino").getProgramaVigente();
    }

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    private List<Integer> ids(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(c -> c.getUnidad().getIdUnidad()).toList();
    }

    private List<Integer> semanas(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(UnidadCronograma::getSemanasDuracion).toList();
    }

    private List<Integer> ordenes(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(UnidadCronograma::getNumeroOrden).toList();
    }

    /** Registra un programa nuevo en el curso del programa dado (queda como el vigente, sin unidades). */
    private Programa programaNuevo(Programa deCurso) {
        return programaServicio.registrarPrograma(fausto(), deCurso.getCurso().getIdCurso(), null, "Programa 2027", null,
                "Objetivos", null, "Bibliografía");
    }

    // ------------------------------------------------------------------ CU-24 Modificar cronograma

    @Test
    @DisplayName("CU-24: se reordenan las unidades y se cambian sus semanas, y queda la fecha de modificación del programa")
    void reordenaYCambiaLasSemanas() {
        Programa programa = macro();
        List<Integer> original = ids(programa);
        assertEquals(3, original.size());
        List<Integer> nuevoOrden = List.of(original.get(2), original.get(0), original.get(1));

        cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), nuevoOrden, List.of("2", "4", "1"));

        assertEquals(nuevoOrden, ids(programa));
        assertEquals(List.of(2, 4, 1), semanas(programa));
        assertEquals(List.of(1, 2, 3), ordenes(programa));
        assertEquals(7, programa.getDuracionTotalSemanas());
        assertNotNull(programa.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar el máximo de semanas del parámetro del sistema (52 por defecto)")
    void noSuperaElMaximoDeSemanas() {
        Programa programa = macro();
        List<Integer> unidades = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(),
                unidades, List.of("50", "2", "1")));
        assertTrue(error.contains("Máximo: 52 semanas"), error);
        assertEquals(antes, semanas(programa)); // nada se modificó

        // justo en el máximo, se guarda (con cohortes cuyas semanas de acceso alcanzan)
        programa.getCohortesActivas().forEach(c -> c.setSemanasAcceso(52));
        cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), unidades, List.of("50", "1", "1"));
        assertEquals(52, programa.getDuracionTotalSemanas());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar las semanas de acceso de una cohorte del programa")
    void noSuperaLasSemanasDeAccesoDeLasCohortes() {
        Programa programa = macro();
        List<Integer> unidades = ids(programa);
        List<Integer> antes = semanas(programa);
        int minimoDeAcceso = programa.getCohortesActivas().stream().mapToInt(c -> c.getSemanasAcceso()).min().orElseThrow();

        String error = mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), unidades,
                List.of(String.valueOf(minimoDeAcceso), "1", "1")));
        assertTrue(error.startsWith("Error! La cohorte «"), error);
        assertTrue(error.endsWith("Modificá la cohorte o acortá el cronograma."), error);
        assertEquals(antes, semanas(programa)); // nada se modificó
    }

    @Test
    @DisplayName("CU-24: se incorpora una unidad de otro programa del curso, con las semanas indicadas")
    void incorporaUnaUnidadDeOtroPrograma() {
        Programa original = macro();
        int unidad = ids(original).get(0);
        Programa nuevo = programaNuevo(original);

        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(unidad), List.of("3"));

        assertEquals(List.of(unidad), ids(nuevo));
        assertEquals(List.of(3), semanas(nuevo));
        assertEquals(List.of(1), ordenes(nuevo));
        assertEquals(3, original.getCantidadUnidades());
    }

    @Test
    @DisplayName("CU-24: la unidad que se quita sigue activa si está en otro programa, y se da de baja si no")
    void quitaUnidades() {
        Programa original = macro();
        List<Integer> unidades = ids(original);
        Programa nuevo = programaNuevo(original);
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(unidades.get(0)), List.of("1"));

        // se quita del programa nuevo: sigue en el original
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(), List.of());
        Unidad compartida = original.getCronogramaOrdenado().get(0).getUnidad();
        assertFalse(compartida.getBaja());
        assertEquals(0, nuevo.getCantidadUnidades());

        // se quita la segunda del programa original, donde estaba sola: queda dada de baja
        Unidad sola = original.getCronogramaOrdenado().get(1).getUnidad();
        cronogramaServicio.modificarCronograma(fausto(), original.getIdPrograma(),
                List.of(unidades.get(0), unidades.get(2)), List.of("1", "1"));
        assertTrue(sola.getBaja());
        assertEquals(List.of(unidades.get(0), unidades.get(2)), ids(original));
        assertEquals(List.of(1, 2), ordenes(original));
    }

    @Test
    @DisplayName("CU-24: se quita, se reordena y se agrega en una sola operación sin chocar con los números de orden")
    void todoJunto() {
        Programa original = macro();
        List<Integer> unidades = ids(original);
        Programa nuevo = programaNuevo(original);
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), unidades, List.of("1", "1", "1"));
        // una unidad más, que queda en el programa original
        Unidad extra = unidadServicio.agregarUnidad(fausto(), original.getIdPrograma(), null, "Unidad extra", null,
                "Contenido");

        // en el programa nuevo: se quita la del medio, se invierten las otras y se agrega la extra
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(),
                List.of(unidades.get(2), extra.getIdUnidad(), unidades.get(0)), List.of("5", "2", "3"));

        assertEquals(List.of(unidades.get(2), extra.getIdUnidad(), unidades.get(0)), ids(nuevo));
        assertEquals(List.of(5, 2, 3), semanas(nuevo));
        assertEquals(List.of(1, 2, 3), ordenes(nuevo));
        // la unidad del medio sigue en el programa original: no se dio de baja
        assertFalse(original.getCronogramaOrdenado().stream().anyMatch(c -> c.getUnidad().getBaja()));
    }

    @Test
    @DisplayName("CU-24: no se modifica un programa con inscripciones activas")
    void conInscripciones() {
        Programa programa = mercado();
        assertTrue(programa.getCantidadInscripcionesActivas() > 0);

        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(),
                ids(programa), List.of("1", "1", "1"))).contains("tiene inscripciones activas"));
    }

    @Test
    @DisplayName("CU-24: cada duración debe ser un entero mayor a cero, y los datos recibidos deben ser coherentes")
    void validaLosDatos() {
        Programa programa = macro();
        int id = programa.getIdPrograma();
        List<Integer> unidades = ids(programa);
        String duracion = "La duración de cada unidad no es válida. Formato válido: número entero de semanas mayor a cero";

        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, unidades, List.of("1", "0", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, unidades, List.of("1", "-2", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, unidades, List.of("1", "2,5", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, unidades, List.of("1", "abc", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, unidades, List.of("1", "1")))
                .contains("Los datos del cronograma no son válidos"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id,
                List.of(unidades.get(0), unidades.get(0)), List.of("1", "1")))
                .contains("Los datos del cronograma no son válidos"));
        // nada cambió
        assertEquals(unidades, ids(programa));
        assertEquals(List.of(1, 2, 3), ordenes(programa));
    }

    @Test
    @DisplayName("CU-24: no se incorpora una unidad de otro curso ni de otro lado, y se exige participar en el curso")
    void validaLasUnidadesYElDocente() {
        Programa programa = macro();
        int id = programa.getIdPrograma();
        List<Integer> unidades = ids(programa);
        int deOtroCurso = ids(mercado()).get(0);

        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id,
                List.of(unidades.get(0), deOtroCurso), List.of("1", "1"))).contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, List.of(999999), List.of("1")))
                .contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(docente("sebastian.bordato@idoneos.online"), id,
                unidades, List.of("1", "1", "1"))).contains("No participás en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(null, id, unidades, List.of("1", "1", "1")))
                .contains("rol de docente"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), 999999, unidades, List.of("1", "1", "1")))
                .contains("El programa no se encuentra activo"));
    }
}
