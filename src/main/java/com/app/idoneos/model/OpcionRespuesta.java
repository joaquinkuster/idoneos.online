package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad OpcionRespuesta: Opción de respuesta asociada a una Pregunta de evaluación.
 * Mapea directamente a la tabla "OpcionRespuesta" en base_datos.sql.
 */
@Entity
@Table(name = "OpcionRespuesta")
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion_respuesta")
    private int idOpcionRespuesta;

    /** Texto de la opción de respuesta. */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /** Indica si esta opción es la respuesta correcta. */
    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta = false;

    /** Estado de baja lógica de la opción. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Pregunta a la que pertenece esta opción. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private Pregunta pregunta;

    public OpcionRespuesta() {
    }

    public OpcionRespuesta(String texto, boolean esCorrecta, Pregunta pregunta) {
        this.texto = texto;
        this.esCorrecta = esCorrecta;
        this.pregunta = pregunta;
    }

    public int getId() {
        return idOpcionRespuesta;
    }

    public int getIdOpcionRespuesta() {
        return idOpcionRespuesta;
    }

    public void setId(int id) {
        this.idOpcionRespuesta = id;
    }

    public void setIdOpcionRespuesta(int idOpcionRespuesta) {
        this.idOpcionRespuesta = idOpcionRespuesta;
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
