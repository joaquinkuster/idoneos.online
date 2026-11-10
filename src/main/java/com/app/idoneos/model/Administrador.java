package com.app.idoneos.model;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el perfil de un usuario con el rol de administrador del sistema. */
@Entity
@Table(name = "Administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    /** Identificador único del administrador. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdministrador")
    private int idAdministrador;

    /** Usuario al que pertenece este perfil de rol. */
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    /** Reportes que referencian a este registro. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private Set<Reporte> reportes = new HashSet<>();

    /** Parametros que referencian a este registro. */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private Set<Parametro> parametros = new HashSet<>();

    /** Crea el administrador con los datos básicos. */
    public Administrador(Usuario usuario) {
        this.usuario = usuario;
    }

    /** Devuelve una representación en forma de cadena del administrador. */
    @Override
    public String toString() {
        return "Administrador #" + idAdministrador;
    }
}
