package com.app.idoneos.model;
import com.app.idoneos.service.modulo_reportes.*;


import jakarta.persistence.*;

/**
 * Entidad Pregunta: Pregunta individual perteneciente a un Pool de evaluación.
 * Mapea directamente a la tabla "Pregunta" en base_datos.sql.
 */
@Entity
@Table(name = "Pregunta")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private int idPregunta;

    /** Enunciado o texto de la pregunta. */
    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    /** Indica si es de opción múltiple (true) o verdadero/falso (false). */
    @Column(name = "es_opcion_multiple", nullable = false)
    private boolean esOpcionMultiple = true;

    /** Estado de baja lógica de la pregunta. */
    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    /** Pool de preguntas al que pertenece esta pregunta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pool", nullable = false)
    private Pool pool;

    public Pregunta() {
    }

    public Pregunta(String texto, boolean esOpcionMultiple, Pool pool) {
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
        this.pool = pool;
    }

    public int getId() {
        return idPregunta;
    }

    public int getIdPregunta() {
        return idPregunta;
    }

    public void setId(int id) {
        this.idPregunta = id;
    }

    public void setIdPregunta(int idPregunta) {
        this.idPregunta = idPregunta;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isEsOpcionMultiple() {
        return esOpcionMultiple;
    }

    public boolean getEsOpcionMultiple() {
        return esOpcionMultiple;
    }

    public void setEsOpcionMultiple(boolean esOpcionMultiple) {
        this.esOpcionMultiple = esOpcionMultiple;
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

    public Pool getPool() {
        return pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }
}

