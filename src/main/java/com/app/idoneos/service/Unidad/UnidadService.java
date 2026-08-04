package com.app.idoneos.service.Unidad;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.service.CrudService;

import java.util.List;

public interface UnidadService extends CrudService<Unidad> {

    List<Unidad> obtenerPorCurso(Curso curso);

    int contarUnidadesPorCurso(Curso curso);
}
