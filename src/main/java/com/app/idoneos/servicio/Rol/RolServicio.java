package com.app.idoneos.servicio.Rol;

import java.util.Optional;
import com.app.idoneos.modelo.Rol;

/**
 * Servicio para gestionar las operaciones relacionadas con el rol.
 */
public interface RolServicio {

    /**
     * Busca el rol por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Rol> buscarPorNombre(String nombre);
}
