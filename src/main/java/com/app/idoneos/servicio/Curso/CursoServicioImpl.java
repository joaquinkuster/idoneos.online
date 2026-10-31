package com.app.idoneos.servicio.Curso;

import com.app.idoneos.utilidades.RespuestaFormularioUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import com.app.idoneos.utilidades.MensajeErrorUtilidad;
import java.time.LocalDateTime;
import com.app.idoneos.utilidades.ManejadorArchivosUtilidad;
import com.app.idoneos.utilidades.DuracionUtilidad;
import com.app.idoneos.servicio.Parametro.ParametroServicio;
import com.app.idoneos.servicio.Tema.DedicacionDocenteTemas;
import org.springframework.beans.factory.annotation.Qualifier;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import com.app.idoneos.modelo.*;
import com.app.idoneos.repositorio.*;
import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Categoria;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.repositorio.CursoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import com.app.idoneos.servicio.Inscripcion.InscripcionServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Curso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CursoServicio}.
 */
@Service
public class CursoServicioImpl implements CursoServicio, CrudServicio<Curso> {

    private static final Logger REGISTRO = LoggerFactory.getLogger(CursoServicioImpl.class);

    /** Carpeta base donde se guardan los archivos de los cursos. */
    @Autowired
    @Qualifier("directorioCursos")
    private Path directorioCursos;

    @Autowired
    private CursoRepositorio cursoRepositorio;

    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private NivelRepositorio nivelRepositorio;

    @Autowired
    private ModalidadRepositorio modalidadRepositorio;

    @Autowired
    private DocenteRepositorio docenteRepositorio;

    @Autowired
    private CursoModalidadRepositorio cursoModalidadRepositorio;

    @Autowired
    private DocenteCursoRepositorio docenteCursoRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private InscripcionServicio inscripcionServicio;

    @Autowired
    private ParametroServicio parametroServicio;

    @Autowired
    private DedicacionDocenteTemas dedicacionDocenteTemas;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el curso por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Curso> buscarPorId(Integer id) {
        return cursoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Curso> obtenerTodo() {
        return cursoRepositorio.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return cursoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca los registros vigentes de curso asociados a nivel.
     *
     * @param nivel el registro de Nivel asociado
     * @return una lista de Curso asociados al Nivel indicado
     */
    @Override
    public List<Curso> buscarPorNivel(Nivel nivel) {
        return cursoRepositorio.findByNivelAndBajaFalse(nivel);
    }

    /**
     * Busca los registros vigentes de curso asociados a categoria.
     *
     * @param categoria el registro de Categoria asociado
     * @return una lista de Curso asociados al Categoria indicado
     */
    @Override
    public List<Curso> buscarPorCategoria(Categoria categoria) {
        return cursoRepositorio.findByCategoriaAndBajaFalse(categoria);
    }

    /**
     * Busca el curso por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Curso> buscarPorNombre(String nombre) {
        return cursoRepositorio.findByNombre(nombre);
    }

    /**
     * Busca cursos aplicando filtros opcionales. Incluye los cursos dados de baja, que se ordenan siempre al final.
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @param orden el orden de los resultados: "nombre" (A–Z, por defecto) o "recientes" (más nuevos primero)
     * @param soloDeDocente si no es {@code null}, restringe el resultado a los cursos en los que ese docente participa
     * @return una lista de cursos que cumplen los criterios
     */
    @Override
    public List<Curso> buscarConFiltros(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad, String orden, Docente soloDeDocente) {
        String criterio = texto == null ? "" : texto.trim().toLowerCase();
        Comparator<Curso> criterioDeOrden = Comparator.comparing(Curso::esInactivo)
                .thenComparing("recientes".equals(orden)
                        ? Comparator.comparing(Curso::getFechaCreacion).thenComparingInt(Curso::getIdCurso).reversed()
                        : Comparator.comparing(Curso::getNombre, String.CASE_INSENSITIVE_ORDER));
        return cursoRepositorio.findAll().stream()
                .filter(curso -> criterio.isEmpty() || curso.getNombre().toLowerCase().contains(criterio)
                        || (curso.getDescripcion() != null && curso.getDescripcion().toLowerCase().contains(criterio)))
                .filter(curso -> idCategoria == null || curso.getCategoria().getIdCategoria() == idCategoria)
                .filter(curso -> idNivel == null || curso.getNivel().getIdNivel() == idNivel)
                .filter(curso -> idDocente == null || curso.tieneDocente(idDocente))
                .filter(curso -> idModalidad == null || curso.getModalidades().stream()
                        .anyMatch(modalidad -> modalidad.getIdModalidad() == idModalidad))
                .filter(curso -> soloDeDocente == null || curso.tieneDocente(soloDeDocente))
                .sorted(criterioDeOrden)
                .toList();
    }

    /**
     * Busca los cursos del catálogo público: cursos activos con al menos una cohorte con inscripción abierta.
     *
     * @param texto parte del nombre o la descripción del curso (opcional)
     * @param idCategoria identificador de la categoría (opcional)
     * @param idNivel identificador del nivel (opcional)
     * @param idDocente identificador de un docente del equipo docente (opcional)
     * @param idModalidad identificador de una modalidad de dictado (opcional)
     * @return una lista de cursos con cohortes abiertas
     */
    @Override
    public List<Curso> buscarEnCatalogo(String texto, Integer idCategoria, Integer idNivel, Integer idDocente,
            Integer idModalidad) {
        return buscarConFiltros(texto, idCategoria, idNivel, idDocente, idModalidad, "recientes", null).stream()
                .filter(curso -> !curso.esInactivo())
                .filter(curso -> !buscarCohortesAbiertas(curso).isEmpty())
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

    /**
     * Busca las cohortes con inscripción abierta de un curso.
     *
     * @param curso el curso a consultar
     * @return una lista de cohortes abiertas, de la que abre antes a la que abre después
     */
    @Override
    public List<Cohorte> buscarCohortesAbiertas(Curso curso) {
        return curso.getProgramas().stream()
                .filter(programa -> !programa.esInactivo())
                .flatMap(programa -> programa.getCohortes().stream())
                .filter(cohorte -> !cohorte.esInactivo() && cohorte.estaAbierta())
                .sorted(Comparator.comparing(Cohorte::getFechaInicioInscripcion))
                .toList();
    }

    /**
     * Verifica si un curso tiene inscripciones activas asociadas.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos una inscripción activa
     */
    @Override
    public boolean tieneInscripcionesActivas(Curso curso) {
        return inscripcionRepositorio.contarActivasPorCurso(curso) > 0;
    }

    /**
     * Verifica si un curso tiene programas o temas activos asociados.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos un programa o un tema activos
     */
    @Override
    public boolean tieneProgramasOTemasActivas(Curso curso) {
        return !programaRepositorio.findByCursoAndBajaFalse(curso).isEmpty()
                || !temaRepositorio.findByCursoAndBajaFalse(curso).isEmpty();
    }

    /**
     * Busca las inscripciones activas (no dadas de baja) de un curso, a través de sus programas y cohortes.
     *
     * @param curso el curso a consultar
     * @return una lista de inscripciones activas del curso
     */
    @Override
    public List<Inscripcion> buscarInscripcionesActivas(Curso curso) {
        return curso.getProgramas().stream()
                .flatMap(programa -> programa.getCohortes().stream())
                .flatMap(cohorte -> cohorte.getInscripciones().stream())
                .filter(inscripcion -> !inscripcion.esInactivo())
                .toList();
    }

    /**
     * Verifica que el docente pueda acceder al curso (CU-27).
     *
     * @param idCurso El identificador del curso.
     * @param docente El docente que solicita el acceso.
     * @return La participación del docente en el curso.
     * @throws IllegalArgumentException Si el docente no puede acceder, con el motivo.
     */
    @Override
    public DocenteCurso validarAccesoDocente(int idCurso, Docente docente) {
        Curso curso = buscarActivoPorId(idCurso);
        DocenteCurso participacion = docenteCursoRepositorio.findByCursoAndBajaFalse(curso).stream()
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
        return inscripcionServicio.buscarConAccesoATema(usuario.getAlumno(), tema).map(ContextoCurso::deAlumno);
    }

    @Override
    public ResumenCursos resumir(List<Curso> cursos) {
        Map<Integer, List<Inscripcion>> inscripciones = new HashMap<>();
        Map<Integer, List<Programa>> programas = new HashMap<>();
        Set<Integer> conInscripcionAbierta = new HashSet<>();
        for (Curso curso : cursos) {
            inscripciones.put(curso.getIdCurso(), buscarInscripcionesActivas(curso));
            programas.put(curso.getIdCurso(), programaRepositorio.findByCursoAndBajaFalse(curso));
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

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el curso en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Curso guardar(Curso entidad) {
        return cursoRepositorio.save(entidad);
    }

    /**
     * Registra un curso con sus modalidades de dictado y su equipo docente.
     *
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param cargaHorariaMinutos la carga horaria del curso para el alumno, en minutos
     * @param dedicacionDocenteMinutos la dedicación docente total del curso, en minutos
     * @param duracionMaximaDias la duración máxima del curso, en días
     * @param imagen la ruta de la imagen de portada (opcional)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso registrado
     * @throws IllegalArgumentException si no se cumple alguna regla de registro
     */
    @Override
    @Transactional
    public Curso registrarCurso(String nombre, String descripcion, Float precio, Integer cargaHorariaMinutos,
            Integer dedicacionDocenteMinutos, Integer duracionMaximaDias, String imagen, Integer idCategoria, Integer idNivel, boolean emiteCertificado,
            List<Integer> idsModalidades, Integer idDocenteTitular, List<Integer> idsDocentesAyudantes) {
        validarCamposObligatorios(nombre, precio, cargaHorariaMinutos, dedicacionDocenteMinutos, duracionMaximaDias, idCategoria, idNivel,
                idsModalidades, idDocenteTitular);
        Categoria categoria = obtenerCategoriaActiva(idCategoria);
        Nivel nivel = obtenerNivel(idNivel);
        Docente titular = obtenerDocenteHabilitado(idDocenteTitular);
        List<Docente> ayudantes = obtenerAyudantes(idsDocentesAyudantes);
        validarTitularEntreAyudantes(titular, ayudantes);
        validarPrecio(precio);
        validarCargaHoraria(cargaHorariaMinutos);
        validarDedicacionDocente(dedicacionDocenteMinutos);
        validarDuracionMaxima(duracionMaximaDias);
        validarCargaContraDuracionMaxima(cargaHorariaMinutos, duracionMaximaDias);
        String nombreLimpio = nombre.trim();
        if (cursoRepositorio.findByNombre(nombreLimpio).isPresent()) {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", "Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        }

        Curso curso = new Curso(nivel, categoria, nombreLimpio, precio, cargaHorariaMinutos, dedicacionDocenteMinutos,
                duracionMaximaDias);
        curso.setDescripcion(vacioANulo(descripcion));
        curso.setImagen(vacioANulo(imagen));
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepositorio.save(curso);
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Curso modificar(Curso entidad) {
        return cursoRepositorio.save(entidad);
    }

    /**
     * Modifica un curso activo. Si tiene inscripciones activas, solo pueden modificarse el precio,
     * el equipo docente y la imagen de portada.
     *
     * @param idCurso el identificador del curso
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
     * @param cargaHorariaMinutos la carga horaria del curso para el alumno, en minutos
     * @param dedicacionDocenteMinutos la dedicación docente total del curso, en minutos
     * @param duracionMaximaDias la duración máxima del curso, en días
     * @param imagen la ruta de la imagen de portada (opcional, si es nula se conserva la actual)
     * @param idCategoria el identificador de la categoría
     * @param idNivel el identificador del nivel
     * @param emiteCertificado si el curso emite certificado al finalizar
     * @param idsModalidades los identificadores de las modalidades de dictado
     * @param idDocenteTitular el identificador del docente titular
     * @param idsDocentesAyudantes los identificadores de los docentes ayudantes (opcional)
     * @return el curso modificado
     * @throws IllegalArgumentException si no se cumple alguna regla de modificación
     */
    @Override
    @Transactional
    public Curso modificarCurso(Integer idCurso, String nombre, String descripcion, Float precio,
            Integer cargaHorariaMinutos, Integer dedicacionDocenteMinutos, Integer duracionMaximaDias, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            List<Integer> idsDocentesAyudantes) {
        Curso curso = buscarActivoPorId(idCurso);
        validarCamposObligatorios(nombre, precio, cargaHorariaMinutos, dedicacionDocenteMinutos, duracionMaximaDias, idCategoria, idNivel,
                idsModalidades, idDocenteTitular);
        if (tieneInscripcionesActivas(curso)
                && cambiaDatosRestringidos(curso, nombre, descripcion, cargaHorariaMinutos, dedicacionDocenteMinutos, duracionMaximaDias,
                        idCategoria, idNivel, emiteCertificado, idsModalidades)) {
            throw new IllegalArgumentException("Error! El curso tiene inscripciones activas: solo pueden modificarse "
                    + "el precio, el equipo docente y la imagen de portada.");
        }
        Categoria categoria = obtenerCategoriaActiva(idCategoria);
        Nivel nivel = obtenerNivel(idNivel);
        Docente titular = obtenerDocenteHabilitado(idDocenteTitular);
        List<Docente> ayudantes = obtenerAyudantes(idsDocentesAyudantes);
        validarTitularEntreAyudantes(titular, ayudantes);
        validarDesvinculaciones(curso, titular, ayudantes);
        validarModalidadesEliminadas(curso, idsModalidades);
        validarModalidadEnVivoAgregada(curso, idsModalidades);
        validarPrecio(precio);
        validarCargaHoraria(cargaHorariaMinutos);
        validarDedicacionDocente(dedicacionDocenteMinutos);
        validarDuracionMaxima(duracionMaximaDias);
        validarCargaContraDuracionMaxima(cargaHorariaMinutos, duracionMaximaDias);
        validarDuracionMaximaContraCronogramas(curso, duracionMaximaDias);
        boolean cambiaDedicacion = curso.getDedicacionDocenteMinutos() != dedicacionDocenteMinutos;
        if (cambiaDedicacion) {
            dedicacionDocenteTemas.validarDedicacionDelCurso(curso, dedicacionDocenteMinutos);
        }
        String nombreLimpio = nombre.trim();
        cursoRepositorio.findByNombre(nombreLimpio).filter(otro -> otro.getIdCurso() != idCurso).ifPresent(otro -> {
            throw RespuestaFormularioUtilidad.campoInvalido("nombre", "Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        });

        curso.setNombre(nombreLimpio);
        curso.setDescripcion(vacioANulo(descripcion));
        curso.setPrecio(precio);
        curso.setCargaHorariaMinutos(cargaHorariaMinutos);
        curso.setDedicacionDocenteMinutos(dedicacionDocenteMinutos);
        curso.setDuracionMaximaDias(duracionMaximaDias);
        if (imagen != null && !imagen.isBlank()) {
            curso.setImagen(imagen.trim());
        }
        curso.setCategoria(categoria);
        curso.setNivel(nivel);
        curso.setEmiteCertificado(emiteCertificado);
        curso.setUltimaModificacion(LocalDateTime.now());
        curso = cursoRepositorio.save(curso);
        if (cambiaDedicacion) {
            // La nueva dedicación docente se reparte entre los temas del curso en proporción a lo que tiene cada uno
            dedicacionDocenteTemas.redistribuir(curso);
        }
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    /**
     * Asigna la imagen de un curso activo.
     *
     * @param idCurso El identificador del curso.
     * @param imagen  El nombre del archivo de la imagen dentro de la carpeta del curso.
     * @return El curso actualizado.
     * @throws IllegalArgumentException Si el curso no está activo.
     */
    @Override
    @Transactional
    public Curso actualizarImagen(Integer idCurso, String imagen) {
        Curso curso = buscarActivoPorId(idCurso);
        curso.setImagen(imagen);
        curso.setUltimaModificacion(LocalDateTime.now());
        return cursoRepositorio.save(curso);
    }

    /**
     * Lleva las imágenes de los cursos a la estructura {@code curso{id}/imagen.{ext}}. La imagen se busca en la
     * carpeta anterior ({@code cursos/}) y, si no está, entre las imágenes de ejemplo de la aplicación.
     */
    @Override
    @Transactional
    public void migrarImagenes() {
        for (Curso curso : cursoRepositorio.findAll()) {
            String imagen = curso.getImagen();
            if (imagen == null || imagen.isBlank() || imagen.startsWith("imagen.") || imagen.contains("/")
                    || imagen.contains("\\") || imagen.contains("..")) {
                continue;
            }
            String extension = ManejadorArchivosUtilidad.extensionDe(imagen);
            if (extension == null) {
                continue;
            }
            extension = extension.equals("jpeg") ? "jpg" : extension;
            try {
                Path carpeta = ManejadorArchivosUtilidad.carpetaDeCurso(directorioCursos, curso.getIdCurso());
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
                cursoRepositorio.save(curso);
            } catch (IOException e) {
                REGISTRO.warn("No se pudo migrar la imagen '{}' del curso {}", imagen, curso.getIdCurso(), e);
            }
        }
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Curso entidad) {
        entidad.marcarInactivo(); // Baja lógica
        cursoRepositorio.save(entidad);
    }

    /**
     * Da de baja un curso activo que no tenga programas ni temas activos asociadas.
     *
     * @param idCurso el identificador del curso
     * @throws IllegalArgumentException si el curso no está activo o tiene programas o temas activos
     */
    @Override
    @Transactional
    public void darDeBajaCurso(Integer idCurso) {
        Curso curso = buscarActivoPorId(idCurso);
        int programasActivos = programaRepositorio.findByCursoAndBajaFalse(curso).size();
        int temasActivas = temaRepositorio.findByCursoAndBajaFalse(curso).size();
        if (programasActivos > 0 || temasActivas > 0) {
            String motivo = (programasActivos > 0
                    ? MensajeErrorUtilidad.cantidad(programasActivos, "programa activo", "programas activos") : "")
                    + (programasActivos > 0 && temasActivas > 0 ? " y " : "")
                    + (temasActivas > 0
                            ? MensajeErrorUtilidad.cantidad(temasActivas, "tema activo", "temas activos") : "");
            throw new IllegalArgumentException(MensajeErrorUtilidad.bajaBloqueada("el curso", curso.getNombre(),
                    "tiene " + motivo, "Dé de baja primero sus programas y temas."));
        }
        curso.marcarInactivo();
        curso.setUltimaModificacion(LocalDateTime.now());
        cursoRepositorio.save(curso);
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
                darDeBajaCurso(id);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
    }

    private String vacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private void validarCamposObligatorios(String nombre, Float precio, Integer cargaHorariaMinutos,
            Integer dedicacionDocenteMinutos, Integer duracionMaximaDias, Integer idCategoria, Integer idNivel, List<Integer> idsModalidades,
            Integer idDocenteTitular) {
        List<String> faltantes = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) faltantes.add("nombre");
        if (precio == null) faltantes.add("precio");
        if (cargaHorariaMinutos == null) faltantes.add("carga horaria");
        if (dedicacionDocenteMinutos == null) faltantes.add("dedicación docente");
        if (duracionMaximaDias == null) faltantes.add("duración máxima");
        if (idCategoria == null) faltantes.add("categoría");
        if (idNivel == null) faltantes.add("nivel");
        if (idsModalidades == null || idsModalidades.isEmpty()) faltantes.add("al menos una modalidad");
        if (idDocenteTitular == null) faltantes.add("docente titular");
        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Error! Faltan completar los campos obligatorios: "
                    + String.join(", ", faltantes) + ".");
        }
    }

    private void validarPrecio(Float precio) {
        if (precio < 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("precio", MensajeErrorUtilidad.formatoInvalido("El precio", "número desde 0 hasta $" + String.format("%,.0f", Curso.PRECIO_MAXIMO).replace(',', '.')));
        }
        if (precio > Curso.PRECIO_MAXIMO) {
            throw RespuestaFormularioUtilidad.campoInvalido("precio", MensajeErrorUtilidad.formatoInvalido("El precio", "número desde 0 hasta $" + String.format("%,.0f", Curso.PRECIO_MAXIMO).replace(',', '.')));
        }
    }

    private void validarCargaHoraria(Integer cargaHorariaMinutos) {
        if (cargaHorariaMinutos <= 0 || cargaHorariaMinutos > Curso.CARGA_HORARIA_MAXIMA_MINUTOS) {
            throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria", MensajeErrorUtilidad.formatoInvalido(
                    "La carga horaria", "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(Curso.CARGA_HORARIA_MAXIMA_MINUTOS)));
        }
    }

    private void validarDedicacionDocente(Integer dedicacionDocenteMinutos) {
        if (dedicacionDocenteMinutos <= 0 || dedicacionDocenteMinutos > Curso.DEDICACION_DOCENTE_MAXIMA_MINUTOS) {
            throw RespuestaFormularioUtilidad.campoInvalido("dedicacionDocente", MensajeErrorUtilidad.formatoInvalido(
                    "La dedicación docente", "mayor a cero y hasta " + DuracionUtilidad.formatearMinutos(Curso.DEDICACION_DOCENTE_MAXIMA_MINUTOS)));
        }
    }

    /** La duración máxima de un curso no puede superar el máximo que el sistema admite para un cronograma. */
    private void validarDuracionMaxima(Integer duracionMaximaDias) {
        int maximo = parametroServicio.obtenerEntero(Parametro.CRONOGRAMA_DIAS_MAXIMOS,
                Parametro.CRONOGRAMA_DIAS_MAXIMOS_POR_DEFECTO);
        if (duracionMaximaDias <= 0 || duracionMaximaDias > maximo) {
            throw RespuestaFormularioUtilidad.campoInvalido("duracionMaxima", MensajeErrorUtilidad.formatoInvalido(
                    "La duración máxima", "mayor a cero y hasta " + DuracionUtilidad.formatear(maximo)));
        }
    }

    /**
     * La carga horaria del alumno no puede ser mayor que el tiempo que dura el curso: la duración máxima tiene como mucho
     * 24 horas por día, así que un curso de 1 día no admite 25 horas.
     */
    private void validarCargaContraDuracionMaxima(Integer cargaHorariaMinutos, Integer duracionMaximaDias) {
        long maximo = (long) duracionMaximaDias * DuracionUtilidad.MINUTOS_POR_DIA;
        if (cargaHorariaMinutos > maximo) {
            throw RespuestaFormularioUtilidad.campoInvalido("cargaHoraria",
                    "Error! La carga horaria no puede ser mayor al tiempo de la duración máxima del curso.");
        }
    }

    /** La duración máxima no puede quedar por debajo del cronograma de ningún programa vigente del curso. */
    private void validarDuracionMaximaContraCronogramas(Curso curso, Integer duracionMaximaDias) {
        for (Programa programa : curso.getProgramas()) {
            if (!programa.esInactivo() && programa.getDuracionTotalDias() > duracionMaximaDias) {
                throw RespuestaFormularioUtilidad.campoInvalido("duracionMaxima", "Error! La duración máxima ("
                        + DuracionUtilidad.formatear(duracionMaximaDias) + ") no puede ser menor a la duración del cronograma del programa «"
                        + programa.getNombre() + "» (" + DuracionUtilidad.formatear(programa.getDuracionTotalDias()) + ").");
            }
        }
    }

    private Categoria obtenerCategoriaActiva(Integer idCategoria) {
        return categoriaRepositorio.findById(idCategoria).filter(categoria -> !categoria.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! La categoría seleccionada no se encuentra activa."));
    }

    private Nivel obtenerNivel(Integer idNivel) {
        return nivelRepositorio.findById(idNivel)
                .orElseThrow(() -> new IllegalArgumentException("Error! El nivel seleccionado no existe."));
    }

    private Docente obtenerDocenteHabilitado(Integer idDocente) {
        Docente docente = docenteRepositorio.findById(idDocente)
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
            throw RespuestaFormularioUtilidad.campoInvalido("idsDocentesAyudantes", "Error! El docente titular no puede ser también ayudante del curso.");
        }
    }

    private Set<Integer> modalidadesActuales(Curso curso) {
        Set<Integer> ids = new HashSet<>();
        curso.getCursoModalidades().forEach(cm -> ids.add(cm.getModalidad().getIdModalidad()));
        return ids;
    }

    private boolean cambiaDatosRestringidos(Curso curso, String nombre, String descripcion, Integer cargaHorariaMinutos,
            Integer dedicacionDocenteMinutos, Integer duracionMaximaDias, Integer idCategoria, Integer idNivel, boolean emiteCertificado,
            List<Integer> idsModalidades) {
        return !curso.getNombre().equals(nombre.trim())
                || curso.getCargaHorariaMinutos() != cargaHorariaMinutos
                || curso.getDedicacionDocenteMinutos() != dedicacionDocenteMinutos
                || curso.getDuracionMaximaDias() != duracionMaximaDias
                || !Objects.equals(vacioANulo(curso.getDescripcion()), vacioANulo(descripcion))
                || curso.getCategoria().getIdCategoria() != idCategoria
                || curso.getNivel().getIdNivel() != idNivel
                || curso.getEmiteCertificado() != emiteCertificado
                || !modalidadesActuales(curso).equals(new HashSet<>(idsModalidades));
    }

    private boolean tieneActividadVigente(DocenteCurso participacion) {
        return participacion.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja())
                || participacion.getClasesClon().stream().anyMatch(clase -> !clase.getBaja())
                || participacion.getMateriales().stream().anyMatch(material -> !material.getBaja());
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

    /** Agregar la modalidad En vivo exige que las cohortes existentes tengan fechas de dictado. */
    private void validarModalidadEnVivoAgregada(Curso curso, List<Integer> idsNuevas) {
        if (curso.incluyeModalidad(Modalidad.EN_VIVO)) return;
        boolean agregaEnVivo = idsNuevas.stream().map(modalidadRepositorio::findById)
                .anyMatch(m -> m.isPresent() && Modalidad.EN_VIVO.equals(m.get().getNombre()));
        int sinDictado = curso.getCohortesSinFechasDeDictado().size();
        if (agregaEnVivo && sinDictado > 0) {
            throw RespuestaFormularioUtilidad.campoInvalido("idsModalidades", "Error! No puede agregarse la modalidad '"
                    + Modalidad.EN_VIVO + "': el curso tiene " + MensajeErrorUtilidad.cantidad(sinDictado,
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
                    throw RespuestaFormularioUtilidad.campoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '"
                            + nombre + "': el curso tiene " + MensajeErrorUtilidad.cantidad(conDictado, "cohorte con fechas de dictado",
                                    "cohortes con fechas de dictado") + ". Dé de baja " + (conDictado == 1 ? "la cohorte" : "las cohortes")
                            + " antes.");
                }
            }
            boolean enVivo = curso.getParticipacionesDocente().stream()
                    .anyMatch(p -> p.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()));
            boolean clon = curso.getParticipacionesDocente().stream()
                    .anyMatch(p -> p.getClasesClon().stream().anyMatch(clase -> !clase.getBaja()));
            if ((Modalidad.EN_VIVO.equals(nombre) && enVivo) || (Modalidad.CLON_IA.equals(nombre) && clon)) {
                throw RespuestaFormularioUtilidad.campoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '" + nombre
                        + "' porque el curso tiene clases activas de esa modalidad.");
            }
            int materiales = curso.getMaterialesPublicadosDeModalidad(nombre).size();
            if (materiales > 0) {
                throw RespuestaFormularioUtilidad.campoInvalido("idsModalidades", "Error! No puede quitarse la modalidad '" + nombre
                        + "': el curso tiene " + materiales + " " + (materiales == 1
                                ? Material.describirPorModalidad(nombre).replace("grabaciones", "grabación")
                                : Material.describirPorModalidad(nombre))
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
                cursoModalidadRepositorio.delete(actual);
            }
        }
        for (Integer idModalidad : deseadas) {
            Modalidad modalidad = modalidadRepositorio.findById(idModalidad)
                    .orElseThrow(() -> new IllegalArgumentException("Error! La modalidad seleccionada no existe."));
            curso.getCursoModalidades().add(cursoModalidadRepositorio.save(new CursoModalidad(curso, modalidad)));
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
                docenteCursoRepositorio.save(participacion);
                docentes.remove(idDocente);
            } else if (!participacion.esInactivo()) {
                participacion.marcarInactivo();
                participacion.setProgramaPorDefecto(null);
                participacion.setCohortePorDefecto(null);
                participacion.setUltimaModificacion(LocalDateTime.now());
                docenteCursoRepositorio.save(participacion);
            }
        }
        for (Docente docente : docentes.values()) {
            curso.getParticipacionesDocente().add(docenteCursoRepositorio
                    .save(new DocenteCurso(curso, docente, titulares.get(docente.getIdDocente()))));
        }
    }
}
