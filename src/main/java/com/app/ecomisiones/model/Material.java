package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Material didáctico (video, diapositiva, lectura, resumen, glosario) de una unidad.
 */
@Entity
@Table(name = "materiales")
@Getter @Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMaterial tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "ruta", nullable = true, length = 500)
    private String ruta;

    @Column(name = "publicado", nullable = false)
    private Boolean publicado = true;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "id_unidad", nullable = false)
    private Unidad unidad;

    public Material(TipoMaterial tipo, String titulo, String ruta, Unidad unidad) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.ruta = ruta;
        this.unidad = unidad;
    }
}
