package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Administrador;

/** Repositorio de Administrador. */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

}
