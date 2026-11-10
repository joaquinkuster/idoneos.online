package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Sesion;

/** Repositorio de Sesion. */
@Repository
public interface SesionRepository extends JpaRepository<Sesion, Integer> {

}
