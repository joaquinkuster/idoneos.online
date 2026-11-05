package com.app.idoneos.service.nivel;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Nivel;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con el nivel.
 */
public interface NivelService extends CrudService<Nivel> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el nivel por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Nivel> buscarPorNombre(String nombre);
}
