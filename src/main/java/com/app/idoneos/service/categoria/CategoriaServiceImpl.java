package com.app.idoneos.service.categoria;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.repository.CategoriaRepository;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Categoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CategoriaService}.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la categoría por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Categoria> obtenerTodo() {
        return categoriaRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return categoriaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca la categoría por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    /**
     * Busca categorías por nombre y, opcionalmente, según estén dadas de baja o vigentes.
     *
     * @param nombre parte del nombre de la categoría (opcional)
     * @param baja {@code true} para solo las dadas de baja, {@code false} para solo las vigentes, {@code null} para todas
     * @param orden el orden de los resultados: "nombre" (A–Z, por defecto) o "recientes" (más nuevas primero);
     *              las dadas de baja van siempre al final
     * @return una lista de categorías que cumplen los criterios
     */
    @Override
    public List<Categoria> buscarConFiltros(String nombre, Boolean baja, String orden) {
        String criterio = nombre == null ? "" : nombre.trim().toLowerCase();
        return categoriaRepository.findAll().stream()
                .filter(categoria -> baja == null || categoria.getBaja().equals(baja))
                .filter(categoria -> criterio.isEmpty() || categoria.getNombre().toLowerCase().contains(criterio))
                .sorted(Comparator.comparing(Categoria::esInactivo)
                        .thenComparing("recientes".equals(orden)
                                ? Comparator.comparing(Categoria::getFechaCreacion)
                                        .thenComparingInt(Categoria::getIdCategoria).reversed()
                                : Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    /**
     * Cuenta las inscripciones activas asociadas a una categoría, a través de sus cursos.
     *
     * @param categoria la categoría a consultar
     * @return la cantidad de inscripciones activas
     */
    @Override
    public long contarInscripcionesActivas(Categoria categoria) {
        return inscripcionRepository.contarPorCategoria(categoria);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la categoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Categoria guardar(Categoria entidad) {
        return categoriaRepository.save(entidad);
    }

    /**
     * Registra una nueva categoría. Si existe una categoría dada de baja con el mismo nombre, la reactiva.
     *
     * @param nombre el nombre de la categoría
     * @param descripcion la descripción de la categoría (opcional)
     * @return la categoría registrada
     * @throws IllegalArgumentException si el nombre está vacío o ya existe una categoría activa con ese nombre
     */
    @Override
    @Transactional
    public Categoria registrar(String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! El nombre de la categoría es obligatorio.");
        }
        String nombreLimpio = nombre.trim();
        String descripcionLimpia = (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
        Categoria existente = categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getNombre().equalsIgnoreCase(nombreLimpio)).findFirst().orElse(null);
        if (existente != null && !existente.esInactivo()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Ya existe una categoría activa con el nombre '" + nombreLimpio + "'.");
        }
        if (existente != null) {
            // Reactiva la categoría dada de baja con el mismo nombre (el nombre es único).
            existente.setNombre(nombreLimpio);
            existente.setDescripcion(descripcionLimpia);
            existente.setBaja(false);
            existente.setUltimaModificacion(LocalDateTime.now());
            return categoriaRepository.save(existente);
        }
        Categoria categoria = new Categoria(nombreLimpio);
        categoria.setDescripcion(descripcionLimpia);
        return categoriaRepository.save(categoria);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Categoria modificar(Categoria entidad) {
        return categoriaRepository.save(entidad);
    }

    /**
     * Modifica el nombre y la descripción de una categoría activa sin inscripciones activas asociadas.
     *
     * @param idCategoria el identificador de la categoría
     * @param nombre el nuevo nombre
     * @param descripcion la nueva descripción (opcional)
     * @return la categoría modificada
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    @Override
    @Transactional
    public Categoria modificar(Integer idCategoria, String nombre, String descripcion) {
        Categoria categoria = categoriaRepository.findById(idCategoria).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría no se encuentra activa."));
        long inscripcionesActivas = contarInscripcionesActivas(categoria);
        if (inscripcionesActivas > 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarAccionBloqueada("modificar", "la categoría", categoria.getNombre(),
                    "tiene " + MensajeErrorUtil.formatearCantidad(inscripcionesActivas, "inscripción activa asociada",
                            "inscripciones activas asociadas"), ""));
        }
        if (nombre == null || nombre.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! El nombre de la categoría no puede quedar vacío.");
        }
        String nombreLimpio = nombre.trim();
        for (Categoria otra : categoriaRepository.findAll()) {
            if (otra.getIdCategoria() != idCategoria && otra.getNombre().equalsIgnoreCase(nombreLimpio)) {
                throw RespuestaFormularioUtil.armarCampoInvalido("nombre", otra.esInactivo()
                        ? "Error! Ya existe una categoría dada de baja con el nombre '" + nombreLimpio + "'."
                        : "Error! El nombre coincide con el de otra categoría activa.");
            }
        }
        categoria.setNombre(nombreLimpio);
        categoria.setDescripcion((descripcion == null || descripcion.isBlank()) ? null : descripcion.trim());
        categoria.setUltimaModificacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Categoria entidad) {
        entidad.marcarInactivo(); // Baja lógica
        categoriaRepository.save(entidad);
    }

    /**
     * Da de baja una categoría activa que no tenga cursos activos asociados.
     *
     * @param idCategoria el identificador de la categoría
     * @throws IllegalArgumentException si la categoría no está activa o tiene cursos activos asociados
     */
    @Override
    @Transactional
    public void darDeBaja(Integer idCategoria) {
        Categoria categoria = categoriaRepository.findById(idCategoria).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría no se encuentra activa."));
        int cursosActivos = cursoRepository.findByCategoriaAndBajaFalse(categoria).size();
        if (cursosActivos > 0) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("la categoría", categoria.getNombre(),
                    "tiene " + MensajeErrorUtil.formatearCantidad(cursosActivos, "curso activo asociado", "cursos activos asociados"),
                    "Dé de baja primero " + (cursosActivos == 1 ? "ese curso." : "esos cursos.")));
        }
        categoria.marcarInactivo();
        categoria.setUltimaModificacion(LocalDateTime.now());
        categoriaRepository.save(categoria);
    }

    /**
     * Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja, no se da de baja
     * ninguno y el mensaje indica cuál lo impidió.
     *
     * @param ids los identificadores de los registros
     * @throws IllegalArgumentException si no se indicó ningún registro o alguno no puede darse de baja
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un registro.");
        }
        for (Integer id : ids) {
            try {
                darDeBaja(id);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }
}
