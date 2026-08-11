package com.app.idoneos.service.Dictado;

import com.app.idoneos.model.Dictado;
import com.app.idoneos.model.Programa;
import com.app.idoneos.service.CrudService;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de dictados de cursos (cronogramas de cursada).
 * Implementa las reglas de negocio de los Casos de Uso CU-15 a CU-18.
 */
public interface DictadoService extends CrudService<Dictado> {

    /**
     * CU-15 — Buscar dictado por ID.
     */
    Optional<Dictado> buscarPorId(Integer id);

    /**
     * CU-15 — Buscar dictados por programa.
     */
    List<Dictado> obtenerPorPrograma(Programa programa);

    /**
     * CU-16 — Registrar dictado.
     */
    Dictado registrarDictado(Dictado dictado);

    /**
     * CU-17 — Modificar dictado.
     */
    Dictado modificarDictado(Dictado dictado);

    /**
     * CU-18 — Eliminar dictado (Baja lógica).
     */
    void darDeBajaDictado(int dictadoId);
}
