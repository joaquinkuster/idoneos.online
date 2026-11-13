package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.EstadoClaseEnVivo;
import org.springframework.data.repository.query.Param;

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

    /** Busca las clases en vivo de un curso con su cohorte, su tema y su estado ya cargados, filtrando por curso y baja. */
    @Query("""
            select c from ClaseEnVivo c join fetch c.cohorte co join fetch co.curso cu join fetch c.tema t
            join fetch c.estadoClaseEnVivo
            where (cast(:idCurso as integer) is null or cu.idCurso = :idCurso)
              and (cast(:baja as boolean) is null or c.baja = :baja)
            """)
    List<ClaseEnVivo> buscarPorFiltros(@Param("idCurso") Integer idCurso, @Param("baja") Boolean baja);

    /** Busca las clases vigentes de un docente que ocupan su agenda: las que tienen alguno de los estados indicados. */
    @Query("""
            select c from ClaseEnVivo c
            where c.baja = false and c.docenteCurso.docente = :docente and c.estadoClaseEnVivo.nombre in :estados
            """)
    List<ClaseEnVivo> buscarOcupadasDelDocente(@Param("docente") Docente docente, @Param("estados") List<String> estados);

    /** Busca las clases vigentes que tienen un estado. */
    List<ClaseEnVivo> findByEstadoClaseEnVivoAndBajaFalse(EstadoClaseEnVivo estado);

    /** Busca las clases con una grabación que venció antes de la fecha indicada. */
    List<ClaseEnVivo> findByGrabacionIsNotNullAndFechaVencimientoGrabacionBefore(java.time.LocalDate fecha);
}
