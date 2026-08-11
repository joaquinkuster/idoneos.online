package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tipo_accion_auditoria")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoAccionAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @OneToMany(mappedBy = "tipoAccionAuditoria", cascade = CascadeType.ALL)
    private List<Auditoria> auditorias = new ArrayList<>();

    public TipoAccionAuditoria(String nombre) {
        this.nombre = nombre;
    }
}
