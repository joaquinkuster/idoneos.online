package com.app.idoneos.servicio.PreguntaIntento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Pregunta;
import com.app.idoneos.modelo.PreguntaIntento;
import com.app.idoneos.repositorio.PreguntaIntentoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link PreguntaIntento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PreguntaIntentoServicio}.
 */
@Service
public class PreguntaIntentoServicioImpl implements PreguntaIntentoServicio, CrudServicio<PreguntaIntento> {

    @Autowired
    private PreguntaIntentoRepositorio preguntaIntentoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la pregunta del intento por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<PreguntaIntento> buscarPorId(Integer id) {
        return preguntaIntentoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<PreguntaIntento> obtenerTodo() {
        return preguntaIntentoRepositorio.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return preguntaIntentoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de preguntaIntento asociados a intentoAutoevaluacion.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @return una lista de PreguntaIntento asociados al IntentoAutoevaluacion indicado
     */
    @Override
    public List<PreguntaIntento> buscarPorIntentoAutoevaluacion(IntentoAutoevaluacion intentoAutoevaluacion) {
        return preguntaIntentoRepositorio.findByIntentoAutoevaluacion(intentoAutoevaluacion);
    }

    /**
     * Busca los registros de preguntaIntento asociados a pregunta.
     *
     * @param pregunta el registro de Pregunta asociado
     * @return una lista de PreguntaIntento asociados al Pregunta indicado
     */
    @Override
    public List<PreguntaIntento> buscarPorPregunta(Pregunta pregunta) {
        return preguntaIntentoRepositorio.findByPregunta(pregunta);
    }

    /**
     * Busca la pregunta del intento a partir de la combinación única de sus relaciones.
     *
     * @param intentoAutoevaluacion el registro de IntentoAutoevaluacion asociado
     * @param pregunta el registro de Pregunta asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<PreguntaIntento> buscarPorIntentoAutoevaluacionYPregunta(IntentoAutoevaluacion intentoAutoevaluacion, Pregunta pregunta) {
        return preguntaIntentoRepositorio.findByIntentoAutoevaluacionAndPregunta(intentoAutoevaluacion, pregunta);
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
        return preguntaIntentoRepositorio.save(entidad);
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
        return preguntaIntentoRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(PreguntaIntento entidad) {
        preguntaIntentoRepositorio.delete(entidad);
    }
}
