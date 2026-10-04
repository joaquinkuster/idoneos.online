package com.app.idoneos.servicio.Almacenamiento;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

/**
 * Servicio que guarda y elimina las imágenes de portada de los cursos en una carpeta del sistema de archivos
 * del servidor, fuera del código de la aplicación.
 *
 * La carpeta es configurable con la propiedad {@code idoneos.directorio-imagenes} (por defecto
 * {@code ./uploads/cursos}) y se publica en la URL {@code /img/cursos/**} (ver {@code WebConfig}).
 * En la base de datos solo se guarda el nombre del archivo.
 */
@Service
public class AlmacenamientoImagenServicio {

    /**
     * Tamaño máximo permitido para una imagen, en bytes (2 MB).
     */
    public static final long TAMANIO_MAXIMO = 2L * 1024 * 1024;

    /**
     * Ancho máximo, en píxeles, con el que se guardan las imágenes (las más anchas se achican).
     */
    public static final int ANCHO_MAXIMO = 1280;

    private final Path directorio;

    /**
     * Crea el servicio con la carpeta de imágenes configurada.
     *
     * @param directorio La ruta de la carpeta donde se guardan las imágenes.
     */
    public AlmacenamientoImagenServicio(
            @Value("${idoneos.directorio-imagenes:./uploads/cursos}") String directorio) {
        this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
    }

    /**
     * Crea la carpeta de imágenes si todavía no existe.
     *
     * @throws IOException Si no se puede crear la carpeta.
     */
    @PostConstruct
    public void inicializar() throws IOException {
        Files.createDirectories(directorio);
    }

    /**
     * Obtiene la carpeta donde se guardan las imágenes.
     *
     * @return La ruta absoluta de la carpeta de imágenes.
     */
    public Path getDirectorio() {
        return directorio;
    }

    /**
     * Valida y guarda una imagen con un nombre único generado por el sistema.
     * Solo se aceptan imágenes JPG, PNG o WebP de hasta 2 MB; el formato se verifica con el contenido real
     * del archivo y no solo con su extensión o su tipo declarado.
     *
     * @param archivo El archivo subido por el usuario.
     * @return El nombre con el que se guardó el archivo (UUID más extensión).
     * @throws IllegalArgumentException Si el archivo está vacío, supera el tamaño máximo o no es una imagen válida.
     */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Error! La imagen seleccionada está vacía.");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException("Error! La imagen no puede superar los 2 MB.");
        }
        try (InputStream entrada = archivo.getInputStream()) {
            byte[] cabecera = entrada.readNBytes(12);
            String extension = detectarExtension(cabecera);
            if (extension == null) {
                throw new IllegalArgumentException("Error! La imagen debe estar en formato JPG, PNG o WebP.");
            }
            String nombre = UUID.randomUUID() + "." + extension;
            Path destino = directorio.resolve(nombre);
            try (InputStream completa = archivo.getInputStream()) {
                if (!guardarAchicada(completa, extension, destino)) {
                    try (InputStream original = archivo.getInputStream()) {
                        Files.copy(original, destino);
                    }
                }
            }
            return nombre;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo guardar la imagen. Intente nuevamente.");
        }
    }

    /**
     * Elimina una imagen guardada. Si el archivo no existe en la carpeta de imágenes (por ejemplo, una imagen
     * de ejemplo incluida en la aplicación), no hace nada.
     *
     * @param nombre El nombre del archivo a eliminar.
     */
    public void eliminar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            Path archivo = directorio.resolve(nombre).normalize();
            if (archivo.startsWith(directorio)) {
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
    private boolean guardarAchicada(InputStream entrada, String extension, Path destino) {
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

    private String detectarExtension(byte[] cabecera) {
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
