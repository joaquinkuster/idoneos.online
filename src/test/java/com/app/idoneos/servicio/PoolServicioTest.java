package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.Pool.PoolServicio;
import com.app.idoneos.servicio.Pool.PoolServicio.SolicitudOpcion;
import com.app.idoneos.servicio.Pool.PoolServicio.SolicitudPregunta;

/**
 * Pruebas del ABM de pools (CU-60 a CU-63) con los datos de la semilla: en "Mercado de Capitales Argentino" (titular
 * Fausto Spotorno; Sebastián Bordato no participa en "Macroeconomía de Coyuntura") el pool "Marco regulatorio" de la
 * unidad 1 tiene intentos (los de Valentina) y el pool "Renta fija" de la unidad 2 tiene 6 preguntas y lo usa una
 * autoevaluación que sortea 5.
 */
@SpringBootTest
@Transactional
class PoolServicioTest {

    @Autowired
    private PoolServicio poolServicio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst().orElseThrow();
    }

    private Unidad terceraDeMercado() {
        return unidad("Mercado de Capitales Argentino", "Renta variable y opciones");
    }

    private Pool pool(String nombre) {
        return poolServicio.buscarConFiltros(null, null, nombre, false, null).stream()
                .filter(p -> p.getNombre().equals(nombre)).findFirst().orElseThrow();
    }

    /** Pregunta de la solicitud; las opciones correctas llevan un asterisco inicial. */
    private SolicitudPregunta pregunta(String texto, boolean multiple, String... opciones) {
        return new SolicitudPregunta(null, texto, multiple, java.util.Arrays.stream(opciones)
                .map(o -> new SolicitudOpcion(null, o.startsWith("*") ? o.substring(1) : o, o.startsWith("*"))).toList());
    }

    private List<SolicitudPregunta> dosPreguntas() {
        return List.of(pregunta("¿Cuál es la capital?", false, "*Buenos Aires", "Córdoba", "Rosario"),
                pregunta("¿Cuáles son monedas?", true, "*Peso", "*Dólar", "Acción"));
    }

    private Pool crear(String nombre) {
        return poolServicio.registrarPool(fausto(), terceraDeMercado().getIdUnidad(), nombre, dosPreguntas());
    }

    private void assertError(String fragmento, Runnable accion) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, accion::run);
        assertTrue(e.getMessage().contains(fragmento), e.getMessage());
    }

    @Test
    @DisplayName("CU-61: crea un pool con sus preguntas y opciones de los tres tipos")
    void crearPool() {
        Pool pool = poolServicio.registrarPool(fausto(), terceraDeMercado().getIdUnidad(), "  Opciones  ", List.of(
                pregunta("Única", false, "*A", "B"), pregunta("Múltiple", true, "*A", "*B", "C"),
                pregunta("Verdadero o falso", false, "*Verdadero", "Falso")));

        assertEquals("Opciones", pool.getNombre());
        assertEquals(3, pool.getCantidadPreguntas());
        assertTrue(pool.getPreguntasActivas().stream().allMatch(Pregunta::esValida));
        assertEquals(1, pool.getPreguntasActivas().get(1).getOpcionesActivas().stream().filter(o -> !o.getEsCorrecta()).count());
        assertTrue(poolServicio.buscarPorUnidad(terceraDeMercado()).stream().anyMatch(p -> p.getNombre().equals("Opciones")));
    }

    @Test
    @DisplayName("CU-61: rechaza un pool sin nombre, repetido, sin preguntas o con preguntas mal armadas")
    void validacionesDelAlta() {
        int unidad = terceraDeMercado().getIdUnidad();
        assertError("nombre", () -> poolServicio.registrarPool(fausto(), unidad, " ", dosPreguntas()));
        assertError("Ya existe un pool", () -> poolServicio.registrarPool(fausto(), unidad, "preguntas de la unidad", dosPreguntas()));
        assertError("al menos una pregunta", () -> poolServicio.registrarPool(fausto(), unidad, "Vacío", List.of()));
        assertError("no tiene enunciado", () -> poolServicio.registrarPool(fausto(), unidad, "Sin enunciado",
                List.of(pregunta(" ", false, "*A", "B"))));
        assertError("al menos dos opciones", () -> poolServicio.registrarPool(fausto(), unidad, "Una opción",
                List.of(pregunta("P", false, "*A"))));
        assertError("exactamente una opción correcta", () -> poolServicio.registrarPool(fausto(), unidad, "Dos correctas",
                List.of(pregunta("P", false, "*A", "*B"))));
        assertError("al menos una opción correcta", () -> poolServicio.registrarPool(fausto(), unidad, "Sin correcta",
                List.of(pregunta("P", true, "A", "B"))));
        assertError("opción sin texto", () -> poolServicio.registrarPool(fausto(), unidad, "Opción vacía",
                List.of(pregunta("P", false, "*A", " "))));
        assertError("no se encuentra activa", () -> poolServicio.registrarPool(fausto(), 999999, "X", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-61: solo el docente que participa en el curso puede crear pools")
    void soloElDocenteDelCurso() {
        Unidad macro = unidad("Macroeconomía de Coyuntura", "Tipo de cambio");
        Docente sebastian = docente("sebastian.bordato@idoneos.online");

        assertError("No participás", () -> poolServicio.registrarPool(sebastian, macro.getIdUnidad(), "X", dosPreguntas()));
        assertError("rol de docente", () -> poolServicio.registrarPool(null, macro.getIdUnidad(), "X", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-62: modifica el nombre, edita, quita y agrega preguntas y opciones conservando lo que no cambió")
    void modificarPool() {
        Pool pool = crear("Para modificar");
        Pregunta primera = pool.getPreguntasActivas().get(0);
        Pregunta segunda = pool.getPreguntasActivas().get(1);
        int idPrimera = primera.getIdPregunta();
        var opcionesPrimera = primera.getOpcionesActivas();

        poolServicio.modificarPool(pool.getIdPool(), fausto(), "Modificado", List.of(
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
        assertError("ya tiene intentos", () -> poolServicio.modificarPool(marcoRegulatorio.getIdPool(), fausto(),
                "Marco regulatorio", dosPreguntas()));
    }

    @Test
    @DisplayName("CU-62: el pool debe conservar las preguntas que necesita la autoevaluación que lo usa")
    void modificarConAutoevaluacion() {
        Pool rentaFija = pool("Renta fija");
        assertEquals(6, rentaFija.getCantidadPreguntas());
        assertFalse(rentaFija.tieneIntentosActivos());

        List<SolicitudPregunta> cuatro = rentaFija.getPreguntasActivas().stream().limit(4).map(this::comoSolicitud).toList();
        assertError("necesita la autoevaluación", () -> poolServicio.modificarPool(rentaFija.getIdPool(), fausto(),
                "Renta fija", cuatro));

        List<SolicitudPregunta> cinco = rentaFija.getPreguntasActivas().stream().limit(5).map(this::comoSolicitud).toList();
        poolServicio.modificarPool(rentaFija.getIdPool(), fausto(), "Renta fija", cinco);
        assertEquals(5, rentaFija.getCantidadPreguntas());
    }

    private SolicitudPregunta comoSolicitud(Pregunta pregunta) {
        return new SolicitudPregunta(pregunta.getIdPregunta(), pregunta.getTexto(), pregunta.getEsOpcionMultiple(),
                pregunta.getOpcionesActivas().stream()
                        .map(o -> new SolicitudOpcion(o.getIdOpcionRespuesta(), o.getTexto(), o.getEsCorrecta())).toList());
    }

    @Test
    @DisplayName("CU-63: da de baja un pool sin autoevaluaciones y no el que usa una autoevaluación activa")
    void darDeBajaPool() {
        Pool libre = crear("Libre");
        poolServicio.darDeBajaPool(libre.getIdPool(), fausto());
        assertTrue(libre.esInactivo());
        assertError("no se encuentra activo", () -> poolServicio.darDeBajaPool(libre.getIdPool(), fausto()));

        Pool enUso = pool("Renta fija");
        assertError("está asociado a 1 autoevaluación activa", () -> poolServicio.darDeBajaPool(enUso.getIdPool(), null));
        assertFalse(enUso.esInactivo());
    }

    @Test
    @DisplayName("La baja masiva es todo o nada")
    void darDeBajaVarios() {
        Pool libre = crear("Libre");
        Pool enUso = pool("Renta fija");

        assertError("al menos un pool", () -> poolServicio.darDeBajaVarios(List.of(), fausto()));
        assertError("autoevaluación", () -> poolServicio.darDeBajaVarios(List.of(libre.getIdPool(), enUso.getIdPool()), fausto()));
        assertFalse(libre.esInactivo()); // ninguno se dio de baja

        poolServicio.darDeBajaVarios(List.of(libre.getIdPool()), null);
        assertTrue(libre.esInactivo());
    }

    @Test
    @DisplayName("CU-60: busca los pools de un curso por unidad y nombre; los dados de baja van al final")
    void buscarPools() {
        int curso = terceraDeMercado().getCurso().getIdCurso();
        Pool nuevo = crear("Opciones de venta");
        Pool baja = crear("Opciones de compra");
        poolServicio.darDeBajaPool(baja.getIdPool(), fausto());

        List<Pool> activos = poolServicio.buscarConFiltros(curso, null, null, false, fausto());
        assertTrue(activos.stream().noneMatch(Pool::esInactivo));
        assertTrue(activos.contains(nuevo));

        List<Pool> todos = poolServicio.buscarConFiltros(curso, null, null, null, null);
        assertTrue(todos.get(todos.size() - 1).esInactivo());

        assertEquals(1, poolServicio.buscarConFiltros(curso, terceraDeMercado().getIdUnidad(), "venta", false, null).size());
        assertTrue(poolServicio.buscarConFiltros(null, null, null, null, docente("sebastian.bordato@idoneos.online")).stream()
                .noneMatch(p -> p.getUnidad().getCurso().getNombre().equals("Macroeconomía de Coyuntura")));
    }
}
