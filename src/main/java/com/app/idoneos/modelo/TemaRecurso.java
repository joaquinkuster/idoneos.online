package com.app.idoneos.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Un recurso usado en un tema, con el docente que lo incorporó, su orden y si está oculto para los alumnos. Es lo que el
 * tema muestra como su material.
 */
@Entity
@Table(name = "TemaRecurso", uniqueConstraints = @UniqueConstraint(name = "uk_tema_recurso", columnNames = {"idTema", "idRecurso"}))
@Getter
@Setter
@NoArgsConstructor
public class TemaRecurso {

    /**
     * Identificador único.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTemaRecurso")
    private int idTemaRecurso;

    /**
     * Relación con Tema.
     */
    @ManyToOne
    @JoinColumn(name = "idTema", nullable = false)
    private Tema tema;

    /**
     * Relación con Recurso.
     */
    @ManyToOne
    @JoinColumn(name = "idRecurso", nullable = false)
    private Recurso recurso;

    /**
     * Relación con DocenteCurso: la participación del docente que incorporó el recurso al tema.
     */
    @ManyToOne
    @JoinColumn(name = "idDocenteCurso", nullable = false)
    private DocenteCurso docenteCurso;

    /**
     * Indica si está oculto para los alumnos.
     */
    @Column(name = "oculto", nullable = false)
    private Boolean oculto = false;

    /**
     * Posición del recurso entre los del tema.
     */
    @Column(name = "numeroOrden", nullable = false)
    private int numeroOrden;

    /**
     * Fecha y hora de creación.
     */
    @Column(name = "fechaCreacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    /**
     * Fecha y hora de la última modificación. Es opcional.
     */
    @Column(name = "ultimaModificacion", nullable = true)
    private LocalDateTime ultimaModificacion;

    /**
     * Baja lógica.
     */
    @Column(name = "baja", nullable = false)
    private Boolean baja = false;

    /**
     * Constructor para crear el recurso de un tema con los datos básicos.
     *
     * @param tema         el tema.
     * @param recurso      el recurso.
     * @param docenteCurso la participación del docente que lo incorpora.
     * @param numeroOrden  la posición entre los recursos del tema.
     */
    public TemaRecurso(Tema tema, Recurso recurso, DocenteCurso docenteCurso, int numeroOrden) {
        this.tema = tema;
        this.recurso = recurso;
        this.docenteCurso = docenteCurso;
        this.numeroOrden = numeroOrden;
    }

    /**
     * Da de baja el registro (baja lógica).
     */
    public void marcarInactivo() {
        baja = true;
    }

    /**
     * Verifica si el registro está dado de baja.
     *
     * @return true si está dado de baja.
     */
    public boolean esInactivo() {
        return baja;
    }

    /**
     * Devuelve una representación en forma de cadena.
     *
     * @return El título del recurso.
     */
    @Override
    public String toString() {
        return recurso.getTitulo();
    }

    /**
     * Indica si los alumnos lo ven: no está dado de baja ni oculto, y su recurso tampoco.
     *
     * @return true si está publicado.
     */
    public boolean estaPublicado() {
        return !baja && !oculto && !recurso.getBaja();
    }

    /**
     * Obtiene el curso del tema.
     *
     * @return El curso.
     */
    public Curso getCurso() {
        return tema.getCurso();
    }

    /**
     * Obtiene el título del recurso.
     *
     * @return El título.
     */
    public String getTitulo() {
        return recurso.getTitulo();
    }

    /**
     * Obtiene la modalidad del curso a la que corresponde el recurso.
     *
     * @return La modalidad, o {@code null} si no corresponde a ninguna.
     * @see Recurso#getModalidadAsociada()
     */
    public String getModalidadAsociada() {
        return recurso.getModalidadAsociada();
    }

    /**
     * Describe en plural los recursos de una modalidad (para mensajes).
     *
     * @param modalidad el nombre de la modalidad.
     * @return Por ejemplo "grabaciones de clases en vivo".
     */
    public static String describirPorModalidad(String modalidad) {
        return switch (modalidad) {
            case Modalidad.EN_VIVO -> "grabaciones de clases en vivo";
            case Modalidad.CLON_IA -> "videos con clon de IA";
            default -> "grabaciones";
        };
    }

    /**
     * Obtiene el tipo del recurso ("Presentación", "Bibliografía", "Grabación", "Enlace"...).
     *
     * @return El tipo.
     */
    public String getTipo() {
        return recurso.getTipo();
    }

    /**
     * Obtiene el autor del recurso, si lo tiene.
     *
     * @return El autor, o {@code null}.
     */
    public String getAutor() {
        return recurso.getAutor();
    }

    /**
     * Indica si el recurso lo generó una inteligencia artificial.
     *
     * @return true si lo generó.
     */
    public boolean getGeneradoPorIa() {
        return recurso.getGeneradoPorIa();
    }

    /**
     * Obtiene la ruta del archivo del recurso, o su dirección si es un enlace.
     *
     * @return La ruta o la dirección.
     */
    public String getRutaArchivo() {
        return recurso.getRutaArchivo();
    }

    /**
     * Indica si el recurso es un enlace externo.
     *
     * @return true si lo es.
     */
    public boolean esEnlaceExterno() {
        return recurso.esEnlaceExterno();
    }

    /**
     * Indica si el recurso tiene un archivo guardado en la plataforma.
     *
     * @return true si lo tiene.
     */
    public boolean tieneArchivo() {
        return recurso.tieneArchivo();
    }

    /**
     * Obtiene la extensión del archivo del recurso.
     *
     * @return La extensión en minúsculas.
     */
    public String getExtension() {
        return recurso.getExtension();
    }

    /**
     * Obtiene cómo se muestra el recurso dentro de la plataforma.
     *
     * @return "enlace", "video", "pdf" o "vacio" si no se puede mostrar.
     */
    public String getFormatoDeVista() {
        if (recurso.esEnlaceExterno()) {
            return "enlace";
        }
        if (!recurso.tieneArchivo()) {
            return "vacio";
        }
        return switch (recurso.getExtension()) {
            case "mp4", "webm" -> "video";
            case "pdf" -> "pdf";
            default -> "vacio";
        };
    }
}
