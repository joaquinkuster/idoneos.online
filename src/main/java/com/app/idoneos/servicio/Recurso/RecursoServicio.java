package com.app.idoneos.servicio.Recurso;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.modelo.Usuario;

/**
 * Servicio de los recursos de los temas (CU-28 a CU-31, "Materiales"): lo que el docente carga a un tema (presentaciones,
 * bibliografía, grabaciones y enlaces) y lo que el alumno ve. Cada material es un {@link TemaRecurso}: un recurso del
 * docente usado en un tema.
 */
public interface RecursoServicio {

    /**
     * Los datos que se piden al cargar o modificar un material; cada tipo usa los suyos.
     *
     * @param titulo          título (obligatorio).
     * @param descripcion     descripción (opcional).
     * @param oculto          si queda oculto para los alumnos.
     * @param autor           autor (presentación y bibliografía).
     * @param enlace          dirección web (enlace).
     * @param isbn            ISBN (bibliografía).
     * @param editorial       editorial (bibliografía, opcional).
     * @param paginas         cantidad de páginas (opcional).
     * @param fechaPublicacion fecha de publicación (bibliografía, opcional).
     * @param duracionMinutos duración en minutos (grabación, opcional).
     */
    record DatosRecurso(String titulo, String descripcion, boolean oculto, String autor, String enlace, String isbn,
            String editorial, Integer paginas, LocalDate fechaPublicacion, Integer duracionMinutos) {
    }

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca un material activo por su identificador.
     *
     * @param id el identificador del {@link TemaRecurso}.
     * @return el material, o vacío si no existe o está dado de baja.
     */
    Optional<TemaRecurso> buscarPorId(Integer id);

    /**
     * Busca los materiales de una participación docente.
     *
     * @param docenteCurso la participación.
     * @return los materiales activos.
     */
    List<TemaRecurso> buscarPorDocenteCurso(DocenteCurso docenteCurso);

    /**
     * Busca los materiales activos de un tema.
     *
     * @param tema el tema.
     * @return los materiales, en su orden.
     */
    List<TemaRecurso> buscarPorTema(Tema tema);

    /**
     * Busca materiales con filtros opcionales.
     *
     * @param idCurso         curso (opcional).
     * @param idTema          tema (opcional).
     * @param tipo            tipo ("Grabación", "PRESENTACION"...) (opcional).
     * @param titulo          parte del título (opcional).
     * @param generadoPorIa   si lo generó una IA (opcional).
     * @param baja            si está dado de baja (opcional).
     * @param soloDeDocente   si se indica, solo los de cursos en los que participa.
     * @return los materiales que cumplen.
     */
    List<TemaRecurso> buscarConFiltros(Integer idCurso, Integer idTema, String tipo, String titulo, Boolean generadoPorIa,
            Boolean baja, Docente soloDeDocente);

    /**
     * Indica si un usuario puede abrir un material: el administrador, un docente del curso o un alumno con acceso.
     *
     * @param material el material.
     * @param usuario  el usuario.
     * @return {@code true} si puede abrirlo.
     */
    boolean puedeAcceder(TemaRecurso material, Usuario usuario);

    /**
     * Busca la inscripción con la que un alumno accede a un material.
     *
     * @param material el material.
     * @param alumno   el alumno.
     * @return la inscripción, o vacío si no tiene acceso.
     */
    Optional<Inscripcion> buscarInscripcionConAcceso(TemaRecurso material, Alumno alumno);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-28: Carga un material a un tema.
     *
     * @param docente  el docente que lo carga (debe participar en el curso).
     * @param idTema   el tema.
     * @param tipo     el tipo de material.
     * @param datos    los datos del material.
     * @param archivo  el archivo (según el tipo).
     * @param terminos si el docente aceptó los términos y condiciones sobre el contenido.
     * @return el material cargado.
     * @throws IllegalArgumentException si algún dato no es válido.
     */
    TemaRecurso subirMaterial(Docente docente, Integer idTema, String tipo, DatosRecurso datos, MultipartFile archivo,
            boolean terminos);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-29: Modifica un material.
     *
     * @param id      el identificador del material.
     * @param docente el docente que lo modifica.
     * @param datos   los datos nuevos.
     * @param archivo el archivo nuevo (opcional).
     * @return el material modificado.
     * @throws IllegalArgumentException si algún dato no es válido.
     */
    TemaRecurso modificarMaterial(Integer id, Docente docente, DatosRecurso datos, MultipartFile archivo);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-30: Da de baja un material.
     *
     * @param id      el identificador del material.
     * @param docente el docente que lo da de baja, o {@code null} si es el administrador.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBajaMaterial(Integer id, Docente docente);

    /**
     * CU-30: Da de baja varios materiales a la vez, todos o ninguno.
     *
     * @param ids     los identificadores.
     * @param docente el docente, o {@code null} si es el administrador.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);
}
