package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Pruebas de {@link ManejadorArchivosUtilidad}: rutas de las carpetas, validación por contenido y guardado de archivos.
 */
class ManejadorArchivosUtilidadTest {

    private static final byte[] PDF = { '%', 'P', 'D', 'F', '-', '1', '.', '7', 0, 0, 0, 0 };
    private static final byte[] MP4 = { 0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'm', 'p', '4', '2' };
    private static final byte[] WEBM = { 0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static final byte[] ZIP = { 'P', 'K', 0x03, 0x04, 0, 0, 0, 0, 0, 0, 0, 0 };
    private static final byte[] OLE = { (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1, 0, 0, 0, 0 };

    private static final long DIEZ_MB = 10L * 1024 * 1024;

    @TempDir
    Path base;

    private MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, "application/octet-stream", contenido);
    }

    @Test
    @DisplayName("Arma las carpetas del curso y de la unidad dentro de la carpeta base")
    void carpetas() {
        assertEquals(base.resolve("curso3"), ManejadorArchivosUtilidad.carpetaDeCurso(base, 3));
        assertEquals(base.resolve("curso3").resolve("unidad12"), ManejadorArchivosUtilidad.carpetaDeUnidad(base, 3, 12));
    }

    @Test
    @DisplayName("Guarda cada formato permitido si su contenido coincide con la extensión")
    void guardaFormatosValidos() {
        Path carpeta = ManejadorArchivosUtilidad.carpetaDeUnidad(base, 1, 2);
        List<String> todos = List.of("mp4", "webm", "pdf", "ppt", "pptx", "doc", "docx");

        assertEquals("material1.mp4", ManejadorArchivosUtilidad.guardar(archivo("clase.MP4", MP4), carpeta, "material1", DIEZ_MB, todos));
        assertEquals("material2.webm", ManejadorArchivosUtilidad.guardar(archivo("clase.webm", WEBM), carpeta, "material2", DIEZ_MB, todos));
        assertEquals("material3.pdf", ManejadorArchivosUtilidad.guardar(archivo("guia.pdf", PDF), carpeta, "material3", DIEZ_MB, todos));
        assertEquals("material4.pptx", ManejadorArchivosUtilidad.guardar(archivo("clase.pptx", ZIP), carpeta, "material4", DIEZ_MB, todos));
        assertEquals("material5.ppt", ManejadorArchivosUtilidad.guardar(archivo("clase.ppt", OLE), carpeta, "material5", DIEZ_MB, todos));
        assertEquals("material6.docx", ManejadorArchivosUtilidad.guardar(archivo("texto.docx", ZIP), carpeta, "material6", DIEZ_MB, todos));
        assertTrue(Files.exists(carpeta.resolve("material1.mp4")), "la carpeta de la unidad se crea si no existe");
    }

    @Test
    @DisplayName("Rechaza un archivo cuya extensión no está permitida")
    void rechazaExtensionNoPermitida() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad
                .validar(archivo("clase.avi", MP4), DIEZ_MB, List.of("mp4", "webm")));

        assertTrue(error.getMessage().contains("MP4 o WEBM"));
    }

    @Test
    @DisplayName("Rechaza un archivo renombrado: su contenido no coincide con la extensión")
    void rechazaContenidoQueNoCoincide() {
        assertThrows(IllegalArgumentException.class,
                () -> ManejadorArchivosUtilidad.validar(archivo("clase.mp4", PDF), DIEZ_MB, List.of("mp4")));
        assertThrows(IllegalArgumentException.class,
                () -> ManejadorArchivosUtilidad.validar(archivo("guia.pdf", "texto".getBytes()), DIEZ_MB, List.of("pdf")));
        assertThrows(IllegalArgumentException.class,
                () -> ManejadorArchivosUtilidad.validar(archivo("sin-extension", PDF), DIEZ_MB, List.of("pdf")));
    }

    @Test
    @DisplayName("Rechaza un archivo vacío, ausente o que supera el tamaño máximo")
    void rechazaVaciosYGrandes() {
        assertThrows(IllegalArgumentException.class,
                () -> ManejadorArchivosUtilidad.validar(archivo("guia.pdf", new byte[0]), DIEZ_MB, List.of("pdf")));
        assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad.validar(null, DIEZ_MB, List.of("pdf")));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> ManejadorArchivosUtilidad.validar(archivo("guia.pdf", PDF), 8, List.of("pdf")));
        assertTrue(error.getMessage().contains("MB"));
    }

    @Test
    @DisplayName("Al guardar con el mismo nombre se reemplaza el archivo anterior")
    void reemplazaElArchivo() throws IOException {
        Path carpeta = base.resolve("c");
        ManejadorArchivosUtilidad.guardar(archivo("a.pdf", PDF), carpeta, "material1", DIEZ_MB, List.of("pdf"));
        byte[] otro = { '%', 'P', 'D', 'F', 'X', 'X', 'X', 'X', 'X', 'X', 'X', 'X' };

        ManejadorArchivosUtilidad.guardar(archivo("b.pdf", otro), carpeta, "material1", DIEZ_MB, List.of("pdf"));

        assertEquals(1, carpeta.toFile().list().length);
        assertEquals('X', Files.readAllBytes(carpeta.resolve("material1.pdf"))[4]);
    }

    @Test
    @DisplayName("Elimina el archivo por su ruta relativa, ignora los enlaces y no sale de la carpeta base")
    void eliminaSoloDentroDeLaBase() throws IOException {
        Path carpeta = ManejadorArchivosUtilidad.carpetaDeUnidad(base, 1, 2);
        ManejadorArchivosUtilidad.guardar(archivo("a.pdf", PDF), carpeta, "material1", DIEZ_MB, List.of("pdf"));
        Path fuera = Files.createTempFile("fuera", ".txt");

        ManejadorArchivosUtilidad.eliminar("curso1/unidad2/material1.pdf", base);
        ManejadorArchivosUtilidad.eliminar("../" + fuera.getFileName(), base);
        ManejadorArchivosUtilidad.eliminar("https://sitio.com/archivo.pdf", base);
        ManejadorArchivosUtilidad.eliminar(null, base);

        assertFalse(Files.exists(carpeta.resolve("material1.pdf")));
        assertTrue(Files.exists(fuera), "no debe borrar archivos fuera de la carpeta base");
        Files.deleteIfExists(fuera);
    }

    @Test
    @DisplayName("Distingue un enlace externo de un archivo del servidor")
    void enlaceExterno() {
        assertTrue(ManejadorArchivosUtilidad.esEnlaceExterno("https://sitio.com/libro.pdf"));
        assertTrue(ManejadorArchivosUtilidad.esEnlaceExterno("http://sitio.com"));
        assertFalse(ManejadorArchivosUtilidad.esEnlaceExterno("curso1/unidad2/material1.pdf"));
        assertFalse(ManejadorArchivosUtilidad.esEnlaceExterno(null));
    }
}
