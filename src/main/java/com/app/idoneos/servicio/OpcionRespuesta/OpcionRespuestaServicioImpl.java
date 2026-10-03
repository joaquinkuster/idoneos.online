package com.app.idoneos.servicio.OpcionRespuesta;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.repositorio.OpcionRespuestaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link OpcionRespuesta}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link OpcionRespuestaServicio}.
 */
@Service
public class OpcionRespuestaServicioImpl implements OpcionRespuestaServicio, CrudServicio<OpcionRespuesta> {

    @Autowired
    private OpcionRespuestaRepositorio opcionRespuestaRepositorio;

    /**
     * Guarda la opción de respuesta en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public OpcionRespuesta guardar(OpcionRespuesta entidad) {
        return opcionRespuestaRepositorio.save(entidad);
    }

    /**
     * Busca la opción de respuesta por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<OpcionRespuesta> buscarPorId(Integer id) {
        return opcionRespuestaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<OpcionRespuesta> obtenerTodo() {
        return opcionRespuestaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public OpcionRespuesta modificar(OpcionRespuesta entidad) {
        return opcionRespuestaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(OpcionRespuesta entidad) {
        entidad.marcarInactivo(); // Baja lógica
        opcionRespuestaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return opcionRespuestaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de opcionRespuesta asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de OpcionRespuesta asociados al Pregunta indicado
     */
    @Override
    public List<OpcionRespuesta> buscarPorPregunta(Pregunta pregunta) {
        return opcionRespuestaRepositorio.findByPreguntaAndBajaFalse(pregunta);
    }
}
