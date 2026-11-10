package com.app.idoneos.repository;

import java.util.Optional;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositorio de Inscripcion. */
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    /** Cuenta las inscripciones vigentes de cada cohorte, en una sola consulta (pares idCohorte, cantidad). */
    @Query("select i.cohorte.idCohorte, count(i) from Inscripcion i where i.baja = false group by i.cohorte.idCohorte")
    List<Object[]> contarPorCohorte();

    /** Encuentra todos los registros activos (no dados de baja). */
    List<Inscripcion> findByBajaFalse();

    /** Busca las inscripciones vigentes en un estado cuya cohorte ya cerró su inscripción (terminó antes del momento dado). */
    @Query("SELECT i FROM Inscripcion i WHERE i.baja = false AND i.estadoInscripcion.nombre = :estado "
            + "AND i.cohorte.fechaFinInscripcion < :ahora")
    List<Inscripcion> buscarDeCohortesConLaInscripcionCerrada(@Param("estado") String estado,
            @Param("ahora") java.time.LocalDateTime ahora);

    /** Cuenta las inscripciones de la cohorte que siguen en juego: pendientes de pago. */
    @Query("select count(i) from Inscripcion i where i.cohorte = :cohorte and i.baja = false and ("
            + "i.estadoInscripcion.nombre in ('Pendiente de pago', 'Pago en curso') or "
            + "(i.estadoInscripcion.nombre in ('Acceso habilitado', 'Acceso suspendido') "
            + "and (i.fechaVencimientoAcceso is null or i.fechaVencimientoAcceso >= :ahora)))")
    long contarEnJuego(@Param("cohorte") Cohorte cohorte, @Param("ahora") java.time.LocalDateTime ahora);

    /** Busca las inscripciones vigentes en alguno de esos estados cuyo vencimiento del acceso ya pasó. */
    @Query("SELECT i FROM Inscripcion i WHERE i.baja = false AND i.estadoInscripcion.nombre IN :estados "
            + "AND i.fechaVencimientoAcceso < :ahora")
    List<Inscripcion> buscarConElAccesoVencido(@Param("estados") java.util.Collection<String> estados,
            @Param("ahora") java.time.LocalDateTime ahora);

    /** Busca los registros vigentes de inscripcion asociados a cohorte. */
    List<Inscripcion> findByCohorteAndBajaFalse(Cohorte cohorte);

    /** Busca los registros vigentes de inscripcion asociados a alumno. */
    List<Inscripcion> findByAlumnoAndBajaFalse(Alumno alumno);

    /** Verifica si un alumno tiene una inscripción activa (no dada de baja) a una cohorte. */
    boolean existsByCohorteAndAlumnoAndBajaFalse(Cohorte cohorte, Alumno alumno);

    /** Cuenta las inscripciones activas (no dadas de baja) asociadas a un curso. */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso = :curso")
    long contarPorCurso(@Param("curso") Curso curso);

    /** Cuenta las inscripciones activas (no dadas de baja) asociadas a una categoría. */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso.categoria = :categoria")
    long contarPorCategoria(@Param("categoria") Categoria categoria);

    /** Busca una inscripción bloqueándola hasta el fin de la transacción. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inscripcion i where i.idInscripcion = :id")
    java.util.Optional<Inscripcion> buscarPorIdConBloqueo(@Param("id") Integer id);

    /** Cuenta las inscripciones vigentes de la cohorte que ocupan un lugar: habilitadas. */
    @Query("select count(i) from Inscripcion i where i.cohorte = :cohorte and i.baja = false "
            + "and i.estadoInscripcion.nombre in ('Acceso habilitado', 'Acceso suspendido', 'Acceso vencido', 'Pago en curso')")
    long contarQueOcupanCupo(@Param("cohorte") Cohorte cohorte);

    /** Busca el identificador de la cohorte de una inscripción. */
    @Query("select i.cohorte.idCohorte from Inscripcion i where i.idInscripcion = :id")
    Optional<Integer> buscarIdCohorte(@Param("id") Integer id);
}
