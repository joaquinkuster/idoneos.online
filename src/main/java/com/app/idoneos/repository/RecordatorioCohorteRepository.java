package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.RecordatorioCohorte;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de RecordatorioCohorte. */
@Repository
public interface RecordatorioCohorteRepository extends JpaRepository<RecordatorioCohorte, Integer> {

    /** Busca el recordatorio de un alumno para una cohorte, esté vigente o quitado. */
    Optional<RecordatorioCohorte> findByAlumnoAndCohorte(Alumno alumno, Cohorte cohorte);

    /** Busca los recordatorios vigentes de un alumno. */
    List<RecordatorioCohorte> findByAlumnoAndBajaFalse(Alumno alumno);

    /** Busca los recordatorios vigentes de una cohorte. */
    List<RecordatorioCohorte> findByCohorteAndBajaFalse(Cohorte cohorte);

    /** Busca todos los recordatorios vigentes, que son los que revisa la tarea periódica. */
    List<RecordatorioCohorte> findByBajaFalse();
}
