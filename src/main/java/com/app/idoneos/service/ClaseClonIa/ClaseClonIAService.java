package com.app.idoneos.service.IA;

import com.app.idoneos.model.ClaseClonIA;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Unidad;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de clases generadas con Clon IA del docente (CU-71 a CU-74).
 */
public interface ClaseClonIAService {

    Optional<ClaseClonIA> buscarPorId(Integer id);

    List<ClaseClonIA> obtenerPorUnidad(Unidad unidad);

    ClaseClonIA generarClaseClonIA(ClaseClonIA claseClon, Docente docente);

    ClaseClonIA modificarClaseClonIA(ClaseClonIA claseClon);

    void darDeBajaClaseClonIA(int claseClonId);
}
