package com.app.idoneos.servicio.Curso;

import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Cohorte;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;

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
     * Busca cursos aplicando filtros opcionales. Incluye los cursos dados de baja, que se ordenan siempre al final.
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @param orden el orden de los resultados: "nombre" (A–Z, por defecto) o "recientes" (más nuevos primero)
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que ese docente participa
     * @return una lista de cursos que cumplen los criterios
     */
    List<Curso> buscarConFiltros(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad, String orden, Docente soloDeDocente);

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
     * Busca los cursos destacados del catálogo: los que tienen cohortes con inscripción abierta, de los que más
     * inscripciones históricas acumularon (incluidas las bajas y las de cohortes ya finalizadas) a los que menos.
     * A igual cantidad va primero el curso más reciente.
     *
     * @param cantidad la cantidad máxima de cursos a devolver
     * @return los cursos destacados, del más al menos inscripto
     */
    List<Curso> buscarDestacados(int cantidad);

    /**
     * Cuenta las inscripciones históricas de un curso: todas las de todas las cohortes de todos sus programas.
     *
     * @param curso el curso a consultar
     * @return la cantidad de inscripciones, activas o no
     */
    long contarInscripcionesHistoricas(Curso curso);

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
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso registrado
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Curso registrarCurso(String nombre, String descripcion, Float precio, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            List<Integer> idsDocentesAyudantes);

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
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso modificado
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Curso modificarCurso(Integer idCurso, String nombre, String descripcion, Float precio, String imagen,
            Integer idCategoria, Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades,
            Integer idDocenteTitular, List<Integer> idsDocentesAyudantes);

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

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja ninguno.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    void darDeBajaVarios(java.util.List<Integer> ids);

    /**
     * Verifica que el docente pueda acceder al curso: el curso debe estar activo, el docente debe participar en
     * él. No hace falta que el curso tenga programas (CU-27): desde la página del curso el docente registra el primero.
     *
     * @param idCurso El identificador del curso.
     * @param docente El docente que solicita el acceso.
     * @return La participación del docente en el curso, de la que se obtiene su programa y cohorte de trabajo.
     * @throws IllegalArgumentException Si el docente no puede acceder, con el motivo.
     */
    ParticipacionDocente validarAccesoDocente(int idCurso, Docente docente);

    /**
     * Busca un curso activo y, si no existe o está dado de baja, informa el motivo.
     *
     * @param idCurso El identificador del curso.
     * @return El curso activo.
     * @throws IllegalArgumentException Si el curso no se encuentra activo.
     */
    Curso buscarActivoPorId(Integer idCurso);

    /**
     * Reúne, para cada curso de una lista, sus inscripciones y programas activos y si tiene inscripción abierta.
     *
     * @param cursos Los cursos del listado.
     * @return El resumen de los cursos.
     */
    ResumenCursos resumir(List<Curso> cursos);

    /**
     * Resuelve con qué rol y programa entra una persona al panel de un curso que se abre con su identificador: el
     * administrador trabaja con el programa vigente y el docente, con el que tiene elegido.
     *
     * @param idCurso El identificador del curso (obligatorio).
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws IllegalArgumentException Si no se indicó el curso, no está activo o el docente no participa.
     */
    ContextoCurso resolverContextoDelPanel(Integer idCurso, Usuario usuario);

    /**
     * Resuelve el contexto de una pantalla que se abre desde una unidad (foro, glosario, material): el administrador y
     * los docentes del curso entran como tales; el alumno, por su inscripción con acceso a la unidad. Quien la llame
     * debe haber comprobado antes que la persona puede ver el contenido.
     *
     * @param unidad  La unidad de la pantalla.
     * @param usuario El usuario autenticado.
     * @return El contexto, o vacío si no es de la gestión del curso ni tiene una inscripción con acceso.
     */
    Optional<ContextoCurso> resolverContextoDeLaUnidad(Unidad unidad, Usuario usuario);

    /**
     * Asigna la imagen de un curso activo (el archivo ya se guardó en la carpeta del curso).
     *
     * @param idCurso El identificador del curso.
     * @param imagen  El nombre del archivo de la imagen dentro de la carpeta del curso.
     * @return El curso actualizado.
     * @throws IllegalArgumentException Si el curso no está activo.
     */
    Curso actualizarImagen(Integer idCurso, String imagen);

    /**
     * Lleva las imágenes de los cursos a la estructura {@code curso{id}/imagen.{ext}}: copia las que estaban en la
     * carpeta anterior o entre las imágenes de ejemplo de la aplicación y actualiza el nombre guardado.
     */
    void migrarImagenes();
}
