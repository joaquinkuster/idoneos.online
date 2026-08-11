package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "curso")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "precio", nullable = false)
    private double precio;

    @Column(name = "imagen", length = 150)
    private String imagen;

    @Column(name = "publicado", nullable = false)
    private boolean publicado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultima_modificacion")
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private List<Programa> programas = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL)
    private List<ModalidadCurso> modalidadesCursos = new ArrayList<>();

    public Curso(String nombre, String descripcion, double precio, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
    }

    public Curso(String nombre, String descripcion, float precio, Categoria categoria) {
        this(nombre, descripcion, (double) precio, categoria);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setPrecio(float precio) { this.precio = (double) precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

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

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public List<Programa> getProgramas() { return programas; }
    public void setProgramas(List<Programa> programas) { this.programas = programas; }

    public List<ModalidadCurso> getModalidadesCursos() { return modalidadesCursos; }
    public void setModalidadesCursos(List<ModalidadCurso> modalidadesCursos) { this.modalidadesCursos = modalidadesCursos; }

    // Helper compatibility getters for legacy services
    public List<Unidad> getUnidades() {
        List<Unidad> list = new ArrayList<>();
        if (programas != null) {
            for (Programa p : programas) {
                if (p.getUnidades() != null) {
                    list.addAll(p.getUnidades());
                }
            }
        }
        return list;
    }

    public int getMesesAcceso() {
        if (programas != null && !programas.isEmpty()) {
            return programas.get(0).getMesesAcceso();
        }
        return 12;
    }

    public void setMesesAcceso(int meses) {
        if (programas == null) {
            programas = new ArrayList<>();
        }
        if (programas.isEmpty()) {
            Programa p = new Programa();
            p.setNombre("Programa " + nombre);
            p.setMesesAcceso(meses);
            p.setCurso(this);
            programas.add(p);
        } else {
            programas.get(0).setMesesAcceso(meses);
        }
    }
}
