package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Categoria;
import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByBajaFalseAndPublicadoTrue();

    List<Curso> findByBajaFalse();

    List<Curso> findByCategoriaAndBajaFalseAndPublicadoTrue(Categoria categoria);

    List<Curso> findByDocenteTitularAndBajaFalse(Usuario docente);

    List<Curso> findByNombreContainingIgnoreCaseAndBajaFalseAndPublicadoTrue(String query);
}
