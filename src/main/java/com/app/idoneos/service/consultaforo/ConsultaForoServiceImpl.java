package com.app.idoneos.service.consultaforo;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.ConsultaForo;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Parametro;
import com.app.idoneos.model.Tema;
import com.app.idoneos.repository.ConsultaForoRepository;
import com.app.idoneos.repository.RespuestaForoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ConsultaForo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ConsultaForoService}.
 */
@Service
public class ConsultaForoServiceImpl implements ConsultaForoService {

    @Autowired
    private ConsultaForoRepository consultaForoRepository;

    @Autowired
    private RespuestaForoRepository respuestaForoRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private ParametroService parametroService;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la consulta del foro por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ConsultaForo> buscarPorId(Integer id) {
        return consultaForoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ConsultaForo> obtenerTodo() {
        return consultaForoRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return consultaForoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de consultaForo asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de ConsultaForo asociados al Tema indicado
     */
    @Override
    public List<ConsultaForo> buscarPorTema(Tema tema) {
        return consultaForoRepository.findByTemaAndBajaFalse(tema);
    }


    /**
     * CU-35: Busca las consultas de un tema por texto y rango de fechas.
     */
    @Override
    public List<ConsultaForo> buscarConFiltros(Tema tema, String texto, LocalDate desde, LocalDate hasta) {
        String buscado = TextoUtil.normalizar(texto);
        return tema.getConsultasForoOrdenadas().stream()
                .filter(c -> buscado.isEmpty() || TextoUtil.normalizar(c.getTexto()).contains(buscado))
                .filter(c -> desde == null || !c.getFechaCreacion().toLocalDate().isBefore(desde))
                .filter(c -> hasta == null || !c.getFechaCreacion().toLocalDate().isAfter(hasta)).toList();
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la consulta del foro en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ConsultaForo guardar(ConsultaForo entidad) {
        return consultaForoRepository.save(entidad);
    }

    /**
     * CU-36: Registra una consulta en el foro de un tema.
     */
    @Override
    @Transactional
    public ConsultaForo registrar(Alumno alumno, Integer temaId, String texto) {
        if (alumno == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
        }
        Tema tema = temaRepository.findById(temaId == null ? -1 : temaId).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        Inscripcion inscripcion = inscripcionService.buscarConAccesoATema(alumno, tema).orElseThrow(
                () -> new IllegalArgumentException("Error! No tenés una inscripción vigente con este tema habilitado."));
        ConsultaForo consulta = consultaForoRepository.save(new ConsultaForo(tema, inscripcion, validarTexto(texto)));
        tema.getConsultasForo().add(consulta);
        return consulta;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ConsultaForo modificar(ConsultaForo entidad) {
        return consultaForoRepository.save(entidad);
    }

    /**
     * CU-37: Modifica una consulta propia dentro del plazo de edición.
     */
    @Override
    @Transactional
    public ConsultaForo modificar(Integer id, Alumno alumno, String texto) {
        ConsultaForo consulta = consultaForoRepository.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        if (alumno == null || consulta.getInscripcion().getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! Solo podés modificar tus propias consultas.");
        }
        int plazo = parametroService.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO);
        if (!consulta.estaDentroDelPlazo(plazo)) {
            throw new IllegalArgumentException("Error! La consulta ya no puede modificarse: pasó el plazo de edición de "
                    + plazo + " minutos.");
        }
        consulta.setTexto(validarTexto(texto));
        consulta.setUltimaModificacion(LocalDateTime.now());
        return consultaForoRepository.save(consulta);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ConsultaForo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        consultaForoRepository.save(entidad);
    }

    /**
     * CU-38: Da de baja una consulta y sus respuestas.
     */
    @Override
    @Transactional
    public void darDeBaja(Integer id) {
        ConsultaForo consulta = consultaForoRepository.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        LocalDateTime ahora = LocalDateTime.now();
        consulta.getRespuestasVigentes().forEach(respuesta -> {
            respuesta.marcarInactivo();
            respuesta.setUltimaModificacion(ahora);
            respuestaForoRepository.save(respuesta);
        });
        consulta.marcarInactivo();
        consulta.setUltimaModificacion(ahora);
        consultaForoRepository.save(consulta);
    }

    /** Valida el texto de la consulta y lo devuelve sin espacios sobrantes. */
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("texto", "Error! Debe completar el texto de la consulta.");
        }
        if (texto.trim().length() > ConsultaForo.LONGITUD_TEXTO) {
            throw RespuestaFormularioUtil.armarCampoInvalido("texto", MensajeErrorUtil.armarDemasiadoLargo("El texto", ConsultaForo.LONGITUD_TEXTO, "caracteres"));
        }
        return texto.trim();
    }
}
