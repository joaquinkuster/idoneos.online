package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.service.modulo_configuracion.CrudService;
import java.util.List;

public interface UnidadService extends CrudService<Unidad> {

    List<Unidad> obtenerPorCurso(Curso curso);

    int contarUnidadesPorCurso(Curso curso);

    void darDeBaja(Integer id);
}
