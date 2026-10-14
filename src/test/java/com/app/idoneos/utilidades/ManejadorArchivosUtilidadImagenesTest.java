package com.app.idoneos.utilidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Pruebas de {@link ManejadorArchivosUtilidad}: guardado y eliminación de imágenes en el sistema de archivos.
 */
class ManejadorArchivosUtilidadImagenesTest {

    private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F' };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D };
    private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' ' };

    @TempDir
    Path carpeta;

    private MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("imagenArchivo", nombre, "image/png", contenido);
    }

    @Test
    @DisplayName("Guarda imágenes JPG, PNG y WebP con el nombre pedido y la extensión del formato real")
    void guardaImagenesValidas() {
        String jpg = ManejadorArchivosUtilidad.guardarImagen(archivo("foto.png", JPG), carpeta.resolve("a"), "imagen");
        String png = ManejadorArchivosUtilidad.guardarImagen(archivo("foto.jpg", PNG), carpeta.resolve("b"), "imagen");
        String webp = ManejadorArchivosUtilidad.guardarImagen(archivo("foto.gif", WEBP), carpeta.resolve("c"), "imagen");

        assertEquals("imagen.jpg", jpg);
        assertEquals("imagen.png", png);
        assertEquals("imagen.webp", webp);
        assertTrue(Files.exists(carpeta.resolve("a").resolve(jpg)), "la carpeta se crea si no existe");
    }

    @Test
    @DisplayName("Al guardar otra imagen con el mismo nombre se reemplaza la anterior, aunque cambie la extensión")
    void reemplazaLaImagenAnterior() throws IOException {
        ManejadorArchivosUtilidad.guardarImagen(archivo("uno.png", PNG), carpeta, "imagen");
        assertTrue(Files.exists(carpeta.resolve("imagen.png")));

        ManejadorArchivosUtilidad.guardarImagen(archivo("dos.jpg", JPG), carpeta, "imagen");

        assertFalse(Files.exists(carpeta.resolve("imagen.png")), "no debe quedar la imagen anterior");
        assertTrue(Files.exists(carpeta.resolve("imagen.jpg")));
        assertEquals(1, carpeta.toFile().list().length);
    }

    @Test
    @DisplayName("Achica las imágenes más anchas que el máximo para que carguen más rápido y no toca las chicas")
    void achicaLasImagenesMuyAnchas() throws IOException {
        String ancha = ManejadorArchivosUtilidad.guardarImagen(imagenReal(2400, 1200, "jpg"), carpeta.resolve("a"), "imagen");
        String chica = ManejadorArchivosUtilidad.guardarImagen(imagenReal(800, 400, "png"), carpeta.resolve("b"), "imagen");

        assertEquals(ManejadorArchivosUtilidad.ANCHO_MAXIMO_IMAGEN, ImageIO.read(carpeta.resolve("a").resolve(ancha).toFile()).getWidth());
        assertEquals(640, ImageIO.read(carpeta.resolve("a").resolve(ancha).toFile()).getHeight());
        assertEquals(800, ImageIO.read(carpeta.resolve("b").resolve(chica).toFile()).getWidth());
    }

    private MockMultipartFile imagenReal(int ancho, int alto, String formato) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, formato, salida);
        return new MockMultipartFile("imagenArchivo", "foto." + formato, "image/" + formato, salida.toByteArray());
    }

    @Test
    @DisplayName("Rechaza un archivo que no es una imagen aunque tenga extensión de imagen")
    void rechazaArchivosQueNoSonImagenes() {
        MockMultipartFile falso = archivo("foto.jpg", "esto no es una imagen".getBytes());

        assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad.guardarImagen(falso, carpeta, "imagen"));
        assertFalse(Files.exists(carpeta.resolve("imagen.jpg")));
    }

    @Test
    @DisplayName("Rechaza un archivo vacío o ausente")
    void rechazaArchivosVacios() {
        assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad.validarImagen(archivo("foto.jpg", new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad.validarImagen(null));
    }

    @Test
    @DisplayName("Rechaza una imagen que supera los 2 MB")
    void rechazaImagenesMuyGrandes() {
        byte[] grande = new byte[(int) ManejadorArchivosUtilidad.TAMANIO_MAXIMO_IMAGEN + 1];
        System.arraycopy(JPG, 0, grande, 0, JPG.length);

        assertThrows(IllegalArgumentException.class, () -> ManejadorArchivosUtilidad.validarImagen(archivo("foto.jpg", grande)));
    }

    @Test
    @DisplayName("Elimina la imagen guardada y no sale de la carpeta de imágenes")
    void eliminaSoloDentroDeLaCarpeta() throws IOException {
        String nombre = ManejadorArchivosUtilidad.guardarImagen(archivo("foto.jpg", JPG), carpeta, "imagen");
        Path fuera = Files.createTempFile("fuera", ".txt");

        ManejadorArchivosUtilidad.eliminar(nombre, carpeta);
        ManejadorArchivosUtilidad.eliminar("../" + fuera.getFileName(), carpeta);
        ManejadorArchivosUtilidad.eliminar(null, carpeta);

        assertFalse(Files.exists(carpeta.resolve(nombre)));
        assertTrue(Files.exists(fuera), "no debe borrar archivos fuera de la carpeta de imágenes");
        Files.deleteIfExists(fuera);
    }
}
