package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.AutoevaluacionPool;

/** Repositorio de AutoevaluacionPool. */
@Repository
public interface AutoevaluacionPoolRepository extends JpaRepository<AutoevaluacionPool, Integer> {

}
