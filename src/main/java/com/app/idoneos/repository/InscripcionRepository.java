package com.app.idoneos.repository;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.baja = false")
    List<Inscripcion> findByUsuarioAndBajaFalse(@Param("usuario") Usuario usuario);

    List<Inscripcion> findByAlumno(com.app.idoneos.model.Alumno alumno);

    @Query("SELECT i FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.dictado.programa.curso = :curso AND i.baja = false")
    Optional<Inscripcion> findByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.alumno.usuario = :usuario AND i.dictado.programa.curso = :curso AND i.baja = false")
    boolean existsByUsuarioAndCursoAndBajaFalse(@Param("usuario") Usuario usuario, @Param("curso") Curso curso);

    @Query("SELECT i FROM Inscripcion i WHERE i.dictado.programa.curso = :curso AND i.baja = false")
    List<Inscripcion> findByCursoAndBajaFalse(@Param("curso") Curso curso);

    /** Verificar si un curso tiene inscripciones vigentes — CU-04. */
    @Query("SELECT COUNT(i) > 0 FROM Inscripcion i WHERE i.dictado.programa.curso = :curso AND i.baja = false")
    boolean existsByCursoAndBajaFalse(@Param("curso") Curso curso);
}
