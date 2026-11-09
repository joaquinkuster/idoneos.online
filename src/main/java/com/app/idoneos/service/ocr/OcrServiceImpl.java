package com.app.idoneos.service.ocr;

import com.app.idoneos.model.EstadoOCR;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.repository.EstadoOCRRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Implementación de {@link OcrService}: detecta los PDF escaneados con OpenPDF y los procesa con OCRmyPDF, de a uno por vez.
 */
@Service
public class OcrServiceImpl implements OcrService {

    private static final Logger registro = LoggerFactory.getLogger(OcrServiceImpl.class);

    /** Páginas que se muestrean para decidir si el PDF tiene texto. */
    private static final int PAGINAS_DE_MUESTRA = 8;

    /** Caracteres por página por debajo de los cuales una página se considera sin texto. */
    private static final int MINIMO_DE_CARACTERES_POR_PAGINA = 25;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private EstadoOCRRepository estadoRepository;

    @Autowired
    private TransactionTemplate transacciones;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Value("${idoneos.ocr.habilitado:true}")
    private boolean habilitado;

    @Value("${idoneos.ocr.comando:ocrmypdf}")
    private String comando;

    @Value("${idoneos.ocr.idioma:spa}")
    private String idioma;

    @Value("${idoneos.ocr.max-paginas:300}")
    private int maximoDePaginas;

    @Value("${idoneos.ocr.max-mb:50}")
    private long maximoDeMegas;

    @Value("${idoneos.ocr.timeout-minutos:60}")
    private long minutosDeEspera;

    /** El OCR corre de a un documento por vez, para no saturar el servidor. */
    private final ExecutorService cola = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "ocr-pdf");
        hilo.setDaemon(true);
        return hilo;
    });

    private volatile Boolean disponible;

    @PreDestroy
    void cerrar() {
        cola.shutdownNow();
    }

    @Override
    public boolean estaDisponible() {
        if (!habilitado) {
            return false;
        }
        if (disponible == null) {
            disponible = comprobarComando();
            if (!disponible) {
                registro.warn("OCR: no se encontró el comando «{}»: los PDF escaneados se guardan sin reconocimiento de texto.", comando);
            }
        }
        return disponible;
    }

    private boolean comprobarComando() {
        try {
            Process proceso = new ProcessBuilder(comando, "--version").redirectErrorStream(true).start();
            proceso.getInputStream().readAllBytes();
            return proceso.waitFor(20, TimeUnit.SECONDS) && proceso.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void encolarSiHaceFalta(Recurso recurso) {
        if (!habilitado || recurso == null || !recurso.tieneArchivo() || !"pdf".equals(recurso.getExtension())) {
            return;
        }
        Path archivo = ManejadorArchivosUtil.resolver(directorioCursos, recurso.getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            return;
        }
        int idRecurso = recurso.getIdRecurso();
        // Un archivo nuevo parte de cero: se borra el estado del anterior
        cambiarEstado(idRecurso, null);
        int paginas = contarPaginasSiNecesitaOcr(archivo);
        if (paginas == 0 || !estaDisponible()) {
            return;
        }
        long megas = 0;
        try {
            megas = Files.size(archivo) / (1024 * 1024);
        } catch (IOException e) {
            return;
        }
        if (paginas > maximoDePaginas || megas > maximoDeMegas) {
            registro.warn("OCR: el recurso {} supera los límites ({} páginas, {} MB)", idRecurso, paginas, megas);
            cambiarEstado(idRecurso, EstadoOCR.ERROR);
            return;
        }
        cambiarEstado(idRecurso, EstadoOCR.PENDIENTE);
        cola.submit(() -> procesar(idRecurso, archivo));
    }

    /** Devuelve la cantidad de páginas del PDF si es un escaneo (las páginas de muestra casi no tienen texto) y 0 si tiene texto o no se puede leer. */
    private int contarPaginasSiNecesitaOcr(Path archivo) {
        PdfReader lector = null;
        try {
            lector = new PdfReader(archivo.toString());
            int paginas = lector.getNumberOfPages();
            if (paginas < 1) {
                return 0;
            }
            PdfTextExtractor extractor = new PdfTextExtractor(lector);
            int muestra = Math.min(paginas, PAGINAS_DE_MUESTRA);
            long caracteres = 0;
            for (int i = 0; i < muestra; i++) {
                int pagina = muestra == paginas ? i + 1 : 1 + (int) ((long) i * (paginas - 1) / (muestra - 1));
                caracteres += extractor.getTextFromPage(pagina).trim().length();
            }
            return caracteres < (long) MINIMO_DE_CARACTERES_POR_PAGINA * muestra ? paginas : 0;
        } catch (Exception e) {
            registro.debug("OCR: no se pudo leer el PDF para ver si tiene texto: {}", e.getMessage());
            return 0;
        } finally {
            if (lector != null) {
                lector.close();
            }
        }
    }

    /** Corre OCRmyPDF sobre el archivo; si sale bien, el resultado reemplaza al original y el recurso pasa a «Listo». */
    private void procesar(int idRecurso, Path archivo) {
        Path salida = archivo.resolveSibling(archivo.getFileName() + ".ocr.tmp");
        try {
            cambiarEstado(idRecurso, EstadoOCR.PROCESANDO);
            List<String> orden = new ArrayList<>(List.of(comando, "--language", idioma, "--skip-text", "--output-type", "pdf",
                    "--optimize", "1", "--jobs", "1", archivo.toString(), salida.toString()));
            Process proceso = new ProcessBuilder(orden).redirectErrorStream(true).start();
            proceso.getInputStream().readAllBytes();
            if (!proceso.waitFor(minutosDeEspera, TimeUnit.MINUTES)) {
                proceso.destroyForcibly();
                throw new IOException("se agotó el tiempo de espera");
            }
            if (proceso.exitValue() != 0 || !Files.isRegularFile(salida) || Files.size(salida) == 0) {
                throw new IOException("OCRmyPDF terminó con el código " + proceso.exitValue());
            }
            Files.move(salida, archivo, StandardCopyOption.REPLACE_EXISTING);
            long tamanio = Files.size(archivo);
            transacciones.executeWithoutResult(t -> recursoRepository.findById(idRecurso).ifPresent(r -> {
                r.actualizarTamanioBytes(tamanio);
                r.setEstadoOcr(buscarEstado(EstadoOCR.LISTO));
                recursoRepository.save(r);
            }));
            registro.info("OCR: el recurso {} ya tiene texto", idRecurso);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            registro.warn("OCR: falló el recurso {}: {}", idRecurso, e.getMessage());
            cambiarEstado(idRecurso, EstadoOCR.ERROR);
        } finally {
            try {
                Files.deleteIfExists(salida);
            } catch (IOException e) {
                registro.debug("OCR: no se pudo borrar el archivo temporal {}", salida);
            }
        }
    }

    private EstadoOCR buscarEstado(String nombre) {
        return nombre == null ? null : estadoRepository.findByNombre(nombre).orElseThrow();
    }

    private void cambiarEstado(int idRecurso, String nombre) {
        transacciones.executeWithoutResult(t -> recursoRepository.findById(idRecurso).ifPresent(r -> {
            r.setEstadoOcr(buscarEstado(nombre));
            recursoRepository.save(r);
        }));
    }
}
