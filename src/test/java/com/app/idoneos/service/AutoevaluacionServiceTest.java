package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.IntentoAutoevaluacion;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.IntentoAutoevaluacionRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.autoevaluacion.AutoevaluacionService;
import com.app.idoneos.service.autoevaluacion.AutoevaluacionService.SolicitudAutoevaluacion;
import com.app.idoneos.service.pool.PoolService;

/** Pruebas del ABM de autoevaluaciones con los datos de la semilla. */
@SpringBootTest
@Transactional
class AutoevaluacionServiceTest {

    private static final String APERTURA = "2026-01-01T09:00";

    @Autowired
    private AutoevaluacionService autoevaluacionService;

    @Autowired
    private PoolService poolService;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private IntentoAutoevaluacionRepository intentoRepository;

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Tema tema(String curso, String titulo) {
        return temaRepository.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    private Tema primera() {
        return tema("Mercado de Capitales Argentino", "Introducción al mercado de capitales");
    }

    private Tema segunda() {
        return tema("Mercado de Capitales Argentino", "Renta fija y bonos");
    }

    private Tema tercera() {
        return tema("Mercado de Capitales Argentino", "Renta variable y opciones");
    }

    private Pool pool(String nombre) {
        return poolService.buscarConFiltros(null, null, nombre, false, null).stream()
                .filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    private Autoevaluacion autoevaluacion(String nombre) {
        return autoevaluacionService.buscarConFiltros(null, null, nombre, false, null).stream()
                .filter(a -> a.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    /** Solicitud válida: 3 preguntas, 60 minutos, nota mínima 6, con apertura y sin cierre ni límite de intentos. */
    private SolicitudAutoevaluacion solicitud(String nombre, Pool... pools) {
        return new SolicitudAutoevaluacion(nombre, "60", "3", "6", "", APERTURA, "", false, true, true, false,
                java.util.Arrays.stream(pools).map(Pool::getIdPool).toList());
    }

    private SolicitudAutoevaluacion conCambios(Autoevaluacion actual, String nombre, String intentos, String cierre,
            boolean oculto) {
        return new SolicitudAutoevaluacion(nombre, String.valueOf(actual.getTiempoLimite()),
                String.valueOf(actual.getCantidadPreguntas()), String.valueOf((int) actual.getNotaMinima()), intentos,
                actual.getFechaApertura().withSecond(0).withNano(0).toString(), cierre, actual.getDescuentoPorError(),
                actual.getNavegacionLibre(), actual.getPermiteSalir(), oculto,
                actual.getPoolsActivos().stream().map(Pool::getIdPool).toList());
    }

    private String error(Runnable accion) {
        return assertThrows(IllegalArgumentException.class, accion::run).getMessage();
    }

    private void assertError(String fragmento, Runnable accion) {
        String mensaje = error(accion);
        assertTrue(mensaje.contains(fragmento), mensaje);
    }

    @Test
    @DisplayName("CU-65: crea una autoevaluación asociada a los pools de su tema")
    void crearAutoevaluacion() {
        Autoevaluacion creada = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("  Segunda autoevaluación ", pool("Renta fija")));

        assertEquals("Segunda autoevaluación", creada.getNombre());
        assertEquals(60, creada.getTiempoLimite());
        assertEquals(3, creada.getCantidadPreguntas());
        assertEquals(6f, creada.getNotaMinima());
        assertNull(creada.getIntentosPermitidos());
        assertNull(creada.getFechaCierre());
        assertTrue(creada.getNavegacionLibre());
        assertEquals(List.of(pool("Renta fija")), creada.getPoolsActivos());
        assertFalse(creada.esEvaluacionFinal());
        assertTrue(segunda().getAutoevaluacionesPublicadas().contains(creada));
    }

    @Test
    @DisplayName("CU-65: una autoevaluación combina pools de su tema y de cualquier otro tema del curso (repaso o evaluación final)")
    void poolsDeTemasAnteriores() {
        // El tema 3 (la última) puede usar los pools de los tres temas
        Autoevaluacion final_ = autoevaluacionService.registrar(fausto(), tercera().getIdTema(),
                solicitud("Evaluación final", pool("Preguntas del tema"), pool("Renta fija"), pool("Marco regulatorio")));
        assertTrue(final_.esEvaluacionFinal());
        assertEquals(3, final_.getPoolsActivos().size());

        // Un tema intermedia también puede repasar: el tema 2 usa el pool de la 1
        Autoevaluacion repaso = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("Repaso de los temas 1 y 2", pool("Renta fija"), pool("Marco regulatorio")));
        assertTrue(repaso.esEvaluacionFinal());

        // También se pueden usar pools de otros temas del curso, sin importar el orden
        Autoevaluacion conPoolDeOtroTema = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("Con pool de otro tema", pool("Renta fija"), pool("Preguntas del tema")));
        assertEquals(2, conPoolDeOtroTema.getPoolsActivos().size());

        // Y la lista de temas que pueden usar un pool son todos los del curso
        List<Tema> usanElPoolDeLaTema1 = autoevaluacionService.temasQuePuedenUsar(pool("Marco regulatorio"));
        assertEquals(List.of(primera(), segunda(), tercera()), usanElPoolDeLaTema1);
        assertEquals(List.of(primera(), segunda(), tercera()), autoevaluacionService.temasQuePuedenUsar(pool("Preguntas del tema")));
    }

    @Test
    @DisplayName("CU-65: valida los campos obligatorios, los valores enteros, las fechas y las preguntas disponibles")
    void validacionesDelAlta() {
        int tema = segunda().getIdTema();
        Pool renta = pool("Renta fija");
        var base = solicitud("X", renta);
        List<Integer> pools = base.idsPools();

        assertError("nombre", () -> autoevaluacionService.registrar(fausto(), tema, solicitud(" ", renta)));
        assertError("al menos un pool", () -> autoevaluacionService.registrar(fausto(), tema, solicitud("X")));
        assertError("tiempo límite", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "", "3", "6", "", APERTURA, "", false, false, false, false, pools)));
        assertError("entero mayor a cero", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "0", "3", "6", "", APERTURA, "", false, false, false, false, pools)));
        assertError("entero mayor a cero", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "tres", "6", "", APERTURA, "", false, false, false, false, pools)));
        assertError("no puede ser mayor a 10", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "3", "11", "", APERTURA, "", false, false, false, false, pools)));
        assertError("intentos permitidos", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "3", "6", "-1", APERTURA, "", false, false, false, false, pools)));
        assertError("posterior a la fecha de apertura", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "3", "6", "", APERTURA, "2025-12-31T09:00", false, false, false,
                        false, pools)));
        assertError("fecha de apertura", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "3", "6", "", "", "", false, false, false, false, pools)));
        assertError("reúnen 6 preguntas", () -> autoevaluacionService.registrar(fausto(), tema,
                new SolicitudAutoevaluacion("X", "60", "7", "6", "", APERTURA, "", false, false, false, false, pools)));
        assertError("no se encuentra activa", () -> autoevaluacionService.registrar(fausto(), 999999, base));
        assertError("No participás", () -> autoevaluacionService.registrar(
                docente("sebastian.bordato@idoneos.online"), tema("Macroeconomía de Coyuntura", "Tipo de cambio").getIdTema(),
                solicitud("X", pool("Preguntas del tema"))));
    }

    @Test
    @DisplayName("CU-66: sin intentos se modifican todos los datos, incluidos los pools")
    void modificarSinIntentos() {
        Autoevaluacion creada = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("Para modificar", pool("Renta fija")));

        autoevaluacionService.modificar(creada.getIdAutoevaluacion(), fausto(),
                new SolicitudAutoevaluacion("Modificada", "45", "4", "7", "2", APERTURA, "2027-01-01T10:00", true, false,
                        false, true, List.of(pool("Renta fija").getIdPool())));

        assertEquals("Modificada", creada.getNombre());
        assertEquals(45, creada.getTiempoLimite());
        assertEquals(4, creada.getCantidadPreguntas());
        assertEquals(7f, creada.getNotaMinima());
        assertEquals(2, creada.getIntentosPermitidos());
        assertEquals(LocalDateTime.of(2027, 1, 1, 10, 0), creada.getFechaCierre());
        assertTrue(creada.getDescuentoPorError());
        assertTrue(creada.getOculto());
        assertTrue(creada.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-66: con intentos solo se extiende el cierre, se oculta o se amplían los intentos")
    void modificarConIntentos() {
        Autoevaluacion primera = autoevaluacion("Autoevaluación Tema 1: Marco regulatorio");
        assertTrue(primera.tieneIntentosActivos());
        assertEquals(3, primera.getIntentosPermitidos());

        assertError("ya registra intentos", () -> autoevaluacionService.modificar(primera.getIdAutoevaluacion(),
                fausto(), conCambios(primera, "Otro nombre", "3", "", false)));
        assertError("solo puede ampliarse", () -> autoevaluacionService.modificar(
                primera.getIdAutoevaluacion(), fausto(), conCambios(primera, primera.getNombre(), "2", "", false)));
        assertError("solo puede extenderse", () -> autoevaluacionService.modificar(
                primera.getIdAutoevaluacion(), fausto(), conCambios(primera, primera.getNombre(), "3", "2027-01-01T10:00", false)));

        // Lo permitido: ampliar los intentos y quedar sin límite
        autoevaluacionService.modificar(primera.getIdAutoevaluacion(), fausto(),
                conCambios(primera, primera.getNombre(), "5", "", false));
        assertEquals(5, primera.getIntentosPermitidos());
        autoevaluacionService.modificar(primera.getIdAutoevaluacion(), fausto(),
                conCambios(primera, primera.getNombre(), "", "", false));
        assertNull(primera.getIntentosPermitidos());
    }

    @Test
    @DisplayName("CU-66 y CU-67: no se oculta ni se da de baja la última autoevaluación visible de un tema con cohortes en curso")
    void ultimaAutoevaluacionVisible() {
        Autoevaluacion unica = autoevaluacion("Autoevaluación del tema 3");
        assertTrue(tercera().tieneCohortesActivas());

        assertError("única autoevaluación visible", () -> autoevaluacionService.modificar(unica.getIdAutoevaluacion(),
                fausto(), conCambios(unica, unica.getNombre(), "3", "", true)));
        assertError("única autoevaluación visible", () -> autoevaluacionService.darDeBaja(unica.getIdAutoevaluacion(), fausto()));
        assertFalse(unica.getOculto());
        assertFalse(unica.esInactivo());

        // Con otra visible en el tema, sí se puede
        Autoevaluacion otra = autoevaluacionService.registrar(fausto(), tercera().getIdTema(),
                solicitud("Otra del tema 3", pool("Preguntas del tema")));
        autoevaluacionService.modificar(unica.getIdAutoevaluacion(), fausto(),
                conCambios(unica, unica.getNombre(), "3", "", true));
        assertTrue(unica.getOculto());
        assertError("única autoevaluación visible", () -> autoevaluacionService.darDeBaja(otra.getIdAutoevaluacion(), fausto()));
    }

    @Test
    @DisplayName("CU-67: no se da de baja una autoevaluación con intentos; la baja masiva es todo o nada")
    void darDeBaja() {
        Autoevaluacion conIntentos = autoevaluacion("Autoevaluación Tema 1: Marco regulatorio");
        Autoevaluacion libre = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("Libre", pool("Renta fija")));

        assertError("tiene 3 intentos registrados", () -> autoevaluacionService.darDeBaja(
                conIntentos.getIdAutoevaluacion(), null));
        assertError("al menos una", () -> autoevaluacionService.darDeBajaVarias(List.of(), fausto()));
        assertError("intentos registrados", () -> autoevaluacionService.darDeBajaVarias(
                List.of(libre.getIdAutoevaluacion(), conIntentos.getIdAutoevaluacion()), fausto()));
        assertFalse(libre.esInactivo()); // ninguna se dio de baja

        autoevaluacionService.darDeBaja(libre.getIdAutoevaluacion(), fausto());
        assertTrue(libre.esInactivo());
        assertError("no se encuentra activa", () -> autoevaluacionService.darDeBaja(libre.getIdAutoevaluacion(), fausto()));
    }

    @Test
    @DisplayName("CU-66 y CU-67: al ocultar o dar de baja una autoevaluación se completa el tema de quien aprobó las demás")
    void completaLaTemaAlQuitarUnaAutoevaluacion() {
        Inscripcion valentina = inscripcionRepository.findAll().stream()
                .filter(i -> i.getAlumno().getUsuario().getCorreo().equals("valentina.ruiz@correo.com")
                        && i.getCurso().getNombre().equals("Mercado de Capitales Argentino"))
                .findFirst().orElseThrow();
        Tema segunda = segunda();
        Autoevaluacion rendimiento = autoevaluacion("Autoevaluación Tema 2: Ejercicios de rendimiento");
        Autoevaluacion nueva = autoevaluacionService.registrar(fausto(), segunda.getIdTema(),
                solicitud("Segunda del tema 2", pool("Renta fija")));
        assertFalse(valentina.estaTemaCompletada(segunda));

        aprobar(valentina, rendimiento);
        assertFalse(valentina.estaTemaCompletada(segunda)); // falta aprobar la nueva

        autoevaluacionService.modificar(nueva.getIdAutoevaluacion(), fausto(),
                conCambios(nueva, nueva.getNombre(), "", "", true));

        assertTrue(valentina.estaTemaCompletada(segunda));
    }

    private void aprobar(Inscripcion inscripcion, Autoevaluacion autoevaluacion) {
        IntentoAutoevaluacion intento = new IntentoAutoevaluacion(inscripcion, autoevaluacion);
        intento.setNota(10f);
        intento.setAprobado(true);
        intento.setFechaEntrega(LocalDateTime.now());
        intento = intentoRepository.save(intento);
        autoevaluacion.getIntentosAutoevaluacion().add(intento);
        inscripcion.getIntentosAutoevaluacion().add(intento);
    }

    @Test
    @DisplayName("CU-64: busca las autoevaluaciones de un curso por tema y nombre; las dadas de baja van al final")
    void buscarAutoevaluaciones() {
        int curso = primera().getCurso().getIdCurso();
        Autoevaluacion libre = autoevaluacionService.registrar(fausto(), segunda().getIdTema(),
                solicitud("Para buscar", pool("Renta fija")));
        autoevaluacionService.darDeBaja(libre.getIdAutoevaluacion(), fausto());

        List<Autoevaluacion> vigentes = autoevaluacionService.buscarConFiltros(curso, null, null, false, fausto());
        assertTrue(vigentes.stream().noneMatch(Autoevaluacion::esInactivo));
        assertEquals(1, autoevaluacionService.buscarConFiltros(curso, primera().getIdTema(), "marco", false, null).size());

        List<Autoevaluacion> todas = autoevaluacionService.buscarConFiltros(curso, null, null, null, null);
        assertTrue(todas.get(todas.size() - 1).esInactivo());
        assertTrue(autoevaluacionService.buscarConFiltros(null, null, null, false,
                docente("sebastian.bordato@idoneos.online")).stream()
                .noneMatch(a -> a.getTema().getCurso().getNombre().equals("Macroeconomía de Coyuntura")));
    }
}
