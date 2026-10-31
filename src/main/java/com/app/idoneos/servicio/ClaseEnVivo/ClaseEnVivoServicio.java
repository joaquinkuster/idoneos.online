package com.app.idoneos.servicio.ClaseEnVivo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.modelo.Grabacion;
import com.app.idoneos.modelo.DocenteCurso;

/**
 * Servicio para gestionar las operaciones relacionadas con la clase en vivo.
 */
public interface ClaseEnVivoServicio {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca los registros vigentes de claseEnVivo asociados a docenteCurso.
     *
     * @param docenteCurso el registro de DocenteCurso asociado
     * @return una lista de ClaseEnVivo asociados al DocenteCurso indicado
     */
    List<ClaseEnVivo> buscarPorDocenteCurso(DocenteCurso docenteCurso);

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
     * @param grabacion el registro de Grabacion asociado
     * @return una lista de ClaseEnVivo asociados a la Grabacion indicada
     */
    List<ClaseEnVivo> buscarPorGrabacion(Grabacion grabacion);

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
