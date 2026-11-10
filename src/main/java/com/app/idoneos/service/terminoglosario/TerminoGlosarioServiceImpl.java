package com.app.idoneos.service.terminoglosario;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TerminoGlosario;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.repository.TerminoGlosarioRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de TerminoGlosarioService. */
@Service
public class TerminoGlosarioServiceImpl implements TerminoGlosarioService {

    @Autowired
    private TerminoGlosarioRepository terminoGlosarioRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private TemaService temaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el término del glosario por su identificador. */
    @Override
    public Optional<TerminoGlosario> buscarPorId(Integer id) {
        return terminoGlosarioRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<TerminoGlosario> obtenerTodo() {
        return terminoGlosarioRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return terminoGlosarioRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de terminoGlosario asociados a tema. */
    @Override
    public List<TerminoGlosario> buscarPorTema(Tema tema) {
        return terminoGlosarioRepository.findByTemaAndBajaFalse(tema);
    }

    @Override
    public Map<String, List<TerminoGlosario>> agruparPorLetra(List<TerminoGlosario> terminos) {
        Map<String, List<TerminoGlosario>> grupos = new LinkedHashMap<>();
        terminos.forEach(termino -> grupos.computeIfAbsent(termino.getLetraInicial(), letra -> new ArrayList<>())
                .add(termino));
        return grupos;
    }

    /** Busca los términos de un tema que contienen un texto, en orden alfabético. */
    @Override
    public List<TerminoGlosario> buscarConFiltros(Tema tema, String texto, boolean enDefiniciones) {
        String buscado = TextoUtil.normalizar(texto);
        return tema.getTerminosGlosarioOrdenados().stream()
                .sorted(Comparator.comparing((TerminoGlosario t) -> TextoUtil.normalizar(t.getTermino())))
                .filter(t -> buscado.isEmpty() || TextoUtil.normalizar(t.getTermino()).contains(buscado)
                        || (enDefiniciones && TextoUtil.normalizar(t.getDefinicion()).contains(buscado)))
                .toList();
    }

    /** Indica si un usuario puede ver el glosario de un tema. */
    @Override
    public boolean puedeVer(Tema tema, Usuario usuario) {
        return temaService.puedeVerContenido(tema, usuario);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el término del glosario en la base de datos. */
    @Override
    public TerminoGlosario guardar(TerminoGlosario entidad) {
        return terminoGlosarioRepository.save(entidad);
    }

    /** Registra un término en el glosario de un tema. */
    @Override
    @Transactional
    public TerminoGlosario registrar(Docente docente, Integer temaId, String termino, String definicion) {
        Tema tema = temaRepository.findById(temaId == null ? -1 : temaId)
                .filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        verificarParticipacion(tema.getCurso(), docente);
        String terminoLimpio = validarDatos(termino, definicion);
        String definicionLimpia = definicion.trim();

        List<TerminoGlosario> existentes = terminoGlosarioRepository.findByTema(tema);
        if (existentes.stream().anyMatch(t -> !t.getBaja() && t.getTermino().equalsIgnoreCase(terminoLimpio))) {
            throw RespuestaFormularioUtil.armarCampoInvalido("termino", "Error! El término ya está registrado en el glosario de este tema.");
        }
        // Un término dado de baja con el mismo nombre se vuelve a dar de alta (el glosario no admite repetidos)
        TerminoGlosario guardado = existentes.stream().filter(t -> t.getBaja() && t.getTermino().equals(terminoLimpio))
                .findFirst().orElseGet(() -> new TerminoGlosario(tema, terminoLimpio, definicionLimpia));
        guardado.setDefinicion(definicionLimpia);
        guardado.setBaja(false);
        guardado.setUltimaModificacion(LocalDateTime.now());
        return terminoGlosarioRepository.save(guardado);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public TerminoGlosario modificar(TerminoGlosario entidad) {
        return terminoGlosarioRepository.save(entidad);
    }

    /** Modifica un término del glosario. */
    @Override
    @Transactional
    public TerminoGlosario modificar(Integer id, Docente docente, String termino, String definicion) {
        TerminoGlosario existente = buscarActivo(id);
        verificarParticipacion(existente.getTema().getCurso(), docente);
        String terminoLimpio = validarDatos(termino, definicion);

        boolean repetido = terminoGlosarioRepository.findByTema(existente.getTema()).stream()
                .anyMatch(t -> t.getIdTermino() != existente.getIdTermino()
                        && (t.getBaja() ? t.getTermino().equals(terminoLimpio) : t.getTermino().equalsIgnoreCase(terminoLimpio)));
        if (repetido) {
            throw RespuestaFormularioUtil.armarCampoInvalido("termino", "Error! El término ya está registrado en el glosario de este tema.");
        }
        existente.setTermino(terminoLimpio);
        existente.setDefinicion(definicion.trim());
        existente.setUltimaModificacion(LocalDateTime.now());
        return terminoGlosarioRepository.save(existente);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(TerminoGlosario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        terminoGlosarioRepository.save(entidad);
    }

    /** Da de baja un término del glosario. */
    @Override
    @Transactional
    public void darDeBaja(Integer id, Docente docente) {
        TerminoGlosario existente = buscarActivo(id);
        if (docente != null) {
            verificarParticipacion(existente.getTema().getCurso(), docente);
        }
        existente.marcarInactivo();
        existente.setUltimaModificacion(LocalDateTime.now());
        terminoGlosarioRepository.save(existente);
    }

    private TerminoGlosario buscarActivo(Integer id) {
        return terminoGlosarioRepository.findById(id == null ? -1 : id).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El término no se encuentra activo."));
    }

    /** Verifica que el docente participe en el curso (como titular o ayudante). */
    private void verificarParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
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
            throw RespuestaFormularioUtil.armarCampoInvalido("termino", MensajeErrorUtil.armarDemasiadoLargo("El término", TerminoGlosario.LONGITUD_TERMINO, "caracteres"));
        }
        if (definicion.trim().length() > TerminoGlosario.LONGITUD_DEFINICION) {
            throw RespuestaFormularioUtil.armarCampoInvalido("definicion", MensajeErrorUtil.armarDemasiadoLargo("La definición", TerminoGlosario.LONGITUD_DEFINICION, "caracteres"));
        }
        return termino.trim();
    }
}
