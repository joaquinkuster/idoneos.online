package com.app.idoneos.service.modulo_cursos;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

/**
 * TRAZABILIDAD — Contrato de Servicio para la gestión pedagógica y comercial del catálogo de cursos.
 *
 * MOD-F-01: Módulo de Cursos
 *   CU-01 — Buscar curso (con filtros multicriterio y restricción por rol docente)
 *   CU-02 — Ver mis cursos (alumno)
 *   CU-03 — Registrar curso (admin)
 *   CU-04 — Modificar curso (admin con protección de datos si hay inscripciones)
 *   CU-05 — Dar de baja curso (admin con validación de dependencias activas)
 *   CU-06 — Explorar catálogo de cursos (público / alumno)
 */
public interface CursoService {

    Curso guardar(Curso curso);

    Curso modificar(Curso curso);

    Curso registrarCurso(Curso curso);

    Curso registrarCursoConEquipo(Curso curso, Integer docenteSupervisorId);

    Curso modificarCurso(Integer cursoId, String nombre, String descripcion, float precio, Integer categoriaId,
                         Integer docenteTitularId, Integer docenteSupervisorId, Integer nivelId, boolean emiteCertificado);

    void darDeBajaCurso(Integer idCurso);

    Optional<Curso> buscarPorId(int id);

    List<Curso> buscarPorNombre(String nombre);

    List<Curso> obtenerPorCategoria(Categoria categoria);

    List<Curso> obtenerPublicados();

    List<Curso> obtenerTodo();

    List<Curso> obtenerPorDocente(Usuario usuario);

    List<Curso> buscarCursosPorDocente(int docenteId);

    List<Curso> buscarCursosPublicadosConFiltros(String nombre, Integer categoriaId, Integer modalidadId);

    List<Curso> buscarCursosAdminConFiltros(String busqueda, Integer categoriaId, Integer nivelId, Integer docenteId, Boolean publicado);

    List<Curso> buscarCursosAdminConFiltros(String busqueda, Integer categoriaId, Integer nivelId, Integer docenteId, Boolean publicado, Boolean ordenBajasPrimero);
}
