package com.app.idoneos.modelo;

import java.text.Normalizer;
import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Término del glosario de un tema, con su definición.
 */
@Entity
@Table(name = "TerminoGlosario", uniqueConstraints = { @UniqueConstraint(columnNames = {"idTema", "termino"}) })
@Check(name = "ck_termino_glosario_termino", constraints = "trim(termino) <> ''")
@Check(name = "ck_termino_glosario_definicion", constraints = "trim(definicion) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class TerminoGlosario {

    /** Cantidad máxima de caracteres del término. */
    public static final int LONGITUD_TERMINO = 50;

    /** Cantidad máxima de caracteres de la definición. */
    public static final int LONGITUD_DEFINICION = 500;

    /** Letra con la que se agrupan los términos que no empiezan con una letra de la A a la Z (números, símbolos). */
    public static final String LETRA_ESPECIAL = "#";

    /**
     * Identificador único del término del glosario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTermino")
    private int idTermino;

    /**
     * Relación con Tema.
     */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /**
     * Término del glosario.
     */
    @Column(name = "termino", nullable = false, length = LONGITUD_TERMINO)
    private String termino;

    /**
     * Definición del término.
     */
    @Column(name = "definicion", nullable = false, length = LONGITUD_DEFINICION)
    private String definicion;

    /**
     * Fecha y hora de creación del registro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Indica si el registro fue dado de baja (baja lógica). El valor predeterminado es "false", indicando que está vigente.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear el término del glosario con los datos básicos.
     *
     * @param tema relación con Tema.
     * @param termino término del glosario.
     * @param definicion definición del término.
     */
    public TerminoGlosario(Tema tema, String termino, String definicion) {
        this.tema = tema;
        this.termino = termino;
        this.definicion = definicion;
    }

    /**
     * Obtiene la letra con la que se ordena el término en el índice del glosario: la inicial en mayúscula y sin tilde
     * (la Ñ cuenta como N), o {@link #LETRA_ESPECIAL} si no empieza con una letra de la A a la Z.
     *
     * @return La letra de la A a la Z, o "#".
     */
    public String getLetraInicial() {
        if (termino == null || termino.isBlank()) {
            return LETRA_ESPECIAL;
        }
        String sinTilde = Normalizer.normalize(termino.trim().substring(0, 1), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase();
        return sinTilde.matches("[A-Z]") ? sinTilde : LETRA_ESPECIAL;
    }

    /**
     * Obtiene la fecha de la última actualización del término: la de su última modificación o, si nunca se modificó,
     * la de su creación.
     *
     * @return La fecha y hora de la última actualización.
     */
    public LocalDateTime getUltimaActualizacion() {
        return ultimaModificacion != null ? ultimaModificacion : fechaCreacion;
    }

    /**
     * Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true.
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja (baja = true), false si está vigente.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena del término del glosario.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return termino;
    }
}
