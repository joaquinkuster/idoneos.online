package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad OpciónRespuesta: Opción elegible dentro de una Pregunta de
 * autoevaluación.
 * Mapea directamente a la tabla "OpcionRespuesta" en base_datos.sql.
 */
@Entity
@Table(name = "OpcionRespuesta")
public class OpcionRespuesta {

    /** Identificador único de la opción de respuesta. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Texto explicativo o enunciado de la opción. */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /** Indica si esta opción constituye la respuesta correcta de la pregunta. */
    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta = false;

    /** Estado de baja lógica de la opción. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Pregunta a la que pertenece la opción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private Pregunta pregunta;

    public OpcionRespuesta() {
    }

    public OpcionRespuesta(String texto, boolean esCorrecta, Pregunta pregunta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
        this.pregunta = pregunta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isEsCorrecta() {
        return esCorrecta;
    }

    public boolean getEsCorrecta() {
        return esCorrecta;
    }

    public void setEsCorrecta(boolean esCorrecta) {
        this.esCorrecta = esCorrecta;
    }

    public boolean isBaja() {
        return baja;
    }

    public boolean getBaja() {
        return baja;
    }

    public void setBaja(boolean baja) {
        this.baja = baja;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }
}
