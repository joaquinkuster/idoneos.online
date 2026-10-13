package com.app.idoneos.servicio.TerminoGlosario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Unidad.UnidadServicio;
import com.app.idoneos.utilidades.TextoUtilidad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TerminoGlosario}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TerminoGlosarioServicio}.
 */
@Service
public class TerminoGlosarioServicioImpl implements TerminoGlosarioServicio, CrudServicio<TerminoGlosario> {

    @Autowired
    private TerminoGlosarioRepositorio terminoGlosarioRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private UnidadServicio unidadServicio;

    /**
     * Guarda el término del glosario en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TerminoGlosario guardar(TerminoGlosario entidad) {
        return terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Busca el término del glosario por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TerminoGlosario> buscarPorId(Integer id) {
        return terminoGlosarioRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TerminoGlosario> obtenerTodo() {
        return terminoGlosarioRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TerminoGlosario modificar(TerminoGlosario entidad) {
        return terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TerminoGlosario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        terminoGlosarioRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return terminoGlosarioRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de terminoGlosario asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de TerminoGlosario asociados al Unidad indicado
     */
    @Override
    public List<TerminoGlosario> buscarPorUnidad(Unidad unidad) {
        return terminoGlosarioRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    @Override
    public Map<String, List<TerminoGlosario>> agruparPorLetra(List<TerminoGlosario> terminos) {
        Map<String, List<TerminoGlosario>> grupos = new LinkedHashMap<>();
        terminos.forEach(termino -> grupos.computeIfAbsent(termino.getLetraInicial(), letra -> new ArrayList<>())
                .add(termino));
        return grupos;
    }

    /**
     * CU-32: Busca los términos de una unidad que contienen un texto, en orden alfabético.
     */
    @Override
    public List<TerminoGlosario> buscarConFiltros(Unidad unidad, String texto, boolean enDefiniciones) {
        String buscado = TextoUtilidad.normalizar(texto);
        return unidad.getTerminosGlosarioOrdenados().stream()
                .sorted(Comparator.comparing((TerminoGlosario t) -> TextoUtilidad.normalizar(t.getTermino())))
                .filter(t -> buscado.isEmpty() || TextoUtilidad.normalizar(t.getTermino()).contains(buscado)
                        || (enDefiniciones && TextoUtilidad.normalizar(t.getDefinicion()).contains(buscado)))
                .toList();
    }

    /**
     * CU-33: Registra un término en el glosario de una unidad.
     */
    @Override
    @Transactional
    public TerminoGlosario registrarTermino(Docente docente, Integer unidadId, String termino, String definicion) {
        Unidad unidad = unidadRepositorio.findById(unidadId == null ? -1 : unidadId)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        verificarParticipacion(unidad.getCurso(), docente);
        String terminoLimpio = validarDatos(termino, definicion);
        String definicionLimpia = definicion.trim();

        List<TerminoGlosario> existentes = terminoGlosarioRepositorio.findByUnidad(unidad);
        if (existentes.stream().anyMatch(t -> !t.getBaja() && t.getTermino().equalsIgnoreCase(terminoLimpio))) {
            throw new IllegalArgumentException("Error! El término ya está registrado en el glosario de esta unidad.");
        }
        // Un término dado de baja con el mismo nombre se vuelve a dar de alta (el glosario no admite repetidos)
        TerminoGlosario guardado = existentes.stream().filter(t -> t.getBaja() && t.getTermino().equals(terminoLimpio))
                .findFirst().orElseGet(() -> new TerminoGlosario(unidad, terminoLimpio, definicionLimpia));
        guardado.setDefinicion(definicionLimpia);
        guardado.setBaja(false);
        guardado.setUltimaModificacion(LocalDateTime.now());
        return terminoGlosarioRepositorio.save(guardado);
    }

    /**
     * CU-34: Modifica un término del glosario.
     */
    @Override
    @Transactional
    public TerminoGlosario modificarTermino(Integer id, Docente docente, String termino, String definicion) {
        TerminoGlosario existente = buscarActivo(id);
        verificarParticipacion(existente.getUnidad().getCurso(), docente);
        String terminoLimpio = validarDatos(termino, definicion);

        boolean repetido = terminoGlosarioRepositorio.findByUnidad(existente.getUnidad()).stream()
                .anyMatch(t -> t.getIdTermino() != existente.getIdTermino()
                        && (t.getBaja() ? t.getTermino().equals(terminoLimpio) : t.getTermino().equalsIgnoreCase(terminoLimpio)));
        if (repetido) {
            throw new IllegalArgumentException("Error! El término ya está registrado en el glosario de esta unidad.");
        }
        existente.setTermino(terminoLimpio);
        existente.setDefinicion(definicion.trim());
        existente.setUltimaModificacion(LocalDateTime.now());
        return terminoGlosarioRepositorio.save(existente);
    }

    /**
     * CU-35: Da de baja un término del glosario.
     */
    @Override
    @Transactional
    public void darDeBajaTermino(Integer id, Docente docente) {
        TerminoGlosario existente = buscarActivo(id);
        if (docente != null) {
            verificarParticipacion(existente.getUnidad().getCurso(), docente);
        }
        existente.marcarInactivo();
        existente.setUltimaModificacion(LocalDateTime.now());
        terminoGlosarioRepositorio.save(existente);
    }

    /**
     * Indica si un usuario puede ver el glosario de una unidad.
     */
    @Override
    public boolean puedeVer(Unidad unidad, Usuario usuario) {
        return unidadServicio.puedeVerContenido(unidad, usuario);
    }

    private TerminoGlosario buscarActivo(Integer id) {
        return terminoGlosarioRepositorio.findById(id == null ? -1 : id).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El término no se encuentra activo."));
    }

    /** Verifica que el docente participe en el curso (como titular o ayudante). */
    private void verificarParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = participacionDocenteRepositorio.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
    }

    /** Valida los datos del término y devuelve el término sin espacios sobrantes. */
    private String validarDatos(String termino, String definicion) {
        if (termino == null || termino.isBlank() || definicion == null || definicion.isBlank()) {
            throw new IllegalArgumentException("Error! Debe completar el término y la definición.");
        }
        if (termino.trim().length() > TerminoGlosario.LONGITUD_TERMINO) {
            throw new IllegalArgumentException(
                    "Error! El término no puede superar los " + TerminoGlosario.LONGITUD_TERMINO + " caracteres.");
        }
        if (definicion.trim().length() > TerminoGlosario.LONGITUD_DEFINICION) {
            throw new IllegalArgumentException(
                    "Error! La definición no puede superar los " + TerminoGlosario.LONGITUD_DEFINICION + " caracteres.");
        }
        return termino.trim();
    }
}
