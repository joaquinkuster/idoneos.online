package com.app.idoneos.service.Glosario;

import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.service.CrudService;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de términos del glosario técnico (CU-29 a CU-32).
 */
public interface GlosarioService extends CrudService<TerminoGlosario> {

    /**
     * CU-29 — Buscar término de glosario por ID.
     */
    Optional<TerminoGlosario> buscarPorId(Integer id);

    /**
     * CU-29 — Obtener términos por unidad temática.
     */
    List<TerminoGlosario> obtenerPorUnidad(Unidad unidad);

    /**
     * CU-30 — Registrar término de glosario.
     */
    TerminoGlosario registrarTermino(TerminoGlosario termino);

    /**
     * CU-31 — Modificar término de glosario.
     */
    TerminoGlosario modificarTermino(TerminoGlosario termino);

    /**
     * CU-32 — Eliminar término de glosario (Baja lógica).
     */
    void darDeBajaTermino(int terminoId);
}
