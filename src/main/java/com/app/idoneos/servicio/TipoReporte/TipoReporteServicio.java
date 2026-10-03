package com.app.idoneos.servicio.TipoReporte;

import java.util.Optional;
import com.app.idoneos.modelo.TipoReporte;

/**
 * Servicio para gestionar las operaciones relacionadas con el tipo de reporte.
 */
public interface TipoReporteServicio {

    /**
     * Busca el tipo de reporte por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<TipoReporte> buscarPorNombre(String nombre);
}
