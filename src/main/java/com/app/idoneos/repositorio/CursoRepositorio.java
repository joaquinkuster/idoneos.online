package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Curso} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Curso}.
 */
@Repository
public interface CursoRepositorio extends JpaRepository<Curso, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Curso} activos.
     */
    List<Curso> findByBajaFalse();

    /**
     * Busca los registros vigentes de curso asociados a nivel.
     *
     * @param nivel el registro de Nivel asociado
     * @return una lista de Curso asociados al Nivel indicado
     */
    List<Curso> findByNivelAndBajaFalse(Nivel nivel);

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    List<Curso> findByCategoriaAndBajaFalse(Categoria categoria);

    /**
     * Busca el curso por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Curso> findByNombre(String nombre);
}
