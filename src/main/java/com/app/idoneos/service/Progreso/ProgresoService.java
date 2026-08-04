package com.app.idoneos.service.Progreso;

import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Progreso;
import com.app.idoneos.model.Unidad;
import com.app.idoneos.service.CrudService;

import java.util.List;
import java.util.Optional;

public interface ProgresoService extends CrudService<Progreso> {

    List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion);

    Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad);

    Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad);

    int contarCompletadas(Inscripcion inscripcion);

    boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad);
}
