package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.DocenteCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocenteCursoRepository extends JpaRepository<DocenteCurso, Integer> {
}
