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
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.programa.ProgramaService;
import com.app.idoneos.service.tema.TemaService;

/**
 * Pruebas del ABM de temas (CU-19 a CU-22) con los datos de la semilla: "Mercado de Capitales Argentino" tiene un
 * programa con tres temas e inscripciones activas; "Macroeconomía de Coyuntura" tiene un programa con una cohorte sin
 * inscriptos. Fausto Spotorno es titular de ambos cursos y Sebastián Bordato no participa en el segundo.
 */
@SpringBootTest
@Transactional
class TemaServiceTest {

    @Autowired
    private TemaService temaService;

    @Autowired
    private ProgramaService programaService;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    private Curso curso(String nombre) {
        return cursoRepository.findAll().stream().filter(c -> c.getNombre().equals(nombre)).findFirst().orElseThrow();
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

    private Curso mercado() {
        Curso curso = curso("Mercado de Capitales Argentino");
        sinCohortesActivas(curso.getProgramaVigente());
        return curso;
    }

    private Curso macro() {
        Curso curso = curso("Macroeconomía de Coyuntura");
        sinCohortesActivas(curso.getProgramaVigente());
        return curso;
    }

    private Programa macroConCohorteActiva() {
        Programa programa = curso("Macroeconomía de Coyuntura").getProgramaVigente();
        assertTrue(programa.tieneCohortesActivas(), "la semilla deja cohortes activas en el programa");
        return programa;
    }

    @Test
    @DisplayName("Un tema que está en un programa no se da de baja: dejaría sin asignar sus días y el programa nunca queda incompleto")
    void conCohorteActivaNoDesajustaLosDias() {
        Programa programa = macroConCohorteActiva();
        Tema tema = programa.getCronogramaOrdenado().get(0).getTema();
        int dias = programa.getDuracionTotalDias();

        String baja = mensaje(() -> temaService.darDeBajaPorCurso(tema.getIdTema(), fausto()));
        assertTrue(baja.contains("No se puede dar de baja el tema"), baja);
        assertTrue(baja.contains("Quitalo antes del programa"), baja);

        assertFalse(tema.getBaja());
        assertEquals(dias, programa.getDuracionTotalDias());
    }

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
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

    // ------------------------------------------------------------------ CU-19 Buscar tema

    @Test
    @DisplayName("CU-19: se buscan los temas del cronograma por título, en orden de dictado")
    void busca() {
        Programa programa = mercado().getProgramaVigente();

        List<TemaCronograma> todas = temaService.buscarConFiltros(programa, null);
        assertEquals(3, todas.size());
        assertEquals(List.of(1, 2, 3), todas.stream().map(TemaCronograma::getNumeroOrden).toList());

        assertEquals(1, temaService.buscarConFiltros(programa, "RENTA FIJA").size());
        assertEquals(1, temaService.buscarConFiltros(programa, "introduccion").size());
        assertTrue(temaService.buscarConFiltros(programa, "zzz").isEmpty());
    }

    // ------------------------------------------------------------------ CU-21 Modificar tema

    @Test
    @DisplayName("CU-21: se modifica un tema sin inscripciones activas asociadas")
    void modifica() {
        Tema tema = macro().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();

        Tema modificada = temaService.modificar(tema.getIdTema(), fausto(), " Nuevo título ", "",
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
        assertTrue(mensaje(() -> temaService.modificar(conInscripciones.getIdTema(), fausto(), "Otro", null,
                "Contenido")).contains("inscripciones activas asociadas"));

        List<TemaCronograma> cronograma = macro().getProgramaVigente().getCronogramaOrdenado();
        Tema tema = cronograma.get(0).getTema();
        int id = tema.getIdTema();
        assertFalse(tema.tieneInscripcionesActivas());
        assertTrue(mensaje(() -> temaService.modificar(id, fausto(), " ", null, "Contenido"))
                .contains("Debe completar el título del tema."));
        assertTrue(mensaje(() -> temaService.modificar(id, sebastian(), "Otro", null, "Contenido"))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> temaService.modificar(999999, fausto(), "Otro", null, "Contenido"))
                .contains("El tema no se encuentra activa"));
        // conservar su propio título no es un repetido
        assertNotNull(temaService.modificar(id, fausto(), tema.getTitulo(), null, "Otro contenido"));
    }

    // ------------------------------------------------------------------ Temas del curso

    @Test
    @DisplayName("CU-20: registrar un tema del curso no lo incorpora a ningún cronograma")
    void registraUnTemaDelCurso() {
        Programa programa = macro().getProgramaVigente();
        int enElCronograma = programa.getCantidadTemas();

        Tema nuevo = temaService.registrar(fausto(), macro().getIdCurso(), "  Política monetaria ", " Tasas ", null);

        assertEquals("Política monetaria", nuevo.getTitulo());
        assertEquals(enElCronograma, programa.getCantidadTemas());
        assertTrue(nuevo.getProgramasQueLaIncluyen().isEmpty());
        assertTrue(macro().getTemas().contains(nuevo));
        assertTrue(mensaje(() -> temaService.registrar(fausto(), macro().getIdCurso(), "política monetaria", null, null))
                .contains("Ya existe un tema con ese título"));
        assertTrue(mensaje(() -> temaService.registrar(sebastian(), macro().getIdCurso(), "Otro", null, null))
                .contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-20 y CU-21: guardar cambios registra los temas nuevos y modifica los existentes, todos o ninguno")
    void guardaCambios() {
        Tema existente = macro().getProgramaVigente().getCronogramaOrdenado().get(0).getTema();

        int cantidad = temaService.guardarCambios(fausto(), macro().getIdCurso(), List.of(
                new TemaService.CambioDeTema(null, "Tema A", null, "Contenido A"),
                new TemaService.CambioDeTema(existente.getIdTema(), "Título nuevo", null, existente.getContenido())));

        assertEquals(2, cantidad);
        assertEquals("Título nuevo", existente.getTitulo());

        // si uno falla (título repetido), no se guarda ninguno y el mensaje nombra el tema
        String mensaje = mensaje(() -> temaService.guardarCambios(fausto(), macro().getIdCurso(), List.of(
                new TemaService.CambioDeTema(null, "Tema B", null, null),
                new TemaService.CambioDeTema(null, "Tema A", null, null))));
        assertTrue(mensaje.contains("«Tema A»"));
        assertTrue(mensaje.contains("Ya existe un tema con ese título"));
        assertTrue(mensaje(() -> temaService.guardarCambios(fausto(), macro().getIdCurso(), List.of()))
                .contains("No hay cambios para guardar"));
    }

    @Test
    @DisplayName("CU-22: dar de baja un tema del curso que no está en ningún programa lo deja dado de baja")
    void daDeBajaUnTemaDelCurso() {
        Tema tema = temaService.registrar(fausto(), macro().getIdCurso(), "Tema suelto", null, null);

        temaService.darDeBajaPorCurso(tema.getIdTema(), fausto());

        assertTrue(tema.getBaja());
        assertTrue(mensaje(() -> temaService.darDeBajaPorCurso(tema.getIdTema(), fausto()))
                .contains("no se encuentra activo"));
    }

    // ------------------------------------------------------------------ Contenido opcional

    @Test
    @DisplayName("El contenido de un tema es opcional: se puede completar después de registrarlo")
    void registraUnTemaSinContenido() {
        Tema nuevo = temaService.registrar(fausto(), macro().getIdCurso(), "Tema sin contenido", null, null);

        assertNull(nuevo.getContenido());
        Tema completado = temaService.modificar(nuevo.getIdTema(), fausto(), "Tema sin contenido", null, "Ya tiene contenido");
        assertEquals("Ya tiene contenido", completado.getContenido());
    }
}
