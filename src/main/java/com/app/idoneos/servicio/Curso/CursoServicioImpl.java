package com.app.idoneos.servicio.Curso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Curso}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link CursoServicio}.
 */
@Service
public class CursoServicioImpl implements CursoServicio, CrudServicio<Curso> {

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
    private ParticipacionDocenteRepositorio participacionDocenteRepositorio;

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private ProgramaRepositorio programaRepositorio;

    @Autowired
    private UnidadRepositorio unidadRepositorio;

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
                .filter(curso -> idDocente == null || participa(curso, idDocente))
                .filter(curso -> idModalidad == null || curso.getModalidades().stream()
                        .anyMatch(modalidad -> modalidad.getIdModalidad() == idModalidad))
                .filter(curso -> soloDeDocente == null || participa(curso, soloDeDocente.getIdDocente()))
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
     * Verifica si un curso tiene programas o unidades activos asociados.
     *
     * @param curso el curso a consultar
     * @return {@code true} si tiene al menos un programa o una unidad activos
     */
    @Override
    public boolean tieneProgramasOUnidadesActivas(Curso curso) {
        return !programaRepositorio.findByCursoAndBajaFalse(curso).isEmpty()
                || !unidadRepositorio.findByCursoAndBajaFalse(curso).isEmpty();
    }

    /**
     * Registra un curso con sus modalidades de dictado y su equipo docente.
     *
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
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
    public Curso registrarCurso(String nombre, String descripcion, Float precio, String imagen, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades, Integer idDocenteTitular,
            List<Integer> idsDocentesAyudantes) {
        validarCamposObligatorios(nombre, precio, idCategoria, idNivel, idsModalidades, idDocenteTitular);
        Categoria categoria = obtenerCategoriaActiva(idCategoria);
        Nivel nivel = obtenerNivel(idNivel);
        Docente titular = obtenerDocenteHabilitado(idDocenteTitular);
        List<Docente> ayudantes = obtenerAyudantes(idsDocentesAyudantes);
        validarTitularEntreAyudantes(titular, ayudantes);
        validarPrecio(precio);
        String nombreLimpio = nombre.trim();
        if (cursoRepositorio.findByNombre(nombreLimpio).isPresent()) {
            throw new IllegalArgumentException("Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        }

        Curso curso = new Curso(nivel, categoria, nombreLimpio, precio);
        curso.setDescripcion(vacioANulo(descripcion));
        curso.setImagen(vacioANulo(imagen));
        curso.setEmiteCertificado(emiteCertificado);
        curso = cursoRepositorio.save(curso);
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    /**
     * Modifica un curso activo. Si tiene inscripciones activas, solo pueden modificarse el precio,
     * el equipo docente y la imagen de portada.
     *
     * @param idCurso el identificador del curso
     * @param nombre el nombre del curso
     * @param descripcion la descripción del curso (opcional)
     * @param precio el precio del curso
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
    public Curso modificarCurso(Integer idCurso, String nombre, String descripcion, Float precio, String imagen,
            Integer idCategoria, Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades,
            Integer idDocenteTitular, List<Integer> idsDocentesAyudantes) {
        Curso curso = cursoRepositorio.findById(idCurso).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        validarCamposObligatorios(nombre, precio, idCategoria, idNivel, idsModalidades, idDocenteTitular);
        if (tieneInscripcionesActivas(curso)
                && cambiaDatosRestringidos(curso, nombre, descripcion, idCategoria, idNivel, emiteCertificado, idsModalidades)) {
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
        validarPrecio(precio);
        String nombreLimpio = nombre.trim();
        cursoRepositorio.findByNombre(nombreLimpio).filter(otro -> otro.getIdCurso() != idCurso).ifPresent(otro -> {
            throw new IllegalArgumentException("Error! Ya existe un curso con el nombre '" + nombreLimpio + "'.");
        });

        curso.setNombre(nombreLimpio);
        curso.setDescripcion(vacioANulo(descripcion));
        curso.setPrecio(precio);
        if (imagen != null && !imagen.isBlank()) {
            curso.setImagen(imagen.trim());
        }
        curso.setCategoria(categoria);
        curso.setNivel(nivel);
        curso.setEmiteCertificado(emiteCertificado);
        curso.setUltimaModificacion(LocalDateTime.now());
        curso = cursoRepositorio.save(curso);
        sincronizarModalidades(curso, idsModalidades);
        sincronizarEquipoDocente(curso, titular, ayudantes);
        return curso;
    }

    /**
     * Da de baja un curso activo que no tenga programas ni unidades activas asociadas.
     *
     * @param idCurso el identificador del curso
     * @throws IllegalArgumentException si el curso no está activo o tiene programas o unidades activas
     */
    @Override
    @Transactional
    public void darDeBajaCurso(Integer idCurso) {
        Curso curso = cursoRepositorio.findById(idCurso).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        if (tieneProgramasOUnidadesActivas(curso)) {
            throw new IllegalArgumentException("Error! El curso tiene programas y/o unidades activas asociadas. "
                    + "Dé de baja primero sus programas y unidades.");
        }
        curso.marcarInactivo();
        curso.setUltimaModificacion(LocalDateTime.now());
        cursoRepositorio.save(curso);
    }

    // ---------------------------------------------------------------- métodos auxiliares

    private boolean participa(Curso curso, int idDocente) {
        return curso.getEquipoDocente().stream()
                .anyMatch(participacion -> participacion.getDocente().getIdDocente() == idDocente);
    }

    private String vacioANulo(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private void validarCamposObligatorios(String nombre, Float precio, Integer idCategoria, Integer idNivel,
            List<Integer> idsModalidades, Integer idDocenteTitular) {
        List<String> faltantes = new ArrayList<>();
        if (nombre == null || nombre.isBlank()) faltantes.add("nombre");
        if (precio == null) faltantes.add("precio");
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
            throw new IllegalArgumentException("Error! El precio ingresado debe ser mayor o igual a cero.");
        }
        if (precio > Curso.PRECIO_MAXIMO) {
            throw new IllegalArgumentException("Error! El precio ingresado no puede superar los $"
                    + String.format("%,.0f", Curso.PRECIO_MAXIMO).replace(',', '.') + " (ARS).");
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
            throw new IllegalArgumentException("Error! El docente titular no puede ser también ayudante del curso.");
        }
    }

    private Set<Integer> modalidadesActuales(Curso curso) {
        Set<Integer> ids = new HashSet<>();
        curso.getCursoModalidades().forEach(cm -> ids.add(cm.getModalidad().getIdModalidad()));
        return ids;
    }

    private boolean cambiaDatosRestringidos(Curso curso, String nombre, String descripcion, Integer idCategoria,
            Integer idNivel, boolean emiteCertificado, List<Integer> idsModalidades) {
        return !curso.getNombre().equals(nombre.trim())
                || !Objects.equals(vacioANulo(curso.getDescripcion()), vacioANulo(descripcion))
                || curso.getCategoria().getIdCategoria() != idCategoria
                || curso.getNivel().getIdNivel() != idNivel
                || curso.getEmiteCertificado() != emiteCertificado
                || !modalidadesActuales(curso).equals(new HashSet<>(idsModalidades));
    }

    private boolean tieneActividadVigente(ParticipacionDocente participacion) {
        return participacion.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja())
                || participacion.getClasesClon().stream().anyMatch(clase -> !clase.getBaja())
                || participacion.getMateriales().stream().anyMatch(material -> !material.getBaja());
    }

    private void validarDesvinculaciones(Curso curso, Docente titular, List<Docente> ayudantes) {
        Set<Integer> nuevos = new HashSet<>();
        nuevos.add(titular.getIdDocente());
        ayudantes.forEach(ayudante -> nuevos.add(ayudante.getIdDocente()));
        for (ParticipacionDocente participacion : curso.getEquipoDocente()) {
            if (!nuevos.contains(participacion.getDocente().getIdDocente()) && tieneActividadVigente(participacion)) {
                throw new IllegalArgumentException("Error! El docente " + participacion.getDocente().getUsuario().getNombreCompleto()
                        + " tiene clases y/o material activo en el curso. Dé de baja primero sus clases y materiales "
                        + "para poder desvincularlo.");
            }
        }
    }

    private void validarModalidadesEliminadas(Curso curso, List<Integer> idsNuevas) {
        for (CursoModalidad cursoModalidad : curso.getCursoModalidades()) {
            if (idsNuevas.contains(cursoModalidad.getModalidad().getIdModalidad())) continue;
            String nombre = cursoModalidad.getModalidad().getNombre();
            boolean enVivo = curso.getParticipacionesDocente().stream()
                    .anyMatch(p -> p.getClasesEnVivo().stream().anyMatch(clase -> !clase.getBaja()));
            boolean clon = curso.getParticipacionesDocente().stream()
                    .anyMatch(p -> p.getClasesClon().stream().anyMatch(clase -> !clase.getBaja()));
            if ((Modalidad.EN_VIVO.equals(nombre) && enVivo) || (Modalidad.CLON_IA.equals(nombre) && clon)) {
                throw new IllegalArgumentException("Error! No puede quitarse la modalidad '" + nombre
                        + "' porque el curso tiene clases activas de esa modalidad.");
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
        for (ParticipacionDocente participacion : new ArrayList<>(curso.getParticipacionesDocente())) {
            Integer idDocente = participacion.getDocente().getIdDocente();
            if (titulares.containsKey(idDocente)) {
                participacion.setEsTitular(titulares.get(idDocente));
                participacion.setBaja(false);
                participacion.setUltimaModificacion(LocalDateTime.now());
                participacionDocenteRepositorio.save(participacion);
                docentes.remove(idDocente);
            } else if (!participacion.esInactivo()) {
                participacion.marcarInactivo();
                participacion.setProgramaPorDefecto(null);
                participacion.setCohortePorDefecto(null);
                participacion.setUltimaModificacion(LocalDateTime.now());
                participacionDocenteRepositorio.save(participacion);
            }
        }
        for (Docente docente : docentes.values()) {
            curso.getParticipacionesDocente().add(participacionDocenteRepositorio
                    .save(new ParticipacionDocente(curso, docente, titulares.get(docente.getIdDocente()))));
        }
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
                String nombre = cursoRepositorio.findById(id).map(c -> c.getNombre()).orElse("#" + id);
                throw new IllegalArgumentException("Error! No se dio de baja ningún registro. «" + nombre + "»: "
                        + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
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
    public ParticipacionDocente validarAccesoDocente(int idCurso, Docente docente) {
        Curso curso = buscarPorId(idCurso)
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        ParticipacionDocente participacion = participacionDocenteRepositorio.findByCursoAndBajaFalse(curso).stream()
                .filter(p -> p.getDocente().getIdDocente() == docente.getIdDocente()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Error! No participás en este curso."));
        if (participacion.getProgramaDeTrabajo() == null) {
            throw new IllegalArgumentException("Error! El curso no tiene un programa vigente.");
        }
        return participacion;
    }
}
