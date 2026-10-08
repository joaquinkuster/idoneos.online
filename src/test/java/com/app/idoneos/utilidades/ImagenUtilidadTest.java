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
 * Pruebas de {@link ImagenUtilidad}: guardado y eliminación de imágenes en el sistema de archivos.
 */
class ImagenUtilidadTest {

    private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F' };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D };
    private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' ' };

    @TempDir
    Path carpeta;

    @Test
    @DisplayName("Guarda imágenes JPG, PNG y WebP con un nombre único y la extensión del formato real")
    void guardaImagenesValidas() {
        String jpg = ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.png", "image/png", JPG), carpeta);
        String png = ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", PNG), carpeta);
        String webp = ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.gif", "image/gif", WEBP), carpeta);

        assertTrue(jpg.endsWith(".jpg"));
        assertTrue(png.endsWith(".png"));
        assertTrue(webp.endsWith(".webp"));
        assertTrue(Files.exists(carpeta.resolve(jpg)));
        assertEquals(3, carpeta.toFile().list().length);
    }

    @Test
    @DisplayName("Achica las imágenes más anchas que el máximo para que carguen más rápido y no toca las chicas")
    void achicaLasImagenesMuyAnchas() throws IOException {
        String ancha = ImagenUtilidad.guardar(imagenReal(2400, 1200, "jpg"), carpeta);
        String chica = ImagenUtilidad.guardar(imagenReal(800, 400, "png"), carpeta);

        assertEquals(ImagenUtilidad.ANCHO_MAXIMO, ImageIO.read(carpeta.resolve(ancha).toFile()).getWidth());
        assertEquals(640, ImageIO.read(carpeta.resolve(ancha).toFile()).getHeight());
        assertEquals(800, ImageIO.read(carpeta.resolve(chica).toFile()).getWidth());
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
        MockMultipartFile falso = new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg",
                "esto no es una imagen".getBytes());

        assertThrows(IllegalArgumentException.class, () -> ImagenUtilidad.guardar(falso, carpeta));
        assertEquals(0, carpeta.toFile().list().length);
    }

    @Test
    @DisplayName("Rechaza un archivo vacío o ausente")
    void rechazaArchivosVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", new byte[0]), carpeta));
        assertThrows(IllegalArgumentException.class, () -> ImagenUtilidad.guardar(null, carpeta));
    }

    @Test
    @DisplayName("Rechaza una imagen que supera los 2 MB")
    void rechazaImagenesMuyGrandes() {
        byte[] grande = new byte[(int) ImagenUtilidad.TAMANIO_MAXIMO + 1];
        System.arraycopy(JPG, 0, grande, 0, JPG.length);

        assertThrows(IllegalArgumentException.class,
                () -> ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", grande), carpeta));
    }

    @Test
    @DisplayName("Elimina la imagen guardada y no sale de la carpeta de imágenes")
    void eliminaSoloDentroDeLaCarpeta() throws IOException {
        String nombre = ImagenUtilidad.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", JPG), carpeta);
        Path fuera = Files.createTempFile("fuera", ".txt");

        ImagenUtilidad.eliminar(nombre, carpeta);
        ImagenUtilidad.eliminar("../" + fuera.getFileName(), carpeta);
        ImagenUtilidad.eliminar(null, carpeta);

        assertFalse(Files.exists(carpeta.resolve(nombre)));
        assertTrue(Files.exists(fuera), "no debe borrar archivos fuera de la carpeta de imágenes");
        Files.deleteIfExists(fuera);
    }
}
