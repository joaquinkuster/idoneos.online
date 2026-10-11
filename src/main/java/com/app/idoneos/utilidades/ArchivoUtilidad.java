package com.app.idoneos.utilidades;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

/**
 * Métodos auxiliares para guardar y eliminar archivos (videos, documentos, presentaciones) en el sistema de
 * archivos del servidor, y para armar las rutas de las carpetas de cada curso y unidad:
 * {@code {base}/curso{id}/} (imagen del curso) y {@code {base}/curso{id}/unidad{id}/} (archivos de la unidad).
 * Las rutas que se guardan en la base de datos son siempre relativas a la carpeta base.
 */
public class ArchivoUtilidad {

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
     * Obtiene la carpeta de una unidad.
     *
     * @param base     La carpeta base de los cursos.
     * @param idCurso  El identificador del curso de la unidad.
     * @param idUnidad El identificador de la unidad.
     * @return La carpeta {@code curso{id}/unidad{id}} dentro de la carpeta base.
     */
    public static Path carpetaDeUnidad(Path base, int idCurso, int idUnidad) {
        return carpetaDeCurso(base, idCurso).resolve("unidad" + idUnidad);
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
                    "Error! El archivo no puede superar los " + (tamanioMaximo / (1024 * 1024)) + " MB.");
        }
        String formatos = descripcionDeFormatos(extensionesPermitidas);
        String extension = extensionDe(archivo.getOriginalFilename());
        if (extension == null || !extensionesPermitidas.contains(extension)) {
            throw new IllegalArgumentException("Error! El archivo debe estar en formato " + formatos + ".");
        }
        try (InputStream entrada = archivo.getInputStream()) {
            if (!firmaCoincide(entrada.readNBytes(12), extension)) {
                throw new IllegalArgumentException("Error! El archivo debe estar en formato " + formatos + ".");
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
            Path carpeta = directorio.toAbsolutePath().normalize();
            Files.createDirectories(carpeta);
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, carpeta.resolve(nombre + "." + extension), StandardCopyOption.REPLACE_EXISTING);
            }
            return nombre + "." + extension;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error! No se pudo guardar el archivo. Intente nuevamente.");
        }
    }

    /**
     * Elimina un archivo guardado, indicado por su ruta relativa a la carpeta base. Si no existe o es un enlace
     * externo, no hace nada. Nunca elimina archivos fuera de la carpeta base.
     *
     * @param rutaRelativa La ruta guardada del archivo.
     * @param base         La carpeta base de los cursos.
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
     * @return La ruta relativa, por ejemplo {@code curso3/unidad12/material45.mp4}.
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
}
