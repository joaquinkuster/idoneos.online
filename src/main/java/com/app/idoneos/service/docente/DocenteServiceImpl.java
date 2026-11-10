package com.app.idoneos.service.docente;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.Docente;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de DocenteService. */
@Service
public class DocenteServiceImpl implements DocenteService {

    @Autowired
    private DocenteRepository docenteRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el docente por su identificador. */
    @Override
    public Optional<Docente> buscarPorId(Integer id) {
        return docenteRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Docente> obtenerTodo() {
        return docenteRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return docenteRepository.existsById(id);
    }

    /** Busca los docentes activos y habilitados, que pueden participar en cursos. */
    @Override
    public List<Docente> buscarHabilitados() {
        return docenteRepository.buscarPorHabilitado();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el docente en la base de datos. */
    @Override
    public Docente guardar(Docente entidad) {
        return docenteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Docente modificar(Docente entidad) {
        return docenteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Docente entidad) {
        docenteRepository.delete(entidad);
    }
}
