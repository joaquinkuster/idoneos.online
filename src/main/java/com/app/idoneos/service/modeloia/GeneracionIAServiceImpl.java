package com.app.idoneos.service.modeloia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.app.idoneos.model.Bibliografia;
import com.app.idoneos.model.CategoriaMaterialLectura;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.EstadoGeneracionIA;
import com.app.idoneos.model.EstadoOCR;
import com.app.idoneos.model.MaterialLectura;
import com.app.idoneos.model.OpcionRespuesta;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Pool;
import com.app.idoneos.model.Pregunta;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.repository.CategoriaMaterialLecturaRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.EstadoGeneracionIARepository;
import com.app.idoneos.repository.EstadoOCRRepository;
import com.app.idoneos.repository.MaterialLecturaRepository;
import com.app.idoneos.repository.OpcionRespuestaRepository;
import com.app.idoneos.repository.PoolRepository;
import com.app.idoneos.repository.PreguntaRepository;
import com.app.idoneos.repository.RecursoRepository;
import com.app.idoneos.repository.TemaRecursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.repository.TerminoGlosarioRepository;
import com.app.idoneos.service.indice.IndiceRecursoService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.MarcadoresPdfUtil.Marcador;
import com.app.idoneos.service.recurso.RecursoService.TipoRecurso;
import com.app.idoneos.util.EntidadesUtil;
import com.app.idoneos.util.EstructuraPdfUtil;
import com.app.idoneos.util.GramaticaUtil;
import com.app.idoneos.util.FidelidadTextoUtil;
import com.app.idoneos.util.LogicaPreguntasAurea;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.OracionesUtil;
import com.app.idoneos.util.PalabrasClaveUtil;
import com.app.idoneos.util.PresentacionPdfUtil;
import com.app.idoneos.util.PresentacionPdfUtil.Presentacion;
import com.app.idoneos.util.ResumenPdfUtil;
import com.app.idoneos.util.ResumenPdfUtil.Resumen;
import com.app.idoneos.util.TextoPdfUtil;

/** Implementación de GeneracionIAService con el modelo local y una cola de un solo hilo. */
@Service
public class GeneracionIAServiceImpl implements GeneracionIAService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(GeneracionIAServiceImpl.class);

    /** Largo máximo, en tokens, del texto libre del resumen y de la presentación, que es el que usa AUREA. */
    private static final int TOKENS_TEXTO = 512;

    /** Cantidad máxima de partes (secciones) en que se divide el material: si hay más, se juntan las vecinas para no alargar el tiempo. */
    private static final int MAX_PARTES = 8;

    /** Medidas de una diapositiva, según la letra del PDF, para saber cuántos puntos entran: caracteres por línea, alto de línea, espacio entre puntos y alto útil. */
    private static final int CARACTERES_POR_LINEA = 58;
    private static final int ALTO_LINEA = 31;
    private static final int SEPARACION_PUNTOS = 16;
    private static final int ALTO_UTIL_DIAPOSITIVA = 340;

    /** Tokens mínimos de lo que escribe el modelo por cada parte del resumen. */
    private static final int TOKENS_MINIMOS_PARTE = 60;

    /** Cuántas palabras entran en un token, más o menos, para pedirle al modelo el máximo en palabras. */
    private static final double PALABRAS_POR_TOKEN = 0.55;

    /** Largo máximo del motivo de un error, que es el de la columna. */
    private static final int LARGO_DETALLE = 500;

    /** Parecido a partir del cual un punto o un enunciado se considera repetido. */
    private static final double SIMILITUD_MAXIMA = 0.5;

    /** Parte mínima de las palabras de una oración del resumen que tiene que estar en el material. Es menor porque un resumen parafrasea. */
    private static final double COBERTURA_MINIMA_RESUMEN = 0.5;

    /** Parte mínima de las palabras de una oración de introducción o conclusiones que tiene que estar en la síntesis. Es menor porque retoma las ideas con otras palabras. */
    private static final double COBERTURA_MINIMA_SINTESIS = 0.35;

    /**
     * Lo que se le pide al modelo para redactar el resumen, con el formato de AUREA: texto plano en introducción, desarrollo y conclusiones.
     * No se le dice el tema, para que no escriba de lo que ya sabe.
     */
    private static final String INSTRUCCIONES_RESUMEN = """
            Eres un redactor académico experto. Tu tarea es generar un resumen coherente, fluido y bien estructurado de un material de estudio a partir de su texto.
            Usá EXCLUSIVAMENTE el texto que recibís. No uses conocimientos propios ni agregues datos, definiciones, ejemplos o explicaciones que no estén en él.
            Resumí lo que el documento dice, aunque trate de otra cosa que lo que esperabas: no expliques el tema en general ni lo que sabés de él.
            El texto debe tener sentido completo, conectando las ideas de manera lógica.
            Formato de salida: TEXTO PLANO, sin Markdown, sin HTML y sin viñetas.
            Estructura requerida, con cada parte empezando en un renglón con su nombre:
            Introducción: de qué trata el documento.
            Desarrollo: los temas principales explicados con cohesión, sin repetir ideas.
            Conclusiones: cierre sintetizando lo central.
            IMPORTANTE: redactá en tercera persona, con un tono formal y educativo. Evitá frases como "En este texto" o "El autor dijo".""";

    /** Lo que se le pide al modelo para resumir una sección del documento. El máximo de palabras se completa con el reparto de tokens. */
    private static final String INSTRUCCIONES_SECCION = """

            Eres un redactor académico experto. Resumí la sección que recibís en un solo párrafo, de MÁXIMO %d palabras.
            Usá EXCLUSIVAMENTE el texto que recibís. No uses conocimientos propios ni agregues datos, definiciones o ejemplos que no estén en él.
            Redactá en tercera persona, con tono formal y educativo, en TEXTO PLANO: sin Markdown, sin viñetas y sin títulos.
            Terminá siempre la última oración.""";

    /** Largo máximo del nombre de un pool, que es el de la columna. */
    private static final int LARGO_NOMBRE_POOL = 50;

    /** Largo máximo del guión adicional que escribe el docente. */
    private static final int LARGO_GUION = 300;

    /** Cantidad máxima de preguntas de un banco. */
    private static final int PREGUNTAS_MAXIMAS = 30;

    /** Las preguntas que admite un pool en Aurea: con su flujo, el banco no pasa de ahí. */
    private static final int PREGUNTAS_MAXIMAS_DE_AUREA = 8;

    /** Tipos de contenido que se pueden generar. */
    private enum Tipo {
        RESUMEN(CategoriaMaterialLectura.RESUMEN, TipoRecurso.RESUMEN, "Resumen IA de ", "el resumen", "un resumen",
                "Resumen generado con inteligencia artificial a partir de la bibliografía del tema. Revisalo antes de publicarlo."),
        PRESENTACION(CategoriaMaterialLectura.PRESENTACION, TipoRecurso.PRESENTACION, "Presentación IA de ", "la presentación", "una presentación",
                "Presentación generada con inteligencia artificial a partir de la bibliografía del tema. Revisala antes de publicarla.");

        private final String categoria;
        private final TipoRecurso tipoRecurso;
        private final String prefijoTitulo;
        private final String conArticulo;
        private final String conArticuloIndefinido;
        private final String descripcion;

        Tipo(String categoria, TipoRecurso tipoRecurso, String prefijoTitulo, String conArticulo, String conArticuloIndefinido,
                String descripcion) {
            this.categoria = categoria;
            this.tipoRecurso = tipoRecurso;
            this.prefijoTitulo = prefijoTitulo;
            this.conArticulo = conArticulo;
            this.conArticuloIndefinido = conArticuloIndefinido;
            this.descripcion = descripcion;
        }
    }

    private final TemaRepository temaRepository;
    private final TemaRecursoRepository temaRecursoRepository;
    private final RecursoRepository recursoRepository;
    private final MaterialLecturaRepository materialLecturaRepository;
    private final CategoriaMaterialLecturaRepository categoriaRepository;
    private final DocenteCursoRepository docenteCursoRepository;
    private final EstadoGeneracionIARepository estadoGeneracionRepository;
    private final EstadoOCRRepository estadoOcrRepository;
    private final PoolRepository poolRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionRespuestaRepository opcionRespuestaRepository;
    private final TerminoGlosarioRepository terminoGlosarioRepository;
    private final ParametroService parametroService;
    private final IndiceRecursoService indiceService;
    private final ModeloIAService modelo;
    private final TaskExecutor cola;
    private final TransactionTemplate transaccion;
    private final Path directorioCursos;

    @Value("${idoneos.ia.max-caracteres-material:8000}")
    private int maximoCaracteresMaterial;

    @Value("${idoneos.ia.max-segundos-banco:720}")
    private int maximoSegundosBanco;

    /** Crea el servicio con lo que necesita para registrar, leer y generar. */
    public GeneracionIAServiceImpl(TemaRepository temaRepository, TemaRecursoRepository temaRecursoRepository,
            RecursoRepository recursoRepository, MaterialLecturaRepository materialLecturaRepository,
            CategoriaMaterialLecturaRepository categoriaRepository, DocenteCursoRepository docenteCursoRepository,
            EstadoGeneracionIARepository estadoGeneracionRepository, EstadoOCRRepository estadoOcrRepository,
            PoolRepository poolRepository, PreguntaRepository preguntaRepository, OpcionRespuestaRepository opcionRespuestaRepository,
            TerminoGlosarioRepository terminoGlosarioRepository, ParametroService parametroService, IndiceRecursoService indiceService,
            ModeloIAService modelo, @Qualifier("colaIA") TaskExecutor cola,
            PlatformTransactionManager gestorTransacciones, @Qualifier("directorioCursos") Path directorioCursos) {
        this.temaRepository = temaRepository;
        this.temaRecursoRepository = temaRecursoRepository;
        this.recursoRepository = recursoRepository;
        this.materialLecturaRepository = materialLecturaRepository;
        this.categoriaRepository = categoriaRepository;
        this.docenteCursoRepository = docenteCursoRepository;
        this.estadoGeneracionRepository = estadoGeneracionRepository;
        this.estadoOcrRepository = estadoOcrRepository;
        this.poolRepository = poolRepository;
        this.preguntaRepository = preguntaRepository;
        this.opcionRespuestaRepository = opcionRespuestaRepository;
        this.terminoGlosarioRepository = terminoGlosarioRepository;
        this.parametroService = parametroService;
        this.indiceService = indiceService;
        this.modelo = modelo;
        this.cola = cola;
        this.transaccion = new TransactionTemplate(gestorTransacciones);
        this.directorioCursos = directorioCursos;
    }

    /** Documento de la bibliografía que se va a leer. */
    private record Documento(String titulo, String ruta, List<Marcador> indice) {
    }

    /** Lo que necesita la generación, leído de la base antes de empezar. */
    private record Pedido(String tituloTema, int idDocente, int idRecurso, String tituloRecurso, List<Documento> documentos) {
    }

    /** Parte del material que se trabaja por separado: una sección del índice, o el documento entero si no tiene índice. */
    private record Parte(String titulo, String texto) {
    }

    /** Texto leído de un documento de la bibliografía. */
    private record Leido(String titulo, TextoPdfUtil.Lectura lectura, List<TextoPdfUtil.Seccion> secciones) {
    }

    // ----------------------------------------------------------------------
    // Pedido del docente
    // ----------------------------------------------------------------------

    /** Pide el resumen de un tema. Registra el material oculto y en espera, y lo genera en segundo plano. */
    @Override
    public TemaRecurso solicitarResumen(Docente docente, Integer idTema) {
        return solicitar(docente, idTema, Tipo.RESUMEN);
    }

    /** Pide la presentación de un tema. Registra el material oculto y en espera, y lo genera en segundo plano. */
    @Override
    public TemaRecurso solicitarPresentacion(Docente docente, Integer idTema) {
        return solicitar(docente, idTema, Tipo.PRESENTACION);
    }

    private TemaRecurso solicitar(Docente docente, Integer idTema, Tipo tipo) {
        TemaRecurso material = transaccion.execute(estado -> registrar(docente, idTema, tipo));
        int idMaterial = material.getRecurso().getMaterialLectura().getIdMaterialLectura();
        try {
            cola.execute(() -> generar(idMaterial, tipo));
        } catch (TaskRejectedException e) {
            marcarError(idMaterial, "La cola de generación está llena.");
            throw new IllegalArgumentException("Error! Hay demasiadas generaciones en espera. Intentá de nuevo en unos minutos.");
        }
        return material;
    }

    /** Valida el pedido y registra el material, oculto y en espera, sin archivo. */
    private TemaRecurso registrar(Docente docente, Integer idTema, Tipo tipo) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activo."));
        DocenteCurso participacion = docenteCursoRepository.findByCursoAndBajaFalse(tema.getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));

        List<TemaRecurso> delTema = temaRecursoRepository.findByTemaAndBajaFalse(tema);
        boolean yaGenerando = delTema.stream().map(TemaRecurso::getRecurso)
                .anyMatch(r -> tipo.categoria.equals(r.getTipo()) && r.getEstadoGeneracion() != null
                        && EstadoGeneracionIA.PENDIENTE.equals(r.getEstadoGeneracion()));
        if (yaGenerando) {
            throw new IllegalArgumentException("Error! Ya hay " + tipo.conArticuloIndefinido + " de este tema en generación. Esperá a que termine.");
        }
        validarBibliografia(delTema, tipo);

        EstadoGeneracionIA pendiente = estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.PENDIENTE).orElseThrow();
        CategoriaMaterialLectura categoria = categoriaRepository.findByNombre(tipo.categoria)
                .orElseThrow(() -> new IllegalStateException("Falta la categoría de material de lectura '" + tipo.categoria + "'."));

        Recurso recurso = new Recurso(docente, abreviar(tipo.prefijoTitulo + tema.getTitulo(), 150));
        recurso.setDescripcion(tipo.descripcion);
        recurso = recursoRepository.saveAndFlush(recurso);
        MaterialLectura lectura = new MaterialLectura(recurso, categoria, null,
                abreviar(docente.getUsuario().getNombreCompleto(), 100));
        lectura.setEstadoGeneracionIa(pendiente);
        recurso.setMaterialLectura(materialLecturaRepository.save(lectura));

        int orden = delTema.stream().mapToInt(TemaRecurso::getNumeroOrden).max().orElse(0) + 1;
        TemaRecurso material = new TemaRecurso(tema, recurso, participacion, orden);
        material.setOculto(true);
        material = temaRecursoRepository.save(material);
        tema.getTemasRecurso().add(material);
        recurso.getTemasRecurso().add(material);
        return material;
    }

    /** Verifica que el tema tenga al menos una bibliografía con el texto ya reconocido. */
    private void validarBibliografia(List<TemaRecurso> delTema, Tipo tipo) {
        List<Bibliografia> bibliografias = delTema.stream().map(TemaRecurso::getRecurso).filter(r -> !r.esInactivo())
                .map(Recurso::getBibliografia).filter(b -> b != null && b.getRutaArchivo() != null).toList();
        if (bibliografias.isEmpty()) {
            throw new IllegalArgumentException("Error! El tema debe tener al menos una bibliografía cargada para generar " + tipo.conArticulo + ".");
        }
        if (bibliografias.stream().noneMatch(this::tieneTextoListo)) {
            throw new IllegalArgumentException("Error! La bibliografía del tema todavía se está procesando. Intentá de nuevo en unos minutos.");
        }
    }

    /** Indica si el texto del archivo ya se puede leer: no tiene OCR pendiente o ya terminó bien. */
    private boolean tieneTextoListo(Bibliografia bibliografia) {
        EstadoOCR estado = bibliografia.getEstadoOcr();
        return estado == null || EstadoOCR.LISTO.equals(estado.getNombre());
    }

    // ----------------------------------------------------------------------
    // Generación en segundo plano
    // ----------------------------------------------------------------------

    /** Se da de baja lo que se estaba generando: se deja de generar y no se guarda nada. */
    private static final class GeneracionCancelada extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private GeneracionCancelada() {
            super("Se dio de baja lo que se estaba generando.", null, false, false);
        }
    }

    /** Corta la generación si lo que se estaba generando se dio de baja. Se llama antes de cada llamada al modelo. */
    private static void verificarVigencia(java.util.function.BooleanSupplier cancelado) {
        if (cancelado.getAsBoolean()) {
            throw new GeneracionCancelada();
        }
    }

    /** Indica si el material (o su recurso) se dio de baja o ya no existe. */
    private boolean materialDadoDeBaja(int idMaterial) {
        return Boolean.TRUE.equals(transaccion.execute(estado -> materialLecturaRepository.findById(idMaterial)
                .map(m -> m.getRecurso().esInactivo()).orElse(true)));
    }

    /** Indica si el pool se dio de baja o ya no existe. */
    private boolean poolDadoDeBaja(int idPool) {
        return Boolean.TRUE.equals(transaccion.execute(estado -> poolRepository.findById(idPool).map(Pool::esInactivo).orElse(true)));
    }

    /** Genera el contenido de un material en espera. Corre en el hilo de la cola. */
    private void generar(int idMaterial, Tipo tipo) {
        Path archivo = null;
        java.util.function.BooleanSupplier cancelado = () -> materialDadoDeBaja(idMaterial);
        try {
            if (cancelado.getAsBoolean()) {
                REGISTRO.info("{} {} se dio de baja antes de empezar: no se genera.", tipo, idMaterial);
                return;
            }
            Pedido pedido = transaccion.execute(estado -> empezar(idMaterial));
            if (!modelo.estaDisponible()) {
                throw new ModeloIAException("El servidor del modelo de IA no está disponible.");
            }
            long inicio = System.currentTimeMillis();
            List<Leido> textos = leerDocumentos(pedido.documentos(), maximoCaracteresMaterial);
            if (textos.isEmpty()) {
                throw new ModeloIAException("No se pudo leer texto de la bibliografía del tema.");
            }
            String material = armarMaterial(textos, maximoCaracteresMaterial);
            String lectura = describirLectura(tipo, textos, material.length(), pedido.documentos().size());
            REGISTRO.info("{} {}: {}", tipo, idMaterial, lectura);

            Path carpeta = ManejadorArchivosUtil.resolverCarpetaDeBiblioteca(directorioCursos, pedido.idDocente());
            Files.createDirectories(carpeta);
            archivo = carpeta.resolve(tipo.tipoRecurso.getNombreDeArchivo() + pedido.idRecurso() + ".pdf");
            int paginas;
            List<String> titulosDelIndice;
            long desde = System.currentTimeMillis();
            List<Parte> partes = armarPartes(textos);
            REGISTRO.info("{} {}: {} parte(s) para trabajar por separado.", tipo, idMaterial, partes.size());
            if (tipo == Tipo.RESUMEN) {
                Resumen resumen;
                if (partes.size() < 2) {
                    verificarVigencia(cancelado);
                    String texto = modelo.generarTexto(INSTRUCCIONES_RESUMEN, material, TOKENS_TEXTO);
                    resumen = armarResumen(texto, material);
                } else {
                    resumen = resumirPorSecciones(partes, cancelado);
                }
                registrarTiempo(desde, material);
                paginas = ResumenPdfUtil.escribir(pedido.tituloRecurso(), resumen, lectura, archivo);
                titulosDelIndice = resumen.bloques().stream().map(Resumen.Bloque::subtitulo).filter(t -> t != null && !t.isBlank()).toList();
            } else {
                // La presentación sale del mismo resumen: cada bloque es una diapositiva y cada oración, un punto
                Resumen resumen;
                if (partes.size() < 2) {
                    verificarVigencia(cancelado);
                    resumen = armarResumen(modelo.generarTexto(INSTRUCCIONES_RESUMEN, material, TOKENS_TEXTO), material);
                } else {
                    resumen = resumirPorSecciones(partes, cancelado);
                }
                Presentacion presentacion = presentacionDesdeResumen(resumen);
                registrarTiempo(desde, material);
                paginas = PresentacionPdfUtil.escribir(pedido.tituloRecurso(), presentacion, lectura, archivo);
                // Las diapositivas que siguen a otra con el mismo título no son una entrada nueva del índice
                titulosDelIndice = presentacion.diapositivas().stream().map(Presentacion.Diapositiva::titulo)
                        .filter(t -> t != null && !t.isBlank() && !t.endsWith("(continuación)")).toList();
            }

            verificarVigencia(cancelado);
            // El índice del archivo generado queda en la base, como el de cualquier documento: cada título con su página
            List<Marcador> indice = armarIndice(archivo, titulosDelIndice);
            String ruta = ManejadorArchivosUtil.construirRutaRelativa(directorioCursos, archivo);
            long tamanio = Files.size(archivo);
            transaccion.executeWithoutResult(estado -> terminar(idMaterial, ruta, tamanio, paginas, indice));
            REGISTRO.info("{} {} generado en {} segundos.", tipo, idMaterial, (System.currentTimeMillis() - inicio) / 1000);
        } catch (GeneracionCancelada e) {
            REGISTRO.info("{} {} se dio de baja mientras se generaba: se descarta lo generado.", tipo, idMaterial);
            borrarSiExiste(archivo);
        } catch (Exception e) {
            REGISTRO.warn("No se pudo generar {} {}: {}", tipo.conArticulo, idMaterial, e.getMessage(), e);
            if (archivo != null) {
                try {
                    Files.deleteIfExists(archivo);
                } catch (IOException ignorada) {
                    // Queda un archivo suelto que no usa ningún registro
                }
            }
            marcarError(idMaterial, e instanceof ModeloIAException || e instanceof IllegalArgumentException
                    ? e.getMessage() : "No se pudo generar " + tipo.conArticulo + ".");
        }
    }

    private static void borrarSiExiste(Path archivo) {
        if (archivo != null) {
            try {
                Files.deleteIfExists(archivo);
            } catch (IOException ignorada) {
                // Queda un archivo suelto que no usa ningún registro
            }
        }
    }

    private void registrarTiempo(long desde, String material) {
        REGISTRO.info("El modelo respondió en {} segundos, con {} caracteres de material.", (System.currentTimeMillis() - desde) / 1000,
                material.length());
    }

    /** Pasa el material a generándose y lee de la base lo que hace falta. */
    private Pedido empezar(int idMaterial) {
        MaterialLectura material = materialLecturaRepository.findById(idMaterial).orElseThrow();
        material.setDetalleError(null);
        Recurso recurso = material.getRecurso();
        TemaRecurso delTema = recurso.getTemasRecurso().stream().filter(t -> !t.getBaja()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El material ya no pertenece a ningún tema."));
        List<Documento> documentos = new ArrayList<>();
        for (TemaRecurso candidato : temaRecursoRepository.findByTemaAndBajaFalse(delTema.getTema())) {
            Recurso otro = candidato.getRecurso();
            Bibliografia bibliografia = otro.getBibliografia();
            if (!otro.esInactivo() && bibliografia != null && bibliografia.getRutaArchivo() != null && tieneTextoListo(bibliografia)) {
                documentos.add(new Documento(otro.getTitulo(), bibliografia.getRutaArchivo(), indiceDe(otro)));
            }
        }
        return new Pedido(delTema.getTema().getTitulo(), recurso.getDocente().getIdDocente(), recurso.getIdRecurso(),
                recurso.getTitulo(), documentos);
    }

    /** Lee de la base el índice de un documento, vacío si no tiene. */
    private List<Marcador> indiceDe(Recurso recurso) {
        return indiceService.buscar(recurso).stream().map(e -> new Marcador(e.titulo(), e.pagina(), e.nivel())).toList();
    }

    /**
     * Lee el principio del texto de los documentos, en orden, hasta juntar el presupuesto de caracteres. Los documentos que siguen no se leen,
     * como en AUREA. Puede devolver una lista vacía.
     *
     * @param presupuesto Los caracteres a leer: los del material para el modelo, o los del texto de validación del banco.
     */
    private List<Leido> leerDocumentos(List<Documento> documentos, int presupuesto) throws IOException {
        List<Leido> textos = new ArrayList<>();
        int acumulado = 0;
        for (Documento documento : documentos) {
            if (acumulado >= presupuesto) {
                break;
            }
            Path pdf = ManejadorArchivosUtil.resolver(directorioCursos, documento.ruta());
            if (pdf == null || !Files.isRegularFile(pdf)) {
                continue;
            }
            // Con índice, el material se reparte entre sus secciones. Sin índice, se leen las primeras páginas, como AUREA.
            if (!documento.indice().isEmpty()) {
                TextoPdfUtil.LecturaPorIndice porIndice = TextoPdfUtil.leerPorIndice(pdf, documento.indice(),
                        presupuesto - acumulado - 1);
                if (porIndice != null) {
                    String junto = porIndice.secciones().stream().map(TextoPdfUtil.Seccion::texto)
                            .collect(java.util.stream.Collectors.joining(" "));
                    TextoPdfUtil.Lectura lectura = new TextoPdfUtil.Lectura(junto, porIndice.paginasTotales(), porIndice.paginasTotales(),
                            porIndice.recortado());
                    textos.add(new Leido(documento.titulo(), lectura, porIndice.secciones()));
                    acumulado += junto.length() + 1;
                    continue;
                }
            }
            TextoPdfUtil.Lectura lectura = TextoPdfUtil.leerInicio(pdf, presupuesto - acumulado);
            if (lectura.texto().length() >= 200) {
                textos.add(new Leido(documento.titulo(), lectura, List.of()));
                acumulado += lectura.texto().length() + 1;
            }
        }
        return textos;
    }

    /** Arma el texto de los documentos juntos, cortado a los primeros caracteres indicados. */
    private String armarMaterial(List<Leido> textos, int maximo) {
        String junto = textos.stream().map(t -> t.lectura().texto()).collect(java.util.stream.Collectors.joining(" "));
        return TextoPdfUtil.cortar(junto, maximo);
    }

    /**
     * Divide el material en las partes que se trabajan por separado: cada sección del índice, o el documento entero si no tiene índice.
     * Si son más de {@link #MAX_PARTES}, se juntan las vecinas.
     */
    private List<Parte> armarPartes(List<Leido> textos) {
        List<Parte> partes = new ArrayList<>();
        int usado = 0;
        for (Leido leido : textos) {
            if (leido.secciones().isEmpty()) {
                String texto = TextoPdfUtil.cortar(leido.lectura().texto(), Math.max(0, maximoCaracteresMaterial - usado));
                if (texto.length() >= 2 * LARGO_MINIMO_PARTE) {
                    // Sin índice, el texto se divide en partes para que el desarrollo tenga secciones; cada una se titula después
                    partes.addAll(dividirEnPartes(texto));
                    usado += texto.length() + 1;
                } else if (texto.length() >= 200) {
                    partes.add(new Parte(leido.titulo(), texto));
                    usado += texto.length() + 1;
                }
            } else {
                for (TextoPdfUtil.Seccion seccion : leido.secciones()) {
                    partes.add(new Parte(seccion.titulo(), seccion.texto()));
                    usado += seccion.texto().length() + 1;
                }
            }
        }
        if (partes.size() <= MAX_PARTES) {
            return partes;
        }
        int tamanio = (int) Math.ceil(partes.size() / (double) MAX_PARTES);
        List<Parte> grupos = new ArrayList<>();
        for (int i = 0; i < partes.size(); i += tamanio) {
            List<Parte> grupo = partes.subList(i, Math.min(partes.size(), i + tamanio));
            grupos.add(new Parte(abreviar(String.join(" / ", grupo.stream().map(Parte::titulo).toList()), 90),
                    String.join(" ", grupo.stream().map(Parte::texto).toList())));
        }
        return grupos;
    }

    /** Largo mínimo, en caracteres, de cada parte en que se divide un texto sin índice. */
    private static final int LARGO_MINIMO_PARTE = 1500;

    /** Cantidad máxima de partes en que se divide un texto sin índice. */
    private static final int MAXIMO_PARTES_SIN_INDICE = 4;

    /** Divide un texto sin índice en partes de largo parecido, cortando al final de una oración. Las partes quedan sin título. */
    private static List<Parte> dividirEnPartes(String texto) {
        int cantidad = Math.max(2, Math.min(MAXIMO_PARTES_SIN_INDICE, texto.length() / LARGO_MINIMO_PARTE));
        int objetivo = texto.length() / cantidad;
        List<Parte> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String oracion : texto.split("(?<=[.!?])\\s+")) {
            actual.append(oracion).append(' ');
            if (actual.length() >= objetivo && partes.size() < cantidad - 1) {
                partes.add(new Parte("", actual.toString().strip()));
                actual = new StringBuilder();
            }
        }
        if (actual.toString().strip().length() > 0) {
            partes.add(new Parte("", actual.toString().strip()));
        }
        return partes;
    }

    /** Parecido a partir del cual una oración de introducción o conclusión se considera repetida: es alto porque retoma lo que dicen las secciones. */
    private static final double SIMILITUD_MAXIMA_SINTESIS = 0.8;

    /** Le pide al modelo un título corto para una parte sin título. Si no sirve lo que escribe, usa «Aspectos principales» con su número. */
    private String tituloDeLaParte(String texto, int numero, List<String> yaUsados) {
        String titulo = "";
        try {
            // El pedido va entero en el mensaje, sin instrucciones de sistema: con ellas, el modelo chico a veces las repetía como si fueran el título
            String respuesta = modelo.generarTexto("Texto:" + System.lineSeparator() + TextoPdfUtil.cortar(texto, 1500)
                    + System.lineSeparator() + System.lineSeparator() + "Título corto del texto anterior (2 a 5 palabras):", 24);
            String primera = quitarCorchetes(respuesta.lines().filter(l -> !l.isBlank()).findFirst().orElse("")).replace("**", "")
                    .replaceAll("^[#\\s\"'«-]+", "");
            // Si el modelo escribió una frase en lugar de un título, se corta donde empieza lo que sigue: dos puntos, punto, paréntesis o guion
            primera = primera.split("[:.(\\[]| - | – ")[0];
            titulo = quitarNumeracion(primera.replaceAll("[\\s\"'».:;,]+$", "")).strip();
            String[] palabras = titulo.isEmpty() ? new String[0] : titulo.split("\\s+");
            if (palabras.length > 6) {
                titulo = String.join(" ", java.util.Arrays.copyOf(palabras, 5)).replaceAll("(?iu)(\\s+(y|o|e|de|del|la|el|los|las|con|en|a|que|para|por))+$", "");
            }
        } catch (ModeloIAException e) {
            titulo = "";
        }
        String elegido = titulo;
        if (elegido.length() < 3 || yaUsados.stream().anyMatch(t -> t.equalsIgnoreCase(elegido))) {
            // Sin un título que sirva o repetido: se arma con las palabras más repetidas de la parte, que siempre dicen de qué trata
            List<String> claves = PalabrasClaveUtil.extraer(texto, 2);
            String deClaves = claves.isEmpty() ? "Aspectos principales" : capitalizar(String.join(" y ", claves));
            titulo = yaUsados.stream().anyMatch(t -> t.equalsIgnoreCase(deClaves)) ? deClaves + " " + numero : deClaves;
        }
        return capitalizar(titulo);
    }

    /** Se queda con las primeras oraciones de un párrafo. */
    private static String primerasOraciones(String parrafo, int cantidad) {
        String[] oraciones = parrafo.strip().split("(?<=[.!?])\\s+");
        return String.join(" ", java.util.Arrays.copyOf(oraciones, Math.min(cantidad, oraciones.length)));
    }

    /**
     * Arma el resumen con una llamada al modelo por sección, más una para la introducción y otra para las conclusiones.
     * Los tokens de AUREA se reparten entre las llamadas, y el máximo de palabras se le pide en el prompt.
     */
    private Resumen resumirPorSecciones(List<Parte> partes, java.util.function.BooleanSupplier cancelado) {
        int tokens = Math.max(TOKENS_MINIMOS_PARTE, TOKENS_TEXTO / (partes.size() + 2));
        int palabras = (int) Math.round(tokens * PALABRAS_POR_TOKEN);
        List<String> usadas = new ArrayList<>();
        List<String> titulosUsados = new ArrayList<>();
        List<String[]> desarrollo = new ArrayList<>();
        for (int i = 0; i < partes.size(); i++) {
            Parte parte = partes.get(i);
            verificarVigencia(cancelado);
            String titulo = parte.titulo().isBlank() ? tituloDeLaParte(parte.texto(), i + 1, titulosUsados) : quitarNumeracion(parte.titulo());
            titulosUsados.add(titulo);
            String contenido = "Sección: " + titulo + "\n" + parte.texto();
            verificarVigencia(cancelado);
            try {
                String parrafo = depurarParrafo(modelo.generarTexto(INSTRUCCIONES_SECCION.formatted(palabras), contenido, tokens), parte.texto(), usadas,
                        COBERTURA_MINIMA_RESUMEN, SIMILITUD_MAXIMA);
                if (parrafo.isEmpty()) {
                    REGISTRO.info("Sin párrafo utilizable para la sección: {}", titulo);
                } else {
                    desarrollo.add(new String[] { titulo, parrafo });
                }
            } catch (ModeloIAException e) {
                REGISTRO.warn("El modelo no resumió la sección {}: {}", titulo, e.getMessage());
            }
        }
        if (desarrollo.isEmpty()) {
            throw new ModeloIAException("El modelo no pudo armar " + Tipo.RESUMEN.conArticuloIndefinido + " que se base en la bibliografía del tema.");
        }
        String sintesis = String.join("\n", desarrollo.stream().map(d -> d[0] + ". " + d[1]).toList());
        // La introducción y las conclusiones se comparan solo entre sí: es natural que retomen lo que dicen las secciones.
        // Se le da a cada una un material distinto: a la introducción, la primera idea de cada sección, y a la conclusión, la última, de atrás hacia
        // adelante. Con el mismo material, el modelo chico repite lo mismo en las dos y la conclusión se descartaba por repetida.
        List<String> usadasSintesis = new ArrayList<>();
        StringBuilder primerasIdeas = new StringBuilder();
        StringBuilder ultimasIdeas = new StringBuilder();
        for (int i = 0; i < desarrollo.size(); i++) {
            primerasIdeas.append("- ").append(desarrollo.get(i)[0]).append(": ").append(primerasOraciones(desarrollo.get(i)[1], 1)).append('\n');
            String[] oraciones = desarrollo.get(desarrollo.size() - 1 - i)[1].split("(?<=[.!?])\\s+");
            ultimasIdeas.append("- ").append(desarrollo.get(desarrollo.size() - 1 - i)[0]).append(": ").append(oraciones[oraciones.length - 1]).append('\n');
        }
        verificarVigencia(cancelado);
        String introduccion = redactarSintesis("Secciones de un documento y su idea principal:\n" + primerasIdeas
                + "\nEscribí la introducción del resumen del documento en 2 oraciones, de MÁXIMO " + palabras
                + " palabras en total: de qué trata y qué temas recorre. Texto plano, sin títulos ni viñetas.", sintesis, tokens, usadasSintesis);
        verificarVigencia(cancelado);
        String conclusiones = redactarSintesis("Ideas finales de cada sección del documento, de la última a la primera:\n" + ultimasIdeas
                + "\nEscribí las conclusiones del resumen del documento en 2 oraciones, de MÁXIMO " + palabras
                + " palabras en total, que sinteticen lo central. No repitas la introducción. Texto plano, sin títulos ni viñetas.", sintesis, tokens,
                usadasSintesis);
        if (conclusiones.isEmpty() && !desarrollo.isEmpty()) {
            // Si el modelo no dio nada que sirva, cierra con la última idea del desarrollo: un resumen sin conclusión queda incompleto
            String[] oraciones = desarrollo.get(desarrollo.size() - 1)[1].split("(?<=[.!?])\\s+");
            conclusiones = oraciones[oraciones.length - 1];
        }
        // La introducción y la conclusión son breves: a lo sumo dos oraciones, para que el desarrollo sea lo más extenso
        introduccion = primerasOraciones(introduccion, 2);
        conclusiones = primerasOraciones(conclusiones, 2);
        // Índice propio del resumen: 1. Introducción, 2. Desarrollo (2.1, 2.2...) y 3. Conclusión
        List<Resumen.Bloque> bloques = new ArrayList<>();
        int numero = 1;
        if (!introduccion.isEmpty()) {
            bloques.add(new Resumen.Bloque(numero++ + ". Introducción", List.of(introduccion)));
        }
        int numeroDesarrollo = numero++;
        bloques.add(new Resumen.Bloque(numeroDesarrollo + ". Desarrollo", List.of()));
        for (int i = 0; i < desarrollo.size(); i++) {
            bloques.add(new Resumen.Bloque(numeroDesarrollo + "." + (i + 1) + ". " + desarrollo.get(i)[0], List.of(desarrollo.get(i)[1])));
        }
        if (!conclusiones.isEmpty()) {
            bloques.add(new Resumen.Bloque(numero + ". Conclusión", List.of(conclusiones)));
        }
        return new Resumen("", bloques);
    }

    /**
     * Arma la presentación a partir del resumen, que es lo que el modelo escribe mejor: cada bloque con texto es una diapositiva, y cada
     * oración, un punto. Si las oraciones no entran en una diapositiva, siguen en otra con el mismo título. La carátula y la diapositiva
     * final las dibuja el PDF.
     */
    private Presentacion presentacionDesdeResumen(Resumen resumen) {
        List<Presentacion.Diapositiva> diapositivas = new ArrayList<>();
        for (Resumen.Bloque bloque : resumen.bloques()) {
            List<String> puntos = new ArrayList<>();
            for (String parrafo : bloque.parrafos() == null ? List.<String>of() : bloque.parrafos()) {
                for (String oracion : quitarCorchetes(parrafo).split("(?<=[.!?])\\s+")) {
                    String punto = formatearPunto(oracion);
                    if (punto.length() >= 10) {
                        puntos.add(punto);
                    }
                }
            }
            String titulo = bloque.subtitulo() == null || bloque.subtitulo().isBlank() ? "Resumen" : bloque.subtitulo().strip();
            List<String> actuales = new ArrayList<>();
            int alto = 0;
            int parte = 1;
            for (String punto : puntos) {
                int altoPunto = (int) Math.ceil(punto.length() / (double) CARACTERES_POR_LINEA) * ALTO_LINEA + SEPARACION_PUNTOS;
                if (!actuales.isEmpty() && alto + altoPunto > ALTO_UTIL_DIAPOSITIVA) {
                    diapositivas.add(new Presentacion.Diapositiva(parte == 1 ? titulo : titulo + " (continuación)", actuales));
                    actuales = new ArrayList<>();
                    alto = 0;
                    parte++;
                }
                actuales.add(punto);
                alto += altoPunto;
            }
            if (!actuales.isEmpty()) {
                diapositivas.add(new Presentacion.Diapositiva(parte == 1 ? titulo : titulo + " (continuación)", actuales));
            }
        }
        if (diapositivas.isEmpty()) {
            throw new ModeloIAException("El modelo no pudo armar " + Tipo.PRESENTACION.conArticuloIndefinido + " que se base en la bibliografía del tema.");
        }
        return new Presentacion("", diapositivas);
    }

    /** Deja un punto con la primera letra en mayúscula y con punto final, y sin signos sueltos al principio. */
    private static String formatearPunto(String punto) {
        String limpio = punto.replaceFirst("^[\\s\\-–•.,;:)\\]]+", "").replaceFirst("[\\s.;:,]+$", "");
        return capitalizar(limpio) + (limpio.matches("(?s).*[?!)\"”]$") ? "" : ".");
    }

    /** Acepta una oración si se apoya en la referencia y no repite otra ya usada. Devuelve la oración con la primera letra en mayúscula, o {@code null}. */
    private String aceptarOracion(String oracion, String referencia, List<String> usadas, double coberturaMinima, double similitudMaxima) {
        if (oracion.length() < 15) {
            return null;
        }
        if (LogicaPreguntasAurea.tieneRestosDeTabla(oracion)) {
            REGISTRO.info("Oración descartada por arrastrar restos de una tabla: {}", oracion);
            return null;
        }
        if (FidelidadTextoUtil.cobertura(oracion, referencia) < coberturaMinima) {
            REGISTRO.info("Oración descartada por no apoyarse en el material: {}", oracion);
            return null;
        }
        if (usadas.stream().anyMatch(otra -> FidelidadTextoUtil.similitud(oracion, otra) >= similitudMaxima)) {
            REGISTRO.info("Oración descartada por repetir otra: {}", oracion);
            return null;
        }
        String aceptada = capitalizar(oracion);
        usadas.add(aceptada);
        return aceptada;
    }

    /** Pide un párrafo de síntesis (introducción o conclusiones) y lo depura. Reintenta una vez si queda vacío. Si el modelo falla, devuelve vacío. */
    private String redactarSintesis(String pedido, String sintesis, int tokens, List<String> usadas) {
        for (int intento = 1; intento <= 2; intento++) {
            try {
                // Todo en un mensaje, sin instrucciones de sistema, que es como mejor responde el modelo chico a un pedido corto
                String parrafo = depurarParrafo(modelo.generarTexto(pedido, tokens), sintesis, usadas, COBERTURA_MINIMA_SINTESIS,
                        SIMILITUD_MAXIMA_SINTESIS);
                if (!parrafo.isEmpty()) {
                    return parrafo;
                }
            } catch (ModeloIAException e) {
                REGISTRO.warn("El modelo no redactó la síntesis: {}", e.getMessage());
            }
        }
        return "";
    }

    /** Quita la numeración del principio de un título, como «3.» o «2.1.», porque el resumen y la presentación arman su propio índice. */
    private static String quitarNumeracion(String titulo) {
        return titulo == null ? "" : titulo.replaceFirst("^\\s*\\d+(?:\\.\\d+)*\\.?\\s+", "").strip();
    }

    /** Quita los corchetes que el modelo deja sueltos al escribir JSON y los espacios de más. */
    private static String quitarCorchetes(String texto) {
        return texto.replaceAll("\\[\\s*\\]", " ").replace("[", " ").replace("]", " ").replaceAll("\\s+", " ").strip();
    }

    /** Pone la primera letra en mayúscula. */
    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /**
     * Deja del texto del modelo solo las oraciones que se apoyan en la referencia y no repiten otra ya usada, y descarta la última
     * si quedó a medias. Quita el formato de Markdown y los encabezados que el modelo agrega aunque se le pida texto plano.
     */
    private String depurarParrafo(String texto, String referencia, List<String> usadas, double coberturaMinima, double similitudMaxima) {
        String limpio = quitarCorchetes(texto).replace("**", "").replace("__", "").replaceAll("(?m)^\\s*(#+\\s*|[-•*]\\s+|\\d+[.)]\\s+)", "")
                .replaceAll("\\s+", " ").strip()
                .replaceFirst("(?iu)^(introducci[oó]n|desarrollo|conclusi[oó]n(?:es)?|resumen)\\s*:\\s*", "");
        boolean cortado = !limpio.matches("(?s).*[.!?\"”)]$");
        String[] oraciones = limpio.split("(?<=[.!?])\\s+");
        List<String> aceptadas = new ArrayList<>();
        for (int i = 0; i < oraciones.length; i++) {
            String oracion = oraciones[i].strip();
            boolean aMedias = cortado && i == oraciones.length - 1;
            if (oracion.length() < 15 || aMedias) {
                continue;
            }
            String aceptada = aceptarOracion(oracion, referencia, usadas, coberturaMinima, similitudMaxima);
            if (aceptada != null) {
                aceptadas.add(aceptada);
            }
        }
        return String.join(" ", aceptadas);
    }

    /** Describe cuánta bibliografía se usó, para aclararlo en el archivo generado. */
    private String describirLectura(Tipo tipo, List<Leido> textos, int caracteresDelMaterial, int documentosEnElTema) {
        int totales = textos.stream().mapToInt(t -> t.lectura().paginasTotales()).sum();
        int caracteres = textos.stream().mapToInt(t -> t.lectura().texto().length()).sum();
        boolean recortado = caracteresDelMaterial < caracteres || textos.size() < documentosEnElTema || textos.stream().anyMatch(t -> t.lectura().recortado());
        String fuentes = " Fuentes: " + String.join(", ", textos.stream().map(Leido::titulo).toList()) + ".";
        if (!recortado) {
            return "Se usó la bibliografía completa (" + totales + " páginas)." + fuentes;
        }
        return "Puede haber temas sin cubrir." + fuentes;
    }

    // ----------------------------------------------------------------------
    // Banco de preguntas
    // ----------------------------------------------------------------------

    /** Lo que se va aceptando mientras se arma el banco: los enunciados usados y las preguntas listas. */
    private static final class AvanceBanco {
        private final Set<String> usados = new HashSet<>();
        private final List<LogicaPreguntasAurea.Pregunta> generadas = new ArrayList<>();
        /** Línea breve que dice de qué documento sale cada oración, para que el modelo sepa de qué tema trata («software», «finanzas»…). */
        private String contextoDelMaterial = "";
        /** Todas las oraciones del material que sirven para armar preguntas: de ahí salen las respuestas incorrectas. */
        private List<String> distractores = List.of();
        /** El peso (IDF) de cada lema en el documento, para medir la similitud entre oraciones y entre una pregunta y su oración. */
        private java.util.Map<String, Double> idf = java.util.Map.of();
        /** Los números y los nombres del documento, de donde salen los datos que se cambian en las respuestas incorrectas. */
        private EntidadesUtil.Indice entidades = new EntidadesUtil.Indice(java.util.Map.of(), java.util.Set.of(), java.util.Set.of());
        /** La sección del documento (el último título) en la que está cada oración. */
        private final java.util.Map<String, String> secciones = new java.util.HashMap<>();

        private int deSeleccion() {
            return (int) generadas.stream().filter(p -> p.tipo() != LogicaPreguntasAurea.Tipo.VERDADERO_FALSO).count();
        }

        private int deVerdaderoFalso() {
            return generadas.size() - deSeleccion();
        }
    }

    /** Lo que necesita la generación del banco, leído de la base antes de empezar. */
    private record PedidoBanco(String tituloTema, List<Documento> documentos, List<String> glosario, int preguntas, int porcentajeOpcionMultiple,
            int porcentajeModelo, int estrategia, List<String> existentes) {
    }

    /** Pide el banco de preguntas de un tema. Registra el pool en espera y lo genera en segundo plano. */
    @Override
    public Pool solicitarBanco(Docente docente, Integer idTema, String guion) {
        String guionLimpio = guion == null ? "" : guion.trim();
        if (guionLimpio.length() > LARGO_GUION) {
            throw new IllegalArgumentException("Error! El guión adicional no puede superar los " + LARGO_GUION + " caracteres.");
        }
        Pool pool = transaccion.execute(estado -> registrarBanco(docente, idTema));
        int idPool = pool.getIdPool();
        try {
            cola.execute(() -> generarBanco(idPool, guionLimpio));
        } catch (TaskRejectedException e) {
            marcarErrorBanco(idPool, "La cola de generación está llena.");
            throw new IllegalArgumentException("Error! Hay demasiadas generaciones en espera. Intentá de nuevo en unos minutos.");
        }
        return pool;
    }

    /** Valida el pedido y registra el pool, en espera y sin preguntas. */
    private Pool registrarBanco(Docente docente, Integer idTema) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        Tema tema = temaRepository.findById(idTema == null ? -1 : idTema).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activo."));
        docenteCursoRepository.findByCursoAndBajaFalse(tema.getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));

        boolean yaGenerando = poolRepository.findByTema(tema).stream().anyMatch(p -> !p.esInactivo() && p.getEstadoGeneracionIa() != null
                && EstadoGeneracionIA.PENDIENTE.equals(p.getEstadoGeneracionIa().getNombre()));
        if (yaGenerando) {
            throw new IllegalArgumentException("Error! Ya hay un banco de preguntas de este tema en generación. Esperá a que termine.");
        }
        List<Bibliografia> bibliografias = temaRecursoRepository.findByTemaAndBajaFalse(tema).stream().map(TemaRecurso::getRecurso)
                .filter(r -> !r.esInactivo()).map(Recurso::getBibliografia).filter(b -> b != null && b.getRutaArchivo() != null).toList();
        boolean hayGlosario = !terminoGlosarioRepository.findByTemaAndBajaFalse(tema).isEmpty();
        if (bibliografias.isEmpty() && !hayGlosario) {
            throw new IllegalArgumentException("Error! El tema debe tener al menos una bibliografía o un término de glosario cargado para generar el banco de preguntas.");
        }
        if (!bibliografias.isEmpty() && bibliografias.stream().noneMatch(this::tieneTextoListo) && !hayGlosario) {
            throw new IllegalArgumentException("Error! La bibliografía del tema todavía se está procesando. Intentá de nuevo en unos minutos.");
        }

        // El nombre incluye la hora, hasta el segundo, porque no se repite el nombre de un pool dentro de un tema
        String nombre = abreviar("IA " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss")) + " " + tema.getTitulo(),
                LARGO_NOMBRE_POOL);
        Pool pool = new Pool(tema, nombre);
        pool.setRevisado(false);
        pool.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.PENDIENTE).orElseThrow());
        return poolRepository.save(pool);
    }

    /** Hasta cuántos caracteres de cada documento se leen para buscar lo que pide el guion. */
    private static final int LECTURA_COMPLETA = 400_000;

    /** Oración del documento que menciona lo que pide el guion, con la cantidad de palabras del guion que menciona y su lugar en el documento. */
    private record Hallada(String oracion, int puntaje, int orden) {
    }

    /** Cuántos intentos, con oraciones distintas, tiene el modelo para armar cada pregunta. */
    private static final int INTENTOS_POR_PREGUNTA = 5;

    /** Largo máximo, en tokens, de lo que escribe el modelo en cada llamada: una pregunta, una variante o una paráfrasis. Escribir es lo más lento. */
    private static final int TOKENS_POR_LLAMADA = 130;

    /** Oraciones más largas que esto no se le dan al modelo: en las largas reescribe de más y las variantes no sirven. */
    private static final int LARGO_MAXIMO_PARA_EL_MODELO = 170;

    /** Lo que se le pide al modelo para escribir la pregunta de una oración. Es un pedido chico, con un ejemplo resuelto: un modelo de 1B falla si se le pide mucho junto. */
    private static final String INSTRUCCIONES_PREGUNTA = """
            Recibís una oración de un texto de estudio. Escribí una pregunta de examen, con signos ¿?, cuya respuesta sea exactamente lo que dice esa oración.
            La pregunta no repite la oración ni dice la respuesta, y no agrega datos ni siglas que no estén en la oración.
            Es una sola pregunta, no dos.
            Puede venir una línea «Documento: …» que dice de qué trata el material y una línea «Sección: …» que dice a qué parte pertenece la oración.
            Son solo contexto y no son parte de la oración: sirven para saber de qué habla cuando dice «su», «él» o «esto».

            Ejemplo.
            Oración: "Las acciones representan una parte del capital de una empresa y dan derecho a dividendos."
            Salida: {"pregunta": "¿Qué representan las acciones y qué derecho dan?"}""";

    /** Lo que se le pide al modelo para decir lo mismo que una oración con otras palabras. */
    private static final String INSTRUCCIONES_PREGUNTA_DEL_DATO = """
            Recibís una oración de un texto de estudio y un dato de esa oración. Escribí una pregunta de examen, con signos ¿?, cuya respuesta sea exactamente
            ese dato. La pregunta no dice el dato ni lo repite, y no agrega datos que no estén en la oración. Es una sola pregunta, no dos.
            Puede venir una línea «Documento: …» que dice de qué trata el material y una línea «Sección: …» que dice a qué parte pertenece la oración.
            Son solo contexto y no son parte de la oración.

            Ejemplo.
            Oración: "El Banco Central fue creado en 1935 para regular la moneda."
            Dato: "1935"
            Salida: {"pregunta": "¿En qué año fue creado el Banco Central?"}""";

    /** Lo que se le pide al modelo para comprobar una pregunta: que la responda con la parte de la oración que la contesta. */
    private static final String INSTRUCCIONES_RESPUESTA = """
            Recibís una oración de un texto de estudio y una pregunta sobre ella. Respondé la pregunta con la parte de la oración que la contesta, copiada tal
            cual y en pocas palabras. No expliques nada más.

            Ejemplo.
            Oración: "Los bonos son emitidos por los estados y las obligaciones negociables, por las empresas."
            Pregunta: "¿Quién emite los bonos?"
            Salida: {"respuesta": "los estados"}""";

    private static final String INSTRUCCIONES_PARAFRASIS = """
            Recibís una oración de un texto de estudio. Escribila de nuevo con otras palabras, en una sola oración, sin agregar ni quitar ninguna idea.

            Ejemplo.
            Oración: "Las acciones dan derecho a dividendos."
            Salida: {"afirmacion": "Quien tiene acciones recibe dividendos."}""";

    /** Reparte entre los tipos que arma el modelo las preguntas que le tocan: las múltiples según el porcentaje, y el resto, entre única y verdadero o falso. */
    private static List<LogicaPreguntasAurea.Tipo> planDelModelo(int cantidad, int porcentajeMultiple) {
        int multiples = Math.min(cantidad, Math.round(cantidad * porcentajeMultiple / 100f));
        int resto = cantidad - multiples;
        int unicas = (resto + 1) / 2;
        int verdaderoFalso = resto - unicas;
        List<LogicaPreguntasAurea.Tipo> plan = new ArrayList<>();
        // Se intercalan, para que los tipos no queden todos juntos
        while (unicas + multiples + verdaderoFalso > 0) {
            if (unicas-- > 0) {
                plan.add(LogicaPreguntasAurea.Tipo.UNICA_RESPUESTA);
            }
            if (multiples-- > 0) {
                plan.add(LogicaPreguntasAurea.Tipo.MULTIPLE_CHOICE);
            }
            if (verdaderoFalso-- > 0) {
                plan.add(LogicaPreguntasAurea.Tipo.VERDADERO_FALSO);
            }
        }
        return plan;
    }

    /** Lee el texto completo de los documentos, no solo el principio: el modelo ya no lee el material, solo recibe oraciones sueltas. */
    private String leerTextoCompleto(List<Documento> documentos) throws IOException {
        StringBuilder texto = new StringBuilder();
        for (Documento documento : documentos) {
            Path pdf = ManejadorArchivosUtil.resolver(directorioCursos, documento.ruta());
            if (pdf != null && Files.isRegularFile(pdf)) {
                texto.append(TextoPdfUtil.leerInicio(pdf, LECTURA_COMPLETA).texto()).append(' ');
            }
        }
        return texto.toString().strip();
    }

    /**
     * Saca las oraciones de los documentos respetando su estructura: los títulos, las celdas de las tablas y los ítems de las listas quedan separados, y
     * cada oración conserva la sección en la que está. Si un PDF no se puede leer así, se usa el texto plano de ese documento, sin sección.
     */
    private List<OracionesUtil.OracionDeSeccion> leerOraciones(List<Documento> documentos, String textoPlano) {
        List<OracionesUtil.OracionDeSeccion> oraciones = new ArrayList<>();
        for (Documento documento : documentos) {
            Path pdf = ManejadorArchivosUtil.resolver(directorioCursos, documento.ruta());
            if (pdf == null || !Files.isRegularFile(pdf)) {
                continue;
            }
            try {
                List<EstructuraPdfUtil.Bloque> bloques = EstructuraPdfUtil.leer(pdf, LECTURA_COMPLETA);
                // Se le agrega el título del documento como sección de lo que viene antes del primer título
                List<EstructuraPdfUtil.Bloque> conSeccion = bloques.stream().map(b -> b.seccion().isEmpty()
                        ? new EstructuraPdfUtil.Bloque(b.tipo(), b.texto(), documento.titulo(), b.campo()) : b).toList();
                oraciones.addAll(OracionesUtil.candidatasPorBloques(conSeccion, Integer.MAX_VALUE));
            } catch (IOException | RuntimeException e) {
                REGISTRO.warn("No se pudo leer la estructura de «{}»: se usa el texto plano. {}", documento.titulo(), e.getMessage());
            }
        }
        if (oraciones.isEmpty()) {
            LogicaPreguntasAurea.candidatas(textoPlano, Integer.MAX_VALUE).forEach(o -> oraciones.add(new OracionesUtil.OracionDeSeccion(o, "", "")));
        }
        return oraciones;
    }

    /**
     * Busca, en el texto, las oraciones que mencionan lo que pide el guion del docente y que sirven para armar preguntas, en el orden en que aparecen
     * y hasta el largo del material. Una palabra del guion coincide con las que empiezan igual, para que «funcionales» encuentre también «funcional».
     *
     * @return Las oraciones, o una lista vacía si el guion no tiene palabras útiles o no encontró nada: ahí se sigue con el proceso normal.
     */
    private List<String> buscarOracionesDelGuion(List<String> delTexto, String guion) {
        // Las palabras del guion que están en muchas oraciones del documento (artículos, conectores, palabras genéricas) no dicen qué pide el docente
        Set<String> frecuentes = PalabrasClaveUtil.frecuentes(delTexto);
        List<String> raices = new ArrayList<>();
        for (String palabra : LogicaPreguntasAurea.normalizar(guion).split("\\s+")) {
            if (palabra.length() >= 4 && !frecuentes.contains(palabra)) {
                raices.add(palabra.length() > 6 ? palabra.substring(0, 6) : palabra);
            }
        }
        if (raices.isEmpty()) {
            return List.of();
        }
        List<Hallada> halladas = new ArrayList<>();
        int orden = 0;
        for (String oracion : delTexto) {
            orden++;
            List<String> palabras = java.util.Arrays.asList(LogicaPreguntasAurea.normalizar(oracion).split("\\s+"));
            int puntaje = (int) raices.stream().distinct().filter(raiz -> palabras.stream().anyMatch(p -> p.startsWith(raiz))).count();
            if (puntaje > 0) {
                halladas.add(new Hallada(oracion, puntaje, orden));
            }
        }
        if (halladas.isEmpty()) {
            return List.of();
        }
        // Con pocas palabras se exigen todas las que más aparecen juntas; con más, alcanza con la mayoría
        int mejor = halladas.stream().mapToInt(Hallada::puntaje).max().orElse(1);
        int minimo = raices.size() <= 2 ? mejor : (int) Math.ceil(mejor * 0.6);
        List<Hallada> candidatas = new ArrayList<>(halladas.stream().filter(h -> h.puntaje() >= minimo).toList());
        candidatas.sort(java.util.Comparator.comparingInt(Hallada::puntaje).reversed().thenComparingInt(Hallada::orden));
        return candidatas.stream().limit(40).map(Hallada::oracion).toList();
    }

    /** Una llamada al modelo con un pedido chico. Devuelve nulo si el modelo falla. */
    private <T> T llamarAlModelo(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema) {
        return llamarAlModelo(instrucciones, contenido, tipo, esquema, TOKENS_POR_LLAMADA);
    }

    private <T> T llamarAlModelo(String instrucciones, String contenido, Class<T> tipo, java.util.Map<String, Object> esquema, int tokens) {
        try {
            return modelo.generarObjeto(instrucciones, contenido, tipo, esquema, tokens);
        } catch (ModeloIAException e) {
            REGISTRO.warn("El modelo no respondió: {}", e.getMessage());
            return null;
        }
    }

    private String pedirPregunta(String instrucciones, String contenido) {
        PreguntaIA respuesta = llamarAlModelo(instrucciones, contenido, PreguntaIA.class, EsquemasIA.pregunta());
        return respuesta == null ? null : respuesta.pregunta();
    }

    /**
     * El pedido de una oración, con la línea de contexto del material delante y la sección del documento en la que está. La sección es el último título
     * que apareció antes de la oración: le dice al modelo de qué habla cuando la oración dice «su», «él» o «esto».
     */
    private static String conContexto(String contexto, String oracion, String seccion) {
        return (contexto.isEmpty() ? "" : contexto + "\n") + (seccion == null || seccion.isBlank() ? "" : LogicaPreguntasAurea.ETIQUETA_DE_SECCION + ": " + seccion + "\n")
                + LogicaPreguntasAurea.ETIQUETA_DE_ORACION + ": \"" + oracion + "\"";
    }

    private String pedirAfirmacion(String oracion, String contexto, String seccion) {
        AfirmacionIA respuesta = llamarAlModelo(INSTRUCCIONES_PARAFRASIS, conContexto(contexto, oracion, seccion), AfirmacionIA.class, EsquemasIA.afirmacion());
        return respuesta == null ? null : respuesta.afirmacion();
    }

    // ----------------------------------------------------------------------
    // Flujo de un pool, paso por paso
    //
    //   1. Leer el texto completo de los documentos del tema                      (código)
    //   2. Elegir y ordenar las oraciones que sirven para preguntar               (código)
    //   3. Por cada pregunta del plan, hasta INTENTOS_POR_PREGUNTA con otra oración:
    //        3.1 Tomar la siguiente oración, tal cual está en el texto            (código)
    //        3.2 Redactar la pregunta de esa oración: única tarea del modelo      (modelo)
    //        3.3 Comprobar que la pregunta sirve                                  (código)
    //        3.4 Buscar tres oraciones distractoras del mismo texto               (código)
    //   4. Completar con las preguntas de reglas (glosario y completar)           (código)
    //   5. Guardar el pool                                                        (código)
    // ----------------------------------------------------------------------

    /**
     * Genera las preguntas de un pool en espera. Corre en el hilo de la cola. Al modelo le toca el porcentaje configurado de las preguntas, y solo para
     * redactarlas; a las reglas, el resto. Si el modelo no logra armar alguna de las suyas, también la arman las reglas.
     */
    private void generarBanco(int idPool, String guion) {
        try {
            if (poolDadoDeBaja(idPool)) {
                REGISTRO.info("El banco {} se dio de baja antes de empezar: no se genera.", idPool);
                return;
            }
            PedidoBanco pedido = transaccion.execute(estado -> empezarBanco(idPool));
            if (!modelo.estaDisponible()) {
                throw new ModeloIAException("El servidor del modelo de IA no está disponible.");
            }
            long inicio = System.currentTimeMillis();
            // Con el flujo de Aurea, un pool tiene como mucho las preguntas que admite Aurea, y todas se le piden al modelo
            boolean flujoDeAurea = pedido.estrategia() == Parametro.IA_ESTRATEGIA_BANCO_EN_UNA_LLAMADA;
            boolean porRondas = pedido.estrategia() == Parametro.IA_ESTRATEGIA_BANCO_POR_RONDAS;
            int total = flujoDeAurea ? Math.min(pedido.preguntas(), PREGUNTAS_MAXIMAS_DE_AUREA) : pedido.preguntas();

            // 1. LEER EL TEXTO COMPLETO. El modelo recibe una oración por vez, así que no hay un límite de lectura que cuidar
            String texto = leerTextoCompleto(pedido.documentos());
            if (texto.length() < 200) {
                throw new ModeloIAException("No hay material suficiente en el tema para armar preguntas.");
            }
            LogicaPreguntasAurea.Contexto contexto = LogicaPreguntasAurea.Contexto.desde(texto, pedido.glosario());
            AvanceBanco avance = prepararAvance(pedido, texto);

            // 2. ELEGIR LAS ORACIONES: primero las que pidió el docente y después las más importantes de todo el texto. Salen de la estructura del PDF
            // (títulos, párrafos, celdas, ítems), y cada una conserva la sección donde está
            List<OracionesUtil.OracionDeSeccion> delDocumento = leerOraciones(pedido.documentos(), texto);
            List<String> oraciones = elegirOraciones(idPool, delDocumento, guion, total, contexto, avance);

            // 3. ARMAR LAS PREGUNTAS DEL MODELO
            int deModelo = flujoDeAurea || porRondas ? total : Math.min(total, Math.round(total * pedido.porcentajeModelo() / 100f));
            int delModelo;
            if (porRondas) {
                // Camino por rondas: el modelo solo escribe las preguntas, de a pocas oraciones objetivo; el código arma las opciones
                avance.generadas.addAll(new BancoPorRondasIA(modelo).armar(deModelo, oraciones, avance.distractores, avance.usados, avance.entidades,
                        () -> hayTiempo(inicio)));
                delModelo = avance.generadas.size();
            } else if (flujoDeAurea) {
                // Camino alternativo (flujo de Aurea): el modelo escribe preguntas y opciones en una sola llamada por lote, de a pocas oraciones objetivo
                avance.generadas.addAll(new BancoEnUnaLlamadaIA(modelo).armar(deModelo, oraciones, avance.distractores, avance.usados, pedido.existentes(),
                        oraciones.subList(0, Math.min(60, oraciones.size()))));
                delModelo = avance.generadas.size();
            } else {
                // Camino principal: oración -> el modelo redacta la pregunta -> el código arma las opciones
                delModelo = armarPreguntasConModelo(idPool, planDelModelo(deModelo, pedido.porcentajeOpcionMultiple()), oraciones, contexto, avance, inicio);
                if (delModelo < 0) {
                    return;
                }
            }
            REGISTRO.info("Banco {}: el modelo armó {} de {} preguntas.", idPool, delModelo, deModelo);

            // 4. COMPLETAR CON REGLAS: arman las que les tocan y, si el modelo no logró alguna de las suyas, también esa
            avance.generadas.addAll(LogicaPreguntasAurea.armarFormatosDeReglas(contexto, total - avance.generadas.size(), avance.usados));
            int conReglas = avance.generadas.size() - delModelo;
            // Si no alcanzaron las preguntas pero hay al menos la mitad, se guardan las que hay: es mejor que ninguna y el docente puede completarlas
            if (avance.generadas.size() < Math.max(1, (total + 1) / 2)) {
                throw new ModeloIAException("Se armaron solo " + avance.generadas.size() + " de " + total
                        + " preguntas válidas: el material del tema no alcanza.");
            }
            if (avance.generadas.size() < total) {
                REGISTRO.warn("Banco {}: se guardan {} de {} preguntas, el material no alcanzó para más.", idPool, avance.generadas.size(), total);
            }

            // 5. GUARDAR
            if (poolDadoDeBaja(idPool)) {
                REGISTRO.info("El banco {} se dio de baja mientras se generaba: se descarta.", idPool);
                return;
            }
            List<LogicaPreguntasAurea.Pregunta> aGuardar = new ArrayList<>(avance.generadas.subList(0, Math.min(total, avance.generadas.size())));
            // Se mezclan las del modelo con las de las reglas
            java.util.Collections.shuffle(aGuardar);
            transaccion.executeWithoutResult(estado -> terminarBanco(idPool, aGuardar));
            REGISTRO.info("Banco {} generado con {} preguntas ({} del modelo y {} con reglas) en {} segundos.", idPool, aGuardar.size(), delModelo,
                    conReglas, (System.currentTimeMillis() - inicio) / 1000);
        } catch (Exception e) {
            REGISTRO.warn("No se pudo generar el banco {}: {}", idPool, e.getMessage(), e);
            marcarErrorBanco(idPool, e instanceof ModeloIAException || e instanceof IllegalArgumentException
                    ? e.getMessage() : "No se pudo generar el banco de preguntas.");
        }
    }

    /** Lo que se va acumulando mientras se arma el banco: las preguntas ya usadas en el tema y la línea breve que dice de qué trata el material. */
    private AvanceBanco prepararAvance(PedidoBanco pedido, String texto) {
        AvanceBanco avance = new AvanceBanco();
        // Las preguntas ya existentes en el tema cuentan como usadas: el pool nuevo no las repite
        pedido.existentes().forEach(enunciado -> avance.usados.add(LogicaPreguntasAurea.normalizar(enunciado)));
        // El contexto breve del material: el título del documento y sus palabras clave. Se usa el título del documento y no el del tema, porque el
        // documento dice de qué trata de verdad, y el tema puede ser otro
        avance.contextoDelMaterial = "Documento: " + String.join("; ", pedido.documentos().stream().map(Documento::titulo).limit(2).toList())
                + ". Palabras clave: " + String.join(", ", PalabrasClaveUtil.extraer(texto, 6)) + ".";
        return avance;
    }

    /**
     * PASO 2. Elige las oraciones del texto con las que se arman las preguntas, en el orden en que se van a probar: primero las que mencionan lo que
     * pidió el docente y después las de las que más se habla en todo el texto, repartidas entre sus capítulos. Todas son oraciones literales y ya
     * filtradas (con verbo, sin referencias a otra oración, sin restos de tabla). También deja en el avance todas las oraciones válidas del texto, de
     * donde salen los distractores.
     */
    private List<String> elegirOraciones(int idPool, List<OracionesUtil.OracionDeSeccion> delDocumento, String guion, int total,
            LogicaPreguntasAurea.Contexto contexto, AvanceBanco avance) {
        List<String> candidatas = delDocumento.stream().map(OracionesUtil.OracionDeSeccion::texto).toList();
        delDocumento.forEach(o -> avance.secciones.putIfAbsent(o.texto(), o.seccion()));
        List<String> oraciones = new ArrayList<>(guion.isEmpty() ? List.<String>of() : buscarOracionesDelGuion(candidatas, guion));
        if (!guion.isEmpty()) {
            REGISTRO.info("Banco {}: {} oraciones del documento mencionan lo que pide el guion.", idPool, oraciones.size());
        }
        for (String oracion : LogicaPreguntasAurea.masImportantes(candidatas, Math.max(30, total * 6))) {
            if (!oraciones.contains(oracion)) {
                oraciones.add(oracion);
            }
        }
        // Los ítems en infinitivo («Gestionar la inscripción de los alumnos…») son objetivos, no afirmaciones: el modelo redacta peor su pregunta y no
        // sirven para verdadero o falso. Se dejan para el final; el orden dentro de cada grupo sigue siendo el de importancia (la ordenación es estable)
        oraciones.sort(java.util.Comparator.comparing(LogicaPreguntasAurea::esItemDeLista));
        contexto.preferir(oraciones);
        avance.distractores = candidatas;
        avance.idf = GramaticaUtil.idfDeLemas(candidatas);
        avance.entidades = EntidadesUtil.indexar(candidatas);
        REGISTRO.info("Banco {}: el código priorizó {} oraciones de las {} que tiene el documento.", idPool, oraciones.size(), candidatas.size());
        return oraciones;
    }

    /**
     * PASO 3. Arma las preguntas que le tocan al modelo. Cada una tiene hasta INTENTOS_POR_PREGUNTA intentos, y cada intento usa una oración distinta:
     * si el modelo no redacta una pregunta que sirva, o no hay tres distractores buenos, se pasa a la oración siguiente.
     *
     * @return Cuántas preguntas se armaron, o -1 si el pool se dio de baja mientras se generaba.
     */
    private int armarPreguntasConModelo(int idPool, List<LogicaPreguntasAurea.Tipo> plan, List<String> oraciones, LogicaPreguntasAurea.Contexto contexto,
            AvanceBanco avance, long inicio) {
        int siguiente = 0;
        int armadas = 0;
        for (LogicaPreguntasAurea.Tipo tipo : plan) {
            boolean armada = false;
            for (int intento = 1; intento <= INTENTOS_POR_PREGUNTA && !armada && siguiente < oraciones.size() && hayTiempo(inicio); intento++) {
                if (poolDadoDeBaja(idPool)) {
                    REGISTRO.info("El banco {} se dio de baja mientras se generaba: se descarta.", idPool);
                    return -1;
                }
                // 3.1 Tomar la siguiente oración
                String oracion = oraciones.get(siguiente++);
                if (oracion.length() > LARGO_MAXIMO_PARA_EL_MODELO) {
                    // No consume el intento: simplemente no sirve para el modelo
                    intento--;
                    continue;
                }
                LogicaPreguntasAurea.Pregunta pregunta = armarPregunta(tipo, oracion, oraciones, contexto, avance);
                if (pregunta != null) {
                    avance.generadas.add(pregunta);
                    armadas++;
                    armada = true;
                }
            }
        }
        return armadas;
    }

    /** Arma una pregunta del tipo indicado con una oración, o devuelve nulo si esa oración no sirve para ese tipo. */
    private LogicaPreguntasAurea.Pregunta armarPregunta(LogicaPreguntasAurea.Tipo tipo, String oracion, List<String> oraciones,
            LogicaPreguntasAurea.Contexto contexto, AvanceBanco avance) {
        if (tipo == LogicaPreguntasAurea.Tipo.MULTIPLE_CHOICE) {
            // Opción múltiple: dos oraciones con al menos dos palabras clave en común
            String pareja = LogicaPreguntasAurea.parejaDe(oracion, oraciones, contexto);
            String consigna = pareja == null ? null : LogicaPreguntasAurea.consignaMultiple(oracion, pareja, contexto);
            if (consigna != null) {
                return armarOpcionMultiple(oracion, pareja, consigna, contexto, avance);
            }
            // Sin otra oración que trate de lo mismo, no hay opción múltiple posible: se arma una de opción única
            tipo = LogicaPreguntasAurea.Tipo.UNICA_RESPUESTA;
        }
        return tipo == LogicaPreguntasAurea.Tipo.VERDADERO_FALSO ? armarVerdaderoFalso(oracion, avance) : armarOpcionUnica(oracion, avance);
    }

    /**
     * Opción única. 3.2 El modelo redacta la pregunta de la oración (su única tarea). 3.3 El código comprueba la pregunta. 3.4 El código elige tres
     * oraciones distractoras del mismo texto. La respuesta correcta es la oración tal cual.
     */
    private LogicaPreguntasAurea.Pregunta armarOpcionUnica(String oracion, AvanceBanco avance) {
        // Primero con un dato de la oración (un número o un nombre): la pregunta pide ese dato y las incorrectas son la misma oración con el dato cambiado
        EntidadesUtil.Entidad dato = EntidadesUtil.elegir(oracion, avance.entidades, avance.idf);
        if (dato != null) {
            String pregunta = redactarPreguntaDelDato(oracion, dato, avance);
            if (pregunta != null) {
                List<String> cambiadas = EntidadesUtil.incorrectas(oracion, dato, avance.entidades, avance.distractores, 3);
                LogicaPreguntasAurea.Pregunta armada = LogicaPreguntasAurea.unicaConCambios(oracion, pregunta, cambiadas, avance.distractores, avance.usados);
                if (armada != null) {
                    return armada;
                }
            }
        }
        // Sin un dato que sirva: el modelo redacta la pregunta de la oración y las incorrectas son las oraciones más parecidas del documento
        String enunciado = redactarPregunta(oracion, avance);
        if (enunciado == null) {
            REGISTRO.info("Opción única descartada: la pregunta del modelo no sirve para «{}»", oracion);
            return null;
        }
        LogicaPreguntasAurea.Pregunta armada = LogicaPreguntasAurea.unica(oracion, enunciado, avance.distractores, avance.usados);
        if (armada == null) {
            REGISTRO.info("Opción única descartada: no hay tres oraciones distractoras buenas para «{}»", enunciado);
        }
        return armada;
    }

    /**
     * Le pide al modelo una pregunta cuya respuesta sea el dato de la oración y la comprueba con la vuelta: le da la pregunta y la oración y le pide que
     * la responda; si no responde con el dato, la pregunta no apunta a él y se descarta. También se descarta si nombra el dato (lo regalaría) o si tiene
     * poca similitud con la oración. Prueba hasta tres versiones: la primera, la más precisa, y dos con más variedad.
     */
    private String redactarPreguntaDelDato(String oracion, EntidadesUtil.Entidad dato, AvanceBanco avance) {
        String seccion = avance.secciones.getOrDefault(oracion, "");
        String contextoValido = avance.contextoDelMaterial + " " + LogicaPreguntasAurea.ETIQUETA_DE_SECCION + ": " + seccion;
        String contenido = conContexto(avance.contextoDelMaterial, oracion, seccion) + "\nDato: \"" + dato.texto() + "\"";
        for (int version = 0; version < VERSIONES_DE_LA_PREGUNTA; version++) {
            String texto = version == 0 ? pedirPregunta(INSTRUCCIONES_PREGUNTA_DEL_DATO, contenido) : pedirPreguntaVariada(INSTRUCCIONES_PREGUNTA_DEL_DATO, contenido);
            String pregunta = LogicaPreguntasAurea.mejorEnunciado(oracion, contextoValido, List.of(texto == null ? "" : texto), avance.idf);
            if (pregunta == null || (" " + LogicaPreguntasAurea.normalizar(pregunta) + " ").contains(" " + LogicaPreguntasAurea.normalizar(dato.texto()) + " ")) {
                continue;
            }
            String respuesta = pedirRespuesta(oracion, pregunta);
            String normalRespuesta = LogicaPreguntasAurea.normalizar(respuesta == null ? "" : respuesta);
            String normalDato = LogicaPreguntasAurea.normalizar(dato.texto());
            if (!normalRespuesta.isEmpty() && (normalRespuesta.contains(normalDato) || normalDato.contains(normalRespuesta))) {
                return pregunta;
            }
        }
        return null;
    }

    /** La vuelta: el modelo responde la pregunta usando solo la oración. Devuelve nulo si no responde. */
    private String pedirRespuesta(String oracion, String pregunta) {
        RespuestaIA respuesta = llamarAlModelo(INSTRUCCIONES_RESPUESTA, LogicaPreguntasAurea.ETIQUETA_DE_ORACION + ": \"" + oracion + "\"\nPregunta: \"" + pregunta
                + "\"", RespuestaIA.class, EsquemasIA.respuesta());
        return respuesta == null ? null : respuesta.respuesta();
    }

    /** 3.2 y 3.3. Le pide al modelo la pregunta de una oración y la comprueba. Devuelve nulo si el modelo falla o la pregunta no sirve. */
    private String redactarPregunta(String oracion, AvanceBanco avance) {
        String seccion = avance.secciones.getOrDefault(oracion, "");
        // Las palabras de la sección también valen en la pregunta: el modelo las usa para nombrar de qué habla una oración que dice «su» o «esto»
        String contextoValido = avance.contextoDelMaterial + " " + LogicaPreguntasAurea.ETIQUETA_DE_SECCION + ": " + seccion;
        String contenido = conContexto(avance.contextoDelMaterial, oracion, seccion);
        // La primera versión es la más precisa. Si ya sirve y no agrega palabras de su cuenta, no hace falta pedir más
        List<String> versiones = new ArrayList<>();
        versiones.add(pedirPregunta(INSTRUCCIONES_PREGUNTA, contenido));
        String mejor = LogicaPreguntasAurea.mejorEnunciado(oracion, contextoValido, versiones, avance.idf);
        if (mejor != null && LogicaPreguntasAurea.palabrasAjenas(oracion, contextoValido, mejor) == 0) {
            return mejor;
        }
        // Las otras versiones se piden con más variedad, para que no sean la misma pregunta. Se elige la que se apoya más en la oración
        for (int version = 1; version < VERSIONES_DE_LA_PREGUNTA; version++) {
            versiones.add(pedirPreguntaVariada(INSTRUCCIONES_PREGUNTA, contenido));
        }
        return LogicaPreguntasAurea.mejorEnunciado(oracion, contextoValido, versiones, avance.idf);
    }

    /** Cuántas versiones de la pregunta se piden como máximo para cada oración: el código elige la que mejor se apoya en ella. */
    private static final int VERSIONES_DE_LA_PREGUNTA = 3;

    /** Pide una pregunta con más variedad (más temperatura y más palabras candidatas), para obtener versiones distintas de la misma oración. */
    private String pedirPreguntaVariada(String instrucciones, String contenido) {
        try {
            PreguntaIA respuesta = modelo.generarObjeto(instrucciones, contenido, PreguntaIA.class, EsquemasIA.pregunta(), TOKENS_POR_LLAMADA, 0.7, 0.95, 40, 1.15);
            return respuesta == null ? null : respuesta.pregunta();
        } catch (ModeloIAException e) {
            REGISTRO.warn("El modelo no respondió: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Opción múltiple: las dos correctas son las dos oraciones tal cual, la consigna la arma el código y las dos incorrectas son otras oraciones del
     * texto que no mencionan la palabra clave de la consigna. El modelo no interviene.
     */
    private LogicaPreguntasAurea.Pregunta armarOpcionMultiple(String oracion, String pareja, String consigna, LogicaPreguntasAurea.Contexto contexto,
            AvanceBanco avance) {
        LogicaPreguntasAurea.Pregunta armada = LogicaPreguntasAurea.multiple(oracion, pareja, consigna,
                LogicaPreguntasAurea.claveDeLaConsigna(oracion, pareja, contexto), avance.distractores, avance.usados);
        if (armada == null) {
            REGISTRO.info("Pregunta múltiple descartada: {} (no hay dos oraciones incorrectas buenas)", consigna);
        }
        return armada;
    }

    /**
     * Verdadero o falso. Se alternan verdaderas y falsas. La verdadera es la oración (o su paráfrasis, que escribe el modelo); la falsa es el término de una
     * definición con la definición de otro término, armada por el código. Si no se puede armar una falsa, se arma una verdadera.
     */
    private LogicaPreguntasAurea.Pregunta armarVerdaderoFalso(String oracion, AvanceBanco avance) {
        // Un objetivo en infinitivo («Gestionar pagos…») no es una afirmación: no se puede decir si es verdadero o falso
        if (LogicaPreguntasAurea.esItemDeLista(oracion)) {
            return null;
        }
        String falsa = avance.deVerdaderoFalso() % 2 == 1 ? LogicaPreguntasAurea.falsaDeDefinicion(oracion, avance.distractores) : null;
        LogicaPreguntasAurea.Pregunta armada = falsa != null ? LogicaPreguntasAurea.verdaderoFalsoFalso(falsa, avance.usados)
                : LogicaPreguntasAurea.verdaderoFalso(oracion, pedirAfirmacion(oracion, avance.contextoDelMaterial, avance.secciones.getOrDefault(oracion, "")), avance.usados);
        if (armada == null) {
            REGISTRO.info("Verdadero o falso descartado: {}", oracion);
        }
        return armada;
    }

    private boolean hayTiempo(long inicio) {
        return (System.currentTimeMillis() - inicio) / 1000 < maximoSegundosBanco;
    }

    /** Pasa el pool a generándose y lee de la base lo que hace falta. */
    private PedidoBanco empezarBanco(int idPool) {
        Pool pool = poolRepository.findById(idPool).orElseThrow();
        pool.setDetalleError(null);
        Tema tema = pool.getTema();
        List<Documento> documentos = new ArrayList<>();
        for (TemaRecurso candidato : temaRecursoRepository.findByTemaAndBajaFalse(tema)) {
            Recurso otro = candidato.getRecurso();
            Bibliografia bibliografia = otro.getBibliografia();
            if (!otro.esInactivo() && bibliografia != null && bibliografia.getRutaArchivo() != null && tieneTextoListo(bibliografia)) {
                documentos.add(new Documento(otro.getTitulo(), bibliografia.getRutaArchivo(), indiceDe(otro)));
            }
        }
        List<String> glosario = terminoGlosarioRepository.findByTemaAndBajaFalse(tema).stream()
                .map((TerminoGlosario t) -> t.getTermino() + ": " + t.getDefinicion()).toList();
        int preguntas = Math.max(1, Math.min(PREGUNTAS_MAXIMAS, parametroService.obtenerEntero(Parametro.IA_CANTIDAD_PREGUNTAS_BANCO,
                Parametro.IA_CANTIDAD_PREGUNTAS_BANCO_POR_DEFECTO)));
        int porcentaje = Math.max(0, Math.min(100, parametroService.obtenerEntero(Parametro.IA_PROPORCION_OPCION_MULTIPLE,
                Parametro.IA_PROPORCION_OPCION_MULTIPLE_POR_DEFECTO)));
        int porcentajeModelo = Math.max(0, Math.min(100, parametroService.obtenerEntero(Parametro.IA_PROPORCION_MODELO,
                Parametro.IA_PROPORCION_MODELO_POR_DEFECTO)));
        int estrategia = parametroService.obtenerEntero(Parametro.IA_ESTRATEGIA_BANCO, Parametro.IA_ESTRATEGIA_BANCO_POR_DEFECTO);
        return new PedidoBanco(tema.getTitulo(), documentos, glosario, preguntas, porcentaje, porcentajeModelo, estrategia,
                preguntaRepository.findTextosDeOtrosPoolsDelTema(tema, idPool));
    }

    /** Guarda las preguntas y sus opciones en el pool y lo da por generado. */
    private void terminarBanco(int idPool, List<LogicaPreguntasAurea.Pregunta> preguntas) {
        Pool pool = poolRepository.findById(idPool).orElseThrow();
        for (LogicaPreguntasAurea.Pregunta valida : preguntas) {
            Pregunta pregunta = preguntaRepository.save(new Pregunta(pool, valida.enunciado(),
                    valida.tipo() == LogicaPreguntasAurea.Tipo.MULTIPLE_CHOICE));
            for (LogicaPreguntasAurea.Opcion opcion : valida.opciones()) {
                pregunta.getOpcionesRespuesta().add(opcionRespuestaRepository
                        .save(new OpcionRespuesta(pregunta, opcion.texto(), opcion.correcta())));
            }
            pool.getPreguntas().add(pregunta);
        }
        pool.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.LISTO).orElseThrow());
        pool.setDetalleError(null);
        pool.setUltimaModificacion(LocalDateTime.now());
    }

    /** Pasa el pool a error con el motivo, sin dejar nunca que esto mismo falle hacia afuera. */
    private void marcarErrorBanco(int idPool, String motivo) {
        try {
            transaccion.executeWithoutResult(estado -> {
                Pool pool = poolRepository.findById(idPool).orElseThrow();
                pool.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.FALLIDO).orElseThrow());
                pool.setDetalleError(abreviar(motivo, LARGO_DETALLE));
            });
        } catch (RuntimeException e) {
            REGISTRO.error("No se pudo registrar el error del pool {}: {}", idPool, e.getMessage());
        }
    }

    // ----------------------------------------------------------------------
    // Fidelidad al material
    // ----------------------------------------------------------------------

    /**
     * Arma el resumen a partir del texto que redactó el modelo: lo divide en introducción, desarrollo y conclusiones, y descarta las oraciones
     * que no se apoyan en el material o repiten otra, porque un modelo chico agrega lo que sabe aunque se le pida que no.
     * Es una comprobación de palabras: no entiende el significado, pero atrapa lo que habla de otra cosa.
     */
    private Resumen armarResumen(String texto, String material) {
        List<Resumen.Bloque> bloques = new ArrayList<>();
        List<String> usadas = new ArrayList<>();
        // Si el modelo se quedó sin espacio, la última oración quedó a medias y no se usa
        boolean cortado = !texto.stripTrailing().matches("(?s).*[.!?\"”)]$");
        Dividido dividido = dividirEnBloques(texto);
        for (var bloque : dividido.bloques()) {
            List<String> oraciones = new ArrayList<>();
            for (String parrafo : bloque.parrafos()) {
                oraciones.addAll(java.util.Arrays.asList(parrafo.split("(?<=[.!?])\\s+")));
            }
            List<String> aceptadas = new ArrayList<>();
            for (int i = 0; i < oraciones.size(); i++) {
                String oracion = oraciones.get(i).strip();
                boolean aMedias = cortado && i == oraciones.size() - 1 && !oracion.matches("(?s).*[.!?]$");
                if (oracion.length() < 15 || aMedias) {
                    continue;
                }
                if (LogicaPreguntasAurea.tieneRestosDeTabla(oracion)) {
                    REGISTRO.info("Oración descartada por arrastrar restos de una tabla: {}", oracion);
                } else if (FidelidadTextoUtil.cobertura(oracion, material) < COBERTURA_MINIMA_RESUMEN) {
                    REGISTRO.info("Oración descartada por no apoyarse en el material: {}", oracion);
                } else if (usadas.stream().anyMatch(otra -> FidelidadTextoUtil.similitud(oracion, otra) >= SIMILITUD_MAXIMA)) {
                    REGISTRO.info("Oración descartada por repetir otra: {}", oracion);
                } else {
                    aceptadas.add(oracion);
                    usadas.add(oracion);
                }
            }
            if (!aceptadas.isEmpty()) {
                bloques.add(new Resumen.Bloque(bloque.subtitulo(), List.of(String.join(" ", aceptadas))));
            }
        }
        if (bloques.isEmpty()) {
            throw new ModeloIAException("El modelo no pudo armar " + Tipo.RESUMEN.conArticuloIndefinido + " que se base en la bibliografía del tema.");
        }
        return new Resumen(dividido.titulo(), aEsqueleto(bloques));
    }

    /**
     * Lleva los bloques del texto del modelo al esqueleto estándar: 1. Introducción, 2. Desarrollo (con 2.1, 2.2… para los títulos propios que el
     * modelo haya puesto) y 3. Conclusión. Los renglones sin título se suman a lo que se venía escribiendo, y se quita la numeración que el modelo
     * haya puesto en sus títulos.
     */
    private static List<Resumen.Bloque> aEsqueleto(List<Resumen.Bloque> bloques) {
        List<String> introduccion = new ArrayList<>();
        List<String> desarrollo = new ArrayList<>();
        List<String> conclusion = new ArrayList<>();
        List<Resumen.Bloque> secciones = new ArrayList<>();
        String actual = "introduccion";
        for (Resumen.Bloque bloque : bloques) {
            String titulo = quitarNumeracion(bloque.subtitulo()).strip();
            String nombre = titulo.toLowerCase(java.util.Locale.ROOT);
            List<String> parrafos = bloque.parrafos() == null ? List.of() : bloque.parrafos();
            if (nombre.startsWith("introducci")) {
                actual = "introduccion";
                introduccion.addAll(parrafos);
            } else if (nombre.startsWith("conclusi")) {
                actual = "conclusion";
                conclusion.addAll(parrafos);
            } else if (nombre.startsWith("desarroll")) {
                actual = "desarrollo";
                desarrollo.addAll(parrafos);
            } else if (nombre.isEmpty()) {
                if (actual.equals("introduccion")) {
                    introduccion.addAll(parrafos);
                } else if (actual.equals("conclusion")) {
                    conclusion.addAll(parrafos);
                } else if (actual.equals("seccion") && !secciones.isEmpty()) {
                    secciones.get(secciones.size() - 1).parrafos().addAll(parrafos);
                } else {
                    desarrollo.addAll(parrafos);
                }
            } else {
                actual = "seccion";
                secciones.add(new Resumen.Bloque(titulo, new ArrayList<>(parrafos)));
            }
        }
        List<Resumen.Bloque> esqueleto = new ArrayList<>();
        int numero = 1;
        // La introducción y la conclusión son breves: a lo sumo dos oraciones
        if (!introduccion.isEmpty()) {
            introduccion = List.of(primerasOraciones(String.join(" ", introduccion), 2));
            esqueleto.add(new Resumen.Bloque(numero++ + ". Introducción", introduccion));
        }
        if (!desarrollo.isEmpty() || !secciones.isEmpty()) {
            int numeroDesarrollo = numero++;
            esqueleto.add(new Resumen.Bloque(numeroDesarrollo + ". Desarrollo", desarrollo));
            for (int i = 0; i < secciones.size(); i++) {
                esqueleto.add(new Resumen.Bloque(numeroDesarrollo + "." + (i + 1) + ". " + secciones.get(i).subtitulo(), secciones.get(i).parrafos()));
            }
        }
        if (!conclusion.isEmpty()) {
            esqueleto.add(new Resumen.Bloque(numero + ". Conclusión", List.of(primerasOraciones(String.join(" ", conclusion), 2))));
        }
        return esqueleto;
    }

    /** Resultado de leer el texto del modelo: un título opcional y los bloques. */
    private record Dividido(String titulo, List<Resumen.Bloque> bloques) {
    }

    /**
     * Divide el texto del modelo en bloques. Un modelo chico no respeta el texto plano que se le pide: mezcla negritas, viñetas y listas numeradas.
     * Se tratan como encabezado los renglones cortos que terminan en dos puntos o están enteros en negrita, y como título el primero si va antes
     * de cualquier texto. Introducción, desarrollo y conclusiones se reconocen aunque lleven el texto en el mismo renglón.
     */
    private static Dividido dividirEnBloques(String texto) {
        java.util.regex.Pattern conocido = java.util.regex.Pattern.compile("(?iu)^(introducci[oó]n|desarrollo|conclusi[oó]n(?:es)?)\\s*[:.]?\\s*(.*)$");
        List<Resumen.Bloque> bloques = new ArrayList<>();
        String titulo = "";
        String subtitulo = "";
        List<String> parrafos = new ArrayList<>();
        for (String renglon : texto.replace("\r", "").split("\n")) {
            boolean enNegrita = renglon.strip().matches("^\\*\\*[^*]+\\*\\*[:.]?$");
            String limpio = renglon.replaceFirst("^\\s*(?:#+\\s*|[-•*]\\s+|\\d+[.)]\\s+)", "").replace("**", "").replace("__", "").strip();
            if (limpio.isEmpty()) {
                continue;
            }
            java.util.regex.Matcher coincidencia = conocido.matcher(limpio);
            boolean esEncabezado = limpio.length() <= 60 && (enNegrita || limpio.endsWith(":")) && !limpio.matches(".*[a-zñ]\\.$");
            if (coincidencia.matches() || esEncabezado) {
                if (!parrafos.isEmpty()) {
                    bloques.add(new Resumen.Bloque(subtitulo, parrafos));
                    parrafos = new ArrayList<>();
                }
                String restante = "";
                if (coincidencia.matches()) {
                    String nombre = coincidencia.group(1).toLowerCase();
                    subtitulo = nombre.startsWith("introd") ? "Introducción" : nombre.startsWith("desarr") ? "Desarrollo" : "Conclusiones";
                    restante = coincidencia.group(2).strip();
                } else if (bloques.isEmpty() && parrafos.isEmpty() && titulo.isEmpty() && subtitulo.isEmpty()) {
                    titulo = limpio.replaceAll("[:.]$", "");
                    continue;
                } else {
                    subtitulo = limpio.replaceAll("[:.]$", "");
                }
                if (!restante.isBlank()) {
                    parrafos.add(restante);
                }
            } else {
                // Un renglón sin punto final, como una viñeta, se cierra con punto para que se lea como una oración
                parrafos.add(limpio.matches(".*[.!?:]$") ? limpio : limpio + ".");
            }
        }
        if (!parrafos.isEmpty()) {
            bloques.add(new Resumen.Bloque(subtitulo, parrafos));
        }
        return new Dividido(titulo, bloques);
    }

    // ----------------------------------------------------------------------
    // Cierre
    // ----------------------------------------------------------------------

    /**
     * Arma el índice de un archivo generado: la página de cada título y su nivel. Los títulos numerados como «2.1.» son de segundo nivel, y si no
     * hay una entrada «2.» que los agrupe (la presentación no tiene diapositiva de desarrollo), se agrega «2. Desarrollo» en la página del primero.
     */
    private List<Marcador> armarIndice(Path pdf, List<String> titulos) throws IOException {
        if (titulos.isEmpty()) {
            return List.of();
        }
        int[] paginas = TextoPdfUtil.ubicarTitulos(pdf, titulos);
        java.util.regex.Pattern hijo = java.util.regex.Pattern.compile("^(\\d+)\\.\\d+\\.\\s");
        java.util.regex.Pattern padre = java.util.regex.Pattern.compile("^(\\d+)\\.\\s");
        Set<String> conPadre = new HashSet<>();
        List<Marcador> indice = new ArrayList<>();
        for (int i = 0; i < titulos.size(); i++) {
            String titulo = titulos.get(i).strip();
            java.util.regex.Matcher esHijo = hijo.matcher(titulo);
            java.util.regex.Matcher esPadre = padre.matcher(titulo);
            if (esHijo.find()) {
                if (conPadre.add(esHijo.group(1))) {
                    indice.add(new Marcador(esHijo.group(1) + ". Desarrollo", paginas[i], 1));
                }
                indice.add(new Marcador(titulo, paginas[i], 2));
            } else {
                if (esPadre.find()) {
                    conPadre.add(esPadre.group(1));
                }
                indice.add(new Marcador(titulo, paginas[i], 1));
            }
        }
        return indice;
    }

    /** Guarda el archivo y su índice en el material y lo da por generado. */
    private void terminar(int idMaterial, String ruta, long tamanio, int paginas, List<Marcador> indice) {
        MaterialLectura material = materialLecturaRepository.findById(idMaterial).orElseThrow();
        material.setRutaArchivo(ruta);
        material.setTamanioBytes(tamanio);
        material.setExtension("pdf");
        material.setPaginas(paginas);
        material.setEstadoOcr(estadoOcrRepository.findByNombre(EstadoOCR.LISTO).orElse(null));
        material.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.LISTO).orElseThrow());
        material.setDetalleError(null);
        material.getRecurso().setUltimaModificacion(LocalDateTime.now());
        try {
            List<java.util.Map<String, Object>> entradas = new ArrayList<>();
            for (Marcador marcador : indice) {
                entradas.add(java.util.Map.of("titulo", marcador.titulo(), "pagina", marcador.pagina(), "nivel", marcador.nivel()));
            }
            indiceService.reemplazar(material.getRecurso(), new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(entradas));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("No se pudo guardar el índice del archivo generado.", e);
        }
    }

    /** Pasa el material a error con el motivo, sin dejar nunca que esto mismo falle hacia afuera. */
    private void marcarError(int idMaterial, String motivo) {
        try {
            transaccion.executeWithoutResult(estado -> {
                MaterialLectura material = materialLecturaRepository.findById(idMaterial).orElseThrow();
                material.setEstadoGeneracionIa(estadoGeneracionRepository.findByNombre(EstadoGeneracionIA.FALLIDO).orElseThrow());
                material.setDetalleError(abreviar(motivo, LARGO_DETALLE));
            });
        } catch (RuntimeException e) {
            REGISTRO.error("No se pudo registrar el error del material {}: {}", idMaterial, e.getMessage());
        }
    }

    private static String abreviar(String texto, int maximo) {
        String limpio = texto == null ? "" : texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo - 1).trim() + "…";
    }
}
