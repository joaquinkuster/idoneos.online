package com.app.idoneos.servicio.TipoAccionAuditoria;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TipoAccionAuditoria;
import com.app.idoneos.repositorio.TipoAccionAuditoriaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TipoAccionAuditoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TipoAccionAuditoriaServicio}.
 */
@Service
public class TipoAccionAuditoriaServicioImpl implements TipoAccionAuditoriaServicio, CrudServicio<TipoAccionAuditoria> {

    @Autowired
    private TipoAccionAuditoriaRepositorio tipoAccionAuditoriaRepositorio;

    /**
     * Guarda el tipo de acción auditada en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TipoAccionAuditoria guardar(TipoAccionAuditoria entidad) {
        return tipoAccionAuditoriaRepositorio.save(entidad);
    }

    /**
     * Busca el tipo de acción auditada por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TipoAccionAuditoria> buscarPorId(Integer id) {
        return tipoAccionAuditoriaRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TipoAccionAuditoria> obtenerTodo() {
        return tipoAccionAuditoriaRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TipoAccionAuditoria modificar(TipoAccionAuditoria entidad) {
        return tipoAccionAuditoriaRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TipoAccionAuditoria entidad) {
        tipoAccionAuditoriaRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return tipoAccionAuditoriaRepositorio.existsById(id);
    }

    /**
     * Busca el tipo de acción auditada por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<TipoAccionAuditoria> buscarPorNombre(String nombre) {
        return tipoAccionAuditoriaRepositorio.findByNombre(nombre);
    }
}
