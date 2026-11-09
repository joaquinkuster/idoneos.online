package com.app.idoneos.util;

import org.springframework.stereotype.Component;

/**
 * Los textos de los avisos de «no se puede modificar» para las vistas ({@code ${@textos.avisoCurso}}). Vienen de
 * {@link MensajeErrorUtil}, que es la única fuente de estos mensajes.
 */
@Component("textos")
public class TextosBloqueo {

    public String getAvisoCurso() {
        return MensajeErrorUtil.AVISO_CURSO;
    }

    public String getAvisoDiseno() {
        return MensajeErrorUtil.AVISO_DISENO;
    }

    public String getAvisoCohorte() {
        return MensajeErrorUtil.AVISO_COHORTE;
    }

    public String getMotivoPublicado() {
        return MensajeErrorUtil.MOTIVO_PUBLICADO;
    }

    public String getMotivoCursoPublicado() {
        return MensajeErrorUtil.MOTIVO_CURSO_PUBLICADO;
    }
}
