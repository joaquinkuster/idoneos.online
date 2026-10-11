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
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.Material.MaterialServicio;

/**
 * Pruebas del ABM de materiales (CU-28 a CU-31) con los datos de la semilla: el docente Fausto Spotorno es titular de
 * "Mercado de Capitales Argentino" y Sebastián Bordato no participa en "Macroeconomía de Coyuntura".
 */
@SpringBootTest
@Transactional
class MaterialServicioTest {

    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };
    private static final byte[] WEBM = { 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Autowired
    private MaterialServicio materialServicio;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

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

    private Unidad unidad(String curso, String titulo) {
        return unidadRepositorio.findAll().stream()
                .filter(u -> u.getCurso().getNombre().equals(curso) && u.getTitulo().equals(titulo)).findFirst()
                .orElseThrow();
    }

    private Unidad unidadDeRentaFija() {
        return unidad("Mercado de Capitales Argentino", "Renta fija y bonos");
    }

    private TipoMaterial tipo(String nombre) {
        return tipoMaterialRepositorio.findAll().stream().filter(t -> t.getNombre().equals(nombre)).findFirst()
                .orElseThrow();
    }

    private MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, "application/octet-stream", contenido);
    }

    private Material subirGrabacion(String titulo) {
        return materialServicio.subirMaterial(fausto(), unidadDeRentaFija().getIdUnidad(),
                tipo(TipoMaterial.GRABACION).getIdTipoMaterial(), titulo, false, null, null, archivo("clase.mp4", MP4));
    }

    /** Material de "Macroeconomía de Coyuntura", curso en el que no participa Sebastián Bordato. */
    private Material subirEnMacroeconomia() {
        return materialServicio.subirMaterial(fausto(),
                unidad("Macroeconomía de Coyuntura", "Inflación y precios").getIdUnidad(),
                tipo(TipoMaterial.GRABACION).getIdTipoMaterial(), "Clase de inflación", false, null, null,
                archivo("clase.mp4", MP4));
    }

    private String mensaje(Runnable accion) {
        return assertThrows(IllegalArgumentException.class, accion::run).getMessage();
    }

    // ------------------------------------------------------------------ CU-29 Subir material

    @Test
    @DisplayName("CU-29: sube una grabación y la guarda en curso{id}/unidad{id}/material{id}.mp4")
    void subeUnaGrabacion() {
        Material material = subirGrabacion("Clase 3: Valuación de bonos");

        Unidad unidad = material.getUnidad();
        String esperada = "curso" + unidad.getCurso().getIdCurso() + "/unidad" + unidad.getIdUnidad() + "/material"
                + material.getIdMaterial() + ".mp4";
        assertEquals(esperada, material.getRutaArchivo());
        assertTrue(Files.exists(directorioCursos.resolve(esperada)));
        assertEquals("Clase 3: Valuación de bonos", material.getTitulo());
        assertEquals(TipoMaterial.GRABACION, material.getTipoMaterial().getNombre());
        assertEquals(fausto().getIdDocente(), material.getParticipacionDocente().getDocente().getIdDocente());
        assertNotNull(material.getFechaCreacion());
        assertTrue(material.estaPublicado());
        assertTrue(material.tieneArchivo());
    }

    @Test
    @DisplayName("CU-29: puede subirse oculto, y una bibliografía puede ser un enlace externo con su autor")
    void subeOcultoYBibliografiaConEnlace() {
        Material oculto = materialServicio.subirMaterial(fausto(), unidadDeRentaFija().getIdUnidad(),
                tipo(TipoMaterial.PRESENTACION).getIdTipoMaterial(), "Diapositivas", true, null, null,
                archivo("clase.pdf", PDF));
        Material enlace = materialServicio.subirMaterial(fausto(), unidadDeRentaFija().getIdUnidad(),
                tipo(TipoMaterial.BIBLIOGRAFIA).getIdTipoMaterial(), "Libro de bonos", false, "J. Pérez",
                "https://sitio.com/libro.pdf", null);

        assertTrue(oculto.getOculto());
        assertFalse(oculto.estaPublicado());
        assertTrue(enlace.esEnlaceExterno());
        assertFalse(enlace.tieneArchivo());
        assertEquals("https://sitio.com/libro.pdf", enlace.getRutaArchivo());
        assertEquals("J. Pérez", enlace.getAutor());
    }

    @Test
    @DisplayName("CU-29: valida el título, el tipo y los datos que pide cada tipo de material")
    void validaLosDatos() {
        int unidad = unidadDeRentaFija().getIdUnidad();
        int grabacion = tipo(TipoMaterial.GRABACION).getIdTipoMaterial();
        int bibliografia = tipo(TipoMaterial.BIBLIOGRAFIA).getIdTipoMaterial();
        int presentacion = tipo(TipoMaterial.PRESENTACION).getIdTipoMaterial();
        int resumen = tipo(TipoMaterial.RESUMEN).getIdTipoMaterial();
        Docente docente = fausto();

        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, grabacion, "  ", false, null, null,
                archivo("c.mp4", MP4))).contains("título"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, grabacion, "x".repeat(51), false, null,
                null, archivo("c.mp4", MP4))).contains("50 caracteres"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, resumen, "Resumen", false, null, null,
                archivo("c.pdf", PDF))).contains("tipo de material válido"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, grabacion, "Clase", false, null, null,
                null)).contains("archivo de video"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, presentacion, "Clase", false, null,
                null, null)).contains("presentación"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, bibliografia, "Libro", false, null,
                null, null)).contains("archivo o ingresar un enlace"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, bibliografia, "Libro", false, null,
                "https://sitio.com", archivo("l.pdf", PDF))).contains("no ambos"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, bibliografia, "Libro", false, null,
                "ftp://sitio.com", null)).contains("http://"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, grabacion, "Clase", false, null,
                "https://sitio.com", null)).contains("no admite un enlace"));
        // un archivo renombrado (un PDF que dice ser MP4) o de otro formato se rechaza
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, grabacion, "Clase", false, null, null,
                archivo("clase.mp4", PDF))).contains("MP4 o WEBM"));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, presentacion, "Clase", false, null,
                null, archivo("clase.mp4", MP4))).contains("en formato PDF."));
        // los documentos de Office ya no se admiten: solo PDF
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, presentacion, "Clase", false, null,
                null, archivo("clase.pptx", PDF))).contains("en formato PDF."));
        assertTrue(mensaje(() -> materialServicio.subirMaterial(docente, unidad, bibliografia, "Libro", false, null,
                null, archivo("libro.docx", PDF))).contains("en formato PDF."));
    }

    @Test
    @DisplayName("CU-29: una subida inválida no registra el material")
    void subidaInvalidaNoRegistra() {
        long antes = materialRepositorio.count();

        mensaje(() -> materialServicio.subirMaterial(fausto(), unidadDeRentaFija().getIdUnidad(),
                tipo(TipoMaterial.GRABACION).getIdTipoMaterial(), "Clase", false, null, null, archivo("c.mp4", PDF)));

        assertEquals(antes, materialRepositorio.count());
    }

    @Test
    @DisplayName("CU-29: el docente debe participar en el curso y la unidad debe estar activa")
    void participacionYUnidadActiva() {
        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        Unidad ajena = unidad("Macroeconomía de Coyuntura", "Inflación y precios");

        assertTrue(mensaje(() -> materialServicio.subirMaterial(sebastian, ajena.getIdUnidad(),
                tipo(TipoMaterial.GRABACION).getIdTipoMaterial(), "Clase", false, null, null, archivo("c.mp4", MP4)))
                .contains("No participás"));

        Unidad deBaja = unidadDeRentaFija();
        deBaja.marcarInactivo();
        assertTrue(mensaje(() -> materialServicio.subirMaterial(fausto(), deBaja.getIdUnidad(),
                tipo(TipoMaterial.GRABACION).getIdTipoMaterial(), "Clase", false, null, null, archivo("c.mp4", MP4)))
                .contains("unidad no se encuentra activa"));
    }

    // ------------------------------------------------------------------ CU-30 Modificar material

    @Test
    @DisplayName("CU-30: modifica el título y la visibilidad, y registra la fecha de modificación")
    void modificaTituloYVisibilidad() {
        Material material = subirGrabacion("Clase 3");

        Material modificado = materialServicio.modificarMaterial(material.getIdMaterial(), fausto(), "Clase 3 (editada)",
                true, "Fausto Spotorno", null, null);

        assertEquals("Clase 3 (editada)", modificado.getTitulo());
        assertTrue(modificado.getOculto());
        assertEquals("Fausto Spotorno", modificado.getAutor());
        assertNotNull(modificado.getUltimaModificacion());
        assertEquals(material.getRutaArchivo(), modificado.getRutaArchivo(), "sin archivo nuevo se conserva el actual");
    }

    @Test
    @DisplayName("CU-30: al reemplazar el archivo por otro formato no queda el anterior")
    void reemplazaElArchivo() {
        Material grabacion = subirGrabacion("Clase 3");
        String anterior = grabacion.getRutaArchivo();
        assertTrue(anterior.endsWith(".mp4"));
        assertTrue(Files.exists(directorioCursos.resolve(anterior)));

        Material modificado = materialServicio.modificarMaterial(grabacion.getIdMaterial(), fausto(), "Clase 3", false,
                null, null, archivo("clase.webm", WEBM));

        assertTrue(modificado.getRutaArchivo().endsWith(".webm"));
        assertFalse(Files.exists(directorioCursos.resolve(anterior)), "no debe quedar el archivo anterior");
        assertTrue(Files.exists(directorioCursos.resolve(modificado.getRutaArchivo())));
    }

    @Test
    @DisplayName("CU-30: una bibliografía puede pasar de archivo a enlace externo y se borra el archivo")
    void pasaDeArchivoAEnlace() {
        Material biblio = materialServicio.subirMaterial(fausto(), unidadDeRentaFija().getIdUnidad(),
                tipo(TipoMaterial.BIBLIOGRAFIA).getIdTipoMaterial(), "Libro", false, null, null,
                archivo("libro.pdf", PDF));
        String anterior = biblio.getRutaArchivo();

        Material modificado = materialServicio.modificarMaterial(biblio.getIdMaterial(), fausto(), "Libro", false, null,
                "https://sitio.com/libro.pdf", null);

        assertTrue(modificado.esEnlaceExterno());
        assertFalse(Files.exists(directorioCursos.resolve(anterior)));
    }

    @Test
    @DisplayName("CU-30: valida que el material esté activo, que el título no quede vacío y que el docente participe")
    void validaLaModificacion() {
        Material material = subirGrabacion("Clase 3");
        int id = material.getIdMaterial();
        Docente fausto = fausto();

        assertTrue(mensaje(() -> materialServicio.modificarMaterial(id, fausto, "", false, null, null, null))
                .contains("título"));
        int idAjeno = subirEnMacroeconomia().getIdMaterial();
        assertTrue(mensaje(() -> materialServicio.modificarMaterial(idAjeno, docente("sebastian.bordato@idoneos.online"),
                "Otro", false, null, null, null)).contains("No participás"));
        assertTrue(mensaje(() -> materialServicio.modificarMaterial(id, fausto, "Clase", false, null, null,
                archivo("clase.mp4", PDF))).contains("MP4 o WEBM"));

        material.marcarInactivo();
        assertTrue(mensaje(() -> materialServicio.modificarMaterial(id, fausto, "Otro", false, null, null, null))
                .contains("no se encuentra activo"));
    }

    // ------------------------------------------------------------------ CU-31 Dar de baja material

    @Test
    @DisplayName("CU-31: da de baja el material, conserva el archivo y deja de publicarse")
    void daDeBajaConservandoElArchivo() {
        Material material = subirGrabacion("Clase 3");

        materialServicio.darDeBajaMaterial(material.getIdMaterial(), fausto());

        assertTrue(material.esInactivo());
        assertFalse(material.estaPublicado());
        assertTrue(Files.exists(directorioCursos.resolve(material.getRutaArchivo())));
        assertTrue(mensaje(() -> materialServicio.darDeBajaMaterial(material.getIdMaterial(), fausto()))
                .contains("no se encuentra activo"));
    }

    @Test
    @DisplayName("CU-31: el administrador puede dar de baja sin participar en el curso; otro docente no")
    void administradorYDocenteAjeno() {
        Material material = subirEnMacroeconomia();

        assertTrue(mensaje(() -> materialServicio.darDeBajaMaterial(material.getIdMaterial(),
                docente("sebastian.bordato@idoneos.online"))).contains("No participás"));
        assertFalse(material.esInactivo());

        materialServicio.darDeBajaMaterial(material.getIdMaterial(), null);

        assertTrue(material.esInactivo());
    }

    @Test
    @DisplayName("La baja masiva es todo o nada")
    void bajaMasivaTodoONada() {
        Material uno = subirGrabacion("Uno");
        Material dos = subirGrabacion("Dos");
        dos.marcarInactivo();

        assertTrue(mensaje(() -> materialServicio.darDeBajaVarios(List.of(uno.getIdMaterial(), dos.getIdMaterial()),
                fausto())).contains("no se encuentra activo"));
        assertFalse(uno.esInactivo(), "si alguno falla no se da de baja ninguno");
        assertTrue(mensaje(() -> materialServicio.darDeBajaVarios(List.of(), fausto())).contains("al menos un material"));

        Material tres = subirGrabacion("Tres");
        materialServicio.darDeBajaVarios(List.of(uno.getIdMaterial(), tres.getIdMaterial()), fausto());
        assertTrue(uno.esInactivo());
        assertTrue(tres.esInactivo());
    }

    // ------------------------------------------------------------------ CU-28 Buscar material

    @Test
    @DisplayName("CU-28: la búsqueda incluye el material oculto y filtra por unidad, tipo, título y estado")
    void buscaConFiltros() {
        Unidad unidad = unidadDeRentaFija();
        Integer curso = unidad.getCurso().getIdCurso();

        List<Material> deLaUnidad = materialServicio.buscarConFiltros(curso, unidad.getIdUnidad(), null, null, null,
                false, null);
        assertTrue(deLaUnidad.stream().anyMatch(m -> m.getTitulo().equals("Borrador: ejercicios de duration") && m.getOculto()),
                "debe listar también el material no publicado");

        List<Material> porTitulo = materialServicio.buscarConFiltros(curso, null, null, "guía", null, null, null);
        assertEquals(1, porTitulo.size());
        assertEquals("Guía teórica de renta fija", porTitulo.get(0).getTitulo());

        Integer bibliografia = tipo(TipoMaterial.BIBLIOGRAFIA).getIdTipoMaterial();
        assertTrue(materialServicio.buscarConFiltros(curso, null, bibliografia, null, null, null, null).stream()
                .allMatch(m -> m.getTipoMaterial().getNombre().equals(TipoMaterial.BIBLIOGRAFIA)));
        assertTrue(materialServicio.buscarConFiltros(curso, null, null, null, true, null, null).isEmpty(),
                "ninguno de la semilla fue generado con IA");
    }

    @Test
    @DisplayName("CU-28: los dados de baja van al final y cada docente solo ve el material de sus cursos")
    void bajasAlFinalYSoloDeDocente() {
        Unidad unidad = unidadDeRentaFija();
        Integer curso = unidad.getCurso().getIdCurso();
        Material primero = materialServicio.buscarConFiltros(curso, unidad.getIdUnidad(), null, null, null, null, null)
                .get(0);
        primero.marcarInactivo();

        List<Material> todos = materialServicio.buscarConFiltros(curso, unidad.getIdUnidad(), null, null, null, null, null);
        assertTrue(todos.get(todos.size() - 1).esInactivo());
        assertEquals(1, materialServicio.buscarConFiltros(curso, null, null, null, null, true, null).size());

        Docente sebastian = docente("sebastian.bordato@idoneos.online");
        assertTrue(materialServicio.buscarConFiltros(unidad("Macroeconomía de Coyuntura", "Inflación y precios")
                .getCurso().getIdCurso(), null, null, null, null, null, sebastian).isEmpty());
        assertFalse(materialServicio.buscarConFiltros(curso, null, null, null, null, null, fausto()).isEmpty());
    }
}
