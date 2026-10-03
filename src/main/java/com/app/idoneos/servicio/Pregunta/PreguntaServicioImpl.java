package com.app.idoneos.servicio.Pregunta;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Pool;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.repositorio.PreguntaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pregunta}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PreguntaServicio}.
 */
@Service
public class PreguntaServicioImpl implements PreguntaServicio, CrudServicio<Pregunta> {

    @Autowired
    private PreguntaRepositorio preguntaRepositorio;

    /**
     * Guarda la pregunta en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pregunta guardar(Pregunta entidad) {
        return preguntaRepositorio.save(entidad);
    }

    /**
     * Busca la pregunta por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pregunta> buscarPorId(Integer id) {
        return preguntaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pregunta> obtenerTodo() {
        return preguntaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pregunta modificar(Pregunta entidad) {
        return preguntaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pregunta entidad) {
        entidad.marcarInactivo(); // Baja lógica
        preguntaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de pregunta asociados a pool.
     *
     * @param pool el registro de Pool asociado
     * @return una lista de Pregunta asociados al Pool indicado
     */
    @Override
    public List<Pregunta> buscarPorPool(Pool pool) {
        return preguntaRepositorio.findByPoolAndBajaFalse(pool);
    }
}
