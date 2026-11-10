package com.app.idoneos.service.opcionrespuesta;

import com.app.idoneos.model.OpcionRespuesta;
import com.app.idoneos.repository.OpcionRespuestaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de OpcionRespuestaService. */
@Service
public class OpcionRespuestaServiceImpl implements OpcionRespuestaService {

    @Autowired
    private OpcionRespuestaRepository opcionRespuestaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la opción de respuesta por su identificador. */
    @Override
    public Optional<OpcionRespuesta> buscarPorId(Integer id) {
        return opcionRespuestaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<OpcionRespuesta> obtenerTodo() {
        return opcionRespuestaRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return opcionRespuestaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la opción de respuesta en la base de datos. */
    @Override
    public OpcionRespuesta guardar(OpcionRespuesta entidad) {
        return opcionRespuestaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public OpcionRespuesta modificar(OpcionRespuesta entidad) {
        return opcionRespuestaRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(OpcionRespuesta entidad) {
        entidad.marcarInactivo(); // Baja lógica
        opcionRespuestaRepository.save(entidad);
    }
}
