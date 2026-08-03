package com.app.ecomisiones.service.Unidad;

import com.app.ecomisiones.model.Curso;
import com.app.ecomisiones.model.Unidad;
import com.app.ecomisiones.service.CrudService;

import java.util.List;

public interface UnidadService extends CrudService<Unidad> {

    List<Unidad> obtenerPorCurso(Curso curso);

    int contarUnidadesPorCurso(Curso curso);
}
