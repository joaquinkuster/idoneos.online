package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Inscripcion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Inscripcion}.
 */
@Repository
public interface InscripcionRepositorio extends JpaRepository<Inscripcion, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Inscripcion} activos.
     */
    List<Inscripcion> findByBajaFalse();

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    List<Inscripcion> findByCohorteAndBajaFalse(Cohorte cohorte);

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    List<Inscripcion> findByAlumnoAndBajaFalse(Alumno alumno);
}
