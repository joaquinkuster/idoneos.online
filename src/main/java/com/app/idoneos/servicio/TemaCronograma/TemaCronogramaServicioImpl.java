package com.app.idoneos.servicio.TemaCronograma;

import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Parametro;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaCronograma;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.TemaCronogramaRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.Tema.TemaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TemaCronograma}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TemaCronogramaServicio}.
 */
@Service
public class TemaCronogramaServicioImpl implements TemaCronogramaServicio, CrudServicio<TemaCronograma> {

    @Autowired
    private TemaCronogramaRepositorio temaCronogramaRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    @Autowired
    private TemaServicio temaServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Autowired
    private com.app.idoneos.servicio.Programa.MinimoDeContenidoValidador minimoDeContenido;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el tema del cronograma por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TemaCronograma> buscarPorId(Integer id) {
        return temaCronogramaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TemaCronograma> obtenerTodo() {
        return temaCronogramaRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return temaCronogramaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de temaCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de TemaCronograma asociados al Programa indicado
     */
    @Override
    public List<TemaCronograma> buscarPorPrograma(Programa programa) {
        return temaCronogramaRepositorio.findByProgramaAndBajaFalse(programa);
    }

    /**
     * Busca los registros vigentes de temaCronograma asociados a tema.
     *
     * @param tema el registro de Tema asociado
     * @return una lista de TemaCronograma asociados al Tema indicado
     */
    @Override
    public List<TemaCronograma> buscarPorTema(Tema tema) {
        return temaCronogramaRepositorio.findByTemaAndBajaFalse(tema);
    }

    /**
     * Busca el tema del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param tema el registro de Tema asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<TemaCronograma> buscarPorProgramaYTema(Programa programa, Tema tema) {
        return temaCronogramaRepositorio.findByProgramaAndTema(programa, tema);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el tema del cronograma en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TemaCronograma guardar(TemaCronograma entidad) {
        return temaCronogramaRepositorio.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TemaCronograma modificar(TemaCronograma entidad) {
        return temaCronogramaRepositorio.save(entidad);
    }

    /**
     * CU-24: Modifica el cronograma de un programa (temas, orden y días).
     */
    @Override
    @Transactional
    public void modificarCronograma(Docente docente, Integer programaId, List<Integer> temaIds, List<String> dias) {
        Programa programa = programaRepositorio.findById(programaId == null ? -1 : programaId)
                .filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = docenteCursoRepositorio.findByCursoAndBajaFalse(programa.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
        if (programa.getCantidadInscripcionesActivas() > 0) {
            throw new IllegalArgumentException("Error! El programa tiene inscripciones activas: el cronograma no puede "
                    + "modificarse porque es lo que el alumno vio y pagó al inscribirse.");
        }

        // Se valida todo lo que se recibió antes de cambiar algo
        List<Integer> ids = temaIds == null ? List.of() : temaIds;
        List<String> duraciones = dias == null ? List.of() : dias;
        if (ids.size() != duraciones.size() || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.formatoInvalido("Los datos del cronograma", "una duración entera en días para cada tema del programa"));
        }
        List<Integer> diasPorTema = new ArrayList<>();
        for (String duracion : duraciones) {
            diasPorTema.add(parsearDias(duracion));
        }
        // El cronograma no puede superar la duración del curso
        int maximo = programa.getCurso().getDuracionDias();
        int total = diasPorTema.stream().mapToInt(Integer::intValue).sum();
        if (total > maximo) {
            throw new IllegalArgumentException(MensajeErrorUtilidad.demasiadoLargo("El cronograma", maximo, "días"));
        }
        // Los días de acceso de cada cohorte activa del programa no pueden quedar por debajo de la nueva duración
        for (com.app.idoneos.modelo.Cohorte cohorte : programa.getCohortesActivas()) {
            if (cohorte.getAccesoDias() < total) {
                throw new IllegalArgumentException("Error! La cohorte «" + cohorte.getEtiqueta() + "» tiene "
                        + MensajeErrorUtilidad.cantidad(cohorte.getAccesoDias(), "día de acceso", "días de acceso")
                        + ", menos que los " + total + " del cronograma. Modificá la cohorte o acortá el cronograma.");
            }
        }
        Map<Integer, Tema> temas = new HashMap<>();
        for (Integer id : ids) {
            Tema tema = temaRepositorio.findById(id == null ? -1 : id)
                    .filter(u -> !u.esInactivo() && u.getCurso().getIdCurso() == programa.getCurso().getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! Uno de los temas no se encuentra activa en este curso."));
            if (!tema.perteneceAlCronograma(programa) && tema.getProgramasQueLaIncluyen().isEmpty()) {
                throw new IllegalArgumentException("Error! El tema '" + tema.getTitulo()
                        + "' no se puede incorporar: no forma parte del cronograma de ningún programa del curso.");
            }
            temas.put(id, tema);
        }
        if (programa.tieneCohortesEnCurso()) {
            List<String> sinAutoevaluacion = ids.stream().map(temas::get)
                    .filter(u -> u.getAutoevaluacionesPublicadas().isEmpty()).map(u -> "«" + u.getTitulo() + "»").toList();
            if (!sinAutoevaluacion.isEmpty()) {
                throw new IllegalArgumentException("Error! El programa tiene cohortes en curso: todos los temas del "
                        + "cronograma deben tener al menos una autoevaluación visible. Faltan en: "
                        + String.join(", ", sinAutoevaluacion) + ".");
            }
        }

        if (programa.tieneCohortesEnCurso()) {
            // El cronograma que se guarda debe conservar el mínimo de temas
            int minimo = minimoDeContenido.minimo();
            if (ids.size() < programa.getCantidadTemas() && ids.size() < minimo) {
                throw new IllegalArgumentException("Error! El programa tiene cohortes en curso: debe conservar al menos "
                        + MensajeErrorUtilidad.cantidad(minimo, "tema", "temas")
                        + " en su cronograma (quedaría con " + ids.size() + ").");
            }
        }

        // Los temas que ya no figuran se dan de baja del cronograma (si no están en otro programa, quedan de baja)
        Set<Integer> conservadas = Set.copyOf(ids);
        List<Integer> quitadas = programa.getTemasCronograma().stream().map(c -> c.getTema().getIdTema())
                .filter(id -> !conservadas.contains(id)).toList();
        if (!quitadas.isEmpty()) {
            temaServicio.darDeBajaVarias(quitadas, programa.getIdPrograma(), docente);
        }

        // Números provisionales para poder reubicar sin repetir un número de orden del programa
        Map<Integer, TemaCronograma> existentes = programa.getTemasCronograma().stream()
                .collect(Collectors.toMap(c -> c.getTema().getIdTema(), c -> c));
        int provisorio = -1;
        for (TemaCronograma cronograma : existentes.values()) {
            cronograma.setNumeroOrden(provisorio--);
            temaCronogramaRepositorio.saveAndFlush(cronograma);
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 0; i < ids.size(); i++) {
            TemaCronograma cronograma = existentes.get(ids.get(i));
            if (cronograma == null) {
                Tema tema = temas.get(ids.get(i));
                cronograma = new TemaCronograma(programa, tema, i + 1, diasPorTema.get(i));
                cronograma = temaCronogramaRepositorio.saveAndFlush(cronograma);
                programa.getTemasCronograma().add(cronograma);
                tema.getTemasCronograma().add(cronograma);
            } else {
                cronograma.setNumeroOrden(i + 1);
                cronograma.setDuracionDias(diasPorTema.get(i));
                cronograma.setUltimaModificacion(ahora);
                temaCronogramaRepositorio.saveAndFlush(cronograma);
            }
        }
        programa.setUltimaModificacion(ahora);
        programaRepositorio.save(programa);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TemaCronograma entidad) {
        entidad.marcarInactivo(); // Baja lógica
        temaCronogramaRepositorio.save(entidad);
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
                MensajeErrorUtilidad.formatoInvalido("La duración de cada tema", "número entero de días mayor a cero"));
    }
}
