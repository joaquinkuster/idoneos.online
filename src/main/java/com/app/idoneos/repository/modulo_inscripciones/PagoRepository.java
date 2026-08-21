package com.app.idoneos.repository.modulo_inscripciones;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.model.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /**
     * CU-88 — Generar informe de ingresos de un curso.
     * Pagos acreditados de un curso en un rango de fechas.
     * "Acreditado" es el estado que representa ingreso efectivo.
     */
    @Query("SELECT p FROM Pago p WHERE p.inscripcion.cohorte.programa.curso = :curso " +
           "AND p.estadoPago.nombre = 'Acreditado' AND p.fecha >= :desde AND p.fecha < :hasta")
    List<Pago> findAcreditadosByCursoAndFechaRange(
            @Param("curso") Curso curso,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-88 — Ingresos totales por curso (todos los cursos) en rango para comparación.
     * Devuelve Object[]{curso_id, suma_monto}.
     */
    @Query("SELECT p.inscripcion.cohorte.programa.curso.id, SUM(p.monto) FROM Pago p " +
           "WHERE p.estadoPago.nombre = 'Acreditado' AND p.fecha >= :desde AND p.fecha < :hasta " +
           "GROUP BY p.inscripcion.cohorte.programa.curso.id")
    List<Object[]> sumarIngresosPorCursoEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-88 — Ingresos por categoría de curso en rango.
     * Devuelve Object[]{categoria_nombre, suma_monto}.
     */
    @Query("SELECT p.inscripcion.cohorte.programa.curso.categoria.nombre, SUM(p.monto) FROM Pago p " +
           "WHERE p.estadoPago.nombre = 'Acreditado' AND p.fecha >= :desde AND p.fecha < :hasta " +
           "GROUP BY p.inscripcion.cohorte.programa.curso.categoria.nombre")
    List<Object[]> sumarIngresosPorCategoriaEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * CU-89 — Ingresos del mes (pagos acreditados en el mes indicado).
     */
    @Query("SELECT COALESCE(SUM(p.monto), 0.0) FROM Pago p " +
           "WHERE p.estadoPago.nombre = 'Acreditado' AND p.fecha >= :desde AND p.fecha < :hasta")
    Double sumarIngresosEnRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}

