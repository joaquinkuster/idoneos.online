package com.app.idoneos.service.Programa;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Programa;
import com.app.idoneos.service.CrudService;

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
