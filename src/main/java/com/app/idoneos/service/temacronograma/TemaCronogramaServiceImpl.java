package com.app.idoneos.service.temacronograma;

import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.repository.TemaCronogramaRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.service.tema.TemaService;
import com.app.idoneos.util.DuracionUtil;
import com.app.idoneos.util.MensajeErrorUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de TemaCronogramaService. */
@Service
public class TemaCronogramaServiceImpl implements TemaCronogramaService {

    @Autowired
    private TemaCronogramaRepository temaCronogramaRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private TemaService temaService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.app.idoneos.service.curso.CursoService cursoService;

    @Autowired
    private com.app.idoneos.service.programa.ProgramaService programaService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el tema del cronograma por su identificador. */
    @Override
    public Optional<TemaCronograma> buscarPorId(Integer id) {
        return temaCronogramaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<TemaCronograma> obtenerTodo() {
        return temaCronogramaRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return temaCronogramaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de temaCronograma asociados a tema. */
    @Override
    public List<TemaCronograma> buscarPorTema(Tema tema) {
        return temaCronogramaRepository.findByTemaAndBajaFalse(tema);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el tema del cronograma en la base de datos. */
    @Override
    public TemaCronograma guardar(TemaCronograma entidad) {
        return temaCronogramaRepository.save(entidad);
    }

    /** Arma el motivo de un bloqueo, que se da si el programa tiene cohortes activas. */
    private String motivo(Programa programa, String detalle) {
        return programa.tieneCohortesActivas() ? MensajeErrorUtil.motivoDisenoIncompleto(detalle) : detalle;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public TemaCronograma modificar(TemaCronograma entidad) {
        return temaCronogramaRepository.save(entidad);
    }

    /** Modifica el cronograma de un programa (temas, orden y días). */
    @Override
    @Transactional
    public void modificar(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias) {
        guardarCronograma(docente, programaId, temaIds, dias, false);
    }

    @Override
    @Transactional
    public void definirDeProgramaNuevo(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias) {
        guardarCronograma(docente, programaId, temaIds, dias, true);
    }

    private void guardarCronograma(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias, boolean programaNuevo) {
        Programa programa = programaRepository.findById(programaId == null ? -1 : programaId)
                .filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = docenteCursoRepository.findByCursoAndBajaFalse(programa.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
        if (!programaNuevo && programa.estaCongelado()) {
            throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("el cronograma del programa",
                    programa.getNombre(), programa.getMotivoDeCongelado(), MensajeErrorUtil.CIERRE_DISENO));
        }

        // Se valida todo lo que se recibió antes de cambiar algo
        List<Integer> ids = temaIds == null ? List.of() : temaIds;
        List<String> duraciones = dias == null ? List.of() : dias;
        if (ids.size() != duraciones.size() || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarFormatoInvalido("Los datos del cronograma", "una duración entera en días para cada tema del programa"));
        }
        List<Integer> diasPorTema = new ArrayList<>();
        for (String duracion : duraciones) {
            diasPorTema.add(parsearDias(duracion));
        }
        // El cronograma no puede superar la duración del curso
        int maximo = programa.getCurso().getDuracionDias();
        int total = diasPorTema.stream().mapToInt(Integer::intValue).sum();
        if (total > maximo) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarDemasiadoLargo("El cronograma", maximo, "días"));
        }
        // Un programa siempre está completo: tiene como mínimo los temas que exige el sistema
        int minimoDeTemas = programaService.buscarMinimoDeContenido();
        if (ids.size() < minimoDeTemas) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el cronograma del programa",
                    programa.getNombre(), "tiene " + MensajeErrorUtil.formatearCantidad(ids.size(), "tema", "temas")
                            + " y hacen falta al menos " + minimoDeTemas,
                    "Sumá temas con el contenido completo hasta llegar al mínimo."));
        }
        // Los días de los temas tienen que sumar siempre la duración del curso: la define el administrador, ni más ni menos
        if (total != maximo) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el cronograma del programa",
                    programa.getNombre(), motivo(programa, "asigna " + DuracionUtil.formatear(total)
                            + " y el curso dura " + DuracionUtil.formatear(maximo)),
                    "Asigná entre los temas los " + DuracionUtil.formatear(maximo) + " del curso."));
        }
        // Los días de acceso de cada cohorte activa del programa no pueden quedar por debajo de la nueva duración
        for (com.app.idoneos.model.Cohorte cohorte : programa.getCohortesActivas()) {
            if (cohorte.getAccesoDias() < total) {
                throw new IllegalArgumentException("Error! La cohorte «" + cohorte.getEtiqueta() + "» tiene "
                        + MensajeErrorUtil.formatearCantidad(cohorte.getAccesoDias(), "día de acceso", "días de acceso")
                        + ", menos que los " + total + " del cronograma. Modificá la cohorte o acortá el cronograma.");
            }
        }
        Map<Integer, Tema> temas = new HashMap<>();
        for (Integer id : ids) {
            Tema tema = temaRepository.findById(id == null ? -1 : id)
                    .filter(u -> !u.esInactivo() && u.getCurso().getIdCurso() == programa.getCurso().getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! Uno de los temas no se encuentra activa en este curso."));
            temas.put(id, tema);
        }
        if (programa.tieneCohortesActivas()) {
            List<String> sinAutoevaluacion = ids.stream().map(temas::get)
                    .filter(u -> u.getAutoevaluacionesPublicadas().isEmpty()).map(u -> "«" + u.getTitulo() + "»").toList();
            if (!sinAutoevaluacion.isEmpty()) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el cronograma del programa",
                        programa.getNombre(), MensajeErrorUtil.motivoDisenoIncompleto("incluye "
                                + (sinAutoevaluacion.size() == 1 ? "un tema sin autoevaluación visible: " : "temas sin autoevaluación visible: ")
                                + String.join(", ", sinAutoevaluacion)),
                        "Registrá una autoevaluación en " + (sinAutoevaluacion.size() == 1 ? "ese tema" : "esos temas") + " antes."));
            }
        }
        // Un tema que se incorpora al cronograma tiene que traer el contenido completo que pide el diseño del curso
        Set<Integer> yIncluidos = programa.getTemasCronograma().stream().map(c -> c.getTema().getIdTema()).collect(Collectors.toSet());
        for (Integer id : ids) {
            if (yIncluidos.contains(id)) {
                continue;
            }
            Tema nuevo = temas.get(id);
            List<String> faltantes = cursoService.buscarContenidoDeTema(programa.getCurso(), nuevo).getFaltantes();
            if (!faltantes.isEmpty()) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el cronograma del programa",
                        programa.getNombre(), motivo(programa, "incorpora el tema «" + nuevo.getTitulo()
                                + "», al que le falta contenido (" + String.join(", ", faltantes).toLowerCase() + ")"),
                        "Completá su contenido antes."));
            }
        }

        if (programa.tieneCohortesActivas()) {
            // El cronograma que se guarda debe conservar el mínimo de temas
            int minimo = programaService.buscarMinimoDeContenido();
            if (ids.size() < programa.getCantidadTemas() && ids.size() < minimo) {
                throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "el cronograma del programa",
                        programa.getNombre(), MensajeErrorUtil.motivoDisenoIncompleto("debe conservar al menos "
                                + MensajeErrorUtil.formatearCantidad(minimo, "tema", "temas") + " y quedaría con " + ids.size()),
                        "Mantené al menos " + MensajeErrorUtil.formatearCantidad(minimo, "tema", "temas") + " en el cronograma."));
            }
        }

        // Los temas que ya no figuran se dan de baja del cronograma (si no están en otro programa, quedan de baja)
        Set<Integer> conservadas = Set.copyOf(ids);
        List<Integer> quitadas = programa.getTemasCronograma().stream().map(c -> c.getTema().getIdTema())
                .filter(id -> !conservadas.contains(id)).toList();
        if (!quitadas.isEmpty()) {
            temaService.quitarVariasDelCronograma(quitadas, programa.getIdPrograma(), docente);
        }

        // Números provisionales para poder reubicar sin repetir un número de orden del programa
        Map<Integer, TemaCronograma> existentes = programa.getTemasCronograma().stream()
                .collect(Collectors.toMap(c -> c.getTema().getIdTema(), c -> c));
        int provisorio = -1;
        for (TemaCronograma cronograma : existentes.values()) {
            cronograma.setNumeroOrden(provisorio--);
            temaCronogramaRepository.saveAndFlush(cronograma);
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 0; i < ids.size(); i++) {
            TemaCronograma cronograma = existentes.get(ids.get(i));
            if (cronograma == null) {
                Tema tema = temas.get(ids.get(i));
                cronograma = new TemaCronograma(programa, tema, i + 1, diasPorTema.get(i));
                cronograma = temaCronogramaRepository.saveAndFlush(cronograma);
                programa.getTemasCronograma().add(cronograma);
                tema.getTemasCronograma().add(cronograma);
            } else {
                cronograma.setNumeroOrden(i + 1);
                cronograma.setDuracionDias(diasPorTema.get(i));
                cronograma.setUltimaModificacion(ahora);
                temaCronogramaRepository.saveAndFlush(cronograma);
            }
        }
        programa.setUltimaModificacion(ahora);
        programaRepository.save(programa);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(TemaCronograma entidad) {
        entidad.marcarInactivo(); // Baja lógica
        temaCronogramaRepository.save(entidad);
    }

    /** Convierte la duración recibida en un número entero de días mayor a cero. */
    private int parsearDias(String duracion) {
        try {
            int valor = Integer.parseInt(duracion == null ? "" : duracion.trim());
            if (valor > 0) {
                return valor;
            }
        } catch (NumberFormatException e) {
            // se informa abajo, igual que un valor menor o igual a cero
        }
        throw new IllegalArgumentException(
                MensajeErrorUtil.armarFormatoInvalido("La duración de cada tema", "número entero de días mayor a cero"));
    }
}
