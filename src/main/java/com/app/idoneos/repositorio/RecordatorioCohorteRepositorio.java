package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.RecordatorioCohorte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de los recordatorios de cohortes próximas que marcan los alumnos.
 */
@Repository
public interface RecordatorioCohorteRepositorio extends JpaRepository<RecordatorioCohorte, Integer> {

    /** Busca el recordatorio de un alumno para una cohorte, esté vigente o quitado. */
    Optional<RecordatorioCohorte> findByAlumnoAndCohorte(Alumno alumno, Cohorte cohorte);

    /** Los recordatorios vigentes de un alumno. */
    List<RecordatorioCohorte> findByAlumnoAndBajaFalse(Alumno alumno);

    /** Los recordatorios vigentes de una cohorte. */
    List<RecordatorioCohorte> findByCohorteAndBajaFalse(Cohorte cohorte);

    /** Todos los recordatorios vigentes: los que revisa la tarea periódica. */
    List<RecordatorioCohorte> findByBajaFalse();
}
