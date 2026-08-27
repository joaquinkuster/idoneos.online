package com.app.idoneos.service.modulo_reportes;
import com.app.idoneos.service.modulo_reportes.*;

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

import java.util.List;

/**
 * CU-89 — Consultar estadísticas.
 * DTO con todos los indicadores del panel de estadísticas ejecutivas.
 * Todos los datos provienen de tablas reales: Alumno, Inscripcion, Pago, Curso, EstadoPago.
 */
public class EstadisticasDashboardDTO {

    /** Indicador 1: Alumnos activos (Alumno con baja=false en Usuario). */
    private long alumnosActivos;

    /** Indicador 2: Inscripciones vigentes (baja=false y fechaVencimientoAcceso >= ahora). */
    private long inscripcionesVigentes;

    /** Indicador 3: Ingresos del mes actual (Pago acreditado con fecha en mes en curso). */
    private double ingresosMesActual;

    /** Indicador 3b: Ingresos del mes anterior (para calcular variación porcentual). */
    private double ingresosMesAnterior;

    /** Indicador 3c: Variación porcentual mes actual vs. mes anterior (null si mes anterior = 0 y actual = 0). */
    private Double variacionPorcentualIngresos;

    /** Indicador 4: Etiquetas de los últimos 30 días (formato "dd/MM"). */
    private List<String> etiquetasUltimos30Dias;

    /** Indicador 4b: Cantidad de inscripciones por cada uno de los últimos 30 días. */
    private List<Long> inscripcionesUltimos30Dias;

    /** Indicador 5: Nombres de los top 5 cursos con más inscriptos. */
    private List<String> top5CursosNombres;

    /** Indicador 5b: Cantidad de inscriptos de cada top 5 curso. */
    private List<Long> top5CursosCantidades;

    public EstadisticasDashboardDTO() {}

    public long getAlumnosActivos() { return alumnosActivos; }
    public void setAlumnosActivos(long alumnosActivos) { this.alumnosActivos = alumnosActivos; }

    public long getInscripcionesVigentes() { return inscripcionesVigentes; }
    public void setInscripcionesVigentes(long inscripcionesVigentes) { this.inscripcionesVigentes = inscripcionesVigentes; }

    public double getIngresosMesActual() { return ingresosMesActual; }
    public void setIngresosMesActual(double ingresosMesActual) { this.ingresosMesActual = ingresosMesActual; }

    public double getIngresosMesAnterior() { return ingresosMesAnterior; }
    public void setIngresosMesAnterior(double ingresosMesAnterior) { this.ingresosMesAnterior = ingresosMesAnterior; }

    public Double getVariacionPorcentualIngresos() { return variacionPorcentualIngresos; }
    public void setVariacionPorcentualIngresos(Double variacionPorcentualIngresos) { this.variacionPorcentualIngresos = variacionPorcentualIngresos; }

    public List<String> getEtiquetasUltimos30Dias() { return etiquetasUltimos30Dias; }
    public void setEtiquetasUltimos30Dias(List<String> etiquetasUltimos30Dias) { this.etiquetasUltimos30Dias = etiquetasUltimos30Dias; }

    public List<Long> getInscripcionesUltimos30Dias() { return inscripcionesUltimos30Dias; }
    public void setInscripcionesUltimos30Dias(List<Long> inscripcionesUltimos30Dias) { this.inscripcionesUltimos30Dias = inscripcionesUltimos30Dias; }

    public List<String> getTop5CursosNombres() { return top5CursosNombres; }
    public void setTop5CursosNombres(List<String> top5CursosNombres) { this.top5CursosNombres = top5CursosNombres; }

    public List<Long> getTop5CursosCantidades() { return top5CursosCantidades; }
    public void setTop5CursosCantidades(List<Long> top5CursosCantidades) { this.top5CursosCantidades = top5CursosCantidades; }
}

