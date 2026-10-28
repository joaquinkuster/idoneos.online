package com.app.idoneos.servicio.ConsultaForo;

import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.ConsultaForo;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.RespuestaForoRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ConsultaForo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ConsultaForoServicio}.
 */
@Service
public class ConsultaForoServicioImpl implements ConsultaForoServicio, CrudServicio<ConsultaForo> {

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    @Autowired
    private RespuestaForoRepositorio respuestaForoRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    // ---------------------------------------------------------------- Lecturas

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
     * Busca los registros vigentes de consultaForo asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de ConsultaForo asociados al Tema indicado
     */
    @Override
    public List<ConsultaForo> buscarPorTema(Tema tema) {
        return consultaForoRepositorio.findByTemaAndBajaFalse(tema);
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

    /**
     * CU-35: Busca las consultas de un tema por texto y rango de fechas.
     */
    @Override
    public List<ConsultaForo> buscarConFiltros(Tema tema, String texto, LocalDate desde, LocalDate hasta) {
        String buscado = TextoUtilidad.normalizar(texto);
        return tema.getConsultasForoOrdenadas().stream()
                .filter(c -> buscado.isEmpty() || TextoUtilidad.normalizar(c.getTexto()).contains(buscado))
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
        return consultaForoRepositorio.save(entidad);
    }

    /**
     * CU-36: Registra una consulta en el foro de un tema.
     */
    @Override
    @Transactional
    public ConsultaForo registrarConsulta(Alumno alumno, Integer temaId, String texto) {
        if (alumno == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de alumno.");
        }
        Tema tema = temaRepositorio.findById(temaId == null ? -1 : temaId).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        Inscripcion inscripcion = inscripcionServicio.buscarConAccesoATema(alumno, tema).orElseThrow(
                () -> new IllegalArgumentException("Error! No tenés una inscripción vigente con este tema habilitado."));
        ConsultaForo consulta = consultaForoRepositorio.save(new ConsultaForo(tema, inscripcion, validarTexto(texto)));
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
        return consultaForoRepositorio.save(entidad);
    }

    /**
     * CU-37: Modifica una consulta propia dentro del plazo de edición.
     */
    @Override
    @Transactional
    public ConsultaForo modificarConsulta(Integer id, Alumno alumno, String texto) {
        ConsultaForo consulta = consultaForoRepositorio.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        if (alumno == null || consulta.getInscripcion().getAlumno().getIdAlumno() != alumno.getIdAlumno()) {
            throw new IllegalArgumentException("Error! Solo podés modificar tus propias consultas.");
        }
        int plazo = parametroServicio.obtenerEntero(Parametro.PLAZO_EDICION_FORO,
                Parametro.PLAZO_EDICION_FORO_POR_DEFECTO);
        if (!consulta.estaDentroDelPlazo(plazo)) {
            throw new IllegalArgumentException("Error! La consulta ya no puede modificarse: pasó el plazo de edición de "
                    + plazo + " minutos.");
        }
        consulta.setTexto(validarTexto(texto));
        consulta.setUltimaModificacion(LocalDateTime.now());
        return consultaForoRepositorio.save(consulta);
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
        consultaForoRepositorio.save(entidad);
    }

    /**
     * CU-38: Da de baja una consulta y sus respuestas.
     */
    @Override
    @Transactional
    public void darDeBajaConsulta(Integer id) {
        ConsultaForo consulta = consultaForoRepositorio.findById(id == null ? -1 : id).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La consulta no se encuentra activa."));
        LocalDateTime ahora = LocalDateTime.now();
        consulta.getRespuestasVigentes().forEach(respuesta -> {
            respuesta.marcarInactivo();
            respuesta.setUltimaModificacion(ahora);
            respuestaForoRepositorio.save(respuesta);
        });
        consulta.marcarInactivo();
        consulta.setUltimaModificacion(ahora);
        consultaForoRepositorio.save(consulta);
    }

    /** Valida el texto de la consulta y lo devuelve sin espacios sobrantes. */
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw RespuestaFormularioUtilidad.campoInvalido("texto", "Error! Debe completar el texto de la consulta.");
        }
        if (texto.trim().length() > ConsultaForo.LONGITUD_TEXTO) {
            throw RespuestaFormularioUtilidad.campoInvalido("texto", MensajeErrorUtilidad.demasiadoLargo("El texto", ConsultaForo.LONGITUD_TEXTO, "caracteres"));
        }
        return texto.trim();
    }
}
