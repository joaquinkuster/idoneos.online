package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Autoevaluacion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Autoevaluacion}.
 */
@Repository
public interface AutoevaluacionRepositorio extends JpaRepository<Autoevaluacion, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Autoevaluacion} activos.
     */
    List<Autoevaluacion> findByBajaFalse();

    /**
     * Busca los registros vigentes de autoevaluacion asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de Autoevaluacion asociados al Tema indicado
     */
    List<Autoevaluacion> findByTemaAndBajaFalse(Tema tema);
}
