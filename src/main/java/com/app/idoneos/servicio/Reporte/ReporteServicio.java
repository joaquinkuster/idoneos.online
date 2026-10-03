package com.app.idoneos.servicio.Reporte;

import java.util.List;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Reporte;
import com.app.idoneos.modelo.TipoReporte;

/**
 * Servicio para gestionar las operaciones relacionadas con el reporte.
 */
public interface ReporteServicio {

    /**
     * Busca los registros de reporte asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Reporte asociados al Administrador indicado
     */
    List<Reporte> buscarPorAdministrador(Administrador administrador);

    /**
     * Busca los registros de reporte asociados a tipoReporte.
     *
     * @param tipoReporte el registro de TipoReporte asociado
     * @return una lista de Reporte asociados al TipoReporte indicado
     */
    List<Reporte> buscarPorTipoReporte(TipoReporte tipoReporte);

    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    List<Reporte> buscarPorCurso(Curso curso);
}
