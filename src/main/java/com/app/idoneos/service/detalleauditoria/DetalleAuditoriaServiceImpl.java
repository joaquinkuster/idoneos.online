package com.app.idoneos.service.detalleauditoria;

import com.app.idoneos.model.DetalleAuditoria;
import com.app.idoneos.repository.DetalleAuditoriaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de DetalleAuditoriaService. */
@Service
public class DetalleAuditoriaServiceImpl implements DetalleAuditoriaService {

    @Autowired
    private DetalleAuditoriaRepository detalleAuditoriaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el detalle de auditoría por su identificador. */
    @Override
    public Optional<DetalleAuditoria> buscarPorId(Integer id) {
        return detalleAuditoriaRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<DetalleAuditoria> obtenerTodo() {
        return detalleAuditoriaRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return detalleAuditoriaRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el detalle de auditoría en la base de datos. */
    @Override
    public DetalleAuditoria guardar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public DetalleAuditoria modificar(DetalleAuditoria entidad) {
        return detalleAuditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(DetalleAuditoria entidad) {
        detalleAuditoriaRepository.delete(entidad);
    }
}
