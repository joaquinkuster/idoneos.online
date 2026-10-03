package com.app.idoneos.modelo;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Perfil de un usuario con el rol de administrador del sistema. Gestiona parámetros y genera reportes.
 */
@Entity
@Table(name = "Administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    /**
     * Identificador único del administrador.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAdministrador")
    private int idAdministrador;

    /**
     * Usuario al que pertenece este perfil de rol.
     */
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    /**
     * Conjunto de reportes asociados.
     */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private Set<Reporte> reportes = new HashSet<>();

    /**
     * Conjunto de parametros asociados.
     */
    @OneToMany(mappedBy = "administrador", cascade = CascadeType.ALL)
    private Set<Parametro> parametros = new HashSet<>();

    /**
     * Constructor para crear el administrador con los datos básicos.
     *
     * @param usuario usuario al que pertenece este perfil de rol.
     */
    public Administrador(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Devuelve una representación en forma de cadena del administrador.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Administrador #" + idAdministrador;
    }
}
