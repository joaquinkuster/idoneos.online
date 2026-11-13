package com.app.idoneos.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.Check;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa una clase dictada en vivo para una cohorte, con transmisión por RTMP. */
@Entity
@Table(name = "ClaseEnVivo")
@Check(name = "ck_clase_en_vivo_titulo", constraints = "trim(titulo) <> ''")
@Check(name = "ck_clase_en_vivo_horario", constraints = "fecha_hora_cierre > fecha_hora_inicio")
@Getter
@Setter
@NoArgsConstructor
public class ClaseEnVivo {

    /** Identificador único de la clase en vivo. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClaseEnVivo")
    private int idClaseEnVivo;

    /** Docente curso de este registro. */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

    /** Estado clase en vivo de este registro. */
    @ManyToOne
    @JoinColumn(name = "idEstadoClaseEnVivo", nullable = false)
    private EstadoClaseEnVivo estadoClaseEnVivo;

    /** Relación con Tema: el tema que se dicta en la clase. */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Relación con Grabacion: la grabación de la clase, cuando ya está disponible. */
    @ManyToOne
    @JoinColumn(name = "idGrabacion", nullable = true)
    private Grabacion grabacion;

    /** Cohorte de este registro. */
    @ManyToOne
    @JoinColumn(name = "idCohorte", nullable = false)
    private Cohorte cohorte;

    /** Título de la clase. */
    @Column(name = "titulo", nullable = false, length = 50)
    private String titulo;

    /** Fecha y hora en que empieza la clase. */
    @Column(name = "fechaHoraInicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    /** Fecha y hora en que termina la clase. */
    @Column(name = "fechaHoraCierre", nullable = false)
    private LocalDateTime fechaHoraCierre;

    /** Fecha hasta la que se puede ver la grabación de la clase. */
    @Column(name = "fechaVencimientoGrabacion", nullable = true)
    private java.time.LocalDate fechaVencimientoGrabacion;

    /** URL del servidor RTMP para transmitir la clase. */
    @Column(name = "urlRtmp", nullable = true, length = 255)
    private String urlRtmp;

    /** Clave de transmisión de la clase. */
    @Column(name = "claveStream", nullable = true, length = 100, unique = true)
    private String claveStream;

    /** Fecha y hora de creación del registro. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación del registro. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Indica si el registro fue dado de baja (baja lógica). */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea la clase en vivo con los datos básicos. */
    public ClaseEnVivo(DocenteCurso docenteCurso, EstadoClaseEnVivo estadoClaseEnVivo, Tema tema, Cohorte cohorte, String titulo, LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraCierre) {
        this.docenteCurso = docenteCurso;
        this.estadoClaseEnVivo = estadoClaseEnVivo;
        this.tema = tema;
        this.cohorte = cohorte;
        this.titulo = titulo;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraCierre = fechaHoraCierre;
    }

    /** Marca el registro como dado de baja (baja lógica), estableciendo el atributo 'baja' a true. */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Indica si la clase todavía no se transmitió ni se canceló. */
    public boolean esProgramada() {
        return !baja && EstadoClaseEnVivo.PROGRAMADA.equals(estadoClaseEnVivo.getNombre());
    }

    /** Indica si la clase se está transmitiendo. */
    public boolean esEnVivo() {
        return !baja && EstadoClaseEnVivo.EN_VIVO.equals(estadoClaseEnVivo.getNombre());
    }

    /** Indica si la clase ya se transmitió. */
    public boolean esFinalizada() {
        return !baja && EstadoClaseEnVivo.FINALIZADA.equals(estadoClaseEnVivo.getNombre());
    }

    /** Indica si la clase se canceló antes de transmitirse. */
    public boolean esCancelada() {
        return EstadoClaseEnVivo.CANCELADA.equals(estadoClaseEnVivo.getNombre());
    }

    /** Indica si la clase ya puede iniciarse: llegó su horario, con la anticipación indicada, y todavía no se transmitió. */
    public boolean puedeIniciarse(LocalDateTime ahora, int anticipacionMinutos) {
        return esProgramada() && !ahora.isBefore(fechaHoraInicio.minusMinutes(anticipacionMinutos));
    }

    /** Indica si la grabación de la clase sigue disponible para los alumnos. */
    public boolean tieneGrabacionDisponible() {
        return grabacion != null && !grabacion.getRecurso().esInactivo();
    }

    /** Indica si la clase terminó hace poco y su grabación todavía se está procesando para publicarse. */
    public boolean estaProcesandoGrabacion() {
        return esFinalizada() && grabacion == null && ultimaModificacion != null
                && ultimaModificacion.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    /** Obtiene la fecha y hora de inicio con el día de la semana, por ejemplo Sábado 14/11 19:00. */
    public String getFechaCorta() {
        String texto = fechaHoraInicio.format(java.time.format.DateTimeFormatter.ofPattern("EEEE dd/MM HH:mm",
                java.util.Locale.forLanguageTag("es-AR")));
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** Obtiene el día y el horario para los avisos: Hoy · 19:00 - 20:30 h si es hoy, o por ejemplo Sábado 14/11 · 19:00 h. */
    public String getFechaConHora() {
        if (fechaHoraInicio.toLocalDate().equals(java.time.LocalDate.now())) {
            return "Hoy · " + getHorario();
        }
        String corta = getFechaCorta();
        int corte = corta.lastIndexOf(' ');
        return corta.substring(0, corte) + " · " + corta.substring(corte + 1) + " h";
    }

    /** Obtiene el horario de la clase, por ejemplo 19:00 - 20:30 h. */
    public String getHorario() {
        java.time.format.DateTimeFormatter hora = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        return fechaHoraInicio.format(hora) + " - " + fechaHoraCierre.format(hora) + " h";
    }

    /** Obtiene el rango de fechas de la clase, por ejemplo 14/11/2026 19:00 - 20:30 h, o con las dos fechas si termina otro día. */
    public String getRango() {
        java.time.format.DateTimeFormatter completa = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fechaHoraInicio.format(completa) + " - "
                + (fechaHoraCierre.toLocalDate().equals(fechaHoraInicio.toLocalDate())
                        ? fechaHoraCierre.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                        : fechaHoraCierre.format(completa))
                + " h";
    }

    /** Obtiene la publicación vigente de la grabación en el tema de la clase. */
    public TemaRecurso getPublicacionDeGrabacion() {
        if (grabacion == null) {
            return null;
        }
        return grabacion.getRecurso().getTemasRecurso().stream()
                .filter(publicacion -> !publicacion.getBaja() && publicacion.getTema().getIdTema() == tema.getIdTema())
                .findFirst().orElse(null);
    }

    /** Obtiene la duración de la clase en minutos, a partir de su inicio y su cierre. */
    public int getDuracion() {
        return (int) java.time.Duration.between(fechaHoraInicio, fechaHoraCierre).toMinutes();
    }

    /** Indica si la clase la registró el docente indicado. */
    public boolean esDeDocente(Docente docente) {
        return docente != null && docenteCurso.getDocente().getIdDocente() == docente.getIdDocente();
    }

    /** Devuelve una representación en forma de cadena de la clase en vivo. */
    @Override
    public String toString() {
        return titulo;
    }
}
