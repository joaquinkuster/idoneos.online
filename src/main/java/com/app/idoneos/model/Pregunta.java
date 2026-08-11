package com.app.idoneos.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pregunta")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "texto", nullable = false, length = 150)
    private String texto;

    @Column(name = "es_opcion_multiple", nullable = false)
    private boolean esOpcionMultiple = true;

    @Column(name = "baja", nullable = false)
    private boolean baja = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL)
    private List<OpcionRespuesta> opcionesRespuesta = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isEsOpcionMultiple() { return esOpcionMultiple; }
    public boolean getEsOpcionMultiple() { return esOpcionMultiple; }
    public void setEsOpcionMultiple(boolean esOpcionMultiple) { this.esOpcionMultiple = esOpcionMultiple; }

    public boolean isBaja() { return baja; }
    public boolean getBaja() { return baja; }
    public void setBaja(boolean baja) { this.baja = baja; }

    public Pool getPool() { return pool; }
    public void setPool(Pool pool) { this.pool = pool; }

    public List<OpcionRespuesta> getOpcionesRespuesta() { return opcionesRespuesta; }
    public void setOpcionesRespuesta(List<OpcionRespuesta> opcionesRespuesta) { this.opcionesRespuesta = opcionesRespuesta; }


    public Pregunta(String texto, boolean esOpcionMultiple, Pool pool) {
        this.texto = texto;
        this.esOpcionMultiple = esOpcionMultiple;
        this.pool = pool;
    }

}
