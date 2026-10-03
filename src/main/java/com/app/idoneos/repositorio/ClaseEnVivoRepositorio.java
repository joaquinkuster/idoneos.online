package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link ClaseEnVivo} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link ClaseEnVivo}.
 */
@Repository
public interface ClaseEnVivoRepositorio extends JpaRepository<ClaseEnVivo, Integer> {

    /**
     * Encuentra todos los registros activos (no dados de baja).
     *
     * @return Una lista de {@link ClaseEnVivo} activos.
     */
    List<ClaseEnVivo> findByBajaFalse();

    /**
     * Busca los registros vigentes de claseEnVivo asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseEnVivo asociados al ParticipacionDocente indicado
     */
    List<ClaseEnVivo> findByParticipacionDocenteAndBajaFalse(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a estadoClaseEnVivo.
     *
     * @param estadoClaseEnVivo el registro de EstadoClaseEnVivo asociado
     * @return una lista de ClaseEnVivo asociados al EstadoClaseEnVivo indicado
     */
    List<ClaseEnVivo> findByEstadoClaseEnVivoAndBajaFalse(EstadoClaseEnVivo estadoClaseEnVivo);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseEnVivo asociados al Material indicado
     */
    List<ClaseEnVivo> findByMaterialAndBajaFalse(Material material);

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
