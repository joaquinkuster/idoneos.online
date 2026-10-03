package com.app.idoneos.servicio.Categoria;

import java.util.List;
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

    /**
     * Busca categorías por nombre y, opcionalmente, según estén dadas de baja o vigentes.
     *
     * @param nombre parte del nombre de la categoría (opcional)
     * @param baja {@code true} para solo las dadas de baja, {@code false} para solo las vigentes, {@code null} para todas
     * @return una lista de categorías que cumplen los criterios
     */
    List<Categoria> buscarConFiltros(String nombre, Boolean baja);

    /**
     * Cuenta las inscripciones activas asociadas a una categoría, a través de sus cursos.
     *
     * @param categoria la categoría a consultar
     * @return la cantidad de inscripciones activas
     */
    long contarInscripcionesActivas(Categoria categoria);

    /**
     * Registra una nueva categoría. Si existe una categoría dada de baja con el mismo nombre, la reactiva.
     *
     * @param nombre el nombre de la categoría
     * @param descripcion la descripción de la categoría (opcional)
     * @return la categoría registrada
     * @throws IllegalArgumentException si el nombre está vacío o ya existe una categoría activa con ese nombre
     */
    Categoria registrarCategoria(String nombre, String descripcion);

    /**
     * Modifica el nombre y la descripción de una categoría activa sin inscripciones activas asociadas.
     *
     * @param idCategoria el identificador de la categoría
     * @param nombre el nuevo nombre
     * @param descripcion la nueva descripción (opcional)
     * @return la categoría modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    Categoria modificarCategoria(Integer idCategoria, String nombre, String descripcion);

    /**
     * Da de baja una categoría activa que no tenga cursos activos asociados.
     *
     * @param idCategoria el identificador de la categoría
     * @throws IllegalArgumentException si la categoría no está activa o tiene cursos activos asociados
     */
    void darDeBajaCategoria(Integer idCategoria);
}
