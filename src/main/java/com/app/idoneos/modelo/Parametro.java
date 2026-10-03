package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Parámetro de configuración del sistema, definido como un par clave y valor y gestionado por un administrador.
 */
@Entity
@Table(name = "Parametro")
@Getter
@Setter
@NoArgsConstructor
public class Parametro {

    /**
     * Identificador único del parámetro.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idParametro")
    private int idParametro;

    /**
     * Relación con Administrador.
     */
    @ManyToOne
    @JoinColumn(name = "idAdministrador", nullable = false)
    private Administrador administrador;

    /**
     * Clave del parámetro.
     */
    @Column(name = "clave", nullable = false, length = 100, unique = true)
    private String clave;

    /**
     * Valor del parámetro.
     */
    @Column(name = "valor", nullable = false, columnDefinition = "TEXT")
    private String valor;

    /**
     * Fecha y hora de creación del parámetro. Se establece al momento actual por defecto.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación del registro.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Constructor para crear el parámetro con los datos básicos.
     *
     * @param administrador relación con Administrador.
     * @param clave clave del parámetro.
     * @param valor valor del parámetro.
     */
    public Parametro(Administrador administrador, String clave, String valor) {
        this.administrador = administrador;
        this.clave = clave;
        this.valor = valor;
    }

    /**
     * Devuelve una representación en forma de cadena del parámetro.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return clave;
    }
}
