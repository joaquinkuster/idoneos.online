package com.app.idoneos.service.modulo_cursos;
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
 * TRAZABILIDAD — Servicio para la gestión del catálogo de categorías temáticas de cursos.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-07 — Buscar categoría: consulta de categorías activas por ID o nombre.
 *   CU-08 — Registrar categoría: alta de nueva categoría con validación de unicidad de nombre.
 *   CU-09 — Modificar categoría: edición de nombre y descripción.
 *   CU-10 — Dar de baja categoría: baja lógica con validación de cursos activos asociados.
 */
public interface CategoriaService extends CrudService<Categoria> {

    Optional<Categoria> buscarPorNombre(String nombre);

    List<Categoria> buscarPorNombreContiene(String texto);

    void darDeBaja(Integer id);
}


