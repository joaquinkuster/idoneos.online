package com.app.idoneos.servicio.EstadoClaseClon;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.EstadoClaseClon;
import com.app.idoneos.repositorio.EstadoClaseClonRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link EstadoClaseClon}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link EstadoClaseClonServicio}.
 */
@Service
public class EstadoClaseClonServicioImpl implements EstadoClaseClonServicio, CrudServicio<EstadoClaseClon> {

    @Autowired
    private EstadoClaseClonRepositorio estadoClaseClonRepositorio;

    /**
     * Guarda el estado de la clase con clon en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public EstadoClaseClon guardar(EstadoClaseClon entidad) {
        return estadoClaseClonRepositorio.save(entidad);
    }

    /**
     * Busca el estado de la clase con clon por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<EstadoClaseClon> buscarPorId(Integer id) {
        return estadoClaseClonRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<EstadoClaseClon> obtenerTodo() {
        return estadoClaseClonRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public EstadoClaseClon modificar(EstadoClaseClon entidad) {
        return estadoClaseClonRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(EstadoClaseClon entidad) {
        estadoClaseClonRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return estadoClaseClonRepositorio.existsById(id);
    }

    /**
     * Busca el estado de la clase con clon por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<EstadoClaseClon> buscarPorNombre(String nombre) {
        return estadoClaseClonRepositorio.findByNombre(nombre);
    }
}
