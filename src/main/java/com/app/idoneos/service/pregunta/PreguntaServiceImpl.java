package com.app.idoneos.service.pregunta;

import com.app.idoneos.model.Pregunta;
import com.app.idoneos.repository.PreguntaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de PreguntaService. */
@Service
public class PreguntaServiceImpl implements PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la pregunta por su identificador. */
    @Override
    public Optional<Pregunta> buscarPorId(Integer id) {
        return preguntaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Pregunta> obtenerTodo() {
        return preguntaRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la pregunta en la base de datos. */
    @Override
    public Pregunta guardar(Pregunta entidad) {
        return preguntaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Pregunta modificar(Pregunta entidad) {
        return preguntaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Pregunta entidad) {
        entidad.marcarInactivo(); // Baja lógica
        preguntaRepository.save(entidad);
    }
}
