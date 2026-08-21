package com.app.idoneos.model;

import jakarta.persistence.*;

/**
 * Entidad Configuracion: Parámetros de configuración del sistema gestionados por Administradores.
 * Mapea directamente a la tabla "Configuracion" en base_datos.sql.
 */
@Entity
@Table(name = "Configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_configuracion")
    private int idConfiguracion;

    /** Clave del parámetro de configuración. */
    @Column(name = "clave", nullable = false, length = 100)
    private String clave;

    /** Valor del parámetro de configuración. */
    @Column(name = "valor", nullable = false, columnDefinition = "text")
    private String valor;

    /** Administrador que creó o es responsable de la configuración. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = false)
    private Administrador administrador;

    public Configuracion() {
    }

    public Configuracion(String clave, String valor, Administrador administrador) {
        this.clave = clave;
        this.valor = valor;
        this.administrador = administrador;
    }

    public int getId() {
        return idConfiguracion;
    }

    public int getIdConfiguracion() {
        return idConfiguracion;
    }

    public void setId(int id) {
        this.idConfiguracion = id;
    }

    public void setIdConfiguracion(int idConfiguracion) {
        this.idConfiguracion = idConfiguracion;
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
