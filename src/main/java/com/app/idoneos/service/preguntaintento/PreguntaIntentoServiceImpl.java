package com.app.idoneos.service.preguntaintento;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.PreguntaIntento;
import com.app.idoneos.repository.PreguntaIntentoRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link PreguntaIntento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PreguntaIntentoService}.
 */
@Service
public class PreguntaIntentoServiceImpl implements PreguntaIntentoService {

    @Autowired
    private PreguntaIntentoRepository preguntaIntentoRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la pregunta del intento por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<PreguntaIntento> buscarPorId(Integer id) {
        return preguntaIntentoRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<PreguntaIntento> obtenerTodo() {
        return preguntaIntentoRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaIntentoRepository.existsById(id);
    }




    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la pregunta del intento en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public PreguntaIntento guardar(PreguntaIntento entidad) {
        return preguntaIntentoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public PreguntaIntento modificar(PreguntaIntento entidad) {
        return preguntaIntentoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(PreguntaIntento entidad) {
        preguntaIntentoRepository.delete(entidad);
    }
}
