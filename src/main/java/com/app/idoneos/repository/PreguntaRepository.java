package com.app.idoneos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.idoneos.model.Pregunta;
import com.app.idoneos.model.Tema;

/** Repositorio de Pregunta. */
@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Integer> {

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Pregunta> findByBajaFalse();

    /**
     * Cuántas preguntas vigentes del tema salieron de cada oración: las de bancos sin baja y que no fallaron. Cada fila trae el id de la oración y la
     * cantidad.
     */
    @Query("""
            select p.oracionEvidencia.idOracion, count(p) from Pregunta p
            where p.pool.tema = :tema and p.baja = false and p.pool.baja = false and p.oracionEvidencia is not null
              and (p.pool.estadoGeneracionIa is null or p.pool.estadoGeneracionIa.nombre <> 'Fallido')
            group by p.oracionEvidencia.idOracion
            """)
    List<Object[]> contarEvidenciasDelTema(@Param("tema") Tema tema);

    /** Los enunciados de las preguntas activas de los demás pools activos del tema, para no repetirlas en un pool nuevo. */
    @Query("select p.texto from Pregunta p where p.pool.tema = :tema and p.pool.idPool <> :idPool and p.baja = false and p.pool.baja = false")
    List<String> findTextosDeOtrosPoolsDelTema(@Param("tema") Tema tema, @Param("idPool") int idPool);

}
