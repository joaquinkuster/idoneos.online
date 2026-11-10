package com.app.idoneos.service.curso;

import com.app.idoneos.model.EstadoGeneracion;
import com.app.idoneos.model.Parametro;

import com.app.idoneos.model.Categoria;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.CursoModalidad;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Modalidad;
import com.app.idoneos.model.Nivel;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaRecurso;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.CategoriaRepository;
import com.app.idoneos.repository.CursoModalidadRepository;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.DocenteRepository;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.ModalidadRepository;
import com.app.idoneos.repository.NivelRepository;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.service.parametro.ParametroService;
import com.app.idoneos.util.DuracionUtil;
import com.app.idoneos.util.ManejadorArchivosUtil;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.*;
import com.app.idoneos.repository.*;

/** Implementación de CursoService. */
@Service
public class CursoServiceImpl implements CursoService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(CursoServiceImpl.class);

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private NivelRepository nivelRepository;

    @Autowired
    private ModalidadRepository modalidadRepository;

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private CursoModalidadRepository cursoModalidadRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private com.app.idoneos.repository.CohorteRepository cohorteRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private ParametroService parametroService;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el curso por su identificador. */
    @Override
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Curso> obtenerTodo() {
        return cursoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return cursoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de curso asociados a categoria. */
    @Override
    public List<Curso> buscarPorCategoria(Categoria categoria) {
        return cursoRepository.findByCategoriaAndBajaFalse(categoria);
    }

    /** Busca el curso por su atributo único 'nombre'. */
    @Override
    public Optional<Curso> buscarPorNombre(String nombre) {
        return cursoRepository.findByNombre(nombre);
    }

    /** Busca cursos aplicando filtros opcionales. */
    @Override
    public List<Curso> buscarConFiltros(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad, String orden, Docente soloDeDocente) {
        String criterio = texto == null ? "" : texto.trim().toLowerCase();
        Comparator<Curso> criterioDeOrden = Comparator.comparing(Curso::esInactivo)
                .thenComparing("recientes".equals(orden)
                        ? Comparator.comparing(Curso::getFechaCreacion).thenComparingInt(Curso::getIdCurso).reversed()
                        : Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER));
        // Un docente solo recorre los cursos en los que participa (se piden a la base), no todos los del sistema
        List<Curso> candidatos = soloDeDocente == null ? cursoRepository.findAll()
                : docenteCursoRepository.findByDocenteAndBajaFalse(soloDeDocente).stream()
                        .map(participacion -> participacion.getCurso()).distinct().toList();
        return candidatos.stream()
                .filter(curso -> criterio.isEmpty() || curso.getNombre().toLowerCase().contains(criterio)
                        || (curso.getDescripcion() != null && curso.getDescripcion().toLowerCase().contains(criterio)))
                .filter(curso -> idCategoria == null || curso.getCategoria().getIdCategoria() == idCategoria)
                .filter(curso -> idNivel == null || (curso.getNivel() != null && curso.getNivel().getIdNivel() == idNivel))
                .filter(curso -> idDocente == null || curso.tieneDocente(idDocente))
                .filter(curso -> idModalidad == null || curso.getModalidades().stream()
                        .anyMatch(modalidad -> modalidad.getIdModalidad() == idModalidad))
                .filter(curso -> soloDeDocente == null || curso.tieneDocente(soloDeDocente))
                .sorted(criterioDeOrden)
                .toList();
    }

    /** Busca los cursos del catálogo público. */
    @Override
    public List<Curso> buscarEnCatalogo(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad) {
        return buscarConFiltros(texto, idCategoria, idNivel, idDocente, idModalidad, "recientes", null).stream()
                .filter(curso -> !curso.esInactivo())
                .filter(curso -> !buscarCohortesAbiertas(curso).isEmpty() || !buscarCohortesProximas(curso).isEmpty())
                .toList();
    }

    @Override
    public List<Curso> buscarDestacados(int cantidad) {
        return buscarEnCatalogo(null, null, null, null, null).stream()
                .sorted(Comparator.comparingLong(this::contarInscripcionesHistoricas).reversed()
                        .thenComparing(Comparator.comparingInt(Curso::getIdCurso).reversed()))
                .limit(cantidad).toList();
    }

    @Override
    public long contarInscripcionesHistoricas(Curso curso) {
        return curso.getProgramas().stream()
                .flatMap(programa -> programa.getCohortes().stream())
                .mapToLong(cohorte -> cohorte.getInscripciones().size()).sum();
    }

    /** Busca las cohortes con inscripción abierta de un curso. */
    @Override
    public List<Cohorte> buscarCohortesAbiertas(Curso curso) {
        return curso.getProgramas().stream()
                .filter(programa -> !programa.esInactivo())
                .flatMap(programa -> programa.getCohortes().stream())
                .filter(cohorte -> !cohorte.esInactivo() && cohorte.estaAbierta())
                .sorted(Comparator.comparing(Cohorte::getFechaInicioInscripcion))
                .toList();
    }

    @Override
    public List<Cohorte> buscarCohortesProximas(Curso curso) {
        return curso.getProgramas().stream()
                .filter(programa -> !programa.esInactivo())
                .flatMap(programa -> programa.getCohortes().stream())
                .filter(cohorte -> !cohorte.esInactivo() && cohorte.estaProxima())
                .sorted(Comparator.comparing(Cohorte::getFechaInicioInscripcion))
                .toList();
    }

    /** Verifica si un curso tiene inscripciones activas asociadas. */
    @Override
    public boolean tieneInscripcionesActivas(Curso curso) {
        return inscripcionRepository.contarPorCurso(curso) > 0;
    }

    /** Busca las inscripciones activas (no dadas de baja) de un curso, a través de sus programas y cohortes. */
    @Override
    public List<Inscripcion> buscarInscripcionesActivas(Curso curso) {
        return curso.getProgramas().stream()
                .flatMap(programa -> programa.getCohortes().stream())
                .flatMap(cohorte -> cohorte.getInscripciones().stream())
                .filter(inscripcion -> !inscripcion.esInactivo())
                .toList();
    }

    /** Verifica que el docente pueda acceder al curso. */
    @Override
    public DocenteCurso validarAccesoDocente(int idCurso, Docente docente) {
        Curso curso = buscarActivoPorId(idCurso);
        DocenteCurso participacion = docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
        // Un curso sin programas también se puede abrir: desde ahí el docente registra el primero
        return participacion;
    }

    @Override
    public ContextoCurso resolverContextoDelPanel(Integer idCurso, Usuario usuario) {
        if (idCurso == null) {
            throw new IllegalArgumentException("Error! Debe seleccionar un curso.");
        }
        if (usuario.esAdministradorActivo()) {
            return ContextoCurso.deAdministrador(buscarActivoPorId(idCurso));
        }
        return ContextoCurso.deDocente(validarAccesoDocente(idCurso, usuario.getDocente()));
    }

    @Override
    public Optional<ContextoCurso> resolverContextoDeLaTema(Tema tema, Usuario usuario) {
        Curso curso = tema.getCurso();
        if (usuario.esAdministradorActivo()) {
            return Optional.of(ContextoCurso.deAdministrador(curso));
        }
        if (curso.tieneDocente(usuario.getDocente())) {
            return Optional.of(ContextoCurso.deDocente(validarAccesoDocente(curso.getIdCurso(), usuario.getDocente())));
        }
        return inscripcionService.buscarConAccesoATema(usuario.getAlumno(), tema).map(ContextoCurso::deAlumno);
    }

    @Override
    public ResumenCursos resumir(List<Curso> cursos) {
        Map<Integer, List<Inscripcion>> inscripciones = new HashMap<>();
        Map<Integer, List<Programa>> programas = new HashMap<>();
        Set<Integer> conInscripcionAbierta = new HashSet<>();
        for (Curso curso : cursos) {
            inscripciones.put(curso.getIdCurso(), buscarInscripcionesActivas(curso));
            programas.put(curso.getIdCurso(), programaRepository.findByCursoAndBajaFalse(curso));
            if (!buscarCohortesAbiertas(curso).isEmpty()) {
                conInscripcionAbierta.add(curso.getIdCurso());
            }
        }
        return new ResumenCursos(inscripciones, programas, conInscripcionAbierta);
    }

    @Override
    public Curso buscarActivoPorId(Integer idCurso) {
        return buscarPorId(idCurso)
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el curso en la base de datos. */
    @Override
    public Curso guardar(Curso entidad) {
        return cursoRepository.save(entidad);
    }

    /** Registra un curso con sus modalidades de dictado y su equipo docente. */
    @Override
    @Transactional
    public Curso registrar(String nombre, String descripcion, Float precio, Integer cargaHorariaMinutos,
            Integer duracionDias, String imagen, Integer idCategoria, Integer idNivel, boolean emiteCertificado,
            List<Integer> idsModalidades, Integer idDocenteTitular, List<Integer> idsDocentesAyudantes) {
        validarCamposObligatorios(nombre, precio, duracionDias, idCategoria, idsModalidades, idDocenteTitular);
        Categoria categoria = obtenerCategoriaActiva(idCategoria);
        Nivel nivel = idNivel == null ? null : obtenerNivel(idNivel);
        Docente titular = obtenerDocenteHabilitado(idDocenteTitular);
        List<Docente> ayudantes = obtenerAyudantes(idsDocentesAyudantes);
        validarTitularEntreAyudantes(titular, ayudantes);
        validarPrecio(precio);
        validarDuracion(duracionDias);
        validarCargaHoraria(cargaHorariaMinutos, duracionDias);
        String nombreLimpio = nombre.trim();
        if (cursoRepository.findByNombre(nombreLimpio).isPresent()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        }

        Curso curso = new Curso(nivel, categoria, nombreLimpio, precio, cargaHorariaMinutos, duracionDias);
        curso.setDescripcion(convertirVacioANulo(descripcion));
        curso.setImagen(convertirVacioANulo(imagen));
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepository.save(curso);
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Curso modificar(Curso entidad) {
        return cursoRepository.save(entidad);
    }

    /** Modifica un curso activo. */
    @Override
    @Transactional
    public Curso modificar(Integer idCurso, String nombre, String descripcion, Float precio,
            Integer cargaHorariaMinutos, Integer duracionDias, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            List<Integer> idsDocentesAyudantes) {
        Curso curso = buscarActivoPorId(idCurso);
        validarCamposObligatorios(nombre, precio, duracionDias, idCategoria, idsModalidades, idDocenteTitular);
        // Con cohortes publicadas en el catálogo solo se pueden modificar el precio, la portada y el equipo docente
        if (curso.tieneCohortesPublicadas()
                && cambiaDatosRestringidos(curso, nombre, descripcion, cargaHorariaMinutos, duracionDias,
                        idCategoria, idNivel, emiteCertificado, idsModalidades)) {
            throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("el curso", curso.getNombre(),
                    MensajeErrorUtil.MOTIVO_PUBLICADO, MensajeErrorUtil.CIERRE_CURSO));
        }
        Categoria categoria = obtenerCategoriaActiva(idCategoria);
        Nivel nivel = idNivel == null ? curso.getNivel() : obtenerNivel(idNivel);
        Docente titular = obtenerDocenteHabilitado(idDocenteTitular);
        List<Docente> ayudantes = obtenerAyudantes(idsDocentesAyudantes);
        validarTitularEntreAyudantes(titular, ayudantes);
        validarDesvinculaciones(curso, titular, ayudantes);
        validarModalidadesEliminadas(curso, idsModalidades);
        validarModalidadEnVivoAgregada(curso, idsModalidades);
        validarPrecio(precio);
        validarDuracion(duracionDias);
        validarCargaHoraria(cargaHorariaMinutos, duracionDias);
        validarDuracionContraCronogramas(curso, duracionDias);
        String nombreLimpio = nombre.trim();
        cursoRepository.findByNombre(nombreLimpio).filter(otro -> otro.getIdCurso() != idCurso).ifPresent(otro -> {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", "Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        });

        curso.setNombre(nombreLimpio);
        curso.setDescripcion(convertirVacioANulo(descripcion));
        curso.setPrecio(precio);
        curso.setCargaHorariaMinutos(cargaHorariaMinutos);
        curso.setDuracionDias(duracionDias);
        if (imagen != null && !imagen.isBlank()) {
            curso.setImagen(imagen.trim());
        }
        curso.setCategoria(categoria);
        curso.setNivel(nivel);
        curso.setEmiteCertificado(emiteCertificado);
        curso.setUltimaModificacion(LocalDateTime.now());
        curso = cursoRepository.save(curso);
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    /** Completa los datos académicos de un curso activo: el nivel, la descripción. */
    @Override
    @Transactional
    public Curso modificarPorDocente(Integer idCurso, Docente docente, Integer idNivel, String descripcion,
            String objetivos, Integer cargaHorariaMinutos) {
        Curso curso = buscarActivoPorId(idCurso);
        validarAccesoDocente(idCurso, docente);
        // Con cohortes publicadas en el catálogo los datos del curso son lo que los alumnos ven: ya no se modifican (la portada sí)
        if (curso.tieneCohortesPublicadas()) {
            boolean cambia = (idNivel != null && (curso.getNivel() == null || curso.getNivel().getIdNivel() != idNivel))
                    || (descripcion != null && !Objects.equals(convertirVacioANulo(curso.getDescripcion()), convertirVacioANulo(descripcion)))
                    || (objetivos != null && !Objects.equals(curso.getObjetivos(), objetivos.trim()))
                    || (cargaHorariaMinutos != null && !Objects.equals(curso.getCargaHorariaMinutos(), cargaHorariaMinutos));
            if (cambia) {
                throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("el curso", curso.getNombre(),
                        MensajeErrorUtil.MOTIVO_PUBLICADO, MensajeErrorUtil.CIERRE_CURSO));
            }
            return curso;
        }
        if (idNivel == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nivelId", "Error! Elegí el nivel del curso.");
        }
        if (objetivos == null || objetivos.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("objetivos", "Error! Ingresá los objetivos del curso.");
        }
        if (Boolean.TRUE.equals(curso.getEmiteCertificado()) && cargaHorariaMinutos == null) {
            throw RespuestaFormularioUtil.armarCampoInvalido("cargaHoraria",
                    "Error! Ingresá la carga horaria: va impresa en el certificado.");
        }
        validarCargaHoraria(cargaHorariaMinutos, curso.getDuracionDias());
        curso.setNivel(obtenerNivel(idNivel));
        curso.setDescripcion(convertirVacioANulo(descripcion));
        curso.setObjetivos(objetivos.trim());
        curso.setCargaHorariaMinutos(cargaHorariaMinutos);
        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /** Asigna la imagen de un curso activo. */
    @Override
    @Transactional
    public Curso actualizarImagen(Integer idCurso, String imagen) {
        Curso curso = buscarActivoPorId(idCurso);
        curso.setImagen(imagen);
        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepository.save(curso);
    }

    /** Lleva las imágenes de los cursos a la estructura curso{id/imagen.{ext}}. */
    @Override
    @Transactional
    public void migrarImagenes() {
        for (Curso curso : cursoRepository.findAll()) {
            String imagen = curso.getImagen();
            if (imagen == null || imagen.isBlank() || imagen.startsWith("imagen.") || imagen.contains("/")
                    || imagen.contains("\\") || imagen.contains("..")) {
                continue;
            }
            String extension = ManejadorArchivosUtil.obtenerExtension(imagen);
            if (extension == null) {
                continue;
            }
            extension = extension.equals("jpeg") ? "jpg" : extension;
            try {
                Path carpeta = ManejadorArchivosUtil.resolverCarpetaDeCurso(directorioCursos, curso.getIdCurso());
                Files.createDirectories(carpeta);
                Path destino = carpeta.resolve("imagen." + extension);
                Path anterior = directorioCursos.resolve("cursos").resolve(imagen);
                if (Files.exists(anterior)) {
                    Files.copy(anterior, destino, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    try (InputStream ejemplo = getClass().getResourceAsStream("/static/img/cursos/" + imagen)) {
                        if (ejemplo == null) {
                            REGISTRO.warn("No se encontró la imagen '{}' del curso {}", imagen, curso.getIdCurso());
                            continue;
                        }
                        Files.copy(ejemplo, destino, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                curso.setImagen("imagen." + extension);
                cursoRepository.save(curso);
            } catch (IOException e) {
                REGISTRO.warn("No se pudo migrar la imagen '{}' del curso {}", imagen, curso.getIdCurso(), e);
            }
        }
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Curso entidad) {
        entidad.marcarInactivo(); // Baja lógica
        cursoRepository.save(entidad);
    }

    /** Da de baja un curso activo que no tenga programas. */
    @Override
    @Transactional
    public void darDeBaja(Integer idCurso) {
        Curso curso = buscarActivoPorId(idCurso);
        int programasActivos = programaRepository.findByCursoAndBajaFalse(curso).size();
        int temasActivas = temaRepository.findByCursoAndBajaFalse(curso).size();
        int cohortesActivas = cohorteRepository.findByCursoAndBajaFalse(curso).size();
        if (programasActivos > 0 || temasActivas > 0 || cohortesActivas > 0) {
            List<String> partes = new ArrayList<>();
            if (programasActivos > 0) {
                partes.add(MensajeErrorUtil.formatearCantidad(programasActivos, "programa activo", "programas activos"));
            }
            if (temasActivas > 0) {
                partes.add(MensajeErrorUtil.formatearCantidad(temasActivas, "tema activo", "temas activos"));
            }
            if (cohortesActivas > 0) {
                partes.add(MensajeErrorUtil.formatearCantidad(cohortesActivas, "cohorte activa", "cohortes activas"));
            }
            String motivo = partes.size() == 1 ? partes.get(0)
                    : String.join(", ", partes.subList(0, partes.size() - 1)) + " y " + partes.get(partes.size() - 1);
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el curso", curso.getNombre(),
                    "tiene " + motivo, "Dé de baja primero sus programas, temas y cohortes."));
        }
        curso.marcarInactivo();
        curso.setUltimaModificacion(LocalDateTime.now());
        cursoRepository.save(curso);
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

    private String convertirVacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private void validarCamposObligatorios(String nombre, Float precio, Integer duracionDias, Integer idCategoria,
            List<Integer> idsModalidades, Integer idDocenteTitular) {
        List<String> faltantes = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) faltantes.add("nombre");
        if (precio == null) faltantes.add("precio");
        if (duracionDias == null) faltantes.add("duración");
        if (idCategoria == null) faltantes.add("categoría");
        if (idsModalidades == null || idsModalidades.isEmpty()) faltantes.add("al menos una modalidad");
        if (idDocenteTitular == null) faltantes.add("docente titular");
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Error! Faltan completar los campos obligatorios: "
                    + String.join(", ", faltantes) + ".");
        }
    }

    private void validarPrecio(Float precio) {
        if (precio < 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("precio", MensajeErrorUtil.armarFormatoInvalido("El precio", "número desde 0 hasta $" + String.format("%,.0f", Curso.PRECIO_MAXIMO).replace(',', '.')));
        }
        if (precio > Curso.PRECIO_MAXIMO) {
            throw RespuestaFormularioUtil.armarCampoInvalido("precio", MensajeErrorUtil.armarFormatoInvalido("El precio", "número desde 0 hasta $" + String.format("%,.0f", Curso.PRECIO_MAXIMO).replace(',', '.')));
        }
    }

    /** Valida la carga horaria, que es opcional al registrar el curso. Si se indica, debe ser válida. */
    private void validarCargaHoraria(Integer cargaHorariaMinutos, Integer duracionDias) {
        if (cargaHorariaMinutos == null) {
            return;
        }
        if (cargaHorariaMinutos <= 0 || cargaHorariaMinutos > Curso.CARGA_HORARIA_MAXIMA_MINUTOS) {
            throw RespuestaFormularioUtil.armarCampoInvalido("cargaHoraria", MensajeErrorUtil.armarFormatoInvalido(
                    "La carga horaria", "mayor a cero y hasta " + DuracionUtil.formatearMinutos(Curso.CARGA_HORARIA_MAXIMA_MINUTOS)));
        }
        // No puede ser mayor que el tiempo que dura el curso: como mucho 24 horas por día
        if (cargaHorariaMinutos > (long) duracionDias * DuracionUtil.MINUTOS_POR_DIA) {
            throw RespuestaFormularioUtil.armarCampoInvalido("cargaHoraria",
                    "Error! La carga horaria no puede ser mayor al tiempo de la duración del curso.");
        }
    }

    /** Valida la duración del curso, que es de al menos un día. El acceso de una cohorte, que la incluye, tiene un máximo. */
    private void validarDuracion(Integer duracionDias) {
        if (duracionDias <= 0 || duracionDias > Cohorte.DIAS_ACCESO_MAXIMOS) {
            throw RespuestaFormularioUtil.armarCampoInvalido("duracion", MensajeErrorUtil.armarFormatoInvalido(
                    "La duración", "mayor a cero y hasta " + DuracionUtil.formatear(Cohorte.DIAS_ACCESO_MAXIMOS)));
        }
    }

    /** Valida la duración contra los cronogramas. La define el administrador y no cambia mientras algún programa vigente la tenga repartida. */
    private void validarDuracionContraCronogramas(Curso curso, Integer duracionDias) {
        for (Programa programa : curso.getProgramas()) {
            if (!programa.esInactivo() && duracionDias != curso.getDuracionDias()
                    && (programa.tieneCohortesActivas() || (programa.getCantidadTemas() > 0 && programa.estaDuracionAsignadaCompleta()))) {
                String motivo = programa.tieneCohortesActivas()
                        ? "el programa «" + programa.getNombre() + "» tiene cohortes activas"
                        : "el programa «" + programa.getNombre() + "» ya tiene repartidos sus " + DuracionUtil.formatear(curso.getDuracionDias());
                throw RespuestaFormularioUtil.armarCampoInvalido("duracion", MensajeErrorUtil.armarAccionBloqueada("modificar",
                        "la duración del curso", curso.getNombre(), motivo, ""));
            }
            if (!programa.esInactivo() && programa.getDuracionTotalDias() > duracionDias) {
                throw RespuestaFormularioUtil.armarCampoInvalido("duracion", "Error! La duración ("
                        + DuracionUtil.formatear(duracionDias) + ") no puede ser menor a la duración del cronograma del programa «"
                        + programa.getNombre() + "» (" + DuracionUtil.formatear(programa.getDuracionTotalDias()) + ").");
            }
        }
    }

    private Categoria obtenerCategoriaActiva(Integer idCategoria) {
        return categoriaRepository.findById(idCategoria).filter(categoria -> !categoria.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría seleccionada no se encuentra activa."));
    }

    private Nivel obtenerNivel(Integer idNivel) {
        return nivelRepository.findById(idNivel)
                .orElseThrow(() -> new IllegalArgumentException("Error! El nivel seleccionado no existe."));
    }

    private Docente obtenerDocenteHabilitado(Integer idDocente) {
        Docente docente = docenteRepository.findById(idDocente)
                .orElseThrow(() -> new IllegalArgumentException("Error! El docente seleccionado no existe."));
        if (!docente.estaHabilitado()) {
            throw new IllegalArgumentException("Error! El docente " + docente.getUsuario().getNombreCompleto()
                    + " no se encuentra activo o habilitado.");
        }
        return docente;
    }

    private List<Docente> obtenerAyudantes(List<Integer> idsDocentesAyudantes) {
        List<Docente> ayudantes = new ArrayList<>();
        if (idsDocentesAyudantes == null) return ayudantes;
        for (Integer idDocente : new LinkedHashSet<>(idsDocentesAyudantes)) {
            if (idDocente != null) ayudantes.add(obtenerDocenteHabilitado(idDocente));
        }
        return ayudantes;
    }

    private void validarTitularEntreAyudantes(Docente titular, List<Docente> ayudantes) {
        if (ayudantes.stream().anyMatch(ayudante -> ayudante.getIdDocente() == titular.getIdDocente())) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idsDocentesAyudantes", "Error! El docente titular no puede ser también ayudante del curso.");
        }
    }

    private Set<Integer> modalidadesActuales(Curso curso) {
        Set<Integer> ids = new HashSet<>();
        curso.getCursoModalidades().forEach(cm -> ids.add(cm.getModalidad().getIdModalidad()));
        return ids;
    }

    private boolean cambiaDatosRestringidos(Curso curso, String nombre, String descripcion, Integer cargaHorariaMinutos,
            Integer duracionDias, Integer idCategoria, Integer idNivel, boolean emiteCertificado,
            List<Integer> idsModalidades) {
        return !curso.getNombre().equals(nombre.trim())
                || !Objects.equals(curso.getCargaHorariaMinutos(), cargaHorariaMinutos)
                || curso.getDuracionDias() != duracionDias
                || !Objects.equals(convertirVacioANulo(curso.getDescripcion()), convertirVacioANulo(descripcion))
                || curso.getCategoria().getIdCategoria() != idCategoria
                || (idNivel != null && (curso.getNivel() == null || curso.getNivel().getIdNivel() != idNivel))
                || curso.getEmiteCertificado() != emiteCertificado
                || !modalidadesActuales(curso).equals(new HashSet<>(idsModalidades));
    }

    private boolean tieneActividadVigente(DocenteCurso participacion) {
        return participacion.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja())
                || participacion.getTemasRecurso().stream().anyMatch(material -> !material.getBaja());
    }

    private void validarDesvinculaciones(Curso curso, Docente titular, List<Docente> ayudantes) {
        Set<Integer> nuevos = new HashSet<>();
        nuevos.add(titular.getIdDocente());
        ayudantes.forEach(ayudante -> nuevos.add(ayudante.getIdDocente()));
        for (DocenteCurso participacion : curso.getEquipoDocente()) {
            if (!nuevos.contains(participacion.getDocente().getIdDocente()) && tieneActividadVigente(participacion)) {
                throw new IllegalArgumentException("Error! El docente " + participacion.getDocente().getUsuario().getNombreCompleto()
                        + " tiene clases y/o material activo en el curso. Dé de baja primero sus clases y materiales "
                        + "para poder desvincularlo.");
            }
        }
    }

    /** Valida que, al agregar la modalidad En vivo, las cohortes existentes tengan fechas de dictado. */
    private void validarModalidadEnVivoAgregada(Curso curso, List<Integer> idsNuevas) {
        if (curso.incluyeModalidad(Modalidad.EN_VIVO)) return;
        boolean agregaEnVivo = idsNuevas.stream().map(modalidadRepository::findById)
                .anyMatch(m -> m.isPresent() && Modalidad.EN_VIVO.equals(m.get().getNombre()));
        int sinDictado = curso.getCohortesSinFechasDeDictado().size();
        if (agregaEnVivo && sinDictado > 0) {
            throw RespuestaFormularioUtil.armarCampoInvalido("idsModalidades", "Error! No puede agregarse la modalidad '"
                    + Modalidad.EN_VIVO + "': el curso tiene " + MensajeErrorUtil.formatearCantidad(sinDictado,
                            "cohorte sin fechas de dictado", "cohortes sin fechas de dictado") + ". Dé de baja "
                    + (sinDictado == 1 ? "la cohorte" : "las cohortes") + " antes.");
        }
    }

    private void validarModalidadesEliminadas(Curso curso, List<Integer> idsNuevas) {
        for (CursoModalidad cursoModalidad : curso.getCursoModalidades()) {
            if (idsNuevas.contains(cursoModalidad.getModalidad().getIdModalidad())) continue;
            String nombre = cursoModalidad.getModalidad().getNombre();
            if (Modalidad.EN_VIVO.equals(nombre)) {
                int conDictado = curso.getCohortesConFechasDeDictado().size();
                if (conDictado > 0) {
                    throw RespuestaFormularioUtil.armarCampoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '"
                            + nombre + "': el curso tiene " + MensajeErrorUtil.formatearCantidad(conDictado, "cohorte con fechas de dictado",
                                    "cohortes con fechas de dictado") + ". Dé de baja " + (conDictado == 1 ? "la cohorte" : "las cohortes")
                            + " antes.");
                }
            }
            boolean enVivo = curso.getParticipacionesDocente().stream()
                    .anyMatch(p -> p.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()));
            boolean clon = curso.getTemas().stream().flatMap(tema -> tema.getTemasRecurso().stream())
                    .anyMatch(material -> !material.getBaja() && material.getRecurso().getVideoClon() != null);
            if ((Modalidad.EN_VIVO.equals(nombre) && enVivo) || (Modalidad.CLON_IA.equals(nombre) && clon)) {
                throw RespuestaFormularioUtil.armarCampoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '" + nombre
                        + "' porque el curso tiene clases activas de esa modalidad.");
            }
            int materiales = curso.getMaterialesPublicadosDeModalidad(nombre).size();
            if (materiales > 0) {
                throw RespuestaFormularioUtil.armarCampoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '" + nombre
                        + "': el curso tiene " + materiales + " " + (materiales == 1
                                ? TemaRecurso.describirPorModalidad(nombre).replace("grabaciones", "grabación")
                                : TemaRecurso.describirPorModalidad(nombre))
                        + " publicad" + (materiales == 1 ? "a" : "as") + ". Dé de baja u ocultá "
                        + (materiales == 1 ? "ese material" : "esos materiales") + " antes.");
            }
        }
    }

    private void sincronizarModalidades(Curso curso, List<Integer> idsModalidades) {
        Set<Integer> deseadas = new HashSet<>(idsModalidades);
        for (CursoModalidad actual : new ArrayList<>(curso.getCursoModalidades())) {
            if (deseadas.contains(actual.getModalidad().getIdModalidad())) {
                deseadas.remove(actual.getModalidad().getIdModalidad());
            } else {
                curso.getCursoModalidades().remove(actual);
                cursoModalidadRepository.delete(actual);
            }
        }
        for (Integer idModalidad : deseadas) {
            Modalidad modalidad = modalidadRepository.findById(idModalidad)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La modalidad seleccionada no existe."));
            curso.getCursoModalidades().add(cursoModalidadRepository.save(new CursoModalidad(curso, modalidad)));
        }
    }

    private void sincronizarEquipoDocente(Curso curso, Docente titular, List<Docente> ayudantes) {
        Map<Integer, Docente> docentes = new LinkedHashMap<>();
        Map<Integer, Boolean> titulares = new LinkedHashMap<>();
        docentes.put(titular.getIdDocente(), titular);
        titulares.put(titular.getIdDocente(), true);
        for (Docente ayudante : ayudantes) {
            docentes.put(ayudante.getIdDocente(), ayudante);
            titulares.put(ayudante.getIdDocente(), false);
        }
        for (DocenteCurso participacion : new ArrayList<>(curso.getParticipacionesDocente())) {
            Integer idDocente = participacion.getDocente().getIdDocente();
            if (titulares.containsKey(idDocente)) {
                participacion.setEsTitular(titulares.get(idDocente));
                participacion.setBaja(false);
                participacion.setUltimaModificacion(LocalDateTime.now());
                docenteCursoRepository.save(participacion);
                docentes.remove(idDocente);
            } else if (!participacion.esInactivo()) {
                participacion.marcarInactivo();
                participacion.setCohortePorDefecto(null);
                participacion.setUltimaModificacion(LocalDateTime.now());
                docenteCursoRepository.save(participacion);
            }
        }
        for (Docente docente : docentes.values()) {
            curso.getParticipacionesDocente().add(docenteCursoRepository
                    .save(new DocenteCurso(curso, docente, titulares.get(docente.getIdDocente()))));
        }
    }

    /** Cuando el parámetro no está cargado. */
    private static final int MINIMO_POR_DEFECTO = 1;

    /** Verifica si hay algún pool vigente del tema o de un tema anterior del programa. */
    private boolean hayPoolsParaElTema(Programa programa, Tema tema) {
        if (tema.getPooles().stream().anyMatch(pool -> !pool.getBaja())) {
            return true;
        }
        if (programa == null) {
            return false;
        }
        for (var cronograma : programa.getCronogramaOrdenado()) {
            if (cronograma.getTema().getIdTema() == tema.getIdTema()) {
                return false;
            }
            if (cronograma.getTema().getPooles().stream().anyMatch(pool -> !pool.getBaja())) {
                return true;
            }
        }
        return false;
    }

    /** Calcula el estado del diseño de un curso. */
    @Override
    public EstadoDiseno calcularEstadoDiseno(Curso curso) {
        return calcularEstadoDiseno(curso, curso.getProgramaVigente());
    }

    /** Calcula el estado del diseño de un curso para una cohorte. */
    @Override
    public EstadoDiseno calcularEstadoDiseno(Curso curso, Cohorte cohorte) {
        return calcularEstadoDiseno(curso, cohorte == null ? curso.getProgramaVigente() : cohorte.getPrograma());
    }

    /** Calcula el estado del diseño de un curso tomando como programa el de una cohorte (el campo programaVigente del resultado es ese programa). */
    @Override
    public EstadoDiseno calcularEstadoDiseno(Curso curso, Programa programa) {
        int minimo = parametroService.obtenerEntero(Parametro.MINIMO_TEMAS_CON_MATERIAL, MINIMO_POR_DEFECTO);
        int temas = (int) curso.getTemas().stream().filter(tema -> !tema.getBaja()).count();
        Programa vigente = programa;
        // El contenido es el de los temas del curso (no solo los del programa): primero se arman los temas y después el programa
        List<ContenidoDeTema> contenido = new ArrayList<>();
        curso.getTemas().stream().filter(tema -> !tema.getBaja()).sorted(Comparator.comparingInt(Tema::getIdTema))
                .forEach(tema -> contenido.add(buscarContenidoDeTema(curso, tema)));
        List<Cohorte> pendientes = curso.getCohortes().stream().filter(c -> !c.getBaja() && c.esPendiente()).toList();
        LocalDateTime primera = pendientes.stream().map(Cohorte::getFechaInicioInscripcion).min(Comparator.naturalOrder())
                .orElse(null);
        return new EstadoDiseno(curso.getFaltantesDeDatosAcademicos(), temas, minimo, vigente,
                vigente != null && vigente.estaDuracionAsignadaCompleta(), contenido, pendientes.size(), primera);
    }

    /** Indica si un programa está completo. */
    @Override
    public boolean estaDisenoCompleto(Curso curso, Programa programa) {
        return programa.estaDuracionAsignadaCompleta() && programa.getCronogramaOrdenado().stream()
                .allMatch(cronograma -> buscarContenidoDeTema(curso, cronograma.getTema()).getFaltantes().isEmpty());
    }

    /** Calcula el siguiente recurso que le falta a un tema: primero el material (se propone una bibliografía). */
    @Override
    public SiguienteRecurso buscarSiguienteRecurso(Curso curso, Programa programa, Tema tema) {
        Map<String, Boolean> celdas = buscarContenidoDeTema(curso, tema).celdas();
        if (!celdas.getOrDefault("Material", true)) {
            return new SiguienteRecurso("BIBLIOGRAFIA", null);
        }
        if (!celdas.getOrDefault("Autoevaluación", true)) {
            if (hayPoolsParaElTema(programa, tema)) {
                return new SiguienteRecurso("AUTOEVALUACION", null);
            }
            return new SiguienteRecurso("POOL", "Para crear una autoevaluación tenés que registrar al menos un pool de preguntas, del mismo tema o de uno anterior.");
        }
        if (!celdas.getOrDefault("Clase grabada", true)) {
            return new SiguienteRecurso("GRABACION", null);
        }
        return new SiguienteRecurso(null, null);
    }

    /** Busca las columnas de contenido que corresponden a un curso, según sus modalidades. */
    @Override
    public List<String> buscarColumnasDeContenido(Curso curso) {
        List<String> columnas = new ArrayList<>();
        if (curso.incluyeModalidad(Modalidad.CLON_IA)) {
            columnas.add("Video clon");
        }
        if (curso.incluyeModalidad(Modalidad.GRABADA)) {
            columnas.add("Clase grabada");
        }
        columnas.add("Material");
        columnas.add("Autoevaluación");
        return columnas;
    }

    /** Calcula el contenido de un tema: lo que prometen las modalidades del curso. */
    @Override
    public ContenidoDeTema buscarContenidoDeTema(Curso curso, Tema tema) {
        Map<String, Boolean> celdas = new LinkedHashMap<>();
        List<TemaRecurso> publicados = tema.getMaterialesPublicados();
        if (curso.incluyeModalidad(Modalidad.CLON_IA)) {
            celdas.put("Video clon", publicados.stream().anyMatch(m -> m.getRecurso().getVideoClon() != null
                    && EstadoGeneracion.GENERADO.equals(m.getRecurso().getVideoClon().getEstadoGeneracion().getNombre())));
        }
        if (curso.incluyeModalidad(Modalidad.GRABADA)) {
            celdas.put("Clase grabada", publicados.stream().anyMatch(m -> Modalidad.GRABADA.equals(m.getModalidadAsociada())));
        }
        celdas.put("Material", publicados.stream().anyMatch(m -> m.getModalidadAsociada() == null));
        celdas.put("Autoevaluación", !tema.getAutoevaluacionesPublicadas().isEmpty());
        return new ContenidoDeTema(tema, celdas);
    }
}
