package com.app.idoneos.servicio.ConsultaForo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ConsultaForo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ConsultaForoServicio}.
 */
@Service
public class ConsultaForoServicioImpl implements ConsultaForoServicio, CrudServicio<ConsultaForo> {

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    /**
     * Guarda la consulta del foro en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ConsultaForo guardar(ConsultaForo entidad) {
        return consultaForoRepositorio.save(entidad);
    }

    /**
     * Busca la consulta del foro por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ConsultaForo> buscarPorId(Integer id) {
        return consultaForoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ConsultaForo> obtenerTodo() {
        return consultaForoRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ConsultaForo modificar(ConsultaForo entidad) {
        return consultaForoRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ConsultaForo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        consultaForoRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return consultaForoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de consultaForo asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de ConsultaForo asociados al Unidad indicado
     */
    @Override
    public List<ConsultaForo> buscarPorUnidad(Unidad unidad) {
        return consultaForoRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * Busca los registros vigentes de consultaForo asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de ConsultaForo asociados al Inscripcion indicado
     */
    @Override
    public List<ConsultaForo> buscarPorInscripcion(Inscripcion inscripcion) {
        return consultaForoRepositorio.findByInscripcionAndBajaFalse(inscripcion);
    }
}
