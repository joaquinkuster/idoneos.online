package com.app.idoneos.utilidades;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

/**
 * Métodos auxiliares para guardar y eliminar imágenes en una carpeta del sistema de archivos del servidor,
 * fuera del código de la aplicación. Sirve a cualquier entidad que tenga una imagen: cada una indica la carpeta
 * y el nombre con el que guarda la suya (por ejemplo, la imagen de un curso es {@code uploads/curso{id}/imagen.png}
 * y se publica en {@code /uploads/curso{id}/imagen.png}, ver {@code WebConfig}).
 * En la base de datos solo se guarda el nombre del archivo.
 */
public class ImagenUtilidad {

    /**
     * Tamaño máximo permitido para una imagen, en bytes (2 MB).
     */
    public static final long TAMANIO_MAXIMO = 2L * 1024 * 1024;

    /**
     * Ancho máximo, en píxeles, con el que se guardan las imágenes (las más anchas se achican).
     */
    public static final int ANCHO_MAXIMO = 1280;

    private static final List<String> EXTENSIONES = List.of("jpg", "png", "webp");

    /**
     * Valida una imagen sin guardarla. Solo se aceptan imágenes JPG, PNG o WebP de hasta 2 MB; el formato se
     * verifica con el contenido real del archivo y no solo con su extensión o su tipo declarado.
     *
     * @param archivo El archivo subido por el usuario.
     * @return La extensión que corresponde al formato real de la imagen (jpg, png o webp).
     * @throws IllegalArgumentException Si el archivo está vacío, supera el tamaño máximo o no es una imagen válida.
     */
    public static String validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Error! La imagen seleccionada está vacía.");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("Error! La imagen no puede superar los 2 MB.");
        }
        try (InputStream entrada = archivo.getInputStream()) {
            String extension = detectarExtension(entrada.readNBytes(12));
            if (extension == null) {
                throw new IllegalArgumentException("Error! La imagen debe estar en formato JPG, PNG o WebP.");
            }
            return extension;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo leer la imagen. Intente nuevamente.");
        }
    }

    /**
     * Valida y guarda una imagen con el nombre indicado (más la extensión de su formato real). La carpeta se crea
     * si no existe. Si ya había una imagen con ese nombre, se reemplaza, aunque tuviera otra extensión.
     *
     * @param archivo    El archivo subido por el usuario.
     * @param directorio La carpeta donde se guarda la imagen.
     * @param nombre     El nombre del archivo, sin extensión.
     * @return El nombre con el que se guardó el archivo (nombre más extensión).
     * @throws IllegalArgumentException Si el archivo está vacío, supera el tamaño máximo o no es una imagen válida.
     */
    public static String guardar(MultipartFile archivo, Path directorio, String nombre) {
        String extension = validar(archivo);
        try {
            Path carpeta = directorio.toAbsolutePath().normalize();
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve(nombre + "." + extension);
            try (InputStream completa = archivo.getInputStream()) {
                if (!guardarAchicada(completa, extension, destino)) {
                    try (InputStream original = archivo.getInputStream()) {
                        Files.copy(original, destino, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            for (String otra : EXTENSIONES) {
                if (!otra.equals(extension)) {
                    Files.deleteIfExists(carpeta.resolve(nombre + "." + otra));
                }
            }
            return nombre + "." + extension;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo guardar la imagen. Intente nuevamente.");
        }
    }

    /**
     * Elimina una imagen guardada. Si el archivo no existe en la carpeta, no hace nada. Nunca elimina archivos
     * fuera de la carpeta indicada.
     *
     * @param nombre     El nombre del archivo a eliminar.
     * @param directorio La carpeta donde está guardada la imagen.
     */
    public static void eliminar(String nombre, Path directorio) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            Path carpeta = directorio.toAbsolutePath().normalize();
            Path archivo = carpeta.resolve(nombre).normalize();
            if (archivo.startsWith(carpeta)) {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException e) {
            // Un archivo que no se puede borrar no debe impedir la operación en curso.
        }
    }

    /**
     * Si la imagen (JPG o PNG) es más ancha que {@link #ANCHO_MAXIMO}, la guarda achicada para que cargue más rápido.
     *
     * @return {@code true} si guardó la imagen achicada; {@code false} si no hizo falta (o no pudo) y debe
     *         guardarse el archivo original.
     */
    private static boolean guardarAchicada(InputStream entrada, String extension, Path destino) {
        if (!extension.equals("jpg") && !extension.equals("png")) {
            return false;
        }
        try {
            BufferedImage imagen = ImageIO.read(entrada);
            if (imagen == null || imagen.getWidth() <= ANCHO_MAXIMO) {
                return false;
            }
            int alto = Math.round(imagen.getHeight() * (ANCHO_MAXIMO / (float) imagen.getWidth()));
            int tipo = extension.equals("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage achicada = new BufferedImage(ANCHO_MAXIMO, alto, tipo);
            Graphics2D grafico = achicada.createGraphics();
            grafico.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            grafico.drawImage(imagen, 0, 0, ANCHO_MAXIMO, alto, null);
            grafico.dispose();
            return ImageIO.write(achicada, extension, destino.toFile());
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static String detectarExtension(byte[] cabecera) {
        if (cabecera.length >= 3 && (cabecera[0] & 0xFF) == 0xFF && (cabecera[1] & 0xFF) == 0xD8
                && (cabecera[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        byte[] firmaPng = { (byte) 0x89, 'P', 'N', 'G' };
        if (cabecera.length >= 4 && Arrays.equals(Arrays.copyOf(cabecera, 4), firmaPng)) {
            return "png";
        }
        if (cabecera.length >= 12 && new String(cabecera, 0, 4).equals("RIFF")
                && new String(cabecera, 8, 4).equals("WEBP")) {
            return "webp";
        }
        return null;
    }
}
