package com.app.idoneos.service;

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

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.programa.ProgramaService;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.service.temacronograma.TemaCronogramaService;

/**
 * Pruebas del cronograma (CU-23 y CU-24) con los datos de la semilla: "Macroeconomía de Coyuntura" tiene un programa con
 * tres temas, sin inscripciones; "Mercado de Capitales Argentino" tiene un programa con inscripciones activas.
 * Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el primero.
 */
@SpringBootTest
@Transactional
class CronogramaServiceTest {

    @Autowired
    private TemaCronogramaService cronogramaService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private TemaService temaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    private Curso curso(String nombre) {
        Curso curso = cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
        // la duración la define el administrador: acá se fija en 365 días para trabajar con números redondos
        if (curso.getDuracionDias() < 365) {
            curso.setDuracionDias(365);
        }
        return curso;
    }


    @Autowired
    private com.app.idoneos.repository.EstadoCohorteRepository estadoCohorteRepository;

    /**
     * La semilla deja cohortes activas en los programas. Estas pruebas miran la mecánica del cronograma sin las reglas de cohorte activa
     * (que tienen sus propias pruebas), así que dejan las cohortes del programa pendientes de contenido.
     */
    private Programa sinCohortesActivas(Programa programa) {
        var pendiente = estadoCohorteRepository.findByNombre(com.app.idoneos.model.EstadoCohorte.PENDIENTE).orElseThrow();
        programa.getCohortes().forEach(cohorte -> cohorte.setEstadoCohorte(pendiente));
        return programa;
    }

    private Programa macro() {
        return sinCohortesActivas(curso("Macroeconomía de Coyuntura").getProgramaVigente());
    }

    private Programa mercado() {
        return sinCohortesActivas(curso("Mercado de Capitales Argentino").getProgramaVigente());
    }

    @Test
    @DisplayName("Con cohortes activas, el cronograma tiene que asignar toda la duración del curso")
    void conCohorteActivaLaSumaCierraConLaDelCurso() {
        Programa programa = curso("Macroeconomía de Coyuntura").getProgramaVigente();
        assertTrue(programa.tieneCohortesActivas(), "la semilla deja cohortes activas en el programa");
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(), temas, List.of("1", "1", "1")));

        assertTrue(error.contains("No se puede modificar el cronograma del programa"), error);
        assertTrue(error.contains("asigna 3 días y el curso dura"), error);
        assertEquals(antes, semanas(programa));
    }

    @Test
    @DisplayName("Los días del cronograma suman siempre la duración del curso, también sin cohortes activas")
    void laSumaCierraSiempreConLaDelCurso() {
        Programa programa = macro();
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(), ids(programa), List.of("1", "1", "1")));

        assertTrue(error.contains("No se puede modificar el cronograma del programa"), error);
        assertTrue(error.contains("asigna 3 días y el curso dura"), error);
        assertFalse(error.contains("cohortes activas"), error);
        assertEquals(antes, semanas(programa));
    }

    @Test
    @DisplayName("Un tema al que le falta contenido no se puede incorporar al cronograma")
    void noIncorporaUnTemaSinContenido() {
        Programa original = macro();
        Programa nuevo = programaNuevo(original);
        Tema sinContenido = temaService.registrar(fausto(), original.getCurso().getIdCurso(), "Tema sin contenido", null, null);

        int primero = ids(nuevo).get(0);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), nuevo.getIdPrograma(),
                List.of(primero, sinContenido.getIdTema()), dias(nuevo, 1)));

        assertTrue(error.contains("al que le falta contenido"), error);
        assertTrue(error.contains("Completá su contenido antes"), error);
        assertEquals(3, nuevo.getCantidadTemas());
    }

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
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

    /** Reparte la duración del curso: los días indicados para los primeros temas y el resto para el último. */
    private List<String> dias(Programa programa, int... primeros) {
        List<String> dias = new java.util.ArrayList<>();
        int usados = 0;
        for (int d : primeros) {
            dias.add(String.valueOf(d));
            usados += d;
        }
        dias.add(String.valueOf(programa.getCurso().getDuracionDias() - usados));
        return dias;
    }

    private int duracion(Programa programa) {
        return programa.getCurso().getDuracionDias();
    }

    private List<Integer> semanas(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getDuracionDias).toList();
    }

    private List<Integer> ordenes(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getNumeroOrden).toList();
    }

    /** Registra un programa nuevo a partir del dado (queda como el vigente y con el cronograma completo del original: nunca hay programas incompletos). */
    private Programa programaNuevo(Programa deCurso) {
        return programaService.registrar(fausto(), deCurso.getCurso().getIdCurso(), deCurso.getIdPrograma(), "Programa 2027", null);
    }

    // ------------------------------------------------------------------ CU-24 Modificar cronograma

    @Test
    @DisplayName("CU-24: se reordenan los temas y se cambian sus semanas, y queda la fecha de modificación del programa")
    void reordenaYCambiaLasSemanas() {
        Programa programa = macro();
        List<Integer> original = ids(programa);
        assertEquals(3, original.size());
        List<Integer> nuevoOrden = List.of(original.get(2), original.get(0), original.get(1));

        cronogramaService.modificar(fausto(), programa.getIdPrograma(), nuevoOrden, dias(programa, 2, 4));

        assertEquals(nuevoOrden, ids(programa));
        assertEquals(List.of(2, 4, duracion(programa) - 6), semanas(programa));
        assertEquals(List.of(1, 2, 3), ordenes(programa));
        assertEquals(duracion(programa), programa.getDuracionTotalDias());
        assertNotNull(programa.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar el máximo de días del parámetro del sistema (365 por defecto)")
    void noSuperaElMaximoDeSemanas() {
        Programa programa = macro();
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(),
                temas, List.of("360", "4", "2")));
        assertTrue(error.contains("Máximo: 365 días"), error);
        assertEquals(antes, semanas(programa)); // nada se modificó

        // justo en el máximo, se guarda (con cohortes cuyos días de acceso alcanzan)
        programa.getCohortesActivas().forEach(c -> c.setAccesoDias(365));
        cronogramaService.modificar(fausto(), programa.getIdPrograma(), temas, List.of("360", "3", "2"));
        assertEquals(365, programa.getDuracionTotalDias());
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar la duración máxima del curso")
    void noSuperaLaDuracionMaximaDelCurso() {
        Programa programa = macro();
        programa.getCurso().setDuracionDias(100);
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(),
                temas, List.of("90", "10", "1")));
        assertTrue(error.contains("Máximo: 100 días"), error);
        assertEquals(antes, semanas(programa)); // nada se modificó
    }

    @Test
    @DisplayName("CU-24: el cronograma no puede superar las semanas de acceso de una cohorte del programa")
    void noSuperaLasSemanasDeAccesoDeLasCohortes() {
        Programa programa = curso("Macroeconomía de Coyuntura").getProgramaVigente();
        List<Integer> temas = ids(programa);
        List<Integer> antes = semanas(programa);
        int minimoDeAcceso = programa.getCohortesActivas().stream().mapToInt(c -> c.getAccesoDias()).min().orElseThrow();
        assertTrue(minimoDeAcceso < duracion(programa), "la semilla deja cohortes con menos días de acceso que la duración del curso");

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(), temas,
                dias(programa, 1, 1)));
        assertTrue(error.startsWith("Error! La cohorte «"), error);
        assertTrue(error.endsWith("Modificá la cohorte o acortá el cronograma."), error);
        assertEquals(antes, semanas(programa)); // nada se modificó
    }

    @Test
    @DisplayName("CU-24: un programa nuevo conserva dos temas del original, con los días indicados, y el original no cambia")
    void armaUnProgramaConTemasDeOtro() {
        Programa original = macro();
        List<Integer> temas = ids(original);
        Programa nuevo = programaNuevo(original);

        cronogramaService.modificar(fausto(), nuevo.getIdPrograma(), List.of(temas.get(0), temas.get(1)), dias(nuevo, 3));

        assertEquals(List.of(temas.get(0), temas.get(1)), ids(nuevo));
        assertEquals(List.of(3, duracion(nuevo) - 3), semanas(nuevo));
        assertEquals(List.of(1, 2), ordenes(nuevo));
        assertEquals(3, original.getCantidadTemas());
    }

    @Test
    @DisplayName("CU-24: un programa nunca tiene menos temas que el mínimo del sistema")
    void noBajaDelMinimoDeTemas() {
        Programa programa = macro();
        int minimo = programaService.buscarMinimoDeContenido();
        List<Integer> uno = ids(programa).subList(0, Math.max(0, minimo - 1));
        List<String> dias = uno.isEmpty() ? List.of() : dias(programa);

        String error = mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(), uno, dias));

        assertTrue(error.contains("hacen falta al menos " + minimo), error);
    }

    @Test
    @DisplayName("CU-24: el tema que se quita sigue activa si está en otro programa, y se da de baja si no")
    void quitaTemas() {
        Programa original = macro();
        List<Integer> temas = ids(original);
        Programa nuevo = programaNuevo(original);

        // se quita la tercera del programa nuevo: sigue en el original
        cronogramaService.modificar(fausto(), nuevo.getIdPrograma(), List.of(temas.get(0), temas.get(1)), dias(nuevo, 100));
        Tema compartida = original.getCronogramaOrdenado().get(2).getTema();
        assertFalse(compartida.getBaja());
        assertEquals(2, nuevo.getCantidadTemas());

        // se quita la segunda del programa original, donde estaba sola: el tema sigue siendo del curso
        Tema sola = original.getCronogramaOrdenado().get(1).getTema();
        cronogramaService.modificar(fausto(), original.getIdPrograma(),
                List.of(temas.get(0), temas.get(2)), dias(original, 1));
        assertFalse(sola.getBaja());
        assertEquals(List.of(temas.get(0), temas.get(2)), ids(original));
        assertEquals(List.of(1, 2), ordenes(original));
    }

    @Test
    @DisplayName("CU-24: se quita y se reordena en una sola operación sin chocar con los números de orden")
    void todoJunto() {
        Programa original = macro();
        List<Integer> temas = ids(original);
        Programa nuevo = programaNuevo(original);
        cronogramaService.modificar(fausto(), nuevo.getIdPrograma(), temas, dias(nuevo, 1, 1));

        // en el programa nuevo: se quita la del medio y se invierten las otras
        cronogramaService.modificar(fausto(), nuevo.getIdPrograma(), List.of(temas.get(2), temas.get(0)), dias(nuevo, 5));

        assertEquals(List.of(temas.get(2), temas.get(0)), ids(nuevo));
        assertEquals(List.of(5, duracion(nuevo) - 5), semanas(nuevo));
        assertEquals(List.of(1, 2), ordenes(nuevo));
        // el tema del medio sigue en el programa original: no se dio de baja
        assertFalse(original.getCronogramaOrdenado().stream().anyMatch(c -> c.getTema().getBaja()));
    }

    @Test
    @DisplayName("CU-24: no se modifica un programa con inscripciones activas")
    void conInscripciones() {
        Programa programa = mercado();
        assertTrue(programa.getCantidadInscripcionesActivas() > 0);

        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), programa.getIdPrograma(),
                ids(programa), List.of("1", "1", "1"))).contains("inscripciones activas asociadas"));
    }

    @Test
    @DisplayName("CU-24: cada duración debe ser un entero mayor a cero, y los datos recibidos deben ser coherentes")
    void validaLosDatos() {
        Programa programa = macro();
        int id = programa.getIdPrograma();
        List<Integer> temas = ids(programa);
        String duracion = "La duración de cada tema no es válida. Formato válido: número entero de días mayor a cero";

        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, temas, List.of("1", "0", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, temas, List.of("1", "-2", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, temas, List.of("1", "2,5", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, temas, List.of("1", "abc", "1")))
                .contains(duracion));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, temas, List.of("1", "1")))
                .contains("Los datos del cronograma no son válidos"));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id,
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

        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id,
                List.of(temas.get(0), deOtroCurso), dias(programa, 1))).contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), id, List.of(temas.get(0), 999999), dias(programa, 1)))
                .contains("no se encuentra activa en este curso"));
        assertTrue(mensaje(() -> cronogramaService.modificar(docente("sebastian.bordato@idoneos.online"), id,
                temas, List.of("1", "1", "1"))).contains("No participás en este curso"));
        assertTrue(mensaje(() -> cronogramaService.modificar(null, id, temas, List.of("1", "1", "1")))
                .contains("rol de docente"));
        assertTrue(mensaje(() -> cronogramaService.modificar(fausto(), 999999, temas, List.of("1", "1", "1")))
                .contains("El programa no se encuentra activo"));
    }
}
