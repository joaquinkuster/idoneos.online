package com.app.idoneos.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos propios de un recurso que es un enlace externo.
 */
@Entity
@Table(name = "Enlace")
@Getter
@Setter
@NoArgsConstructor
public class Enlace {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEnlace")
    private int idEnlace;

    /**
     * Relación con Recurso: el recurso del que esta tabla guarda los datos propios.
     */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /**
     * Dirección web (comienza con http:// o https://).
     */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /**
     * Constructor para crear el enlace con los datos básicos.
     *
     * @param recurso el recurso.
     * @param url     la dirección web.
     */
    public Enlace(Recurso recurso, String url) {
        this.recurso = recurso;
        this.url = url;
    }
}
