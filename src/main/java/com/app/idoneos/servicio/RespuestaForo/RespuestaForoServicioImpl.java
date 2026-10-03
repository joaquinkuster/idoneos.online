package com.app.idoneos.servicio.RespuestaForo;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.RespuestaForo;
import com.app.idoneos.repositorio.RespuestaForoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RespuestaForo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RespuestaForoServicio}.
 */
@Service
public class RespuestaForoServicioImpl implements RespuestaForoServicio, CrudServicio<RespuestaForo> {

    @Autowired
    private RespuestaForoRepositorio respuestaForoRepositorio;

    /**
     * Guarda la respuesta del foro en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public RespuestaForo guardar(RespuestaForo entidad) {
        return respuestaForoRepositorio.save(entidad);
    }

    /**
     * Busca la respuesta del foro por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<RespuestaForo> buscarPorId(Integer id) {
        return respuestaForoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<RespuestaForo> obtenerTodo() {
        return respuestaForoRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public RespuestaForo modificar(RespuestaForo entidad) {
        return respuestaForoRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(RespuestaForo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        respuestaForoRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return respuestaForoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de respuestaForo asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de RespuestaForo asociados al ParticipacionDocente indicado
     */
    @Override
    public List<RespuestaForo> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente) {
        return respuestaForoRepositorio.findByParticipacionDocenteAndBajaFalse(participacionDocente);
    }

    /**
     * Busca los registros vigentes de respuestaForo asociados a consultaForo.
     *
     * @param consulta el registro de ConsultaForo asociado
     * @return una lista de RespuestaForo asociados al ConsultaForo indicado
     */
    @Override
    public List<RespuestaForo> buscarPorConsulta(ConsultaForo consulta) {
        return respuestaForoRepositorio.findByConsultaAndBajaFalse(consulta);
    }
}
