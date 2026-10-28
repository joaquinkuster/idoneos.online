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
import java.util.Locale;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

/**
 * Gestiona los archivos del sistema en el sistema de archivos local del servidor (fuera del código de la
 * aplicación): materiales (videos, documentos, presentaciones) e imágenes. Valida por contenido real, guarda,
 * elimina y arma las rutas de las carpetas de cada curso y tema:
 * {@code {base}/curso{id}/} (imagen del curso) y {@code {base}/curso{id}/tema{id}/} (archivos del tema).
 * Las rutas que se guardan en la base de datos son relativas a la carpeta base (los materiales) o solo el nombre
 * del archivo (las imágenes); se publican según {@code WebConfiguracion}.
 */
public class ManejadorArchivosUtilidad {

    /**
     * Tamaño máximo permitido para una imagen, en bytes (2 MB).
     */
    public static final long TAMANIO_MAXIMO_IMAGEN = 2L * 1024 * 1024;

    /**
     * Ancho máximo, en píxeles, con el que se guardan las imágenes (las más anchas se achican).
     */
    public static final int ANCHO_MAXIMO_IMAGEN = 1280;

    private static final List<String> EXTENSIONES_IMAGEN = List.of("jpg", "png", "webp");

    /**
     * Obtiene la carpeta de un curso.
     *
     * @param base    La carpeta base de los cursos.
     * @param idCurso El identificador del curso.
     * @return La carpeta {@code curso{id}} dentro de la carpeta base.
     */
    public static Path carpetaDeCurso(Path base, int idCurso) {
        return base.resolve("curso" + idCurso);
    }

    /**
     * Obtiene la carpeta de un tema.
     *
     * @param base     La carpeta base de los cursos.
     * @param idCurso  El identificador del curso del tema.
     * @param idTema El identificador del tema.
     * @return La carpeta {@code curso{id}/tema{id}} dentro de la carpeta base.
     */
    public static Path carpetaDeTema(Path base, int idCurso, int idTema) {
        return carpetaDeCurso(base, idCurso).resolve("tema" + idTema);
    }

    /**
     * Indica si una ruta guardada es un enlace externo (en lugar de un archivo del servidor).
     *
     * @param ruta La ruta guardada.
     * @return {@code true} si empieza con http:// o https://.
     */
    public static boolean esEnlaceExterno(String ruta) {
        return ruta != null && (ruta.startsWith("http://") || ruta.startsWith("https://"));
    }

    /**
     * Valida un archivo sin guardarlo: que no esté vacío, que no supere el tamaño máximo y que su extensión sea
     * una de las permitidas y coincida con el contenido real del archivo (no alcanza con renombrarlo).
     *
     * @param archivo                El archivo subido por el usuario.
     * @param tamanioMaximo          El tamaño máximo permitido, en bytes.
     * @param extensionesPermitidas  Las extensiones permitidas, en minúscula y sin punto.
     * @return La extensión del archivo, en minúscula.
     * @throws IllegalArgumentException Si el archivo no cumple alguna condición, con el motivo.
     */
    public static String validar(MultipartFile archivo, long tamanioMaximo, List<String> extensionesPermitidas) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Error! El archivo seleccionado está vacío.");
        }
        if (archivo.getSize() > tamanioMaximo) {
            throw new IllegalArgumentException(
                    MensajeErrorUtilidad.demasiadoGrande("El archivo", tamanioMaximo / (1024 * 1024), "MB"));
        }
        String formatos = descripcionDeFormatos(extensionesPermitidas);
        String extension = extensionDe(archivo.getOriginalFilename());
        if (extension == null || !extensionesPermitidas.contains(extension)) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.formatoInvalido("El archivo", formatos));
        }
        try (InputStream entrada = archivo.getInputStream()) {
            if (!firmaCoincide(entrada.readNBytes(12), extension)) {
                throw new IllegalArgumentException(MensajeErrorUtilidad.formatoInvalido("El archivo", formatos));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo leer el archivo. Intente nuevamente.");
        }
        return extension;
    }

    /**
     * Valida y guarda un archivo con el nombre indicado (más su extensión). La carpeta se crea si no existe y, si
     * ya había un archivo con ese nombre, se reemplaza.
     *
     * @param archivo               El archivo subido por el usuario.
     * @param directorio            La carpeta donde se guarda el archivo.
     * @param nombre                El nombre del archivo, sin extensión.
     * @param tamanioMaximo         El tamaño máximo permitido, en bytes.
     * @param extensionesPermitidas Las extensiones permitidas, en minúscula y sin punto.
     * @return El nombre con el que se guardó el archivo (nombre más extensión).
     * @throws IllegalArgumentException Si el archivo no es válido o no se pudo guardar.
     */
    public static String guardar(MultipartFile archivo, Path directorio, String nombre, long tamanioMaximo,
            List<String> extensionesPermitidas) {
        String extension = validar(archivo, tamanioMaximo, extensionesPermitidas);
        try {
            Path carpeta = crearCarpeta(directorio);
            copiar(archivo, carpeta.resolve(nombre + "." + extension));
            return nombre + "." + extension;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo guardar el archivo. Intente nuevamente.");
        }
    }

    /**
     * Elimina un archivo guardado (material o imagen), indicado por su ruta relativa a la carpeta base. Si no
     * existe o es un enlace externo, no hace nada. Nunca elimina archivos fuera de la carpeta base.
     *
     * @param rutaRelativa La ruta guardada del archivo (o el nombre, si es una imagen de la carpeta indicada).
     * @param base         La carpeta base (o la carpeta de la imagen).
     */
    public static void eliminar(String rutaRelativa, Path base) {
        if (rutaRelativa == null || rutaRelativa.isBlank() || esEnlaceExterno(rutaRelativa)) {
            return;
        }
        try {
            Path carpeta = base.toAbsolutePath().normalize();
            Path archivo = carpeta.resolve(rutaRelativa).normalize();
            if (archivo.startsWith(carpeta)) {
                Files.deleteIfExists(archivo);
            }
        } catch (IOException e) {
            // Un archivo que no se puede borrar no debe impedir la operación en curso.
        }
    }

    /**
     * Obtiene la extensión de un nombre de archivo.
     *
     * @param nombre El nombre original del archivo.
     * @return La extensión en minúscula y sin punto, o {@code null} si no tiene.
     */
    public static String extensionDe(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return null;
        }
        return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static String descripcionDeFormatos(List<String> extensiones) {
        List<String> nombres = extensiones.stream().map(e -> e.toUpperCase(Locale.ROOT)).toList();
        if (nombres.size() == 1) {
            return nombres.get(0);
        }
        return String.join(", ", nombres.subList(0, nombres.size() - 1)) + " o " + nombres.get(nombres.size() - 1);
    }

    /**
     * Verifica que el contenido real del archivo corresponda a su extensión, mirando su cabecera.
     */
    private static boolean firmaCoincide(byte[] cabecera, String extension) {
        return switch (extension) {
            case "pdf" -> empieza(cabecera, 0, '%', 'P', 'D', 'F');
            case "mp4" -> empieza(cabecera, 4, 'f', 't', 'y', 'p');
            case "webm" -> empieza(cabecera, 0, 0x1A, 0x45, 0xDF, 0xA3);
            case "pptx", "docx" -> empieza(cabecera, 0, 'P', 'K', 0x03, 0x04);
            case "ppt", "doc" -> empieza(cabecera, 0, 0xD0, 0xCF, 0x11, 0xE0);
            default -> false;
        };
    }

    private static boolean empieza(byte[] cabecera, int desde, int... esperado) {
        if (cabecera.length < desde + esperado.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOfRange(cabecera, desde, desde + esperado.length), aBytes(esperado));
    }

    private static byte[] aBytes(int[] valores) {
        byte[] bytes = new byte[valores.length];
        for (int i = 0; i < valores.length; i++) {
            bytes[i] = (byte) valores[i];
        }
        return bytes;
    }

    /**
     * Obtiene la ruta de un archivo relativa a la carpeta base, con barras normales (es la que se guarda en la
     * base de datos).
     *
     * @param base    La carpeta base de los cursos.
     * @param archivo La ruta del archivo guardado.
     * @return La ruta relativa, por ejemplo {@code curso3/tema12/material45.mp4}.
     */
    public static String rutaRelativa(Path base, Path archivo) {
        return base.toAbsolutePath().normalize().relativize(archivo.toAbsolutePath().normalize()).toString()
                .replace('\\', '/');
    }

    /**
     * Obtiene la ruta completa de un archivo a partir de su ruta relativa guardada.
     *
     * @param base          La carpeta base de los cursos.
     * @param rutaRelativa  La ruta guardada del archivo.
     * @return La ruta del archivo, o {@code null} si no es un archivo del servidor o su ruta sale de la carpeta base.
     */
    public static Path resolver(Path base, String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank() || esEnlaceExterno(rutaRelativa)) {
            return null;
        }
        Path carpeta = base.toAbsolutePath().normalize();
        Path archivo = carpeta.resolve(rutaRelativa).normalize();
        return archivo.startsWith(carpeta) ? archivo : null;
    }

    // ---------------------------------------------------------------- Imágenes

    /**
     * Valida una imagen sin guardarla. Solo se aceptan imágenes JPG, PNG o WebP de hasta 2 MB; el formato se
     * verifica con el contenido real del archivo y no solo con su extensión o su tipo declarado.
     *
     * @param archivo El archivo subido por el usuario.
     * @return La extensión que corresponde al formato real de la imagen (jpg, png o webp).
     * @throws IllegalArgumentException Si el archivo está vacío, supera el tamaño máximo o no es una imagen válida.
     */
    public static String validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Error! La imagen seleccionada está vacía.");
        }
        if (archivo.getSize() > TAMANIO_MAXIMO_IMAGEN) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.demasiadoGrande("La imagen", 2, "MB"));
        }
        try (InputStream entrada = archivo.getInputStream()) {
            String extension = detectarExtensionDeImagen(entrada.readNBytes(12));
            if (extension == null) {
                throw new IllegalArgumentException(MensajeErrorUtilidad.formatoInvalido("La imagen", "JPG, PNG o WebP"));
            }
            return extension;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo leer la imagen. Intente nuevamente.");
        }
    }

    /**
     * Valida y guarda una imagen con el nombre indicado (más la extensión de su formato real). La carpeta se crea
     * si no existe. Si ya había una imagen con ese nombre, se reemplaza, aunque tuviera otra extensión. Las
     * imágenes más anchas que {@link #ANCHO_MAXIMO_IMAGEN} se achican.
     *
     * @param archivo    El archivo subido por el usuario.
     * @param directorio La carpeta donde se guarda la imagen.
     * @param nombre     El nombre del archivo, sin extensión.
     * @return El nombre con el que se guardó el archivo (nombre más extensión).
     * @throws IllegalArgumentException Si el archivo está vacío, supera el tamaño máximo o no es una imagen válida.
     */
    public static String guardarImagen(MultipartFile archivo, Path directorio, String nombre) {
        String extension = validarImagen(archivo);
        try {
            Path carpeta = crearCarpeta(directorio);
            Path destino = carpeta.resolve(nombre + "." + extension);
            try (InputStream completa = archivo.getInputStream()) {
                if (!guardarAchicada(completa, extension, destino)) {
                    copiar(archivo, destino);
                }
            }
            for (String otra : EXTENSIONES_IMAGEN) {
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
     * Si la imagen (JPG o PNG) es más ancha que {@link #ANCHO_MAXIMO_IMAGEN}, la guarda achicada para que cargue
     * más rápido.
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
            if (imagen == null || imagen.getWidth() <= ANCHO_MAXIMO_IMAGEN) {
                return false;
            }
            int alto = Math.round(imagen.getHeight() * (ANCHO_MAXIMO_IMAGEN / (float) imagen.getWidth()));
            int tipo = extension.equals("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage achicada = new BufferedImage(ANCHO_MAXIMO_IMAGEN, alto, tipo);
            Graphics2D grafico = achicada.createGraphics();
            grafico.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            grafico.drawImage(imagen, 0, 0, ANCHO_MAXIMO_IMAGEN, alto, null);
            grafico.dispose();
            return ImageIO.write(achicada, extension, destino.toFile());
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static String detectarExtensionDeImagen(byte[] cabecera) {
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

    // ---------------------------------------------------------------- Comunes

    private static Path crearCarpeta(Path directorio) throws IOException {
        Path carpeta = directorio.toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
        return carpeta;
    }

    private static void copiar(MultipartFile archivo, Path destino) throws IOException {
        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
