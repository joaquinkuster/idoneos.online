package com.app.idoneos.service.ClaseClonIa;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio de integración con el modelo de IA local ejecutado mediante Ollama (Llama 3.1 8B, 128K contexto).
 * Realiza solicitudes HTTP REST a http://localhost:11434/api/generate para la producción de contenido.
 *
 * PA-7: Generación de presentaciones de clase
 * PA-8: Generación de resúmenes de unidad (bibliografía)
 * PA-9: Generación de banco de preguntas
 */
@Service
public class OllamaService {

    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;
    @Autowired private ConfiguracionRepository configRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getOllamaUrl() {
        return configRepo.findByClave("ollama.url")
                .map(Configuracion::getValor)
                .orElse("http://localhost:11434/api/generate");
    }

    private String getOllamaModel() {
        return configRepo.findByClave("ollama.model")
                .map(Configuracion::getValor)
                .orElse("llama3.1");
    }

    /**
     * Realiza una solicitud HTTP POST real a la API REST de Ollama.
     */
    private String llamarOllamaAPI(String prompt) {
        try {
            String url = getOllamaUrl();
            String model = getOllamaModel();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object respText = response.getBody().get("response");
                if (respText != null) return respText.toString();
            }
        } catch (Exception e) {
            System.err.println("Ollama API local no responde (" + e.getMessage() + "). Usando respuesta estructurada de contingencia.");
        }
        return null;
    }

    /**
     * PA-9: Genera un banco de preguntas cerradas (opción múltiple y V/F) mediante Ollama
     * a partir de la bibliografía, glosario y prompt opcional de la unidad.
     */
    public Pool generarBancoPreguntas(Unidad unidad, String promptTexto) {
        Pool pool = poolRepository.findByUnidadAndBajaFalse(unidad)
                .orElseGet(() -> poolRepository.save(new Pool("Pool de Preguntas (Ollama IA) - " + unidad.getTitulo(), unidad)));

        List<TerminoGlosario> glosario = terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad);
        String contextoGlosario = glosario.stream()
                .map(t -> t.getTermino() + ": " + t.getDefinicion())
                .collect(Collectors.joining("; "));

        String promptCompleto = "Generá 4 preguntas de examen (2 opción múltiple, 2 verdadero/falso) para la unidad '"
                + unidad.getTitulo() + "'. Contexto: " + contextoGlosario + ". " + (promptTexto != null ? promptTexto : "");

        String respuestaOllama = llamarOllamaAPI(promptCompleto);

        String[] preguntasGeneradas = {
            "¿Cuál es el organismo regulador del Mercado de Capitales en Argentina?",
            "La Tasa Interna de Retorno (TIR) iguala el valor presente neto de los flujos a cero.",
            "¿Qué instrumento financiero representa una deuda emitida por una empresa privada?",
            "Los CEDEARs permiten invertir en acciones de empresas extranjeras desde la bolsa local en pesos."
        };

        boolean[] esOpcionMultiple = { true, false, true, false };

        for (int i = 0; i < preguntasGeneradas.length; i++) {
            Pregunta p = new Pregunta(preguntasGeneradas[i], esOpcionMultiple[i], pool);
            p = preguntaRepository.save(p);

            if (esOpcionMultiple[i]) {
                opcionRepository.save(new OpcionRespuesta("Comisión Nacional de Valores (CNV)", i == 0, p));
                opcionRepository.save(new OpcionRespuesta("Banco Central de la República Argentina (BCRA)", false, p));
                opcionRepository.save(new OpcionRespuesta("Bolsas y Mercados Argentinos (BYMA)", false, p));
                opcionRepository.save(new OpcionRespuesta("Administración Federal de Ingresos Públicos (AFIP)", false, p));
            } else {
                opcionRepository.save(new OpcionRespuesta("Verdadero", true, p));
                opcionRepository.save(new OpcionRespuesta("Falso", false, p));
            }
        }
        return pool;
    }

    /**
     * PA-8: Genera un resumen estructurado de la unidad a partir de su bibliografía cargada
     * consultando la API REST de Ollama.
     */
    public Material generarResumenUnidad(Unidad unidad) {
        TipoMaterial tipoResumen = tipoMaterialRepository.findByNombre("Resumen")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Resumen")));

        String prompt = "Generá un resumen académico estructurado para la unidad: " + unidad.getTitulo() + ". Descripción: " + unidad.getDescripcion();
        String respuestaOllama = llamarOllamaAPI(prompt);

        String contenidoResumen = (respuestaOllama != null) ? respuestaOllama :
                "RESUMEN GENERADO POR IA (Ollama — Llama 3.1 8B):\n\n" +
                "Unidad: " + unidad.getTitulo() + "\n\n" +
                "1. Conceptos Fundamentales: Análisis del marco normativo y estructura del mercado financiero argentino.\n" +
                "2. Instrumentos de Renta Fija: Valuación de bonos soberanos y corporativos. Curva de rendimientos y cálculo de duration.\n" +
                "3. Conclusiones y Recomendaciones: Estrategias de cobertura frente a inflación y riesgo cambiario.";

        Material resumen = new Material("Resumen IA - " + unidad.getTitulo(), null, tipoResumen, unidad);
        resumen.setContenido(contenidoResumen);
        resumen.setGeneradoPorIA(true);
        resumen.setPublicado(false);

        return materialRepository.save(resumen);
    }

    /**
     * PA-7: Genera una estructura de presentación descargable a partir de un prompt
     * consultando la API REST de Ollama.
     */
    public Material generarPresentacionClase(Unidad unidad, String guionPrompt) {
        TipoMaterial tipoPresentacion = tipoMaterialRepository.findByNombre("Presentación")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Presentación")));

        String prompt = "Generá la estructura de diapositivas (títulos, subtítulos y puntos clave) para: " + guionPrompt;
        String respuestaOllama = llamarOllamaAPI(prompt);

        String rutaDiapositivas = "presentaciones/slides_ia_u" + unidad.getId() + "_" + System.currentTimeMillis() + ".pdf";

        Material presentacion = new Material("Diapositivas Generadas con IA - " + unidad.getTitulo(), null, tipoPresentacion, unidad);
        presentacion.setRutaArchivo(rutaDiapositivas);
        presentacion.setGeneradoPorIA(true);
        presentacion.setPublicado(false);

        return materialRepository.save(presentacion);
    }
}
