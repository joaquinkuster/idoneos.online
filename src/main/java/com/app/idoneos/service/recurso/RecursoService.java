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
    TemaRecurso subir(Docente docente, Integer idTema, String tipo, DatosRecurso datos, MultipartFile archivo,
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
