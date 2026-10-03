package com.app.idoneos.servicio.EstadoClaseEnVivo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.EstadoClaseEnVivo;
import com.app.idoneos.repositorio.EstadoClaseEnVivoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link EstadoClaseEnVivo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link EstadoClaseEnVivoServicio}.
 */
@Service
public class EstadoClaseEnVivoServicioImpl implements EstadoClaseEnVivoServicio, CrudServicio<EstadoClaseEnVivo> {

    @Autowired
    private EstadoClaseEnVivoRepositorio estadoClaseEnVivoRepositorio;

    /**
     * Guarda el estado de la clase en vivo en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public EstadoClaseEnVivo guardar(EstadoClaseEnVivo entidad) {
        return estadoClaseEnVivoRepositorio.save(entidad);
    }

    /**
     * Busca el estado de la clase en vivo por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<EstadoClaseEnVivo> buscarPorId(Integer id) {
        return estadoClaseEnVivoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<EstadoClaseEnVivo> obtenerTodo() {
        return estadoClaseEnVivoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public EstadoClaseEnVivo modificar(EstadoClaseEnVivo entidad) {
        return estadoClaseEnVivoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(EstadoClaseEnVivo entidad) {
        estadoClaseEnVivoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return estadoClaseEnVivoRepositorio.existsById(id);
    }

    /**
     * Busca el estado de la clase en vivo por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<EstadoClaseEnVivo> buscarPorNombre(String nombre) {
        return estadoClaseEnVivoRepositorio.findByNombre(nombre);
    }
}
