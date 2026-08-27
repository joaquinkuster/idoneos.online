package com.app.idoneos.repository.modulo_cursos;
import com.app.idoneos.service.modulo_reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByBajaFalseAndPublicadoTrue();

    List<Curso> findByBajaFalse();

    java.util.Optional<Curso> findByNombre(String nombre);

    List<Curso> findByCategoriaAndBajaFalseAndPublicadoTrue(Categoria categoria);

    List<Curso> findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(String query);

    /**
     * Cursos de un docente (titular o supervisor).
     */
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN Supervisor s ON s.curso = c " +
           "WHERE (c.docente.id = :docenteId OR s.docente.id = :docenteId) AND c.baja = false")
    List<Curso> findByDocenteId(int docenteId);

    /**
     * Cursos donde el docente es titular.
     */
    @Query("SELECT DISTINCT c FROM Curso c WHERE c.docente.id = :docenteId AND c.baja = false")
    List<Curso> findByDocenteTitularId(int docenteId);

    /**
     * CU-05: Buscar cursos publicados por modalidad de dictado.
     */
    @Query("SELECT DISTINCT mc.curso FROM ModalidadCurso mc WHERE mc.modalidad.id = :modalidadId AND mc.curso.baja = false AND mc.curso.publicado = true")
    List<Curso> findByModalidadIdAndPublicadoTrue(int modalidadId);

    /**
     * CU-01 / CU-05: Filtro multicriterio de cursos publicados por nombre, categoría y modalidad.
     */
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN ModalidadCurso mc ON mc.curso = c WHERE c.baja = false AND c.publicado = true " +
           "AND (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:categoriaId IS NULL OR c.categoria.id = :categoriaId) " +
           "AND (:modalidadId IS NULL OR mc.modalidad.id = :modalidadId)")
    List<Curso> buscarCursosPublicadosConFiltros(String nombre, Integer categoriaId, Integer modalidadId);
}

