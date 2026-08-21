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

/**
 * Servicio para la gestión del catálogo de cursos (CU-01 a CU-05).
 */
public interface CursoService extends CrudService<Curso> {

    List<Curso> buscarCursosPublicadosConFiltros(String nombre, Integer categoriaId, Integer modalidadId);

    Curso registrarCurso(Curso curso);

    Curso modificarCurso(Curso curso);

    Curso cambiarEstadoPublicacion(int cursoId, boolean publicar);

    void darDeBajaCurso(int cursoId);

    List<Curso> obtenerPublicados();

    List<Curso> obtenerPorCategoria(Categoria categoria);

    List<Curso> obtenerPorDocente(Usuario docente);

    List<Curso> buscarPorNombre(String query);
}

