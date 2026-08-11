package com.app.idoneos.repository;

import com.app.idoneos.model.Dictado;
import com.app.idoneos.model.DictadoDocente;
import com.app.idoneos.model.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DictadoDocenteRepository extends JpaRepository<DictadoDocente, Integer> {
    List<DictadoDocente> findByDictado(Dictado dictado);
    List<DictadoDocente> findByDocente(Docente docente);
    Optional<DictadoDocente> findByDictadoAndDocente(Dictado dictado, Docente docente);
    void deleteByDictado(Dictado dictado);
}
