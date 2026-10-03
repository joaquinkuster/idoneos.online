package com.app.idoneos.modelo;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Perfil de un usuario con el rol de alumno. Se inscribe en cohortes, rinde autoevaluaciones y consulta el foro.
 */
@Entity
@Table(name = "Alumno")
@Getter
@Setter
@NoArgsConstructor
public class Alumno {

    /**
     * Identificador único del alumno.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlumno")
    private int idAlumno;

    /**
     * Usuario al que pertenece este perfil de rol.
     */
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    /**
     * Conjunto de inscripciones asociados.
     */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    /**
     * Constructor para crear el alumno con los datos básicos.
     *
     * @param usuario usuario al que pertenece este perfil de rol.
     */
    public Alumno(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Devuelve una representación en forma de cadena del alumno.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Alumno #" + idAlumno;
    }
}
