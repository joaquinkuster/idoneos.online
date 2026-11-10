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

/** Implementación de CategoriaService. */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la categoría por su identificador. */
    @Override
    public Optional<Categoria> buscarPorId(Integer id) {
        return categoriaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Categoria> obtenerTodo() {
        return categoriaRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return categoriaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca la categoría por su atributo único 'nombre'. */
    @Override
    public Optional<Categoria> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre);
    }

    /** Busca categorías por nombre y, opcionalmente, según estén dadas de baja o vigentes. */
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

    /** Cuenta las inscripciones activas asociadas a una categoría, a través de sus cursos. */
    @Override
    public long contarInscripcionesActivas(Categoria categoria) {
        return inscripcionRepository.contarPorCategoria(categoria);
    }

    @Override
    public boolean tieneCursosConCohortesPublicadas(Categoria categoria) {
        return cursoRepository.findByCategoriaAndBajaFalse(categoria).stream().anyMatch(com.app.idoneos.model.Curso::tieneCohortesPublicadas);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la categoría en la base de datos. */
    @Override
    public Categoria guardar(Categoria entidad) {
        return categoriaRepository.save(entidad);
    }

    /** Registra una nueva categoría. */
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

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Categoria modificar(Categoria entidad) {
        return categoriaRepository.save(entidad);
    }

    /** Modifica el nombre y la descripción de una categoría activa sin inscripciones activas asociadas. */
    @Override
    @Transactional
    public Categoria modificar(Integer idCategoria, String nombre, String descripcion) {
        Categoria categoria = categoriaRepository.findById(idCategoria).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría no se encuentra activa."));
        if (tieneCursosConCohortesPublicadas(categoria)) {
            throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("la categoría", categoria.getNombre(),
                    "hay cursos con cohortes publicadas en el catálogo", MensajeErrorUtil.CIERRE_DISENO));
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

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Categoria entidad) {
        entidad.marcarInactivo(); // Baja lógica
        categoriaRepository.save(entidad);
    }

    /** Da de baja una categoría activa que no tenga cursos activos asociados. */
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

    /** Da de baja varios registros a la vez, todos o ninguno: si alguno no puede darse de baja. */
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
