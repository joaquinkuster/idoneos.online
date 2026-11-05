package com.app.idoneos.service.detalleauditoria;

import com.app.idoneos.model.DetalleAuditoria;
import com.app.idoneos.repository.DetalleAuditoriaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link DetalleAuditoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DetalleAuditoriaService}.
 */
@Service
public class DetalleAuditoriaServiceImpl implements DetalleAuditoriaService {

    @Autowired
    private DetalleAuditoriaRepository detalleAuditoriaRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el detalle de auditoría por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<DetalleAuditoria> buscarPorId(Integer id) {
        return detalleAuditoriaRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<DetalleAuditoria> obtenerTodo() {
        return detalleAuditoriaRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return detalleAuditoriaRepository.existsById(id);
    }


    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el detalle de auditoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public DetalleAuditoria guardar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public DetalleAuditoria modificar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(DetalleAuditoria entidad) {
        detalleAuditoriaRepository.delete(entidad);
    }
}
