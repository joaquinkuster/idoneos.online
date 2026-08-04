package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de tipos de acción de auditoría: Crear, Modificar, Eliminar, Consultar.
 * Valores genéricos para reutilizar un interceptor Spring AOP.
 */
@Entity
@Table(name = "tipos_accion_auditoria")
@Getter @Setter
@NoArgsConstructor
public class TipoAccionAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public TipoAccionAuditoria(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() { return nombre; }
}
