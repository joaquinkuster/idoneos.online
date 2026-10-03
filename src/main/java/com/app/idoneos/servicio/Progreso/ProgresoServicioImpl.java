package com.app.idoneos.servicio.Progreso;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Progreso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ProgresoServicio}.
 */
@Service
public class ProgresoServicioImpl implements ProgresoServicio, CrudServicio<Progreso> {

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    /**
     * Guarda el progreso en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Progreso guardar(Progreso entidad) {
        return progresoRepositorio.save(entidad);
    }

    /**
     * Busca el progreso por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Progreso> buscarPorId(Integer id) {
        return progresoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Progreso> obtenerTodo() {
        return progresoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Progreso modificar(Progreso entidad) {
        return progresoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Progreso entidad) {
        progresoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return progresoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de progreso asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Progreso asociados al Unidad indicado
     */
    @Override
    public List<Progreso> buscarPorUnidad(Unidad unidad) {
        return progresoRepositorio.findByUnidad(unidad);
    }

    /**
     * Busca los registros de progreso asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Progreso asociados al Inscripcion indicado
     */
    @Override
    public List<Progreso> buscarPorInscripcion(Inscripcion inscripcion) {
        return progresoRepositorio.findByInscripcion(inscripcion);
    }

    /**
     * Busca el progreso a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Progreso> buscarPorInscripcionYUnidad(Inscripcion inscripcion, Unidad unidad) {
        return progresoRepositorio.findByInscripcionAndUnidad(inscripcion, unidad);
    }
}
