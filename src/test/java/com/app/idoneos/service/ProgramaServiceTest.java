package com.app.idoneos.service;

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

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.service.programa.ProgramaService;

/**
 * Pruebas del ABM de programas (CU-15 a CU-18) con los datos de la semilla: "Mercado de Capitales Argentino" tiene un
 * programa con tres temas, tres cohortes e inscripciones activas; "Macroeconomía de Coyuntura" tiene un programa con
 * una cohorte abierta sin inscriptos. Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el
 * segundo.
 */
@SpringBootTest
@Transactional
class ProgramaServiceTest {

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private DocenteCursoRepository participacionRepository;

    private Curso curso(String nombre) {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Curso mercado() {
        return curso("Mercado de Capitales Argentino");
    }

    private Curso macro() {
        return curso("Macroeconomía de Coyuntura");
    }

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private DocenteCurso participacionDeFausto(Curso curso) {
        return participacionRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == fausto().getIdDocente()).findFirst().orElseThrow();
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    private Programa registrar(Curso curso, Integer origen, String nombre) {
        return programaService.registrar(fausto(), curso.getIdCurso(), origen, nombre, null);
    }

    // ------------------------------------------------------------------ CU-15 Buscar programa

    @Test
    @DisplayName("CU-15: el programa más reciente es el vigente y los demás figuran como anteriores")
    void estados() {
        Programa original = mercado().getProgramaVigente();
        assertEquals("Vigente", original.getEstado());

        Programa nuevo = registrar(mercado(), null, "Programa 2027");

        assertEquals("Vigente", nuevo.getEstado());
        assertEquals("Anterior", original.getEstado());
        assertEquals(nuevo.getIdPrograma(), mercado().getProgramaVigente().getIdPrograma());
    }

    @Test
    @DisplayName("CU-15: se buscan por nombre y estado, del más reciente al más antiguo, sin los dados de baja")
    void busca() {
        Programa nuevo = registrar(mercado(), null, "Programa Avanzado");
        programaService.darDeBaja(nuevo.getIdPrograma(), fausto());

        // el programa dado de baja no se lista
        List<Programa> activos = programaService.buscarConFiltros(mercado(), null, null);
        assertEquals(1, activos.size());
        assertTrue(programaService.buscarConFiltros(mercado(), "AVANZADO", null).isEmpty());
        assertTrue(programaService.buscarConFiltros(mercado(), null, "Dado de baja").isEmpty());

        Programa otro = registrar(mercado(), null, "Programa Nuevo");
        List<Programa> todos = programaService.buscarConFiltros(mercado(), null, null);
        assertEquals(2, todos.size());
        assertEquals(otro.getIdPrograma(), todos.get(0).getIdPrograma());
        assertEquals(1, programaService.buscarConFiltros(mercado(), "NUEVO", null).size());
        assertEquals(1, programaService.buscarConFiltros(mercado(), null, "Vigente").size());
        assertEquals(1, programaService.buscarConFiltros(mercado(), null, "Anterior").size());
        assertTrue(programaService.buscarConFiltros(mercado(), "zzz", null).isEmpty());
    }

    // ------------------------------------------------------------------ CU-16 Registrar programa

    @Test
    @DisplayName("CU-16: registrar un programa lo deja vigente y es el programa de trabajo del docente")
    void registra() {
        Programa nuevo = programaService.registrar(fausto(), macro().getIdCurso(), null, "  Programa 2027  ",
                " Plan nuevo ");

        assertTrue(nuevo.getIdPrograma() > 0);
        assertEquals("Programa 2027", nuevo.getNombre());
        assertEquals("Plan nuevo", nuevo.getDescripcion());
        assertEquals(0, nuevo.getCantidadTemas());
        assertEquals("Vigente", nuevo.getEstado());
        DocenteCurso participacion = participacionDeFausto(macro());
        assertNull(participacion.getCohortePorDefecto());
        assertEquals(nuevo.getIdPrograma(), participacion.getProgramaDeTrabajo().getIdPrograma());
    }

    @Test
    @DisplayName("CU-16: al partir de un programa anterior se copia su cronograma sin tocar el original")
    void copiaElCronograma() {
        Programa original = mercado().getProgramaVigente();
        int temas = original.getCantidadTemas();
        int semanas = original.getDuracionTotalDias();
        assertTrue(temas > 0);

        Programa copia = registrar(mercado(), original.getIdPrograma(), "Edición 2027");

        assertEquals(temas, copia.getCantidadTemas());
        assertEquals(semanas, copia.getDuracionTotalDias());
        assertEquals(original.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList(),
                copia.getCronogramaOrdenado().stream().map(c -> c.getTema().getIdTema()).toList());
        assertEquals(temas, original.getCantidadTemas());
    }

    @Test
    @DisplayName("CU-16: supera el máximo, el origen no es válido o el docente no participa")
    void validaAlRegistrar() {
        int curso = macro().getIdCurso();
        // sin nombre, la versión se llama "Versión N" (las que tuvo el curso más una)
        int versiones = macro().getProgramas().size();
        assertEquals("Versión " + (versiones + 1), programaService.registrar(fausto(), curso, null, "", null).getNombre());
        assertTrue(mensaje(() -> programaService.registrar(fausto(), curso, null, "x".repeat(51), null))
                .contains("Máximo: 50 caracteres"));
        assertTrue(mensaje(() -> programaService.registrar(fausto(), curso, null, "Nombre", "x".repeat(151)))
                .contains("Máximo: 150 caracteres"));
        // el programa de otro curso no sirve como origen
        assertTrue(mensaje(() -> programaService.registrar(fausto(), curso,
                mercado().getProgramaVigente().getIdPrograma(), "Nombre", null))
                .contains("programa de origen de la lista"));
        assertTrue(mensaje(() -> programaService.registrar(fausto(), 999999, null, "Nombre", null))
                .contains("El curso no se encuentra activo"));
        assertTrue(mensaje(() -> programaService.registrar(docente("sebastian.bordato@idoneos.online"), curso,
                null, "Nombre", null)).contains("No participás en este curso"));
        assertTrue(mensaje(() -> programaService.registrar(null, curso, null, "Nombre", null))
                .contains("rol de docente"));
    }

    // ------------------------------------------------------------------ CU-17 Modificar programa

    @Test
    @DisplayName("CU-17: se modifica un programa sin inscripciones activas")
    void modifica() {
        Programa programa = macro().getProgramaVigente();

        Programa modificado = programaService.modificar(programa.getIdPrograma(), fausto(), " Nuevo nombre ",
                "");

        assertEquals("Nuevo nombre", modificado.getNombre());
        assertNull(modificado.getDescripcion());
        assertNotNull(modificado.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-17: no se modifica si tiene inscripciones activas, si queda vacío o si el docente no participa")
    void validaAlModificar() {
        Programa conInscripciones = mercado().getProgramaVigente();
        assertTrue(conInscripciones.getCantidadInscripcionesActivas() > 0);
        assertTrue(mensaje(() -> programaService.modificar(conInscripciones.getIdPrograma(), fausto(), "Nombre",
                null)).contains("tiene inscripciones activas"));

        int id = macro().getProgramaVigente().getIdPrograma();
        assertTrue(mensaje(() -> programaService.modificar(id, fausto(), " ", null))
                .contains("Debe completar el nombre del programa."));
        assertTrue(mensaje(() -> programaService.modificar(id, docente("sebastian.bordato@idoneos.online"),
                "Nombre", null)).contains("No participás en este curso"));
        assertTrue(mensaje(() -> programaService.modificar(999999, fausto(), "Nombre", null))
                .contains("El programa no se encuentra activo"));
    }

    // ------------------------------------------------------------------ CU-18 Dar de baja programa

    @Test
    @DisplayName("CU-18: se da de baja un programa sin cohortes activas, el docente o el administrador")
    void daDeBaja() {
        Programa uno = registrar(macro(), null, "Por error");
        Programa dos = registrar(macro(), null, "Otro error");

        programaService.darDeBaja(uno.getIdPrograma(), fausto());
        programaService.darDeBaja(dos.getIdPrograma(), null);

        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
        assertEquals("Dado de baja", uno.getEstado());
        // el vigente vuelve a ser el programa anterior
        assertEquals("Vigente", macro().getProgramaVigente().getEstado());
        assertTrue(mensaje(() -> programaService.darDeBaja(uno.getIdPrograma(), fausto()))
                .contains("no se encuentra activo"));
    }

    @Test
    @DisplayName("CU-18: no se da de baja un programa con cohortes activas ni el de un curso en el que no se participa")
    void validaAlDarDeBaja() {
        int conCohortes = mercado().getProgramaVigente().getIdPrograma();
        assertTrue(mensaje(() -> programaService.darDeBaja(conCohortes, fausto()))
                .contains("No se puede dar de baja el programa"));
        assertTrue(mensaje(() -> programaService.darDeBaja(conCohortes, null))
                .contains("No se puede dar de baja el programa"));

        Programa sinCohortes = registrar(macro(), null, "Sin cohortes");
        assertTrue(mensaje(() -> programaService.darDeBaja(sinCohortes.getIdPrograma(),
                docente("sebastian.bordato@idoneos.online"))).contains("No participás en este curso"));
        assertFalse(sinCohortes.getBaja());
    }

    @Test
    @DisplayName("CU-18: la baja masiva da de baja todos los programas o ninguno")
    void daDeBajaVarios() {
        Programa uno = registrar(macro(), null, "Uno");
        Programa dos = registrar(macro(), null, "Dos");
        Programa conCohortes = mercado().getProgramaVigente();

        assertTrue(mensaje(() -> programaService.darDeBajaVarios(List.of(uno.getIdPrograma(),
                conCohortes.getIdPrograma()), fausto())).contains("No se puede dar de baja el programa"));
        assertFalse(uno.getBaja());

        assertTrue(mensaje(() -> programaService.darDeBajaVarios(List.of(uno.getIdPrograma(), dos.getIdPrograma()),
                docente("sebastian.bordato@idoneos.online"))).contains("No participás en este curso"));
        assertFalse(uno.getBaja());
        assertTrue(mensaje(() -> programaService.darDeBajaVarios(List.of(), fausto()))
                .contains("Debe seleccionar al menos un programa"));

        programaService.darDeBajaVarios(List.of(uno.getIdPrograma(), dos.getIdPrograma(), dos.getIdPrograma()), null);
        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
    }

    // ------------------------------------------------------------------ Cronograma del programa

    @Test
    @DisplayName("CU-16: registrar un programa con su cronograma deja los temas en el orden y con los días indicados")
    void registraConCronograma() {
        List<Tema> temas = macro().getTemas().stream().filter(t -> !t.getBaja()).limit(2).toList();

        Programa nuevo = programaService.registrar(fausto(), macro().getIdCurso(), null, "Con cronograma", null,
                List.of(temas.get(1).getIdTema(), temas.get(0).getIdTema()), List.of("7", "14"));

        assertEquals(2, nuevo.getCantidadTemas());
        assertEquals(temas.get(1).getIdTema(), nuevo.getCronogramaOrdenado().get(0).getTema().getIdTema());
        assertEquals(7, nuevo.getCronogramaOrdenado().get(0).getDuracionDias());
        assertEquals(21, nuevo.getDuracionTotalDias());
    }

    @Test
    @DisplayName("CU-16: un cronograma vacío no copia el del programa de origen")
    void cronogramaVacioNoCopiaElDelOrigen() {
        Programa origen = macro().getProgramaVigente();
        assertTrue(origen.getCantidadTemas() > 0);

        Programa nuevo = programaService.registrar(fausto(), macro().getIdCurso(), origen.getIdPrograma(),
                "En blanco", null, List.of(), List.of());

        assertEquals(0, nuevo.getCantidadTemas());
    }

    @Test
    @DisplayName("CU-17: modificar un programa con cronograma cambia sus datos y su cronograma; quitar un tema no lo da de baja")
    void modificaConCronograma() {
        List<Tema> temas = macro().getTemas().stream().filter(t -> !t.getBaja()).limit(2).toList();
        Programa programa = programaService.registrar(fausto(), macro().getIdCurso(), null, "Para modificar", null,
                List.of(temas.get(0).getIdTema(), temas.get(1).getIdTema()), List.of("7", "7"));
        Tema quitado = temas.get(0);
        Tema conservado = temas.get(1);

        programaService.modificar(programa.getIdPrograma(), fausto(), "Renombrado", "Otro plan",
                List.of(conservado.getIdTema()), List.of("30"));

        assertEquals("Renombrado", programa.getNombre());
        assertEquals(1, programa.getCantidadTemas());
        assertEquals(30, programa.getDuracionTotalDias());
        assertFalse(quitado.getBaja());
    }

    @Test
    @DisplayName("El programa de trabajo del docente es el vigente del curso mientras no tenga una cohorte de trabajo")
    void elProgramaDeTrabajoEsElVigente() {
        Programa nuevo = registrar(macro(), null, "Programa 2027");

        assertEquals(nuevo.getIdPrograma(), participacionDeFausto(macro()).getProgramaDeTrabajo().getIdPrograma());
    }
}
