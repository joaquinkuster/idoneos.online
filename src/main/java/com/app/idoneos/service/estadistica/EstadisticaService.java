package com.app.idoneos.service.estadistica;

import java.time.LocalDate;
import java.util.List;

import com.app.idoneos.model.Curso;

/** Servicio de estadísticas: arma los datos de los informes de alumnos y de ingresos de un curso y los indicadores del tablero. */
public interface EstadisticaService {

    /** Una barra de un gráfico de barras horizontales. */
    record Barra(String etiqueta, double valor, boolean seleccionada) {
    }

    /** Una línea de un gráfico de evolución: un valor por cada punto del eje horizontal. */
    record Serie(String nombre, double[] valores) {
    }

    /** Evolución en el tiempo: las etiquetas del eje horizontal y una línea por programa. */
    record Evolucion(List<String> etiquetas, List<Serie> series) {
    }

    /** Datos del informe de alumnos de un curso. */
    record InformeAlumnos(Curso curso, LocalDate desde, LocalDate hasta, String programaVigente, int total, int completadas, int vigentes, int bajas,
            List<Barra> porCurso, Evolucion evolucion, List<Barra> porPrograma) {

        /** Cantidad de inscripciones que no se dieron de baja ni se cancelaron. */
        public int retenidas() {
            return total - bajas;
        }

        /** Porcentaje de abandono sobre el total de inscripciones. */
        public double porcentajeAbandono() {
            return total == 0 ? 0 : bajas * 100.0 / total;
        }
    }

    /** Datos del informe de ingresos de un curso. */
    record InformeIngresos(Curso curso, LocalDate desde, LocalDate hasta, String programaVigente, double neto, double bruto, double descuentos, int pagos,
            List<Barra> porCurso, Evolucion evolucion, List<Barra> porCategoria, List<Barra> porPrograma) {
    }

    /** Indicadores del tablero de estadísticas. */
    record Tablero(int alumnosActivos, int inscripcionesVigentes, int cursosActivos, double ingresosMes, double ingresosMesAnterior,
            List<String> etiquetasDias, int[] inscripcionesPorDia, double promedioDiario, int picoInscripciones, LocalDate diaPico, List<Barra> top5) {

        /** Variación porcentual de los ingresos respecto al mes anterior, o nulo si el mes anterior no tuvo ingresos. */
        public Double variacionIngresos() {
            return ingresosMesAnterior <= 0 ? null : (ingresosMes - ingresosMesAnterior) * 100.0 / ingresosMesAnterior;
        }
    }

    /**
     * Arma los datos del informe de alumnos de un curso en un rango de fechas.
     *
     * @param curso el curso sobre el que se informa
     * @param desde primer día del rango
     * @param hasta último día del rango
     * @return los datos del informe
     * @throws IllegalArgumentException si falta el curso, o el rango es inválido
     */
    InformeAlumnos informeAlumnos(Curso curso, LocalDate desde, LocalDate hasta);

    /**
     * Arma los datos del informe de ingresos de un curso en un rango de fechas.
     *
     * @param curso el curso sobre el que se informa
     * @param desde primer día del rango
     * @param hasta último día del rango
     * @return los datos del informe
     * @throws IllegalArgumentException si falta el curso, o el rango es inválido
     */
    InformeIngresos informeIngresos(Curso curso, LocalDate desde, LocalDate hasta);

    /**
     * Calcula los indicadores del tablero de estadísticas a hoy.
     *
     * @return los indicadores
     */
    Tablero tablero();
}
