package com.app.idoneos.service.respuestaforo;

import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.RespuestaForo;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de RespuestaForoService. */
@Service
public class RespuestaForoServiceImpl implements RespuestaForoService {

    @Autowired
    private RespuestaForoRepository respuestaForoRepository;

    @Autowired
    private ConsultaForoRepository consultaForoRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private ParametroService parametroService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la respuesta del foro por su identificador. */
    @Override
    public Optional<RespuestaForo> buscarPorId(Integer id) {
        return respuestaForoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<RespuestaForo> obtenerTodo() {
        return respuestaForoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return respuestaForoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la respuesta del foro en la base de datos. */
    @Override
    public RespuestaForo guardar(RespuestaForo entidad) {
        return respuestaForoRepository.save(entidad);
    }

    /** Registra la respuesta de un docente a una consulta. */
    @Override
    @Transactional
    public RespuestaForo registrar(Docente docente, Integer consultaId, String texto) {
        ConsultaForo consulta = consultaForoRepository.findById(consultaId == null ? -1 : consultaId)
                .filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        DocenteCurso participacion = obtenerParticipacion(consulta, docente);
        RespuestaForo respuesta = respuestaForoRepository.save(new RespuestaForo(participacion, consulta,
                validarTexto(texto)));
        consulta.getRespuestasForo().add(respuesta);
        return respuesta;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public RespuestaForo modificar(RespuestaForo entidad) {
        return respuestaForoRepository.save(entidad);
    }

    /** Modifica una respuesta propia dentro del plazo de edición. */
    @Override
    @Transactional
    public RespuestaForo modificar(Integer id, Docente docente, String texto) {
        RespuestaForo respuesta = buscarActiva(id);
        obtenerParticipacion(respuesta.getConsulta(), docente);
        if (respuesta.getDocenteCurso().getDocente().getIdDocente() != docente.getIdDocente()) {
            throw new IllegalArgumentException("Error! Solo podés modificar tus propias respuestas.");
        }
        int plazo = parametroService.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO);
        if (!respuesta.puedeModificar(docente, plazo)) {
            throw new IllegalArgumentException("Error! La respuesta ya no puede modificarse: pasó el plazo de edición de "
                    + plazo + " minutos.");
        }
        respuesta.setTexto(validarTexto(texto));
        respuesta.setUltimaModificacion(LocalDateTime.now());
        return respuestaForoRepository.save(respuesta);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(RespuestaForo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        respuestaForoRepository.save(entidad);
    }

    /** Da de baja una respuesta. */
    @Override
    @Transactional
    public void darDeBaja(Integer id) {
        RespuestaForo respuesta = buscarActiva(id);
        respuesta.marcarInactivo();
        respuesta.setUltimaModificacion(LocalDateTime.now());
        respuestaForoRepository.save(respuesta);
    }

    private RespuestaForo buscarActiva(Integer id) {
        return respuestaForoRepository.findById(id == null ? -1 : id).filter(r -> !r.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La respuesta no se encuentra activa."));
    }

    /** Obtiene la participación del docente en el curso de la consulta (como titular o ayudante). */
    private DocenteCurso obtenerParticipacion(ConsultaForo consulta, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepository.findByCursoAndBajaFalse(consulta.getTema().getCurso()).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida el texto de la respuesta y lo devuelve sin espacios sobrantes. */
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("texto", "Error! Debe completar el texto de la respuesta.");
        }
        if (texto.trim().length() > RespuestaForo.LONGITUD_TEXTO) {
            throw RespuestaFormularioUtil.armarCampoInvalido("texto", MensajeErrorUtil.armarDemasiadoLargo("El texto", RespuestaForo.LONGITUD_TEXTO, "caracteres"));
        }
        return texto.trim();
    }
}
