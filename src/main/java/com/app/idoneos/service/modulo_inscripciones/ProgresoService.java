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

public interface ProgresoService extends CrudService<Progreso> {

    List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion);

    Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad);

    Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad);

    int contarCompletadas(Inscripcion inscripcion);

    boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad);
}

