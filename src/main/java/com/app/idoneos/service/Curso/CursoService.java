package com.app.idoneos.service.Curso;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.service.CrudService;

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
