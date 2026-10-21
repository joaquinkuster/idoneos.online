package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.repositorio.UsuarioRepositorio;
import com.app.idoneos.servicio.TerminoGlosario.TerminoGlosarioServicio;

/**
 * Pruebas del glosario (CU-32 a CU-35) con los datos de la semilla: la unidad 1 de "Mercado de Capitales Argentino"
 * tiene los términos BYMA, CNV y ON; la 2, TIR y Duration; la 3 no tiene ninguno. Fausto Spotorno es titular de
 * ambos cursos y Sebastián Bordato no participa en "Macroeconomía de Coyuntura".
 */
@SpringBootTest
@Transactional
class TerminoGlosarioServicioTest {

    @Autowired
    private TerminoGlosarioServicio glosarioServicio;

    @Autowired
    private TerminoGlosarioRepositorio terminoRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private Unidad primera() {
        return unidad("Mercado de Capitales Argentino", "Introducción al mercado de capitales");
    }

    private Unidad segunda() {
        return unidad("Mercado de Capitales Argentino", "Renta fija y bonos");
    }

    private Unidad tercera() {
        return unidad("Mercado de Capitales Argentino", "Renta variable y opciones");
    }

    private Usuario usuario(String correo) {
        return usuarioRepositorio.findAll().stream().filter(u -> u.getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    private List<String> nombres(List<TerminoGlosario> terminos) {
        return terminos.stream().map(TerminoGlosario::getTermino).toList();
    }

    // ------------------------------------------------------------------ CU-32 Buscar término

    @Test
    @DisplayName("CU-32: sin texto se devuelven todos los términos de la unidad, en orden alfabético")
    void buscaTodos() {
        assertEquals(List.of("BYMA", "CNV", "ON"), nombres(glosarioServicio.buscarConFiltros(primera(), null, true)));
        assertEquals(List.of("Duration", "TIR"), nombres(glosarioServicio.buscarConFiltros(segunda(), "  ", true)));
        assertTrue(glosarioServicio.buscarConFiltros(tercera(), null, true).isEmpty());
    }

    @Test
    @DisplayName("CU-32: la casilla decide si se busca solo en el término o también en la definición")
    void buscaEnTerminosYDefiniciones() {
        // "mercado" figura en las definiciones de BYMA y CNV, pero en ningún término
        assertEquals(List.of("BYMA", "CNV"), nombres(glosarioServicio.buscarConFiltros(primera(), "mercado", true)));
        assertTrue(glosarioServicio.buscarConFiltros(primera(), "mercado", false).isEmpty());
        // un texto que está en el término se encuentra en ambos modos
        assertEquals(List.of("CNV"), nombres(glosarioServicio.buscarConFiltros(primera(), "cnv", false)));
    }

    @Test
    @DisplayName("CU-32: la búsqueda no distingue mayúsculas ni tildes")
    void buscaSinTildesNiMayusculas() {
        assertEquals(List.of("CNV"), nombres(glosarioServicio.buscarConFiltros(primera(), "COMISION", true)));
        assertEquals(List.of("ON"), nombres(glosarioServicio.buscarConFiltros(primera(), "obligación", true)));
    }

    @Test
    @DisplayName("CU-32: los términos con tilde se ordenan según su letra, no al final")
    void ordenaSinTildes() {
        glosarioServicio.registrarTermino(fausto(), primera().getIdUnidad(), "Álgebra", "Definición");
        glosarioServicio.registrarTermino(fausto(), primera().getIdUnidad(), "Zeta", "Definición");

        assertEquals(List.of("Álgebra", "BYMA", "CNV", "ON", "Zeta"),
                nombres(glosarioServicio.buscarConFiltros(primera(), null, true)));
    }

    // ------------------------------------------------------------------ CU-33 Registrar término

    @Test
    @DisplayName("CU-33: el docente del curso registra un término con su definición")
    void registra() {
        TerminoGlosario nuevo = glosarioServicio.registrarTermino(fausto(), tercera().getIdUnidad(), "  Opción  ",
                " Derecho a comprar o vender un activo a un precio fijado. ");

        assertTrue(nuevo.getIdTermino() > 0);
        assertEquals("Opción", nuevo.getTermino());
        assertEquals("Derecho a comprar o vender un activo a un precio fijado.", nuevo.getDefinicion());
        assertFalse(nuevo.getBaja());
        assertEquals(List.of("Opción"), nombres(glosarioServicio.buscarConFiltros(tercera(), null, true)));
    }

    @Test
    @DisplayName("CU-33: faltan datos, superan el máximo, el término ya existe o el docente no participa")
    void validaAlRegistrar() {
        int unidad = primera().getIdUnidad();
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), unidad, "", "Definición"))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), unidad, "Término", " "))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), unidad, "x".repeat(51), "Definición"))
                .contains("Máximo: 50 caracteres"));
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), unidad, "Término", "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        // el máximo exacto se admite
        assertNotNull(glosarioServicio.registrarTermino(fausto(), unidad, "x".repeat(50), "y".repeat(500)));

        // repetido en la misma unidad, sin distinguir mayúsculas
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), unidad, "byma", "Otra definición"))
                .contains("El término ya está registrado en el glosario de esta unidad."));
        // pero se puede repetir en otra unidad
        assertNotNull(glosarioServicio.registrarTermino(fausto(), segunda().getIdUnidad(), "BYMA", "Definición"));

        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(fausto(), null, "Término", "Definición"))
                .contains("La unidad no se encuentra activa"));
        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(null, unidad, "Término", "Definición"))
                .contains("no tiene el rol de docente"));
    }

    @Test
    @DisplayName("CU-33: un docente que no participa en el curso no puede registrar términos")
    void docenteAjenoNoRegistra() {
        Unidad ajena = unidad("Macroeconomía de Coyuntura", "Inflación y precios");

        assertTrue(mensaje(() -> glosarioServicio.registrarTermino(docente("sebastian.bordato@idoneos.online"),
                ajena.getIdUnidad(), "IPC", "Índice de precios al consumidor.")).contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-33: un término dado de baja se vuelve a dar de alta con la nueva definición")
    void reactivaElTerminoDadoDeBaja() {
        TerminoGlosario byma = terminoRepositorio.findByUnidad(primera()).stream()
                .filter(t -> t.getTermino().equals("BYMA")).findFirst().orElseThrow();
        glosarioServicio.darDeBajaTermino(byma.getIdTermino(), fausto());
        assertEquals(List.of("CNV", "ON"), nombres(glosarioServicio.buscarConFiltros(primera(), null, true)));

        TerminoGlosario vuelto = glosarioServicio.registrarTermino(fausto(), primera().getIdUnidad(), "BYMA",
                "Nueva definición");

        assertEquals(byma.getIdTermino(), vuelto.getIdTermino());
        assertFalse(vuelto.getBaja());
        assertEquals("Nueva definición", vuelto.getDefinicion());
        assertEquals(List.of("BYMA", "CNV", "ON"), nombres(glosarioServicio.buscarConFiltros(primera(), null, true)));
    }

    // ------------------------------------------------------------------ CU-34 Modificar término

    @Test
    @DisplayName("CU-34: el docente modifica el término y la definición, y queda registrada la fecha")
    void modifica() {
        TerminoGlosario tir = terminoRepositorio.findByUnidad(segunda()).stream()
                .filter(t -> t.getTermino().equals("TIR")).findFirst().orElseThrow();

        TerminoGlosario modificado = glosarioServicio.modificarTermino(tir.getIdTermino(), fausto(), "TIR (tasa)",
                "Rendimiento anual.");

        assertEquals("TIR (tasa)", modificado.getTermino());
        assertEquals("Rendimiento anual.", modificado.getDefinicion());
        assertNotNull(modificado.getUltimaModificacion());
        // se puede guardar sin cambiar el término (la unicidad no cuenta contra sí mismo)
        assertNotNull(glosarioServicio.modificarTermino(tir.getIdTermino(), fausto(), "TIR (tasa)", "Otra."));
    }

    @Test
    @DisplayName("CU-34: valida los datos, rechaza un término repetido y exige participar en el curso")
    void validaAlModificar() {
        TerminoGlosario tir = terminoRepositorio.findByUnidad(segunda()).stream()
                .filter(t -> t.getTermino().equals("TIR")).findFirst().orElseThrow();
        int id = tir.getIdTermino();

        assertTrue(mensaje(() -> glosarioServicio.modificarTermino(id, fausto(), " ", "Definición"))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioServicio.modificarTermino(id, fausto(), "TIR", "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        assertTrue(mensaje(() -> glosarioServicio.modificarTermino(id, fausto(), "duration", "Definición"))
                .contains("El término ya está registrado en el glosario de esta unidad."));
        assertTrue(mensaje(() -> glosarioServicio.modificarTermino(999999, fausto(), "TIR", "Definición"))
                .contains("El término no se encuentra activo"));
    }

    @Test
    @DisplayName("CU-34 y CU-35: un docente que no participa en el curso no modifica ni da de baja sus términos")
    void docenteAjenoNoModificaNiDaDeBaja() {
        Unidad ajena = unidad("Macroeconomía de Coyuntura", "Inflación y precios");
        TerminoGlosario ipc = glosarioServicio.registrarTermino(fausto(), ajena.getIdUnidad(), "IPC",
                "Índice de precios al consumidor.");
        Docente sebastian = docente("sebastian.bordato@idoneos.online");

        assertTrue(mensaje(() -> glosarioServicio.modificarTermino(ipc.getIdTermino(), sebastian, "IPC", "Otra."))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> glosarioServicio.darDeBajaTermino(ipc.getIdTermino(), sebastian))
                .contains("No participás en este curso"));
        assertFalse(terminoRepositorio.findById(ipc.getIdTermino()).orElseThrow().getBaja());
    }

    // ------------------------------------------------------------------ CU-35 Dar de baja término

    @Test
    @DisplayName("CU-35: el docente da de baja un término (baja lógica) y el administrador también, sin participar")
    void daDeBaja() {
        TerminoGlosario cnv = terminoRepositorio.findByUnidad(primera()).stream()
                .filter(t -> t.getTermino().equals("CNV")).findFirst().orElseThrow();
        TerminoGlosario on = terminoRepositorio.findByUnidad(primera()).stream()
                .filter(t -> t.getTermino().equals("ON")).findFirst().orElseThrow();

        glosarioServicio.darDeBajaTermino(cnv.getIdTermino(), fausto());
        glosarioServicio.darDeBajaTermino(on.getIdTermino(), null);

        assertTrue(terminoRepositorio.findById(cnv.getIdTermino()).orElseThrow().getBaja());
        assertEquals(List.of("BYMA"), nombres(glosarioServicio.buscarConFiltros(primera(), null, true)));
        // ya no está activo
        assertTrue(mensaje(() -> glosarioServicio.darDeBajaTermino(cnv.getIdTermino(), fausto()))
                .contains("El término no se encuentra activo"));
    }

    // ------------------------------------------------------------------ Quién puede ver el glosario

    @Test
    @DisplayName("Ven el glosario el administrador, el docente del curso y el alumno con la unidad habilitada")
    void quienPuedeVer() {
        assertTrue(glosarioServicio.puedeVer(primera(), usuario("admin@idoneos.online")));
        assertTrue(glosarioServicio.puedeVer(primera(), usuario("fausto.spotorno@idoneos.online")));
        // Valentina tiene habilitadas las unidades 1 y 2, pero no la 3
        assertTrue(glosarioServicio.puedeVer(segunda(), usuario("valentina.ruiz@correo.com")));
        assertFalse(glosarioServicio.puedeVer(tercera(), usuario("valentina.ruiz@correo.com")));
        // el dictado de su cohorte todavía no empezó / no está inscripto en el curso
        assertFalse(glosarioServicio.puedeVer(segunda(), usuario("martin.gomez@correo.com")));
        assertFalse(glosarioServicio.puedeVer(segunda(), usuario("tomas.herrera@correo.com")));
        // Sebastián no participa en "Macroeconomía de Coyuntura"
        assertFalse(glosarioServicio.puedeVer(unidad("Macroeconomía de Coyuntura", "Inflación y precios"),
                usuario("sebastian.bordato@idoneos.online")));
    }
}
