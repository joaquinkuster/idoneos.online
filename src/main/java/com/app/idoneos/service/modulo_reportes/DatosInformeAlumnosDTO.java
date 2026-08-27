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

import java.time.LocalDate;
import java.util.List;

/**
 * CU-87 — Generar informe de alumnos de un curso.
 * DTO con los datos para las 3 vistas del informe:
 * - Vista 1: comparación de inscriptos (barras horizontales)
 * - Vista 2: evolución temporal (línea)
 * - Vista 3: estado de inscripciones (barras apiladas: completadas, vigentes, bajas)
 *
 * Fuente de datos: Inscripcion (fecha, baja, fechaVencimientoAcceso, numeroCertificado), Curso.
 */
public class DatosInformeAlumnosDTO {

    private Curso curso;
    private LocalDate desde;
    private LocalDate hasta;
    private long totalInscripcionesCurso;

    // Vista 1 — Comparación de inscriptos (barras horizontales)
    private List<String> nombresComparacion;
    private List<Long> cantidadesComparacion;

    // Vista 2 — Evolución de inscripciones (línea)
    private List<String> etiquetasEvolucion;
    private List<Long> valoresEvolucion;

    // Vista 3 — Estado de inscripciones (barras apiladas)
    private long completadas;
    private long vigentes;
    private long bajas;

    public DatosInformeAlumnosDTO() {}

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
    public long getTotalInscripcionesCurso() { return totalInscripcionesCurso; }
    public void setTotalInscripcionesCurso(long totalInscripcionesCurso) { this.totalInscripcionesCurso = totalInscripcionesCurso; }
    public List<String> getNombresComparacion() { return nombresComparacion; }
    public void setNombresComparacion(List<String> nombresComparacion) { this.nombresComparacion = nombresComparacion; }
    public List<Long> getCantidadesComparacion() { return cantidadesComparacion; }
    public void setCantidadesComparacion(List<Long> cantidadesComparacion) { this.cantidadesComparacion = cantidadesComparacion; }
    public List<String> getEtiquetasEvolucion() { return etiquetasEvolucion; }
    public void setEtiquetasEvolucion(List<String> etiquetasEvolucion) { this.etiquetasEvolucion = etiquetasEvolucion; }
    public List<Long> getValoresEvolucion() { return valoresEvolucion; }
    public void setValoresEvolucion(List<Long> valoresEvolucion) { this.valoresEvolucion = valoresEvolucion; }
    public long getCompletadas() { return completadas; }
    public void setCompletadas(long completadas) { this.completadas = completadas; }
    public long getVigentes() { return vigentes; }
    public void setVigentes(long vigentes) { this.vigentes = vigentes; }
    public long getBajas() { return bajas; }
    public void setBajas(long bajas) { this.bajas = bajas; }
}

