package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Recurso;
import com.app.idoneos.model.ResaltadoAlumno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ResaltadoAlumno} en la base de datos.
 */
@Repository
public interface ResaltadoAlumnoRepository extends JpaRepository<ResaltadoAlumno, Integer> {

    /**
     * Todas las páginas anotadas por un alumno en un documento.
     *
     * @param alumno  el alumno
     * @param recurso el documento
     * @return las anotaciones, una por página
     */
    List<ResaltadoAlumno> findByAlumnoAndRecursoOrderByPagina(Alumno alumno, Recurso recurso);

    /**
     * Las anotaciones de un alumno en una página de un documento.
     *
     * @param alumno  el alumno
     * @param recurso el documento
     * @param pagina  la página
     * @return las anotaciones, si las hay
     */
    Optional<ResaltadoAlumno> findByAlumnoAndRecursoAndPagina(Alumno alumno, Recurso recurso, int pagina);
}
