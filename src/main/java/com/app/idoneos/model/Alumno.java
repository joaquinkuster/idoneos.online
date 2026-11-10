package com.app.idoneos.model;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa el perfil de un usuario con el rol de alumno. */
@Entity
@Table(name = "Alumno")
@Getter
@Setter
@NoArgsConstructor
public class Alumno {

    /** Identificador único del alumno. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAlumno")
    private int idAlumno;

    /** Usuario al que pertenece este perfil de rol. */
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    /** Inscripciones que referencian a este registro. */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL)
    private Set<Inscripcion> inscripciones = new HashSet<>();

    /** Crea el alumno con los datos básicos. */
    public Alumno(Usuario usuario) {
        this.usuario = usuario;
    }

    /** Devuelve una representación en forma de cadena del alumno. */
    @Override
    public String toString() {
        return "Alumno #" + idAlumno;
    }

    /** Devuelve la inscripción activa del alumno a alguna cohorte del curso. */
    public Inscripcion getInscripcionActivaEn(Curso curso) {
        return inscripciones.stream()
                .filter(i -> !i.esInactivo() && i.getCohorte().getCurso().getIdCurso() == curso.getIdCurso())
                .findFirst().orElse(null);
    }
}
