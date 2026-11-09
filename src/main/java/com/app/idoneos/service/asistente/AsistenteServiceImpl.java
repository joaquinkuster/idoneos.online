package com.app.idoneos.service.asistente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Autoevaluacion;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.TemaCronograma;
import com.app.idoneos.model.Usuario;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.service.curso.CursoService;
import com.app.idoneos.util.MensajeErrorUtil;

@Service
public class AsistenteServiceImpl implements AsistenteService {

    /** Atajo que no lleva a otra pantalla: abre la ventana de intereses del alumno (la resuelve el script del asistente). */
    static final String ATAJO_INTERESES = "#intereses";

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CursoService cursoService;

    /** Cuándo cierra o abre, con los días que faltan si está cerca: «Cierra el 09/11» (con el año si no es el actual, 20 caracteres como máximo). Sin fecha, {@code null}. */
    private static String fecha(String prefijo, LocalDateTime cuando, LocalDate hoy) {
        if (cuando == null) {
            return null;
        }
        long dias = ChronoUnit.DAYS.between(hoy, cuando.toLocalDate());
        String dia = cuando.toLocalDate().getYear() == hoy.getYear()
                ? String.format("%02d/%02d", cuando.getDayOfMonth(), cuando.getMonthValue())
                : String.format("%02d/%02d/%d", cuando.getDayOfMonth(), cuando.getMonthValue(), cuando.getYear());
        if (dias < 0) {
            return prefijo.replace("Cierra", "Cerró").replace("Abre", "Abrió") + " " + dia;
        }
        return prefijo + " " + dia;
    }

    /** La urgencia según los días que faltan: «alta» (rojo), «media» (dorado) o {@code null}. */
    private static String urgencia(LocalDateTime cuando, LocalDate hoy) {
        if (cuando == null) {
            return null;
        }
        long dias = ChronoUnit.DAYS.between(hoy, cuando.toLocalDate());
        return dias <= DIAS_URGENTE ? "alta" : dias <= DIAS_PRONTO ? "media" : null;
    }

    private static Pendiente pendiente(String titulo, String curso, String accion, String prefijo, String pronto, LocalDateTime cuando,
            String url) {
        LocalDate hoy = LocalDate.now();
        String urgencia = urgencia(cuando, hoy);
        return new Pendiente(titulo, curso, accion, fecha(prefijo, cuando, hoy), urgencia == null ? null : pronto, urgencia, url);
    }

    @Override
    @Transactional(readOnly = true)
    public Respuesta armar(Usuario usuario) {
        if (usuario.esAdministradorActivo()) {
            return paraAdministrador(usuario);
        }
        if (usuario.esDocenteActivo()) {
            return paraDocente(usuario);
        }
        if (usuario.esAlumnoActivo()) {
            return paraAlumno(usuario);
        }
        return new Respuesta(usuario.getNombre(), "No tenés pendientes.", null, "Pendientes", 0, List.of(), List.of());
    }

    // ---------------------------------------------------------------- Alumno

    private record AutoevaluacionPendiente(Autoevaluacion autoevaluacion, Inscripcion inscripcion, boolean atrasada) {
    }

    private Respuesta paraAlumno(Usuario usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        List<AutoevaluacionPendiente> pendientes = new ArrayList<>();
        for (Inscripcion inscripcion : inscripcionRepository.findByAlumnoAndBajaFalse(usuario.getAlumno())) {
            Cohorte cohorte = inscripcion.getCohorte();
            for (Autoevaluacion autoevaluacion : inscripcion.getAutoevaluacionesPendientes()) {
                // Está atrasada si el cronograma ya daba por terminado su tema
                boolean atrasada = cohorte.getPrograma().getCronogramaOrdenado().stream()
                        .anyMatch(c -> c.getTema().getIdTema() == autoevaluacion.getTema().getIdTema()
                                && c.getDiaHasta() < cohorte.getDiasTranscurridos());
                pendientes.add(new AutoevaluacionPendiente(autoevaluacion, inscripcion, atrasada));
            }
        }
        // Primero las que cierran antes (las atrasadas ya quedan arriba por su fecha; las que no cierran, al final)
        pendientes.sort(Comparator.comparing(
                (AutoevaluacionPendiente p) -> p.autoevaluacion().getFechaCierre() == null ? LocalDateTime.MAX : p.autoevaluacion().getFechaCierre()));

        long atrasadas = pendientes.stream().filter(AutoevaluacionPendiente::atrasada).count();
        String resumen = pendientes.isEmpty()
                ? "Estás al día: no tenés autoevaluaciones pendientes"
                : MensajeErrorUtil.formatearCantidad(pendientes.size(), "autoevaluación pendiente", "autoevaluaciones pendientes")
                        + (atrasadas == 0 ? "" : atrasadas == 1 ? " · 1 atrasada" : " · " + atrasadas + " atrasadas");
        List<Pendiente> items = pendientes.stream().limit(MAXIMO_PENDIENTES).map(p -> pendiente(
                p.autoevaluacion().getNombre(),
                p.inscripcion().getCurso().getNombre(),
                "Realizar intento", "Cierra el", "Cierra pronto", p.autoevaluacion().getFechaCierre(),
                "/autoevaluacion/ver/" + p.autoevaluacion().getIdAutoevaluacion())).toList();
        return new Respuesta(usuario.getNombre(), resumen, null, "Autoevaluaciones pendientes", pendientes.size(), items,
                List.of(new Atajo("Mis cursos", "/inscripcion/buscar"), new Atajo("Mis pagos", "/pago/buscar"),
                        new Atajo("Mis certificados", "/certificado/buscar"), new Atajo("Mis intereses", ATAJO_INTERESES)));
    }

    // ---------------------------------------------------------------- Docente

    private record CohortePendiente(Curso curso, Cohorte cohorte, CursoService.EstadoDiseno estado, long dias) {
    }

    private Respuesta paraDocente(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        List<CohortePendiente> pendientes = new ArrayList<>();
        for (Curso curso : cursoService.buscarConFiltros(null, null, null, null, null, null, usuario.getDocente())) {
            if (curso.getBaja()) {
                continue;
            }
            for (Cohorte cohorte : curso.getCohortes()) {
                if (cohorte.getBaja() || !cohorte.esPendiente()) {
                    continue;
                }
                long dias = ChronoUnit.DAYS.between(hoy, cohorte.getFechaInicioInscripcion().toLocalDate());
                pendientes.add(new CohortePendiente(curso, cohorte, cursoService.calcularEstadoDiseno(curso, cohorte), dias));
            }
        }
        pendientes.sort(Comparator.comparingLong(CohortePendiente::dias));

        String resumen = pendientes.isEmpty()
                ? "Estás al día: no tenés cohortes pendientes de contenido"
                : MensajeErrorUtil.formatearCantidad(pendientes.size(), "cohorte pendiente de contenido", "cohortes pendientes de contenido");
        String aviso = pendientes.isEmpty() ? null
                : "Si no completás el contenido antes de que abra la inscripción, la cohorte se cancela.";
        // Cada cohorte lleva al paso del diseño en que está, como si el docente hubiera hecho clic en el botón de ese paso
        List<Pendiente> items = pendientes.stream().limit(MAXIMO_PENDIENTES).map(p -> {
            String[] paso = pasoDelDiseno(p);
            return pendiente(p.cohorte().getMesAnio(), p.curso().getNombre(), paso[1], "Abre el", "Abre pronto",
                    p.cohorte().getFechaInicioInscripcion(), paso[2]);
        }).toList();
        return new Respuesta(usuario.getNombre(), resumen, aviso, "Cohortes pendientes de contenido", pendientes.size(), items,
                List.of(new Atajo("Mis cursos", "/curso/buscar"), new Atajo("Biblioteca digital", "/biblioteca/buscar")));
    }

    /**
     * Lo que le falta a la cohorte en el paso del diseño en que está, la acción de ese paso (la misma que el botón del diseño del curso y que
     * la alerta guía) y a dónde lleva.
     */
    private String[] pasoDelDiseno(CohortePendiente p) {
        CursoService.EstadoDiseno estado = p.estado();
        String curso = "?cursoId=" + p.curso().getIdCurso();
        return switch (estado.getPasoActual()) {
            case 1 -> new String[] {"Faltan datos del curso", "Completar datos", "/curso/acceder/" + p.curso().getIdCurso() + "?completar=datos"};
            case 2 -> estado.cantidadTemas() < estado.minimoTemas()
                    ? new String[] {"Faltan temas (" + estado.cantidadTemas() + " de " + estado.minimoTemas() + ")", "Registrar tema",
                            "/tema/buscar" + curso + "&nuevo="}
                    : new String[] {estado.getTemasQueFaltan() == 1 ? "Falta el contenido de 1 tema"
                            : "Falta el contenido de " + Math.max(1, estado.getTemasQueFaltan()) + " temas",
                            "Completar contenido", "/curso/acceder/" + p.curso().getIdCurso() + "?completar=contenido"};
            case 3 -> {
                boolean puedeAsignar = p.cohorte().getCantidadInscriptos() == 0 && !p.curso().getProgramasActivos().isEmpty();
                yield puedeAsignar
                        ? new String[] {"Falta asignar el programa", "Asignar programa",
                                "/cohorte/buscar" + curso + "&asignar=" + p.cohorte().getIdCohorte() + "&volver=curso"}
                        : new String[] {"Falta registrar el programa", "Registrar programa", "/programa/buscar" + curso + "&nuevo="};
            }
            default -> new String[] {"Diseño completo", "Ver curso", "/curso/acceder/" + p.curso().getIdCurso()};
        };
    }

    // ---------------------------------------------------------------- Administrador

    private Respuesta paraAdministrador(Usuario usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Inscripcion> pendientes = inscripcionRepository.findByBajaFalse().stream()
                .filter(Inscripcion::requierePago)
                .sorted(Comparator.comparing((Inscripcion i) -> i.getCohorte().getFechaFinInscripcion())).collect(Collectors.toList());
        String resumen = pendientes.isEmpty()
                ? "Estás al día: no hay pagos pendientes de registrar"
                : MensajeErrorUtil.formatearCantidad(pendientes.size(), "pago pendiente de registrar", "pagos pendientes de registrar");
        List<Pendiente> items = pendientes.stream().limit(MAXIMO_PENDIENTES).map(i -> pendiente(
                i.getAlumno().getUsuario().getNombreCompleto(),
                i.getCurso().getNombre(),
                "Registrar pago", "Cierra el", "Cierra pronto", i.getCohorte().getFechaFinInscripcion(),
                "/pago/buscar?registrarPara=" + i.getIdInscripcion())).toList();
        return new Respuesta(usuario.getNombre(), resumen, null, "Pagos pendientes", pendientes.size(), items,
                List.of(new Atajo("Pagos", "/pago/buscar"), new Atajo("Inscripciones", "/inscripcion/buscar"),
                        new Atajo("Cursos", "/curso/buscar")));
    }
}
