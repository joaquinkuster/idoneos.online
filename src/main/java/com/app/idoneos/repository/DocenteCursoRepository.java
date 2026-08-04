package com.app.idoneos.repository;

import com.app.idoneos.model.DocenteCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocenteCursoRepository extends JpaRepository<DocenteCurso, Integer> {
}
