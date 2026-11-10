package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un título académico o profesional de un docente. */
@Entity
@Table(name = "TituloDocente")
@Check(name = "ck_titulo_docente_titulo", constraints = "trim(titulo) <> ''")
@Getter
@Setter
@NoArgsConstructor
public class TituloDocente {

    /** Identificador único del título del docente. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTituloDocente")
    private int idTituloDocente;

    /** Docente de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDocente", nullable = false)
    private Docente docente;

    /** Nombre del título. */
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    /** Matrícula profesional asociada al título. */
    @Column(name = "matricula", nullable = true, length = 50)
    private String matricula;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea el título del docente con los datos básicos. */
    public TituloDocente(Docente docente, String titulo) {
        this.docente = docente;
        this.titulo = titulo;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del título del docente. */
    @Override
    public String toString() {
        return titulo;
    }
}
