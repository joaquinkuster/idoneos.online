package com.app.idoneos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una cohorte próxima que un alumno marcó con Recordarme. */
@Entity
@Table(name = "RecordatorioCohorte")
@IdClass(RecordatorioCohorte.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class RecordatorioCohorte {
    /** Clave primaria compuesta del recordatorio de cohorte. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int cohorte;
        private int alumno;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

    @Id
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    public RecordatorioCohorte(Alumno alumno, Cohorte cohorte) {
        this.alumno = alumno;
        this.cohorte = cohorte;
    }

    /** Marca el recordatorio como quitado (baja lógica). */
    public void marcarInactivo() {
        baja = true;
    }

    /** Vuelve a activar un recordatorio que el alumno había quitado. */
    public void reactivar() {
        baja = false;
    }

    /** Indica si el recordatorio fue quitado. */
    public boolean esInactivo() {
        return baja;
    }

    @Override
    public String toString() {
        return "RecordatorioCohorte #" + cohorte.getIdCohorte() + "-" + alumno.getIdAlumno();
    }
}
