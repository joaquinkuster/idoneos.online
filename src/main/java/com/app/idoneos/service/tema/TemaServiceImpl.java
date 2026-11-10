package com.app.idoneos.service.tema;

import java.util.Set;
import java.util.stream.Collectors;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Programa;
import com.app.idoneos.model.Tema;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.CursoRepository;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.repository.ProgramaRepository;
import com.app.idoneos.repository.TemaCronogramaRepository;
import com.app.idoneos.repository.TemaRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.service.inscripcion.InscripcionService;
import com.app.idoneos.util.DuracionUtil;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.app.idoneos.util.TextoUtil;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de TemaService. */
@Service
public class TemaServiceImpl implements TemaService {

    @Autowired
    private com.app.idoneos.service.programa.ProgramaService programaService;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    @Autowired
    private com.app.idoneos.repository.CursoRepository cursoRepository;

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private TemaCronogramaRepository temaCronogramaRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el tema por su identificador. */
    @Override
    public Optional<Tema> buscarPorId(Integer id) {
        return temaRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Tema> obtenerTodo() {
        return temaRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return temaRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de tema asociados a curso. */
    @Override
    public List<Tema> buscarPorCurso(Curso curso) {
        return temaRepository.findByCursoAndBajaFalse(curso);
    }

    /** Indica si un usuario puede ver el contenido de un tema. */
    @Override
    public boolean puedeVerContenido(Tema tema, Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return true;
        }
        Docente docente = usuario.getDocente();
        if (docente != null && docenteCursoRepository.findByCursoAndBajaFalse(tema.getCurso()).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente())) {
            return true;
        }
        Alumno alumno = usuario.getAlumno();
        return alumno != null && inscripcionService.buscarConAccesoATema(alumno, tema).isPresent();
    }

    /** Busca los temas del cronograma de un programa por título. */
    @Override
    public List<TemaCronograma> buscarConFiltros(Programa programa, String titulo) {
        String buscado = TextoUtil.normalizar(titulo);
        return programa.getCronogramaOrdenado().stream()
                .filter(c -> buscado.isEmpty() || TextoUtil.normalizar(c.getTema().getTitulo()).contains(buscado))
                .toList();
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el tema en la base de datos. */
    @Override
    public Tema guardar(Tema entidad) {
        return temaRepository.save(entidad);
    }

    /** Registra un tema del curso, sin incorporarlo todavía al cronograma de ningún programa. */
    @Override
    @Transactional
    public Tema registrar(Docente docente, Integer cursoId, String titulo, String descripcion, String contenido) {
        Curso curso = cursoRepository.findById(cursoId == null ? -1 : cursoId).filter(c -> !c.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El curso no se encuentra activo."));
        obtenerParticipacion(curso, docente);
        String tituloLimpio = validarDatos(titulo);
        validarTituloLibre(curso, tituloLimpio, null);
        Tema tema = new Tema(curso, tituloLimpio, contenidoLimpio(contenido));
        tema.setDescripcion(validarDescripcion(descripcion));
        tema = temaRepository.save(tema);
        curso.getTemas().add(tema);
        return tema;
    }

    /** Guarda de una vez los temas nuevos y los modificados de un curso. */
    @Override
    @Transactional
    public int guardarCambios(Docente docente, Integer cursoId, List<CambioDeTema> cambios) {
        if (cambios == null || cambios.isEmpty()) {
            throw new IllegalArgumentException("Error! No hay cambios para guardar.");
        }
        int cantidad = 0;
        for (CambioDeTema cambio : cambios) {
            try {
                if (cambio.id() == null) {
                    registrar(docente, cursoId, cambio.titulo(), cambio.descripcion(), cambio.contenido());
                } else {
                    modificar(cambio.id(), docente, cambio.titulo(), cambio.descripcion(), cambio.contenido());
                }
                cantidad++;
            } catch (IllegalArgumentException e) {
                // Se indica de qué tema es el error y no se guarda ninguno (la transacción se revierte)
                String nombre = cambio.titulo() == null || cambio.titulo().isBlank() ? "(sin título)" : "«" + cambio.titulo().trim() + "»";
                throw new IllegalArgumentException("Error! Tema " + nombre + ": " + e.getMessage().replaceFirst("^Error! ", ""));
            }
        }
        return cantidad;
    }

    /** Da de baja un tema del curso: lo quita del cronograma de todos sus programas y lo deja dado de baja. */
    @Override
    @Transactional
    public void darDeBajaPorCurso(Integer id, Docente docente) {
        Tema tema = temaRepository.findById(id == null ? -1 : id).filter(t -> !t.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activo."));
        obtenerParticipacion(tema.getCurso(), docente);
        // El tema no se da de baja mientras tenga pools o autoevaluaciones activos (la base de datos tampoco lo permite)
        long pools = tema.getPooles().stream().filter(pool -> !pool.getBaja()).count();
        int autoevaluaciones = tema.getAutoevaluacionesVigentes().size();
        if (pools + autoevaluaciones > 0) {
            String detalle = (pools > 0 ? MensajeErrorUtil.formatearCantidad((int) pools, "pool activo", "pools activos") : "")
                    + (pools > 0 && autoevaluaciones > 0 ? " y " : "")
                    + (autoevaluaciones > 0 ? MensajeErrorUtil.formatearCantidad(autoevaluaciones, "autoevaluación activa", "autoevaluaciones activas") : "");
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el tema", tema.getTitulo(), "tiene " + detalle,
                    "Dé de baja primero " + (pools + autoevaluaciones == 1 ? "ese registro." : "esos registros.")));
        }
        List<Programa> programas = tema.getProgramasQueLaIncluyen();
        if (tema.getCurso().tieneCohortesPublicadas()) {
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el tema", tema.getTitulo(),
                    MensajeErrorUtil.MOTIVO_CURSO_PUBLICADO, MensajeErrorUtil.COMPROMISO_PUBLICADO + " " + MensajeErrorUtil.CIERRE_DISENO));
        }
        for (Programa programa : programas) {
            TemaCronograma aQuitar = validarBaja(tema.getIdTema(), programa, docente);
            validarQueNoDesajusteLosDias(programa, List.of(aQuitar));
            validarMinimoAlQuitar(programa, List.of(aQuitar));
            quitarDelCronograma(programa, List.of(aQuitar));
        }
        if (!tema.esInactivo()) {
            tema.marcarInactivo();
            tema.setUltimaModificacion(LocalDateTime.now());
            temaRepository.save(tema);
        }
    }

    @Override
    @Transactional
    public void darDeBajaVariasPorCurso(List<Integer> ids, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un tema.");
        }
        // Todos o ninguno: si alguno no se puede dar de baja, la excepción deshace también los anteriores
        for (Integer id : ids.stream().distinct().toList()) {
            darDeBajaPorCurso(id, docente);
        }
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Tema modificar(Tema entidad) {
        return temaRepository.save(entidad);
    }

    /** Modifica un tema sin inscripciones activas asociadas. */
    @Override
    @Transactional
    public Tema modificar(Integer id, Docente docente, String titulo, String descripcion, String contenido) {
        Tema tema = temaRepository.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        obtenerParticipacion(tema.getCurso(), docente);
        if (tema.getCurso().tieneCohortesPublicadas()) {
            throw new IllegalArgumentException(MensajeErrorUtil.bloqueoPorPublicacion("el tema", tema.getTitulo(),
                    MensajeErrorUtil.MOTIVO_CURSO_PUBLICADO, MensajeErrorUtil.CIERRE_DISENO));
        }
        String tituloLimpio = validarDatos(titulo);
        validarTituloLibre(tema.getCurso(), tituloLimpio, tema);
        tema.setTitulo(tituloLimpio);
        tema.setDescripcion(validarDescripcion(descripcion));
        tema.setContenido(contenidoLimpio(contenido));
        tema.setUltimaModificacion(LocalDateTime.now());
        return temaRepository.save(tema);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Tema entidad) {
        entidad.marcarInactivo(); // Baja lógica
        temaRepository.save(entidad);
    }

    /** Quita varios temas del cronograma de un programa como parte de la modificación de todo el cronograma. */
    @Override
    @Transactional
    public void quitarVariasDelCronograma(List<Integer> ids, Integer programaId, Docente docente) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Error! Debe seleccionar al menos un tema.");
        }
        Programa programa = buscarProgramaActivo(programaId);
        // Se valida todo antes de quitar alguna
        List<TemaCronograma> aQuitar = ids.stream().distinct().map(id -> validarBaja(id, programa, docente)).toList();
        // Solo se saca el tema del programa (no se da de baja el tema). El mínimo de temas se controla sobre el cronograma que queda completo,
        // porque en la misma modificación se pueden sumar otros temas
        quitarDelCronograma(programa, aQuitar);
    }

    /** Valida que dar de baja un tema no deje desajustados los días del programa, porque deja sin asignar los suyos. */
    private void validarQueNoDesajusteLosDias(Programa programa, List<TemaCronograma> aQuitar) {
        TemaCronograma primera = aQuitar.get(0);
        int dias = aQuitar.stream().mapToInt(TemaCronograma::getDuracionDias).sum();
        throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el tema", primera.getTema().getTitulo(),
                MensajeErrorUtil.motivoDisenoIncompleto("dejaría sin asignar " + DuracionUtil.formatear(dias) + " del cronograma"),
                "Quitalo antes del programa: modificá el programa o registrá una versión nueva sin este tema."));
    }

    /** Verifica que, sin estos temas, el programa con cohortes en curso conserve el mínimo de contenido publicado. */
    private void validarMinimoAlQuitar(Programa programa, List<TemaCronograma> aQuitar) {
        java.util.Set<Integer> ids = aQuitar.stream().map(c -> c.getTema().getIdTema()).collect(java.util.stream.Collectors.toSet());
        programaService.verificarMinimoDeTemas(programa, ids).ifPresent(bloqueo -> {
            Tema primera = aQuitar.get(0).getTema();
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el tema", primera.getTitulo(),
                    MensajeErrorUtil.motivoDisenoIncompleto(bloqueo.minimo() == 1
                            ? "es el único tema que debe conservar su programa"
                            : "es uno de los " + bloqueo.minimoEnTemas() + " temas que debe conservar su programa"),
                    "Registrá otro antes de darlo de baja."));
        });
    }

    /** Verifica que el tema se pueda quitar del cronograma del programa y devuelve su lugar en el cronograma. */
    private TemaCronograma validarBaja(Integer id, Programa programa, Docente docente) {
        Tema tema = temaRepository.findById(id == null ? -1 : id).filter(u -> !u.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El tema no se encuentra activa."));
        if (docente != null) {
            obtenerParticipacion(programa.getCurso(), docente);
        }
        TemaCronograma cronograma = programa.getTemasCronograma().stream()
                .filter(c -> c.getTema().getIdTema() == tema.getIdTema()).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! El tema '" + tema.getTitulo() + "' no pertenece al cronograma de este programa."));
        if (programa.estaCongelado()) {
            // Lo que los alumnos ven no se puede deshacer: no hay un paso que indicar
            throw new IllegalArgumentException(MensajeErrorUtil.armarBajaBloqueada("el tema", tema.getTitulo(),
                    "el programa «" + programa.getNombre() + "» " + programa.getMotivoDeCongelado(),
                    MensajeErrorUtil.COMPROMISO_PUBLICADO + " " + MensajeErrorUtil.CIERRE_DISENO));
        }
        return cronograma;
    }

    /** Quita los temas del cronograma del programa. */
    private void quitarDelCronograma(Programa programa, List<TemaCronograma> aQuitar) {
        LocalDateTime ahora = LocalDateTime.now();
        for (TemaCronograma cronograma : aQuitar) {
            Tema tema = cronograma.getTema();
            programa.getTemasCronograma().remove(cronograma);
            tema.getTemasCronograma().remove(cronograma);
            temaCronogramaRepository.delete(cronograma);
        }
        temaCronogramaRepository.flush();

        // Las restantes se renumeran sin huecos, de a una para no repetir un número de orden
        int orden = 1;
        for (TemaCronograma restante : programa.getTemasCronograma().stream()
                .sorted(Comparator.comparingInt(TemaCronograma::getNumeroOrden)).toList()) {
            if (restante.getNumeroOrden() != orden) {
                restante.setNumeroOrden(orden);
                restante.setUltimaModificacion(ahora);
                temaCronogramaRepository.saveAndFlush(restante);
            }
            orden++;
        }
    }

    private Programa buscarProgramaActivo(Integer programaId) {
        return programaRepository.findById(programaId == null ? -1 : programaId).filter(p -> !p.esInactivo())
                .orElseThrow(() -> new IllegalArgumentException("Error! El programa no se encuentra activo."));
    }

    /** Obtiene la participación del docente en el curso (como titular o ayudante). */
    private void obtenerParticipacion(Curso curso, Docente docente) {
        if (docente == null) {
            throw new IllegalArgumentException("Error! El usuario no tiene el rol de docente.");
        }
        boolean participa = docenteCursoRepository.findByCursoAndBajaFalse(curso).stream()
                .anyMatch(p -> p.getDocente().getIdDocente() == docente.getIdDocente());
        if (!participa) {
            throw new IllegalArgumentException("Error! No participás en este curso.");
        }
    }

    /** Valida el título y el contenido, y devuelve el título sin espacios sobrantes. */
    /** El contenido de un tema es opcional: se puede completar después de registrarlo. */
    private String contenidoLimpio(String contenido) {
        return contenido == null || contenido.isBlank() ? null : contenido.trim();
    }

    private String validarDatos(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! Debe completar el título del tema.");
        }
        if (titulo.trim().length() > Tema.LONGITUD_TITULO) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo", MensajeErrorUtil.armarDemasiadoLargo("El título", Tema.LONGITUD_TITULO, "caracteres"));
        }
        return titulo.trim();
    }

    private String validarDescripcion(String descripcion) {
        String limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (limpia != null && limpia.length() > Tema.LONGITUD_DESCRIPCION) {
            throw RespuestaFormularioUtil.armarCampoInvalido("descripcion", MensajeErrorUtil.armarDemasiadoLargo("La descripción", Tema.LONGITUD_DESCRIPCION, "caracteres"));
        }
        return limpia;
    }

    /** Verifica que el curso no tenga otro tema con el mismo título (el título es único por curso). */
    private void validarTituloLibre(Curso curso, String titulo, Tema propia) {
        boolean repetido = temaRepository.findAll().stream()
                .anyMatch(u -> u.getCurso().getIdCurso() == curso.getIdCurso() && u.getTitulo().equalsIgnoreCase(titulo)
                        && (propia == null || u.getIdTema() != propia.getIdTema()));
        if (repetido) {
            throw RespuestaFormularioUtil.armarCampoInvalido("titulo", "Error! Ya existe un tema con ese título en el curso.");
        }
    }
}
