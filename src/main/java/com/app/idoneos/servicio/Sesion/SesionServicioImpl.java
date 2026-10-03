package com.app.idoneos.servicio.Sesion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Sesion;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.SesionRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Sesion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link SesionServicio}.
 */
@Service
public class SesionServicioImpl implements SesionServicio, CrudServicio<Sesion> {

    @Autowired
    private SesionRepositorio sesionRepositorio;

    /**
     * Guarda la sesión en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Sesion guardar(Sesion entidad) {
        return sesionRepositorio.save(entidad);
    }

    /**
     * Busca la sesión por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Sesion> buscarPorId(Integer id) {
        return sesionRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Sesion> obtenerTodo() {
        return sesionRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Sesion modificar(Sesion entidad) {
        return sesionRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Sesion entidad) {
        sesionRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return sesionRepositorio.existsById(id);
    }

    /**
     * Busca los registros de sesion asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Sesion asociados al Usuario indicado
     */
    @Override
    public List<Sesion> buscarPorUsuario(Usuario usuario) {
        return sesionRepositorio.findByUsuario(usuario);
    }

    /**
     * Busca la sesión por su atributo único 'token'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param token el valor de 'token' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Sesion> buscarPorToken(String token) {
        return sesionRepositorio.findByToken(token);
    }
}
