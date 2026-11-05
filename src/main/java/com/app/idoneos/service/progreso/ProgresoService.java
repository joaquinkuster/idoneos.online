package com.app.idoneos.service.progreso;

import com.app.idoneos.service.CrudService;
import com.app.idoneos.model.TemaCronograma;
import java.time.LocalDateTime;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Tema;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el progreso.
 */
public interface ProgresoService extends CrudService<Progreso> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros de progreso asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Progreso asociados al Tema indicado
     */
    List<Progreso> buscarPorTema(Tema tema);


    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Progreso> buscarPorInscripcionYTema(Inscripcion inscripcion, Tema tema);

    /**
     * CU-55: Busca las inscripciones de un curso con su progreso. Solo incluye inscripciones activas (no dadas de baja),
     * de todas las cohortes del curso, y permite filtrar por alumno.
     *
     * @param cursoId El identificador del curso.
     * @param alumno  Texto a buscar en el nombre, DNI o correo del alumno (opcional).
     * @return Las inscripciones que cumplen los criterios, de la más reciente a la más antigua.
     */
    List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno);

    /**
     * CU-55: Arma el detalle del progreso de una inscripción: cada tema del programa de su cohorte, si fue
     * completada y la fecha en que se completó.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @return El detalle del progreso.
     * @throws IllegalArgumentException Si la inscripción no existe o fue dada de baja.
     */
    DetalleProgreso buscarDetalle(Integer idInscripcion);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Registra el tema como completada para un alumno si la aprobó: tiene al menos una autoevaluación visible y aprobó
     * todas las autoevaluaciones que tiene visibles. Al completarla se habilita el siguiente tema del cronograma y, si con ella el alumno
     * completó todos los temas del programa, se emite su certificado de finalización. No hace nada si el tema ya
     * estaba completada.
     *
     * @param inscripcion la inscripción del alumno
     * @param tema el tema a verificar
     * @return {@code true} si el tema quedó completada con esta llamada
     */
    boolean actualizar(Inscripcion inscripcion, Tema tema);

    /**
     * Verifica el tema para cada alumno con inscripción vigente en un programa que la incluye: completa la de quienes ya
     * aprobaron todas las autoevaluaciones visibles que le quedan (por ejemplo, después de ocultar o dar de baja una).
     *
     * @param tema el tema cuyos autoevaluaciones cambiaron
     */
    void actualizarPorTema(Tema tema);

    /**
     * Revierte el tema a no completada para un alumno (por ejemplo, por fraude). Los temas posteriores ya completadas
     * conservan su estado pero quedan bloqueadas hasta que el alumno vuelva a completar esta. Si el alumno tenía un
     * certificado vigente, lo anula, porque el curso deja de estar completo.
     *
     * @param inscripcion la inscripción del alumno
     * @param tema el tema a revertir
     * @return {@code true} si el tema estaba completada
     */
    boolean revertir(Inscripcion inscripcion, Tema tema);

    /**
     * Detalle del progreso de un alumno (CU-55): cada tema del programa de su cohorte, si la completó y cuándo.
     * Lo arma {@link ProgresoService} y la plantilla lo recibe como atributo del modelo.
     *
     * @param inscripcion La inscripción del alumno.
     * @param temas    Los temas del programa de la cohorte, en orden de dictado.
     */
    public record DetalleProgreso(Inscripcion inscripcion, List<AvanceTema> temas) {

        /**
         * Avance del alumno en un tema del programa.
         *
         * @param tema          El tema del cronograma.
         * @param completada      Si el alumno la completó.
         * @param fechaCompletada Cuándo la completó; {@code null} si no la completó o no se registró la fecha.
         */
        public record AvanceTema(TemaCronograma tema, boolean completada, LocalDateTime fechaCompletada) {
        }
    }
}
