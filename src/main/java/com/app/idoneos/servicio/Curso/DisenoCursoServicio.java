package com.app.idoneos.servicio.Curso;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.EstadoGeneracion;
import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Calcula en qué estado está el diseño de un curso: lo que el docente completa (datos del curso, temas, programa y su
 * contenido) y lo que espera ese diseño (las cohortes pendientes). Alimenta la vista Diseño de curso y los indicadores del
 * menú lateral.
 */
@Service
public class DisenoCursoServicio {

    /** Cuando el parámetro no está cargado, el mínimo de temas con material que se exige (el mismo de los programas). */
    private static final int MINIMO_POR_DEFECTO = 1;

    @Autowired
    private ParametroServicio parametroServicio;

    /**
     * El contenido de un tema del programa vigente, según lo que prometen las modalidades del curso.
     *
     * @param tema   El tema.
     * @param celdas Para cada columna que corresponde al curso (por ejemplo "Video clon"), si el tema ya la tiene.
     */
    public record ContenidoDeTema(Tema tema, Map<String, Boolean> celdas) {

        /** Los nombres de lo que le falta al tema. */
        public List<String> getFaltantes() {
            return celdas.entrySet().stream().filter(celda -> !celda.getValue()).map(Map.Entry::getKey).toList();
        }
    }

    /**
     * Estado del diseño de un curso.
     *
     * @param faltantesDeDatos     Lo que falta de los datos del curso (nivel, objetivos, carga horaria).
     * @param cantidadTemas        La cantidad de temas vigentes del curso.
     * @param minimoTemas          El mínimo de temas que exige el sistema.
     * @param programaVigente      El programa vigente, o {@code null} si el curso no tiene.
     * @param duracionAsignada     Si el programa vigente asigna toda la duración del curso.
     * @param contenido            El contenido de cada tema del programa vigente.
     * @param cohortesPendientes   La cantidad de cohortes pendientes del curso.
     * @param primeraInscripcion   El inicio de inscripción más próximo de las cohortes pendientes, o {@code null}.
     */
    public record EstadoDiseno(List<String> faltantesDeDatos, int cantidadTemas, int minimoTemas, Programa programaVigente,
            boolean duracionAsignada, List<ContenidoDeTema> contenido, int cohortesPendientes,
            LocalDateTime primeraInscripcion) {

        /** Si el docente completó los datos del curso. */
        public boolean isDatosCompletos() {
            return faltantesDeDatos.isEmpty();
        }

        /** Si el curso tiene el mínimo de temas. */
        public boolean isTemasCompletos() {
            return cantidadTemas >= minimoTemas;
        }

        /** Si el tema del programa vigente que más le falta existe: lo que falta de contenido, uno por tema. */
        public List<ContenidoDeTema> getTemasIncompletos() {
            return contenido.stream().filter(c -> !c.getFaltantes().isEmpty()).toList();
        }

        /** Los números de orden (en el cronograma del programa vigente) de los temas a los que les falta contenido, separados por coma. */
        public String getOrdenesIncompletos() {
            if (programaVigente == null) {
                return "";
            }
            java.util.Set<Integer> ids = getTemasIncompletos().stream().map(c -> c.tema().getIdTema())
                    .collect(java.util.stream.Collectors.toSet());
            return programaVigente.getCronogramaOrdenado().stream().filter(u -> ids.contains(u.getTema().getIdTema()))
                    .map(u -> String.valueOf(u.getNumeroOrden())).collect(java.util.stream.Collectors.joining(","));
        }

        /** Si el programa vigente está completo: asigna toda la duración y todos sus temas tienen contenido. */
        public boolean isProgramaCompleto() {
            return programaVigente != null && duracionAsignada && getTemasIncompletos().isEmpty();
        }

        /**
         * El paso del diseño en el que está la cohorte: 1 datos del curso, 2 programa de la cohorte, 3 temas, 4 cronograma
         * y 5 contenido; 6 si el diseño está completo.
         */
        public int getPasoActual() {
            if (!isDatosCompletos()) {
                return 1;
            }
            if (programaVigente == null) {
                return 2;
            }
            if (!isTemasCompletos()) {
                return 3;
            }
            if (!duracionAsignada) {
                return 4;
            }
            return getTemasIncompletos().isEmpty() ? 6 : 5;
        }

        /**
         * Hasta qué parte del menú se habilita: el número del paso en que está la cohorte (el diseño completo habilita
         * todo, como el paso 5). Programas se habilita en el paso 2, Temas en el 3, Cronogramas en el 4 y todo el contenido
         * de los temas en el 5.
         */
        public int getNivelDeMenu() {
            // Cada paso se mira por sí solo, igual que las marcas de la línea de tiempo: si el programa ya está asignado y los temas
            // cargados, el menú se habilita hasta ahí aunque otro paso anterior (por ejemplo los datos) siga pendiente
            boolean programa = programaVigente != null;
            boolean temas = isTemasCompletos();
            boolean cronograma = programa && duracionAsignada;
            int nivel = 1;
            if (isDatosCompletos()) {
                nivel = 2;
            }
            if (programa) {
                nivel = 3;
            }
            if (programa && temas) {
                nivel = 4;
            }
            if (cronograma && temas) {
                nivel = 5;
            }
            return nivel;
        }

        /** Si el diseño del curso está completo. */
        public boolean isCompleto() {
            return isDatosCompletos() && isTemasCompletos() && isProgramaCompleto();
        }
    }

    /**
     * Calcula el estado del diseño de un curso.
     *
     * @param curso El curso.
     * @return El estado de su diseño.
     */
    public EstadoDiseno estadoDe(Curso curso) {
        return estadoDe(curso, curso.getProgramaVigente());
    }

    /**
     * Calcula el estado del diseño de un curso para una cohorte: el programa es el de la cohorte (ninguno si todavía no
     * se le asignó). Sin cohorte, el del programa vigente del curso.
     *
     * @param curso   El curso.
     * @param cohorte La cohorte de trabajo, o {@code null}.
     * @return El estado de su diseño.
     */
    public EstadoDiseno estadoDe(Curso curso, Cohorte cohorte) {
        return estadoDe(curso, cohorte == null ? curso.getProgramaVigente() : cohorte.getPrograma());
    }

    /**
     * Calcula el estado del diseño de un curso tomando como programa el de una cohorte (el campo
     * {@code programaVigente} del resultado es ese programa).
     *
     * @param curso    El curso.
     * @param programa El programa cuyo cronograma y contenido se miran, o {@code null} si todavía no hay.
     * @return El estado de su diseño.
     */
    public EstadoDiseno estadoDe(Curso curso, Programa programa) {
        int minimo = parametroServicio.obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO);
        int temas = (int) curso.getTemas().stream().filter(tema -> !tema.getBaja()).count();
        Programa vigente = programa;
        List<ContenidoDeTema> contenido = new ArrayList<>();
        if (vigente != null) {
            vigente.getCronogramaOrdenado().forEach(cronograma -> contenido.add(contenidoDe(curso, cronograma.getTema())));
        }
        List<Cohorte> pendientes = curso.getCohortes().stream().filter(c -> !c.getBaja() && c.esPendiente()).toList();
        LocalDateTime primera = pendientes.stream().map(Cohorte::getFechaInicioInscripcion).min(Comparator.naturalOrder())
                .orElse(null);
        return new EstadoDiseno(curso.getFaltantesDeDatosAcademicos(), temas, minimo, vigente,
                vigente != null && vigente.estaDuracionAsignadaCompleta(), contenido, pendientes.size(), primera);
    }

    /**
     * Indica si un programa está completo: asigna toda la duración del curso y todos sus temas tienen su contenido.
     *
     * @param curso    El curso del programa.
     * @param programa El programa.
     * @return {@code true} si está completo.
     */
    public boolean estaCompleto(Curso curso, Programa programa) {
        return programa.estaDuracionAsignadaCompleta() && programa.getCronogramaOrdenado().stream()
                .allMatch(cronograma -> contenidoDe(curso, cronograma.getTema()).getFaltantes().isEmpty());
    }

    /**
     * El próximo recurso que el docente debe agregar a un tema para completar su contenido.
     *
     * @param opcion Qué opción del selector de recursos corresponde: {@code BIBLIOGRAFIA}, {@code AUTOEVALUACION}, {@code POOL},
     *               {@code GRABACION}, o {@code null} si no hay nada que se pueda agregar a mano (por ejemplo, el video clon).
     * @param aviso  Una nota para el docente (por ejemplo, que primero tiene que registrar un pool), o {@code null}.
     */
    public record SiguienteRecurso(String opcion, String aviso) {
    }

    /**
     * Calcula el siguiente recurso que le falta a un tema: primero el material (se propone una bibliografía), después la
     * autoevaluación (si no hay pools que se puedan usar, se propone registrar uno) y por último lo que pide la modalidad del
     * curso (por ejemplo, la grabación).
     *
     * @param curso    El curso.
     * @param programa El programa en que está el tema (de él salen los temas anteriores, cuyos pools también sirven).
     * @param tema     El tema.
     * @return El próximo recurso, o uno sin opción si el tema está completo o lo que falta no se agrega a mano.
     */
    public SiguienteRecurso siguienteRecurso(Curso curso, Programa programa, Tema tema) {
        Map<String, Boolean> celdas = contenidoDe(curso, tema).celdas();
        if (!celdas.getOrDefault("Material", true)) {
            return new SiguienteRecurso("BIBLIOGRAFIA", null);
        }
        if (!celdas.getOrDefault("Autoevaluación", true)) {
            if (hayPoolsParaElTema(programa, tema)) {
                return new SiguienteRecurso("AUTOEVALUACION", null);
            }
            return new SiguienteRecurso("POOL", "Para crear una autoevaluación tenés que registrar al menos un pool de preguntas, del mismo tema o de uno anterior.");
        }
        if (!celdas.getOrDefault("Clase grabada", true)) {
            return new SiguienteRecurso("GRABACION", null);
        }
        return new SiguienteRecurso(null, null);
    }

    /** Si hay algún pool vigente del tema o de un tema anterior del programa, que se pueda usar para su autoevaluación. */
    private boolean hayPoolsParaElTema(Programa programa, Tema tema) {
        if (tema.getPooles().stream().anyMatch(pool -> !pool.getBaja())) {
            return true;
        }
        if (programa == null) {
            return false;
        }
        for (var cronograma : programa.getCronogramaOrdenado()) {
            if (cronograma.getTema().getIdTema() == tema.getIdTema()) {
                return false;
            }
            if (cronograma.getTema().getPooles().stream().anyMatch(pool -> !pool.getBaja())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Las columnas de contenido que corresponden a un curso, según sus modalidades.
     *
     * @param curso El curso.
     * @return Por ejemplo "Video clon", "Material", "Autoevaluación".
     */
    public List<String> columnasDeContenido(Curso curso) {
        List<String> columnas = new ArrayList<>();
        if (curso.incluyeModalidad(Modalidad.CLON_IA)) {
            columnas.add("Video clon");
        }
        if (curso.incluyeModalidad(Modalidad.GRABADA)) {
            columnas.add("Clase grabada");
        }
        columnas.add("Material");
        columnas.add("Autoevaluación");
        return columnas;
    }

    /**
     * Calcula el contenido de un tema: lo que prometen las modalidades del curso (video clon y clase grabada, solo si el
     * curso las ofrece), más el material y la autoevaluación, que siempre se piden.
     *
     * @param curso El curso (de él salen las modalidades).
     * @param tema  El tema.
     * @return Lo que el tema tiene y lo que le falta.
     */
    public ContenidoDeTema contenidoDe(Curso curso, Tema tema) {
        Map<String, Boolean> celdas = new LinkedHashMap<>();
        List<TemaRecurso> publicados = tema.getMaterialesPublicados();
        if (curso.incluyeModalidad(Modalidad.CLON_IA)) {
            celdas.put("Video clon", publicados.stream().anyMatch(m -> m.getRecurso().getVideoClon() != null
                    && EstadoGeneracion.GENERADO.equals(m.getRecurso().getVideoClon().getEstadoGeneracion().getNombre())));
        }
        if (curso.incluyeModalidad(Modalidad.GRABADA)) {
            celdas.put("Clase grabada", publicados.stream().anyMatch(m -> Modalidad.GRABADA.equals(m.getModalidadAsociada())));
        }
        celdas.put("Material", publicados.stream().anyMatch(m -> m.getModalidadAsociada() == null));
        celdas.put("Autoevaluación", !tema.getAutoevaluacionesPublicadas().isEmpty());
        return new ContenidoDeTema(tema, celdas);
    }
}
