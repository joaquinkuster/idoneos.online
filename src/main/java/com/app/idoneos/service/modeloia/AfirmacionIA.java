package com.app.idoneos.service.modeloia;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Paráfrasis verdadera de una oración del material que escribe el modelo de inteligencia artificial. */
public record AfirmacionIA(@JsonProperty(required = true) String afirmacion) {
}
