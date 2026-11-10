package com.app.idoneos.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa los datos propios de un recurso que es un enlace externo. */
@Entity
@Table(name = "Enlace")
@Check(name = "ck_enlace_url", constraints = "url ~ '^https?://'")
@Getter
@Setter
@NoArgsConstructor
public class Enlace {

    /** Identificador único. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEnlace")
    private int idEnlace;

    /** Relación con Recurso: el recurso del que esta tabla guarda los datos propios. */
    @OneToOne
    @JoinColumn(name = "idRecurso", nullable = false, unique = true)
    private Recurso recurso;

    /** Dirección web (comienza con http:// o https://). */
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    /** Crea el enlace con los datos básicos. */
    public Enlace(Recurso recurso, String url) {
        this.recurso = recurso;
        this.url = url;
    }
}
