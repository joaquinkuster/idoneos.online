package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;

/** Repositorio de ClaseEnVivo. */
@Repository
public interface ClaseEnVivoRepository extends JpaRepository<ClaseEnVivo, Integer> {

    /** Cuenta las clases en vivo vigentes de cada cohorte, en una sola consulta (pares idCohorte, cantidad). */
    @org.springframework.data.jpa.repository.Query("select c.cohorte.idCohorte, count(c) from ClaseEnVivo c where c.baja = false and c.cohorte is not null group by c.cohorte.idCohorte")
    List<Object[]> contarPorCohorte();

    /** Encuentra todos los registros activos (no dados de baja). */
    List<ClaseEnVivo> findByBajaFalse();

    /** Busca los registros vigentes de claseEnVivo asociados a cohorte. */
    List<ClaseEnVivo> findByCohorteAndBajaFalse(Cohorte cohorte);

}
