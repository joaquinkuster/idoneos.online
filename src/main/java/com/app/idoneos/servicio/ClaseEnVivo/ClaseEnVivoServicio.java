package com.app.idoneos.servicio.ClaseEnVivo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;

/**
 * Servicio para gestionar las operaciones relacionadas con la clase en vivo.
 */
public interface ClaseEnVivoServicio {

    /**
     * Busca los registros vigentes de claseEnVivo asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de ClaseEnVivo asociados al ParticipacionDocente indicado
     */
    List<ClaseEnVivo> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a estadoClaseEnVivo.
     *
     * @param estadoClaseEnVivo el registro de EstadoClaseEnVivo asociado
     * @return una lista de ClaseEnVivo asociados al EstadoClaseEnVivo indicado
     */
    List<ClaseEnVivo> buscarPorEstadoClaseEnVivo(EstadoClaseEnVivo estadoClaseEnVivo);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a material.
     *
     * @param material el registro de Material asociado
     * @return una lista de ClaseEnVivo asociados al Material indicado
     */
    List<ClaseEnVivo> buscarPorMaterial(Material material);

    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte);

    /**
     * Busca la clase en vivo por su atributo único 'claveStream'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param claveStream el valor de 'claveStream' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<ClaseEnVivo> buscarPorClaveStream(String claveStream);
}
