package com.app.idoneos.servicio.Rol;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.repositorio.RolRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Rol}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RolServicio}.
 */
@Service
public class RolServicioImpl implements RolServicio, CrudServicio<Rol> {

    @Autowired
    private RolRepositorio rolRepositorio;

    /**
     * Guarda el rol en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Rol guardar(Rol entidad) {
        return rolRepositorio.save(entidad);
    }

    /**
     * Busca el rol por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Rol> buscarPorId(Integer id) {
        return rolRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Rol> obtenerTodo() {
        return rolRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Rol modificar(Rol entidad) {
        return rolRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Rol entidad) {
        rolRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return rolRepositorio.existsById(id);
    }

    /**
     * Busca el rol por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepositorio.findByNombre(nombre);
    }
}
