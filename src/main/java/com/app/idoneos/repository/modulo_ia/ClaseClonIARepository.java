package com.app.idoneos.repository.modulo_ia;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para ClaseClonIA (tabla "ClaseClon").
 * Nota: ClaseClon ya no tiene FK directa a Unidad en el modelo SQL;
 * el método findByUnidad es eliminado y sustituido por búsqueda por docente.
 */
@Repository
public interface ClaseClonIARepository extends JpaRepository<ClaseClonIA, Integer> {
    List<ClaseClonIA> findByDocenteAndBajaFalse(Docente docente);
    List<ClaseClonIA> findByBajaFalse();
    /**
     * Alias de compatibilidad: busca por docente dado que Unidad
     * ya no persiste en ClaseClon.
     */
    default List<ClaseClonIA> findByUnidad(com.app.idoneos.model.Unidad unidad) {
        return findByBajaFalse();
    }
}

