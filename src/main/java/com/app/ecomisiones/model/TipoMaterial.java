package com.app.ecomisiones.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Catálogo de tipos de material didáctico: Grabación, Bibliografía, Presentación, Resumen.
 * Glosario NO es un tipo de material — se modela como TerminoGlosario (entidad separada).
 * Se convirtió de enum a entidad catálogo porque tiene más de 2 valores.
 */
@Entity
@Table(name = "tipos_material")
@Getter @Setter
@NoArgsConstructor
public class TipoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    public TipoMaterial(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
