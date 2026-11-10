package com.app.idoneos.service.modalidad;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Modalidad;
import java.util.Optional;

/** Servicio de la modalidad. */
public interface ModalidadService extends CrudService<Modalidad> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca la modalidad por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Modalidad> buscarPorNombre(String nombre);
}
