package com.app.idoneos.servicio.Cohorte;

import java.util.List;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Programa;

/**
 * Servicio para gestionar las operaciones relacionadas con la cohorte.
 */
public interface CohorteServicio {

    /**
     * Busca los registros vigentes de cohorte asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de Cohorte asociados al Programa indicado
     */
    List<Cohorte> buscarPorPrograma(Programa programa);
}
