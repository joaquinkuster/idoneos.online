package com.app.idoneos.servicio.Categoria;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.repositorio.CategoriaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Categoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CategoriaServicio}.
 */
@Service
public class CategoriaServicioImpl implements CategoriaServicio, CrudServicio<Categoria> {

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    /**
     * Guarda la categoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Categoria guardar(Categoria entidad) {
        return categoriaRepositorio.save(entidad);
    }

    /**
     * Busca la categoría por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Categoria> obtenerTodo() {
        return categoriaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Categoria modificar(Categoria entidad) {
        return categoriaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Categoria entidad) {
        entidad.marcarInactivo(); // Baja lógica
        categoriaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return categoriaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca la categoría por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepositorio.findByNombre(nombre);
    }
}
