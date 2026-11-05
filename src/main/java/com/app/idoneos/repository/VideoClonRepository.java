package com.app.idoneos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.VideoClon;

/**
 * Repositorio para acceder y gestionar la entidad {@link VideoClon} en la base de datos.
 */
@Repository
public interface VideoClonRepository extends JpaRepository<VideoClon, Integer> {
}
