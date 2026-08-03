package com.app.ecomisiones.service.Progreso;

import com.app.ecomisiones.model.Inscripcion;
import com.app.ecomisiones.model.Progreso;
import com.app.ecomisiones.model.Unidad;
import com.app.ecomisiones.service.CrudService;

import java.util.List;
import java.util.Optional;

public interface ProgresoService extends CrudService<Progreso> {

    List<Progreso> obtenerPorInscripcion(Inscripcion inscripcion);

    Optional<Progreso> obtenerPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad);

    Progreso marcarCompletada(Inscripcion inscripcion, Unidad unidad);

    int contarCompletadas(Inscripcion inscripcion);

    boolean unidadCompletada(Inscripcion inscripcion, Unidad unidad);
}
