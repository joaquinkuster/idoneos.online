package com.app.idoneos.service.modalidad;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Modalidad;
import java.util.Optional;

/**
 * Servicio para gestionar las operaciones relacionadas con la modalidad.
 */
public interface ModalidadService extends CrudService<Modalidad> {

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la modalidad por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Modalidad> buscarPorNombre(String nombre);
}
