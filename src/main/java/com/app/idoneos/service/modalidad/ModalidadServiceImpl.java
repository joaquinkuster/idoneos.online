package com.app.idoneos.service.modalidad;

import com.app.idoneos.model.Modalidad;
import com.app.idoneos.repository.ModalidadRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de ModalidadService. */
@Service
public class ModalidadServiceImpl implements ModalidadService {

    @Autowired
    private ModalidadRepository modalidadRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la modalidad por su identificador. */
    @Override
    public Optional<Modalidad> buscarPorId(Integer id) {
        return modalidadRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Modalidad> obtenerTodo() {
        return modalidadRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return modalidadRepository.existsById(id);
    }

    /** Busca la modalidad por su atributo único 'nombre'. */
    @Override
    public Optional<Modalidad> buscarPorNombre(String nombre) {
        return modalidadRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la modalidad en la base de datos. */
    @Override
    public Modalidad guardar(Modalidad entidad) {
        return modalidadRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Modalidad modificar(Modalidad entidad) {
        return modalidadRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Modalidad entidad) {
        modalidadRepository.delete(entidad);
    }
}
