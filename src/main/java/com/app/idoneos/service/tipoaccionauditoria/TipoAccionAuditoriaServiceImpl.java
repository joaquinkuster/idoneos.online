package com.app.idoneos.service.tipoaccionauditoria;

import com.app.idoneos.model.TipoAccionAuditoria;
import com.app.idoneos.repository.TipoAccionAuditoriaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de TipoAccionAuditoriaService. */
@Service
public class TipoAccionAuditoriaServiceImpl implements TipoAccionAuditoriaService {

    @Autowired
    private TipoAccionAuditoriaRepository tipoAccionAuditoriaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el tipo de acción auditada por su identificador. */
    @Override
    public Optional<TipoAccionAuditoria> buscarPorId(Integer id) {
        return tipoAccionAuditoriaRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<TipoAccionAuditoria> obtenerTodo() {
        return tipoAccionAuditoriaRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return tipoAccionAuditoriaRepository.existsById(id);
    }

    /** Busca el tipo de acción auditada por su atributo único 'nombre'. */
    @Override
    public Optional<TipoAccionAuditoria> buscarPorNombre(String nombre) {
        return tipoAccionAuditoriaRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el tipo de acción auditada en la base de datos. */
    @Override
    public TipoAccionAuditoria guardar(TipoAccionAuditoria entidad) {
        return tipoAccionAuditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public TipoAccionAuditoria modificar(TipoAccionAuditoria entidad) {
        return tipoAccionAuditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(TipoAccionAuditoria entidad) {
        tipoAccionAuditoriaRepository.delete(entidad);
    }
}
