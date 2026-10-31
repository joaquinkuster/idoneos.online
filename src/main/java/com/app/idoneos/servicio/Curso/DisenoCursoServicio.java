package com.app.idoneos.servicio.Curso;

import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Material;
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

        /** Si el programa vigente está completo: asigna toda la duración y todos sus temas tienen contenido. */
        public boolean isProgramaCompleto() {
            return programaVigente != null && duracionAsignada && getTemasIncompletos().isEmpty();
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
        int minimo = parametroServicio.obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO);
        int temas = (int) curso.getTemas().stream().filter(tema -> !tema.getBaja()).count();
        Programa vigente = curso.getProgramaVigente();
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
        List<Material> publicados = tema.getMaterialesPublicados();
        if (curso.incluyeModalidad(Modalidad.CLON_IA)) {
            celdas.put("Video clon", publicados.stream().anyMatch(m -> Modalidad.CLON_IA.equals(m.getModalidadAsociada())));
        }
        if (curso.incluyeModalidad(Modalidad.GRABADA)) {
            celdas.put("Clase grabada", publicados.stream().anyMatch(m -> Modalidad.GRABADA.equals(m.getModalidadAsociada())));
        }
        celdas.put("Material", publicados.stream().anyMatch(m -> m.getModalidadAsociada() == null));
        celdas.put("Autoevaluación", !tema.getAutoevaluacionesPublicadas().isEmpty());
        return new ContenidoDeTema(tema, celdas);
    }
}
