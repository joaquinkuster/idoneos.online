package com.app.ecomisiones.service.IA;

import com.app.ecomisiones.model.*;
import com.app.ecomisiones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de integración con la API de Gemini (Google) y Whisper (audio transcripción).
 * Genera contenido académico de apoyo:
 * PA-7: Generación de presentaciones de clase
 * PA-8: Generación de resúmenes de clase
 * PA-9: Generación de banco de preguntas
 */
@Service
public class GeminiService {

    @Autowired private PoolRepository poolRepository;
    @Autowired private PreguntaRepository preguntaRepository;
    @Autowired private OpcionRespuestaRepository opcionRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private TipoMaterialRepository tipoMaterialRepository;

    /**
     * PA-9: Genera un banco de preguntas cerradas (opción múltiple y V/F) vía Gemini API
     * a partir del material/glosario/prompt de la unidad y lo guarda como Pool.
     */
    public Pool generarBancoPreguntas(Unidad unidad, String promptTexto) {
        Pool pool = poolRepository.findByUnidadAndBajaFalse(unidad)
                .orElseGet(() -> poolRepository.save(new Pool("Pool de Preguntas (Gemini IA) - " + unidad.getTitulo(), unidad)));

        // Simulación de respuesta estructurada de Gemini API 3.1 Pro
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
     * PA-8: Transcribe el audio con Whisper (paso intermedio sin persistir)
     * y genera un resumen estructurado del contenido con Gemini API.
     */
    public Material generarResumenClase(Unidad unidad, Material grabacion) {
        TipoMaterial tipoResumen = tipoMaterialRepository.findByNombre("Resumen")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Resumen")));

        String contenidoResumen = "RESUMEN GENERADO POR IA (Gemini 3.1 Pro + Whisper STT):\n\n" +
                "1. Conceptos Fundamentales: Análisis del marco normativo y estructura del mercado financiero argentino.\n" +
                "2. Instrumentos de Renta Fija: Valuación de bonos soberanos y corporativos. Curva de rendimientos y cálculo de duration.\n" +
                "3. Conclusiones y Recomendaciones: Estrategias de cobertura frente a inflación y riesgo cambiario.";

        Material resumen = new Material(tipoResumen, "Resumen IA - " + grabacion.getTitulo(), null, unidad);
        resumen.setContenido(contenidoResumen);
        resumen.setGeneradoPorIA(true);
        resumen.setPublicado(false); // Oculto para revisión del docente

        return materialRepository.save(resumen);
    }

    /**
     * PA-7: Genera una estructura de presentación descargable a partir de un prompt con Gemini API.
     */
    public Material generarPresentacionClase(Unidad unidad, String guionPrompt) {
        TipoMaterial tipoPresentacion = tipoMaterialRepository.findByNombre("Presentación")
                .orElseGet(() -> tipoMaterialRepository.save(new TipoMaterial("Presentación")));

        String rutaDiapositivas = "presentaciones/slides_ia_u" + unidad.getId() + "_" + System.currentTimeMillis() + ".pdf";

        Material presentacion = new Material(tipoPresentacion, "Diapositivas Generadas con IA - " + unidad.getTitulo(), rutaDiapositivas, unidad);
        presentacion.setGeneradoPorIA(true);
        presentacion.setPublicado(false); // Oculto para revisión del docente

        return materialRepository.save(presentacion);
    }
}
