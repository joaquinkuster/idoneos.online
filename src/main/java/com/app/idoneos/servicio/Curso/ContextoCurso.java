package com.app.idoneos.servicio.Curso;

import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Programa;

/**
 * Contexto con el que se muestra una pantalla del panel de un curso: quién la ve, sobre qué curso y programa trabaja y
 * a dónde vuelve. Lo resuelve {@link CursoServicio} y las plantillas lo reciben como atributos del modelo.
 *
 * @param curso         El curso abierto.
 * @param programa      El programa con el que se trabaja (puede ser {@code null} si el curso todavía no tiene).
 * @param rol           El rol con el que la persona entra al curso.
 * @param participacion La participación del docente en el curso ({@code null} si no entra como docente).
 * @param urlBase       La ruta de la página principal del curso para ese rol.
 * @param urlMisCursos  La ruta del listado de cursos para ese rol.
 */
public record ContextoCurso(Curso curso, Programa programa, Rol rol, DocenteCurso participacion,
        String urlBase, String urlMisCursos) {

    /** Rol con el que se entra al panel de un curso. */
    public enum Rol { ADMINISTRADOR, DOCENTE, ALUMNO }

    /**
     * Contexto del administrador: trabaja con el programa vigente del curso.
     *
     * @param curso El curso abierto.
     * @return El contexto del administrador.
     */
    public static ContextoCurso deAdministrador(Curso curso) {
        return new ContextoCurso(curso, curso.getProgramaVigente(), Rol.ADMINISTRADOR, null,
                "/curso/acceder/" + curso.getIdCurso(), "/curso/misCursos");
    }

    /**
     * Contexto del docente: trabaja con el programa que tiene elegido en su participación.
     *
     * @param participacion La participación del docente en el curso.
     * @return El contexto del docente.
     */
    public static ContextoCurso deDocente(DocenteCurso participacion) {
        Curso curso = participacion.getCurso();
        return new ContextoCurso(curso, participacion.getProgramaDeTrabajo(), Rol.DOCENTE, participacion,
                "/curso/acceder/" + curso.getIdCurso(), "/curso/buscar");
    }

    /**
     * Contexto del alumno: trabaja con el programa de la cohorte en la que está inscripto.
     *
     * @param inscripcion La inscripción del alumno.
     * @return El contexto del alumno.
     */
    public static ContextoCurso deAlumno(Inscripcion inscripcion) {
        return new ContextoCurso(inscripcion.getCurso(), inscripcion.getCohorte().getPrograma(), Rol.ALUMNO, null,
                "/inscripcion/acceder/" + inscripcion.getIdInscripcion(), "/inscripcion/misCursos");
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean esDocente() {
        return rol == Rol.DOCENTE;
    }

    public boolean esAlumno() {
        return rol == Rol.ALUMNO;
    }
}
