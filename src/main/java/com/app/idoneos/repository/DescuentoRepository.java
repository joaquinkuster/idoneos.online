package com.app.idoneos.repository;

import com.app.idoneos.model.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Integer> {
    List<Descuento> findByBajaFalse();

    @Query("SELECT d FROM Descuento d WHERE d.baja = false AND (d.vigenciaHasta IS NULL OR d.vigenciaHasta >= CURRENT_TIMESTAMP)")
    List<Descuento> findVigentes();
}
