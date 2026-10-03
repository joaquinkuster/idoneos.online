package com.app.idoneos.servicio.DetalleAuditoria;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Auditoria;
import com.app.idoneos.modelo.DetalleAuditoria;
import com.app.idoneos.repositorio.DetalleAuditoriaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link DetalleAuditoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DetalleAuditoriaServicio}.
 */
@Service
public class DetalleAuditoriaServicioImpl implements DetalleAuditoriaServicio, CrudServicio<DetalleAuditoria> {

    @Autowired
    private DetalleAuditoriaRepositorio detalleAuditoriaRepositorio;

    /**
     * Guarda el detalle de auditoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public DetalleAuditoria guardar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepositorio.save(entidad);
    }

    /**
     * Busca el detalle de auditoría por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<DetalleAuditoria> buscarPorId(Integer id) {
        return detalleAuditoriaRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<DetalleAuditoria> obtenerTodo() {
        return detalleAuditoriaRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public DetalleAuditoria modificar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(DetalleAuditoria entidad) {
        detalleAuditoriaRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return detalleAuditoriaRepositorio.existsById(id);
    }

    /**
     * Busca los registros de detalleAuditoria asociados a auditoria.
     *
     * @param auditoria el registro de Auditoria asociado
     * @return una lista de DetalleAuditoria asociados al Auditoria indicado
     */
    @Override
    public List<DetalleAuditoria> buscarPorAuditoria(Auditoria auditoria) {
        return detalleAuditoriaRepositorio.findByAuditoria(auditoria);
    }
}
