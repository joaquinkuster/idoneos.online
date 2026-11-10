package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Pregunta;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.pool.PoolService;
import com.app.idoneos.service.pool.PoolService.SolicitudOpcion;
import com.app.idoneos.service.pool.PoolService.SolicitudPregunta;

/** Pruebas del ABM de pools con los datos de la semilla. */
@SpringBootTest
@Transactional
class PoolServiceTest {

    @Autowired
    private PoolService poolService;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

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

    private Tema terceraDeMercado() {
        return tema("Mercado de Capitales Argentino", "Renta variable y opciones");
    }

    private Pool pool(String nombre) {
        return poolService.buscarConFiltros(null, null, nombre, false, null).stream()
                .filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    /** Pregunta de la solicitud. Las opciones correctas llevan un asterisco inicial. */
    private SolicitudPregunta pregunta(String texto, boolean multiple, String... opciones) {
        return new SolicitudPregunta(null, texto, multiple, java.util.Arrays.stream(opciones)
                .map(o -> new SolicitudOpcion(null, o.startsWith("*") ? o.substring(1) : o, o.startsWith("*"))).toList());
    }

    private List<SolicitudPregunta> dosPreguntas() {
        return List.of(pregunta("¿Cuál es la capital?", false, "*Buenos Aires", "Córdoba", "Rosario"),
                pregunta("¿Cuáles son monedas?", true, "*Peso", "*Dólar", "Acción"));
    }

    private Pool crear(String nombre) {
        return poolService.registrar(fausto(), terceraDeMercado().getIdTema(), nombre, dosPreguntas());
    }

    private void assertError(String fragmento, Runnable accion) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, accion::run);
        assertTrue(e.getMessage().contains(fragmento), e.getMessage());
    }

    @Test
    @DisplayName("CU-61: crea un pool con sus preguntas y opciones de los tres tipos")
    void crearPool() {
        Pool pool = poolService.registrar(fausto(), terceraDeMercado().getIdTema(), "  Opciones  ", List.of(
                pregunta("Única", false, "*A", "B"), pregunta("Múltiple", true, "*A", "*B", "C"),
                pregunta("Verdadero o falso", false, "*Verdadero", "Falso")));

        assertEquals("Opciones", pool.getNombre());
        assertEquals(3, pool.getCantidadPreguntas());
        assertTrue(pool.getPreguntasActivas().stream().allMatch(Pregunta::esValida));
        assertEquals(1, pool.getPreguntasActivas().get(1).getOpcionesActivas().stream().filter(o -> !o.getEsCorrecta()).count());
        assertTrue(poolService.buscarPorTema(terceraDeMercado()).stream().anyMatch(p -> p.getNombre().equals("Opciones")));
    }

    @Test
    @DisplayName("CU-61: rechaza un pool sin nombre, repetido, sin preguntas o con preguntas mal armadas")
    void validacionesDelAlta() {
        int tema = terceraDeMercado().getIdTema();
        assertError("nombre", () -> poolService.registrar(fausto(), tema, " ", dosPreguntas()));
        assertError("Ya existe un pool", () -> poolService.registrar(fausto(), tema, "preguntas del tema", dosPreguntas()));
        assertError("al menos una pregunta", () -> poolService.registrar(fausto(), tema, "Vacío", List.of()));
        assertError("no tiene enunciado", () -> poolService.registrar(fausto(), tema, "Sin enunciado",
                List.of(pregunta(" ", false, "*A", "B"))));
        assertError("al menos dos opciones", () -> poolService.registrar(fausto(), tema, "Una opción",
                List.of(pregunta("P", false, "*A"))));
        assertError("exactamente una opción correcta", () -> poolService.registrar(fausto(), tema, "Dos correctas",
                List.of(pregunta("P", false, "*A", "*B"))));
        assertError("al menos una opción correcta", () -> poolService.registrar(fausto(), tema, "Sin correcta",
                List.of(pregunta("P", true, "A", "B"))));
        assertError("opción sin texto", () -> poolService.registrar(fausto(), tema, "Opción vacía",
                List.of(pregunta("P", false, "*A", " "))));
        assertError("no se encuentra activa", () -> poolService.registrar(fausto(), 999999, "X", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-61: solo el docente que participa en el curso puede crear pools")
    void soloElDocenteDelCurso() {
        Tema macro = tema("Macroeconomía de Coyuntura", "Tipo de cambio");
        Docente sebastian = docente("sebastian.bordato@idoneos.online");

        assertError("No participás", () -> poolService.registrar(sebastian, macro.getIdTema(), "X", dosPreguntas()));
        assertError("rol de docente", () -> poolService.registrar(null, macro.getIdTema(), "X", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-62: modifica el nombre, edita, quita y agrega preguntas y opciones conservando lo que no cambió")
    void modificar() {
        Pool pool = crear("Para modificar");
        Pregunta primera = pool.getPreguntasActivas().get(0);
        Pregunta segunda = pool.getPreguntasActivas().get(1);
        int idPrimera = primera.getIdPregunta();
        var opcionesPrimera = primera.getOpcionesActivas();

        poolService.modificar(pool.getIdPool(), fausto(), "Modificado", List.of(
                new SolicitudPregunta(idPrimera, "Enunciado nuevo", false, List.of(
                        new SolicitudOpcion(opcionesPrimera.get(0).getIdOpcionRespuesta(), "Buenos Aires", true),
                        new SolicitudOpcion(opcionesPrimera.get(1).getIdOpcionRespuesta(), "Córdoba", false),
                        new SolicitudOpcion(null, "Mendoza", false))),
                pregunta("Pregunta agregada", false, "*Sí", "No")));

        assertEquals("Modificado", pool.getNombre());
        assertEquals(2, pool.getCantidadPreguntas());
        assertEquals("Enunciado nuevo", primera.getTexto());
        assertEquals(idPrimera, pool.getPreguntasActivas().get(0).getIdPregunta()); // se conserva
        assertEquals(3, primera.getOpcionesActivas().size()); // una opción quitada, una agregada
        assertTrue(segunda.esInactivo()); // la que no figura se da de baja
        assertFalse(pool.getPreguntasActivas().contains(segunda));
        assertTrue(pool.getUltimaModificacion() != null);
    }

    @Test
    @DisplayName("CU-62: el contenido no puede modificarse si el pool tiene intentos asociados")
    void modificarConIntentos() {
        Pool marcoRegulatorio = pool("Marco regulatorio");

        assertTrue(marcoRegulatorio.tieneIntentosActivos());
        assertError("tiene intentos de autoevaluación asociados", () -> poolService.modificar(marcoRegulatorio.getIdPool(), fausto(),
                "Marco regulatorio", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-62: el pool debe conservar las preguntas que necesita la autoevaluación que lo usa")
    void modificarConAutoevaluacion() {
        Pool rentaFija = pool("Renta fija");
        assertEquals(6, rentaFija.getCantidadPreguntas());
        assertFalse(rentaFija.tieneIntentosActivos());

        List<SolicitudPregunta> cuatro = rentaFija.getPreguntasActivas().stream().limit(4).map(this::comoSolicitud).toList();
        assertError("necesita la autoevaluación", () -> poolService.modificar(rentaFija.getIdPool(), fausto(),
                "Renta fija", cuatro));

        List<SolicitudPregunta> cinco = rentaFija.getPreguntasActivas().stream().limit(5).map(this::comoSolicitud).toList();
        poolService.modificar(rentaFija.getIdPool(), fausto(), "Renta fija", cinco);
        assertEquals(5, rentaFija.getCantidadPreguntas());
    }

    private SolicitudPregunta comoSolicitud(Pregunta pregunta) {
        return new SolicitudPregunta(pregunta.getIdPregunta(), pregunta.getTexto(), pregunta.getEsOpcionMultiple(),
                pregunta.getOpcionesActivas().stream()
                        .map(o -> new SolicitudOpcion(o.getIdOpcionRespuesta(), o.getTexto(), o.getEsCorrecta())).toList());
    }

    @Test
    @DisplayName("CU-63: da de baja un pool sin autoevaluaciones y no el que usa una autoevaluación activa")
    void darDeBaja() {
        Pool libre = crear("Libre");
        poolService.darDeBaja(libre.getIdPool(), fausto());
        assertTrue(libre.esInactivo());
        assertError("no se encuentra activo", () -> poolService.darDeBaja(libre.getIdPool(), fausto()));

        Pool enUso = pool("Renta fija");
        assertError("está asociado a 1 autoevaluación activa", () -> poolService.darDeBaja(enUso.getIdPool(), null));
        assertFalse(enUso.esInactivo());
    }

    @Test
    @DisplayName("La baja masiva es todo o nada")
    void darDeBajaVarios() {
        Pool libre = crear("Libre");
        Pool enUso = pool("Renta fija");

        assertError("al menos un pool", () -> poolService.darDeBajaVarios(List.of(), fausto()));
        assertError("autoevaluación", () -> poolService.darDeBajaVarios(List.of(libre.getIdPool(), enUso.getIdPool()), fausto()));
        assertFalse(libre.esInactivo()); // ninguno se dio de baja

        poolService.darDeBajaVarios(List.of(libre.getIdPool()), null);
        assertTrue(libre.esInactivo());
    }

    @Test
    @DisplayName("CU-60: busca los pools de un curso por tema y nombre; los dados de baja van al final")
    void buscarPools() {
        int curso = terceraDeMercado().getCurso().getIdCurso();
        Pool nuevo = crear("Opciones de venta");
        Pool baja = crear("Opciones de compra");
        poolService.darDeBaja(baja.getIdPool(), fausto());

        List<Pool> activos = poolService.buscarConFiltros(curso, null, null, false, fausto());
        assertTrue(activos.stream().noneMatch(Pool::esInactivo));
        assertTrue(activos.contains(nuevo));

        List<Pool> todos = poolService.buscarConFiltros(curso, null, null, null, null);
        assertTrue(todos.get(todos.size() - 1).esInactivo());

        assertEquals(1, poolService.buscarConFiltros(curso, terceraDeMercado().getIdTema(), "venta", false, null).size());
        assertTrue(poolService.buscarConFiltros(null, null, null, null, docente("sebastian.bordato@idoneos.online")).stream()
                .noneMatch(p -> p.getTema().getCurso().getNombre().equals("Macroeconomía de Coyuntura")));
    }
}
