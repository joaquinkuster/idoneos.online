package com.app.idoneos.service.respuestaintento;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.RespuestaIntento;
import com.app.idoneos.repository.RespuestaIntentoRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RespuestaIntento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RespuestaIntentoService}.
 */
@Service
public class RespuestaIntentoServiceImpl implements RespuestaIntentoService {

    @Autowired
    private RespuestaIntentoRepository respuestaIntentoRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la respuesta del intento por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<RespuestaIntento> buscarPorId(Integer id) {
        return respuestaIntentoRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<RespuestaIntento> obtenerTodo() {
        return respuestaIntentoRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return respuestaIntentoRepository.existsById(id);
    }




    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la respuesta del intento en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public RespuestaIntento guardar(RespuestaIntento entidad) {
        return respuestaIntentoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public RespuestaIntento modificar(RespuestaIntento entidad) {
        return respuestaIntentoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(RespuestaIntento entidad) {
        respuestaIntentoRepository.delete(entidad);
    }
}
