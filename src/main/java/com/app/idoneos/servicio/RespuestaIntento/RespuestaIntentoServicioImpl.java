package com.app.idoneos.servicio.RespuestaIntento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.OpcionRespuesta;
import com.app.idoneos.modelo.RespuestaIntento;
import com.app.idoneos.repositorio.RespuestaIntentoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RespuestaIntento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RespuestaIntentoServicio}.
 */
@Service
public class RespuestaIntentoServicioImpl implements RespuestaIntentoServicio, CrudServicio<RespuestaIntento> {

    @Autowired
    private RespuestaIntentoRepositorio respuestaIntentoRepositorio;

    /**
     * Guarda la respuesta del intento en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public RespuestaIntento guardar(RespuestaIntento entidad) {
        return respuestaIntentoRepositorio.save(entidad);
    }

    /**
     * Busca la respuesta del intento por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<RespuestaIntento> buscarPorId(Integer id) {
        return respuestaIntentoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<RespuestaIntento> obtenerTodo() {
        return respuestaIntentoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public RespuestaIntento modificar(RespuestaIntento entidad) {
        return respuestaIntentoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(RespuestaIntento entidad) {
        respuestaIntentoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return respuestaIntentoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de respuestaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de RespuestaIntento asociados al IntentoAutoevaluacion indicado
     */
    @Override
    public List<RespuestaIntento> buscarPorIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion) {
        return respuestaIntentoRepositorio.findByIntentoAutoevaluacion(intentoAutoevaluacion);
    }

    /**
     * Busca los registros de respuestaIntento asociados a opcionRespuesta.
     *
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return una lista de RespuestaIntento asociados al OpcionRespuesta indicado
     */
    @Override
    public List<RespuestaIntento> buscarPorOpcionRespuesta(OpcionRespuesta opcionRespuesta) {
        return respuestaIntentoRepositorio.findByOpcionRespuesta(opcionRespuesta);
    }

    /**
     * Busca la respuesta del intento a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param opcionRespuesta el registro de OpcionRespuesta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<RespuestaIntento> buscarPorIntentoAutoevaluacionYOpcionRespuesta(IntentoAutoevaluacion intentoAutoevaluacion, OpcionRespuesta opcionRespuesta) {
        return respuestaIntentoRepositorio.findByIntentoAutoevaluacionAndOpcionRespuesta(intentoAutoevaluacion, opcionRespuesta);
    }
}
