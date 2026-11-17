package com.app.idoneos.service.reporte;

import com.app.idoneos.service.CrudService;

import java.util.List;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;

/** Servicio del reporte. */
public interface ReporteService extends CrudService<Reporte, Integer> {

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    List<Reporte> buscarPorCurso(Curso curso);

    /**
     * Obtiene el historial de informes generados, del más reciente al más antiguo.
     *
     * @return la lista de reportes
     */
    List<Reporte> buscarRecientes();

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Genera el informe de alumnos o de ingresos de un curso y registra la generación en el historial.
     *
     * @param administrador quien lo genera
     * @param tipo nombre del tipo de reporte ({@code TipoReporte.INFORME_ALUMNOS} o {@code TipoReporte.INFORME_INGRESOS})
     * @param idCurso curso sobre el que se informa
     * @param desde primer día del rango
     * @param hasta último día del rango
     * @return el PDF del informe
     * @throws IllegalArgumentException si falta algún dato o el rango es inválido
     */
    byte[] generarInforme(com.app.idoneos.model.Administrador administrador, String tipo, Integer idCurso, java.time.LocalDate desde, java.time.LocalDate hasta);
}
