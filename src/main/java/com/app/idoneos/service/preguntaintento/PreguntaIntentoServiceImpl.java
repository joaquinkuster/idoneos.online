package com.app.idoneos.service.preguntaintento;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.PreguntaIntento;
import com.app.idoneos.repository.PreguntaIntentoRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de PreguntaIntentoService. */
@Service
public class PreguntaIntentoServiceImpl implements PreguntaIntentoService {

    @Autowired
    private PreguntaIntentoRepository preguntaIntentoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la pregunta del intento por su identificador. */
    @Override
    public Optional<PreguntaIntento> buscarPorId(Integer id) {
        return preguntaIntentoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<PreguntaIntento> obtenerTodo() {
        return preguntaIntentoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaIntentoRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la pregunta del intento en la base de datos. */
    @Override
    public PreguntaIntento guardar(PreguntaIntento entidad) {
        return preguntaIntentoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public PreguntaIntento modificar(PreguntaIntento entidad) {
        return preguntaIntentoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(PreguntaIntento entidad) {
        preguntaIntentoRepository.delete(entidad);
    }
}
