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
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.Programa.ProgramaServicio;
import com.app.idoneos.servicio.Tema.TemaServicio;

/**
 * Pruebas del ABM de temas (CU-19 a CU-22) con los datos de la semilla: "Mercado de Capitales Argentino" tiene un
 * programa con tres temas e inscripciones activas; "Macroeconomía de Coyuntura" tiene un programa con una cohorte sin
 * inscriptos. Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el segundo.
 */
@SpringBootTest
@Transactional
class TemaServicioTest {

    @Autowired
    private TemaServicio temaServicio;

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

    private Tema agregar(Programa programa, String titulo) {
        return temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, titulo, null, "Contenido de " + titulo);
    }

    private List<Integer> ordenes(Programa programa) {
        return programa.getCronogramaOrdenado().stream().map(TemaCronograma::getNumeroOrden).toList();
    }

    // ------------------------------------------------------------------ CU-19 Buscar tema

    @Test
    @DisplayName("CU-19: se buscan los temas del cronograma por título, en orden de dictado")
    void busca() {
        Programa programa = mercado().getProgramaVigente();

        List<TemaCronograma> todas = temaServicio.buscarConFiltros(programa, null);
        assertEquals(3, todas.size());
        assertEquals(List.of(1, 2, 3), todas.stream().map(TemaCronograma::getNumeroOrden).toList());

        assertEquals(1, temaServicio.buscarConFiltros(programa, "RENTA FIJA").size());
        assertEquals(1, temaServicio.buscarConFiltros(programa, "introduccion").size());
        assertTrue(temaServicio.buscarConFiltros(programa, "zzz").isEmpty());
    }

    // ------------------------------------------------------------------ CU-20 Agregar tema

    @Test
    @DisplayName("CU-20: al agregar un tema se puede indicar cuántos días dura en el cronograma")
    void agregaTemaConDuracion() {
        Programa programa = macro().getProgramaVigente();
        int dias = programa.getDuracionTotalDias();

        Tema nueva = temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, "Tema con duración", null,
                "Contenido", null, 14);

        TemaCronograma ultima = programa.getCronogramaOrdenado().get(programa.getCantidadTemas() - 1);
        assertEquals(nueva.getIdTema(), ultima.getTema().getIdTema());
        assertEquals(14, ultima.getDuracionDias());
        assertEquals(dias + 14, programa.getDuracionTotalDias());
    }

    @Test
    @DisplayName("CU-20: la duración del tema debe ser mayor a cero y entrar en la duración máxima del curso y en los días de acceso")
    void validaLaDuracionAlAgregar() {
        Programa programa = macro().getProgramaVigente();
        int id = programa.getIdPrograma();

        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "Tema uno", null, "Contenido", null, 0))
                .contains("La duración en el cronograma no es válida"));

        // la cohorte del programa tiene menos días de acceso que el cronograma con esta duración
        int acceso = programa.getCohortesActivas().get(0).getAccesoDias();
        int demasiado = acceso - programa.getDuracionTotalDias() + 1;
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "Tema dos", null, "Contenido", null, demasiado))
                .contains("de acceso de la cohorte"));

        // la duración máxima del curso deja menos días que los pedidos
        programa.getCurso().setDuracionMaximaDias(programa.getDuracionTotalDias() + 5);
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "Tema tres", null, "Contenido", null, 6))
                .contains("puede durar hasta 5 días"));
        assertNotNull(temaServicio.agregarTema(fausto(), id, null, "Tema cuatro", null, "Contenido", null, 5));
    }

    @Test
    @DisplayName("CU-20: con el cronograma lleno no se agregan más temas y el error indica cómo conseguir más tiempo")
    void noAgregaConElCronogramaLleno() {
        Programa programa = macro().getProgramaVigente();
        programa.getCurso().setDuracionMaximaDias(programa.getDuracionTotalDias());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> agregar(programa, "Sin lugar"));

        assertTrue(error.getMessage().startsWith("Error! La duración del cronograma ya alcanzó la duración máxima del curso."));
        assertTrue(error.getMessage().contains("modificá el cronograma o comunicate con un administrador"));
    }

    @Test
    @DisplayName("CU-21: se puede cambiar la duración de un tema en el cronograma de un programa")
    void modificaLaDuracion() {
        Programa programa = macro().getProgramaVigente();
        TemaCronograma primero = programa.getCronogramaOrdenado().get(0);
        Tema tema = primero.getTema();
        int total = programa.getDuracionTotalDias();
        int nueva = primero.getDuracionDias() + 7;

        temaServicio.modificarTema(tema.getIdTema(), programa.getIdPrograma(), fausto(), tema.getTitulo(),
                tema.getDescripcion(), tema.getContenido(), null, nueva);

        assertEquals(nueva, primero.getDuracionDias());
        assertEquals(total + 7, programa.getDuracionTotalDias());
        assertTrue(mensaje(() -> temaServicio.modificarTema(tema.getIdTema(), programa.getIdPrograma(), fausto(),
                tema.getTitulo(), tema.getDescripcion(), tema.getContenido(), null, 0)).contains("La duración en el cronograma no es válida"));
        // un tema que no está en el cronograma de ese programa no se puede modificar desde él
        Tema ajeno = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();
        assertTrue(mensaje(() -> temaServicio.modificarTema(ajeno.getIdTema(), programa.getIdPrograma(), fausto(),
                ajeno.getTitulo(), ajeno.getDescripcion(), ajeno.getContenido(), null, 7)).length() > 0);
    }

    @Test
    @DisplayName("CU-20: un tema nueva queda al final del cronograma con una semana de duración")
    void agregaTemaNueva() {
        Programa programa = macro().getProgramaVigente();
        int temas = programa.getCantidadTemas();
        int dias = programa.getDuracionTotalDias();

        Tema nueva = temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, "  Política monetaria avanzada  ", "  ",
                "  Regímenes cambiarios  ");

        assertTrue(nueva.getIdTema() > 0);
        assertEquals("Política monetaria avanzada", nueva.getTitulo());
        assertNull(nueva.getDescripcion());
        assertEquals("Regímenes cambiarios", nueva.getContenido());
        assertEquals(temas + 1, programa.getCantidadTemas());
        assertEquals(dias + 7, programa.getDuracionTotalDias());
        TemaCronograma ultima = programa.getCronogramaOrdenado().get(temas);
        assertEquals(nueva.getIdTema(), ultima.getTema().getIdTema());
        assertEquals(temas + 1, ultima.getNumeroOrden());
        assertEquals(7, ultima.getDuracionDias());
    }

    @Test
    @DisplayName("CU-20: se puede agregar un tema a un programa con inscripciones: solo suma a lo que se pagó")
    void agregaEnProgramaConInscripciones() {
        Programa programa = mercado().getProgramaVigente();
        assertTrue(programa.getCantidadInscripcionesActivas() > 0);

        Tema nueva = agregar(programa, "Tema extra");

        assertEquals(4, programa.getCantidadTemas());
        assertEquals(4, programa.getCronogramaOrdenado().get(3).getNumeroOrden());
        assertEquals("Tema extra", nueva.getTitulo());
    }

    @Test
    @DisplayName("CU-20: faltan datos, superan el máximo, el título se repite, el programa no está activo o el docente no participa")
    void validaAlAgregar() {
        Programa programa = macro().getProgramaVigente();
        int id = programa.getIdPrograma();
        String repetido = programa.getCronogramaOrdenado().get(0).getTema().getTitulo();

        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "", null, "Contenido"))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "Título", null, " "))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "x".repeat(51), null, "Contenido"))
                .contains("Máximo: 50 caracteres"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, "Título", "x".repeat(151), "Contenido"))
                .contains("Máximo: 150 caracteres"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), id, null, repetido.toUpperCase(), null, "Contenido"))
                .contains("Ya existe un tema con ese título en el curso"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), 999999, null, "Título", null, "Contenido"))
                .contains("El programa no se encuentra activo"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(sebastian(), id, null, "Título", null, "Contenido"))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(null, id, null, "Título", null, "Contenido"))
                .contains("rol de docente"));
        assertNotNull(temaServicio.agregarTema(fausto(), id, null, "x".repeat(50), "y".repeat(150), "Contenido"));
    }

    @Test
    @DisplayName("CU-20: se incorpora un tema de otro programa del curso, al final y con una semana")
    void incorporaUnaTemaExistente() {
        Programa original = macro().getProgramaVigente();
        Tema tema = original.getCronogramaOrdenado().get(0).getTema();
        Programa nuevo = programaServicio.registrarPrograma(fausto(), macro().getIdCurso(), null, "Programa 2027", null,
                "Objetivos");
        assertEquals(0, nuevo.getCantidadTemas());

        temaServicio.agregarTema(fausto(), nuevo.getIdPrograma(), tema.getIdTema(), null, null, null);

        assertEquals(1, nuevo.getCantidadTemas());
        assertEquals(tema.getIdTema(), nuevo.getCronogramaOrdenado().get(0).getTema().getIdTema());
        assertEquals(1, nuevo.getCronogramaOrdenado().get(0).getNumeroOrden());
        // el tema sigue en el programa anterior y ahora forma parte de los dos
        assertEquals(2, tema.getProgramasQueLaIncluyen().size());
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), nuevo.getIdPrograma(), tema.getIdTema(), null,
                null, null)).contains("ya forma parte del cronograma"));
        // un tema de otro curso no se puede incorporar
        Tema ajena = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), nuevo.getIdPrograma(), ajena.getIdTema(), null,
                null, null)).contains("no se encuentra activa en este curso"));
    }

    // ------------------------------------------------------------------ CU-21 Modificar tema

    @Test
    @DisplayName("CU-21: se modifica un tema sin inscripciones activas asociadas")
    void modifica() {
        Tema tema = macro().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();

        Tema modificada = temaServicio.modificarTema(tema.getIdTema(), fausto(), " Nuevo título ", "",
                "Nuevo contenido");

        assertEquals("Nuevo título", modificada.getTitulo());
        assertNull(modificada.getDescripcion());
        assertEquals("Nuevo contenido", modificada.getContenido());
        assertNotNull(modificada.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-21: no se modifica con inscripciones activas, con datos vacíos, con título repetido o sin participar")
    void validaAlModificar() {
        Tema conInscripciones = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();
        assertTrue(conInscripciones.tieneInscripcionesActivas());
        assertTrue(mensaje(() -> temaServicio.modificarTema(conInscripciones.getIdTema(), fausto(), "Otro", null,
                "Contenido")).contains("tiene inscripciones activas asociadas"));

        List<TemaCronograma> cronograma = macro().getProgramaVigente().getCronogramaOrdenado();
        Tema tema = cronograma.get(0).getTema();
        int id = tema.getIdTema();
        assertFalse(tema.tieneInscripcionesActivas());
        assertTrue(mensaje(() -> temaServicio.modificarTema(id, fausto(), " ", null, "Contenido"))
                .contains("Debe completar el título y el contenido."));
        assertTrue(mensaje(() -> temaServicio.modificarTema(id, sebastian(), "Otro", null, "Contenido"))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> temaServicio.modificarTema(999999, fausto(), "Otro", null, "Contenido"))
                .contains("El tema no se encuentra activa"));
        // conservar su propio título no es un repetido
        assertNotNull(temaServicio.modificarTema(id, fausto(), tema.getTitulo(), null, "Otro contenido"));
    }

    // ------------------------------------------------------------------ CU-22 Dar de baja tema

    @Test
    @DisplayName("CU-22: dar de baja un tema la quita del cronograma, renumera las demás y la da de baja si no está en otro programa")
    void daDeBajaYRenumera() {
        Programa programa = macro().getProgramaVigente();
        Tema uno = agregar(programa, "Uno");
        Tema dos = agregar(programa, "Dos");
        int antes = programa.getCantidadTemas();

        temaServicio.darDeBajaTema(uno.getIdTema(), programa.getIdPrograma(), fausto());

        assertEquals(antes - 1, programa.getCantidadTemas());
        assertTrue(uno.getBaja());
        assertFalse(dos.getBaja());
        List<Integer> ordenes = ordenes(programa);
        assertEquals(java.util.stream.IntStream.rangeClosed(1, antes - 1).boxed().toList(), ordenes);
        assertEquals(dos.getIdTema(), programa.getCronogramaOrdenado().get(antes - 2).getTema().getIdTema());
    }

    @Test
    @DisplayName("CU-22: si el tema sigue en el cronograma de otro programa, solo se quita de este")
    void conservaLaTemaSiEstaEnOtroPrograma() {
        Programa original = macro().getProgramaVigente();
        Tema tema = original.getCronogramaOrdenado().get(0).getTema();
        Programa nuevo = programaServicio.registrarPrograma(fausto(), macro().getIdCurso(), null, "Programa 2027", null,
                "Objetivos");
        temaServicio.agregarTema(fausto(), nuevo.getIdPrograma(), tema.getIdTema(), null, null, null);

        temaServicio.darDeBajaTema(tema.getIdTema(), nuevo.getIdPrograma(), fausto());

        assertFalse(tema.getBaja());
        assertEquals(0, nuevo.getCantidadTemas());
        assertTrue(tema.perteneceAlCronograma(original));
    }

    @Test
    @DisplayName("CU-22: no se quita si el programa tiene inscripciones activas, si no pertenece al cronograma o sin participar")
    void validaAlDarDeBaja() {
        Programa conInscripciones = mercado().getProgramaVigente();
        Tema deMercado = conInscripciones.getCronogramaOrdenado().get(0).getTema();
        assertTrue(mensaje(() -> temaServicio.darDeBajaTema(deMercado.getIdTema(), conInscripciones.getIdPrograma(),
                fausto())).contains("No se puede dar de baja el tema"));
        assertFalse(deMercado.getBaja());

        Programa programa = macro().getProgramaVigente();
        Tema tema = programa.getCronogramaOrdenado().get(0).getTema();
        assertTrue(mensaje(() -> temaServicio.darDeBajaTema(deMercado.getIdTema(), programa.getIdPrograma(), fausto()))
                .contains("no pertenece al cronograma de este programa"));
        assertTrue(mensaje(() -> temaServicio.darDeBajaTema(tema.getIdTema(), programa.getIdPrograma(), sebastian()))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> temaServicio.darDeBajaTema(tema.getIdTema(), 999999, fausto()))
                .contains("El programa no se encuentra activo"));
        assertTrue(mensaje(() -> temaServicio.darDeBajaTema(999999, programa.getIdPrograma(), fausto()))
                .contains("El tema no se encuentra activa"));
    }

    @Test
    @DisplayName("CU-22: la baja masiva da de baja todos los temas o ninguna, también para el administrador")
    void daDeBajaVarias() {
        Programa programa = macro().getProgramaVigente();
        Tema uno = agregar(programa, "Uno");
        Tema dos = agregar(programa, "Dos");
        Tema deMercado = mercado().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();
        int id = programa.getIdPrograma();

        // uno de los temas no está en este programa: no se da de baja ninguna
        assertTrue(mensaje(() -> temaServicio.darDeBajaVarias(List.of(uno.getIdTema(), deMercado.getIdTema()), id,
                fausto())).contains("no pertenece al cronograma"));
        assertFalse(uno.getBaja());
        assertTrue(mensaje(() -> temaServicio.darDeBajaVarias(List.of(), id, fausto()))
                .contains("Debe seleccionar al menos un tema"));
        assertTrue(mensaje(() -> temaServicio.darDeBajaVarias(List.of(uno.getIdTema()), id, sebastian()))
                .contains("No participás en este curso"));

        temaServicio.darDeBajaVarias(List.of(uno.getIdTema(), dos.getIdTema(), dos.getIdTema()), id, null);

        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
    }

    // ------------------------------------------------------------------ Dedicación docente de los temas

    /** Suma los minutos de los temas vigentes de un curso. */
    private int dedicacionRepartida(Curso curso) {
        return temaServicio.buscarPorCurso(curso).stream().mapToInt(Tema::getDedicacionDocenteMinutos).sum();
    }

    @Test
    @DisplayName("La dedicación docente del curso siempre está repartida por completo entre sus temas")
    void dedicacionDocenteRepartidaPorCompleto() {
        Programa programa = macro().getProgramaVigente();
        Curso curso = programa.getCurso();
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
        int temas = temaServicio.buscarPorCurso(curso).size();

        Tema nueva = agregar(programa, "Tema parejo");

        // el tema nuevo entra con una parte pareja y los demás se reajustan en proporción
        assertEquals(curso.getDedicacionDocenteMinutos() / (temas + 1), nueva.getDedicacionDocenteMinutos());
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
    }

    @Test
    @DisplayName("Al agregar un tema se puede indicar su dedicación docente y los demás absorben la diferencia")
    void agregaConDedicacionDocenteIndicada() {
        Programa programa = macro().getProgramaVigente();
        Curso curso = programa.getCurso();

        Tema nueva = temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, "Tema con carga", null,
                "Contenido", 90);

        assertEquals(90, nueva.getDedicacionDocenteMinutos());
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
        // la carga pedida debe dejar al menos un minuto a cada otro tema
        int otros = temaServicio.buscarPorCurso(curso).size() - 1;
        int maxima = curso.getDedicacionDocenteMinutos() - otros;
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, "Otro tema", null,
                "Contenido", maxima + 1)).contains("La dedicación docente no es válida"));
        assertTrue(mensaje(() -> temaServicio.agregarTema(fausto(), programa.getIdPrograma(), null, "Otro tema", null,
                "Contenido", 0)).contains("La dedicación docente no es válida"));
    }

    @Test
    @DisplayName("Al modificar la dedicación docente de un tema, los demás se reajustan y la suma no cambia")
    void modificaLaDedicacionDocente() {
        Programa programa = macro().getProgramaVigente();
        Curso curso = programa.getCurso();
        Tema tema = programa.getCronogramaOrdenado().get(0).getTema();

        Tema modificado = temaServicio.modificarTema(tema.getIdTema(), fausto(), tema.getTitulo(), tema.getDescripcion(),
                tema.getContenido(), 600);

        assertEquals(600, modificado.getDedicacionDocenteMinutos());
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
        // con un solo tema que se pueda ajustar, no hay de dónde sacar la diferencia
        int maxima = curso.getDedicacionDocenteMinutos() - (temaServicio.buscarPorCurso(curso).size() - 1);
        assertTrue(mensaje(() -> temaServicio.modificarTema(tema.getIdTema(), fausto(), tema.getTitulo(),
                tema.getDescripcion(), tema.getContenido(), maxima + 1)).contains("La dedicación docente no es válida"));
        assertEquals(600, tema.getDedicacionDocenteMinutos());
    }

    @Test
    @DisplayName("Al dar de baja un tema, su dedicación docente se vuelve a repartir entre los que quedan")
    void bajaRepartePorCompletoLaDedicacionDocente() {
        Programa programa = macro().getProgramaVigente();
        Curso curso = programa.getCurso();
        Tema uno = agregar(programa, "Uno");
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));

        temaServicio.darDeBajaTema(uno.getIdTema(), programa.getIdPrograma(), fausto());

        assertTrue(uno.getBaja());
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
    }

    @Test
    @DisplayName("Se puede agregar un tema a un programa con inscripciones: la dedicación docente no es lo que el alumno pagó")
    void agregaEnProgramaConInscripcionesReajustaLaCarga() {
        Programa programa = mercado().getProgramaVigente();
        Curso curso = programa.getCurso();

        Tema nueva = agregar(programa, "Tema con inscripciones");

        assertTrue(nueva.getDedicacionDocenteMinutos() > 0);
        assertEquals(curso.getDedicacionDocenteMinutos(), dedicacionRepartida(curso));
    }
}
