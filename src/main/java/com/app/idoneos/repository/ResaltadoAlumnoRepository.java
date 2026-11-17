package com.app.idoneos.repository;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Documento;
import com.app.idoneos.model.ResaltadoAlumno;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de ResaltadoAlumno. */
@Repository
public interface ResaltadoAlumnoRepository extends JpaRepository<ResaltadoAlumno, ResaltadoAlumno.Clave> {

    /** Busca todas las páginas anotadas por un alumno en un documento. */
    List<ResaltadoAlumno> findByAlumnoAndDocumentoOrderByPagina(Alumno alumno, Documento documento);

    /** Busca las anotaciones de un alumno en una página de un documento. */
    Optional<ResaltadoAlumno> findByAlumnoAndDocumentoAndPagina(Alumno alumno, Documento documento, int pagina);
}
