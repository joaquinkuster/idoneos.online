package com.app.idoneos.servicio.RolUsuario;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.RolUsuario;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.RolUsuarioRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RolUsuario}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RolUsuarioServicio}.
 */
@Service
public class RolUsuarioServicioImpl implements RolUsuarioServicio, CrudServicio<RolUsuario> {

    @Autowired
    private RolUsuarioRepositorio rolUsuarioRepositorio;

    /**
     * Guarda la asignación de rol en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public RolUsuario guardar(RolUsuario entidad) {
        return rolUsuarioRepositorio.save(entidad);
    }

    /**
     * Busca la asignación de rol por su identificador. Incluye los registros dados de baja o anulados, para poder consultarlos.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<RolUsuario> buscarPorId(Integer id) {
        return rolUsuarioRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<RolUsuario> obtenerTodo() {
        return rolUsuarioRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public RolUsuario modificar(RolUsuario entidad) {
        return rolUsuarioRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(RolUsuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        rolUsuarioRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return rolUsuarioRepositorio.existsById(id);
    }

    /**
     * Busca los registros vigentes de rolUsuario asociados a rol.
     *
     * @param rol el registro de Rol asociado
     * @return una lista de RolUsuario asociados al Rol indicado
     */
    @Override
    public List<RolUsuario> buscarPorRol(Rol rol) {
        return rolUsuarioRepositorio.findByRolAndBajaFalse(rol);
    }

    /**
     * Busca los registros vigentes de rolUsuario asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de RolUsuario asociados al Usuario indicado
     */
    @Override
    public List<RolUsuario> buscarPorUsuario(Usuario usuario) {
        return rolUsuarioRepositorio.findByUsuarioAndBajaFalse(usuario);
    }

    /**
     * Busca la asignación de rol a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param rol el registro de Rol asociado
     * @param usuario el registro de Usuario asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<RolUsuario> buscarPorRolYUsuario(Rol rol, Usuario usuario) {
        return rolUsuarioRepositorio.findByRolAndUsuario(rol, usuario);
    }
}
