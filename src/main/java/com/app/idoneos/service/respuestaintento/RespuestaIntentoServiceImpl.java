package com.app.idoneos.service.respuestaintento;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.RespuestaIntento;
import com.app.idoneos.repository.RespuestaIntentoRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de RespuestaIntentoService. */
@Service
public class RespuestaIntentoServiceImpl implements RespuestaIntentoService {

    @Autowired
    private RespuestaIntentoRepository respuestaIntentoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la respuesta del intento por su identificador. */
    @Override
    public Optional<RespuestaIntento> buscarPorId(Integer id) {
        return respuestaIntentoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<RespuestaIntento> obtenerTodo() {
        return respuestaIntentoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return respuestaIntentoRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la respuesta del intento en la base de datos. */
    @Override
    public RespuestaIntento guardar(RespuestaIntento entidad) {
        return respuestaIntentoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public RespuestaIntento modificar(RespuestaIntento entidad) {
        return respuestaIntentoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(RespuestaIntento entidad) {
        respuestaIntentoRepository.delete(entidad);
    }
}
