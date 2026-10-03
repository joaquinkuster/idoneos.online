package com.app.idoneos.servicio.Usuario;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Rol;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.UsuarioRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Usuario}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link UsuarioServicio}.
 */
@Service
public class UsuarioServicioImpl implements UsuarioServicio, CrudServicio<Usuario> {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    /**
     * Guarda el usuario en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Usuario guardar(Usuario entidad) {
        return usuarioRepositorio.save(entidad);
    }

    /**
     * Busca el usuario por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Usuario> obtenerTodo() {
        return usuarioRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Usuario modificar(Usuario entidad) {
        return usuarioRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Usuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        usuarioRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return usuarioRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de usuario asociados a rol.
     *
     * @param rolPorDefecto el registro de Rol asociado
     * @return una lista de Usuario asociados al Rol indicado
     */
    @Override
    public List<Usuario> buscarPorRolPorDefecto(Rol rolPorDefecto) {
        return usuarioRepositorio.findByRolPorDefectoAndBajaFalse(rolPorDefecto);
    }

    /**
     * Busca el usuario por su atributo único 'dni'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param dni el valor de 'dni' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Usuario> buscarPorDni(String dni) {
        return usuarioRepositorio.findByDni(dni);
    }

    /**
     * Busca el usuario por su atributo único 'email'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param email el valor de 'email' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email);
    }

    /**
     * Busca el usuario por su atributo único 'googleId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param googleId el valor de 'googleId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Usuario> buscarPorGoogleId(String googleId) {
        return usuarioRepositorio.findByGoogleId(googleId);
    }

    /**
     * Busca el usuario por su atributo único 'tokenVerificacion'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param tokenVerificacion el valor de 'tokenVerificacion' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Usuario> buscarPorTokenVerificacion(String tokenVerificacion) {
        return usuarioRepositorio.findByTokenVerificacion(tokenVerificacion);
    }
}
