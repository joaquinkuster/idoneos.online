package com.app.idoneos.servicio;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.app.idoneos.servicio.Almacenamiento.AlmacenamientoImagenServicio;

/**
 * Pruebas del almacenamiento de las imágenes de los cursos en el sistema de archivos.
 */
class AlmacenamientoImagenServicioTest {

    private static final byte[] JPG = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F' };
    private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D };
    private static final byte[] WEBP = { 'R', 'I', 'F', 'F', 1, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' ' };

    @TempDir
    Path carpeta;

    private AlmacenamientoImagenServicio servicio;

    @BeforeEach
    void iniciar() throws IOException {
        servicio = new AlmacenamientoImagenServicio(carpeta.toString());
        servicio.inicializar();
    }

    @Test
    @DisplayName("Guarda imágenes JPG, PNG y WebP con un nombre único y la extensión del formato real")
    void guardaImagenesValidas() {
        String jpg = servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.png", "image/png", JPG));
        String png = servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", PNG));
        String webp = servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.gif", "image/gif", WEBP));

        assertTrue(jpg.endsWith(".jpg"));
        assertTrue(png.endsWith(".png"));
        assertTrue(webp.endsWith(".webp"));
        assertTrue(Files.exists(carpeta.resolve(jpg)));
        assertEquals(3, carpeta.toFile().list().length);
    }

    @Test
    @DisplayName("Achica las imágenes más anchas que el máximo para que carguen más rápido y no toca las chicas")
    void achicaLasImagenesMuyAnchas() throws IOException {
        String ancha = servicio.guardar(imagenReal(2400, 1200, "jpg"));
        String chica = servicio.guardar(imagenReal(800, 400, "png"));

        assertEquals(AlmacenamientoImagenServicio.ANCHO_MAXIMO, ImageIO.read(carpeta.resolve(ancha).toFile()).getWidth());
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

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(falso));
        assertEquals(0, carpeta.toFile().list().length);
    }

    @Test
    @DisplayName("Rechaza un archivo vacío o ausente")
    void rechazaArchivosVacios() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", new byte[0])));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(null));
    }

    @Test
    @DisplayName("Rechaza una imagen que supera los 2 MB")
    void rechazaImagenesMuyGrandes() {
        byte[] grande = new byte[(int) AlmacenamientoImagenServicio.TAMANIO_MAXIMO + 1];
        System.arraycopy(JPG, 0, grande, 0, JPG.length);

        assertThrows(IllegalArgumentException.class,
                () -> servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", grande)));
    }

    @Test
    @DisplayName("Elimina la imagen guardada y no sale de la carpeta de imágenes")
    void eliminaSoloDentroDeLaCarpeta() throws IOException {
        String nombre = servicio.guardar(new MockMultipartFile("imagenArchivo", "foto.jpg", "image/jpeg", JPG));
        Path fuera = Files.createTempFile("fuera", ".txt");

        servicio.eliminar(nombre);
        servicio.eliminar("../" + fuera.getFileName());
        servicio.eliminar(null);

        assertFalse(Files.exists(carpeta.resolve(nombre)));
        assertTrue(Files.exists(fuera), "no debe borrar archivos fuera de la carpeta de imágenes");
        Files.deleteIfExists(fuera);
    }
}
