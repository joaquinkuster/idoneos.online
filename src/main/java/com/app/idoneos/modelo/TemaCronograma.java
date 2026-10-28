package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Posición y duración en días de un tema dentro del cronograma de un programa.
 */
@Entity
@Table(name = "TemaCronograma", uniqueConstraints = { @UniqueConstraint(columnNames = {"idPrograma", "idTema"}), @UniqueConstraint(columnNames = {"idPrograma", "numeroOrden"}) })
@Check(name = "ck_tema_cronograma_duracion_dias", constraints = "duracion_dias > 0")
@Getter
@Setter
@NoArgsConstructor
public class TemaCronograma {

    /**
     * Identificador único del tema del cronograma.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTemaCronograma")
    private int idTemaCronograma;

    /**
     * Relación con Programa.
     */
    @ManyToOne
    @JoinColumn(name = "idPrograma", nullable = false)
    private Programa programa;

    /**
     * Relación con Tema.
     */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /**
     * Número de orden del tema dentro del cronograma.
     */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /**
     * Duración del tema en días.
     */
    @Column(name = "duracionDias", nullable = false)
    private int duracionDias;

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
     * Constructor para crear el tema del cronograma con los datos básicos.
     *
     * @param programa relación con Programa.
     * @param tema relación con Tema.
     * @param numeroOrden número de orden del tema dentro del cronograma.
     * @param duracionDias duración del tema en días.
     */
    public TemaCronograma(Programa programa, Tema tema, int numeroOrden, int duracionDias) {
        this.programa = programa;
        this.tema = tema;
        this.numeroOrden = numeroOrden;
        this.duracionDias = duracionDias;
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
     * Devuelve una representación en forma de cadena del tema del cronograma.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "TemaCronograma #" + idTemaCronograma;
    }

    /**
     * Obtiene el primer día del programa en el que se dicta el tema.
     *
     * @return El número de día en que empieza el tema.
     */
    public int getDiaDesde() {
        return programa.getDiaDesde(this);
    }

    /**
     * Obtiene el último día del programa en el que se dicta el tema.
     *
     * @return El número de día en que termina el tema.
     */
    public int getDiaHasta() {
        return programa.getDiaHasta(this);
    }

    /**
     * Obtiene la primera columna que ocupa el tema al dibujar el cronograma (ver {@link Programa#getPasoCronograma()}).
     *
     * @return El número de columna, desde 1.
     */
    public int getColumnaDesde() {
        return (getDiaDesde() - 1) / programa.getPasoCronograma() + 1;
    }

    /**
     * Obtiene la última columna que ocupa el tema al dibujar el cronograma.
     *
     * @return El número de columna.
     */
    public int getColumnaHasta() {
        int paso = programa.getPasoCronograma();
        return (getDiaHasta() + paso - 1) / paso;
    }
}
