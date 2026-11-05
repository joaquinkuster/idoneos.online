package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.AutoevaluacionPool;

/**
 * Repositorio para acceder y gestionar la entidad {@link AutoevaluacionPool} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link AutoevaluacionPool}.
 */
@Repository
public interface AutoevaluacionPoolRepository extends JpaRepository<AutoevaluacionPool, Integer> {



}
