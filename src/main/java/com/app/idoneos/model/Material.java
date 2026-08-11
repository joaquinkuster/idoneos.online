package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "material")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "ruta_archivo", length = 150)
    private String rutaArchivo;

    @Column(name = "contenido", length = 500)
    private String contenido;

    @Column(name = "duracion")
    private Integer duracion;

    @Column(name = "autor", length = 50)
    private String autor;

    @Column(name = "generado_por_ia", nullable = false)
    private boolean generadoPorIa = false;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga = LocalDateTime.now();

    @Column(name = "publicado", nullable = false)
    private boolean publicado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private Unidad unidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_material_id", nullable = false)
    private TipoMaterial tipoMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = true)
    private Docente docente;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    public Material(TipoMaterial tipoMaterial, String titulo, String contenido, Unidad unidad) {
        this.tipoMaterial = tipoMaterial;
        this.titulo = titulo;
        this.contenido = contenido;
        this.unidad = unidad;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Integer getDuracion() { return duracion; }
    public void setDuracion(Integer duracion) { this.duracion = duracion; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public boolean isGeneradoPorIa() { return generadoPorIa; }
    public boolean getGeneradoPorIa() { return generadoPorIa; }
    public void setGeneradoPorIa(boolean generadoPorIa) { this.generadoPorIa = generadoPorIa; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime fechaCarga) { this.fechaCarga = fechaCarga; }

    public boolean isPublicado() { return publicado; }
    public boolean getPublicado() { return publicado; }
    public void setPublicado(boolean publicado) { this.publicado = publicado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Unidad getUnidad() { return unidad; }
    public void setUnidad(Unidad unidad) { this.unidad = unidad; }

    public TipoMaterial getTipoMaterial() { return tipoMaterial; }
    public void setTipoMaterial(TipoMaterial tipoMaterial) { this.tipoMaterial = tipoMaterial; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }

    public List<ClaseClonIA> getClasesClonIA() { return clasesClonIA; }
    public void setClasesClonIA(List<ClaseClonIA> clasesClonIA) { this.clasesClonIA = clasesClonIA; }

    public List<ClaseEnVivo> getClasesEnVivo() { return clasesEnVivo; }
    public void setClasesEnVivo(List<ClaseEnVivo> clasesEnVivo) { this.clasesEnVivo = clasesEnVivo; }


    public void setGeneradoPorIA(boolean generadoPorIA) { this.generadoPorIa = generadoPorIA; }
    public boolean isGeneradoPorIA() { return generadoPorIa; }

}
