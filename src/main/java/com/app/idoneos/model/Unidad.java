package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "unidad")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "numero_orden", nullable = false)
    private int numeroOrden;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "programa_id", nullable = false)
    private Programa programa;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Material> materiales = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseClonIA> clasesClonIA = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ClaseEnVivo> clasesEnVivo = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<ConsultaForo> consultasForo = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Autoevaluacion> autoevaluaciones = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Pool> pools = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<Progreso> progresos = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    private List<TerminoGlosario> terminosGlosario = new ArrayList<>();

    public Unidad(String titulo, String descripcion, int numeroOrden, Programa programa) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        this.programa = programa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(int numeroOrden) { this.numeroOrden = numeroOrden; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimaModificacion() { return ultimaModificacion; }
    public void setUltimaModificacion(LocalDateTime ultimaModificacion) { this.ultimaModificacion = ultimaModificacion; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Programa getPrograma() { return programa; }
    public void setPrograma(Programa programa) { this.programa = programa; }

    public List<Material> getMateriales() { return materiales; }
    public void setMateriales(List<Material> materiales) { this.materiales = materiales; }

    public List<ClaseClonIA> getClasesClonIA() { return clasesClonIA; }
    public void setClasesClonIA(List<ClaseClonIA> clasesClonIA) { this.clasesClonIA = clasesClonIA; }

    public List<ClaseEnVivo> getClasesEnVivo() { return clasesEnVivo; }
    public void setClasesEnVivo(List<ClaseEnVivo> clasesEnVivo) { this.clasesEnVivo = clasesEnVivo; }

    public List<ConsultaForo> getConsultasForo() { return consultasForo; }
    public void setConsultasForo(List<ConsultaForo> consultasForo) { this.consultasForo = consultasForo; }

    public List<Autoevaluacion> getAutoevaluaciones() { return autoevaluaciones; }
    public void setAutoevaluaciones(List<Autoevaluacion> autoevaluaciones) { this.autoevaluaciones = autoevaluaciones; }

    public List<Pool> getPools() { return pools; }
    public void setPools(List<Pool> pools) { this.pools = pools; }

    public List<Progreso> getProgresos() { return progresos; }
    public void setProgresos(List<Progreso> progresos) { this.progresos = progresos; }

    public List<TerminoGlosario> getTerminosGlosario() { return terminosGlosario; }
    public void setTerminosGlosario(List<TerminoGlosario> terminosGlosario) { this.terminosGlosario = terminosGlosario; }


    public Curso getCurso() { return programa != null ? programa.getCurso() : null; }


    /** Constructor de compatibilidad: SemillaService y DocenteController pasan un Curso.
     *  Crea un Programa anónimo que se persistirá en cascada. */
    public Unidad(String titulo, String descripcion, int numeroOrden, Curso curso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.numeroOrden = numeroOrden;
        // Crea un Programa asociado al Curso (1:1 lógico en los dictados iniciales)
        Programa p = new Programa();
        p.setNombre(titulo.length() > 50 ? titulo.substring(0, 50) : titulo);
        p.setDescripcion(descripcion != null && descripcion.length() > 150 ? descripcion.substring(0, 150) : descripcion);
        p.setMesesAcceso(curso.getMesesAcceso() > 0 ? curso.getMesesAcceso() : 12);
        p.setCurso(curso);
        this.programa = p;
    }

}
