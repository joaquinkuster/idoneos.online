package com.app.idoneos.service.modeloia;

import com.fasterxml.jackson.annotation.JsonProperty;

/** La pregunta que escribe el modelo de inteligencia artificial a partir de una oración del material. */
public record PreguntaIA(@JsonProperty(required = true) String pregunta) {
}
