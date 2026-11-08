package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.certificado.CertificadoServiceImpl;
import com.app.idoneos.service.cohorte.CohorteServiceImpl;
import com.app.idoneos.service.curso.CursoServiceImpl;
import com.app.idoneos.service.inscripcion.InscripcionServiceImpl;
import com.app.idoneos.service.programa.ProgramaServiceImpl;

/**
 * Pruebas de las reglas de negocio de las inscripciones (CU-44 a CU-49), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class InscripcionGestionServiceTest {

    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private InscripcionServiceImpl inscripcionService;

    @Autowired
    private CertificadoServiceImpl certificadoService;

    @Autowired
    private CohorteServiceImpl cohorteService;

    @Autowired
    private CursoServiceImpl cursoService;

    @Autowired
    private ProgramaServiceImpl programaService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ConsultaForoRepository consultaForoRepository;

    private Cohorte cohorte(String curso, String estado) {
        Integer idPrograma = programaService.buscarPorCurso(cursoService.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        return cohorteService.buscarConFiltros(idPrograma, null, estado, null, null, "recientes", null).get(0);
    }

    /** La semilla trae una inscripción con la fecha de vencimiento pasada: la tarea periódica la deja en «Acceso vencido». */
    @org.junit.jupiter.api.BeforeEach
    void vencerLosAccesos() {
        inscripcionService.vencerAccesos();
    }

    private Inscripcion inscripcion(String correo, String estado) {
        return inscripcionService.buscarConFiltros(null, correo, estado).get(0);
    }

    /** La inscripción de Valentina al curso de Mercado de Capitales, que tiene intentos de autoevaluación y consultas de foro. */
    private Inscripcion inscripcionConActividad() {
        Integer idCurso = cursoService.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow().getIdCurso();
        return inscripcionService.buscarConFiltros(idCurso, VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA).get(0);
    }

    private Alumno alumno(String correo) {
        return inscripcionRepository.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow().getAlumno();
    }

    @Test
    @DisplayName("CU-44: busca las inscripciones por estado")
    void buscarPorEstado() {
        assertEquals(5, inscripcionService.buscarConFiltros(null, null, com.app.idoneos.model.EstadoInscripcion.HABILITADA).size());
        assertEquals(1, inscripcionService.buscarConFiltros(null, null, com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO).size());
        assertEquals(1, inscripcionService.buscarConFiltros(null, null, com.app.idoneos.model.EstadoInscripcion.VENCIDA).size());
    }

    @Test
    @DisplayName("CU-44: busca por curso y por alumno (nombre, DNI o correo, sin tildes ni mayúsculas)")
    void buscarPorCursoYAlumno() {
        Integer idCurso = cursoService.buscarPorNombre("Finanzas Personales e Inversión").orElseThrow().getIdCurso();

        assertEquals(2, inscripcionService.buscarConFiltros(idCurso, null, null).size());
        assertEquals(2, inscripcionService.buscarConFiltros(null, "VALENTINA", null).size());
        assertEquals(1, inscripcionService.buscarConFiltros(null, "41234567", com.app.idoneos.model.EstadoInscripcion.HABILITADA).size());
        assertEquals(1, inscripcionService.buscarConFiltros(null, "lucia", com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO).size());
    }

    @Test
    @DisplayName("CU-44: no lista las inscripciones dadas de baja")
    void buscarNoListaLasDadasDeBaja() {
        int antes = inscripcionService.buscarConFiltros(null, null, null).size();
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);

        inscripcionService.darDeBaja(vigente.getIdInscripcion(), null, null);

        assertEquals(antes - 1, inscripcionService.buscarConFiltros(null, null, null).size());
    }

    @Test
    @DisplayName("CU-45: inscribirse a un curso gratuito deja el acceso habilitado, con vencimiento y progreso inicial")
    void inscribirCursoGratuito() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        Alumno valentina = alumno(VALENTINA);

        Inscripcion inscripcion = inscripcionService.inscribir(valentina, cohorte.getIdCohorte(), true);

        assertEquals(com.app.idoneos.model.EstadoInscripcion.HABILITADA, inscripcion.getNombreEstado());
        assertTrue(inscripcion.fueHabilitada());
        long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), inscripcion.getFechaVencimientoAcceso());
        assertTrue(dias >= cohorte.getAccesoDias() - 1 && dias <= cohorte.getAccesoDias());
        // Progreso inicial: la primer tema del cronograma, sin completar
        assertEquals(1, inscripcion.getProgresos().size());
        Tema primera = cohorte.getPrograma().getCronogramaOrdenado().get(0).getTema();
        assertEquals(primera, inscripcion.getProgresos().iterator().next().getTema());
        assertFalse(inscripcion.getProgresos().iterator().next().getCompletada());
        assertEquals(0, inscripcion.getPorcentajeProgreso());
    }

    @Test
    @DisplayName("CU-45: inscribirse a un curso con costo deja la inscripción pendiente de pago")
    void inscribirCursoConCosto() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", com.app.idoneos.model.EstadoCohorte.ABIERTA);

        Inscripcion inscripcion = inscripcionService.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);

        assertFalse(inscripcion.fueHabilitada());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO, inscripcion.getNombreEstado());
    }

    @Test
    @DisplayName("CU-45: con fechas de dictado, el vencimiento se cuenta desde el inicio del dictado")
    void inscribirCohorteConDictado() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        cohorte.getPrograma().getCurso().setPrecio(0);

        Inscripcion inscripcion = inscripcionService.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);

        assertEquals(cohorte.getFechaInicioDictado().plusDays(cohorte.getAccesoDias()),
                inscripcion.getFechaVencimientoAcceso());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.HABILITADA, inscripcion.getNombreEstado());
    }

    @Test
    @DisplayName("CU-45: la inscripción se rechaza fuera de la ventana de inscripción")
    void inscribirFueraDeLaVentana() {
        Cohorte finalizada = cohorte("Mercado de Capitales Argentino", com.app.idoneos.model.EstadoCohorte.CURSADA_FINALIZADA);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.inscribir(alumno(VALENTINA), finalizada.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("ya no está habilitada"));
    }

    @Test
    @DisplayName("CU-45: la inscripción se rechaza si la cohorte alcanzó su cupo máximo")
    void inscribirSinCupo() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        inscripcionService.inscribir(alumno(LUCIA), cohorte.getIdCohorte(), true); // curso sin costo: queda habilitada y ocupa un lugar
        cohorte.setCupoMaximo(cohorte.getCuposOcupados());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("cupo máximo"));
    }

    @Test
    @DisplayName("CU-45: no admite dos inscripciones activas del mismo alumno a una cohorte")
    void inscribirDuplicada() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", com.app.idoneos.model.EstadoCohorte.ABIERTA); // Martín ya está inscripto

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.inscribir(alumno(MARTIN), cohorte.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("ya tiene una inscripción vigente"));
    }

    @Test
    @DisplayName("CU-45: exige aceptar los términos y condiciones, un alumno y una cohorte disponible")
    void inscribirSinTerminosNiDatos() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        Alumno valentina = alumno(VALENTINA);

        IllegalArgumentException sinTerminos = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.inscribir(valentina, cohorte.getIdCohorte(), false));
        assertTrue(sinTerminos.getMessage().contains("términos y condiciones"));
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.inscribir(null, cohorte.getIdCohorte(), true));
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.inscribir(valentina, null, true));
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.inscribir(valentina, 99999, true));
        // Nada se registró
        assertEquals(0, inscripcionRepository.findByCohorteAndBajaFalse(cohorte).size());
    }

    @Test
    @DisplayName("CU-46: el administrador registra la inscripción de un alumno con las mismas reglas")
    void registrar() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        Integer idAlumno = alumno(LUCIA).getIdAlumno();

        Inscripcion inscripcion = inscripcionService.registrar(idAlumno, cohorte.getIdCohorte(), true);

        assertEquals(LUCIA, inscripcion.getAlumno().getUsuario().getCorreo());
        assertEquals(1, inscripcion.getProgresos().size());
        // Reglas comunes: sin confirmar los términos o repitiendo la inscripción, se rechaza
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.registrar(idAlumno, cohorte.getIdCohorte(), true));
        IllegalArgumentException sinTerminos = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.registrar(alumno(MARTIN).getIdAlumno(), cohorte.getIdCohorte(), false));
        assertTrue(sinTerminos.getMessage().contains("el alumno acepta"));
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.registrar(null, cohorte.getIdCohorte(), true));
    }

    @Test
    @DisplayName("CU-47: modifica las observaciones y suspende el acceso, sin tocar el vencimiento")
    void modificar() {
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        LocalDateTime vencimiento = vigente.getFechaVencimientoAcceso();

        inscripcionService.modificar(vigente.getIdInscripcion(), "  Acceso en pausa ", false);

        assertEquals("Acceso en pausa", vigente.getObservaciones());
        assertEquals(vencimiento, vigente.getFechaVencimientoAcceso());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.SUSPENDIDA, vigente.getNombreEstado());
        assertNotNull(vigente.getUltimaModificacion());

        // reactivarla la devuelve al acceso habilitado
        inscripcionService.modificar(vigente.getIdInscripcion(), null, true);
        assertEquals(com.app.idoneos.model.EstadoInscripcion.HABILITADA, vigente.getNombreEstado());
    }

    @Test
    @DisplayName("CU-47: reactivar una suspendida cuyo vencimiento ya pasó la deja con el acceso vencido")
    void reactivarUnaSuspendidaVencida() {
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        inscripcionService.modificar(vigente.getIdInscripcion(), null, false);
        vigente.setFechaVencimientoAcceso(LocalDateTime.now().minusDays(1));

        inscripcionService.modificar(vigente.getIdInscripcion(), null, true);

        assertEquals(com.app.idoneos.model.EstadoInscripcion.VENCIDA, vigente.getNombreEstado());
    }

    @Test
    @DisplayName("Una suspendida con el vencimiento pasado y una habilitada con el vencimiento pasado pasan a «Acceso vencido» por la tarea periódica")
    void vencerAccesosPorFecha() {
        Inscripcion habilitada = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        Inscripcion suspendida = inscripcion(MARTIN, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        inscripcionService.modificar(suspendida.getIdInscripcion(), null, false);
        habilitada.setFechaVencimientoAcceso(LocalDateTime.now().minusMinutes(5));
        suspendida.setFechaVencimientoAcceso(LocalDateTime.now().minusMinutes(5));

        assertEquals(2, inscripcionService.vencerAccesos());

        assertEquals(com.app.idoneos.model.EstadoInscripcion.VENCIDA, habilitada.getNombreEstado());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.VENCIDA, suspendida.getNombreEstado());
        assertEquals(0, inscripcionService.vencerAccesos());
    }

    @Test
    @DisplayName("CU-47: el administrador no habilita a mano una inscripción pendiente de pago: se habilita al aprobarse el pago")
    void noHabilitaSinPagoAprobado() {
        Inscripcion pendiente = inscripcion(LUCIA, com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.modificar(pendiente.getIdInscripcion(), null, true));

        assertTrue(error.getMessage().contains("se habilita al aprobarse el pago"), error.getMessage());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO, pendiente.getNombreEstado());
    }

    @Test
    @DisplayName("CU-47: las observaciones tienen un máximo de caracteres")
    void modificarConObservacionesLargas() {
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);

        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.modificar(vigente.getIdInscripcion(), "x".repeat(501), null));
    }

    @Test
    @DisplayName("CU-47: una inscripción cancelada por falta de pago o dada de baja no admite ningún cambio")
    void noModificaUnaInscripcionInmutable() {
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        Inscripcion pendiente = inscripcion(LUCIA, com.app.idoneos.model.EstadoInscripcion.PENDIENTE_DE_PAGO);
        pendiente.getCohorte().setFechaFinInscripcion(LocalDateTime.now().minusDays(1));
        inscripcionService.cancelarPendientesDeInscripcionCerrada();
        assertEquals(com.app.idoneos.model.EstadoInscripcion.CANCELADA, pendiente.getNombreEstado());

        IllegalArgumentException cancelada = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.modificar(pendiente.getIdInscripcion(), "Nota", null));
        assertTrue(cancelada.getMessage().contains("cancelada por falta de pago"));
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.darDeBaja(pendiente.getIdInscripcion(), null, null));

        inscripcionService.darDeBaja(vigente.getIdInscripcion(), null, null);
        assertEquals(com.app.idoneos.model.EstadoInscripcion.DADA_DE_BAJA, vigente.getNombreEstado());
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.modificar(vigente.getIdInscripcion(), null, null));
    }

    @Test
    @DisplayName("CU-47: no se modifica una inscripción dada de baja")
    void modificarInscripcionDadaDeBaja() {
        Inscripcion vigente = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        inscripcionService.darDeBaja(vigente.getIdInscripcion(), null, null);

        assertThrows(IllegalArgumentException.class, () -> inscripcionService.modificar(
                vigente.getIdInscripcion(), null, null));
    }

    @Test
    @DisplayName("CU-48: corrige el nombre y el DNI de un certificado emitido")
    void corregirCertificado() {
        Certificado certificado = inscripcion(MARTIN, com.app.idoneos.model.EstadoInscripcion.VENCIDA).getCertificadoVigente();

        certificadoService.corregirDatos(certificado.getIdCertificado(), "  Martín A. Gómez ", "41234568");

        assertEquals("Martín A. Gómez", certificado.getNombreAlumno());
        assertEquals("41234568", certificado.getDniAlumno());
        assertNotNull(certificado.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-48: valida los campos obligatorios y el DNI")
    void corregirCertificadoConDatosInvalidos() {
        Integer id = inscripcion(MARTIN, com.app.idoneos.model.EstadoInscripcion.VENCIDA).getCertificadoVigente().getIdCertificado();

        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(id, " ", "41234567"));
        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(id, "Martín Gómez", null));
        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(id, "Martín Gómez", "12.345"));
        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(id, "Martín Gómez", "123456789"));
        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(99999, "Martín Gómez", "41234567"));
    }

    @Test
    @DisplayName("CU-48: no se corrige el certificado de una inscripción dada de baja")
    void corregirCertificadoDeInscripcionDadaDeBaja() {
        Inscripcion vencida = inscripcion(MARTIN, com.app.idoneos.model.EstadoInscripcion.VENCIDA);
        Integer id = vencida.getCertificadoVigente().getIdCertificado();
        inscripcionService.darDeBaja(vencida.getIdInscripcion(), null, null);

        assertThrows(IllegalArgumentException.class, () -> certificadoService.corregirDatos(id, "Martín Gómez", "41234567"));
    }

    @Test
    @DisplayName("CU-49: el alumno se da de baja: revoca el acceso, da de baja certificado e intentos y conserva los progresos")
    void darDeBajaDelAlumno() {
        Inscripcion vencida = inscripcion(MARTIN, com.app.idoneos.model.EstadoInscripcion.VENCIDA);
        Certificado certificado = vencida.getCertificadoVigente();
        Inscripcion valentina = inscripcionConActividad();
        int progresos = valentina.getProgresos().size();
        long intentos = valentina.getIntentosAutoevaluacion().stream().filter(i -> !i.esInactivo()).count();
        assertTrue(intentos > 0);

        inscripcionService.darDeBaja(valentina.getIdInscripcion(), valentina.getAlumno(), "  Cambio de planes ");
        inscripcionService.darDeBaja(vencida.getIdInscripcion(), vencida.getAlumno(), null);

        assertTrue(valentina.esInactivo());
        assertEquals(com.app.idoneos.model.EstadoInscripcion.DADA_DE_BAJA, valentina.getNombreEstado());
        assertTrue(valentina.getObservaciones().contains("Cambio de planes"));
        assertTrue(valentina.getIntentosAutoevaluacion().stream().allMatch(i -> i.esInactivo()));
        assertEquals(progresos, valentina.getProgresos().size()); // los progresos no tienen baja lógica
        assertTrue(certificado.estaAnulado());
        assertNull(vencida.getCertificadoVigente());
        // Sin acceso al contenido
        assertTrue(inscripcionService.buscarMisCursos(valentina.getAlumno(), null, null).stream()
                .noneMatch(i -> i.getIdInscripcion() == valentina.getIdInscripcion()));
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.validarAcceso(valentina.getIdInscripcion(), valentina.getAlumno()));
    }

    @Test
    @DisplayName("CU-49: las consultas de foro del alumno que se da de baja quedan de forma anónima")
    void darDeBajaAnonimizaLosForos() {
        Inscripcion valentina = inscripcionConActividad();
        List<ConsultaForo> consultas = consultaForoRepository.findAll().stream()
                .filter(c -> c.getInscripcion().getIdInscripcion() == valentina.getIdInscripcion()).toList();
        assertFalse(consultas.isEmpty());
        assertEquals("Valentina Ruiz", consultas.get(0).getAutor());

        inscripcionService.darDeBaja(valentina.getIdInscripcion(), valentina.getAlumno(), null);

        assertEquals(ConsultaForo.AUTOR_ANONIMO, consultas.get(0).getAutor());
        assertNull(consultas.get(0).getUsuario());
        assertFalse(consultas.get(0).getBaja()); // la consulta se conserva
    }

    @Test
    @DisplayName("CU-49: un alumno no puede dar de baja la inscripción de otro; el administrador puede con cualquiera")
    void darDeBajaAjenaYDelAdministrador() {
        Inscripcion valentina = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);
        Alumno martin = alumno(MARTIN);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.darDeBaja(valentina.getIdInscripcion(), martin, null));
        assertTrue(error.getMessage().contains("no te pertenece"));
        assertFalse(valentina.esInactivo());

        inscripcionService.darDeBaja(valentina.getIdInscripcion(), null, "Fraude detectado");
        assertTrue(valentina.esInactivo());
    }

    @Test
    @DisplayName("CU-49: no se da de baja dos veces una inscripción y el motivo tiene un máximo")
    void darDeBajaInvalida() {
        Inscripcion valentina = inscripcion(VALENTINA, com.app.idoneos.model.EstadoInscripcion.HABILITADA);

        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.darDeBaja(valentina.getIdInscripcion(), null, "x".repeat(301)));
        inscripcionService.darDeBaja(valentina.getIdInscripcion(), null, null);
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionService.darDeBaja(valentina.getIdInscripcion(), null, null));
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.darDeBaja(99999, null, null));
    }

    @Test
    @DisplayName("CU-49: una inscripción dada de baja libera el cupo de la cohorte")
    void darDeBajaLiberaElCupo() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", com.app.idoneos.model.EstadoCohorte.ABIERTA);
        int inscriptos = cohorte.getCantidadInscriptos();
        Inscripcion nueva = inscripcionService.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);
        assertEquals(inscriptos + 1, cohorte.getCantidadInscriptos());

        inscripcionService.darDeBaja(nueva.getIdInscripcion(), nueva.getAlumno(), null);

        assertEquals(inscriptos, cohorte.getCantidadInscriptos());
    }
}
