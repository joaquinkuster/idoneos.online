package com.app.idoneos.modelo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Persona que participa de un curso, tal como se muestra en la lista de participantes (CU-25): un docente del equipo
 * (titular o ayudante) o un alumno inscripto. No es una entidad: se arma a partir de los docentes y las inscripciones.
 */
@Getter
@AllArgsConstructor
public class Participante {

    /** Rol de un docente titular del curso. */
    public static final String DOCENTE_TITULAR = "Docente titular";

    /** Rol de un docente ayudante del curso. */
    public static final String DOCENTE_AYUDANTE = "Docente ayudante";

    /** Rol de un alumno inscripto. */
    public static final String ALUMNO = "Alumno";

    /** El usuario que participa (de él salen el nombre, el apellido y la foto de perfil). */
    private final Usuario usuario;

    /** El rol en el curso: "Docente titular", "Docente ayudante" o "Alumno". */
    private final String rol;

    /** La cohorte del alumno (para los docentes es {@code null}: enseñan en todas). */
    private final Cohorte cohorte;

    /**
     * Obtiene el nombre del participante.
     *
     * @return El nombre.
     */
    public String getNombre() {
        return usuario.getNombre();
    }

    /**
     * Obtiene el apellido del participante.
     *
     * @return El apellido.
     */
    public String getApellido() {
        return usuario.getApellido();
    }

    /**
     * Indica si el participante es del equipo docente.
     *
     * @return {@code true} si es docente titular o ayudante.
     */
    public boolean esDocente() {
        return !ALUMNO.equals(rol);
    }
}
