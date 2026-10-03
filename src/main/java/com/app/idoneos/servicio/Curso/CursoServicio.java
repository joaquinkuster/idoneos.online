package com.app.idoneos.servicio.Curso;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;

/**
 * Servicio para gestionar las operaciones relacionadas con el curso.
 */
public interface CursoServicio {

    /**
     * Busca los registros vigentes de curso asociados a nivel.
     *
     * @param nivel el registro de Nivel asociado
     * @return una lista de Curso asociados al Nivel indicado
     */
    List<Curso> buscarPorNivel(Nivel nivel);

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    List<Curso> buscarPorCategoria(Categoria categoria);

    /**
     * Busca el curso por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    Optional<Curso> buscarPorNombre(String nombre);
}
