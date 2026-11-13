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

/** Implementación de OCRService. Detecta los PDF escaneados con OpenPDF y los procesa con OCRmyPDF. */
@Service
public class OCRServiceImpl implements OCRService {

    private static final Logger registro = LoggerFactory.getLogger(OCRServiceImpl.class);

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

    @Value("${idoneos.ocr.idioma:spa+eng}")
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

    /** Los recursos que están en la cola o en proceso, con lo necesario para estimar su avance. */
    private final java.util.Map<Integer, Seguimiento> seguimientos = new java.util.concurrent.ConcurrentHashMap<>();

    /** Páginas de un reconocimiento y el momento en que empezó (0 mientras espera su turno). */
    private static final class Seguimiento {
        private final int paginas;
        private volatile long inicio;
        private volatile int hechas;
        private volatile long ultimoHecho;
        private volatile long inicioPost;
        private volatile String etapa = "optimizando";

        private Seguimiento(int paginas) {
            this.paginas = paginas;
        }
    }

    @Override
    public Progreso progreso(int idRecurso) {
        Seguimiento seguimiento = seguimientos.get(idRecurso);
        return seguimiento == null ? null : estimar(seguimiento);
    }

    /**
     * Informa el avance. Solo el reconocimiento de texto se puede estimar, porque es lo único que ocrmypdf va informando
     * (una línea por página terminada). Las otras etapas no llevan estimación: solo se dice qué se está haciendo.
     */
    private Progreso estimar(Seguimiento s) {
        if (s.inicio == 0) {
            return new Progreso(0, -1, "ocr");
        }
        if (s.inicioPost > 0) {
            return new Progreso(95, -1, s.etapa);
        }
        int hechas = Math.min(s.hechas, s.paginas);
        int porcentaje = (int) Math.round(95.0 * hechas / s.paginas);
        if (hechas < 1) {
            return new Progreso(porcentaje, -1, "ocr");
        }
        // El ritmo es el de las páginas ya terminadas; lo que falta se descuenta con el tiempo desde la última
        double ritmo = (s.ultimoHecho - s.inicio) / 1000.0 / hechas;
        double desdeLaUltima = (System.currentTimeMillis() - s.ultimoHecho) / 1000.0;
        int segundos = (int) Math.max(0, Math.ceil((s.paginas - hechas) * ritmo - desdeLaUltima));
        return new Progreso(porcentaje, segundos, "ocr");
    }

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
    public boolean encolarSiHaceFalta(Recurso recurso) {
        if (!habilitado || recurso == null || !recurso.tieneArchivo() || !"pdf".equals(recurso.getExtension())) {
            return false;
        }
        Path archivo = ManejadorArchivosUtil.resolver(directorioCursos, recurso.getRutaArchivo());
        if (archivo == null || !Files.isRegularFile(archivo)) {
            return false;
        }
        int idRecurso = recurso.getIdRecurso();
        // Un archivo nuevo parte de cero: se borra el estado del anterior
        cambiarEstado(idRecurso, null);
        int paginas = contarPaginasSiNecesitaOcr(archivo);
        if (paginas == 0 || !estaDisponible()) {
            return false;
        }
        long megas = 0;
        try {
            megas = Files.size(archivo) / (1024 * 1024);
        } catch (IOException e) {
            return false;
        }
        if (paginas > maximoDePaginas || megas > maximoDeMegas) {
            registro.warn("OCR: el recurso {} supera los límites ({} páginas, {} MB)", idRecurso, paginas, megas);
            cambiarEstado(idRecurso, EstadoOCR.ERROR);
            return false;
        }
        cambiarEstado(idRecurso, EstadoOCR.PENDIENTE);
        seguimientos.put(idRecurso, new Seguimiento(paginas));
        cola.submit(() -> procesar(idRecurso, archivo));
        return true;
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

    /** Corre OCRmyPDF sobre el archivo. Si sale bien. */
    private void procesar(int idRecurso, Path archivo) {
        Path salida = archivo.resolveSibling(archivo.getFileName() + ".ocr.tmp");
        Seguimiento seguimiento = seguimientos.get(idRecurso);
        try {
            if (seguimiento != null) {
                seguimiento.inicio = System.currentTimeMillis();
            }
            cambiarEstado(idRecurso, EstadoOCR.PROCESANDO);
            correrOcr(archivo, salida, seguimiento);
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
            seguimientos.remove(idRecurso);
            try {
                Files.deleteIfExists(salida);
            } catch (IOException e) {
                registro.debug("OCR: no se pudo borrar el archivo temporal {}", salida);
            }
        }
    }

    /** Corre OCRmyPDF sobre un archivo y deja el resultado en la salida. Lee su registro para saber cuántas páginas lleva. */
    private void correrOcr(Path entrada, Path salida, Seguimiento seguimiento) throws IOException, InterruptedException {
        List<String> orden = new ArrayList<>(List.of(comando, "-v", "1", "--language", idioma, "--skip-text", "--output-type", "pdf",
                "--optimize", "1", "--jobs", "1", entrada.toString(), salida.toString()));
        Process proceso = new ProcessBuilder(orden).redirectErrorStream(true).start();
        try (java.io.BufferedReader lector = new java.io.BufferedReader(
                new java.io.InputStreamReader(proceso.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (seguimiento == null) {
                    continue;
                }
                // Cada página terminada deja una línea "N Grafting" (o "N page already has text"); al final empieza "Postprocessing"
                if (linea.matches("^\\s*\\d+ (Grafting|.*already has text).*")) {
                    seguimiento.hechas++;
                    seguimiento.ultimoHecho = System.currentTimeMillis();
                } else if (linea.startsWith("Postprocessing")) {
                    seguimiento.inicioPost = System.currentTimeMillis();
                } else if (linea.startsWith("Optimize ratio")) {
                    seguimiento.etapa = "guardando";
                }
            }
        }
        if (!proceso.waitFor(minutosDeEspera, TimeUnit.MINUTES)) {
            proceso.destroyForcibly();
            throw new IOException("se agotó el tiempo de espera");
        }
        if (proceso.exitValue() != 0 || !Files.isRegularFile(salida) || Files.size(salida) == 0) {
            throw new IOException("OCRmyPDF terminó con el código " + proceso.exitValue());
        }
    }

    // ----------------------------------------------------------------------
    // Reconocimiento previo: arranca cuando se elige el PDF, antes de guardar el recurso
    // ----------------------------------------------------------------------

    /** Un reconocimiento previo: el archivo temporal, su estado y lo necesario para estimar el avance. */
    private static final class PrevioEnCurso {
        private final Path archivo;
        private final Seguimiento seguimiento;
        private volatile String estado = EstadoOCR.PENDIENTE;

        private PrevioEnCurso(Path archivo, Seguimiento seguimiento) {
            this.archivo = archivo;
            this.seguimiento = seguimiento;
        }
    }

    private final java.util.Map<String, PrevioEnCurso> previos = new java.util.concurrent.ConcurrentHashMap<>();

    private Path carpetaTemporal() throws IOException {
        Path carpeta = directorioCursos.resolve("temp").resolve("ocr");
        Files.createDirectories(carpeta);
        return carpeta;
    }

    /** Borra los archivos temporales de más de seis horas. */
    private void limpiarTemporales(Path carpeta) {
        try (java.util.stream.Stream<Path> archivos = Files.list(carpeta)) {
            long limite = System.currentTimeMillis() - 6L * 60 * 60 * 1000;
            previos.values().removeIf(p -> p.archivo.toFile().lastModified() < limite);
            archivos.filter(a -> a.toFile().lastModified() < limite).forEach(a -> {
                try {
                    Files.deleteIfExists(a);
                } catch (IOException e) {
                    registro.debug("OCR: no se pudo borrar el temporal {}", a);
                }
            });
        } catch (IOException e) {
            registro.debug("OCR: no se pudo limpiar la carpeta temporal: {}", e.getMessage());
        }
    }

    @Override
    public String iniciarPrevio(org.springframework.web.multipart.MultipartFile archivo) throws IOException {
        String nombre = archivo == null || archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        if (!habilitado || archivo == null || archivo.isEmpty() || !nombre.endsWith(".pdf") || !estaDisponible()) {
            return null;
        }
        Path carpeta = carpetaTemporal();
        limpiarTemporales(carpeta);
        String token = java.util.UUID.randomUUID().toString();
        Path destino = carpeta.resolve(token + ".pdf");
        try (java.io.InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        int paginas = contarPaginasSiNecesitaOcr(destino);
        long megas = Files.size(destino) / (1024 * 1024);
        if (paginas == 0 || paginas > maximoDePaginas || megas > maximoDeMegas) {
            Files.deleteIfExists(destino);
            return null;
        }
        PrevioEnCurso previo = new PrevioEnCurso(destino, new Seguimiento(paginas));
        previos.put(token, previo);
        cola.submit(() -> procesarPrevio(token, previo));
        return token;
    }

    private void procesarPrevio(String token, PrevioEnCurso previo) {
        Path salida = previo.archivo.resolveSibling(previo.archivo.getFileName() + ".ocr.tmp");
        try {
            previo.seguimiento.inicio = System.currentTimeMillis();
            previo.estado = EstadoOCR.PROCESANDO;
            correrOcr(previo.archivo, salida, previo.seguimiento);
            Files.move(salida, previo.archivo, StandardCopyOption.REPLACE_EXISTING);
            previo.estado = EstadoOCR.LISTO;
            registro.info("OCR previo {}: el PDF ya tiene texto", token);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            previo.estado = EstadoOCR.ERROR;
        } catch (Exception e) {
            registro.warn("OCR previo {}: falló: {}", token, e.getMessage());
            previo.estado = EstadoOCR.ERROR;
        } finally {
            try {
                Files.deleteIfExists(salida);
            } catch (IOException e) {
                registro.debug("OCR: no se pudo borrar el archivo temporal {}", salida);
            }
        }
    }

    @Override
    public Previo consultarPrevio(String token) {
        PrevioEnCurso previo = token == null ? null : previos.get(token);
        if (previo == null) {
            return null;
        }
        boolean enCurso = EstadoOCR.PENDIENTE.equals(previo.estado) || EstadoOCR.PROCESANDO.equals(previo.estado);
        return new Previo(previo.estado, enCurso ? estimar(previo.seguimiento) : null);
    }

    @Override
    public Path archivoPrevio(String token) {
        PrevioEnCurso previo = token == null ? null : previos.get(token);
        return previo != null && EstadoOCR.LISTO.equals(previo.estado) && Files.isRegularFile(previo.archivo) ? previo.archivo : null;
    }

    @Override
    public org.springframework.web.multipart.MultipartFile reemplazarPorProcesado(String token, org.springframework.web.multipart.MultipartFile original) {
        PrevioEnCurso previo = token == null || token.isBlank() ? null : previos.get(token);
        if (previo == null) {
            return original;
        }
        if (EstadoOCR.PENDIENTE.equals(previo.estado) || EstadoOCR.PROCESANDO.equals(previo.estado)) {
            // Es un error del campo "ocr": el formulario lo muestra en el paso del índice, debajo del aviso del reconocimiento
            throw com.app.idoneos.util.RespuestaFormularioUtil.armarCampoInvalido("ocr",
                    "Error! No se puede guardar el recurso: el reconocimiento de texto del PDF todavía no terminó. Esperá a que finalice.");
        }
        Path procesado = archivoPrevio(token);
        if (procesado == null || original == null || original.isEmpty()) {
            return original;
        }
        return new ArchivoProcesado(procesado, original.getOriginalFilename());
    }

    @Override
    public void descartarPrevio(String token) {
        PrevioEnCurso previo = token == null || token.isBlank() ? null : previos.remove(token);
        if (previo == null) {
            return;
        }
        try {
            Files.deleteIfExists(previo.archivo);
        } catch (IOException e) {
            registro.debug("OCR: no se pudo borrar el temporal {}", previo.archivo);
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
