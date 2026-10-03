package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Descuento porcentual por cantidad de cursos, con vigencia y cantidad límite de usos.
 */
@Entity
@Table(name = "Descuento")
@Getter
@Setter
@NoArgsConstructor
public class Descuento {

    /**
     * Identificador único del descuento.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDescuento")
    private int idDescuento;

    /**
     * Nombre del descuento.
     */
    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    /**
     * Cantidad de cursos que el alumno debe inscribirse para acceder al descuento.
     */
    @Column(name = "cursosRequeridos", nullable = false)
    private int cursosRequeridos;

    /**
     * Porcentaje de descuento (0-100).
     */
    @Column(name = "porcentaje", nullable = false)
    private float porcentaje;

    /**
     * Fecha y hora desde la que el descuento está vigente.
     */
    @Column(name = "vigenciaDesde", nullable = false)
    private LocalDateTime vigenciaDesde;

    /**
     * Fecha y hora hasta la que el descuento está vigente.
     */
    @Column(name = "vigenciaHasta", nullable = false)
    private LocalDateTime vigenciaHasta;

    /**
     * Cantidad máxima de veces que puede aplicarse el descuento.
     */
    @Column(name = "cantidadLimite", nullable = false)
    private int cantidadLimite;

    /**
     * Cantidad de veces que ya se aplicó el descuento.
     */
    @Column(name = "cantidadUsada", nullable = false)
    private int cantidadUsada;

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
     * Conjunto de pagos asociados.
     */
    @OneToMany(mappedBy = "descuento", cascade = CascadeType.ALL)
    private Set<Pago> pagos = new HashSet<>();

    /**
     * Constructor para crear el descuento con los datos básicos.
     *
     * @param nombre nombre del descuento.
     * @param cursosRequeridos cantidad de cursos que el alumno debe inscribirse para acceder al descuento.
     * @param porcentaje porcentaje de descuento (0-100).
     * @param vigenciaDesde fecha y hora desde la que el descuento está vigente.
     * @param vigenciaHasta fecha y hora hasta la que el descuento está vigente.
     * @param cantidadLimite cantidad máxima de veces que puede aplicarse el descuento.
     * @param cantidadUsada cantidad de veces que ya se aplicó el descuento.
     */
    public Descuento(String nombre, int cursosRequeridos, float porcentaje, LocalDateTime vigenciaDesde, LocalDateTime vigenciaHasta, int cantidadLimite, int cantidadUsada) {
        this.nombre = nombre;
        this.cursosRequeridos = cursosRequeridos;
        this.porcentaje = porcentaje;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.cantidadLimite = cantidadLimite;
        this.cantidadUsada = cantidadUsada;
    }

    /**
     * Devuelve una representación en forma de cadena del descuento.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
