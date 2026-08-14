package com.app.idoneos.service.Reportes;

import com.app.idoneos.model.Curso;
import java.time.LocalDate;
import java.util.List;

/**
 * CU-88 — Generar informe de ingresos de un curso.
 * DTO con los datos para las 4 vistas del informe:
 * - Vista 1: comparación de ingresos (barras horizontales)
 * - Vista 2: evolución temporal (línea)
 * - Vista 3: ingresos por categoría (torta/pie)
 * - Vista 4: bruto vs. neto (barras comparativas)
 *
 * Fuente de datos: Pago (monto, fecha, estadoPago="Acreditado"),
 *                  Inscripcion (descuento), Curso (precio, categoria).
 */
public class DatosInformeIngresosDTO {

    private Curso curso;
    private LocalDate desde;
    private LocalDate hasta;
    private long totalPagosAcreditados;

    // Vista 1 — Comparación de ingresos (barras horizontales)
    private List<String> nombresComparacion;
    private List<Double> ingresosComparacion;

    // Vista 2 — Evolución de ingresos (línea)
    private List<String> etiquetasEvolucion;
    private List<Double> valoresEvolucion;

    // Vista 3 — Ingresos por categoría (torta)
    private List<String> nombresCategoria;
    private List<Double> ingresosCategoria;

    // Vista 4 — Bruto vs. neto
    private double montoBruto;
    private double montoNeto;
    private double descuentoAplicado;

    public DatosInformeIngresosDTO() {}

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public LocalDate getDesde() { return desde; }
    public void setDesde(LocalDate desde) { this.desde = desde; }
    public LocalDate getHasta() { return hasta; }
    public void setHasta(LocalDate hasta) { this.hasta = hasta; }
    public long getTotalPagosAcreditados() { return totalPagosAcreditados; }
    public void setTotalPagosAcreditados(long totalPagosAcreditados) { this.totalPagosAcreditados = totalPagosAcreditados; }
    public List<String> getNombresComparacion() { return nombresComparacion; }
    public void setNombresComparacion(List<String> nombresComparacion) { this.nombresComparacion = nombresComparacion; }
    public List<Double> getIngresosComparacion() { return ingresosComparacion; }
    public void setIngresosComparacion(List<Double> ingresosComparacion) { this.ingresosComparacion = ingresosComparacion; }
    public List<String> getEtiquetasEvolucion() { return etiquetasEvolucion; }
    public void setEtiquetasEvolucion(List<String> etiquetasEvolucion) { this.etiquetasEvolucion = etiquetasEvolucion; }
    public List<Double> getValoresEvolucion() { return valoresEvolucion; }
    public void setValoresEvolucion(List<Double> valoresEvolucion) { this.valoresEvolucion = valoresEvolucion; }
    public List<String> getNombresCategoria() { return nombresCategoria; }
    public void setNombresCategoria(List<String> nombresCategoria) { this.nombresCategoria = nombresCategoria; }
    public List<Double> getIngresosCategoria() { return ingresosCategoria; }
    public void setIngresosCategoria(List<Double> ingresosCategoria) { this.ingresosCategoria = ingresosCategoria; }
    public double getMontoBruto() { return montoBruto; }
    public void setMontoBruto(double montoBruto) { this.montoBruto = montoBruto; }
    public double getMontoNeto() { return montoNeto; }
    public void setMontoNeto(double montoNeto) { this.montoNeto = montoNeto; }
    public double getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(double descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }
}
