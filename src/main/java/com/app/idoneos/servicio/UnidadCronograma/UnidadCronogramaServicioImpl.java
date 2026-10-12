package com.app.idoneos.servicio.UnidadCronograma;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.Programa;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.ProgramaRepositorio;
import com.app.idoneos.repositorio.UnidadCronogramaRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Unidad.UnidadServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link UnidadCronograma}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link UnidadCronogramaServicio}.
 */
@Service
public class UnidadCronogramaServicioImpl implements UnidadCronogramaServicio, CrudServicio<UnidadCronograma> {

    @Autowired
    private UnidadCronogramaRepositorio unidadCronogramaRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private UnidadServicio unidadServicio;

    /**
     * Guarda la unidad del cronograma en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public UnidadCronograma guardar(UnidadCronograma entidad) {
        return unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Busca la unidad del cronograma por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<UnidadCronograma> buscarPorId(Integer id) {
        return unidadCronogramaRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<UnidadCronograma> obtenerTodo() {
        return unidadCronogramaRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public UnidadCronograma modificar(UnidadCronograma entidad) {
        return unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(UnidadCronograma entidad) {
        entidad.marcarInactivo(); // Baja lógica
        unidadCronogramaRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return unidadCronogramaRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de unidadCronograma asociados a programa.
     *
     * @param programa el registro de Programa asociado
     * @return una lista de UnidadCronograma asociados al Programa indicado
     */
    @Override
    public List<UnidadCronograma> buscarPorPrograma(Programa programa) {
        return unidadCronogramaRepositorio.findByProgramaAndBajaFalse(programa);
    }

    /**
     * Busca los registros vigentes de unidadCronograma asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de UnidadCronograma asociados al Unidad indicado
     */
    @Override
    public List<UnidadCronograma> buscarPorUnidad(Unidad unidad) {
        return unidadCronogramaRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * Busca la unidad del cronograma a partir de la combinación única de sus relaciones. Incluye los registros dados de baja.
     *
     * @param programa el registro de Programa asociado
     * @param unidad el registro de Unidad asociado
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<UnidadCronograma> buscarPorProgramaYUnidad(Programa programa, Unidad unidad) {
        return unidadCronogramaRepositorio.findByProgramaAndUnidad(programa, unidad);
    }

    /**
     * CU-24: Modifica el cronograma de un programa (unidades, orden y semanas).
     */
    @Override
    @Transactional
    public void modificarCronograma(Docente docente, Integer programaId, List<Integer> unidadIds, List<String> semanas) {
        Programa programa = programaRepositorio.findById(programaId == null ? -1 : programaId)
                .filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = participacionDocenteRepositorio.findByCursoAndBajaFalse(programa.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
        if (programa.getCantidadInscripcionesActivas() > 0) {
            throw new IllegalArgumentException("Error! El programa tiene inscripciones activas: el cronograma no puede "
                    + "modificarse porque es lo que el alumno vio y pagó al inscribirse.");
        }

        // Se valida todo lo que se recibió antes de cambiar algo
        List<Integer> ids = unidadIds == null ? List.of() : unidadIds;
        List<String> duraciones = semanas == null ? List.of() : semanas;
        if (ids.size() != duraciones.size() || ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("Error! Los datos del cronograma no son válidos.");
        }
        List<Integer> semanasPorUnidad = new ArrayList<>();
        for (String duracion : duraciones) {
            semanasPorUnidad.add(parsearSemanas(duracion));
        }
        Map<Integer, Unidad> unidades = new HashMap<>();
        for (Integer id : ids) {
            Unidad unidad = unidadRepositorio.findById(id == null ? -1 : id)
                    .filter(u -> !u.esInactivo() && u.getCurso().getIdCurso() == programa.getCurso().getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Error! Una de las unidades no se encuentra activa en este curso."));
            if (!unidad.perteneceAlCronograma(programa) && unidad.getProgramasQueLaIncluyen().isEmpty()) {
                throw new IllegalArgumentException("Error! La unidad '" + unidad.getTitulo()
                        + "' no se puede incorporar: no forma parte del cronograma de ningún programa del curso.");
            }
            unidades.put(id, unidad);
        }

        // Las unidades que ya no figuran se dan de baja del cronograma (si no están en otro programa, quedan de baja)
        Set<Integer> conservadas = Set.copyOf(ids);
        List<Integer> quitadas = programa.getUnidadesCronograma().stream().map(c -> c.getUnidad().getIdUnidad())
                .filter(id -> !conservadas.contains(id)).toList();
        if (!quitadas.isEmpty()) {
            unidadServicio.darDeBajaVarias(quitadas, programa.getIdPrograma(), docente);
        }

        // Números provisionales para poder reubicar sin repetir un número de orden del programa
        Map<Integer, UnidadCronograma> existentes = programa.getUnidadesCronograma().stream()
                .collect(Collectors.toMap(c -> c.getUnidad().getIdUnidad(), c -> c));
        int provisorio = -1;
        for (UnidadCronograma cronograma : existentes.values()) {
            cronograma.setNumeroOrden(provisorio--);
            unidadCronogramaRepositorio.saveAndFlush(cronograma);
        }
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 0; i < ids.size(); i++) {
            UnidadCronograma cronograma = existentes.get(ids.get(i));
            if (cronograma == null) {
                Unidad unidad = unidades.get(ids.get(i));
                cronograma = new UnidadCronograma(programa, unidad, i + 1, semanasPorUnidad.get(i));
                cronograma = unidadCronogramaRepositorio.saveAndFlush(cronograma);
                programa.getUnidadesCronograma().add(cronograma);
                unidad.getUnidadesCronograma().add(cronograma);
            } else {
                cronograma.setNumeroOrden(i + 1);
                cronograma.setSemanasDuracion(semanasPorUnidad.get(i));
                cronograma.setUltimaModificacion(ahora);
                unidadCronogramaRepositorio.saveAndFlush(cronograma);
            }
        }
        programa.setUltimaModificacion(ahora);
        programaRepositorio.save(programa);
    }

    /** Convierte la duración recibida en un número entero de semanas mayor a cero. */
    private int parsearSemanas(String duracion) {
        try {
            int valor = Integer.parseInt(duracion == null ? "" : duracion.trim());
            if (valor > 0) {
                return valor;
            }
        } catch (NumberFormatException e) {
            // se informa abajo, igual que un valor menor o igual a cero
        }
        throw new IllegalArgumentException(
                "Error! La duración de cada unidad debe ser un número entero de semanas mayor a cero.");
    }
}
