package com.app.idoneos.servicio.AccesoCurso;

import java.util.List;

import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;

/**
 * Unidad del programa de la cohorte del alumno, con su estado de avance y su contenido publicado (CU-27).
 * El contenido sólo se carga si la unidad está habilitada según el avance secuencial del alumno.
 *
 * @param cronograma          La ubicación de la unidad en el cronograma (orden y duración).
 * @param completada          Indica si el alumno completó la unidad.
 * @param habilitada          Indica si el alumno puede acceder a la unidad (es la primera o completó la anterior).
 * @param enCurso             Indica si es la unidad que el alumno debe cursar ahora (habilitada y sin completar).
 * @param materiales          El material publicado (vigente y no oculto).
 * @param glosario            Los términos del glosario de la unidad.
 * @param autoevaluaciones    Las autoevaluaciones publicadas con los intentos propios del alumno.
 * @param cantidadConsultas   La cantidad de consultas registradas en el foro de la unidad.
 */
public record UnidadAcceso(UnidadCronograma cronograma, boolean completada, boolean habilitada, boolean enCurso,
        List<Material> materiales, List<TerminoGlosario> glosario, List<AutoevaluacionAcceso> autoevaluaciones,
        int cantidadConsultas) {

    /**
     * Unidad del curso a la que corresponde este elemento del cronograma.
     *
     * @return La unidad.
     */
    public Unidad unidad() {
        return cronograma.getUnidad();
    }

    /**
     * Número de orden de la unidad dentro del programa.
     *
     * @return El número de orden.
     */
    public int numeroOrden() {
        return cronograma.getNumeroOrden();
    }

    /**
     * Estado de la unidad para mostrar al alumno: Completada, En curso o Bloqueada.
     *
     * @return El estado de la unidad.
     */
    public String estado() {
        if (completada) {
            return "Completada";
        }
        return habilitada ? "En curso" : "Bloqueada";
    }
}
