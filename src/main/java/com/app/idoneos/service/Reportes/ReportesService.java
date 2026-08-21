package com.app.idoneos.service.Reportes;
import com.app.idoneos.service.Reportes.*;

import com.app.idoneos.exception.*;
import com.app.idoneos.repository.modulo_cursos.*;
import com.app.idoneos.repository.modulo_gestion_academica.*;
import com.app.idoneos.repository.modulo_inscripciones.*;
import com.app.idoneos.repository.modulo_evaluaciones.*;
import com.app.idoneos.repository.modulo_clases_vivo.*;
import com.app.idoneos.repository.modulo_ia.*;
import com.app.idoneos.repository.modulo_usuarios.*;
import com.app.idoneos.repository.modulo_auditoria.*;
import com.app.idoneos.repository.modulo_reportes.*;
import com.app.idoneos.repository.modulo_configuracion.*;
import com.app.idoneos.service.modulo_configuracion.*;
import com.app.idoneos.service.modulo_cursos.*;
import com.app.idoneos.service.modulo_gestion_academica.*;
import com.app.idoneos.service.modulo_inscripciones.*;
import com.app.idoneos.service.modulo_evaluaciones.*;
import com.app.idoneos.service.modulo_ia.*;
import com.app.idoneos.service.modulo_usuarios.*;

import com.app.idoneos.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TRAZABILIDAD — Servicio central del Módulo de Reportes Ejecutivos y Métricas Estadísticas.
 *
 * MOD-NF-03: Módulo de Reportes y Estadísticas
 *   CU-96 — Generar informe de alumnos de un curso: consolidación y exportación en PDF de métricas de cursada.
 *   CU-97 — Generar informe de ingresos de un curso: consolidación financiera y exportación en PDF de recaudación.
 *   CU-98 — Consultar estadísticas: métricas, KPIs globales y gráficos ejecutivos del sistema en tiempo real.
 */
@Service
public class ReportesService {

    @Autowired private InscripcionRepository inscripcionRepository;
    @Autowired private PagoRepository pagoRepository;
    @Autowired private AlumnoRepository alumnoRepository;
    @Autowired private CursoRepository cursoRepository;
    @Autowired private ReporteRepository reporteRepository;
    @Autowired private TipoReporteRepository tipoReporteRepository;
    @Autowired private AdministradorRepository administradorRepository;

    // =========================================================
    // CU-87 — Generar informe de alumnos de un curso
    // =========================================================

    /**
     * CU-87: Reúne los datos estadísticos de alumnos para un curso y rango de fechas.
     * Fuente: Inscripcion (fecha, baja, fechaVencimientoAcceso, numeroCertificado),
     *         Curso (nombre), todos los Cursos (comparación).
     */
    public DatosInformeAlumnosDTO obtenerDatosInformeAlumnos(int cursoId, LocalDate desde, LocalDate hasta) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay(); // inclusivo hasta fin del día

        // Vista 1: comparación de inscriptos (barras horizontales) — todos los cursos en el rango
        List<Object[]> rawComparacion = inscripcionRepository.contarInscripcionesPorCursoEnRango(desdeDateTime, hastaDateTime);
        Map<Integer, Long> conteosPorCursoId = new HashMap<>();
        for (Object[] row : rawComparacion) {
            conteosPorCursoId.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        List<Curso> todosCursos = cursoRepository.findAll();
        List<String> nombresComparacion = new ArrayList<>();
        List<Long> cantidadesComparacion = new ArrayList<>();
        for (Curso c : todosCursos) {
            nombresComparacion.add(c.getNombre());
            cantidadesComparacion.add(conteosPorCursoId.getOrDefault(c.getId(), 0L));
        }

        // Vista 2: evolución temporal (línea) — inscripciones del curso seleccionado por día
        List<Inscripcion> inscripcionesCurso = inscripcionRepository.findByCursoAndFechaRange(curso, desdeDateTime, hastaDateTime);
        // Agrupar por día
        Map<LocalDate, Long> pordDia = inscripcionesCurso.stream()
                .collect(Collectors.groupingBy(i -> i.getFecha().toLocalDate(), Collectors.counting()));
        List<String> etiquetasEvolucion = new ArrayList<>();
        List<Long> valoresEvolucion = new ArrayList<>();
        DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            etiquetasEvolucion.add(cursor.format(fmtDia));
            valoresEvolucion.add(pordDia.getOrDefault(cursor, 0L));
            cursor = cursor.plusDays(1);
        }

        // Vista 3: estado de inscripciones (barras apiladas)
        // Completada = numeroCertificado != null, Baja = baja=true, Vigente = resto
        long completadas = inscripcionesCurso.stream()
                .filter(i -> i.getNumeroCertificado() != null && !i.getNumeroCertificado().isEmpty())
                .count();
        long bajas = inscripcionesCurso.stream()
                .filter(Inscripcion::isBaja)
                .count();
        long vigentes = inscripcionesCurso.stream()
                .filter(i -> !i.isBaja() && (i.getNumeroCertificado() == null || i.getNumeroCertificado().isEmpty()))
                .count();

        DatosInformeAlumnosDTO dto = new DatosInformeAlumnosDTO();
        dto.setCurso(curso);
        dto.setDesde(desde);
        dto.setHasta(hasta);
        dto.setTotalInscripcionesCurso((long) inscripcionesCurso.size());
        // Vista 1
        dto.setNombresComparacion(nombresComparacion);
        dto.setCantidadesComparacion(cantidadesComparacion);
        // Vista 2
        dto.setEtiquetasEvolucion(etiquetasEvolucion);
        dto.setValoresEvolucion(valoresEvolucion);
        // Vista 3
        dto.setCompletadas(completadas);
        dto.setVigentes(vigentes);
        dto.setBajas(bajas);
        return dto;
    }

    /**
     * CU-87: Persiste el historial del reporte según el modelo (tipo + admin + fecha).
     * El modelo Reporte NO tiene curso_id → se registra únicamente tipo + admin + fecha.
     */
    public void registrarReporteAlumnos(int adminUsuarioId) {
        Administrador admin = administradorRepository.findById(adminUsuarioId).orElse(null);
        TipoReporte tipo = tipoReporteRepository.findByNombre("Alumnos inscriptos").orElse(null);
        if (admin != null && tipo != null) {
            reporteRepository.save(new Reporte(tipo, admin));
        }
    }

    // =========================================================
    // CU-88 — Generar informe de ingresos de un curso
    // =========================================================

    /**
     * CU-88: Reúne los datos estadísticos de ingresos para un curso y rango de fechas.
     * Solo considera pagos con EstadoPago.nombre = "Acreditado".
     * Fuente: Pago, EstadoPago, Inscripcion, Descuento, Curso, Categoria.
     */
    public DatosInformeIngresosDTO obtenerDatosInformeIngresos(int cursoId, LocalDate desde, LocalDate hasta) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        LocalDateTime desdeDateTime = desde.atStartOfDay();
        LocalDateTime hastaDateTime = hasta.plusDays(1).atStartOfDay();

        List<Pago> pagosAcreditados = pagoRepository.findAcreditadosByCursoAndFechaRange(curso, desdeDateTime, hastaDateTime);

        // Vista 1: comparación ingresos todos los cursos (barras horizontales)
        List<Object[]> rawComparacion = pagoRepository.sumarIngresosPorCursoEnRango(desdeDateTime, hastaDateTime);
        Map<Integer, Double> ingresosPorCursoId = new HashMap<>();
        for (Object[] row : rawComparacion) {
            ingresosPorCursoId.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        List<Curso> todosCursos = cursoRepository.findAll();
        List<String> nombresComparacion = new ArrayList<>();
        List<Double> ingresosComparacion = new ArrayList<>();
        for (Curso c : todosCursos) {
            nombresComparacion.add(c.getNombre());
            ingresosComparacion.add(ingresosPorCursoId.getOrDefault(c.getId(), 0.0));
        }

        // Vista 2: evolución ingresos del curso por día (línea)
        Map<LocalDate, Double> ingresosPorDia = pagosAcreditados.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getFecha().toLocalDate(),
                        Collectors.summingDouble(p -> (double) p.getMonto())));
        List<String> etiquetasEvolucion = new ArrayList<>();
        List<Double> valoresEvolucion = new ArrayList<>();
        DateTimeFormatter fmtDia = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate cursor = desde;
        while (!cursor.isAfter(hasta)) {
            etiquetasEvolucion.add(cursor.format(fmtDia));
            valoresEvolucion.add(ingresosPorDia.getOrDefault(cursor, 0.0));
            cursor = cursor.plusDays(1);
        }

        // Vista 3: ingresos por categoría (torta/pie)
        List<Object[]> rawCategoria = pagoRepository.sumarIngresosPorCategoriaEnRango(desdeDateTime, hastaDateTime);
        List<String> nombresCategoria = new ArrayList<>();
        List<Double> ingresosCategoria = new ArrayList<>();
        for (Object[] row : rawCategoria) {
            nombresCategoria.add((String) row[0]);
            ingresosCategoria.add(((Number) row[1]).doubleValue());
        }

        // Vista 4: bruto vs. neto (descuento aplicado)
        // Bruto = precio del curso según la inscripción
        // Descuento aplicado = monto * (porcentaje/100) si la inscripción tenía descuento
        // Neto = monto acreditado (lo que realmente ingresó)
        double montoBruto = pagosAcreditados.stream()
                .mapToDouble(p -> {
                    Inscripcion insc = p.getInscripcion();
                    if (insc != null && insc.getCurso() != null) {
                        return insc.getCurso().getPrecio();
                    }
                    return (double) p.getMonto();
                }).sum();
        double montoNeto = pagosAcreditados.stream().mapToDouble(p -> (double) p.getMonto()).sum();
        double descuentoAplicado = montoBruto - montoNeto;

        DatosInformeIngresosDTO dto = new DatosInformeIngresosDTO();
        dto.setCurso(curso);
        dto.setDesde(desde);
        dto.setHasta(hasta);
        dto.setTotalPagosAcreditados((long) pagosAcreditados.size());
        // Vista 1
        dto.setNombresComparacion(nombresComparacion);
        dto.setIngresosComparacion(ingresosComparacion);
        // Vista 2
        dto.setEtiquetasEvolucion(etiquetasEvolucion);
        dto.setValoresEvolucion(valoresEvolucion);
        // Vista 3
        dto.setNombresCategoria(nombresCategoria);
        dto.setIngresosCategoria(ingresosCategoria);
        // Vista 4
        dto.setMontoBruto(montoBruto);
        dto.setMontoNeto(montoNeto);
        dto.setDescuentoAplicado(descuentoAplicado);
        return dto;
    }

    /**
     * CU-88: Persiste el historial del reporte (tipo Ingresos + admin + fecha).
     */
    public void registrarReporteIngresos(int adminUsuarioId) {
        Administrador admin = administradorRepository.findById(adminUsuarioId).orElse(null);
        TipoReporte tipo = tipoReporteRepository.findByNombre("Ingresos").orElse(null);
        if (admin != null && tipo != null) {
            reporteRepository.save(new Reporte(tipo, admin));
        }
    }

    // =========================================================
    // CU-89 — Consultar estadísticas (panel ejecutivo)
    // =========================================================

    /**
     * CU-89: Construye el DTO con los 5 indicadores del panel de estadísticas ejecutivas.
     * Fuente: Alumno, Inscripcion, Pago, Curso.
     */
    public EstadisticasDashboardDTO obtenerEstadisticasDashboard() {
        LocalDateTime ahora = LocalDateTime.now();

        // Indicador 1: Alumnos activos
        long alumnosActivos = alumnoRepository.contarAlumnosActivos();

        // Indicador 2: Inscripciones vigentes (baja=false y vencimiento >= ahora)
        long inscripcionesVigentes = inscripcionRepository.contarInscripcionesVigentes(ahora);

        // Indicador 3: Ingresos del mes actual
        YearMonth mesActual = YearMonth.from(ahora);
        LocalDateTime inicioMesActual = mesActual.atDay(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = mesActual.plusMonths(1).atDay(1).atStartOfDay();
        Double ingresosMesActual = pagoRepository.sumarIngresosEnRango(inicioMesActual, inicioMesSiguiente);
        if (ingresosMesActual == null) ingresosMesActual = 0.0;

        // Indicador 3b: Ingresos mes anterior (para variación)
        YearMonth mesAnterior = mesActual.minusMonths(1);
        LocalDateTime inicioMesAnterior = mesAnterior.atDay(1).atStartOfDay();
        Double ingresosMesAnterior = pagoRepository.sumarIngresosEnRango(inicioMesAnterior, inicioMesActual);
        if (ingresosMesAnterior == null) ingresosMesAnterior = 0.0;

        // Indicador 3c: Variación porcentual (evitar división por cero)
        Double variacionPorcentual;
        if (ingresosMesAnterior == 0.0 && ingresosMesActual == 0.0) {
            variacionPorcentual = 0.0;
        } else if (ingresosMesAnterior == 0.0) {
            variacionPorcentual = 100.0; // nuevo ingreso donde antes no había
        } else {
            variacionPorcentual = ((ingresosMesActual - ingresosMesAnterior) / ingresosMesAnterior) * 100.0;
        }

        // Indicador 4: Inscripciones de los últimos 30 días por día
        LocalDateTime hace30Dias = ahora.minusDays(30);
        List<Object[]> rawDias = inscripcionRepository.contarInscripcionesPorDiaDesde(hace30Dias);
        Map<LocalDate, Long> inscripcionesPorDia = new LinkedHashMap<>();
        for (Object[] row : rawDias) {
            LocalDate fecha = row[0] instanceof LocalDate ? (LocalDate) row[0] : LocalDate.parse(row[0].toString());
            inscripcionesPorDia.put(fecha, ((Number) row[1]).longValue());
        }
        List<String> etiquetas30Dias = new ArrayList<>();
        List<Long> valores30Dias = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 29; i >= 0; i--) {
            LocalDate fecha = ahora.minusDays(i).toLocalDate();
            etiquetas30Dias.add(fecha.format(fmt));
            valores30Dias.add(inscripcionesPorDia.getOrDefault(fecha, 0L));
        }

        // Indicador 5: Top 5 cursos (solo primeros 5 resultados)
        List<Object[]> rawTop5 = inscripcionRepository.top5CursosPorInscriptos();
        List<String> top5Nombres = new ArrayList<>();
        List<Long> top5Cantidades = new ArrayList<>();
        int limite = Math.min(5, rawTop5.size());
        for (int i = 0; i < limite; i++) {
            Object[] row = rawTop5.get(i);
            top5Nombres.add((String) row[0]);
            top5Cantidades.add(((Number) row[1]).longValue());
        }

        EstadisticasDashboardDTO dto = new EstadisticasDashboardDTO();
        dto.setAlumnosActivos(alumnosActivos);
        dto.setInscripcionesVigentes(inscripcionesVigentes);
        dto.setIngresosMesActual(ingresosMesActual);
        dto.setIngresosMesAnterior(ingresosMesAnterior);
        dto.setVariacionPorcentualIngresos(variacionPorcentual);
        dto.setEtiquetasUltimos30Dias(etiquetas30Dias);
        dto.setInscripcionesUltimos30Dias(valores30Dias);
        dto.setTop5CursosNombres(top5Nombres);
        dto.setTop5CursosCantidades(top5Cantidades);
        return dto;
    }
}

