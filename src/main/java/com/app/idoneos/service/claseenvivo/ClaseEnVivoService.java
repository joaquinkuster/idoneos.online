package com.app.idoneos.service.claseenvivo;

import com.app.idoneos.service.CrudService;
import java.util.List;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;

/**
 * Servicio para gestionar las operaciones relacionadas con la clase en vivo.
 */
public interface ClaseEnVivoService extends CrudService<ClaseEnVivo> {

    // ---------------------------------------------------------------- Lecturas




    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte);

}
