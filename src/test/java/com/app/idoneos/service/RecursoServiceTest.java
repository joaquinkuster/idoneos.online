package com.app.idoneos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.recurso.RecursoService;
import com.app.idoneos.service.recurso.RecursoService.DatosRecurso;

/** Pruebas del ABM de materiales. */
@SpringBootTest
@Transactional
class RecursoServiceTest {

    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };
    private static final byte[] WEBM = { 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Autowired
    private RecursoService recursoService;

    @Autowired
    private TemaRecursoRepository temaRecursoRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    private Docente docente(String correo) {
        return docenteRepository.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Tema temaDeRentaFija() {
        return temaRepository.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Mercado de Capitales Argentino")
                && u.getTitulo().equals("Renta fija y bonos")).findFirst().orElseThrow();
    }

    private MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, "application/octet-stream", contenido);
    }

    private DatosRecurso datos(String titulo, String autor, String enlace, String isbn) {
        return new DatosRecurso(titulo, null, false, autor, enlace, isbn, null, null, null, null);
    }

    private TemaRecurso subirGrabacion(String titulo) {
        return recursoService.subir(fausto(), temaDeRentaFija().getIdTema(), "GRABACION",
                datos(titulo, null, null, null), archivo("clase.mp4", MP4), true);
    }

    private String mensaje(Runnable accion) {
        return assertThrows(IllegalArgumentException.class, accion::run).getMessage();
    }

    // ----------------------------------------------------------------------
    // Subir material
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-29: sube una grabación como recurso del docente y la guarda en curso{id}/tema{id}/recurso{id}.mp4")
    void subeUnaGrabacion() {
        TemaRecurso material = subirGrabacion("Clase 3: Valuación de bonos");

        Recurso recurso = material.getRecurso();
        Tema tema = material.getTema();
        String esperada = "curso" + tema.getCurso().getIdCurso() + "/tema" + tema.getIdTema() + "/recurso"
                + recurso.getIdRecurso() + ".mp4";
        assertEquals(esperada, recurso.getRutaArchivo());
        assertTrue(Files.exists(directorioCursos.resolve(esperada)));
        assertEquals("Grabación", recurso.getTipo());
        assertEquals(fausto().getIdDocente(), recurso.getDocente().getIdDocente());
        assertNotNull(recurso.getFechaAceptacionTyc());
        assertNotNull(recurso.getGrabacion());
        assertEquals("Grabada", material.getModalidadAsociada());
        assertTrue(material.estaPublicado());
        assertTrue(material.tieneArchivo());
        assertEquals(MP4.length, recurso.getGrabacion().getTamanioBytes());
    }

    @Test
    @DisplayName("CU-29: cada tipo guarda sus datos propios: presentación, bibliografía y enlace")
    void subeCadaTipo() {
        int tema = temaDeRentaFija().getIdTema();

        TemaRecurso presentacion = recursoService.subir(fausto(), tema, "PRESENTACION",
                datos("Diapositivas", "Ana Pérez", null, null), archivo("clase.pdf", PDF), true);
        assertEquals("Presentación", presentacion.getTipo());
        assertEquals("Ana Pérez", presentacion.getAutor());
        assertNotNull(presentacion.getRecurso().getMaterialLectura());
        assertEquals("pdf", presentacion.getExtension());

        TemaRecurso bibliografia = recursoService.subir(fausto(), tema, "BIBLIOGRAFIA",
                datos("Manual de bonos", "Luis Gómez", null, "978-987-00-0000-7"), null, true);
        assertEquals("Bibliografía", bibliografia.getTipo());
        assertEquals("978-987-00-0000-7", bibliografia.getRecurso().getBibliografia().getIsbn());

        // no se admiten dos bibliografías con el mismo ISBN (aunque cambien los guiones o los espacios)
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "BIBLIOGRAFIA",
                datos("Otro manual", "Otra persona", null, "9789870000007"), null, true)).contains("ISBN"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "BIBLIOGRAFIA",
                datos("Otro manual", "Otra persona", null, "978 987 00 0000 7"), null, true)).contains("ISBN"));
        assertFalse(bibliografia.tieneArchivo());

        TemaRecurso enlace = recursoService.subir(fausto(), tema, "ENLACE",
                datos("Sitio de la CNV", null, "https://www.argentina.gob.ar/cnv", null), null, true);
        assertEquals("Enlace", enlace.getTipo());
        assertTrue(enlace.esEnlaceExterno());
        assertEquals("https://www.argentina.gob.ar/cnv", enlace.getRutaArchivo());

        // los recursos del tema se ordenan en el orden en que se cargaron
        List<TemaRecurso> delTema = recursoService.buscarPorTema(temaDeRentaFija());
        assertTrue(delTema.indexOf(presentacion) < delTema.indexOf(bibliografia));
        assertTrue(delTema.indexOf(bibliografia) < delTema.indexOf(enlace));
    }

    @Test
    @DisplayName("CU-29: valida el título, el tipo y los datos que pide cada tipo de material")
    void validaLosDatos() {
        int tema = temaDeRentaFija().getIdTema();

        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "GRABACION", datos(" ", null, null, null),
                archivo("clase.mp4", MP4), true)).contains("título"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "RESUMEN", datos("Resumen", "A", null, null),
                archivo("r.pdf", PDF), true)).contains("tipo de recurso"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", PDF), true)).contains("MP4 o WEBM"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                null, true)).contains("archivo de video"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "PRESENTACION", datos("Clase", null, null, null),
                archivo("clase.pdf", PDF), true)).contains("autor"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "BIBLIOGRAFIA", datos("Libro", "Autor", null, null),
                null, true)).contains("ISBN"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "ENLACE", datos("Sitio", null, "sitio.com", null),
                null, true)).contains("http://"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", MP4), false)).contains("términos y condiciones"));
        // la subida inválida no registra nada
        assertEquals(0, temaRecursoRepository.findAll().stream().filter(m -> m.getTitulo().equals("Clase")).count());
    }

    @Test
    @DisplayName("CU-29: el docente debe participar en el curso y el tema debe estar activo")
    void validaElDocenteYElTema() {
        Tema macro = temaRepository.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();

        assertTrue(mensaje(() -> recursoService.subir(docente("sebastian.bordato@idoneos.online"), macro.getIdTema(),
                "GRABACION", datos("Clase", null, null, null), archivo("clase.mp4", MP4), true)).contains("No participás"));
        assertTrue(mensaje(() -> recursoService.subir(fausto(), -1, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", MP4), true)).contains("tema no se encuentra"));
        assertTrue(mensaje(() -> recursoService.subir(null, temaDeRentaFija().getIdTema(), "GRABACION",
                datos("Clase", null, null, null), archivo("clase.mp4", MP4), true)).contains("rol de docente"));
    }

    // ----------------------------------------------------------------------
    // Modificar material
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-30: desde el panel del curso solo se cambia la visibilidad del material; los datos del recurso no se tocan")
    void cambiaLaVisibilidad() {
        TemaRecurso material = subirGrabacion("Clase original");
        String rutaAnterior = material.getRutaArchivo();

        recursoService.cambiarVisibilidad(material.getIdTemaRecurso(), fausto(), true);

        assertTrue(material.getOculto());
        assertFalse(material.estaPublicado());
        assertEquals("Clase original", material.getTitulo());
        assertEquals(rutaAnterior, material.getRutaArchivo());
        assertNotNull(material.getUltimaModificacion());

        recursoService.cambiarVisibilidad(material.getIdTemaRecurso(), fausto(), false);
        assertTrue(material.estaPublicado());
    }

    @Test
    @DisplayName("CU-30: la visibilidad la cambia un docente del curso, y un material dado de baja no se modifica")
    void cambiaLaVisibilidadDeUnaBibliografia() {
        TemaRecurso bibliografia = recursoService.subir(fausto(), temaDeRentaFija().getIdTema(), "BIBLIOGRAFIA",
                datos("Manual", "Autor", null, "978-0-00-000000-0"), null, true);

        assertTrue(mensaje(() -> recursoService.cambiarVisibilidad(bibliografia.getIdTemaRecurso(),
                docente("sebastian.bordato@idoneos.online"), true)).contains("No participás"));
        recursoService.cambiarVisibilidad(bibliografia.getIdTemaRecurso(), fausto(), true);
        assertTrue(bibliografia.getOculto());
        assertEquals("Manual", bibliografia.getTitulo());

        recursoService.darDeBaja(bibliografia.getIdTemaRecurso(), fausto());
        assertTrue(mensaje(() -> recursoService.cambiarVisibilidad(bibliografia.getIdTemaRecurso(), fausto(), false))
                .contains("no se encuentra activo"));
    }

    // ----------------------------------------------------------------------
    // Dar de baja material
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-31: la baja da de baja el uso en el tema; el recurso sigue en la biblioteca para reutilizarlo y conserva el archivo")
    void daDeBaja() {
        TemaRecurso material = subirGrabacion("Clase a borrar");

        recursoService.darDeBaja(material.getIdTemaRecurso(), fausto());

        assertTrue(material.getBaja());
        assertFalse(material.getRecurso().getBaja());
        assertFalse(material.estaPublicado());
        assertTrue(Files.exists(directorioCursos.resolve(material.getRutaArchivo())));
    }

    @Test
    @DisplayName("CU-31: el administrador puede dar de baja sin participar en el curso; otro docente no")
    void permisosDeBaja() {
        Tema macro = temaRepository.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
        TemaRecurso material = recursoService.subir(fausto(), macro.getIdTema(), "GRABACION",
                datos("Clase", null, null, null), archivo("clase.mp4", MP4), true);

        assertTrue(mensaje(() -> recursoService.darDeBaja(material.getIdTemaRecurso(),
                docente("sebastian.bordato@idoneos.online"))).contains("No participás"));
        recursoService.darDeBaja(material.getIdTemaRecurso(), null);
        assertTrue(material.getBaja());
    }

    @Test
    @DisplayName("La baja masiva es todo o nada")
    void bajaMasiva() {
        TemaRecurso uno = subirGrabacion("Uno");
        TemaRecurso dos = subirGrabacion("Dos");

        assertTrue(mensaje(() -> recursoService.darDeBajaVarios(List.of(), fausto())).contains("al menos un recurso"));
        assertTrue(mensaje(() -> recursoService.darDeBajaVarios(List.of(uno.getIdTemaRecurso(), -5), fausto()))
                .contains("no se encuentra activo"));
        assertFalse(uno.getBaja());

        recursoService.darDeBajaVarios(List.of(uno.getIdTemaRecurso(), dos.getIdTemaRecurso()), fausto());
        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
    }

    // ----------------------------------------------------------------------
    // Buscar material
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("CU-28: la búsqueda incluye el material oculto y filtra por tema, tipo, título y estado")
    void busca() {
        TemaRecurso oculto = recursoService.subir(fausto(), temaDeRentaFija().getIdTema(), "ENLACE",
                new DatosRecurso("Enlace escondido", null, true, null, "https://sitio.com", null, null, null, null, null), null,
                true);
        int curso = temaDeRentaFija().getCurso().getIdCurso();

        List<TemaRecurso> todos = recursoService.buscarConFiltros(curso, null, null, null, null, null, fausto());
        assertTrue(todos.contains(oculto));
        assertTrue(recursoService.buscarConFiltros(curso, null, "ENLACE", "escondido", null, null, null).contains(oculto));
        assertFalse(recursoService.buscarConFiltros(curso, null, "GRABACION", "escondido", null, null, null).contains(oculto));
        assertTrue(recursoService.buscarConFiltros(curso, temaDeRentaFija().getIdTema(), null, null, null, false, null)
                .contains(oculto));
        assertFalse(recursoService.buscarConFiltros(curso, null, null, null, null, true, null).contains(oculto));
        // un docente solo ve el material de sus cursos
        assertTrue(recursoService.buscarConFiltros(null, null, null, null, null, null, docente("sebastian.bordato@idoneos.online"))
                .stream().noneMatch(m -> m.getCurso().getNombre().equals("Macroeconomía de Coyuntura")));
    }

    // ----------------------------------------------------------------------
    // Biblioteca: privado o público y recurso compartido
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("Biblioteca: un recurso nuevo es privado salvo que se suba como público")
    void guardaSiEsPrivadoOPublico() {
        int tema = temaDeRentaFija().getIdTema();

        TemaRecurso porDefecto = recursoService.subir(fausto(), tema, "ENLACE", datos("Enlace privado", null, "https://sitio.com", null), null, true);
        TemaRecurso publico = recursoService.subir(fausto(), tema, "ENLACE",
                new DatosRecurso("Enlace público", null, false, null, "https://sitio.com/otro", null, null, null, null, null, false), null, true);

        assertTrue(porDefecto.getRecurso().getPrivado());
        assertFalse(publico.getRecurso().getPrivado());
    }

    @Test
    @DisplayName("Panel del curso: se puede quitar un recurso de un tema aunque otro tema lo use; el recurso sigue en la biblioteca")
    void quitaUnRecursoCompartidoDeUnTema() {
        TemaRecurso material = recursoService.subir(fausto(), temaDeRentaFija().getIdTema(), "ENLACE",
                datos("Enlace compartido", null, "https://sitio.com/compartido", null), null, true);

        // otro tema del mismo curso usa el mismo recurso
        Tema otroTema = temaRepository.findAll().stream().filter(u -> u.getCurso().getIdCurso() == material.getTema().getCurso().getIdCurso()
                && u.getIdTema() != material.getTema().getIdTema()).findFirst().orElseThrow();
        TemaRecurso uso = temaRecursoRepository.save(new TemaRecurso(otroTema, material.getRecurso(), material.getDocenteCurso(), 1));
        material.getRecurso().getTemasRecurso().add(uso);

        recursoService.darDeBaja(material.getIdTemaRecurso(), fausto());

        assertTrue(material.getBaja());
        assertFalse(uso.getBaja());
        assertFalse(material.getRecurso().getBaja());
    }

    // ----------------------------------------------------------------------
    // Biblioteca: recursos del docente, sin tema
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("Biblioteca: registra un recurso sin tema, en la carpeta de la biblioteca del docente, y lo lista entre sus recursos")
    void registraUnRecursoEnLaBiblioteca() {
        Recurso recurso = recursoService.subirABiblioteca(fausto(), "GRABACION",
                new DatosRecurso("Clase suelta", null, false, null, null, null, null, null, null, 30, false), archivo("clase.mp4", MP4), true);

        String esperada = "biblioteca/docente" + fausto().getIdDocente() + "/grabacion" + recurso.getIdRecurso() + ".mp4";
        assertEquals(esperada, recurso.getRutaArchivo());
        assertTrue(Files.exists(directorioCursos.resolve(esperada)));
        assertFalse(recurso.getPrivado());
        assertEquals(0, recursoService.contarUsos(recurso));
        assertTrue(recursoService.buscarRecursosDelDocente(fausto()).contains(recurso));
        assertFalse(recursoService.buscarRecursosDelDocente(docente("sebastian.bordato@idoneos.online")).contains(recurso));
    }

    @Test
    @DisplayName("Biblioteca: pide aceptar los términos y condiciones para registrar un recurso")
    void exigeTerminosAlRegistrar() {
        String mensaje = mensaje(() -> recursoService.subirABiblioteca(fausto(), "ENLACE",
                datos("Sin términos", null, "https://sitio.com/sin-terminos", null), null, false));
        assertTrue(mensaje.contains("términos"), mensaje);
    }

    @Test
    @DisplayName("Biblioteca: modifica y da de baja un recurso sin usar; si un tema lo usa o es de otro docente, no se puede")
    void modificaYDaDeBajaEnLaBiblioteca() {
        Recurso recurso = recursoService.subirABiblioteca(fausto(), "ENLACE",
                datos("Enlace suelto", null, "https://sitio.com/suelto", null), null, true);

        // otro docente no puede tocarlo
        String ajeno = mensaje(() -> recursoService.modificarEnBiblioteca(recurso.getIdRecurso(), docente("sebastian.bordato@idoneos.online"),
                datos("Robado", null, "https://sitio.com/suelto", null), null));
        assertTrue(ajeno.contains("pertenece a otro docente"), ajeno);
        assertTrue(mensaje(() -> recursoService.darDeBajaEnBiblioteca(recurso.getIdRecurso(), docente("sebastian.bordato@idoneos.online")))
                .contains("pertenece a otro docente"));

        // sin usos se modifica
        recursoService.modificarEnBiblioteca(recurso.getIdRecurso(), fausto(),
                new DatosRecurso("Enlace renombrado", "Con descripción", false, null, "https://sitio.com/suelto", null, null, null, null, null, false), null);
        assertEquals("Enlace renombrado", recurso.getTitulo());
        assertFalse(recurso.getPrivado());

        // un tema lo usa: no se modifica ni se da de baja
        Tema tema = temaDeRentaFija();
        var participacion = recursoService.buscarPorTema(tema).stream().findFirst().orElseThrow().getDocenteCurso();
        TemaRecurso uso = temaRecursoRepository.save(new TemaRecurso(tema, recurso, participacion, 99));
        recurso.getTemasRecurso().add(uso);
        assertEquals(1, recursoService.contarUsos(recurso));
        String modificar = mensaje(() -> recursoService.modificarEnBiblioteca(recurso.getIdRecurso(), fausto(),
                datos("Otro", null, "https://sitio.com/suelto", null), null));
        assertTrue(modificar.contains("No se puede modificar") && modificar.contains("1 tema asociado"), modificar);
        String baja = mensaje(() -> recursoService.darDeBajaEnBiblioteca(recurso.getIdRecurso(), fausto()));
        assertTrue(baja.contains("No se puede dar de baja") && baja.contains("1 tema asociado"), baja);

        // cuando deja de usarse, se da de baja
        uso.marcarInactivo();
        recursoService.darDeBajaEnBiblioteca(recurso.getIdRecurso(), fausto());
        assertTrue(recurso.getBaja());
        assertFalse(recursoService.buscarRecursosDelDocente(fausto()).contains(recurso));
    }

    @Test
    @DisplayName("Biblioteca: explorar muestra los recursos públicos de otros docentes y no los propios ni los privados")
    void exploraLosRecursosPublicosDeOtros() {
        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        Recurso publicoDeSebastian = recursoService.subirABiblioteca(sebastian, "ENLACE",
                new DatosRecurso("Público de Sebastián", null, false, null, "https://sitio.com/pub", null, null, null, null, null, false), null, true);
        Recurso privadoDeSebastian = recursoService.subirABiblioteca(sebastian, "ENLACE",
                datos("Privado de Sebastián", null, "https://sitio.com/priv", null), null, true);
        Recurso publicoDeFausto = recursoService.subirABiblioteca(fausto(), "ENLACE",
                new DatosRecurso("Público de Fausto", null, false, null, "https://sitio.com/fausto", null, null, null, null, null, false), null, true);

        List<Recurso> exploraFausto = recursoService.buscarRecursosPublicos(fausto());
        assertTrue(exploraFausto.contains(publicoDeSebastian));
        assertFalse(exploraFausto.contains(privadoDeSebastian));
        assertFalse(exploraFausto.contains(publicoDeFausto));
        assertTrue(recursoService.buscarRecursosPublicos(null).contains(publicoDeFausto));
    }

    @Test
    @DisplayName("Biblioteca: el administrador ve todos los recursos y puede dar de baja uno sin usar; si un tema lo usa, no")
    void administradorVeTodoYDaDeBaja() {
        Recurso privado = recursoService.subirABiblioteca(fausto(), "ENLACE",
                datos("Privado de Fausto", null, "https://sitio.com/admin-priv", null), null, true);
        Recurso publico = recursoService.subirABiblioteca(fausto(), "ENLACE",
                new DatosRecurso("Público de Fausto", null, false, null, "https://sitio.com/admin-pub", null, null, null, null, null, false), null, true);

        List<Recurso> todos = recursoService.buscarTodosLosRecursos();
        assertTrue(todos.contains(privado) && todos.contains(publico));

        // usado por un tema: no se puede dar de baja
        Tema tema = temaDeRentaFija();
        var participacion = recursoService.buscarPorTema(tema).stream().findFirst().orElseThrow().getDocenteCurso();
        TemaRecurso uso = temaRecursoRepository.save(new TemaRecurso(tema, publico, participacion, 98));
        publico.getTemasRecurso().add(uso);
        String bloqueada = mensaje(() -> recursoService.darDeBajaComoAdministrador(publico.getIdRecurso()));
        assertTrue(bloqueada.contains("No se puede dar de baja") && bloqueada.contains("1 tema asociado"), bloqueada);
        assertFalse(publico.getBaja());

        // sin usos se da de baja y deja de listarse
        recursoService.darDeBajaComoAdministrador(privado.getIdRecurso());
        assertTrue(privado.getBaja());
        assertFalse(recursoService.buscarTodosLosRecursos().contains(privado));
    }

    @Test
    @DisplayName("Biblioteca: las bibliografías son siempre públicas, se registren donde se registren y se pida lo que se pida")
    void lasBibliografiasSonPublicas() {
        int tema = temaDeRentaFija().getIdTema();

        TemaRecurso enElCurso = recursoService.subir(fausto(), tema, "BIBLIOGRAFIA", datos("Libro del curso", "Autor", null, "978-1-23-456789-7"), null, true);
        assertFalse(enElCurso.getRecurso().getPrivado());

        Recurso enLaBiblioteca = recursoService.subirABiblioteca(fausto(), "BIBLIOGRAFIA",
                new DatosRecurso("Libro suelto", null, false, "Autor", null, "978-0-12-345678-9", null, null, null, null, true), null, true);
        assertFalse(enLaBiblioteca.getPrivado());

        recursoService.modificarEnBiblioteca(enLaBiblioteca.getIdRecurso(), fausto(),
                new DatosRecurso("Libro suelto", null, false, "Autor", null, "978-0-12-345678-9", null, null, null, null, true), null);
        assertFalse(enLaBiblioteca.getPrivado());
    }

    @Test
    @DisplayName("Biblioteca: el dueño publica un recurso privado; otro docente no puede")
    void publicaUnRecurso() {
        Recurso recurso = recursoService.subirABiblioteca(fausto(), "ENLACE",
                datos("Para publicar", null, "https://sitio.com/publicar", null), null, true);
        assertTrue(recurso.getPrivado());

        String ajeno = mensaje(() -> recursoService.publicar(recurso.getIdRecurso(), docente("sebastian.bordato@idoneos.online")));
        assertTrue(ajeno.contains("pertenece a otro docente"), ajeno);
        assertTrue(recurso.getPrivado());

        recursoService.publicar(recurso.getIdRecurso(), fausto());
        assertFalse(recurso.getPrivado());
    }

    @Test
    @DisplayName("Biblioteca: la baja de varios recursos es todos o ninguno")
    void daDeBajaVariosRecursos() {
        Recurso libre = recursoService.subirABiblioteca(fausto(), "ENLACE", datos("Libre", null, "https://sitio.com/libre", null), null, true);
        Recurso enUso = recursoService.subirABiblioteca(fausto(), "ENLACE", datos("En uso", null, "https://sitio.com/en-uso", null), null, true);
        Tema tema = temaDeRentaFija();
        var participacion = recursoService.buscarPorTema(tema).stream().findFirst().orElseThrow().getDocenteCurso();
        enUso.getTemasRecurso().add(temaRecursoRepository.save(new TemaRecurso(tema, enUso, participacion, 97)));

        String mensaje = mensaje(() -> recursoService.darDeBajaVariosEnBiblioteca(List.of(libre.getIdRecurso(), enUso.getIdRecurso()), fausto()));
        assertTrue(mensaje.contains("No se puede dar de baja") && mensaje.contains("En uso"), mensaje);
        assertFalse(libre.getBaja());
        assertFalse(enUso.getBaja());

        recursoService.darDeBajaVariosEnBiblioteca(List.of(libre.getIdRecurso()), fausto());
        assertTrue(libre.getBaja());
    }

    @Test
    @DisplayName("Biblioteca digital: el docente ve todos sus recursos, públicos y privados, y solo los públicos de otros")
    void veSusRecursosYLosPublicosDeOtros() {
        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        Recurso privadoPropio = recursoService.subirABiblioteca(fausto(), "ENLACE",
                datos("Privado propio", null, "https://sitio.com/vis-priv", null), null, true);
        Recurso publicoPropio = recursoService.subirABiblioteca(fausto(), "ENLACE",
                new DatosRecurso("Público propio", null, false, null, "https://sitio.com/vis-pub", null, null, null, null, null, false), null, true);
        Recurso publicoAjeno = recursoService.subirABiblioteca(sebastian, "ENLACE",
                new DatosRecurso("Público ajeno", null, false, null, "https://sitio.com/vis-ajeno", null, null, null, null, null, false), null, true);
        Recurso privadoAjeno = recursoService.subirABiblioteca(sebastian, "ENLACE",
                datos("Privado ajeno", null, "https://sitio.com/vis-priv-ajeno", null), null, true);

        List<Recurso> visibles = recursoService.buscarRecursosVisibles(fausto());
        assertTrue(visibles.contains(privadoPropio) && visibles.contains(publicoPropio) && visibles.contains(publicoAjeno));
        assertFalse(visibles.contains(privadoAjeno));
    }

    @Test
    @DisplayName("Reutilizar: registra en un tema un recurso público de otro docente, sin crear otro recurso")
    void reutilizaUnRecursoPublicoDeOtroDocente() {
        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        Recurso publico = recursoService.subirABiblioteca(sebastian, "ENLACE",
                new DatosRecurso("Enlace público", null, false, null, "https://sitio.com/reutilizar-pub", null, null, null, null, null, false), null, true);

        TemaRecurso material = recursoService.reutilizar(fausto(), temaDeRentaFija().getIdTema(), publico.getIdRecurso());

        assertEquals(publico.getIdRecurso(), material.getRecurso().getIdRecurso());
        assertEquals(temaDeRentaFija().getIdTema(), material.getTema().getIdTema());
        assertEquals(1, recursoService.contarUsos(publico));
    }

    @Test
    @DisplayName("Reutilizar: no se puede registrar dos veces el mismo recurso en el mismo tema")
    void noReutilizaDosVecesEnElMismoTema() {
        TemaRecurso material = subirGrabacion("Clase para reutilizar");

        String mensaje = mensaje(() -> recursoService.reutilizar(fausto(), material.getTema().getIdTema(), material.getRecurso().getIdRecurso()));

        assertTrue(mensaje.contains("ya está en el tema"), mensaje);
    }

    @Test
    @DisplayName("Reutilizar: un recurso privado de otro docente no está disponible")
    void noReutilizaUnRecursoPrivadoAjeno() {
        Recurso privadoAjeno = recursoService.subirABiblioteca(docente("sebastian.bordato@idoneos.online"), "ENLACE",
                datos("Enlace privado ajeno", null, "https://sitio.com/reutilizar-priv", null), null, true);

        String mensaje = mensaje(() -> recursoService.reutilizar(fausto(), temaDeRentaFija().getIdTema(), privadoAjeno.getIdRecurso()));

        assertTrue(mensaje.contains("no se encuentra disponible"), mensaje);
    }

    @Test
    @DisplayName("Reutilizar varios: se registran todos en el tema, o ninguno si alguno no se puede registrar")
    void reutilizaVariosTodosONinguno() {
        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        Recurso uno = recursoService.subirABiblioteca(sebastian, "ENLACE",
                new DatosRecurso("Varios 1", null, false, null, "https://sitio.com/varios-1", null, null, null, null, null, false), null, true);
        Recurso dos = recursoService.subirABiblioteca(sebastian, "ENLACE",
                new DatosRecurso("Varios 2", null, false, null, "https://sitio.com/varios-2", null, null, null, null, null, false), null, true);
        Recurso privadoAjeno = recursoService.subirABiblioteca(sebastian, "ENLACE",
                datos("Varios privado", null, "https://sitio.com/varios-priv", null), null, true);
        Integer tema = temaDeRentaFija().getIdTema();

        assertThrows(IllegalArgumentException.class,
                () -> recursoService.reutilizar(fausto(), tema, List.of(uno.getIdRecurso(), privadoAjeno.getIdRecurso())));
        assertEquals(0, recursoService.contarUsos(uno));

        List<TemaRecurso> registrados = recursoService.reutilizar(fausto(), tema, List.of(uno.getIdRecurso(), dos.getIdRecurso()));
        assertEquals(2, registrados.size());
        assertEquals(1, recursoService.contarUsos(uno));
        assertEquals(1, recursoService.contarUsos(dos));
    }

    @Test
    @DisplayName("No se da de baja ni se oculta el único material de un tema si eso deja incompleto el diseño de un curso en marcha")
    void noQuitaElUnicoMaterialDeUnCursoEnMarcha() {
        TemaRecurso unico = temaRecursoRepository.findAll().stream().filter(m -> !m.getBaja() && m.getModalidadAsociada() == null)
                .filter(m -> m.getTema().getProgramasQueLaIncluyen().stream().anyMatch(p -> p.tieneCohortesActivas()))
                .filter(m -> m.getTema().getMaterialesPublicados().stream().filter(x -> x.getModalidadAsociada() == null).count() == 1)
                .findFirst().orElse(null);
        org.junit.jupiter.api.Assumptions.assumeTrue(unico != null, "la semilla no tiene un tema con un único material en un curso en marcha");
        Docente docente = unico.getRecurso().getDocente();

        String mensaje = mensaje(() -> recursoService.darDeBaja(unico.getIdTemaRecurso(), docente));

        assertTrue(mensaje.contains("quedaría incompleto") || mensaje.contains("único"), mensaje);
    }
}
