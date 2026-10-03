package com.app.idoneos.servicio.TituloDocente;

import java.util.List;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.TituloDocente;

/**
 * Servicio para gestionar las operaciones relacionadas con el título del docente.
 */
public interface TituloDocenteServicio {

    /**
     * Busca los registros vigentes de tituloDocente asociados a docente.
     *
     * @param docente el registro de Docente asociado
     * @return una lista de TituloDocente asociados al Docente indicado
     */
    List<TituloDocente> buscarPorDocente(Docente docente);
}
