package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.service.modulo_configuracion.CrudService;
import java.util.List;

public interface MaterialService extends CrudService<Material> {

    List<Material> obtenerPublicadosPorUnidad(Unidad unidad);

    List<Material> obtenerTodosPorUnidad(Unidad unidad);

    List<Material> obtenerPorUnidad(Unidad unidad);

    void darDeBaja(Integer id);

    void darDeBajaMaterial(int materialId);
}
