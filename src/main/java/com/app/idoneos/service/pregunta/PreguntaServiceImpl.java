package com.app.idoneos.service.pregunta;

import com.app.idoneos.model.Pregunta;
import com.app.idoneos.repository.PreguntaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pregunta}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PreguntaService}.
 */
@Service
public class PreguntaServiceImpl implements PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la pregunta por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pregunta> buscarPorId(Integer id) {
        return preguntaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pregunta> obtenerTodo() {
        return preguntaRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }


    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la pregunta en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pregunta guardar(Pregunta entidad) {
        return preguntaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pregunta modificar(Pregunta entidad) {
        return preguntaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pregunta entidad) {
        entidad.marcarInactivo(); // Baja lógica
        preguntaRepository.save(entidad);
    }
}
