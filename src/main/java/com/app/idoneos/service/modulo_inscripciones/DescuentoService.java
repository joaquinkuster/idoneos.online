package com.app.idoneos.service.modulo_inscripciones;

import com.app.idoneos.model.*;
import java.util.List;
import java.util.Optional;

public interface DescuentoService {

    Descuento registrarDescuento(String codigo, float porcentaje, String fechaInicio, String fechaFin, Integer cursoId);

    Descuento modificarDescuento(Integer id, String codigo, float porcentaje, String fechaInicio, String fechaFin, Integer cursoId);

    void darDeBajaDescuento(Integer id);

    Optional<Descuento> buscarPorId(Integer id);

    List<Descuento> obtenerTodos();

    List<Descuento> buscarDescuentosConFiltros(String codigo, Boolean vigente);
}
