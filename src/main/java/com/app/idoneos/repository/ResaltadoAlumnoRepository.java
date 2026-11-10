package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.ResaltadoAlumno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de ResaltadoAlumno. */
@Repository
public interface ResaltadoAlumnoRepository extends JpaRepository<ResaltadoAlumno, Integer> {

    /** Busca todas las páginas anotadas por un alumno en un documento. */
    List<ResaltadoAlumno> findByAlumnoAndRecursoOrderByPagina(Alumno alumno, Recurso recurso);

    /** Busca las anotaciones de un alumno en una página de un documento. */
    Optional<ResaltadoAlumno> findByAlumnoAndRecursoAndPagina(Alumno alumno, Recurso recurso, int pagina);
}
