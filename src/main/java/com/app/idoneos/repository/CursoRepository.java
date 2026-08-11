package com.app.idoneos.repository;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByBajaFalseAndPublicadoTrue();

    List<Curso> findByBajaFalse();

    List<Curso> findByCategoriaAndBajaFalseAndPublicadoTrue(Categoria categoria);

    List<Curso> findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(String query);

    /**
     * Cursos de un docente (titular o supervisor) navegando DictadoDocente -> Dictado -> Programa -> Curso.
     */
    @Query("SELECT DISTINCT dd.dictado.programa.curso FROM DictadoDocente dd WHERE dd.docente.id = :docenteId AND dd.dictado.programa.curso.baja = false")
    List<Curso> findByDocenteId(int docenteId);

    /**
     * Cursos donde el docente es titular (esSupervisor = false).
     */
    @Query("SELECT DISTINCT dd.dictado.programa.curso FROM DictadoDocente dd WHERE dd.docente.id = :docenteId AND dd.esSupervisor = false AND dd.dictado.programa.curso.baja = false")
    List<Curso> findByDocenteTitularId(int docenteId);
}
