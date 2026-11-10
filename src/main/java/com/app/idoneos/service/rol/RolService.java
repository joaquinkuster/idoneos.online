package com.app.idoneos.service.rol;

import com.app.idoneos.service.CrudService;

import com.app.idoneos.model.Rol;
import java.util.Optional;

/** Servicio del rol. */
public interface RolService extends CrudService<Rol> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca el rol por su atributo único 'nombre'.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Rol> buscarPorNombre(String nombre);
}
