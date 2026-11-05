package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.RecordatorioCohorte;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio de los recordatorios de cohortes próximas que marcan los alumnos.
 */
@Repository
public interface RecordatorioCohorteRepository extends JpaRepository<RecordatorioCohorte, Integer> {

    /** Busca el recordatorio de un alumno para una cohorte, esté vigente o quitado. */
    Optional<RecordatorioCohorte> findByAlumnoAndCohorte(Alumno alumno, Cohorte cohorte);

    /** Los recordatorios vigentes de un alumno. */
    List<RecordatorioCohorte> findByAlumnoAndBajaFalse(Alumno alumno);

    /** Los recordatorios vigentes de una cohorte. */
    List<RecordatorioCohorte> findByCohorteAndBajaFalse(Cohorte cohorte);

    /** Todos los recordatorios vigentes: los que revisa la tarea periódica. */
    List<RecordatorioCohorte> findByBajaFalse();
}
