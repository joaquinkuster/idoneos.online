package com.app.idoneos.servicio;

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

import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Recurso;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.TemaRecursoRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.Recurso.RecursoServicio;
import com.app.idoneos.servicio.Recurso.RecursoServicio.DatosRecurso;

/**
 * Pruebas del ABM de materiales (CU-28 a CU-31): cada material es un recurso del docente (con los datos propios de su
 * tipo) usado en un tema. El docente Fausto Spotorno es titular de "Mercado de Capitales Argentino" y Sebastián Bordato no
 * participa en "Macroeconomía de Coyuntura".
 */
@SpringBootTest
@Transactional
class RecursoServicioTest {

    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };
    private static final byte[] WEBM = { 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Autowired
    private RecursoServicio recursoServicio;

    @Autowired
    private TemaRecursoRepositorio temaRecursoRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    private Docente docente(String correo) {
        return docenteRepositorio.findAll().stream().filter(d -> d.getUsuario().getCorreo().equals(correo)).findFirst()
                .orElseThrow();
    }

    private Docente fausto() {
        return docente("fausto.spotorno@idoneos.online");
    }

    private Tema temaDeRentaFija() {
        return temaRepositorio.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Mercado de Capitales Argentino")
                && u.getTitulo().equals("Renta fija y bonos")).findFirst().orElseThrow();
    }

    private MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, "application/octet-stream", contenido);
    }

    private DatosRecurso datos(String titulo, String autor, String enlace, String isbn) {
        return new DatosRecurso(titulo, null, false, autor, enlace, isbn, null, null, null, null);
    }

    private TemaRecurso subirGrabacion(String titulo) {
        return recursoServicio.subirMaterial(fausto(), temaDeRentaFija().getIdTema(), "GRABACION",
                datos(titulo, null, null, null), archivo("clase.mp4", MP4), true);
    }

    private String mensaje(Runnable accion) {
        return assertThrows(IllegalArgumentException.class, accion::run).getMessage();
    }

    // ------------------------------------------------------------------ CU-29 Subir material

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

        TemaRecurso presentacion = recursoServicio.subirMaterial(fausto(), tema, "PRESENTACION",
                datos("Diapositivas", "Ana Pérez", null, null), archivo("clase.pdf", PDF), true);
        assertEquals("Presentación", presentacion.getTipo());
        assertEquals("Ana Pérez", presentacion.getAutor());
        assertNotNull(presentacion.getRecurso().getMaterialLectura());
        assertEquals("pdf", presentacion.getExtension());

        TemaRecurso bibliografia = recursoServicio.subirMaterial(fausto(), tema, "BIBLIOGRAFIA",
                datos("Manual de bonos", "Luis Gómez", null, "978-987-00-0000-0"), null, true);
        assertEquals("Bibliografía", bibliografia.getTipo());
        assertEquals("978-987-00-0000-0", bibliografia.getRecurso().getBibliografia().getIsbn());
        assertFalse(bibliografia.tieneArchivo());

        TemaRecurso enlace = recursoServicio.subirMaterial(fausto(), tema, "ENLACE",
                datos("Sitio de la CNV", null, "https://www.argentina.gob.ar/cnv", null), null, true);
        assertEquals("Enlace", enlace.getTipo());
        assertTrue(enlace.esEnlaceExterno());
        assertEquals("https://www.argentina.gob.ar/cnv", enlace.getRutaArchivo());

        // los recursos del tema se ordenan en el orden en que se cargaron
        List<TemaRecurso> delTema = recursoServicio.buscarPorTema(temaDeRentaFija());
        assertTrue(delTema.indexOf(presentacion) < delTema.indexOf(bibliografia));
        assertTrue(delTema.indexOf(bibliografia) < delTema.indexOf(enlace));
    }

    @Test
    @DisplayName("CU-29: valida el título, el tipo y los datos que pide cada tipo de material")
    void validaLosDatos() {
        int tema = temaDeRentaFija().getIdTema();

        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "GRABACION", datos(" ", null, null, null),
                archivo("clase.mp4", MP4), true)).contains("título"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "RESUMEN", datos("Resumen", "A", null, null),
                archivo("r.pdf", PDF), true)).contains("tipo de material"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", PDF), true)).contains("MP4 o WEBM"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                null, true)).contains("archivo de video"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "PRESENTACION", datos("Clase", null, null, null),
                archivo("clase.pdf", PDF), true)).contains("autor"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "BIBLIOGRAFIA", datos("Libro", "Autor", null, null),
                null, true)).contains("ISBN"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "ENLACE", datos("Sitio", null, "sitio.com", null),
                null, true)).contains("http://"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), tema, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", MP4), false)).contains("términos y condiciones"));
        // la subida inválida no registra nada
        assertEquals(0, temaRecursoRepositorio.findAll().stream().filter(m -> m.getTitulo().equals("Clase")).count());
    }

    @Test
    @DisplayName("CU-29: el docente debe participar en el curso y el tema debe estar activo")
    void validaElDocenteYElTema() {
        Tema macro = temaRepositorio.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();

        assertTrue(mensaje(() -> recursoServicio.subirMaterial(docente("sebastian.bordato@idoneos.online"), macro.getIdTema(),
                "GRABACION", datos("Clase", null, null, null), archivo("clase.mp4", MP4), true)).contains("No participás"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(fausto(), -1, "GRABACION", datos("Clase", null, null, null),
                archivo("clase.mp4", MP4), true)).contains("tema no se encuentra"));
        assertTrue(mensaje(() -> recursoServicio.subirMaterial(null, temaDeRentaFija().getIdTema(), "GRABACION",
                datos("Clase", null, null, null), archivo("clase.mp4", MP4), true)).contains("rol de docente"));
    }

    // ------------------------------------------------------------------ CU-30 Modificar material

    @Test
    @DisplayName("CU-30: modifica el título, la descripción y la visibilidad, y reemplaza el archivo sin dejar el anterior")
    void modifica() {
        TemaRecurso material = subirGrabacion("Clase original");
        String rutaAnterior = material.getRutaArchivo();

        recursoServicio.modificarMaterial(material.getIdTemaRecurso(), fausto(),
                new DatosRecurso("Clase nueva", "Con descripción", true, null, null, null, null, null, null, 45),
                archivo("clase.webm", WEBM));

        assertEquals("Clase nueva", material.getTitulo());
        assertEquals("Con descripción", material.getRecurso().getDescripcion());
        assertTrue(material.getOculto());
        assertFalse(material.estaPublicado());
        assertEquals(45, material.getRecurso().getGrabacion().getDuracionMinutos());
        assertTrue(material.getRutaArchivo().endsWith(".webm"));
        assertFalse(Files.exists(directorioCursos.resolve(rutaAnterior)));
        assertNotNull(material.getUltimaModificacion());
    }

    @Test
    @DisplayName("CU-30: una bibliografía se modifica con sus datos propios, y un material dado de baja no se modifica")
    void modificaUnaBibliografia() {
        TemaRecurso bibliografia = recursoServicio.subirMaterial(fausto(), temaDeRentaFija().getIdTema(), "BIBLIOGRAFIA",
                datos("Manual", "Autor", null, "978-0-00-000000-0"), null, true);

        recursoServicio.modificarMaterial(bibliografia.getIdTemaRecurso(), fausto(),
                new DatosRecurso("Manual 2", null, false, "Otro autor", null, "978-1-11-111111-1", "Editorial", 300, null, null),
                null);

        assertEquals("Otro autor", bibliografia.getAutor());
        assertEquals("Editorial", bibliografia.getRecurso().getBibliografia().getEditorial());
        assertEquals(300, bibliografia.getRecurso().getBibliografia().getPaginas());
        assertTrue(mensaje(() -> recursoServicio.modificarMaterial(bibliografia.getIdTemaRecurso(), fausto(),
                datos(" ", "Autor", null, "978"), null)).contains("título"));

        recursoServicio.darDeBajaMaterial(bibliografia.getIdTemaRecurso(), fausto());
        assertTrue(mensaje(() -> recursoServicio.modificarMaterial(bibliografia.getIdTemaRecurso(), fausto(),
                datos("Otro", "Autor", null, "978"), null)).contains("no se encuentra activo"));
    }

    // ------------------------------------------------------------------ CU-31 Dar de baja material

    @Test
    @DisplayName("CU-31: la baja da de baja el uso en el tema y el recurso (si nadie más lo usa), y conserva el archivo")
    void daDeBaja() {
        TemaRecurso material = subirGrabacion("Clase a borrar");

        recursoServicio.darDeBajaMaterial(material.getIdTemaRecurso(), fausto());

        assertTrue(material.getBaja());
        assertTrue(material.getRecurso().getBaja());
        assertFalse(material.estaPublicado());
        assertTrue(Files.exists(directorioCursos.resolve(material.getRutaArchivo())));
    }

    @Test
    @DisplayName("CU-31: el administrador puede dar de baja sin participar en el curso; otro docente no")
    void permisosDeBaja() {
        Tema macro = temaRepositorio.findAll().stream().filter(u -> u.getCurso().getNombre().equals("Macroeconomía de Coyuntura"))
                .findFirst().orElseThrow();
        TemaRecurso material = recursoServicio.subirMaterial(fausto(), macro.getIdTema(), "GRABACION",
                datos("Clase", null, null, null), archivo("clase.mp4", MP4), true);

        assertTrue(mensaje(() -> recursoServicio.darDeBajaMaterial(material.getIdTemaRecurso(),
                docente("sebastian.bordato@idoneos.online"))).contains("No participás"));
        recursoServicio.darDeBajaMaterial(material.getIdTemaRecurso(), null);
        assertTrue(material.getBaja());
    }

    @Test
    @DisplayName("La baja masiva es todo o nada")
    void bajaMasiva() {
        TemaRecurso uno = subirGrabacion("Uno");
        TemaRecurso dos = subirGrabacion("Dos");

        assertTrue(mensaje(() -> recursoServicio.darDeBajaVarios(List.of(), fausto())).contains("al menos un material"));
        assertTrue(mensaje(() -> recursoServicio.darDeBajaVarios(List.of(uno.getIdTemaRecurso(), -5), fausto()))
                .contains("no se encuentra activo"));
        assertFalse(uno.getBaja());

        recursoServicio.darDeBajaVarios(List.of(uno.getIdTemaRecurso(), dos.getIdTemaRecurso()), fausto());
        assertTrue(uno.getBaja());
        assertTrue(dos.getBaja());
    }

    // ------------------------------------------------------------------ CU-28 Buscar material

    @Test
    @DisplayName("CU-28: la búsqueda incluye el material oculto y filtra por tema, tipo, título y estado")
    void busca() {
        TemaRecurso oculto = recursoServicio.subirMaterial(fausto(), temaDeRentaFija().getIdTema(), "ENLACE",
                new DatosRecurso("Enlace escondido", null, true, null, "https://sitio.com", null, null, null, null, null), null,
                true);
        int curso = temaDeRentaFija().getCurso().getIdCurso();

        List<TemaRecurso> todos = recursoServicio.buscarConFiltros(curso, null, null, null, null, null, fausto());
        assertTrue(todos.contains(oculto));
        assertTrue(recursoServicio.buscarConFiltros(curso, null, "ENLACE", "escondido", null, null, null).contains(oculto));
        assertFalse(recursoServicio.buscarConFiltros(curso, null, "GRABACION", "escondido", null, null, null).contains(oculto));
        assertTrue(recursoServicio.buscarConFiltros(curso, temaDeRentaFija().getIdTema(), null, null, null, false, null)
                .contains(oculto));
        assertFalse(recursoServicio.buscarConFiltros(curso, null, null, null, null, true, null).contains(oculto));
        // un docente solo ve el material de sus cursos
        assertTrue(recursoServicio.buscarConFiltros(null, null, null, null, null, null, docente("sebastian.bordato@idoneos.online"))
                .stream().noneMatch(m -> m.getCurso().getNombre().equals("Macroeconomía de Coyuntura")));
    }
}
