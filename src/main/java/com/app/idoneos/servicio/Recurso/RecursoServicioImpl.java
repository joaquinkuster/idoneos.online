package com.app.idoneos.servicio.Recurso;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Bibliografia;
import com.app.idoneos.modelo.CategoriaMaterialLectura;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Docente;
import com.app.idoneos.modelo.DocenteCurso;
import com.app.idoneos.modelo.Enlace;
import com.app.idoneos.modelo.Grabacion;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MaterialLectura;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.modelo.Recurso;
import com.app.idoneos.modelo.Tema;
import com.app.idoneos.modelo.TemaRecurso;
import com.app.idoneos.modelo.TipoRecurso;
import com.app.idoneos.modelo.Usuario;
import com.app.idoneos.repositorio.BibliografiaRepositorio;
import com.app.idoneos.repositorio.CategoriaMaterialLecturaRepositorio;
import com.app.idoneos.repositorio.DocenteCursoRepositorio;
import com.app.idoneos.repositorio.EnlaceRepositorio;
import com.app.idoneos.repositorio.GrabacionRepositorio;
import com.app.idoneos.repositorio.MaterialLecturaRepositorio;
import com.app.idoneos.repositorio.RecursoRepositorio;
import com.app.idoneos.repositorio.TemaRecursoRepositorio;
import com.app.idoneos.repositorio.TemaRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import com.app.idoneos.servicio.Programa.MinimoDeContenidoValidador;
import com.app.idoneos.utilidades.ManejadorArchivosUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;

/**
 * Implementación del servicio de los recursos de los temas ({@link RecursoServicio}).
 */
@Service
public class RecursoServicioImpl implements RecursoServicio, CrudServicio<TemaRecurso> {

    private static final int LARGO_TITULO = 150;
    private static final int LARGO_DESCRIPCION = 500;
    private static final int LARGO_AUTOR = 100;
    private static final int LARGO_ISBN = 17;
    private static final int LARGO_EDITORIAL = 100;
    private static final int LARGO_ENLACE = 500;

    @Autowired
    private MinimoDeContenidoValidador minimoDeContenido;

    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private TemaRecursoRepositorio temaRecursoRepositorio;

    @Autowired
    private RecursoRepositorio recursoRepositorio;

    @Autowired
    private EnlaceRepositorio enlaceRepositorio;

    @Autowired
    private GrabacionRepositorio grabacionRepositorio;

    @Autowired
    private MaterialLecturaRepositorio materialLecturaRepositorio;

    @Autowired
    private BibliografiaRepositorio bibliografiaRepositorio;

    @Autowired
    private CategoriaMaterialLecturaRepositorio categoriaMaterialLecturaRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el material por su identificador. Solo devuelve registros activos.
     */
    @Override
    public Optional<TemaRecurso> buscarPorId(Integer id) {
        return temaRecursoRepositorio.findById(id).filter(entidad -> !entidad.esInactivo());
    }

    /**
     * Obtiene todos los registros activos.
     */
    @Override
    public List<TemaRecurso> obtenerTodo() {
        return temaRecursoRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro activo con el identificador dado.
     */
    @Override
    public boolean existePorId(Integer id) {
        return buscarPorId(id).isPresent();
    }

    /**
     * Busca los materiales activos de una participación docente.
     */
    @Override
    public List<TemaRecurso> buscarPorDocenteCurso(DocenteCurso docenteCurso) {
        return temaRecursoRepositorio.findByDocenteCursoAndBajaFalse(docenteCurso);
    }

    /**
     * Busca los materiales activos de un tema, en su orden.
     */
    @Override
    public List<TemaRecurso> buscarPorTema(Tema tema) {
        return temaRecursoRepositorio.findByTemaAndBajaFalse(tema).stream()
                .sorted(Comparator.comparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /**
     * CU-31: Busca materiales con filtros opcionales.
     */
    @Override
    public List<TemaRecurso> buscarConFiltros(Integer idCurso, Integer idTema, String tipo, String titulo,
            Boolean generadoPorIa, Boolean baja, Docente soloDeDocente) {
        String criterio = titulo == null ? "" : titulo.trim().toLowerCase();
        TipoRecurso tipoBuscado = TipoRecurso.deTexto(tipo);
        return temaRecursoRepositorio.findAll().stream()
                .filter(m -> idCurso == null || m.getCurso().getIdCurso() == idCurso)
                .filter(m -> idTema == null || m.getTema().getIdTema() == idTema)
                .filter(m -> tipoBuscado == null || m.getRecurso().getTipo().equals(tipoBuscado.getNombre()))
                .filter(m -> criterio.isEmpty() || m.getTitulo().toLowerCase().contains(criterio))
                .filter(m -> generadoPorIa == null || m.getRecurso().getGeneradoPorIa() == generadoPorIa)
                .filter(m -> baja == null || m.getBaja().equals(baja))
                .filter(m -> soloDeDocente == null || participaEnElCurso(m.getCurso(), soloDeDocente))
                .sorted(Comparator.comparing(TemaRecurso::getBaja)
                        .thenComparingInt((TemaRecurso m) -> m.getTema().getIdTema())
                        .thenComparingInt(TemaRecurso::getNumeroOrden).thenComparingInt(TemaRecurso::getIdTemaRecurso))
                .toList();
    }

    /**
     * Indica si un usuario puede abrir un material.
     */
    @Override
    public boolean puedeAcceder(TemaRecurso material, Usuario usuario) {
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
     * Busca la inscripción con la que un alumno accede a un material (debe estar publicado).
     */
    @Override
    public Optional<Inscripcion> buscarInscripcionConAcceso(TemaRecurso material, Alumno alumno) {
        if (!material.estaPublicado()) {
            return Optional.empty();
        }
        return inscripcionServicio.buscarConAccesoATema(alumno, material.getTema());
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el material en la base de datos.
     */
    @Override
    public TemaRecurso guardar(TemaRecurso entidad) {
        return temaRecursoRepositorio.save(entidad);
    }

    /**
     * CU-28: Carga un material a un tema.
     */
    @Override
    @Transactional
    public TemaRecurso subirMaterial(Docente docente, Integer idTema, String tipoTexto, DatosRecurso datos,
            MultipartFile archivo, boolean terminos) {
        Tema tema = temaRepositorio.findById(idTema == null ? -1 : idTema).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        DocenteCurso participacion = obtenerParticipacion(tema.getCurso(), docente);
        TipoRecurso tipo = TipoRecurso.deTexto(tipoTexto);
        if (tipo == null || !tipo.permiteSubidaManual()) {
            throw RespuestaFormularioUtilidad.campoInvalido("tipo", "Error! Debe seleccionar un tipo de material de la lista.");
        }
        if (tipo == TipoRecurso.GRABACION && !tema.getCurso().incluyeModalidad(Modalidad.GRABADA)) {
            throw RespuestaFormularioUtilidad.campoInvalido("tipo", "Error! El curso no tiene la modalidad '"
                    + Modalidad.GRABADA + "': no puede registrarse una grabación.");
        }
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatos(tipo, datos, hayArchivo, true);
        if (hayArchivo) {
            ManejadorArchivosUtilidad.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }
        if (!terminos) {
            throw RespuestaFormularioUtilidad.campoInvalido("terminos",
                    "Error! Debe aceptar los términos y condiciones sobre el contenido del material.");
        }

        Recurso recurso = new Recurso(docente, datos.titulo().trim());
        recurso.setDescripcion(vacioANulo(datos.descripcion()));
        recurso.setFechaAceptacionTyc(LocalDateTime.now());
        recurso = recursoRepositorio.saveAndFlush(recurso); // para conocer su identificador
        String ruta = hayArchivo ? guardarArchivo(recurso, tema, tipo, archivo) : null;
        crearDatosPropios(recurso, tipo, datos, ruta, hayArchivo ? archivo.getSize() : null);

        int orden = temaRecursoRepositorio.findByTemaAndBajaFalse(tema).stream()
                .mapToInt(TemaRecurso::getNumeroOrden).max().orElse(0) + 1;
        TemaRecurso material = new TemaRecurso(tema, recurso, participacion, orden);
        material.setOculto(datos.oculto());
        material = temaRecursoRepositorio.save(material);
        tema.getTemasRecurso().add(material);
        recurso.getTemasRecurso().add(material);
        return material;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     */
    @Override
    public TemaRecurso modificar(TemaRecurso entidad) {
        return temaRecursoRepositorio.save(entidad);
    }

    /**
     * CU-29: Modifica un material.
     */
    @Override
    @Transactional
    public TemaRecurso modificarMaterial(Integer id, Docente docente, DatosRecurso datos, MultipartFile archivo) {
        TemaRecurso material = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El material no se encuentra activo."));
        obtenerParticipacion(material.getCurso(), docente);
        Recurso recurso = material.getRecurso();
        TipoRecurso tipo = TipoRecurso.deTexto(recurso.getTipo());
        if (tipo == null || !tipo.permiteSubidaManual()) {
            throw new IllegalArgumentException("Error! Este tipo de material no se puede modificar manualmente.");
        }
        boolean hayArchivo = archivo != null && !archivo.isEmpty();
        validarDatos(tipo, datos, hayArchivo, false);
        if (hayArchivo) {
            ManejadorArchivosUtilidad.validar(archivo, tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        }

        if (!datos.oculto() && material.getOculto()) {
            // Volver a publicar un material cuya modalidad el curso ya no tiene
            String modalidad = material.getModalidadAsociada();
            if (modalidad != null && !material.getCurso().incluyeModalidad(modalidad)) {
                throw RespuestaFormularioUtilidad.campoInvalido("oculto", "Error! No se puede publicar el material «"
                        + material.getTitulo() + "»: el curso no tiene la modalidad '" + modalidad + "'.");
            }
        }
        if (datos.oculto() && !material.getOculto()) {
            minimoDeContenido.motivoParaElMaterial(material.getTema(), Set.of(material.getIdTemaRecurso())).ifPresent(motivo -> {
                // El error es del campo "Visibilidad": se muestra debajo de él, como en las autoevaluaciones
                throw RespuestaFormularioUtilidad.campoInvalido("oculto",
                        MensajeErrorUtilidad.accionBloqueada("ocultar", "el material", material.getTitulo(), motivo,
                                "Registrá otro material antes."));
            });
        }

        String rutaAnterior = recurso.getRutaArchivo();
        String rutaNueva = hayArchivo ? guardarArchivo(recurso, material.getTema(), tipo, archivo) : null;
        if (rutaNueva != null && rutaAnterior != null && !rutaAnterior.equals(rutaNueva)) {
            ManejadorArchivosUtilidad.eliminar(rutaAnterior, directorioCursos); // cambió de formato: no queda el viejo
        }
        actualizarDatosPropios(recurso, tipo, datos, rutaNueva, hayArchivo ? archivo.getSize() : null);
        LocalDateTime ahora = LocalDateTime.now();
        recurso.setTitulo(datos.titulo().trim());
        recurso.setDescripcion(vacioANulo(datos.descripcion()));
        recurso.setUltimaModificacion(ahora);
        recursoRepositorio.save(recurso);
        material.setOculto(datos.oculto());
        material.setUltimaModificacion(ahora);
        return temaRecursoRepositorio.save(material);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica).
     */
    @Override
    public void borrar(TemaRecurso entidad) {
        entidad.marcarInactivo();
        temaRecursoRepositorio.save(entidad);
    }

    /**
     * CU-30: Da de baja un material.
     */
    @Override
    @Transactional
    public void darDeBajaMaterial(Integer id, Docente docente) {
        TemaRecurso material = validarBaja(id, docente);
        validarMinimoAlDarDeBaja(List.of(material));
        darDeBaja(material);
    }

    /**
     * CU-30: Da de baja varios materiales a la vez (todos o ninguno).
     */
    @Override
    @Transactional
    public void darDeBajaVarios(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un material.");
        }
        List<TemaRecurso> materiales = ids.stream().distinct().map(id -> validarBaja(id, docente)).toList();
        validarMinimoAlDarDeBaja(materiales);
        materiales.forEach(this::darDeBaja);
    }

    private TemaRecurso validarBaja(Integer id, Docente docente) {
        TemaRecurso material = buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Error! El material no se encuentra activo."));
        if (docente != null) {
            obtenerParticipacion(material.getCurso(), docente);
        }
        return material;
    }

    private void validarMinimoAlDarDeBaja(List<TemaRecurso> materiales) {
        Set<Integer> ids = materiales.stream().map(TemaRecurso::getIdTemaRecurso).collect(Collectors.toSet());
        for (TemaRecurso material : materiales) {
            minimoDeContenido.motivoParaElMaterial(material.getTema(), ids).ifPresent(motivo -> {
                throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el material", material.getTitulo(), motivo,
                        "Registrá otro material antes."));
            });
        }
    }

    /** Da de baja el uso del recurso en el tema y, si ningún otro tema lo usa, también el recurso. */
    private void darDeBaja(TemaRecurso material) {
        LocalDateTime ahora = LocalDateTime.now();
        material.marcarInactivo();
        material.setUltimaModificacion(ahora);
        temaRecursoRepositorio.save(material);
        Recurso recurso = material.getRecurso();
        if (recurso.getTemasRecurso().stream().allMatch(TemaRecurso::esInactivo)) {
            recurso.marcarInactivo();
            recurso.setUltimaModificacion(ahora);
            recursoRepositorio.save(recurso);
        }
    }

    // ---------------------------------------------------------------- Auxiliares

    private boolean participaEnElCurso(Curso curso, Docente docente) {
        return docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
    }

    private DocenteCurso obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        return docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
    }

    /** Valida los datos que pide cada tipo de material. */
    private void validarDatos(TipoRecurso tipo, DatosRecurso datos, boolean hayArchivo, boolean esAlta) {
        if (datos.titulo() == null || datos.titulo().isBlank()) {
            throw RespuestaFormularioUtilidad.campoInvalido("titulo", "Error! Debe completar el título.");
        }
        largoMaximo("titulo", "El título", datos.titulo().trim(), LARGO_TITULO);
        largoMaximo("descripcion", "La descripción", vacioANulo(datos.descripcion()), LARGO_DESCRIPCION);
        String autor = vacioANulo(datos.autor());
        if ((tipo == TipoRecurso.PRESENTACION || tipo == TipoRecurso.BIBLIOGRAFIA) && autor == null) {
            throw RespuestaFormularioUtilidad.campoInvalido("autor", "Error! Debe completar el autor.");
        }
        largoMaximo("autor", "El autor", autor, LARGO_AUTOR);
        if (datos.paginas() != null && datos.paginas() <= 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("paginas", MensajeErrorUtilidad.formatoInvalido("La cantidad de páginas", "número entero mayor a cero"));
        }
        if (datos.duracionMinutos() != null && datos.duracionMinutos() <= 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("duracionMinutos", MensajeErrorUtilidad.formatoInvalido("La duración", "número entero de minutos mayor a cero"));
        }
        String enlace = vacioANulo(datos.enlace());
        if (tipo == TipoRecurso.ENLACE) {
            if (enlace == null) {
                throw RespuestaFormularioUtilidad.campoInvalido("enlace", "Error! Debe ingresar el enlace.");
            }
            largoMaximo("enlace", "El enlace", enlace, LARGO_ENLACE);
            if (!ManejadorArchivosUtilidad.esEnlaceExterno(enlace)) {
                throw RespuestaFormularioUtilidad.campoInvalido("enlace", MensajeErrorUtilidad.formatoInvalido("El enlace", "comienza con http:// o https://"));
            }
        } else if (enlace != null) {
            throw RespuestaFormularioUtilidad.campoInvalido("enlace", "Error! Este tipo de material no admite un enlace externo.");
        }
        if (tipo == TipoRecurso.BIBLIOGRAFIA) {
            String isbn = vacioANulo(datos.isbn());
            if (isbn == null) {
                throw RespuestaFormularioUtilidad.campoInvalido("isbn", "Error! Debe completar el ISBN.");
            }
            largoMaximo("isbn", "El ISBN", isbn, LARGO_ISBN);
            largoMaximo("editorial", "La editorial", vacioANulo(datos.editorial()), LARGO_EDITORIAL);
        }
        if (tipo == TipoRecurso.ENLACE && hayArchivo) {
            throw RespuestaFormularioUtilidad.campoInvalido("enlace", "Error! Este tipo de material no lleva archivo.");
        }
        if (esAlta && !hayArchivo && (tipo == TipoRecurso.PRESENTACION || tipo == TipoRecurso.GRABACION)) {
            throw RespuestaFormularioUtilidad.campoInvalido("archivo", tipo == TipoRecurso.GRABACION
                    ? "Error! Debe seleccionar el archivo de video." : "Error! Debe seleccionar el archivo de la presentación.");
        }
    }

    private void largoMaximo(String campo, String nombre, String valor, int maximo) {
        if (valor != null && valor.length() > maximo) {
            throw RespuestaFormularioUtilidad.campoInvalido(campo, MensajeErrorUtilidad.demasiadoLargo(nombre, maximo, "caracteres"));
        }
    }

    /** Crea la tabla con los datos propios del tipo de recurso. */
    private void crearDatosPropios(Recurso recurso, TipoRecurso tipo, DatosRecurso datos, String ruta, Long tamanio) {
        String extension = ruta == null ? null : ManejadorArchivosUtilidad.extensionDe(ruta);
        switch (tipo) {
            case PRESENTACION -> {
                CategoriaMaterialLectura categoria = categoriaMaterialLecturaRepositorio.findByNombre(CategoriaMaterialLectura.PRESENTACION)
                        .orElseThrow(() -> new IllegalStateException("Falta la categoría de material de lectura 'Presentación'."));
                MaterialLectura lectura = new MaterialLectura(recurso, categoria, ruta, vacioANulo(datos.autor()));
                lectura.setPaginas(datos.paginas());
                lectura.setTamanioBytes(tamanio);
                lectura.setExtension(extension);
                recurso.setMaterialLectura(materialLecturaRepositorio.save(lectura));
            }
            case BIBLIOGRAFIA -> {
                Bibliografia bibliografia = new Bibliografia(recurso, vacioANulo(datos.isbn()), vacioANulo(datos.autor()));
                bibliografia.setEditorial(vacioANulo(datos.editorial()));
                bibliografia.setPaginas(datos.paginas());
                bibliografia.setFechaPublicacion(datos.fechaPublicacion());
                bibliografia.setRutaArchivo(ruta);
                bibliografia.setTamanioBytes(tamanio);
                bibliografia.setExtension(extension);
                recurso.setBibliografia(bibliografiaRepositorio.save(bibliografia));
            }
            case GRABACION -> {
                Grabacion grabacion = new Grabacion(recurso, ruta);
                grabacion.setTamanioBytes(tamanio);
                grabacion.setDuracionMinutos(datos.duracionMinutos());
                recurso.setGrabacion(grabacionRepositorio.save(grabacion));
            }
            case ENLACE -> recurso.setEnlace(enlaceRepositorio.save(new Enlace(recurso, vacioANulo(datos.enlace()))));
            default -> throw new IllegalArgumentException("Error! Este tipo de material no se puede cargar manualmente.");
        }
    }

    /** Actualiza los datos propios del tipo de recurso; si hay un archivo nuevo, también su ruta. */
    private void actualizarDatosPropios(Recurso recurso, TipoRecurso tipo, DatosRecurso datos, String rutaNueva, Long tamanio) {
        String extension = rutaNueva == null ? null : ManejadorArchivosUtilidad.extensionDe(rutaNueva);
        switch (tipo) {
            case PRESENTACION -> {
                MaterialLectura lectura = recurso.getMaterialLectura();
                lectura.setAutor(vacioANulo(datos.autor()));
                lectura.setPaginas(datos.paginas());
                if (rutaNueva != null) {
                    lectura.setRutaArchivo(rutaNueva);
                    lectura.setTamanioBytes(tamanio);
                    lectura.setExtension(extension);
                }
                materialLecturaRepositorio.save(lectura);
            }
            case BIBLIOGRAFIA -> {
                Bibliografia bibliografia = recurso.getBibliografia();
                bibliografia.setIsbn(vacioANulo(datos.isbn()));
                bibliografia.setAutor(vacioANulo(datos.autor()));
                bibliografia.setEditorial(vacioANulo(datos.editorial()));
                bibliografia.setPaginas(datos.paginas());
                bibliografia.setFechaPublicacion(datos.fechaPublicacion());
                if (rutaNueva != null) {
                    bibliografia.setRutaArchivo(rutaNueva);
                    bibliografia.setTamanioBytes(tamanio);
                    bibliografia.setExtension(extension);
                }
                bibliografiaRepositorio.save(bibliografia);
            }
            case GRABACION -> {
                Grabacion grabacion = recurso.getGrabacion();
                grabacion.setDuracionMinutos(datos.duracionMinutos());
                if (rutaNueva != null) {
                    grabacion.setRutaArchivo(rutaNueva);
                    grabacion.setTamanioBytes(tamanio);
                }
                grabacionRepositorio.save(grabacion);
            }
            case ENLACE -> {
                Enlace enlace = recurso.getEnlace();
                enlace.setUrl(vacioANulo(datos.enlace()));
                enlaceRepositorio.save(enlace);
            }
            default -> throw new IllegalArgumentException("Error! Este tipo de material no se puede modificar manualmente.");
        }
    }

    private String guardarArchivo(Recurso recurso, Tema tema, TipoRecurso tipo, MultipartFile archivo) {
        Path carpeta = ManejadorArchivosUtilidad.carpetaDeTema(directorioCursos, tema.getCurso().getIdCurso(), tema.getIdTema());
        String nombre = ManejadorArchivosUtilidad.guardar(archivo, carpeta, "recurso" + recurso.getIdRecurso(),
                tipo.getTamanioMaximo(), tipo.getExtensionesPermitidas());
        return ManejadorArchivosUtilidad.rutaRelativa(directorioCursos, carpeta.resolve(nombre));
    }

    private String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
