package com.app.idoneos.service.curso;

import com.app.idoneos.service.CrudService;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import java.util.Map;
import java.util.Set;

import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Rol;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.Usuario;
import java.util.List;
import java.util.Optional;

/** Servicio del curso. */
public interface CursoService extends CrudService<Curso> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    List<Curso> buscarPorCategoria(Categoria categoria);

    /**
     * Busca el curso por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Curso> buscarPorNombre(String nombre);

    /**
     * Busca cursos aplicando filtros opcionales.
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @param orden el orden de los resultados: "nombre" (A,Z, por defecto) o "recientes" (más nuevos primero)
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
     * Busca los cursos destacados del catálogo: los que tienen cohortes con inscripción abierta.
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
     * Busca las cohortes próximas de un curso: activas, cuya inscripción todavía no abrió.
     *
     * @param curso el curso a consultar
     * @return una lista de cohortes próximas, de la que abre antes a la que abre después
     */
    List<Cohorte> buscarCohortesProximas(Curso curso);

    /**
     * Verifica si un curso tiene inscripciones activas asociadas.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos una inscripción activa
     */
    boolean tieneInscripcionesActivas(Curso curso);

    /**
     * Busca las inscripciones activas (no dadas de baja) de un curso, a través de sus programas y cohortes.
     *
     * @param curso el curso a consultar
     * @return una lista de inscripciones activas del curso
     */
    List<Inscripcion> buscarInscripcionesActivas(Curso curso);

    /**
     * Verifica que el docente pueda acceder al curso: el curso debe estar activo.
     *
     * @param idCurso El identificador del curso.
     * @param docente El docente que solicita el acceso.
     * @return La participación del docente en el curso, de la que se obtiene su programa y cohorte de trabajo.
     * @throws IllegalArgumentException Si el docente no puede acceder, con el motivo.
     */
    DocenteCurso validarAccesoDocente(int idCurso, Docente docente);

    /**
     * Busca un curso activo y, si no existe o está dado de baja, informa el motivo.
     *
     * @param idCurso El identificador del curso.
     * @return El curso activo.
     * @throws IllegalArgumentException Si el curso no se encuentra activo.
     */
    Curso buscarActivoPorId(Integer idCurso);

    /**
     * Reúne, para cada curso de una lista.
     *
     * @param cursos Los cursos del listado.
     * @return El resumen de los cursos.
     */
    ResumenCursos resumir(List<Curso> cursos);

    /**
     * Resuelve con qué rol y programa entra una persona al panel de un curso que se abre con su identificador.
     *
     * @param idCurso El identificador del curso (obligatorio).
     * @param usuario El usuario autenticado.
     * @return El contexto de la pantalla.
     * @throws IllegalArgumentException Si no se indicó el curso, no está activo o el docente no participa.
     */
    ContextoCurso resolverContextoDelPanel(Integer idCurso, Usuario usuario);

    /**
     * Resuelve el contexto de una pantalla que se abre desde un tema (foro, glosario, material).
     *
     * @param tema El tema de la pantalla.
     * @param usuario El usuario autenticado.
     * @return El contexto, o vacío si no es de la gestión del curso ni tiene una inscripción con acceso.
     */
    Optional<ContextoCurso> resolverContextoDeLaTema(Tema tema, Usuario usuario);

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Registra un curso con sus modalidades de dictado y su equipo docente.
     *
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param cargaHorariaMinutos la carga horaria para el certificado, en minutos (opcional: la completa el docente)
     * @param duracionDias la duración del curso, en días
     * @param imagen la ruta de la imagen de portada (opcional)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel (opcional: lo completa el docente)
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso registrado
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    Curso registrar(String nombre, String descripcion, Float precio, Integer cargaHorariaMinutos,
            Integer duracionDias, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            List<Integer> idsDocentesAyudantes);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica un curso activo.
     *
     * @param idCurso el identificador del curso
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param cargaHorariaMinutos la carga horaria para el certificado, en minutos (opcional)
     * @param duracionDias la duración del curso, en días
     * @param imagen la ruta de la imagen de portada (opcional, si es nula se conserva la actual)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel (opcional: si es nulo se conserva el actual)
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso modificado
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Curso modificar(Integer idCurso, String nombre, String descripcion, Float precio,
            Integer cargaHorariaMinutos, Integer duracionDias, String imagen,
            Integer idCategoria, Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades,
            Integer idDocenteTitular, List<Integer> idsDocentesAyudantes);

    /**
     * Completa los datos académicos de un curso: los que define el docente al diseñarlo.
     *
     * @param idCurso el identificador del curso
     * @param docente el docente que los completa (debe formar parte del equipo del curso)
     * @param idNivel el identificador del nivel
     * @param descripcion la descripción del curso (opcional)
     * @param objetivos los objetivos del curso
     * @param cargaHorariaMinutos la carga horaria para el certificado, en minutos (obligatoria si el curso emite certificado)
     * @return el curso actualizado
     * @throws IllegalArgumentException si el docente no participa en el curso o algún dato no es válido
     */
    Curso modificarPorDocente(Integer idCurso, Docente docente, Integer idNivel, String descripcion, String objetivos,
            Integer cargaHorariaMinutos);

    /**
     * Asigna la imagen de un curso activo (el archivo ya se guardó en la carpeta del curso).
     *
     * @param idCurso El identificador del curso.
     * @param imagen El nombre del archivo de la imagen dentro de la carpeta del curso.
     * @return El curso actualizado.
     * @throws IllegalArgumentException Si el curso no está activo.
     */
    Curso actualizarImagen(Integer idCurso, String imagen);

    /** Lleva las imágenes de los cursos a la estructura curso{id/imagen.{ext}}. */
    void migrarImagenes();

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja un curso activo que no tenga programas ni temas activos asociadas.
     *
     * @param idCurso el identificador del curso
     * @throws IllegalArgumentException si el curso no está activo o tiene programas o temas activos
     */
    void darDeBaja(Integer idCurso);

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    void darDeBajaVarios(java.util.List<Integer> ids);

    /**
     * Contexto con el que se muestra una pantalla del panel de un curso: quién la ve.
     *
     * @param curso El curso abierto.
     * @param programa El programa con el que se trabaja (puede ser {@code null} si el curso todavía no tiene).
     * @param rol El rol con el que la persona entra al curso.
     * @param participacion La participación del docente en el curso ({@code null} si no entra como docente).
     * @param urlBase La ruta de la página principal del curso para ese rol.
     * @param urlMisCursos La ruta del listado de cursos para ese rol.
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
                    "/curso/acceder/" + curso.getIdCurso(), "/curso/buscar?vista=cards");
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
                    "/inscripcion/acceder/" + inscripcion.getIdInscripcion(), "/inscripcion/buscar");
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

    /**
     * Datos de un conjunto de cursos que necesita el listado para validar la baja y la modificación de cada uno.
     *
     * @param inscripcionesPorCurso Las inscripciones activas de cada curso, por identificador del curso.
     * @param programasPorCurso Los programas activos de cada curso, por identificador del curso.
     * @param cursosConInscripcionAbierta Los identificadores de los cursos que tienen alguna cohorte con inscripción abierta.
     */
    public record ResumenCursos(Map<Integer, List<Inscripcion>> inscripcionesPorCurso,
            Map<Integer, List<Programa>> programasPorCurso, Set<Integer> cursosConInscripcionAbierta) {
    }

    /**
     * El contenido de un tema del programa vigente, según lo que prometen las modalidades del curso.
     *
     * @param tema El tema.
     * @param celdas Para cada columna que corresponde al curso (por ejemplo "Video clon"), si el tema ya la tiene.
     */
    public record ContenidoDeTema(Tema tema, Map<String, Boolean> celdas) {

        /** Obtiene los nombres de lo que le falta al tema. */
        public List<String> getFaltantes() {
            return celdas.entrySet().stream().filter(celda -> !celda.getValue()).map(Map.Entry::getKey).toList();
        }
    }

    /**
     * Estado del diseño de un curso.
     *
     * @param faltantesDeDatos Lo que falta de los datos del curso (nivel, objetivos, carga horaria).
     * @param cantidadTemas La cantidad de temas vigentes del curso.
     * @param minimoTemas El mínimo de temas que exige el sistema.
     * @param programaVigente El programa de la cohorte de trabajo (el vigente del curso sin cohorte), o {@code null} si no tiene.
     * @param duracionAsignada Si el programa vigente asigna toda la duración del curso.
     * @param contenido El contenido de cada tema vigente del curso.
     * @param cohortesPendientes La cantidad de cohortes pendientes del curso.
     * @param primeraInscripcion El inicio de inscripción más próximo de las cohortes pendientes, o {@code null}.
     */
    public record EstadoDiseno(List<String> faltantesDeDatos, int cantidadTemas, int minimoTemas, Programa programaVigente,
            boolean duracionAsignada, List<ContenidoDeTema> contenido, int cohortesPendientes,
            LocalDateTime primeraInscripcion) {

        /** Indica si el docente completó los datos del curso (paso 1 del diseño: Datos). */
        public boolean isDatosCompletos() {
            return faltantesDeDatos.isEmpty();
        }

        /** Obtiene los temas del curso que ya tienen todo su contenido. */
        public List<ContenidoDeTema> getTemasConContenido() {
            return contenido.stream().filter(c -> c.getFaltantes().isEmpty()).toList();
        }

        /**
         * Indica si el curso cumple el paso 2 del diseño (Temas y contenido). Mientras haya cohortes pendientes se exige que todos los temas tengan su
         * contenido completo, para que las alertas guíen al docente. Sin cohortes pendientes alcanza con el mínimo de temas completos.
         */
        public boolean isTemasCompletos() {
            if (cohortesPendientes > 0) {
                return cantidadTemas >= minimoTemas && getTemasIncompletos().isEmpty();
            }
            return getTemasConContenido().size() >= minimoTemas;
        }

        /** Calcula cuántos temas le faltan al curso para el paso 2: los que no tienen su contenido completo o los que faltan registrar para llegar al mínimo. */
        public int getTemasQueFaltan() {
            if (cohortesPendientes == 0) {
                return Math.max(0, minimoTemas - getTemasConContenido().size());
            }
            return Math.max(getTemasIncompletos().size(), Math.max(0, minimoTemas - cantidadTemas));
        }

        /** Obtiene los temas del curso a los que les falta contenido. */
        public List<ContenidoDeTema> getTemasIncompletos() {
            return contenido.stream().filter(c -> !c.getFaltantes().isEmpty()).toList();
        }

        /** Obtiene los identificadores de los temas a los que les falta contenido, separados por coma. */
        public String getIdsIncompletos() {
            return getTemasIncompletos().stream().map(c -> String.valueOf(c.tema().getIdTema()))
                    .collect(java.util.stream.Collectors.joining(","));
        }

        /** Indica si el programa está completo, es decir si asigna toda la duración y todos sus temas tienen su contenido. */
        public boolean isProgramaCompleto() {
            if (programaVigente == null || !duracionAsignada) {
                return false;
            }
            java.util.Set<Integer> completos = getTemasConContenido().stream().map(c -> c.tema().getIdTema())
                    .collect(java.util.stream.Collectors.toSet());
            return programaVigente.getCronogramaOrdenado().stream().allMatch(c -> completos.contains(c.getTema().getIdTema()));
        }

        /** Obtiene el paso del diseño en el que está el curso: 1 datos, 2 temas y contenido. */
        public int getPasoActual() {
            if (!isDatosCompletos()) {
                return 1;
            }
            if (!isTemasCompletos()) {
                return 2;
            }
            return isProgramaCompleto() ? 4 : 3;
        }

        /** Calcula hasta qué parte del menú se habilita: el contenido (temas, recursos y demás) desde el paso 2. */
        public int getNivelDeMenu() {
            // Cada paso se mira por sí solo, igual que las marcas de la línea de tiempo
            int nivel = 1;
            if (isDatosCompletos()) {
                nivel = 2;
            }
            if (isTemasCompletos()) {
                nivel = 3;
            }
            if (isTemasCompletos() && isProgramaCompleto()) {
                nivel = 4;
            }
            return nivel;
        }

        /** Indica si el diseño del curso está completo. */
        public boolean isCompleto() {
            return isDatosCompletos() && isTemasCompletos() && isProgramaCompleto();
        }
    }

    /**
     * El próximo recurso que el docente debe agregar a un tema para completar su contenido.
     *
     * @param opcion Qué opción del selector de recursos corresponde: {@code BIBLIOGRAFIA}, {@code AUTOEVALUACION}, {@code POOL}, {@code GRABACION}, o {@code null} si no hay nada que se pueda agregar a mano (por ejemplo, el video clon).
     * @param aviso Una nota para el docente (por ejemplo, que primero tiene que registrar un pool), o {@code null}.
     */
    public record SiguienteRecurso(String opcion, String aviso) {
    }

    /**
     * Calcula el estado del diseño de un curso.
     *
     * @param curso El curso.
     * @return El estado de su diseño.
     */
    EstadoDiseno calcularEstadoDiseno(Curso curso);

    /**
     * Calcula el estado del diseño de un curso para una cohorte.
     *
     * @param curso El curso.
     * @param cohorte La cohorte de trabajo, o {@code null}.
     * @return El estado de su diseño.
     */
    EstadoDiseno calcularEstadoDiseno(Curso curso, Cohorte cohorte);

    /**
     * Calcula el estado del diseño de un curso tomando como programa el de una cohorte (el campo programaVigente del resultado es ese programa).
     *
     * @param curso El curso.
     * @param programa El programa cuyo cronograma y contenido se miran, o {@code null} si todavía no hay.
     * @return El estado de su diseño.
     */
    EstadoDiseno calcularEstadoDiseno(Curso curso, Programa programa);

    /**
     * Indica si un programa está completo.
     *
     * @param curso El curso del programa.
     * @param programa El programa.
     * @return {@code true} si está completo.
     */
    boolean estaDisenoCompleto(Curso curso, Programa programa);

    /**
     * Calcula el siguiente recurso que le falta a un tema: primero el material (se propone una bibliografía).
     *
     * @param curso El curso.
     * @param programa El programa en que está el tema (de él salen los temas anteriores, cuyos pools también sirven).
     * @param tema El tema.
     * @return El próximo recurso, o uno sin opción si el tema está completo o lo que falta no se agrega a mano.
     */
    SiguienteRecurso buscarSiguienteRecurso(Curso curso, Programa programa, Tema tema);

    /**
     * Las columnas de contenido que corresponden a un curso, según sus modalidades.
     *
     * @param curso El curso.
     * @return Por ejemplo "Video clon", "Material", "Autoevaluación".
     */
    List<String> buscarColumnasDeContenido(Curso curso);

    /**
     * Calcula el contenido de un tema: lo que prometen las modalidades del curso.
     *
     * @param curso El curso (de él salen las modalidades).
     * @param tema El tema.
     * @return Lo que el tema tiene y lo que le falta.
     */
    ContenidoDeTema buscarContenidoDeTema(Curso curso, Tema tema);
}
