package com.app.idoneos.service;

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

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.TerminoGlosarioRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.repository.UsuarioRepository;
import com.app.idoneos.service.terminoglosario.TerminoGlosarioService;

/** Pruebas del glosario con los datos de la semilla. */
@SpringBootTest
@Transactional
class TerminoGlosarioServiceTest {

    @Autowired
    private TerminoGlosarioService glosarioService;

    @Autowired
    private TerminoGlosarioRepository terminoRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Tema tema(String curso, String titulo) {
        return temaRepository.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
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

    private Usuario usuario(String correo) {
        return usuarioRepository.findAll().stream().filter(u -> u.getCorreo().equals(correo)).findFirst().orElseThrow();
    }

    private String mensaje(Executable accion) {
        return assertThrows(IllegalArgumentException.class, accion).getMessage();
    }

    private List<String> nombres(List<TerminoGlosario> terminos) {
        return terminos.stream().map(TerminoGlosario::getTermino).toList();
    }

    // ----------------------------------------------------------------------
    // Buscar término
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-32: sin texto se devuelven todos los términos del tema, en orden alfabético")
    void buscaTodos() {
        assertEquals(List.of("BYMA", "CNV", "ON"), nombres(glosarioService.buscarConFiltros(primera(), null, true)));
        assertEquals(List.of("Duration", "TIR"), nombres(glosarioService.buscarConFiltros(segunda(), "  ", true)));
        assertTrue(glosarioService.buscarConFiltros(tercera(), null, true).isEmpty());
    }

    @Test
    @DisplayName("CU-32: la casilla decide si se busca solo en el término o también en la definición")
    void buscaEnTerminosYDefiniciones() {
        // "mercado" figura en las definiciones de BYMA y CNV, pero en ningún término
        assertEquals(List.of("BYMA", "CNV"), nombres(glosarioService.buscarConFiltros(primera(), "mercado", true)));
        assertTrue(glosarioService.buscarConFiltros(primera(), "mercado", false).isEmpty());
        // un texto que está en el término se encuentra en ambos modos
        assertEquals(List.of("CNV"), nombres(glosarioService.buscarConFiltros(primera(), "cnv", false)));
    }

    @Test
    @DisplayName("CU-32: la búsqueda no distingue mayúsculas ni tildes")
    void buscaSinTildesNiMayusculas() {
        assertEquals(List.of("CNV"), nombres(glosarioService.buscarConFiltros(primera(), "COMISION", true)));
        assertEquals(List.of("ON"), nombres(glosarioService.buscarConFiltros(primera(), "obligación", true)));
    }

    @Test
    @DisplayName("CU-32: los términos con tilde se ordenan según su letra, no al final")
    void ordenaSinTildes() {
        glosarioService.registrar(fausto(), primera().getIdTema(), "Álgebra", "Definición");
        glosarioService.registrar(fausto(), primera().getIdTema(), "Zeta", "Definición");

        assertEquals(List.of("Álgebra", "BYMA", "CNV", "ON", "Zeta"),
                nombres(glosarioService.buscarConFiltros(primera(), null, true)));
    }

    // ----------------------------------------------------------------------
    // Registrar término
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-33: el docente del curso registra un término con su definición")
    void registra() {
        TerminoGlosario nuevo = glosarioService.registrar(fausto(), tercera().getIdTema(), "  Opción  ",
                " Derecho a comprar o vender un activo a un precio fijado. ");

        assertTrue(nuevo.getIdTermino() > 0);
        assertEquals("Opción", nuevo.getTermino());
        assertEquals("Derecho a comprar o vender un activo a un precio fijado.", nuevo.getDefinicion());
        assertFalse(nuevo.getBaja());
        assertEquals(List.of("Opción"), nombres(glosarioService.buscarConFiltros(tercera(), null, true)));
    }

    @Test
    @DisplayName("CU-33: faltan datos, superan el máximo, el término ya existe o el docente no participa")
    void validaAlRegistrar() {
        int tema = primera().getIdTema();
        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), tema, "", "Definición"))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), tema, "Término", " "))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), tema, "x".repeat(51), "Definición"))
                .contains("Máximo: 50 caracteres"));
        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), tema, "Término", "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        // el máximo exacto se admite
        assertNotNull(glosarioService.registrar(fausto(), tema, "x".repeat(50), "y".repeat(500)));

        // repetido en la mismo tema, sin distinguir mayúsculas
        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), tema, "byma", "Otra definición"))
                .contains("El término ya está registrado en el glosario de este tema."));
        // pero se puede repetir en otro tema
        assertNotNull(glosarioService.registrar(fausto(), segunda().getIdTema(), "BYMA", "Definición"));

        assertTrue(mensaje(() -> glosarioService.registrar(fausto(), null, "Término", "Definición"))
                .contains("El tema no se encuentra activa"));
        assertTrue(mensaje(() -> glosarioService.registrar(null, tema, "Término", "Definición"))
                .contains("no tiene el rol de docente"));
    }

    @Test
    @DisplayName("CU-33: un docente que no participa en el curso no puede registrar términos")
    void docenteAjenoNoRegistra() {
        Tema ajena = tema("Macroeconomía de Coyuntura", "Inflación y precios");

        assertTrue(mensaje(() -> glosarioService.registrar(docente("sebastian.bordato@idoneos.online"),
                ajena.getIdTema(), "IPC", "Índice de precios al consumidor.")).contains("No participás en este curso"));
    }

    @Test
    @DisplayName("CU-33: un término dado de baja se vuelve a dar de alta con la nueva definición")
    void reactivaElTerminoDadoDeBaja() {
        TerminoGlosario byma = terminoRepository.findByTema(primera()).stream()
                .filter(t -> t.getTermino().equals("BYMA")).findFirst().orElseThrow();
        glosarioService.darDeBaja(byma.getIdTermino(), fausto());
        assertEquals(List.of("CNV", "ON"), nombres(glosarioService.buscarConFiltros(primera(), null, true)));

        TerminoGlosario vuelto = glosarioService.registrar(fausto(), primera().getIdTema(), "BYMA",
                "Nueva definición");

        assertEquals(byma.getIdTermino(), vuelto.getIdTermino());
        assertFalse(vuelto.getBaja());
        assertEquals("Nueva definición", vuelto.getDefinicion());
        assertEquals(List.of("BYMA", "CNV", "ON"), nombres(glosarioService.buscarConFiltros(primera(), null, true)));
    }

    // ----------------------------------------------------------------------
    // Modificar término
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-34: el docente modifica el término y la definición, y queda registrada la fecha")
    void modifica() {
        TerminoGlosario tir = terminoRepository.findByTema(segunda()).stream()
                .filter(t -> t.getTermino().equals("TIR")).findFirst().orElseThrow();

        TerminoGlosario modificado = glosarioService.modificar(tir.getIdTermino(), fausto(), "TIR (tasa)",
                "Rendimiento anual.");

        assertEquals("TIR (tasa)", modificado.getTermino());
        assertEquals("Rendimiento anual.", modificado.getDefinicion());
        assertNotNull(modificado.getUltimaModificacion());
        // se puede guardar sin cambiar el término (la unicidad no cuenta contra sí mismo)
        assertNotNull(glosarioService.modificar(tir.getIdTermino(), fausto(), "TIR (tasa)", "Otra."));
    }

    @Test
    @DisplayName("CU-34: valida los datos, rechaza un término repetido y exige participar en el curso")
    void validaAlModificar() {
        TerminoGlosario tir = terminoRepository.findByTema(segunda()).stream()
                .filter(t -> t.getTermino().equals("TIR")).findFirst().orElseThrow();
        int id = tir.getIdTermino();

        assertTrue(mensaje(() -> glosarioService.modificar(id, fausto(), " ", "Definición"))
                .contains("Debe completar el término y la definición."));
        assertTrue(mensaje(() -> glosarioService.modificar(id, fausto(), "TIR", "x".repeat(501)))
                .contains("Máximo: 500 caracteres"));
        assertTrue(mensaje(() -> glosarioService.modificar(id, fausto(), "duration", "Definición"))
                .contains("El término ya está registrado en el glosario de este tema."));
        assertTrue(mensaje(() -> glosarioService.modificar(999999, fausto(), "TIR", "Definición"))
                .contains("El término no se encuentra activo"));
    }

    @Test
    @DisplayName("CU-34 y CU-35: un docente que no participa en el curso no modifica ni da de baja sus términos")
    void docenteAjenoNoModificaNiDaDeBaja() {
        Tema ajena = tema("Macroeconomía de Coyuntura", "Inflación y precios");
        TerminoGlosario ipc = glosarioService.registrar(fausto(), ajena.getIdTema(), "IPC",
                "Índice de precios al consumidor.");
        Docente sebastian = docente("sebastian.bordato@idoneos.online");

        assertTrue(mensaje(() -> glosarioService.modificar(ipc.getIdTermino(), sebastian, "IPC", "Otra."))
                .contains("No participás en este curso"));
        assertTrue(mensaje(() -> glosarioService.darDeBaja(ipc.getIdTermino(), sebastian))
                .contains("No participás en este curso"));
        assertFalse(terminoRepository.findById(ipc.getIdTermino()).orElseThrow().getBaja());
    }

    // ----------------------------------------------------------------------
    // Dar de baja término
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-35: el docente da de baja un término (baja lógica) y el administrador también, sin participar")
    void daDeBaja() {
        TerminoGlosario cnv = terminoRepository.findByTema(primera()).stream()
                .filter(t -> t.getTermino().equals("CNV")).findFirst().orElseThrow();
        TerminoGlosario on = terminoRepository.findByTema(primera()).stream()
                .filter(t -> t.getTermino().equals("ON")).findFirst().orElseThrow();

        glosarioService.darDeBaja(cnv.getIdTermino(), fausto());
        glosarioService.darDeBaja(on.getIdTermino(), null);

        assertTrue(terminoRepository.findById(cnv.getIdTermino()).orElseThrow().getBaja());
        assertEquals(List.of("BYMA"), nombres(glosarioService.buscarConFiltros(primera(), null, true)));
        // ya no está activo
        assertTrue(mensaje(() -> glosarioService.darDeBaja(cnv.getIdTermino(), fausto()))
                .contains("El término no se encuentra activo"));
    }

    // ----------------------------------------------------------------------
    // Quién puede ver el glosario
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("Ven el glosario el administrador, el docente del curso y el alumno con el tema habilitado")
    void quienPuedeVer() {
        assertTrue(glosarioService.puedeVer(primera(), usuario("admin@idoneos.online")));
        assertTrue(glosarioService.puedeVer(primera(), usuario("fausto.spotorno@idoneos.online")));
        // Valentina tiene habilitadas los temas 1 y 2, pero no la 3
        assertTrue(glosarioService.puedeVer(segunda(), usuario("valentina.ruiz@correo.com")));
        assertFalse(glosarioService.puedeVer(tercera(), usuario("valentina.ruiz@correo.com")));
        // el dictado de su cohorte todavía no empezó / no está inscripto en el curso
        assertFalse(glosarioService.puedeVer(segunda(), usuario("martin.gomez@correo.com")));
        assertFalse(glosarioService.puedeVer(segunda(), usuario("tomas.herrera@correo.com")));
        // Sebastián no participa en "Macroeconomía de Coyuntura"
        assertFalse(glosarioService.puedeVer(tema("Macroeconomía de Coyuntura", "Inflación y precios"),
                usuario("sebastian.bordato@idoneos.online")));
    }
}
