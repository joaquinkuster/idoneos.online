package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa la posición y duración en días de un tema dentro del cronograma de un programa. */
@Entity
@Table(name = "TemaCronograma", uniqueConstraints = { @UniqueConstraint(columnNames = {"idPrograma", "idTema"}), @UniqueConstraint(columnNames = {"idPrograma", "numeroOrden"}) })
@Check(name = "ck_tema_cronograma_duracion_dias", constraints = "duracion_dias > 0")
@Check(name = "ck_tema_cronograma_orden", constraints = "numero_orden > 0")
@Getter
@Setter
@NoArgsConstructor
public class TemaCronograma {

    /** Identificador único del tema del cronograma. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTemaCronograma")
    private int idTemaCronograma;

    /** Programa de este registro. */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = false)
    private Programa programa;

    /** Tema de este registro. */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Número de orden del tema dentro del cronograma. */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /** Duración del tema en días. */
    @Column(name = "duracionDias", nullable = false)
    private int duracionDias;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea el tema del cronograma con los datos básicos. */
    public TemaCronograma(Programa programa, Tema tema, int numeroOrden, int duracionDias) {
        this.programa = programa;
        this.tema = tema;
        this.numeroOrden = numeroOrden;
        this.duracionDias = duracionDias;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve una representación en forma de cadena del tema del cronograma. */
    @Override
    public String toString() {
        return "TemaCronograma #" + idTemaCronograma;
    }

    /** Obtiene el primer día del programa en el que se dicta el tema. */
    public int getDiaDesde() {
        return programa.getDiaDesde(this);
    }

    /** Obtiene el último día del programa en el que se dicta el tema. */
    public int getDiaHasta() {
        return programa.getDiaHasta(this);
    }

    /** Obtiene la primera columna que ocupa el tema al dibujar el cronograma (ver Programa.getPasoCronograma()). */
    public int getColumnaDesde() {
        return (getDiaDesde() - 1) / programa.getPasoCronograma() + 1;
    }

    /** Obtiene la última columna que ocupa el tema al dibujar el cronograma. */
    public int getColumnaHasta() {
        int paso = programa.getPasoCronograma();
        return (getDiaHasta() + paso - 1) / paso;
    }
}
