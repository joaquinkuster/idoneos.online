package com.app.idoneos.servicio.Auditoria;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.TipoAccionAuditoria;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.AuditoriaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Auditoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AuditoriaServicio}.
 */
@Service
public class AuditoriaServicioImpl implements AuditoriaServicio, CrudServicio<Auditoria> {

    @Autowired
    private AuditoriaRepositorio auditoriaRepositorio;

    /**
     * Guarda la auditoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Auditoria guardar(Auditoria entidad) {
        return auditoriaRepositorio.save(entidad);
    }

    /**
     * Busca la auditoría por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Auditoria> buscarPorId(Integer id) {
        return auditoriaRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Auditoria> obtenerTodo() {
        return auditoriaRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Auditoria modificar(Auditoria entidad) {
        return auditoriaRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Auditoria entidad) {
        auditoriaRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return auditoriaRepositorio.existsById(id);
    }

    /**
     * Busca los registros de auditoria asociados a tipoAccionAuditoria.
     *
     * @param tipoAuditoria el registro de TipoAccionAuditoria asociado
     * @return una lista de Auditoria asociados al TipoAccionAuditoria indicado
     */
    @Override
    public List<Auditoria> buscarPorTipoAuditoria(TipoAccionAuditoria tipoAuditoria) {
        return auditoriaRepositorio.findByTipoAuditoria(tipoAuditoria);
    }

    /**
     * Busca los registros de auditoria asociados a usuario.
     *
     * @param usuario el registro de Usuario asociado
     * @return una lista de Auditoria asociados al Usuario indicado
     */
    @Override
    public List<Auditoria> buscarPorUsuario(Usuario usuario) {
        return auditoriaRepositorio.findByUsuario(usuario);
    }
}
