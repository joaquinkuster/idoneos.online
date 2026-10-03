package com.app.idoneos.servicio.Curso;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Cohorte;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;

/**
 * Servicio para gestionar las operaciones relacionadas con el curso.
 */
public interface CursoServicio {

    /**
     * Busca los registros vigentes de curso asociados a nivel.
     *
     * @param nivel el registro de Nivel asociado
     * @return una lista de Curso asociados al Nivel indicado
     */
    List<Curso> buscarPorNivel(Nivel nivel);

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    List<Curso> buscarPorCategoria(Categoria categoria);

    /**
     * Busca el curso por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Curso> buscarPorNombre(String nombre);

    /**
     * Busca cursos aplicando filtros opcionales. Incluye los cursos dados de baja, que se ordenan al final
     * (o al principio, si se indica).
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @param bajasPrimero si es {@code true}, los cursos dados de baja se listan primero
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que ese docente participa
     * @return una lista de cursos que cumplen los criterios
     */
    List<Curso> buscarConFiltros(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad, boolean bajasPrimero, Docente soloDeDocente);

    /**
     * Busca los cursos del catálogo público: cursos activos con al menos una cohorte con inscripción abierta.
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @return una lista de cursos con cohortes abiertas
     */
    List<Curso> buscarEnCatalogo(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad);

    /**
     * Busca las cohortes con inscripción abierta de un curso.
     *
     * @param curso el curso a consultar
     * @return una lista de cohortes abiertas, de la que abre antes a la que abre después
     */
    List<Cohorte> buscarCohortesAbiertas(Curso curso);

    /**
     * Verifica si un curso tiene inscripciones activas asociadas.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos una inscripción activa
     */
    boolean tieneInscripcionesActivas(Curso curso);

    /**
     * Verifica si un curso tiene programas o unidades activos asociados.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos un programa o una unidad activos
     */
    boolean tieneProgramasOUnidadesActivas(Curso curso);

    /**
     * Registra un curso con sus modalidades de dictado y su equipo docente.
     *
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param imagen la ruta de la imagen de portada (opcional)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idDocenteAyudante el identificador del docente ayudante (opcional)
     * @return el curso registrado
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Curso registrarCurso(String nombre, String descripcion, Float precio, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            Integer idDocenteAyudante);

    /**
     * Modifica un curso activo. Si tiene inscripciones activas, solo pueden modificarse el precio,
     * el equipo docente y la imagen de portada.
     *
     * @param idCurso el identificador del curso
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param imagen la ruta de la imagen de portada (opcional, si es nula se conserva la actual)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idDocenteAyudante el identificador del docente ayudante (opcional)
     * @return el curso modificado
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Curso modificarCurso(Integer idCurso, String nombre, String descripcion, Float precio, String imagen,
            Integer idCategoria, Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades,
            Integer idDocenteTitular, Integer idDocenteAyudante);

    /**
     * Da de baja un curso activo que no tenga programas ni unidades activas asociadas.
     *
     * @param idCurso el identificador del curso
     * @throws IllegalArgumentException si el curso no está activo o tiene programas o unidades activas
     */
    void darDeBajaCurso(Integer idCurso);

    /**
     * Busca las inscripciones activas (no dadas de baja) de un curso, a través de sus programas y cohortes.
     *
     * @param curso el curso a consultar
     * @return una lista de inscripciones activas del curso
     */
    List<Inscripcion> buscarInscripcionesActivas(Curso curso);
}
