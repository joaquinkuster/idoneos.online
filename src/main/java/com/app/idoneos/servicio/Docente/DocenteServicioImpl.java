package com.app.idoneos.servicio.Docente;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.DocenteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Docente}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DocenteServicio}.
 */
@Service
public class DocenteServicioImpl implements DocenteServicio, CrudServicio<Docente> {

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    /**
     * Guarda el docente en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Docente guardar(Docente entidad) {
        return docenteRepositorio.save(entidad);
    }

    /**
     * Busca el docente por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Docente> buscarPorId(Integer id) {
        return docenteRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Docente> obtenerTodo() {
        return docenteRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Docente modificar(Docente entidad) {
        return docenteRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Docente entidad) {
        docenteRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return docenteRepositorio.existsById(id);
    }

    /**
     * Busca el perfil de docente asociado a un usuario.
     *
     * @param usuario el usuario del perfil
     * @return un {@link Optional} con el perfil si existe, o vacío si el usuario no tiene ese rol
     */
    @Override
    public Optional<Docente> buscarPorUsuario(Usuario usuario) {
        return docenteRepositorio.findByUsuario(usuario);
    }

    /**
     * Busca el docente por su atributo único 'avatarId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param avatarId el valor de 'avatarId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Docente> buscarPorAvatarId(String avatarId) {
        return docenteRepositorio.findByAvatarId(avatarId);
    }

    /**
     * Busca el docente por su atributo único 'matriculaCnv'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param matriculaCnv el valor de 'matriculaCnv' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Docente> buscarPorMatriculaCnv(String matriculaCnv) {
        return docenteRepositorio.findByMatriculaCnv(matriculaCnv);
    }

    /**
     * Busca el docente por su atributo único 'voiceId'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param voiceId el valor de 'voiceId' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Docente> buscarPorVoiceId(String voiceId) {
        return docenteRepositorio.findByVoiceId(voiceId);
    }
}
