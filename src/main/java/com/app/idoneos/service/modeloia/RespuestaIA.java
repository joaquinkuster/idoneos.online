package com.app.idoneos.service.modeloia;

import com.fasterxml.jackson.annotation.JsonProperty;

/** La respuesta que da el modelo de inteligencia artificial a una pregunta, usando solo la oración de la que salió. */
public record RespuestaIA(@JsonProperty(required = true) String respuesta) {
}
