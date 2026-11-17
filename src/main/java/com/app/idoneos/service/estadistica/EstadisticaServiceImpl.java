package com.app.idoneos.service.estadistica;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.EstadoAvance;
import com.app.idoneos.model.EstadoInscripcion;
import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.model.Pago;
import com.app.idoneos.model.Programa;
import com.app.idoneos.repository.InscripcionRepository;
import com.app.idoneos.repository.PagoRepository;

/** Implementación de EstadisticaService. */
@Service
public class EstadisticaServiceImpl implements EstadisticaService {

    /** Hasta cuántos días de rango se grafica un punto por día; si es más largo, un punto por mes. */
    private static final int DIAS_MAXIMOS_POR_DIA = 62;

    /** Cantidad máxima de años que abarca un informe. */
    private static final int ANIOS_MAXIMOS = 5;

    /** Cantidad de cursos del resto que se comparan con el seleccionado. */
    private static final int CURSOS_DE_COMPARACION = 4;

    /** Cantidad máxima de líneas (programas) del gráfico de evolución. */
    private static final int LINEAS_MAXIMAS = 5;

    /** Nombre con el que se agrupan las inscripciones y los pagos de cohortes sin programa. */
    private static final String SIN_PROGRAMA = "Sin programa";

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    // ----------------------------------------------------------------------
    // Informe de alumnos
    // ----------------------------------------------------------------------

    /** Arma los datos del informe de alumnos de un curso. */
    @Override
    @Transactional(readOnly = true)
    public InformeAlumnos informeAlumnos(Curso curso, LocalDate desde, LocalDate hasta) {
        validar(curso, desde, hasta);
        List<Inscripcion> todas = inscripcionRepository.findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(desde.atStartOfDay(),
                hasta.plusDays(1).atStartOfDay());
        List<Inscripcion> delCurso = todas.stream().filter(i -> i.getCohorte().getCurso().getIdCurso() == curso.getIdCurso()).toList();

        int completadas = 0;
        int vigentes = 0;
        int bajas = 0;
        for (Inscripcion i : delCurso) {
            if (i.getCertificadoVigente() != null || (i.getEstadoAvance() != null && EstadoAvance.FINALIZADO.equals(i.getEstadoAvance().getNombre()))) {
                completadas++;
            }
            String estado = i.getNombreEstado();
            if (EstadoInscripcion.HABILITADA.equals(estado)) {
                vigentes++;
            }
            if (i.estaDadaDeBaja() || i.estaCancelada()) {
                bajas++;
            }
        }

        Map<Integer, double[]> porCurso = new LinkedHashMap<>();
        Map<Integer, String> nombres = new LinkedHashMap<>();
        for (Inscripcion i : todas) {
            Curso c = i.getCohorte().getCurso();
            porCurso.computeIfAbsent(c.getIdCurso(), k -> new double[1])[0]++;
            nombres.put(c.getIdCurso(), c.getNombre());
        }
        List<Barra> barrasCursos = compararCursos(curso, porCurso, nombres);

        Map<String, List<LocalDate>> porProgramaYFecha = new LinkedHashMap<>();
        Map<String, double[]> porPrograma = new LinkedHashMap<>();
        for (Inscripcion i : delCurso) {
            String programa = nombrePrograma(i);
            porProgramaYFecha.computeIfAbsent(programa, k -> new ArrayList<>()).add(i.getFechaCreacion().toLocalDate());
            porPrograma.computeIfAbsent(programa, k -> new double[1])[0]++;
        }
        Evolucion evolucion = evolucion(desde, hasta, porProgramaYFecha, null);
        return new InformeAlumnos(curso, desde, hasta, nombreVigente(curso), delCurso.size(), completadas, vigentes, bajas, barrasCursos, evolucion,
                barrasDe(porPrograma, null));
    }

    // ----------------------------------------------------------------------
    // Informe de ingresos
    // ----------------------------------------------------------------------

    /** Arma los datos del informe de ingresos de un curso. */
    @Override
    @Transactional(readOnly = true)
    public InformeIngresos informeIngresos(Curso curso, LocalDate desde, LocalDate hasta) {
        validar(curso, desde, hasta);
        List<Pago> todos = pagoRepository.findByEstadoPagoNombreAndFechaAprobacionGreaterThanEqualAndFechaAprobacionLessThan(EstadoPago.APROBADO,
                desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());

        Map<Integer, double[]> porCurso = new LinkedHashMap<>();
        Map<Integer, String> nombres = new LinkedHashMap<>();
        Map<String, double[]> porCategoria = new LinkedHashMap<>();
        for (Pago p : todos) {
            Curso c = p.getInscripcion().getCohorte().getCurso();
            porCurso.computeIfAbsent(c.getIdCurso(), k -> new double[1])[0] += p.getMonto();
            nombres.put(c.getIdCurso(), c.getNombre());
            porCategoria.computeIfAbsent(c.getCategoria().getNombre(), k -> new double[1])[0] += p.getMonto();
        }

        double neto = 0;
        double bruto = 0;
        int cantidad = 0;
        Map<String, List<LocalDate>> fechasPorPrograma = new LinkedHashMap<>();
        Map<String, List<Double>> montosPorPrograma = new LinkedHashMap<>();
        Map<String, double[]> porPrograma = new LinkedHashMap<>();
        for (Pago p : todos) {
            Inscripcion inscripcion = p.getInscripcion();
            if (inscripcion.getCohorte().getCurso().getIdCurso() != curso.getIdCurso()) {
                continue;
            }
            cantidad++;
            neto += p.getMonto();
            bruto += Math.max(p.getMonto(), curso.getPrecio());
            String programa = nombrePrograma(inscripcion);
            fechasPorPrograma.computeIfAbsent(programa, k -> new ArrayList<>()).add(p.getFechaAprobacion().toLocalDate());
            montosPorPrograma.computeIfAbsent(programa, k -> new ArrayList<>()).add((double) p.getMonto());
            porPrograma.computeIfAbsent(programa, k -> new double[1])[0] += p.getMonto();
        }
        Evolucion evolucion = evolucion(desde, hasta, fechasPorPrograma, montosPorPrograma);
        return new InformeIngresos(curso, desde, hasta, nombreVigente(curso), neto, bruto, Math.max(0, bruto - neto), cantidad,
                compararCursos(curso, porCurso, nombres), evolucion, barrasDe(porCategoria, null), barrasDe(porPrograma, null));
    }

    // ----------------------------------------------------------------------
    // Tablero
    // ----------------------------------------------------------------------

    /** Calcula los indicadores del tablero a hoy. */
    @Override
    @Transactional(readOnly = true)
    public Tablero tablero() {
        List<Inscripcion> vigentes = inscripcionRepository.findByBajaFalseAndEstadoInscripcionNombre(EstadoInscripcion.HABILITADA);
        Set<Integer> alumnos = new HashSet<>();
        Set<Integer> cursos = new HashSet<>();
        for (Inscripcion i : vigentes) {
            alumnos.add(i.getAlumno().getIdAlumno());
            cursos.add(i.getCohorte().getCurso().getIdCurso());
        }

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        double mes = sumarPagos(inicioMes.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
        double mesAnterior = sumarPagos(inicioMes.minusMonths(1).atStartOfDay(), inicioMes.atStartOfDay());

        // Inscripciones de los últimos 30 días (hoy incluido)
        LocalDate primerDia = hoy.minusDays(29);
        List<Inscripcion> recientes = inscripcionRepository.findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(primerDia.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay());
        int[] porDia = new int[30];
        for (Inscripcion i : recientes) {
            porDia[(int) ChronoUnit.DAYS.between(primerDia, i.getFechaCreacion().toLocalDate())]++;
        }
        List<String> etiquetas = new ArrayList<>();
        int suma = 0;
        int pico = 0;
        LocalDate diaPico = null;
        for (int d = 0; d < 30; d++) {
            LocalDate dia = primerDia.plusDays(d);
            etiquetas.add(String.format("%02d/%02d", dia.getDayOfMonth(), dia.getMonthValue()));
            suma += porDia[d];
            if (porDia[d] > pico) {
                pico = porDia[d];
                diaPico = dia;
            }
        }

        // Ranking de cursos por cantidad de inscriptos
        Map<Integer, double[]> porCurso = new LinkedHashMap<>();
        Map<Integer, String> nombres = new LinkedHashMap<>();
        for (Inscripcion i : inscripcionRepository.findByBajaFalse()) {
            Curso c = i.getCohorte().getCurso();
            porCurso.computeIfAbsent(c.getIdCurso(), k -> new double[1])[0]++;
            nombres.put(c.getIdCurso(), c.getNombre());
        }
        List<Barra> top5 = porCurso.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0])).limit(5)
                .map(e -> new Barra(nombres.get(e.getKey()), e.getValue()[0], false)).toList();

        return new Tablero(alumnos.size(), vigentes.size(), cursos.size(), mes, mesAnterior, etiquetas, porDia, suma / 30.0, pico, diaPico, top5);
    }

    // ----------------------------------------------------------------------
    // Auxiliares
    // ----------------------------------------------------------------------

    private double sumarPagos(LocalDateTime desde, LocalDateTime hasta) {
        return pagoRepository.findByEstadoPagoNombreAndFechaAprobacionGreaterThanEqualAndFechaAprobacionLessThan(EstadoPago.APROBADO, desde, hasta)
                .stream().mapToDouble(Pago::getMonto).sum();
    }

    private static void validar(Curso curso, LocalDate desde, LocalDate hasta) {
        if (curso == null) {
            throw new IllegalArgumentException("Error! Elegí el curso sobre el que querés informar.");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Error! Indicá el rango de fechas del informe.");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("Error! La fecha hasta no puede ser anterior a la fecha desde.");
        }
        if (desde.plusYears(ANIOS_MAXIMOS).isBefore(hasta)) {
            throw new IllegalArgumentException("Error! El rango del informe no puede superar los " + ANIOS_MAXIMOS + " años.");
        }
    }

    private static String nombrePrograma(Inscripcion inscripcion) {
        Programa programa = inscripcion.getCohorte().getPrograma();
        return programa == null ? SIN_PROGRAMA : programa.getNombre();
    }

    private static String nombreVigente(Curso curso) {
        Programa vigente = curso.getProgramaVigente();
        return vigente == null ? null : vigente.getNombre();
    }

    /** Barras con el curso seleccionado y los que más valor tienen del resto, de mayor a menor. */
    private static List<Barra> compararCursos(Curso seleccionado, Map<Integer, double[]> valores, Map<Integer, String> nombres) {
        List<Barra> barras = new ArrayList<>();
        barras.add(new Barra(seleccionado.getNombre(), valores.getOrDefault(seleccionado.getIdCurso(), new double[1])[0], true));
        valores.entrySet().stream().filter(e -> e.getKey() != seleccionado.getIdCurso())
                .sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0])).limit(CURSOS_DE_COMPARACION)
                .forEach(e -> barras.add(new Barra(nombres.get(e.getKey()), e.getValue()[0], false)));
        barras.sort(Comparator.comparingDouble(Barra::valor).reversed());
        return barras;
    }

    private static List<Barra> barrasDe(Map<String, double[]> valores, String seleccionada) {
        return valores.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue()[0], a.getValue()[0]))
                .map(e -> new Barra(e.getKey(), e.getValue()[0], e.getKey().equals(seleccionada))).toList();
    }

    /**
     * Arma la evolución con una línea por programa. Hasta {@value #DIAS_MAXIMOS_POR_DIA} días de rango hay un punto por día; si no, uno por mes.
     *
     * @param fechas fechas de los hechos de cada programa
     * @param montos monto de cada hecho (en el mismo orden que las fechas), o nulo para contar cada hecho como uno
     */
    private static Evolucion evolucion(LocalDate desde, LocalDate hasta, Map<String, List<LocalDate>> fechas, Map<String, List<Double>> montos) {
        boolean porDia = ChronoUnit.DAYS.between(desde, hasta) + 1 <= DIAS_MAXIMOS_POR_DIA;
        List<String> etiquetas = new ArrayList<>();
        boolean variosMeses = desde.getMonthValue() != hasta.getMonthValue() || desde.getYear() != hasta.getYear();
        LocalDate cursor = porDia ? desde : desde.withDayOfMonth(1);
        LocalDate ultimo = porDia ? hasta : hasta.withDayOfMonth(1);
        while (!cursor.isAfter(ultimo)) {
            etiquetas.add(porDia ? (variosMeses ? String.format("%02d/%02d", cursor.getDayOfMonth(), cursor.getMonthValue())
                    : "Día " + String.format("%02d", cursor.getDayOfMonth())) : String.format("%02d/%d", cursor.getMonthValue(), cursor.getYear()));
            cursor = porDia ? cursor.plusDays(1) : cursor.plusMonths(1);
        }
        List<Serie> series = new ArrayList<>();
        for (Map.Entry<String, List<LocalDate>> e : fechas.entrySet()) {
            double[] valores = new double[etiquetas.size()];
            for (int k = 0; k < e.getValue().size(); k++) {
                LocalDate f = e.getValue().get(k);
                int indice = porDia ? (int) ChronoUnit.DAYS.between(desde, f) : (int) ChronoUnit.MONTHS.between(desde.withDayOfMonth(1), f.withDayOfMonth(1));
                if (indice >= 0 && indice < valores.length) {
                    valores[indice] += montos == null ? 1 : montos.get(e.getKey()).get(k);
                }
            }
            series.add(new Serie(e.getKey(), valores));
        }
        series.sort((a, b) -> Double.compare(java.util.Arrays.stream(b.valores()).sum(), java.util.Arrays.stream(a.valores()).sum()));
        return new Evolucion(etiquetas, series.size() > LINEAS_MAXIMAS ? series.subList(0, LINEAS_MAXIMAS) : series);
    }
}
