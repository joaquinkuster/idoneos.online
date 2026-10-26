package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Certificado;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.Certificado.CertificadoServicioImpl;
import com.app.idoneos.servicio.Cohorte.CohorteServicioImpl;
import com.app.idoneos.servicio.Curso.CursoServicioImpl;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicioImpl;
import com.app.idoneos.servicio.Programa.ProgramaServicioImpl;

/**
 * Pruebas de las reglas de negocio de las inscripciones (CU-44 a CU-49), sobre los datos de la semilla.
 * Cada prueba se ejecuta en una transacción que se revierte al finalizar.
 */
@SpringBootTest
@Transactional
class InscripcionGestionServicioTest {

    private static final String LUCIA = "lucia.fernandez@correo.com";
    private static final String MARTIN = "martin.gomez@correo.com";
    private static final String VALENTINA = "valentina.ruiz@correo.com";

    @Autowired
    private InscripcionServicioImpl inscripcionServicio;

    @Autowired
    private CertificadoServicioImpl certificadoServicio;

    @Autowired
    private CohorteServicioImpl cohorteServicio;

    @Autowired
    private CursoServicioImpl cursoServicio;

    @Autowired
    private ProgramaServicioImpl programaServicio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    private Cohorte cohorte(String curso, String estado) {
        Integer idPrograma = programaServicio.buscarPorCurso(cursoServicio.buscarPorNombre(curso).orElseThrow())
                .get(0).getIdPrograma();
        return cohorteServicio.buscarConFiltros(idPrograma, null, estado, null, null, "recientes", null).get(0);
    }

    private Inscripcion inscripcion(String correo, String estadoAcceso) {
        return inscripcionServicio.buscarConFiltros(null, correo, estadoAcceso).get(0);
    }

    /** La inscripción de Valentina al curso de Mercado de Capitales, que tiene intentos de autoevaluación y consultas de foro. */
    private Inscripcion inscripcionConActividad() {
        Integer idCurso = cursoServicio.buscarPorNombre("Mercado de Capitales Argentino").orElseThrow().getIdCurso();
        return inscripcionServicio.buscarConFiltros(idCurso, VALENTINA, Inscripcion.ACCESO_VIGENTE).get(0);
    }

    private Alumno alumno(String correo) {
        return inscripcionRepositorio.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals(correo)).findFirst().orElseThrow().getAlumno();
    }

    @Test
    @DisplayName("CU-44: busca las inscripciones por estado de acceso")
    void buscarPorEstadoDeAcceso() {
        assertEquals(3, inscripcionServicio.buscarConFiltros(null, null, Inscripcion.ACCESO_VIGENTE).size());
        assertEquals(2, inscripcionServicio.buscarConFiltros(null, null, Inscripcion.ACCESO_PROGRAMADA).size());
        assertEquals(1, inscripcionServicio.buscarConFiltros(null, null, Inscripcion.ACCESO_PENDIENTE_DE_PAGO).size());
        assertEquals(1, inscripcionServicio.buscarConFiltros(null, null, Inscripcion.ACCESO_VENCIDA).size());
    }

    @Test
    @DisplayName("CU-44: busca por curso y por alumno (nombre, DNI o correo, sin tildes ni mayúsculas)")
    void buscarPorCursoYAlumno() {
        Integer idCurso = cursoServicio.buscarPorNombre("Finanzas Personales e Inversión").orElseThrow().getIdCurso();

        assertEquals(2, inscripcionServicio.buscarConFiltros(idCurso, null, null).size());
        assertEquals(2, inscripcionServicio.buscarConFiltros(null, "VALENTINA", null).size());
        assertEquals(1, inscripcionServicio.buscarConFiltros(null, "41234567", Inscripcion.ACCESO_VIGENTE).size());
        assertEquals(1, inscripcionServicio.buscarConFiltros(null, "lucia", Inscripcion.ACCESO_PENDIENTE_DE_PAGO).size());
    }

    @Test
    @DisplayName("CU-44: no lista las inscripciones dadas de baja")
    void buscarNoListaLasDadasDeBaja() {
        int antes = inscripcionServicio.buscarConFiltros(null, null, null).size();
        Inscripcion vigente = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);

        inscripcionServicio.darDeBajaInscripcion(vigente.getIdInscripcion(), null, null);

        assertEquals(antes - 1, inscripcionServicio.buscarConFiltros(null, null, null).size());
    }

    @Test
    @DisplayName("CU-45: inscribirse a un curso gratuito deja el acceso habilitado, con vencimiento y progreso inicial")
    void inscribirCursoGratuito() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        Alumno valentina = alumno(VALENTINA);

        Inscripcion inscripcion = inscripcionServicio.inscribir(valentina, cohorte.getIdCohorte(), true);

        assertEquals(Inscripcion.ACCESO_VIGENTE, inscripcion.getEstadoAcceso());
        assertTrue(inscripcion.estaHabilitada());
        long semanas = ChronoUnit.WEEKS.between(LocalDateTime.now(), inscripcion.getFechaVencimientoAcceso());
        assertTrue(semanas >= cohorte.getSemanasAcceso() - 1 && semanas <= cohorte.getSemanasAcceso());
        // Progreso inicial: la primera unidad del cronograma, sin completar
        assertEquals(1, inscripcion.getProgresos().size());
        Unidad primera = cohorte.getPrograma().getCronogramaOrdenado().get(0).getUnidad();
        assertEquals(primera, inscripcion.getProgresos().iterator().next().getUnidad());
        assertFalse(inscripcion.getProgresos().iterator().next().getCompletada());
        assertEquals(0, inscripcion.getPorcentajeProgreso());
    }

    @Test
    @DisplayName("CU-45: inscribirse a un curso con costo deja la inscripción pendiente de pago")
    void inscribirCursoConCosto() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", "Abierta");

        Inscripcion inscripcion = inscripcionServicio.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);

        assertFalse(inscripcion.estaHabilitada());
        assertEquals(Inscripcion.ACCESO_PENDIENTE_DE_PAGO, inscripcion.getEstadoAcceso());
    }

    @Test
    @DisplayName("CU-45: con fechas de dictado, el vencimiento se cuenta desde el fin del dictado")
    void inscribirCohorteConDictado() {
        Cohorte cohorte = cohorte("Mercado de Capitales Argentino", "Abierta");
        cohorte.getPrograma().getCurso().setPrecio(0);

        Inscripcion inscripcion = inscripcionServicio.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);

        assertEquals(cohorte.getFechaFinDictado().plusWeeks(cohorte.getSemanasAcceso()),
                inscripcion.getFechaVencimientoAcceso());
        assertEquals(Inscripcion.ACCESO_PROGRAMADA, inscripcion.getEstadoAcceso());
    }

    @Test
    @DisplayName("CU-45: la inscripción se rechaza fuera de la ventana de inscripción")
    void inscribirFueraDeLaVentana() {
        Cohorte finalizada = cohorte("Mercado de Capitales Argentino", "Finalizada");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.inscribir(alumno(VALENTINA), finalizada.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("ya no está habilitada"));
    }

    @Test
    @DisplayName("CU-45: la inscripción se rechaza si la cohorte alcanzó su cupo máximo")
    void inscribirSinCupo() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        cohorte.setCupoMaximo(cohorte.getCantidadInscriptos());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("cupo máximo"));
    }

    @Test
    @DisplayName("CU-45: no admite dos inscripciones activas del mismo alumno a una cohorte")
    void inscribirDuplicada() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", "Abierta"); // Martín ya está inscripto

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.inscribir(alumno(MARTIN), cohorte.getIdCohorte(), true));
        assertTrue(error.getMessage().contains("ya tiene una inscripción vigente"));
    }

    @Test
    @DisplayName("CU-45: exige aceptar los términos y condiciones, un alumno y una cohorte disponible")
    void inscribirSinTerminosNiDatos() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        Alumno valentina = alumno(VALENTINA);

        IllegalArgumentException sinTerminos = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.inscribir(valentina, cohorte.getIdCohorte(), false));
        assertTrue(sinTerminos.getMessage().contains("términos y condiciones"));
        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.inscribir(null, cohorte.getIdCohorte(), true));
        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.inscribir(valentina, null, true));
        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.inscribir(valentina, 99999, true));
        // Nada se registró
        assertEquals(0, inscripcionRepositorio.findByCohorteAndBajaFalse(cohorte).size());
    }

    @Test
    @DisplayName("CU-46: el administrador registra la inscripción de un alumno con las mismas reglas")
    void registrarInscripcion() {
        Cohorte cohorte = cohorte("Macroeconomía de Coyuntura", "Abierta");
        Integer idAlumno = alumno(LUCIA).getIdAlumno();

        Inscripcion inscripcion = inscripcionServicio.registrarInscripcion(idAlumno, cohorte.getIdCohorte(), true);

        assertEquals(LUCIA, inscripcion.getAlumno().getUsuario().getCorreo());
        assertEquals(1, inscripcion.getProgresos().size());
        // Reglas comunes: sin confirmar los términos o repitiendo la inscripción, se rechaza
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.registrarInscripcion(idAlumno, cohorte.getIdCohorte(), true));
        IllegalArgumentException sinTerminos = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.registrarInscripcion(alumno(MARTIN).getIdAlumno(), cohorte.getIdCohorte(), false));
        assertTrue(sinTerminos.getMessage().contains("el alumno acepta"));
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.registrarInscripcion(null, cohorte.getIdCohorte(), true));
    }

    @Test
    @DisplayName("CU-47: modifica observaciones y vencimiento de una inscripción activa")
    void modificarInscripcion() {
        Inscripcion vigente = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);
        LocalDate vencimiento = LocalDate.now().plusMonths(8);

        inscripcionServicio.modificarInscripcion(vigente.getIdInscripcion(), "  Extensión de cortesía ", true, vencimiento);

        assertEquals("Extensión de cortesía", vigente.getObservaciones());
        assertEquals(vencimiento, vigente.getFechaVencimientoAcceso().toLocalDate());
        assertEquals(Inscripcion.ACCESO_VIGENTE, vigente.getEstadoAcceso());
        assertNotNull(vigente.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-47: no se habilita a mano una inscripción de un curso con costo sin pagos aprobados que cubran el precio")
    void noHabilitaSinPagoAprobado() {
        Inscripcion pendiente = inscripcion(LUCIA, Inscripcion.ACCESO_PENDIENTE_DE_PAGO);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.modificarInscripcion(pendiente.getIdInscripcion(), null, true, null));

        assertTrue(error.getMessage().contains("pago aprobado"), error.getMessage());
        assertEquals(Inscripcion.ACCESO_PENDIENTE_DE_PAGO, pendiente.getEstadoAcceso());
    }

    @Test
    @DisplayName("CU-47: conserva el momento exacto del vencimiento si no se cambia el día")
    void modificarInscripcionSinCambiarElDia() {
        Inscripcion vigente = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);
        LocalDateTime original = vigente.getFechaVencimientoAcceso();

        inscripcionServicio.modificarInscripcion(vigente.getIdInscripcion(), "Nota", null, original.toLocalDate());

        assertEquals(original, vigente.getFechaVencimientoAcceso());
    }

    @Test
    @DisplayName("CU-47: el vencimiento no puede ser anterior al que resulta de la duración del cronograma")
    void modificarInscripcionConVencimientoAnteriorAlMinimo() {
        Inscripcion vigente = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);
        LocalDate antesDelMinimo = vigente.getVencimientoMinimo().toLocalDate().minusDays(1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.modificarInscripcion(vigente.getIdInscripcion(), null, null, antesDelMinimo));
        assertTrue(error.getMessage().contains("duración del cronograma"));
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.modificarInscripcion(vigente.getIdInscripcion(), null, null, null));
        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.modificarInscripcion(
                vigente.getIdInscripcion(), "x".repeat(501), null, LocalDate.now().plusYears(1)));
    }

    @Test
    @DisplayName("CU-47: no se modifica una inscripción dada de baja")
    void modificarInscripcionDadaDeBaja() {
        Inscripcion vigente = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);
        inscripcionServicio.darDeBajaInscripcion(vigente.getIdInscripcion(), null, null);

        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.modificarInscripcion(
                vigente.getIdInscripcion(), null, null, LocalDate.now().plusYears(1)));
    }

    @Test
    @DisplayName("CU-48: corrige el nombre y el DNI de un certificado emitido")
    void corregirCertificado() {
        Certificado certificado = inscripcion(MARTIN, Inscripcion.ACCESO_VENCIDA).getCertificadoVigente();

        certificadoServicio.corregirDatos(certificado.getIdCertificado(), "  Martín A. Gómez ", "41234568");

        assertEquals("Martín A. Gómez", certificado.getNombreAlumno());
        assertEquals("41234568", certificado.getDniAlumno());
        assertNotNull(certificado.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-48: valida los campos obligatorios y el DNI")
    void corregirCertificadoConDatosInvalidos() {
        Integer id = inscripcion(MARTIN, Inscripcion.ACCESO_VENCIDA).getCertificadoVigente().getIdCertificado();

        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(id, " ", "41234567"));
        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(id, "Martín Gómez", null));
        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(id, "Martín Gómez", "12.345"));
        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(id, "Martín Gómez", "123456789"));
        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(99999, "Martín Gómez", "41234567"));
    }

    @Test
    @DisplayName("CU-48: no se corrige el certificado de una inscripción dada de baja")
    void corregirCertificadoDeInscripcionDadaDeBaja() {
        Inscripcion vencida = inscripcion(MARTIN, Inscripcion.ACCESO_VENCIDA);
        Integer id = vencida.getCertificadoVigente().getIdCertificado();
        inscripcionServicio.darDeBajaInscripcion(vencida.getIdInscripcion(), null, null);

        assertThrows(IllegalArgumentException.class, () -> certificadoServicio.corregirDatos(id, "Martín Gómez", "41234567"));
    }

    @Test
    @DisplayName("CU-49: el alumno se da de baja: revoca el acceso, da de baja certificado e intentos y conserva los progresos")
    void darDeBajaDelAlumno() {
        Inscripcion vencida = inscripcion(MARTIN, Inscripcion.ACCESO_VENCIDA);
        Certificado certificado = vencida.getCertificadoVigente();
        Inscripcion valentina = inscripcionConActividad();
        int progresos = valentina.getProgresos().size();
        long intentos = valentina.getIntentosAutoevaluacion().stream().filter(i -> !i.esInactivo()).count();
        assertTrue(intentos > 0);

        inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), valentina.getAlumno(), "  Cambio de planes ");
        inscripcionServicio.darDeBajaInscripcion(vencida.getIdInscripcion(), vencida.getAlumno(), null);

        assertTrue(valentina.esInactivo());
        assertEquals(Inscripcion.ACCESO_DADA_DE_BAJA, valentina.getEstadoAcceso());
        assertTrue(valentina.getObservaciones().contains("Cambio de planes"));
        assertTrue(valentina.getIntentosAutoevaluacion().stream().allMatch(i -> i.esInactivo()));
        assertEquals(progresos, valentina.getProgresos().size()); // los progresos no tienen baja lógica
        assertTrue(certificado.estaAnulado());
        assertNull(vencida.getCertificadoVigente());
        // Sin acceso al contenido
        assertTrue(inscripcionServicio.buscarMisCursos(valentina.getAlumno(), null, null).stream()
                .noneMatch(i -> i.getIdInscripcion() == valentina.getIdInscripcion()));
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.validarAcceso(valentina.getIdInscripcion(), valentina.getAlumno()));
    }

    @Test
    @DisplayName("CU-49: las consultas de foro del alumno que se da de baja quedan de forma anónima")
    void darDeBajaAnonimizaLosForos() {
        Inscripcion valentina = inscripcionConActividad();
        List<ConsultaForo> consultas = consultaForoRepositorio.findAll().stream()
                .filter(c -> c.getInscripcion().getIdInscripcion() == valentina.getIdInscripcion()).toList();
        assertFalse(consultas.isEmpty());
        assertEquals("Valentina Ruiz", consultas.get(0).getAutor());

        inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), valentina.getAlumno(), null);

        assertEquals(ConsultaForo.AUTOR_ANONIMO, consultas.get(0).getAutor());
        assertNull(consultas.get(0).getUsuario());
        assertFalse(consultas.get(0).getBaja()); // la consulta se conserva
    }

    @Test
    @DisplayName("CU-49: un alumno no puede dar de baja la inscripción de otro; el administrador puede con cualquiera")
    void darDeBajaAjenaYDelAdministrador() {
        Inscripcion valentina = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);
        Alumno martin = alumno(MARTIN);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), martin, null));
        assertTrue(error.getMessage().contains("no te pertenece"));
        assertFalse(valentina.esInactivo());

        inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), null, "Fraude detectado");
        assertTrue(valentina.esInactivo());
    }

    @Test
    @DisplayName("CU-49: no se da de baja dos veces una inscripción y el motivo tiene un máximo")
    void darDeBajaInvalida() {
        Inscripcion valentina = inscripcion(VALENTINA, Inscripcion.ACCESO_VIGENTE);

        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), null, "x".repeat(301)));
        inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), null, null);
        assertThrows(IllegalArgumentException.class,
                () -> inscripcionServicio.darDeBajaInscripcion(valentina.getIdInscripcion(), null, null));
        assertThrows(IllegalArgumentException.class, () -> inscripcionServicio.darDeBajaInscripcion(99999, null, null));
    }

    @Test
    @DisplayName("CU-49: una inscripción dada de baja libera el cupo de la cohorte")
    void darDeBajaLiberaElCupo() {
        Cohorte cohorte = cohorte("Finanzas Personales e Inversión", "Abierta");
        int inscriptos = cohorte.getCantidadInscriptos();
        Inscripcion nueva = inscripcionServicio.inscribir(alumno(VALENTINA), cohorte.getIdCohorte(), true);
        assertEquals(inscriptos + 1, cohorte.getCantidadInscriptos());

        inscripcionServicio.darDeBajaInscripcion(nueva.getIdInscripcion(), nueva.getAlumno(), null);

        assertEquals(inscriptos, cohorte.getCantidadInscriptos());
    }
}
