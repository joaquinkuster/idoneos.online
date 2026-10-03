package com.app.idoneos.servicio.Categoria;

import java.util.Optional;
import com.app.idoneos.modelo.Categoria;

/**
 * Servicio para gestionar las operaciones relacionadas con la categoría.
 */
public interface CategoriaServicio {

    /**
     * Busca la categoría por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Categoria> buscarPorNombre(String nombre);
}
