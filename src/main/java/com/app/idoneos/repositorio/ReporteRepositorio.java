package com.app.idoneos.repositorio;

import java.util.List;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Reporte;
import com.app.idoneos.modelo.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Reporte} en la base de datos.
 * Extiende de {@link JpaRepository}, proporcionando métodos CRUD básicos para la entidad {@link Reporte}.
 */
@Repository
public interface ReporteRepositorio extends JpaRepository<Reporte, Integer> {

    /**
     * Busca los registros de reporte asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Reporte asociados al Administrador indicado
     */
    List<Reporte> findByAdministrador(Administrador administrador);

    /**
     * Busca los registros de reporte asociados a tipoReporte.
     *
     * @param tipoReporte el registro de TipoReporte asociado
     * @return una lista de Reporte asociados al TipoReporte indicado
     */
    List<Reporte> findByTipoReporte(TipoReporte tipoReporte);

    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    List<Reporte> findByCurso(Curso curso);
}
