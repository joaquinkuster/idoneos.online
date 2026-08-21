package com.app.idoneos.service.modulo_inscripciones;
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
 * Servicio para la gestión de cupones y promociones de descuento (CU-47 a CU-50).
 */
public interface DescuentoService extends CrudService<Descuento> {

    Optional<Descuento> buscarPorId(Integer id);

    Optional<Descuento> buscarPorCodigo(String codigo);

    List<Descuento> obtenerDescuentosVigentes();

    Descuento registrarDescuento(Descuento descuento);

    Descuento modificarDescuento(Descuento descuento);

    void darDeBajaDescuento(int descuentoId);
}

