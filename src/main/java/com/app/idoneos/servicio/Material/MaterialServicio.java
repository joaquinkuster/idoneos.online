package com.app.idoneos.servicio.Material;

import java.util.List;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Docente;
import org.springframework.web.multipart.MultipartFile;
import com.app.idoneos.modelo.Usuario;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio para gestionar las operaciones relacionadas con el material.
 */
public interface MaterialServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de material asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de Material asociados al DocenteCurso indicado
     */
    List<Material> buscarPorDocenteCurso(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    List<Material> buscarPorTipoMaterial(TipoMaterial tipoMaterial);

    /**
     * Busca los registros vigentes de material asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Material asociados al Unidad indicado
     */
    List<Material> buscarPorUnidad(Unidad unidad);

    /**
     * CU-28: Busca el material (incluido el no publicado) aplicando filtros opcionales.
     *
     * @param idCurso         identificador del curso (opcional)
     * @param idUnidad        identificador de la unidad (opcional)
     * @param idTipoMaterial  identificador del tipo de material (opcional)
     * @param titulo          parte del título (opcional)
     * @param generadoPorIa   si fue generado con IA (opcional)
     * @param baja            {@code true} solo dados de baja, {@code false} solo activos, {@code null} todos (opcional)
     * @param soloDeDocente   si se indica, solo el material de los cursos en los que participa ese docente
     * @return los materiales que cumplen los criterios; los dados de baja van siempre al final
     */
    List<Material> buscarConFiltros(Integer idCurso, Integer idUnidad, Integer idTipoMaterial, String titulo,
            Boolean generadoPorIa, Boolean baja, Docente soloDeDocente);

    /**
     * Indica si un usuario puede abrir el archivo (o enlace) de un material: el administrador, el docente que
     * participa en el curso y el alumno con acceso vigente al curso, siempre que el material esté publicado y su
     * unidad habilitada según su avance.
     *
     * @param material el material a abrir
     * @param usuario  el usuario que lo solicita
     * @return {@code true} si puede abrirlo
     */
    boolean puedeAcceder(Material material, Usuario usuario);

    /**
     * Busca la inscripción con la que un alumno puede abrir un material: debe tener acceso vigente al curso, el
     * material debe estar publicado y su unidad habilitada según el avance del alumno.
     *
     * @param material el material a abrir
     * @param alumno   el alumno que lo solicita
     * @return la inscripción que le da acceso, o vacío si no puede abrirlo
     */
    Optional<Inscripcion> buscarInscripcionConAcceso(Material material, Alumno alumno);

    // ---------------------------------------------------------------- Creaciones

    /**
     * CU-29: Sube un material (grabación, bibliografía o presentación) a una unidad activa.
     *
     * @param docente         el docente que lo sube (debe participar en el curso)
     * @param idUnidad        identificador de la unidad
     * @param idTipoMaterial  identificador del tipo de material
     * @param titulo          título del material
     * @param oculto          si queda oculto para los alumnos
     * @param autor           autor (opcional)
     * @param enlace          enlace externo (solo bibliografía, alternativo al archivo)
     * @param archivo         archivo del material
     * @return el material registrado
     * @throws IllegalArgumentException si algún dato no es válido, con el motivo
     */
    Material subirMaterial(Docente docente, Integer idUnidad, Integer idTipoMaterial, String titulo, boolean oculto,
            String autor, String enlace, MultipartFile archivo);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * CU-30: Modifica el título, el autor, el archivo o enlace y la visibilidad de un material activo.
     *
     * @param idMaterial identificador del material
     * @param docente    el docente que lo modifica (debe participar en el curso)
     * @param titulo     título del material
     * @param oculto     si queda oculto para los alumnos
     * @param autor      autor (opcional)
     * @param enlace     enlace externo (solo bibliografía, alternativo al archivo; opcional)
     * @param archivo    archivo nuevo (opcional: si no se envía se conserva el actual)
     * @return el material modificado
     * @throws IllegalArgumentException si algún dato no es válido, con el motivo
     */
    Material modificarMaterial(Integer idMaterial, Docente docente, String titulo, boolean oculto, String autor,
            String enlace, MultipartFile archivo);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-31: Da de baja un material activo (baja lógica: se conserva el archivo).
     *
     * @param idMaterial identificador del material
     * @param docente    el docente que lo da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si el material no está activo o el docente no participa en el curso
     */
    void darDeBajaMaterial(Integer idMaterial, Docente docente);

    /**
     * Da de baja varios materiales activos. Es todo o nada: si alguno no puede darse de baja, no se da de baja ninguno.
     *
     * @param ids      identificadores de los materiales
     * @param docente  el docente que los da de baja, o {@code null} si lo hace el administrador
     * @throws IllegalArgumentException si no se seleccionó ninguno o alguno no puede darse de baja
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);
}
