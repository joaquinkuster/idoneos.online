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
 * Servicio para la gestión de programas de estudio (planes de estudio de cursos).
 * Implementa las reglas de negocio de los Casos de Uso CU-10 a CU-14.
 */
public interface ProgramaService extends CrudService<Programa> {

    /**
     * CU-10 — Buscar programa por ID.
     */
    Optional<Programa> buscarPorId(Integer id);

    /**
     * CU-10 — Buscar programas por curso.
     */
    List<Programa> obtenerPorCurso(Curso curso);

    /**
     * CU-11 — Registrar programa.
     */
    Programa registrarPrograma(Programa programa);

    /**
     * CU-12 — Modificar programa.
     */
    Programa modificarPrograma(Programa programa);

    /**
     * CU-13 — Eliminar programa (Baja lógica).
     */
    void darDeBajaPrograma(int programaId);

    /**
     * CU-14 — Cambiar programa activo del curso.
     */
    Programa cambiarProgramaActivo(int programaId);
}

