package com.app.idoneos.modelo;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Perfil de un usuario con el rol de docente. Reúne su experiencia, biografía y datos para el clon con inteligencia artificial.
 */
@Entity
@Table(name = "Docente")
@Getter
@Setter
@NoArgsConstructor
public class Docente {

    /**
     * Identificador único del docente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocente")
    private int idDocente;

    /**
     * Usuario al que pertenece este perfil de rol.
     */
    @OneToOne
    @JoinColumn(name = "idUsuario", nullable = false, unique = true)
    private Usuario usuario;

    /**
     * Años de experiencia profesional del docente.
     */
    @Column(name = "aniosExperiencia", nullable = true)
    private Integer aniosExperiencia;

    /**
     * Matrícula del docente ante la CNV.
     */
    @Column(name = "matriculaCnv", nullable = true, length = 50, unique = true)
    private String matriculaCnv;

    /**
     * Biografía del docente.
     */
    @Column(name = "biografia", nullable = true, columnDefinition = "TEXT")
    private String biografia;

    /**
     * Fecha y hora en que el docente aceptó los términos y condiciones del clon con inteligencia artificial.
     */
    @Column(name = "fechaAceptacionTycClon", nullable = true)
    private LocalDateTime fechaAceptacionTycClon;

    /**
     * Identificador del avatar del docente en el servicio de generación de video.
     */
    @Column(name = "avatarId", nullable = true, length = 100, unique = true)
    private String avatarId;

    /**
     * Identificador de la voz del docente en el servicio de generación de video.
     */
    @Column(name = "voiceId", nullable = true, length = 100, unique = true)
    private String voiceId;

    /**
     * Indica si el docente está habilitado para dictar clases, ya sea como titular o como ayudante en un curso.
     */
    @Column(name = "habilitado", nullable = false)
    private Boolean habilitado = false;

    /**
     * Conjunto de titulosDocente asociados.
     */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private Set<TituloDocente> titulosDocente = new HashSet<>();

    /**
     * Conjunto de participacionesDocente asociados.
     */
    @OneToMany(mappedBy = "docente", cascade = CascadeType.ALL)
    private Set<ParticipacionDocente> participacionesDocente = new HashSet<>();

    /**
     * Constructor para crear el docente con los datos básicos.
     *
     * @param usuario usuario al que pertenece este perfil de rol.
     */
    public Docente(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Devuelve una representación en forma de cadena del docente.
     *
     * @return La representación textual.
     */
    @Override
    public String toString() {
        return "Docente #" + idDocente;
    }
}
