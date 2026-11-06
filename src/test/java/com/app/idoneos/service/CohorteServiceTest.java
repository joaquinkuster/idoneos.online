package com.app.idoneos.service;

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

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.service.cohorte.CohorteServiceImpl;
import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.docente.DocenteServiceImpl;
import com.app.idoneos.service.programa.ProgramaServiceImpl;

/**
 * Pruebas de las reglas de negocio de las cohortes (CU-11 a CU-14), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class CohorteServiceTest {

    private static final LocalDate INICIO = LocalDate.now().plusMonths(12);

    @Autowired
    private CohorteServiceImpl cohorteService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private ProgramaServiceImpl programaService;

    @Autowired
    private DocenteServiceImpl docenteService;

    private Programa programaDe(String curso) {
        return programaService.buscarPorCurso(cursoService.buscarPorNombre(curso).orElseThrow()).get(0);
    }

    /** Un programa recién registrado, sin temas ni contenido. */
    private Programa programaSinContenido() {
        Programa existente = programaDe("Macroeconomía de Coyuntura");
        return programaService.registrar(existente.getCurso().getDocenteTitular(), existente.getCurso().getIdCurso(),
                null, "Versión sin contenido", null);
    }

    private Cohorte cohorte(String curso, String estado) {
        return cohorteService.buscarConFiltros(programaDe(curso).getIdPrograma(), null, estado, null, null, "recientes", null).get(0);
    }

    @Test
    @DisplayName("CU-11: busca cohortes por estado calculado a partir de sus fechas")
    void buscarCohortesPorEstado() {
        Integer idPrograma = programaDe("Mercado de Capitales Argentino").getIdPrograma();

        assertEquals(1, cohorteService.buscarConFiltros(idPrograma, null, "Abierta", null, null, "recientes", null).size());
        assertEquals(1, cohorteService.buscarConFiltros(idPrograma, null, "En dictado", null, null, "recientes", null).size());
        assertEquals(1, cohorteService.buscarConFiltros(idPrograma, null, "Finalizada", null, null, "recientes", null).size());
        assertEquals(3, cohorteService.buscarConFiltros(idPrograma, null, null, null, null, "recientes", null).size());
    }

    @Test
    @DisplayName("CU-12: registra una cohorte de un curso grabado")
    void registrarCohorteDeCursoGrabado() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");

        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                null, programa.getDuracionTotalDias(), 25);

        assertEquals(25, cohorte.getCupoMaximo());
        assertNull(cohorte.getFechaInicioDictado());
        assertEquals("Próxima", cohorte.getEstado());
    }

    @Test
    @DisplayName("CU-12: el dictado debe empezar después de la fecha de fin de inscripción, no el mismo día")
    void registrarCohorteConInicioDeDictadoElMismoDiaQueElFinDeInscripcion() {
        Programa programa = programaDe("Mercado de Capitales Argentino");
        int semanas = programa.getDuracionTotalDias();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                        INICIO.plusDays(30), semanas, null));
        assertEquals("Error! La fecha de inicio de dictado debe ser posterior a la fecha de fin de inscripción.", error.getMessage());

        // Al día siguiente sí se puede
        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                INICIO.plusDays(31), semanas, null);
        assertEquals(INICIO.plusDays(31), cohorte.getFechaInicioDictado().toLocalDate());
    }

    @Test
    @DisplayName("CU-12: un curso en vivo exige las fechas de dictado")
    void registrarCohorteEnVivoSinFechasDeDictado() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30), null,
                        12, null));
        assertTrue(error.getMessage().contains("dictado"));
    }

    @Test
    @DisplayName("CU-12: registra una cohorte en vivo con dictado posterior a la inscripción")
    void registrarCohorteEnVivo() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                INICIO.plusDays(40), 84, null);

        assertEquals(INICIO.plusDays(40), cohorte.getFechaInicioDictado().toLocalDate());
    }

    @Test
    @DisplayName("CU-12: el fin del dictado se calcula: el inicio más la duración del curso")
    void registrarCohorteCalculaElFinDelDictado() {
        Programa programa = programaDe("Mercado de Capitales Argentino");
        int duracion = programa.getCurso().getDuracionDias();

        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                INICIO.plusDays(40), duracion, null);

        // el dictado dura lo que el curso: termina el último día de su duración
        assertEquals(INICIO.plusDays(40).plusDays(duracion - 1L), cohorte.getFechaFinDictado().toLocalDate());
    }

    @Test
    @DisplayName("CU-12: el acceso no puede ser menor a la duración del curso, aunque la cohorte no tenga programa")
    void registrarCohorteConAccesoMenorALaDuracionDelCurso() {
        var curso = programaDe("Macroeconomía de Coyuntura").getCurso();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(curso.getIdCurso(), null, INICIO, INICIO.plusDays(30), null,
                        curso.getDuracionDias() - 1, null));
        assertEquals("Error! Los días de acceso (" + (curso.getDuracionDias() - 1)
                + ") no pueden ser menores a la duración del curso (" + curso.getDuracionDias() + " días).", error.getMessage());

        // con la duración justa se registra; lo que se agrega es el tiempo extra para quien se atrase
        Cohorte justa = cohorteService.registrar(curso.getIdCurso(), null, INICIO, INICIO.plusDays(30), null,
                curso.getDuracionDias(), null);
        assertEquals(curso.getDuracionDias(), justa.getAccesoDias());
    }

    @Test
    @DisplayName("CU-12: el dictado no puede comenzar antes de que termine la inscripción")
    void registrarCohorteConDictadoAntesDelFinDeInscripcion() {
        Programa programa = programaDe("Mercado de Capitales Argentino");

        assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30),
                        INICIO.plusDays(10), 12, null));
    }

    @Test
    @DisplayName("CU-12: valida fechas de inscripción, cupo y semanas de acceso")
    void registrarCohorteConDatosInvalidos() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        Integer id = programa.getIdPrograma();
        int duracion = programa.getDuracionTotalDias();

        assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, null, null, duracion, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO.plusDays(30), INICIO, null, duracion, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null, duracion - 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null, duracion, 0));
    }

    @Test
    @DisplayName("CU-12: las semanas de acceso y el cupo tienen un máximo razonable, con un mensaje para cada regla")
    void registrarCohorteConValoresMaximosExcedidos() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        Integer id = programa.getIdPrograma();
        int duracion = programa.getDuracionTotalDias();

        IllegalArgumentException semanas = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null,
                        Cohorte.DIAS_ACCESO_MAXIMOS + 1, null));
        assertTrue(semanas.getMessage().contains("número entero de 1 a " + Cohorte.DIAS_ACCESO_MAXIMOS));
        assertFalse(semanas.getMessage().contains("duración total"));

        IllegalArgumentException cupo = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null, duracion,
                        Cohorte.CUPO_MAXIMO_PERMITIDO + 1));
        assertTrue(cupo.getMessage().contains("número entero de 1 a " + Cohorte.CUPO_MAXIMO_PERMITIDO));

        IllegalArgumentException cero = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null, 0, null));
        assertTrue(cero.getMessage().contains("Formato válido: número entero"));

        Cohorte limite = cohorteService.registrar(id, INICIO, INICIO.plusDays(30), null,
                Cohorte.DIAS_ACCESO_MAXIMOS, Cohorte.CUPO_MAXIMO_PERMITIDO);
        assertEquals(Cohorte.CUPO_MAXIMO_PERMITIDO, limite.getCupoMaximo());
    }

    @Test
    @DisplayName("CU-12: con el contenido del programa incompleto la cohorte se registra como pendiente")
    void registrarCohorteConContenidoIncompleto() {
        Programa programa = programaSinContenido();
        assertEquals(0, programa.getCantidadTemasConMaterialPublicado());

        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), INICIO, INICIO.plusDays(30), null,
                programa.getCurso().getDuracionDias(), null);

        assertTrue(cohorte.esPendiente());
        assertEquals("Pendiente", cohorte.getEstado());
    }

    @Test
    @DisplayName("CU-12: con el contenido del programa completo la cohorte nace activa, sin plazo mínimo")
    void registrarCohorteConContenidoCompleto() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        LocalDate manana = LocalDate.now().plusDays(1);

        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), manana, manana.plusDays(30), null,
                programa.getDuracionTotalDias(), null);

        assertTrue(cohorte.esActiva());
    }

    @Test
    @DisplayName("CU-12: una cohorte pendiente necesita el plazo mínimo para completar el contenido")
    void registrarCohortePendienteConPlazoInsuficiente() {
        Programa programa = programaSinContenido();
        int plazo = cohorteService.plazoMinimoParaCompletarContenidoDias();
        LocalDate corto = LocalDate.now().plusDays(plazo - 1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(programa.getIdPrograma(), corto, corto.plusDays(30), null,
                        programa.getCurso().getDuracionDias(), null));
        assertTrue(error.getMessage().startsWith("Error! La fecha de inicio de inscripción debe ser al menos"), error.getMessage());

        LocalDate justo = LocalDate.now().plusDays(plazo);
        Cohorte cohorte = cohorteService.registrar(programa.getIdPrograma(), justo, justo.plusDays(30), null,
                programa.getCurso().getDuracionDias(), null);
        assertTrue(cohorte.esPendiente());
    }

    @Test
    @DisplayName("Una cohorte pendiente se activa sola cuando el contenido de su programa queda completo")
    void activaLasPendientesConContenidoCompleto() {
        Programa incompleto = programaDe("Macroeconomía de Coyuntura");
        Cohorte cohorte = cohorteService.registrar(incompleto.getCurso().getIdCurso(), null, INICIO,
                INICIO.plusDays(30), null, incompleto.getDuracionTotalDias(), null);
        Programa completo = programaDe("Mercado de Capitales Argentino");
        org.junit.jupiter.api.Assumptions.assumeTrue(cohorteService.faltantesDeContenido(completo).isEmpty());

        // sin programa no se activa; con uno completo, sí
        assertEquals(0, cohorteService.activarPendientesConContenidoCompleto());
        assertTrue(cohorte.esPendiente());
        cohorte.setPrograma(completo);
        assertTrue(cohorteService.activarPendientesConContenidoCompleto() >= 1);
        assertFalse(cohorte.esPendiente());
        assertFalse(cohorte.esCancelada());
    }

    @Test
    @DisplayName("Una cohorte pendiente que llega a su inicio de inscripción sin contenido completo se cancela")
    void cancelaLasPendientesVencidas() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        Cohorte vencida = cohorteService.registrar(programa.getCurso().getIdCurso(), null, INICIO,
                INICIO.plusDays(30), null, programa.getDuracionTotalDias(), null);
        Cohorte futura = cohorteService.registrar(programa.getCurso().getIdCurso(), null, INICIO,
                INICIO.plusDays(30), null, programa.getDuracionTotalDias(), null);
        vencida.setFechaInicioInscripcion(java.time.LocalDateTime.now().minusHours(1));

        int canceladas = cohorteService.cancelarPendientesVencidas();

        assertTrue(canceladas >= 1);
        assertTrue(vencida.esCancelada());
        assertTrue(futura.esPendiente());
        // una cohorte cancelada ya no se modifica
        assertThrows(IllegalArgumentException.class, () -> cohorteService.modificar(vencida.getIdCohorte(), INICIO,
                INICIO.plusDays(30), null, programa.getDuracionTotalDias(), null));
    }

    @Test
    @DisplayName("CU-12: una cohorte se registra sin programa y queda pendiente hasta que el docente elija uno")
    void registrarCohorteSinPrograma() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        var curso = programa.getCurso();

        Cohorte cohorte = cohorteService.registrar(curso.getIdCurso(), null, INICIO, INICIO.plusDays(30), null,
                programa.getDuracionTotalDias(), null);

        assertTrue(cohorte.esPendiente());
        assertNull(cohorte.getPrograma());
        assertEquals(curso.getIdCurso(), cohorte.getCurso().getIdCurso());
        assertFalse(cohorteService.faltantesDeContenido(null).isEmpty());

        // Una cohorte pendiente sin programa se puede elegir como cohorte de trabajo: desde ahí se le asigna el programa
        cohorteService.cambiarContextoDeTrabajo(cohorte, curso.getDocenteTitular());

        // El docente elige el programa: como su contenido está completo, la cohorte pasa a activa
        Docente docente = curso.getDocenteTitular();
        Cohorte asignada = cohorteService.asignarPrograma(cohorte.getIdCohorte(), programa.getIdPrograma(), docente);
        assertEquals(programa.getIdPrograma(), asignada.getPrograma().getIdPrograma());
        assertTrue(asignada.esActiva());
    }

    @Test
    @DisplayName("CU-12: el programa de la cohorte debe ser del curso")
    void registrarCohorteConProgramaDeOtroCurso() {
        Programa programa = programaDe("Macroeconomía de Coyuntura");
        Programa ajeno = programaDe("Mercado de Capitales Argentino");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.registrar(programa.getCurso().getIdCurso(), ajeno.getIdPrograma(), INICIO,
                        INICIO.plusDays(30), null, 84, null));
        assertEquals("Error! El programa no pertenece al curso de la cohorte.", error.getMessage());
    }

    @Test
    @DisplayName("CU-11: filtra las cohortes pendientes y las canceladas, distintas de las dadas de baja")
    void buscarCohortesPendientesYCanceladas() {
        assertTrue(!cohorteService.buscarConFiltros(null, null, "Pendiente", null, null, "recientes", null).isEmpty());
        assertTrue(cohorteService.buscarConFiltros(null, null, "Pendiente", null, null, "recientes", null).stream()
                .allMatch(Cohorte::esPendiente));
        assertTrue(!cohorteService.buscarConFiltros(null, null, "Cancelada", null, null, "recientes", null).isEmpty());
        assertTrue(cohorteService.buscarConFiltros(null, null, "Cancelada", null, null, "recientes", null).stream()
                .allMatch(Cohorte::esCancelada));
    }

    @Test
    @DisplayName("CU-13: una cohorte cancelada no se modifica")
    void modificarCohorteCancelada() {
        Cohorte cancelada = cohorteService.buscarConFiltros(null, null, "Cancelada", null, null, "recientes", null).get(0);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> cohorteService.modificar(cancelada.getIdCohorte(), INICIO, INICIO.plusDays(30), null, 84, null));
        assertTrue(error.getMessage().startsWith("Error! No se puede modificar la cohorte «") && error.getMessage().endsWith("»: está cancelada."), error.getMessage());
    }

    @Test
    @DisplayName("CU-13: modifica una cohorte sin inscripciones")
    void modificarCohorteSinInscripciones() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        int duracion = cohorte.getPrograma().getDuracionTotalDias();

        Cohorte modificada = cohorteService.modificar(cohorte.getIdCohorte(), INICIO, INICIO.plusDays(20), null, duracion + 2, 40);

        assertEquals(40, modificada.getCupoMaximo());
        assertEquals(duracion + 2, modificada.getAccesoDias());
        assertTrue(modificada.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-13: no modifica una cohorte con inscripciones activas")
    void modificarCohorteConInscripciones() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Abierta");
        assertTrue(cohorte.getCantidadInscriptos() > 0);

        assertThrows(IllegalArgumentException.class, () -> cohorteService.modificar(cohorte.getIdCohorte(),
                INICIO, INICIO.plusDays(20), INICIO.plusDays(30), 12, null));
    }

    @Test
    @DisplayName("CU-14: no da de baja una cohorte con inscripciones activas")
    void darDeBajaCohorteConInscripciones() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Abierta");

        assertThrows(IllegalArgumentException.class, () -> cohorteService.darDeBaja(cohorte.getIdCohorte()));
    }

    @Test
    @DisplayName("CU-14: no da de baja una cohorte con clases en vivo activas")
    void darDeBajaCohorteConClasesEnVivo() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "En dictado");
        assertTrue(cohorte.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()));

        assertThrows(IllegalArgumentException.class, () -> cohorteService.darDeBaja(cohorte.getIdCohorte()));
    }

    @Test
    @DisplayName("CU-14: da de baja una cohorte y la desvincula de los docentes que la tienen como contexto de trabajo")
    void darDeBajaCohorteDesvinculaAlDocente() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Finalizada");
        Docente fausto = docenteService.buscarHabilitados().stream()
                .filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online")).findFirst().orElseThrow();
        cohorteService.cambiarContextoDeTrabajo(cohorte, fausto);

        cohorteService.darDeBaja(cohorte.getIdCohorte());

        assertTrue(cohorte.esInactivo());
        assertTrue(cohorte.getParticipacionesDocente().stream().allMatch(p -> p.getCohortePorDefecto() == null));
        assertFalse(cohorteService.buscarPorId(cohorte.getIdCohorte()).isPresent());
    }

    @Test
    @DisplayName("CU-11: el docente cambia su contexto de trabajo solo en los cursos en los que participa")
    void cambiarContextoDeTrabajo() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        List<Docente> docentes = docenteService.buscarHabilitados();
        Docente fausto = docentes.stream().filter(d -> d.getUsuario().getCorreo().equals("fausto.spotorno@idoneos.online"))
                .findFirst().orElseThrow();
        Docente mariano = docentes.stream().filter(d -> d.getUsuario().getCorreo().equals("mariano.otalora@idoneos.online"))
                .findFirst().orElseThrow();

        cohorteService.cambiarContextoDeTrabajo(cohorte, fausto);
        assertThrows(IllegalArgumentException.class, () -> cohorteService.cambiarContextoDeTrabajo(cohorte, mariano));
    }

    @Test
    @DisplayName("CU-11: ordena por inicio de inscripción más reciente o por curso, con las dadas de baja al final")
    void ordenarCohortes() {
        Integer idPrograma = programaDe("Mercado de Capitales Argentino").getIdPrograma();
        Cohorte finalizada = cohorte("Mercado de Capitales Argentino", "Finalizada");
        cohorteService.darDeBaja(finalizada.getIdCohorte());

        List<Cohorte> recientes = cohorteService.buscarConFiltros(idPrograma, null, null, null, null, "recientes", null);
        List<Cohorte> porCurso = cohorteService.buscarConFiltros(null, null, null, null, null, "curso", null);

        assertTrue(recientes.get(recientes.size() - 1).esInactivo());
        assertTrue(recientes.get(0).getFechaInicioInscripcion().isAfter(recientes.get(1).getFechaInicioInscripcion()));
        assertTrue(porCurso.get(porCurso.size() - 1).esInactivo());
        // el primero es el curso que va antes en orden alfabético, sea cual sea
        String primero = porCurso.stream().filter(c -> !c.esInactivo()).map(c -> c.getCurso().getNombre())
                .min(String.CASE_INSENSITIVE_ORDER).orElseThrow();
        assertEquals(primero, porCurso.get(0).getCurso().getNombre());
    }
}
