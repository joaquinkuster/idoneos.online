package com.app.idoneos.servicio.Inscripcion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Tema;

/**
 * Servicio para gestionar las operaciones relacionadas con la inscripción.
 */
public interface InscripcionServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    List<Inscripcion> buscarPorCohorte(Cohorte cohorte);

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    List<Inscripcion> buscarPorAlumno(Alumno alumno);

    /**
     * Busca las inscripciones vigentes de un alumno, filtrando opcionalmente por el nombre del curso
     * y por el estado de la inscripción (En Progreso o Finalizado).
     *
     * @param alumno el alumno dueño de las inscripciones
     * @param nombreCurso parte del nombre del curso (opcional)
     * @param estado el estado de la inscripción (opcional)
     * @return una lista de inscripciones que cumplen los criterios, de la más reciente a la más antigua
     */
    List<Inscripcion> buscarMisCursos(Alumno alumno, String nombreCurso, String estado);

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción: la inscripción debe ser suya y estar
     * vigente y habilitada, su acceso no debe haber vencido y, si la cohorte tiene fecha de inicio de dictado,
     * ésta no debe ser futura (CU-27).
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    Inscripcion validarAcceso(int idInscripcion, Alumno alumno);

    /**
     * Busca la inscripción con la que un alumno puede ver el contenido de un tema: una inscripción vigente a su
     * curso, con acceso vigente (ver {@link #validarAcceso}) y con el tema habilitado según su avance.
     *
     * @param alumno el alumno que quiere ver el contenido
     * @param tema el tema del contenido
     * @return la inscripción con acceso, o vacío si el alumno no puede ver el tema
     */
    Optional<Inscripcion> buscarConAccesoATema(Alumno alumno, Tema tema);

    /**
     * Busca las inscripciones activas (no dadas de baja) por curso, alumno y estado de acceso (CU-44, vista del
     * Administrador), de la más reciente a la más antigua.
     *
     * @param cursoId el curso (opcional)
     * @param alumno parte del nombre, el DNI o el correo del alumno (opcional)
     * @param estadoAcceso Vigente, Vencida, Pendiente de pago o Programada (opcional)
     * @return las inscripciones que cumplen los criterios
     */
    List<Inscripcion> buscarConFiltros(Integer cursoId, String alumno, String estadoAcceso);

    // ---------------------------------------------------------------- Creaciones

    /**
     * El alumno se inscribe a una cohorte con inscripción abierta (CU-45). Valida la ventana de inscripción, el cupo,
     * que no tenga ya una inscripción activa a la cohorte y que acepte los términos y condiciones. Registra el
     * progreso inicial sobre la primer tema del cronograma. Si el curso es gratuito la inscripción queda
     * habilitada; si tiene costo queda pendiente de pago.
     *
     * @param alumno el alumno que se inscribe
     * @param idCohorte la cohorte elegida
     * @param terminosAceptados si aceptó los términos y condiciones de las políticas de privacidad
     * @return la inscripción registrada
     * @throws IllegalArgumentException si no se cumple alguna regla
     */
    Inscripcion inscribir(Alumno alumno, Integer idCohorte, boolean terminosAceptados);

    /**
     * El Administrador registra manualmente la inscripción de un alumno (CU-46), con las mismas reglas que
     * {@link #inscribir}. La confirmación de los términos es la del administrador en nombre del alumno.
     *
     * @param idAlumno el alumno
     * @param idCohorte la cohorte
     * @param terminosConfirmados si el administrador confirmó que el alumno acepta los términos y condiciones
     * @return la inscripción registrada
     * @throws IllegalArgumentException si no se cumple alguna regla
     */
    Inscripcion registrarInscripcion(Integer idAlumno, Integer idCohorte, boolean terminosConfirmados);

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica las observaciones, la habilitación y la fecha de vencimiento del acceso de una inscripción activa
     * (CU-47), para correcciones administrativas.
     *
     * @param idInscripcion la inscripción
     * @param observaciones las observaciones (opcional)
     * @param habilitada si el acceso está habilitado (si es {@code null} no se cambia)
     * @param vencimiento el último día de acceso
     * @return la inscripción modificada
     * @throws IllegalArgumentException si la inscripción no está activa o el vencimiento es anterior al que resulta
     *         de la duración del cronograma
     */
    Inscripcion modificarInscripcion(Integer idInscripcion, String observaciones, Boolean habilitada,
            LocalDate vencimiento);

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja una inscripción activa (CU-49): da de baja su certificado y sus intentos de autoevaluación, deja
     * inaccesible su progreso y revoca el acceso; sus consultas de foro se muestran de forma anónima. No genera
     * reembolso.
     *
     * @param idInscripcion la inscripción
     * @param alumno el alumno que solicita la baja, o {@code null} si la solicita el Administrador
     * @param motivo el motivo u observación de la baja (opcional)
     * @throws IllegalArgumentException si la inscripción no está activa o no pertenece al alumno
     */
    void darDeBajaInscripcion(Integer idInscripcion, Alumno alumno, String motivo);

    /**
     * Da de baja varias inscripciones a la vez (solo el Administrador). Es de todo o nada: si alguna no puede darse de
     * baja, no se da de baja ninguna. Cada una sigue las reglas de {@link #darDeBajaInscripcion}.
     *
     * @param ids identificadores de las inscripciones
     * @throws IllegalArgumentException si no se indicó ninguna o alguna no puede darse de baja
     */
    void darDeBajaVarias(List<Integer> ids);
}
