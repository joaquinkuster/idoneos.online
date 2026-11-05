package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;

/**
 * Repositorio para acceder y gestionar la entidad {@link ClaseEnVivo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ClaseEnVivo}.
 */
@Repository
public interface ClaseEnVivoRepository extends JpaRepository<ClaseEnVivo, Integer> {

    /** Cantidad de clases en vivo vigentes de cada cohorte (pares idCohorte, cantidad), en una sola consulta. */
    @org.springframework.data.jpa.repository.Query("select c.cohorte.idCohorte, count(c) from ClaseEnVivo c where c.baja = false and c.cohorte is not null group by c.cohorte.idCohorte")
    List<Object[]> contarPorCohorte();

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ClaseEnVivo} activos.
     */
    List<ClaseEnVivo> findByBajaFalse();




    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    List<ClaseEnVivo> findByCohorteAndBajaFalse(Cohorte cohorte);

}
