package com.app.idoneos.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cohorte próxima que un alumno marcó con «Recordarme»: cuando falten los días de anticipación que indica el parámetro para
 * que abra su inscripción, el sistema le crea una notificación de tipo «Recordatorio de cohorte». Quitar el recordatorio es una
 * baja lógica; si el alumno lo vuelve a marcar, se reactiva el mismo registro.
 */
@Entity
@Table(name = "RecordatorioCohorte", uniqueConstraints = @UniqueConstraint(name = "uk_recordatorio_cohorte", columnNames = {"idAlumno", "idCohorte"}))
@Getter
@Setter
@NoArgsConstructor
public class RecordatorioCohorte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRecordatorioCohorte")
    private int idRecordatorioCohorte;

    @ManyToOne
    @JoinColumn(name = "idAlumno", nullable = false)
    private Alumno alumno;

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
        return "RecordatorioCohorte #" + idRecordatorioCohorte;
    }
}
