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

/**
 * Repositorio para acceder y gestionar la entidad {@link Inscripcion} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Inscripcion}.
 */
@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    /** Cantidad de inscripciones vigentes de cada cohorte (pares idCohorte, cantidad), en una sola consulta. */
    @Query("select i.cohorte.idCohorte, count(i) from Inscripcion i where i.baja = false group by i.cohorte.idCohorte")
    List<Object[]> contarPorCohorte();

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link Inscripcion} activos.
     */
    List<Inscripcion> findByBajaFalse();

    /**
     * Busca las inscripciones vigentes en un estado cuya cohorte ya cerró su inscripción (terminó antes del momento dado).
     *
     * @param estado El nombre del estado de la inscripción.
     * @param ahora  El momento de referencia.
     * @return Las inscripciones.
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.baja = false AND i.estadoInscripcion.nombre = :estado "
            + "AND i.cohorte.fechaFinInscripcion < :ahora")
    List<Inscripcion> buscarDeCohortesConLaInscripcionCerrada(@Param("estado") String estado,
            @Param("ahora") java.time.LocalDateTime ahora);

    /**
     * Cuenta las inscripciones de la cohorte que siguen en juego: pendientes de pago, con un pago en curso o con el acceso habilitado o suspendido
     * y la fecha de vencimiento por delante. Cuando es cero (y pasó el dictado), la cursada de la cohorte terminó.
     *
     * @param cohorte La cohorte.
     * @param ahora   El momento de referencia.
     * @return La cantidad de inscripciones en juego.
     */
    @Query("select count(i) from Inscripcion i where i.cohorte = :cohorte and i.baja = false and ("
            + "i.estadoInscripcion.nombre in ('Pendiente de pago', 'Pago en curso') or "
            + "(i.estadoInscripcion.nombre in ('Acceso habilitado', 'Acceso suspendido') "
            + "and (i.fechaVencimientoAcceso is null or i.fechaVencimientoAcceso >= :ahora)))")
    long contarEnJuego(@Param("cohorte") Cohorte cohorte, @Param("ahora") java.time.LocalDateTime ahora);

    /**
     * Busca las inscripciones vigentes en alguno de esos estados cuyo vencimiento del acceso ya pasó.
     *
     * @param estados Los nombres de los estados.
     * @param ahora   El momento de referencia.
     * @return Las inscripciones con el acceso por vencer.
     */
    @Query("SELECT i FROM Inscripcion i WHERE i.baja = false AND i.estadoInscripcion.nombre IN :estados "
            + "AND i.fechaVencimientoAcceso < :ahora")
    List<Inscripcion> buscarConElAccesoVencido(@Param("estados") java.util.Collection<String> estados,
            @Param("ahora") java.time.LocalDateTime ahora);

    /**
     * Busca los registros vigentes de inscripcion asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de Inscripcion asociados al Cohorte indicado
     */
    List<Inscripcion> findByCohorteAndBajaFalse(Cohorte cohorte);

    /**
     * Busca los registros vigentes de inscripcion asociados a alumno.
     *
     * @param alumno el registro de Alumno asociado
     * @return una lista de Inscripcion asociados al Alumno indicado
     */
    List<Inscripcion> findByAlumnoAndBajaFalse(Alumno alumno);

    /**
     * Verifica si un alumno tiene una inscripción activa (no dada de baja) a una cohorte.
     *
     * @param cohorte la cohorte
     * @param alumno el alumno
     * @return {@code true} si ya está inscripto
     */
    boolean existsByCohorteAndAlumnoAndBajaFalse(Cohorte cohorte, Alumno alumno);

    /**
     * Cuenta las inscripciones activas (no dadas de baja) asociadas a un curso, a través de su cohorte y programa.
     *
     * @param curso El curso a consultar.
     * @return La cantidad de inscripciones activas del curso.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso = :curso")
    long contarPorCurso(@Param("curso") Curso curso);

    /**
     * Cuenta las inscripciones activas (no dadas de baja) asociadas a una categoría, a través de los cursos de la categoría.
     *
     * @param categoria La categoría a consultar.
     * @return La cantidad de inscripciones activas de la categoría.
     */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.baja = false AND i.cohorte.programa.curso.categoria = :categoria")
    long contarPorCategoria(@Param("categoria") Categoria categoria);

    /**
     * Busca una inscripción bloqueándola hasta el fin de la transacción: sirve para que dos pedidos simultáneos del mismo
     * alumno (por ejemplo, dos clics en "Realizar intento") no registren dos intentos.
     *
     * @param id el identificador de la inscripción
     * @return la inscripción, si existe
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inscripcion i where i.idInscripcion = :id")
    java.util.Optional<Inscripcion> buscarPorIdConBloqueo(@Param("id") Integer id);

    /**
     * Cuenta las inscripciones vigentes de la cohorte que ocupan un lugar: habilitadas, suspendidas y con un pago en curso.
     * Se consulta en la base (no en la colección cargada) para que el conteo sea el real al tener bloqueada la cohorte.
     *
     * @param cohorte La cohorte.
     * @return La cantidad de lugares ocupados.
     */
    @Query("select count(i) from Inscripcion i where i.cohorte = :cohorte and i.baja = false "
            + "and i.estadoInscripcion.nombre in ('Acceso habilitado', 'Acceso suspendido', 'Acceso vencido', 'Pago en curso')")
    long contarQueOcupanCupo(@Param("cohorte") Cohorte cohorte);

    /**
     * Identificador de la cohorte de una inscripción, sin cargar la inscripción (para bloquear la cohorte antes de leerla).
     *
     * @param id Identificador de la inscripción.
     * @return El identificador de la cohorte, si la inscripción existe.
     */
    @Query("select i.cohorte.idCohorte from Inscripcion i where i.idInscripcion = :id")
    Optional<Integer> buscarIdCohorte(@Param("id") Integer id);
}
