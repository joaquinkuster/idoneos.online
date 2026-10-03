package com.app.idoneos.servicio.TerminoGlosario;

import java.util.List;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;

/**
 * Servicio para gestionar las operaciones relacionadas con el término del glosario.
 */
public interface TerminoGlosarioServicio {

    /**
     * Busca los registros vigentes de terminoGlosario asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de TerminoGlosario asociados al Unidad indicado
     */
    List<TerminoGlosario> buscarPorUnidad(Unidad unidad);
}
