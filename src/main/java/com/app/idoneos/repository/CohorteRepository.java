package com.app.idoneos.repository;

import java.util.Collection;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio de Cohorte. */
@Repository
public interface CohorteRepository extends JpaRepository<Cohorte, Integer> {

    /** Busca todas las cohortes con su curso, programa y estado ya cargados. */
    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "curso", "programa", "estadoCohorte" })
    List<Cohorte> findAll();

    /** Busca las cohortes de los cursos indicados, con su curso, programa y estado ya cargados. */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "curso", "programa", "estadoCohorte" })
    List<Cohorte> findByCursoIn(java.util.Collection<Curso> cursos);

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Cohorte> findByBajaFalse();

    /** Busca los registros vigentes de cohorte asociados a programa. */
    List<Cohorte> findByProgramaAndBajaFalse(Programa programa);

    /** Busca las cohortes vigentes del curso, tengan o no programa. */
    List<Cohorte> findByCursoAndBajaFalse(Curso curso);

    /** Trae la cohorte bloqueándola (escritura) hasta que termine la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cohorte c where c.idCohorte = :id")
    Optional<Cohorte> buscarPorIdConBloqueo(@Param("id") Integer id);
}
