package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clase_en_vivo")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaseEnVivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "url_rtmp", nullable = false, length = 255)
    private String urlRtmp;

    @Column(name = "clave_stream", nullable = false, length = 100)
    private String claveStream;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_clase_en_vivo_id", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;
}
