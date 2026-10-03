package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Cohorte} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Cohorte}.
 */
@Repository
public interface CohorteRepositorio extends JpaRepository<Cohorte, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Cohorte} activos.
     */
    List<Cohorte> findByBajaFalse();

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    List<Cohorte> findByProgramaAndBajaFalse(Programa programa);
}
