package com.app.ecomisiones.service.Material;

import com.app.ecomisiones.model.Material;
import com.app.ecomisiones.model.Unidad;
import com.app.ecomisiones.service.CrudService;

import java.util.List;

public interface MaterialService extends CrudService<Material> {

    List<Material> obtenerPublicadosPorUnidad(Unidad unidad);

    List<Material> obtenerTodosPorUnidad(Unidad unidad);
}
