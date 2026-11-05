package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Grabacion;
import com.app.idoneos.modelo.DocenteCurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ClaseEnVivo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ClaseEnVivo}.
 */
@Repository
public interface ClaseEnVivoRepositorio extends JpaRepository<ClaseEnVivo, Integer> {

    /** Cantidad de clases en vivo vigentes de cada cohorte (pares idCohorte, cantidad), en una sola consulta. */
    @org.springframework.data.jpa.repository.Query("select c.cohorte.idCohorte, count(c) from ClaseEnVivo c where c.baja = false and c.cohorte is not null group by c.cohorte.idCohorte")
    List<Object[]> contarVigentesPorCohorte();

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ClaseEnVivo} activos.
     */
    List<ClaseEnVivo> findByBajaFalse();

    /**
     * Busca los registros vigentes de claseEnVivo asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de ClaseEnVivo asociados al DocenteCurso indicado
     */
    List<ClaseEnVivo> findByDocenteCursoAndBajaFalse(DocenteCurso docenteCurso);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a estadoClaseEnVivo.
     *
     * @param estadoClaseEnVivo el registro de EstadoClaseEnVivo asociado
     * @return una lista de ClaseEnVivo asociados al EstadoClaseEnVivo indicado
     */
    List<ClaseEnVivo> findByEstadoClaseEnVivoAndBajaFalse(EstadoClaseEnVivo estadoClaseEnVivo);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a grabación.
     *
     * @param grabacion el registro de Grabacion asociado
     * @return una lista de ClaseEnVivo asociados a la Grabacion indicada
     */
    List<ClaseEnVivo> findByGrabacionAndBajaFalse(Grabacion grabacion);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    List<ClaseEnVivo> findByCohorteAndBajaFalse(Cohorte cohorte);

    /**
     * Busca la clase en vivo por su atributo único 'claveStream'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param claveStream el valor de 'claveStream' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<ClaseEnVivo> findByClaveStream(String claveStream);
}
