package com.app.idoneos.servicio.RespuestaForo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.RespuestaForo;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.RespuestaForoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RespuestaForo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RespuestaForoServicio}.
 */
@Service
public class RespuestaForoServicioImpl implements RespuestaForoServicio, CrudServicio<RespuestaForo> {

    @Autowired
    private RespuestaForoRepositorio respuestaForoRepositorio;

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private ParametroServicio parametroServicio;

    // ---------------------------------------------------------------- Lecturas

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

    // ---------------------------------------------------------------- Creaciones

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
     * CU-40: Registra la respuesta de un docente a una consulta.
     */
    @Override
    @Transactional
    public RespuestaForo registrarRespuesta(Docente docente, Integer consultaId, String texto) {
        ConsultaForo consulta = consultaForoRepositorio.findById(consultaId == null ? -1 : consultaId)
                .filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        ParticipacionDocente participacion = obtenerParticipacion(consulta, docente);
        RespuestaForo respuesta = respuestaForoRepositorio.save(new RespuestaForo(participacion, consulta,
                validarTexto(texto)));
        consulta.getRespuestasForo().add(respuesta);
        return respuesta;
    }

    // ---------------------------------------------------------------- Modificaciones

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
     * CU-41: Modifica una respuesta propia dentro del plazo de edición.
     */
    @Override
    @Transactional
    public RespuestaForo modificarRespuesta(Integer id, Docente docente, String texto) {
        RespuestaForo respuesta = buscarActiva(id);
        obtenerParticipacion(respuesta.getConsulta(), docente);
        if (respuesta.getParticipacionDocente().getDocente().getIdDocente() != docente.getIdDocente()) {
            throw new IllegalArgumentException("Error! Solo podés modificar tus propias respuestas.");
        }
        int plazo = parametroServicio.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO);
        if (!respuesta.puedeModificar(docente, plazo)) {
            throw new IllegalArgumentException("Error! La respuesta ya no puede modificarse: pasó el plazo de edición de "
                    + plazo + " minutos.");
        }
        respuesta.setTexto(validarTexto(texto));
        respuesta.setUltimaModificacion(LocalDateTime.now());
        return respuestaForoRepositorio.save(respuesta);
    }

    // ---------------------------------------------------------------- Bajas

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
     * CU-42: Da de baja una respuesta.
     */
    @Override
    @Transactional
    public void darDeBajaRespuesta(Integer id) {
        RespuestaForo respuesta = buscarActiva(id);
        respuesta.marcarInactivo();
        respuesta.setUltimaModificacion(LocalDateTime.now());
        respuestaForoRepositorio.save(respuesta);
    }

    private RespuestaForo buscarActiva(Integer id) {
        return respuestaForoRepositorio.findById(id == null ? -1 : id).filter(r -> !r.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La respuesta no se encuentra activa."));
    }

    /** Obtiene la participación del docente en el curso de la consulta (como titular o ayudante). */
    private ParticipacionDocente obtenerParticipacion(ConsultaForo consulta, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return participacionDocenteRepositorio.findByCursoAndBajaFalse(consulta.getUnidad().getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida el texto de la respuesta y lo devuelve sin espacios sobrantes. */
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Error! Debe completar el texto de la respuesta.");
        }
        if (texto.trim().length() > RespuestaForo.LONGITUD_TEXTO) {
            throw new IllegalArgumentException(
                    "Error! El texto no puede superar los " + RespuestaForo.LONGITUD_TEXTO + " caracteres.");
        }
        return texto.trim();
    }
}
