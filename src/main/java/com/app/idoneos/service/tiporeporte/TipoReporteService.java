package com.app.idoneos.service.tiporeporte;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.TipoReporte;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el tipo de reporte.
 */
public interface TipoReporteService extends CrudService<TipoReporte> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el tipo de reporte por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoReporte> buscarPorNombre(String nombre);
}
