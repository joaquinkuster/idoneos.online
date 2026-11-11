package com.app.idoneos.service.ocr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;

/** Archivo PDF que ya pasó por el reconocimiento de texto antes de guardar el recurso, y que se guarda como si lo hubiera subido el usuario. */
public final class ArchivoProcesado implements MultipartFile {

    private final Path archivo;
    private final String nombreOriginal;

    /**
     * Crea el archivo procesado.
     *
     * @param archivo La ruta del PDF ya procesado.
     * @param nombreOriginal El nombre con el que lo subió el usuario.
     */
    public ArchivoProcesado(Path archivo, String nombreOriginal) {
        this.archivo = archivo;
        this.nombreOriginal = nombreOriginal;
    }

    @Override
    public String getName() {
        return "archivo";
    }

    @Override
    public String getOriginalFilename() {
        return nombreOriginal;
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public long getSize() {
        try {
            return Files.size(archivo);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public byte[] getBytes() throws IOException {
        return Files.readAllBytes(archivo);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(archivo);
    }

    @Override
    public void transferTo(File destino) throws IOException {
        Files.copy(archivo, destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
