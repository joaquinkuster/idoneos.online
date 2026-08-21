package com.app.idoneos.service.modulo_gestion_academica;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

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

