package com.app.idoneos.service.modulo_gestion_academica;

import com.app.idoneos.model.*;
import com.app.idoneos.service.modulo_configuracion.CrudService;
import java.util.List;
import java.util.Optional;

public interface GlosarioService extends CrudService<TerminoGlosario> {

    Optional<TerminoGlosario> buscarPorId(Integer id);

    List<TerminoGlosario> obtenerPorUnidad(Unidad unidad);

    TerminoGlosario registrarTermino(TerminoGlosario termino);

    TerminoGlosario modificarTermino(TerminoGlosario termino);

    void darDeBajaTermino(int terminoId);

    void darDeBaja(Integer id);
}
