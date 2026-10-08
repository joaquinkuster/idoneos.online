package com.app.idoneos.servicio.AccesoCurso;

import java.util.List;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Servicio del caso de uso CU-27 Acceder curso (MOD-F-02): permite al alumno consultar el cronograma, las unidades
 * con su contenido y las clases en vivo del curso en el que está inscripto.
 */
public interface AccesoCursoServicio {

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción: la inscripción debe ser suya y estar vigente,
     * su acceso no debe haber vencido y, si la cohorte tiene fecha de inicio de dictado, ésta no debe ser futura.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    Inscripcion validarAcceso(int idInscripcion, Alumno alumno);

    /**
     * Lista las unidades del programa de la cohorte del alumno, indicando cuáles están habilitadas según su avance
     * secuencial. Sólo las unidades habilitadas incluyen su contenido publicado.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Las unidades en orden de cronograma.
     */
    List<UnidadAcceso> buscarUnidades(Inscripcion inscripcion);

    /**
     * Arma el cronograma del programa de la cohorte del alumno, con la semana esperada de avance y el indicador
     * de atraso respecto de la última unidad completada.
     *
     * @param inscripcion La inscripción del alumno.
     * @return El cronograma con el avance esperado.
     */
    CronogramaAcceso buscarCronograma(Inscripcion inscripcion);

    /**
     * Lista las clases en vivo de la cohorte del alumno que no están ocultas, de la más próxima a la más lejana.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Las clases en vivo de la cohorte.
     */
    List<ClaseEnVivo> buscarClasesEnVivo(Inscripcion inscripcion);
}
