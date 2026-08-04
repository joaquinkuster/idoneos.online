package com.app.idoneos.service.IA;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de integración con el modelo de IA local ejecutado mediante Ollama (Llama 3.1 8B, 128K contexto).
 * Genera contenido académico de apoyo sin enviar datos fuera de la red local y sin costo por uso.
 *
 * PA-7: Generación de presentaciones de clase
 * PA-8: Generación de resúmenes de unidad (a partir de la bibliografía cargada)
 * PA-9: Generación de banco de preguntas (a partir de bibliografía, glosario y prompt)
 *
 * Decisión de diseño: Ollama fue elegido sobre alternativas como Gemini API (externa, de pago)
 * o LM Studio (cambió sus condiciones de licencia comercial) por ser de código abierto, gratuito,
 * ejecutable localmente sin GPU dedicada y sin riesgo de cambios de política de terceros.
 * Ref.: https://ollama.com | https://ollama.com/library/llama3.1
 */
@Service
public class OllamaService {

    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;
    @Autowired private TerminoGlosarioRepository terminoGlosarioRepository;

    /**
     * PA-9: Genera un banco de preguntas cerradas (opción múltiple y V/F) mediante Ollama
     * a partir de la bibliografía, glosario y prompt opcional de la unidad.
     * El contenido académico no sale de la red local.
     */
    public Pool generarBancoPreguntas(Unidad unidad, String promptTexto) {
        Pool pool = poolRepository.findByUnidadAndBajaFalse(unidad)
                .orElseGet(() -> poolRepository.save(new Pool("Pool de Preguntas (Ollama IA) - " + unidad.getTitulo(), unidad)));

        // Construcción del contexto: bibliografía + glosario + prompt del docente
        // En producción: este contenido se envía a Ollama via REST (localhost:11434/api/generate)
        List<TerminoGlosario> glosario = terminoGlosarioRepository.findByUnidadAndBajaFalse(unidad);
        String contextoGlosario = glosario.stream()
                .map(t -> t.getTermino() + ": " + t.getDefinicion())
                .collect(Collectors.joining("; "));

        // Simulación de respuesta estructurada de Ollama (Llama 3.1 8B)
        // En producción: POST http://localhost:11434/api/generate
        //   { "model": "llama3.1", "prompt": "[Glosario/Biblio/Prompt] → Generá preguntas...", "stream": false }
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
     * PA-8: Genera un resumen estructurado de la unidad a partir de su bibliografía cargada,
     * enviada al modelo de IA local (Ollama).
     * NOTA: Esta versión NO usa Whisper ni transcripción de audio. El modelo procesa
     * directamente el texto de la bibliografía de la unidad.
     */
    public Material generarResumenUnidad(Unidad unidad) {
        TipoMaterial tipoResumen = tipoMaterialRepository.findByNombre("Resumen")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Resumen")));

        // En producción: se obtiene la bibliografía de la unidad y se envía a Ollama
        // POST http://localhost:11434/api/generate
        //   { "model": "llama3.1", "prompt": "[Bibliografía] → Generá un resumen académico estructurado...", "stream": false }
        String contenidoResumen = "RESUMEN GENERADO POR IA (Ollama — Llama 3.1 8B):\n\n" +
                "Unidad: " + unidad.getTitulo() + "\n\n" +
                "1. Conceptos Fundamentales: Análisis del marco normativo y estructura del mercado financiero argentino.\n" +
                "2. Instrumentos de Renta Fija: Valuación de bonos soberanos y corporativos. Curva de rendimientos y cálculo de duration.\n" +
                "3. Conclusiones y Recomendaciones: Estrategias de cobertura frente a inflación y riesgo cambiario.";

        Material resumen = new Material(tipoResumen, "Resumen IA - " + unidad.getTitulo(), null, unidad);
        resumen.setContenido(contenidoResumen);
        resumen.setGeneradoPorIA(true);
        resumen.setPublicado(false); // Oculto para revisión del docente

        return materialRepository.save(resumen);
    }

    /**
     * PA-7: Genera una estructura de presentación descargable a partir de un prompt
     * ingresado por el docente, enviado al modelo de IA local (Ollama).
     */
    public Material generarPresentacionClase(Unidad unidad, String guionPrompt) {
        TipoMaterial tipoPresentacion = tipoMaterialRepository.findByNombre("Presentación")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Presentación")));

        // En producción: se envía el prompt a Ollama y se formatea la respuesta como presentación
        // POST http://localhost:11434/api/generate
        //   { "model": "llama3.1", "prompt": "[guionPrompt] → Generá estructura de diapositivas...", "stream": false }
        String rutaDiapositivas = "presentaciones/slides_ia_u" + unidad.getId() + "_" + System.currentTimeMillis() + ".pdf";

        Material presentacion = new Material(tipoPresentacion, "Diapositivas Generadas con IA - " + unidad.getTitulo(), rutaDiapositivas, unidad);
        presentacion.setGeneradoPorIA(true);
        presentacion.setPublicado(false); // Oculto para revisión del docente

        return materialRepository.save(presentacion);
    }
}
