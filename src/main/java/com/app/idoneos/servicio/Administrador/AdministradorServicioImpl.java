package com.app.idoneos.servicio.Administrador;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.AdministradorRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Administrador}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AdministradorServicio}.
 */
@Service
public class AdministradorServicioImpl implements AdministradorServicio, CrudServicio<Administrador> {

    @Autowired
    private AdministradorRepositorio administradorRepositorio;

    /**
     * Guarda el administrador en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Administrador guardar(Administrador entidad) {
        return administradorRepositorio.save(entidad);
    }

    /**
     * Busca el administrador por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Administrador> buscarPorId(Integer id) {
        return administradorRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Administrador> obtenerTodo() {
        return administradorRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Administrador modificar(Administrador entidad) {
        return administradorRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Administrador entidad) {
        administradorRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return administradorRepositorio.existsById(id);
    }

    /**
     * Busca el perfil de administrador asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    @Override
    public Optional<Administrador> buscarPorUsuario(Usuario usuario) {
        return administradorRepositorio.findByUsuario(usuario);
    }
}
