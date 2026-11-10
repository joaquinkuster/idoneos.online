package com.app.idoneos.service.auditoria;

import com.app.idoneos.model.Auditoria;
import com.app.idoneos.repository.AuditoriaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de AuditoriaService. */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la auditoría por su identificador. */
    @Override
    public Optional<Auditoria> buscarPorId(Integer id) {
        return auditoriaRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Auditoria> obtenerTodo() {
        return auditoriaRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return auditoriaRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la auditoría en la base de datos. */
    @Override
    public Auditoria guardar(Auditoria entidad) {
        return auditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Auditoria modificar(Auditoria entidad) {
        return auditoriaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Auditoria entidad) {
        auditoriaRepository.delete(entidad);
    }
}
