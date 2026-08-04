package com.app.idoneos.service.Material;

import com.app.idoneos.model.Material;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.service.CrudService;

import java.util.List;

public interface MaterialService extends CrudService<Material> {

    List<Material> obtenerPublicadosPorUnidad(Unidad unidad);

    List<Material> obtenerTodosPorUnidad(Unidad unidad);
}
