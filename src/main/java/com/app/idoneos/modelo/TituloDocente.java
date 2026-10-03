package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Título académico o profesional de un docente.
 */
@Entity
@Table(name = "TituloDocente")
@Getter
@Setter
@NoArgsConstructor
public class TituloDocente {

    /**
     * Identificador único del título del docente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTituloDocente")
    private int idTituloDocente;

    /**
     * Relación con Docente.
     */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /**
     * Nombre del título.
     */
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    /**
     * Matrícula profesional asociada al título. Es opcional.
     */
    @Column(name = "matricula", nullable = true, length = 50)
    private String matricula;

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
     * Constructor para crear el título del docente con los datos básicos.
     *
     * @param docente relación con Docente.
     * @param titulo nombre del título.
     */
    public TituloDocente(Docente docente, String titulo) {
        this.docente = docente;
        this.titulo = titulo;
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
     * Devuelve una representación en forma de cadena del título del docente.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return titulo;
    }
}
