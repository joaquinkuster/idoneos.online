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
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.Programa.ProgramaServicio;
import com.app.idoneos.servicio.Tema.TemaServicio;
import com.app.idoneos.servicio.TemaCronograma.TemaCronogramaServicio;

/**
 * Pruebas del cronograma (CU-23 y CU-24) con los datos de la semilla: "Macroeconomía de Coyuntura" tiene un programa con
 * tres temas, sin inscripciones; "Mercado de Capitales Argentino" tiene un programa con inscripciones activas.
 * Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el primero.
 */
@SpringBootTest
@Transactional
class CronogramaServicioTest {

    @Autowired
    private TemaCronogramaServicio cronogramaServicio;

    @Autowired
    private ProgramaServicio programaServicio;

    @Autowired
    private TemaServicio temaServicio;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Curso curso(String nombre) {
        Curso curso = cursoRepositorio.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
        // la semilla deja los cronogramas completos: se amplía la duración para poder agregar temas
        if (curso.getDuracionDias() < 365) {
            curso.setDuracionDias(365);
        }
        return curso;
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
        return programa.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList();
    }

    private List<Integer> semanas(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getDuracionDias).toList();
    }

    private List<Integer> ordenes(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getNumeroOrden).toList();
    }

    /** Registra un programa nuevo en el curso del programa dado (queda como el vigente, sin temas). */
    private Programa programaNuevo(Programa deCurso) {
        return programaServicio.registrarPrograma(fausto(), deCurso.getCurso().getIdCurso(), null, "Programa 2027", null);
    }

    // ------------------------------------------------------------------ CU-24 Modificar cronograma

    @Test
    @DisplayName("CU-24: se reordenan los temas y se cambian sus semanas, y queda la fecha de modificación del programa")
    void reordenaYCambiaLasSemanas() {
        Programa programa = macro();
        List<Integer> original = ids(programa);
        assertEquals(3, original.size());
        List<Integer> nuevoOrden = List.of(original.get(2), original.get(0), original.get(1));

        cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), nuevoOrden, List.of("2", "4", "1"));

        assertEquals(nuevoOrden, ids(programa));
        assertEquals(List.of(2, 4, 1), semanas(programa));
        assertEquals(List.of(1, 2, 3), ordenes(programa));
        assertEquals(7, programa.getDuracionTotalDias());
        assertNotNull(programa.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar el máximo de días del parámetro del sistema (365 por defecto)")
    void noSuperaElMaximoDeSemanas() {
        Programa programa = macro();
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(),
                temas, List.of("360", "4", "2")));
        assertTrue(error.contains("Máximo: 365 días"), error);
        assertEquals(antes, semanas(programa)); // nada se modificó

        // justo en el máximo, se guarda (con cohortes cuyos días de acceso alcanzan)
        programa.getCohortesActivas().forEach(c -> c.setAccesoDias(365));
        cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), temas, List.of("360", "3", "2"));
        assertEquals(365, programa.getDuracionTotalDias());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar la duración máxima del curso")
    void noSuperaLaDuracionMaximaDelCurso() {
        Programa programa = macro();
        programa.getCurso().setDuracionDias(100);
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(),
                temas, List.of("90", "10", "1")));
        assertTrue(error.contains("Máximo: 100 días"), error);
        assertEquals(antes, semanas(programa)); // nada se modificó
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar las semanas de acceso de una cohorte del programa")
    void noSuperaLasSemanasDeAccesoDeLasCohortes() {
        Programa programa = macro();
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);
        int minimoDeAcceso = programa.getCohortesActivas().stream().mapToInt(c -> c.getAccesoDias()).min().orElseThrow();

        String error = mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), programa.getIdPrograma(), temas,
                List.of(String.valueOf(minimoDeAcceso), "1", "1")));
        assertTrue(error.startsWith("Error! La cohorte «"), error);
        assertTrue(error.endsWith("Modificá la cohorte o acortá el cronograma."), error);
        assertEquals(antes, semanas(programa)); // nada se modificó
    }

    @Test
    @DisplayName("CU-24: se incorpora un tema de otro programa del curso, con las semanas indicadas")
    void incorporaUnaTemaDeOtroPrograma() {
        Programa original = macro();
        int tema = ids(original).get(0);
        Programa nuevo = programaNuevo(original);

        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(tema), List.of("3"));

        assertEquals(List.of(tema), ids(nuevo));
        assertEquals(List.of(3), semanas(nuevo));
        assertEquals(List.of(1), ordenes(nuevo));
        assertEquals(3, original.getCantidadTemas());
    }

    @Test
    @DisplayName("CU-24: el tema que se quita sigue activa si está en otro programa, y se da de baja si no")
    void quitaTemas() {
        Programa original = macro();
        List<Integer> temas = ids(original);
        Programa nuevo = programaNuevo(original);
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(temas.get(0)), List.of("1"));

        // se quita del programa nuevo: sigue en el original
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), List.of(), List.of());
        Tema compartida = original.getCronogramaOrdenado().get(0).getTema();
        assertFalse(compartida.getBaja());
        assertEquals(0, nuevo.getCantidadTemas());

        // se quita la segunda del programa original, donde estaba sola: el tema sigue siendo del curso
        Tema sola = original.getCronogramaOrdenado().get(1).getTema();
        cronogramaServicio.modificarCronograma(fausto(), original.getIdPrograma(),
                List.of(temas.get(0), temas.get(2)), List.of("1", "1"));
        assertFalse(sola.getBaja());
        assertEquals(List.of(temas.get(0), temas.get(2)), ids(original));
        assertEquals(List.of(1, 2), ordenes(original));
    }

    @Test
    @DisplayName("CU-24: se quita, se reordena y se agrega en una sola operación sin chocar con los números de orden")
    void todoJunto() {
        Programa original = macro();
        List<Integer> temas = ids(original);
        Programa nuevo = programaNuevo(original);
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(), temas, List.of("1", "1", "1"));
        // un tema más, que queda en el programa original
        Tema extra = temaServicio.agregarTema(fausto(), original.getIdPrograma(), null, "Tema extra", null,
                "Contenido");

        // en el programa nuevo: se quita la del medio, se invierten las otras y se agrega la extra
        cronogramaServicio.modificarCronograma(fausto(), nuevo.getIdPrograma(),
                List.of(temas.get(2), extra.getIdTema(), temas.get(0)), List.of("5", "2", "3"));

        assertEquals(List.of(temas.get(2), extra.getIdTema(), temas.get(0)), ids(nuevo));
        assertEquals(List.of(5, 2, 3), semanas(nuevo));
        assertEquals(List.of(1, 2, 3), ordenes(nuevo));
        // el tema del medio sigue en el programa original: no se dio de baja
        assertFalse(original.getCronogramaOrdenado().stream().anyMatch(c -> c.getTema().getBaja()));
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
        List<Integer> temas = ids(programa);
        String duracion = "La duración de cada tema no es válida. Formato válido: número entero de días mayor a cero";

        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, temas, List.of("1", "0", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, temas, List.of("1", "-2", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, temas, List.of("1", "2,5", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, temas, List.of("1", "abc", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, temas, List.of("1", "1")))
                .contains("Los datos del cronograma no son válidos"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id,
                List.of(temas.get(0), temas.get(0)), List.of("1", "1")))
                .contains("Los datos del cronograma no son válidos"));
        // nada cambió
        assertEquals(temas, ids(programa));
        assertEquals(List.of(1, 2, 3), ordenes(programa));
    }

    @Test
    @DisplayName("CU-24: no se incorpora un tema de otro curso ni de otro lado, y se exige participar en el curso")
    void validaLasTemasYElDocente() {
        Programa programa = macro();
        int id = programa.getIdPrograma();
        List<Integer> temas = ids(programa);
        int deOtroCurso = ids(mercado()).get(0);

        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id,
                List.of(temas.get(0), deOtroCurso), List.of("1", "1"))).contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), id, List.of(999999), List.of("1")))
                .contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(docente("sebastian.bordato@idoneos.online"), id,
                temas, List.of("1", "1", "1"))).contains("No participás en este curso"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(null, id, temas, List.of("1", "1", "1")))
                .contains("rol de docente"));
        assertTrue(mensaje(() -> cronogramaServicio.modificarCronograma(fausto(), 999999, temas, List.of("1", "1", "1")))
                .contains("El programa no se encuentra activo"));
    }
}
