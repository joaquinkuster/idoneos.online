package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Configuración: Parámetro operativo del sistema expresado en formato
 * clave-valor.
 * Mapea directamente a la tabla "Configuracion" en base_datos.sql.
 */
@Entity
@Table(name = "Configuracion")
public class Configuracion {

    /** Identificador único del parámetro de configuración. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    /** Clave identificadora del parámetro (ej. "evaluacion.intentos_maximos"). */
    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    /** Valor asociado guardado como texto para soportar distintos datos. */
    @Column(name = "valor", nullable = false, columnDefinition = "text")
    private String valor;

    /** Administrador que registró o modificó la configuración (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id", nullable = true)
    private Administrador administrador;

    public Configuracion() {
    }

    public Configuracion(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }
}
