package com.app.idoneos.servicio.Alumno;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.AlumnoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Alumno}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AlumnoServicio}.
 */
@Service
public class AlumnoServicioImpl implements AlumnoServicio, CrudServicio<Alumno> {

    @Autowired
    private AlumnoRepositorio alumnoRepositorio;

    /**
     * Guarda el alumno en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Alumno guardar(Alumno entidad) {
        return alumnoRepositorio.save(entidad);
    }

    /**
     * Busca el alumno por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Alumno> buscarPorId(Integer id) {
        return alumnoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Alumno> obtenerTodo() {
        return alumnoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Alumno modificar(Alumno entidad) {
        return alumnoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Alumno entidad) {
        alumnoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return alumnoRepositorio.existsById(id);
    }

    /**
     * Busca el perfil de alumno asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    @Override
    public Optional<Alumno> buscarPorUsuario(Usuario usuario) {
        return alumnoRepositorio.findByUsuario(usuario);
    }
}
