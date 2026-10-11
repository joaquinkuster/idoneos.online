package com.app.idoneos.servicio.Material;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.ParticipacionDocente;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.repositorio.ParticipacionDocenteRepositorio;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.repositorio.UnidadRepositorio;
import com.app.idoneos.utilidades.ArchivoUtilidad;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.modelo.Inscripcion;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Material}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link MaterialServicio}.
 */
@Service
public class MaterialServicioImpl implements MaterialServicio, CrudServicio<Material> {

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

    @Autowired
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    /**
     * Guarda el material en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Material guardar(Material entidad) {
        return materialRepositorio.save(entidad);
    }

    /**
     * Busca el material por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Material> buscarPorId(Integer id) {
        return materialRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Material> obtenerTodo() {
        return materialRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Material modificar(Material entidad) {
        return materialRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Material entidad) {
        entidad.marcarInactivo(); // Baja lógica
        materialRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return materialRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de material asociados a participacionDocente.
     *
     * @param participacionDocente el registro de ParticipacionDocente asociado
     * @return una lista de Material asociados al ParticipacionDocente indicado
     */
    @Override
    public List<Material> buscarPorParticipacionDocente(ParticipacionDocente participacionDocente) {
        return materialRepositorio.findByParticipacionDocenteAndBajaFalse(participacionDocente);
    }

    /**
     * Busca los registros vigentes de material asociados a tipoMaterial.
     *
     * @param tipoMaterial el registro de TipoMaterial asociado
     * @return una lista de Material asociados al TipoMaterial indicado
     */
    @Override
    public List<Material> buscarPorTipoMaterial(TipoMaterial tipoMaterial) {
        return materialRepositorio.findByTipoMaterialAndBajaFalse(tipoMaterial);
    }

    /**
     * Busca los registros vigentes de material asociados a unidad.
     *
     * @param unidad el registro de Unidad asociado
     * @return una lista de Material asociados al Unidad indicado
     */
    @Override
    public List<Material> buscarPorUnidad(Unidad unidad) {
        return materialRepositorio.findByUnidadAndBajaFalse(unidad);
    }

    /**
     * Busca el material (incluido el no publicado) aplicando filtros opcionales. Los dados de baja se ordenan
     * siempre al final.
     */
    @Override
    public List<Material> buscarConFiltros(Integer idCurso, Integer idUnidad, Integer idTipoMaterial, String titulo,
            Boolean generadoPorIa, Boolean baja, Docente soloDeDocente) {
        String criterio = titulo == null ? "" : titulo.trim().toLowerCase();
        return materialRepositorio.findAll().stream()
                .filter(m -> idCurso == null || m.getCurso().getIdCurso() == idCurso)
                .filter(m -> idUnidad == null || m.getUnidad().getIdUnidad() == idUnidad)
                .filter(m -> idTipoMaterial == null || m.getTipoMaterial().getIdTipoMaterial() == idTipoMaterial)
                .filter(m -> criterio.isEmpty() || m.getTitulo().toLowerCase().contains(criterio))
                .filter(m -> generadoPorIa == null || m.getGeneradoPorIa().equals(generadoPorIa))
                .filter(m -> baja == null || m.getBaja().equals(baja))
                .filter(m -> soloDeDocente == null || participaEnElCurso(m.getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(Material::getBaja)
                        .thenComparingInt((Material m) -> m.getUnidad().getIdUnidad())
                        .thenComparingInt(Material::getIdMaterial))
                .toList();
    }

    /**
     * Sube un material a una unidad activa. Si el archivo no puede guardarse, no queda registrado el material.
     */
    @Override
    @Transactional
    public Material subirMaterial(Docente docente, Integer idUnidad, Integer idTipoMaterial, String titulo,
            boolean oculto, String autor, String enlace, MultipartFile archivo) {
        Unidad unidad = unidadRepositorio.findById(idUnidad == null ? -1 : idUnidad).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La unidad no se encuentra activa."));
        ParticipacionDocente participacion = obtenerParticipacion(unidad.getCurso(), docente);
        TipoMaterial tipo = tipoMaterialRepositorio.findById(idTipoMaterial == null ? -1 : idTipoMaterial)
                .filter(TipoMaterial::permiteSubidaManual)
                .orElseThrow(() -> new IllegalArgumentException("Error! Debe seleccionar un tipo de material válido."));
        String tituloLimpio = validarTitulo(titulo);
        String autorLimpio = validarAutor(autor);
        String enlaceLimpio = vacioANulo(enlace);
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatosSegunTipo(tipo, hayArchivo, enlaceLimpio, true);
        if (hayArchivo) {
            ArchivoUtilidad.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }

        Material material = new Material(participacion, tipo, unidad, tituloLimpio);
        material.setAutor(autorLimpio);
        material.setOculto(oculto);
        material.setRutaArchivo(enlaceLimpio);
        material = materialRepositorio.saveAndFlush(material); // para conocer su identificador
        if (hayArchivo) {
            material.setRutaArchivo(guardarArchivo(material, archivo));
            material = materialRepositorio.save(material);
        }
        return material;
    }

    /**
     * Modifica un material activo. Si se envía un archivo o enlace nuevo, reemplaza al actual.
     */
    @Override
    @Transactional
    public Material modificarMaterial(Integer idMaterial, Docente docente, String titulo, boolean oculto, String autor,
            String enlace, MultipartFile archivo) {
        Material material = materialRepositorio.findById(idMaterial == null ? -1 : idMaterial)
                .filter(m -> !m.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El material no se encuentra activo."));
        obtenerParticipacion(material.getCurso(), docente);
        TipoMaterial tipo = material.getTipoMaterial();
        if (!tipo.permiteSubidaManual()) {
            throw new IllegalArgumentException("Error! Este tipo de material no se puede modificar manualmente.");
        }
        String tituloLimpio = validarTitulo(titulo);
        String autorLimpio = validarAutor(autor);
        String enlaceLimpio = vacioANulo(enlace);
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatosSegunTipo(tipo, hayArchivo, enlaceLimpio, false);
        if (hayArchivo) {
            ArchivoUtilidad.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }

        String rutaAnterior = material.getRutaArchivo();
        if (hayArchivo) {
            String rutaNueva = guardarArchivo(material, archivo);
            if (rutaAnterior != null && !rutaAnterior.equals(rutaNueva)) {
                ArchivoUtilidad.eliminar(rutaAnterior, directorioCursos); // cambió de formato: no queda el viejo
            }
            material.setRutaArchivo(rutaNueva);
        } else if (enlaceLimpio != null) {
            ArchivoUtilidad.eliminar(rutaAnterior, directorioCursos);
            material.setRutaArchivo(enlaceLimpio);
        }
        material.setTitulo(tituloLimpio);
        material.setAutor(autorLimpio);
        material.setOculto(oculto);
        material.setUltimaModificacion(LocalDateTime.now());
        return materialRepositorio.save(material);
    }

    /**
     * Da de baja un material activo (baja lógica: el archivo se conserva).
     */
    @Override
    @Transactional
    public void darDeBajaMaterial(Integer idMaterial, Docente docente) {
        darDeBaja(validarBaja(idMaterial, docente));
    }

    /**
     * Da de baja varios materiales. Es todo o nada: primero se valida cada uno y recién después se dan de baja.
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un material.");
        }
        List<Material> materiales = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        materiales.forEach(this::darDeBaja);
    }

    /**
     * Verifica que el material esté activo y que el docente (si se indica) participe en su curso.
     */
    private Material validarBaja(Integer idMaterial, Docente docente) {
        Material material = materialRepositorio.findById(idMaterial == null ? -1 : idMaterial)
                .filter(m -> !m.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El material no se encuentra activo."));
        if (docente != null) {
            obtenerParticipacion(material.getCurso(), docente);
        }
        return material;
    }

    private void darDeBaja(Material material) {
        material.marcarInactivo();
        material.setUltimaModificacion(LocalDateTime.now());
        materialRepositorio.save(material);
    }

    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return participacionDocenteRepositorio.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
    }

    /**
     * Obtiene la participación del docente en el curso (como titular o ayudante).
     */
    private ParticipacionDocente obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return participacionDocenteRepositorio.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    private String validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Error! Debe completar el título.");
        }
        if (titulo.trim().length() > 50) {
            throw new IllegalArgumentException("Error! El título no puede superar los 50 caracteres.");
        }
        return titulo.trim();
    }

    private String validarAutor(String autor) {
        String limpio = vacioANulo(autor);
        if (limpio != null && limpio.length() > 50) {
            throw new IllegalArgumentException("Error! El autor no puede superar los 50 caracteres.");
        }
        return limpio;
    }

    /**
     * Valida los datos que pide cada tipo de material: archivo (grabación y presentación) o archivo o enlace
     * externo (bibliografía).
     *
     * @param esAlta {@code true} si se está subiendo el material; al modificar, el archivo o enlace es opcional
     *               porque se conserva el actual.
     */
    private void validarDatosSegunTipo(TipoMaterial tipo, boolean hayArchivo, String enlace, boolean esAlta) {
        if (enlace != null) {
            if (!tipo.permiteEnlaceExterno()) {
                throw new IllegalArgumentException("Error! Este tipo de material no admite un enlace externo.");
            }
            if (hayArchivo) {
                throw new IllegalArgumentException("Error! Indique un archivo o un enlace externo, no ambos.");
            }
            if (enlace.length() > 500) {
                throw new IllegalArgumentException("Error! El enlace no puede superar los 500 caracteres.");
            }
            if (!ArchivoUtilidad.esEnlaceExterno(enlace)) {
                throw new IllegalArgumentException("Error! El enlace debe comenzar con http:// o https://.");
            }
        }
        if (esAlta && !hayArchivo && enlace == null) {
            throw new IllegalArgumentException(switch (tipo.getNombre()) {
                case TipoMaterial.GRABACION -> "Error! Debe seleccionar el archivo de video.";
                case TipoMaterial.PRESENTACION -> "Error! Debe seleccionar el archivo de la presentación.";
                default -> "Error! Debe seleccionar un archivo o ingresar un enlace externo.";
            });
        }
    }

    /**
     * Guarda el archivo del material en la carpeta de su unidad ({@code curso{id}/unidad{id}/material{id}.{ext}}).
     *
     * @return La ruta del archivo, relativa a la carpeta de los cursos.
     */
    private String guardarArchivo(Material material, MultipartFile archivo) {
        TipoMaterial tipo = material.getTipoMaterial();
        Path carpeta = ArchivoUtilidad.carpetaDeUnidad(directorioCursos, material.getCurso().getIdCurso(),
                material.getUnidad().getIdUnidad());
        String nombre = ArchivoUtilidad.guardar(archivo, carpeta, "material" + material.getIdMaterial(),
                tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        return ArchivoUtilidad.rutaRelativa(directorioCursos, carpeta.resolve(nombre));
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /**
     * Indica si un usuario puede abrir el archivo o enlace de un material.
     */
    @Override
    public boolean puedeAcceder(Material material, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        if (docente != null && participaEnElCurso(material.getCurso(), docente)) {
            return true;
        }
        Alumno alumno = usuario.getAlumno();
        return alumno != null && buscarInscripcionConAcceso(material, alumno).isPresent();
    }

    /**
     * Busca la inscripción con la que un alumno puede abrir un material.
     */
    @Override
    public Optional<Inscripcion> buscarInscripcionConAcceso(Material material, Alumno alumno) {
        if (!material.estaPublicado()) {
            return Optional.empty();
        }
        return inscripcionRepositorio.findByAlumnoAndBajaFalse(alumno).stream()
                .filter(inscripcion -> inscripcion.getCurso().getIdCurso() == material.getCurso().getIdCurso())
                .filter(inscripcion -> tieneAccesoVigente(inscripcion.getIdInscripcion(), alumno))
                .filter(inscripcion -> inscripcion.getCohorte().getPrograma().getCronogramaOrdenado().stream()
                        .filter(cronograma -> cronograma.getUnidad().getIdUnidad() == material.getUnidad().getIdUnidad())
                        .findFirst().map(inscripcion::estaUnidadHabilitada).orElse(false))
                .findFirst();
    }

    private boolean tieneAccesoVigente(int idInscripcion, Alumno alumno) {
        try {
            inscripcionServicio.validarAcceso(idInscripcion, alumno);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
