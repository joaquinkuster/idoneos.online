package com.app.idoneos.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entidad que representa un recurso usado en un tema, con el docente que lo incorporó, su orden y si está oculto para los alumnos. */
@Entity
@Table(name = "TemaRecurso")
@IdClass(TemaRecurso.Clave.class)
@Getter
@Setter
@NoArgsConstructor
public class TemaRecurso {
    /** Clave primaria compuesta del recurso de un tema. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Clave implements Serializable {
        private int tema;
        private int recurso;

        /** Arma la clave desde su texto «tema-recurso», que es como viaja en las direcciones y en los formularios. */
        public static Clave valueOf(String texto) {
            String[] partes = texto == null ? new String[0] : texto.trim().split("-");
            if (partes.length != 2) {
                throw new IllegalArgumentException("Error! La clave del recurso del tema no es válida.");
            }
            try {
                return new Clave(Integer.parseInt(partes[0]), Integer.parseInt(partes[1]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error! La clave del recurso del tema no es válida.", e);
            }
        }

        /** Devuelve la clave como texto «tema-recurso». */
        @Override
        public String toString() {
            return tema + "-" + recurso;
        }
    }

    /** Tema de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /** Recurso de este registro. */
    @Id
    @ManyToOne
    @JoinColumn(name = "idRecurso", nullable = false)
    private Recurso recurso;

    /** Relación con IntegranteEquipo: la participación del docente que incorporó el recurso al tema. */
    @ManyToOne
    @JoinColumn(name = "idIntegranteEquipo", nullable = false)
    private IntegranteEquipo integranteEquipo;

    /** Indica si está oculto para los alumnos. */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

    /** Posición del recurso entre los del tema. */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /** Fecha y hora de creación. */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /** Fecha y hora de la última modificación. */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /** Baja lógica. */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /** Crea el recurso de un tema con los datos básicos. */
    public TemaRecurso(Tema tema, Recurso recurso, IntegranteEquipo integranteEquipo, int numeroOrden) {
        this.tema = tema;
        this.recurso = recurso;
        this.integranteEquipo = integranteEquipo;
        this.numeroOrden = numeroOrden;
    }

    /** Da de baja el registro (baja lógica). */
    public void marcarInactivo() {
        baja = true;
    }

    /** Verifica si el registro está dado de baja. */
    public boolean esInactivo() {
        return baja;
    }

    /** Devuelve la clave primaria del registro: el tema y el recurso. */
    public Clave getClave() {
        return new Clave(tema.getIdTema(), recurso.getIdRecurso());
    }

    /** Devuelve una representación en forma de cadena. */
    @Override
    public String toString() {
        return recurso.getTitulo();
    }

    /** Indica si los alumnos lo ven: no está dado de baja ni oculto, y su recurso tampoco. */
    public boolean estaPublicado() {
        return !baja && !oculto && !recurso.getBaja();
    }

    /** Obtiene el curso del tema. */
    public Curso getCurso() {
        return tema.getCurso();
    }

    /** Obtiene el título del recurso. */
    public String getTitulo() {
        return recurso.getTitulo();
    }

    /** Obtiene la modalidad del curso a la que corresponde el recurso. */
    public String getModalidadAsociada() {
        return recurso.getModalidadAsociada();
    }

    /** Describe en plural los recursos de una modalidad (para mensajes). */
    public static String describirPorModalidad(String modalidad) {
        return switch (modalidad) {
            case Modalidad.EN_VIVO -> "grabaciones de clases en vivo";
            case Modalidad.CLON_IA -> "videos con clon de IA";
            default -> "grabaciones";
        };
    }

    /** Obtiene el tipo del recurso ("Presentación", "Bibliografía", "Grabación", "Enlace"...). */
    public String getTipo() {
        return recurso.getTipo();
    }

    /** Obtiene el autor del recurso, si lo tiene. */
    public String getAutor() {
        return recurso.getAutor();
    }

    /** Indica si el recurso lo generó una inteligencia artificial. */
    public boolean getGeneradoPorIa() {
        return recurso.getGeneradoPorIa();
    }

    /** Obtiene el estado de la generación con inteligencia artificial, o nulo si lo cargó el docente a mano. */
    public String getEstadoGeneracion() {
        return recurso.getEstadoGeneracion();
    }

    /** Obtiene el motivo por el que falló la generación con inteligencia artificial, si falló. */
    public String getDetalleErrorGeneracion() {
        return recurso.getDetalleErrorGeneracion();
    }

    /** Indica si ya se puede usar: lo cargó el docente a mano o la inteligencia artificial terminó de generarlo. */
    public boolean getDisponible() {
        return recurso.getDisponible();
    }

    /** Obtiene la ruta del archivo del recurso, o su dirección si es un enlace. */
    public String getRutaArchivo() {
        return recurso.getRutaArchivo();
    }

    /** Indica si el recurso es un enlace externo. */
    public boolean esEnlaceExterno() {
        return recurso.esEnlaceExterno();
    }

    /** Indica si el recurso tiene un archivo guardado en la plataforma. */
    public boolean tieneArchivo() {
        return recurso.tieneArchivo();
    }

    /** Obtiene la extensión del archivo del recurso. */
    public String getExtension() {
        return recurso.getExtension();
    }

    /** Obtiene cómo se muestra el recurso dentro de la plataforma. */
    public String getFormatoDeVista() {
        if (recurso.esEnlaceExterno()) {
            return "enlace";
        }
        if (!recurso.tieneArchivo()) {
            return "empty";
        }
        return switch (recurso.getExtension()) {
            case "mp4", "webm" -> "video";
            case "pdf" -> "pdf";
            default -> "empty";
        };
    }
}
