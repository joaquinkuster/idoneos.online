package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Parámetros de configuración del sistema en modelo clave-valor.
 * Más flexible que una tabla con una columna por parámetro — permite
 * agregar configuraciones nuevas sin modificar el esquema.
 * Ejemplos: "plataforma.nombre", "evaluacion.umbral_aprobacion", "logo.url"
 */
@Entity
@Table(name = "configuracion")
@Getter @Setter
@NoArgsConstructor
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    /**
     * Texto para poder representar cualquier tipo de dato sin cambiar el esquema.
     */
    @Column(name = "valor", nullable = true, columnDefinition = "TEXT")
    private String valor;

    /**
     * DDL: Configuracion tiene FK a Administrador — administrador que creó/modificó el parámetro.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = true)
    private Administrador administrador;

    // ─── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }

    public Configuracion(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }
}
