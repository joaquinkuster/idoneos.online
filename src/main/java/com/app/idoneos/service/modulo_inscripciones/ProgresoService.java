package com.app.idoneos.service.modulo_inscripciones;
import com.app.idoneos.service.modulo_reportes.*;

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

    /**
     * CU-44 paso 6 — Crea el primer Progreso para la inscripcion (primera unidad del cronograma).
     * Llamado automaticamente al completar la inscripcion de un alumno.
     */
    void registrarProgresoInicial(Inscripcion inscripcion);

    /**
     * CU-63 — Verifica si el alumno puede acceder a la unidad segun su progreso secuencial.
     * La primera unidad del cronograma siempre esta habilitada.
     * Las siguientes requieren que la unidad anterior este completada.
     */
    boolean esUnidadHabilitada(Inscripcion inscripcion, Unidad unidad);

    /**
     * CU-63 paso 10 — Crea el Progreso para la siguiente unidad del cronograma
     * tras que el alumno aprueba la actual.
     */
    void habilitarSiguienteUnidad(Inscripcion inscripcion, Unidad unidadAprobada);

    /**
     * CU-48 — Calcula el porcentaje de avance del alumno en el curso (0 a 100).
     * Se basa en las unidades completadas vs el total del cronograma del programa.
     */
    int calcularPorcentajeAvance(Inscripcion inscripcion);

    /**
     * CU-26 — Detecta si el alumno esta atrasado respecto al cronograma esperado.
     * Compara la semana actual de dictado con la ultima unidad completada del alumno.
     */
    boolean detectarAtraso(Inscripcion inscripcion);
}
