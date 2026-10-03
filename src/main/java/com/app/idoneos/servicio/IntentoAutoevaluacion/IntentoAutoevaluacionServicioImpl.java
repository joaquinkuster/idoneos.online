package com.app.idoneos.servicio.IntentoAutoevaluacion;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link IntentoAutoevaluacion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link IntentoAutoevaluacionServicio}.
 */
@Service
public class IntentoAutoevaluacionServicioImpl implements IntentoAutoevaluacionServicio, CrudServicio<IntentoAutoevaluacion> {

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoAutoevaluacionRepositorio;

    /**
     * Guarda el intento de autoevaluación en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public IntentoAutoevaluacion guardar(IntentoAutoevaluacion entidad) {
        return intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * Busca el intento de autoevaluación por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<IntentoAutoevaluacion> buscarPorId(Integer id) {
        return intentoAutoevaluacionRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<IntentoAutoevaluacion> obtenerTodo() {
        return intentoAutoevaluacionRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public IntentoAutoevaluacion modificar(IntentoAutoevaluacion entidad) {
        return intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(IntentoAutoevaluacion entidad) {
        entidad.marcarInactivo(); // Baja lógica
        intentoAutoevaluacionRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return intentoAutoevaluacionRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Inscripcion indicado
     */
    @Override
    public List<IntentoAutoevaluacion> buscarPorInscripcion(Inscripcion inscripcion) {
        return intentoAutoevaluacionRepositorio.findByInscripcionAndBajaFalse(inscripcion);
    }

    /**
     * Busca los registros vigentes de intentoAutoevaluacion asociados a autoevaluacion.
     *
     * @param autoevaluacion el registro de Autoevaluacion asociado
     * @return una lista de IntentoAutoevaluacion asociados al Autoevaluacion indicado
     */
    @Override
    public List<IntentoAutoevaluacion> buscarPorAutoevaluacion(Autoevaluacion autoevaluacion) {
        return intentoAutoevaluacionRepositorio.findByAutoevaluacionAndBajaFalse(autoevaluacion);
    }
}
