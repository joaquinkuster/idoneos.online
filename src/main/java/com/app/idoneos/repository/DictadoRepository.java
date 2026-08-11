package com.app.idoneos.repository;

import com.app.idoneos.model.Dictado;
import com.app.idoneos.model.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictadoRepository extends JpaRepository<Dictado, Integer> {
    List<Dictado> findByPrograma(Programa programa);
}
