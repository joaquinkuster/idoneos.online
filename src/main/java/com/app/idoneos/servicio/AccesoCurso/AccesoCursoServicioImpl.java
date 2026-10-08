package com.app.idoneos.servicio.AccesoCurso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.modelo.Alumno;
import com.app.idoneos.modelo.Autoevaluacion;
import com.app.idoneos.modelo.ClaseEnVivo;
import com.app.idoneos.modelo.Cohorte;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.IntentoAutoevaluacion;
import com.app.idoneos.modelo.Material;
import com.app.idoneos.modelo.Progreso;
import com.app.idoneos.modelo.TerminoGlosario;
import com.app.idoneos.modelo.Unidad;
import com.app.idoneos.modelo.UnidadCronograma;
import com.app.idoneos.repositorio.AutoevaluacionRepositorio;
import com.app.idoneos.repositorio.ClaseEnVivoRepositorio;
import com.app.idoneos.repositorio.ConsultaForoRepositorio;
import com.app.idoneos.repositorio.InscripcionRepositorio;
import com.app.idoneos.repositorio.IntentoAutoevaluacionRepositorio;
import com.app.idoneos.repositorio.MaterialRepositorio;
import com.app.idoneos.repositorio.ProgresoRepositorio;
import com.app.idoneos.repositorio.TerminoGlosarioRepositorio;
import com.app.idoneos.repositorio.UnidadCronogramaRepositorio;

/**
 * Implementación del servicio del caso de uso CU-27 Acceder curso. Sólo lee datos: no modifica el avance del alumno.
 */
@Service
@Transactional(readOnly = true)
public class AccesoCursoServicioImpl implements AccesoCursoServicio {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private InscripcionRepositorio inscripcionRepositorio;

    @Autowired
    private UnidadCronogramaRepositorio unidadCronogramaRepositorio;

    @Autowired
    private ProgresoRepositorio progresoRepositorio;

    @Autowired
    private MaterialRepositorio materialRepositorio;

    @Autowired
    private TerminoGlosarioRepositorio terminoGlosarioRepositorio;

    @Autowired
    private AutoevaluacionRepositorio autoevaluacionRepositorio;

    @Autowired
    private IntentoAutoevaluacionRepositorio intentoAutoevaluacionRepositorio;

    @Autowired
    private ConsultaForoRepositorio consultaForoRepositorio;

    @Autowired
    private ClaseEnVivoRepositorio claseEnVivoRepositorio;

    /**
     * Verifica que el alumno pueda acceder al curso de la inscripción.
     *
     * @param idInscripcion El identificador de la inscripción.
     * @param alumno        El alumno que solicita el acceso.
     * @return La inscripción a la que puede acceder.
     * @throws IllegalArgumentException Si el alumno no puede acceder, con el motivo.
     */
    @Override
    public Inscripcion validarAcceso(int idInscripcion, Alumno alumno) {
        Inscripcion inscripcion = inscripcionRepositorio.findById(idInscripcion)
                .filter(i -> !i.esInactivo() && i.getAlumno().getIdAlumno() == alumno.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! No tenés una inscripción vigente a este curso."));
        Cohorte cohorte = inscripcion.getCohorte();
        if (Boolean.FALSE.equals(inscripcion.getHabilitado()) || cohorte.esInactivo()) {
            throw new IllegalArgumentException("Error! Tu inscripción a este curso no está habilitada.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(inscripcion.getFechaVencimientoAcceso())) {
            throw new IllegalArgumentException("Error! Tu acceso a este curso venció el "
                    + inscripcion.getFechaVencimientoAcceso().format(FORMATO_FECHA) + ".");
        }
        if (cohorte.getFechaInicioDictado() != null && ahora.isBefore(cohorte.getFechaInicioDictado())) {
            throw new IllegalArgumentException("Error! El dictado de tu cohorte comienza el "
                    + cohorte.getFechaInicioDictado().format(FORMATO_FECHA) + ".");
        }
        return inscripcion;
    }

    /**
     * Lista las unidades del programa de la cohorte del alumno con su estado de avance y su contenido publicado.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Las unidades en orden de cronograma.
     */
    @Override
    public List<UnidadAcceso> buscarUnidades(Inscripcion inscripcion) {
        List<UnidadCronograma> cronograma = buscarCronogramaOrdenado(inscripcion);
        Set<Integer> completadas = unidadesCompletadas(inscripcion);
        List<UnidadAcceso> unidades = new ArrayList<>();
        boolean anteriorCompletada = true; // la primera unidad siempre está habilitada
        boolean hayUnidadEnCurso = false;
        for (UnidadCronograma elemento : cronograma) {
            Unidad unidad = elemento.getUnidad();
            boolean completada = completadas.contains(unidad.getIdUnidad());
            boolean habilitada = anteriorCompletada;
            boolean enCurso = habilitada && !completada && !hayUnidadEnCurso;
            hayUnidadEnCurso = hayUnidadEnCurso || enCurso;
            unidades.add(habilitada
                    ? new UnidadAcceso(elemento, completada, true, enCurso, buscarMateriales(unidad),
                            buscarGlosario(unidad), buscarAutoevaluaciones(unidad, inscripcion),
                            consultaForoRepositorio.findByUnidadAndBajaFalse(unidad).size())
                    : new UnidadAcceso(elemento, false, false, false, List.of(), List.of(), List.of(), 0));
            anteriorCompletada = completada;
        }
        return unidades;
    }

    /**
     * Arma el cronograma del programa de la cohorte del alumno con la semana esperada y el indicador de atraso.
     *
     * @param inscripcion La inscripción del alumno.
     * @return El cronograma con el avance esperado.
     */
    @Override
    public CronogramaAcceso buscarCronograma(Inscripcion inscripcion) {
        List<UnidadCronograma> cronograma = buscarCronogramaOrdenado(inscripcion);
        Set<Integer> completadas = unidadesCompletadas(inscripcion);
        Cohorte cohorte = inscripcion.getCohorte();
        boolean desdeDictado = cohorte.getFechaInicioDictado() != null;
        LocalDateTime base = desdeDictado ? cohorte.getFechaInicioDictado() : cohorte.getFechaInicioInscripcion();
        int semanasTotales = cronograma.stream().mapToInt(UnidadCronograma::getSemanasDuracion).sum();
        int semanaReal = (int) Math.max(0, ChronoUnit.DAYS.between(base, LocalDateTime.now())) / 7 + 1;

        // Unidades que ya deberían estar terminadas hoy y última unidad que el alumno completó
        int acumuladas = 0;
        int esperadasTerminadas = 0;
        int ultimaCompletada = 0;
        List<int[]> semanas = new ArrayList<>();
        for (UnidadCronograma elemento : cronograma) {
            int desde = acumuladas + 1;
            acumuladas += elemento.getSemanasDuracion();
            semanas.add(new int[] { desde, acumuladas });
            if (acumuladas < semanaReal) {
                esperadasTerminadas = elemento.getNumeroOrden();
            }
            if (completadas.contains(elemento.getUnidad().getIdUnidad())) {
                ultimaCompletada = Math.max(ultimaCompletada, elemento.getNumeroOrden());
            }
        }

        List<CronogramaAcceso.FilaCronograma> filas = new ArrayList<>();
        for (int i = 0; i < cronograma.size(); i++) {
            UnidadCronograma elemento = cronograma.get(i);
            boolean completada = completadas.contains(elemento.getUnidad().getIdUnidad());
            int[] rango = semanas.get(i);
            String estado;
            if (completada) {
                estado = "Completada";
            } else if (rango[1] < semanaReal) {
                estado = "Atrasada";
            } else {
                estado = rango[0] <= semanaReal ? "Semana actual" : "Próxima";
            }
            filas.add(new CronogramaAcceso.FilaCronograma(elemento, rango[0], rango[1], completada, estado));
        }

        boolean atrasado = ultimaCompletada < esperadasTerminadas;
        String mensaje = null;
        if (atrasado) {
            mensaje = "Vas por detrás de lo esperado: a esta altura (semana " + Math.min(semanaReal, semanasTotales)
                    + ") deberías haber completado hasta la unidad " + esperadasTerminadas + " y tu última unidad "
                    + (ultimaCompletada == 0 ? "completada es: ninguna." : "completada es la " + ultimaCompletada + ".");
        }
        return new CronogramaAcceso(filas, base, desdeDictado, semanasTotales,
                Math.max(1, Math.min(semanaReal, semanasTotales)), atrasado, mensaje);
    }

    /**
     * Lista las clases en vivo de la cohorte del alumno que no están ocultas.
     *
     * @param inscripcion La inscripción del alumno.
     * @return Las clases en vivo, de la más próxima a la más lejana.
     */
    @Override
    public List<ClaseEnVivo> buscarClasesEnVivo(Inscripcion inscripcion) {
        return claseEnVivoRepositorio.findByCohorteAndBajaFalse(inscripcion.getCohorte()).stream()
                .filter(clase -> !Boolean.TRUE.equals(clase.getOculto()))
                .sorted(Comparator.comparing(ClaseEnVivo::getFechaHora))
                .toList();
    }

    private List<UnidadCronograma> buscarCronogramaOrdenado(Inscripcion inscripcion) {
        return unidadCronogramaRepositorio.findByProgramaAndBajaFalse(inscripcion.getCohorte().getPrograma()).stream()
                .filter(elemento -> !elemento.getUnidad().esInactivo())
                .sorted(Comparator.comparingInt(UnidadCronograma::getNumeroOrden))
                .toList();
    }

    private Set<Integer> unidadesCompletadas(Inscripcion inscripcion) {
        return progresoRepositorio.findByInscripcion(inscripcion).stream()
                .filter(Progreso::getCompletada)
                .map(progreso -> progreso.getUnidad().getIdUnidad())
                .collect(Collectors.toSet());
    }

    private List<Material> buscarMateriales(Unidad unidad) {
        return materialRepositorio.findByUnidadAndBajaFalse(unidad).stream()
                .filter(material -> !Boolean.TRUE.equals(material.getOculto()))
                .sorted(Comparator.comparingInt(Material::getIdMaterial))
                .toList();
    }

    private List<TerminoGlosario> buscarGlosario(Unidad unidad) {
        return terminoGlosarioRepositorio.findByUnidadAndBajaFalse(unidad).stream()
                .sorted(Comparator.comparing(TerminoGlosario::getTermino,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<AutoevaluacionAcceso> buscarAutoevaluaciones(Unidad unidad, Inscripcion inscripcion) {
        List<IntentoAutoevaluacion> intentosDelAlumno = intentoAutoevaluacionRepositorio
                .findByInscripcionAndBajaFalse(inscripcion);
        return autoevaluacionRepositorio.findByUnidadAndBajaFalse(unidad).stream()
                .filter(autoevaluacion -> !Boolean.TRUE.equals(autoevaluacion.getOculto()))
                .sorted(Comparator.comparingInt(Autoevaluacion::getIdAutoevaluacion))
                .map(autoevaluacion -> new AutoevaluacionAcceso(autoevaluacion, intentosDelAlumno.stream()
                        .filter(intento -> intento.getAutoevaluacion().getIdAutoevaluacion() == autoevaluacion
                                .getIdAutoevaluacion())
                        .sorted(Comparator.comparingInt(IntentoAutoevaluacion::getIdIntentoAutoevaluacion))
                        .toList()))
                .toList();
    }
}
