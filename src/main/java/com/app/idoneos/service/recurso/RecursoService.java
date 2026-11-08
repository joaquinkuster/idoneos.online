package com.app.idoneos.service.recurso;

import com.app.idoneos.model.Recurso;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.Usuario;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio de los recursos de los temas (CU-28 a CU-31, "Materiales"): lo que el docente carga a un tema (presentaciones,
 * bibliografía, grabaciones y enlaces) y lo que el alumno ve. Cada material es un {@link TemaRecurso}: un recurso del
 * docente usado en un tema.
 */
public interface RecursoService extends CrudService<TemaRecurso> {

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
     * @param privado         si el recurso es privado (solo lo usa su dueño) o público (cualquier docente puede usarlo desde la biblioteca).
     */
    record DatosRecurso(String titulo, String descripcion, boolean oculto, String autor, String enlace, String isbn,
            String editorial, Integer paginas, LocalDate fechaPublicacion, Integer duracionMinutos, boolean privado) {

        /** Los mismos datos con el recurso privado, que es el valor por defecto. */
        public DatosRecurso(String titulo, String descripcion, boolean oculto, String autor, String enlace, String isbn,
                String editorial, Integer paginas, LocalDate fechaPublicacion, Integer duracionMinutos) {
            this(titulo, descripcion, oculto, autor, enlace, isbn, editorial, paginas, fechaPublicacion, duracionMinutos, true);
        }
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
     * Cuenta los otros temas (activos) que usan el mismo recurso que el material. Mientras haya alguno, el material no se puede modificar
     * ni dar de baja: el recurso está compartido.
     *
     * @param material el material.
     * @return la cantidad de otros usos activos del recurso.
     */
    long contarOtrosUsos(TemaRecurso material);

    /**
     * Indica si el docente es el dueño del recurso del material. Solo el dueño modifica el recurso (y elige si es privado o público);
     * quien usa en su tema un recurso público de otro docente solo puede quitarlo de su tema.
     *
     * @param material el material.
     * @param docente  el docente.
     * @return {@code true} si es el dueño.
     */
    boolean esDuenio(TemaRecurso material, Docente docente);

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

    // ---------------------------------------------------------------- Biblioteca del docente

    /**
     * Busca los recursos activos de un docente (los que es dueño), el más reciente primero.
     *
     * @param docente el docente.
     * @return sus recursos.
     */
    List<Recurso> buscarRecursosDelDocente(Docente docente);

    /**
     * Busca los recursos que un docente ve en la biblioteca digital: todos los suyos (públicos y privados) y los públicos de otros docentes.
     *
     * @param docente el docente.
     * @return los recursos visibles, el más reciente primero.
     */
    List<Recurso> buscarRecursosVisibles(Docente docente);

    /**
     * Busca los recursos públicos activos de otros docentes (lo que se explora en la biblioteca), el más reciente primero.
     *
     * @param excluido el docente cuyos recursos no se incluyen (el que explora), o {@code null} para incluirlos todos.
     * @return los recursos públicos.
     */
    List<Recurso> buscarRecursosPublicos(Docente excluido);

    /**
     * Busca todos los recursos activos de la plataforma, públicos y privados, el más reciente primero. Es lo que ve el administrador en la
     * biblioteca.
     *
     * @return todos los recursos.
     */
    List<Recurso> buscarTodosLosRecursos();

    /**
     * Da de baja un recurso de la biblioteca como administrador (por ejemplo, un recurso público que no corresponde). Mientras algún tema
     * lo use no se puede, como cuando lo hace su dueño.
     *
     * @param idRecurso el identificador del recurso.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBajaComoAdministrador(Integer idRecurso);

    /**
     * Indica si un usuario puede ver un recurso desde la biblioteca: el administrador, su dueño o cualquier docente si es público.
     *
     * @param recurso el recurso.
     * @param usuario el usuario.
     * @return {@code true} si puede verlo.
     */
    boolean puedeVerEnBiblioteca(Recurso recurso, Usuario usuario);

    /**
     * Busca un recurso activo por su identificador.
     *
     * @param idRecurso el identificador del recurso.
     * @return el recurso, o vacío si no existe o está dado de baja.
     */
    Optional<Recurso> buscarRecursoPorId(Integer idRecurso);

    /**
     * Cuenta los temas activos que usan un recurso. Mientras haya alguno, el recurso no se puede modificar ni dar de baja.
     *
     * @param recurso el recurso.
     * @return la cantidad de usos activos.
     */
    long contarUsos(Recurso recurso);

    /**
     * Carga un recurso a la biblioteca del docente, sin asociarlo a un tema.
     *
     * @param docente  el docente dueño.
     * @param tipo     el tipo de recurso.
     * @param datos    los datos del recurso.
     * @param archivo  el archivo (según el tipo).
     * @param terminos si el docente aceptó los términos y condiciones sobre el contenido.
     * @return el recurso cargado.
     * @throws IllegalArgumentException si algún dato no es válido.
     */
    Recurso subirABiblioteca(Docente docente, String tipo, DatosRecurso datos, MultipartFile archivo, boolean terminos);

    /**
     * Modifica un recurso de la biblioteca. Solo su dueño, y mientras ningún tema lo use.
     *
     * @param idRecurso el identificador del recurso.
     * @param docente   el docente que lo modifica.
     * @param datos     los datos nuevos.
     * @param archivo   el archivo nuevo (opcional).
     * @return el recurso modificado.
     * @throws IllegalArgumentException si no se puede o algún dato no es válido.
     */
    Recurso modificarEnBiblioteca(Integer idRecurso, Docente docente, DatosRecurso datos, MultipartFile archivo);

    /**
     * Publica un recurso en la biblioteca digital: pasa de privado a público, y desde ahí otros docentes pueden verlo. Solo su dueño.
     *
     * @param idRecurso el identificador del recurso.
     * @param docente   el docente que lo publica.
     * @throws IllegalArgumentException si no se puede.
     */
    void publicar(Integer idRecurso, Docente docente);

    /**
     * Da de baja varios recursos de la biblioteca a la vez, todos o ninguno. Solo su dueño, y mientras ningún tema los use.
     *
     * @param ids     los identificadores de los recursos.
     * @param docente el docente que los da de baja.
     * @throws IllegalArgumentException si alguno no se puede dar de baja.
     */
    void darDeBajaVariosEnBiblioteca(List<Integer> ids, Docente docente);

    /**
     * Da de baja varios recursos de la biblioteca a la vez, todos o ninguno, como administrador (de cualquier docente). Mientras
     * ningún tema los use.
     *
     * @param ids los identificadores de los recursos.
     * @throws IllegalArgumentException si alguno no se puede dar de baja.
     */
    void darDeBajaVariosComoAdministrador(List<Integer> ids);

    /**
     * Da de baja un recurso de la biblioteca. Solo su dueño, y mientras ningún tema lo use.
     *
     * @param idRecurso el identificador del recurso.
     * @param docente   el docente que lo da de baja.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBajaEnBiblioteca(Integer idRecurso, Docente docente);

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
    TemaRecurso subir(Docente docente, Integer idTema, String tipo, DatosRecurso datos, MultipartFile archivo,
            boolean terminos);

    /**
     * Registra en un tema un recurso que ya está en la biblioteca digital (propio o público de otro docente), sin crear uno nuevo.
     *
     * @param docente   el docente que lo registra (debe participar en el curso del tema).
     * @param idTema    el tema.
     * @param idRecurso el recurso de la biblioteca.
     * @return el vínculo del recurso con el tema.
     * @throws IllegalArgumentException si el tema o el recurso no son válidos, el recurso no es visible para el docente, o ya está en el tema.
     */
    TemaRecurso reutilizar(Docente docente, Integer idTema, Integer idRecurso);

    /**
     * Registra en un tema varios recursos de la biblioteca digital a la vez: o se registran todos o ninguno.
     *
     * @param docente    el docente que los registra.
     * @param idTema     el tema.
     * @param idsRecurso los recursos de la biblioteca (sin repetir).
     * @return los vínculos creados, en el orden pedido.
     * @throws IllegalArgumentException si no se eligió ninguno o alguno no puede registrarse (no se registra ninguno).
     */
    java.util.List<TemaRecurso> reutilizar(Docente docente, Integer idTema, java.util.List<Integer> idsRecurso);

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
    TemaRecurso modificar(Integer id, Docente docente, DatosRecurso datos, MultipartFile archivo);

    // ---------------------------------------------------------------- Bajas

    /**
     * CU-30: Da de baja un material.
     *
     * @param id      el identificador del material.
     * @param docente el docente que lo da de baja, o {@code null} si es el administrador.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBaja(Integer id, Docente docente);

    /**
     * CU-30: Da de baja varios materiales a la vez, todos o ninguno.
     *
     * @param ids     los identificadores.
     * @param docente el docente, o {@code null} si es el administrador.
     * @throws IllegalArgumentException si no se puede.
     */
    void darDeBajaVarios(List<Integer> ids, Docente docente);

    /**
     * Clases de recurso que el docente puede cargar a mano, con lo que cada una pide: los formatos de archivo que admite y su
     * tamaño máximo. El tipo de un recurso guardado se deduce de la tabla que tiene sus datos ({@link Recurso#getTipo()}).
     */
    public enum TipoRecurso {

        /** Diapositivas de una clase (un archivo PDF). */
        PRESENTACION(Recurso.PRESENTACION, true, List.of("pdf"), 50, "Diapositivas de una clase."),

        /** Resumen de un tema (lo genera el sistema). */
        RESUMEN(Recurso.RESUMEN, false, List.of("pdf"), 50, "Resumen de un tema."),

        /** Texto o material de lectura, con su autor y su ISBN (el archivo PDF es opcional). */
        BIBLIOGRAFIA(Recurso.BIBLIOGRAFIA, true, List.of("pdf"), 50, "Texto o material de lectura, con su autor y su ISBN."),

        /** Video de una clase dictada. */
        GRABACION(Recurso.GRABACION, true, List.of("mp4", "webm"), 500, "Video de una clase dictada."),

        /** Dirección web de un material que está fuera de la plataforma. */
        ENLACE(Recurso.ENLACE, true, List.of(), 0, "Dirección web de un material externo."),

        /** Video con el clon de inteligencia artificial del docente (lo genera el sistema). */
        VIDEO_CLON(Recurso.VIDEO_CLON, false, List.of("mp4", "webm"), 500, "Video con el clon del docente.");

        private final String nombre;
        private final boolean subidaManual;
        private final List<String> extensiones;
        private final long tamanioMaximoMb;
        private final String descripcion;

        TipoRecurso(String nombre, boolean subidaManual, List<String> extensiones, long tamanioMaximoMb, String descripcion) {
            this.nombre = nombre;
            this.subidaManual = subidaManual;
            this.extensiones = extensiones;
            this.tamanioMaximoMb = tamanioMaximoMb;
            this.descripcion = descripcion;
        }

        /**
         * Obtiene el nombre del tipo, como se muestra.
         *
         * @return Por ejemplo "Presentación".
         */
        public String getNombre() {
            return nombre;
        }

        /**
         * Indica si el docente puede cargar este tipo de recurso a mano.
         *
         * @return {@code true} si se carga a mano.
         */
        public boolean permiteSubidaManual() {
            return subidaManual;
        }

        /**
         * Obtiene los formatos de archivo que admite.
         *
         * @return Las extensiones permitidas (vacía si no lleva archivo).
         */
        public List<String> getExtensionesPermitidas() {
            return extensiones;
        }

        /**
         * Obtiene el tamaño máximo del archivo.
         *
         * @return El tamaño en bytes.
         */
        public long getTamanioMaximo() {
            return tamanioMaximoMb * 1024 * 1024;
        }

        /**
         * Describe lo que se sube, con sus formatos y su tamaño máximo.
         *
         * @return Un texto para el formulario.
         */
        public String getDescripcionDeSubida() {
            if (extensiones.isEmpty()) {
                return descripcion;
            }
            return descripcion + " Formatos: " + String.join(", ", extensiones).toUpperCase() + ". Máximo " + tamanioMaximoMb
                    + " MB.";
        }

        /**
         * Busca un tipo por su nombre de enumeración o por su nombre visible.
         *
         * @param texto El texto ("PRESENTACION" o "Presentación").
         * @return El tipo, o {@code null} si no existe.
         */
        public static TipoRecurso deTexto(String texto) {
            if (texto == null) {
                return null;
            }
            for (TipoRecurso tipo : values()) {
                if (tipo.name().equalsIgnoreCase(texto.trim()) || tipo.nombre.equalsIgnoreCase(texto.trim())) {
                    return tipo;
                }
            }
            return null;
        }

        /**
         * Devuelve los tipos que el docente carga a mano.
         *
         * @return Los tipos con subida manual.
         */
        public static List<TipoRecurso> deSubidaManual() {
            return List.of(values()).stream().filter(TipoRecurso::permiteSubidaManual).toList();
        }
    }
}
