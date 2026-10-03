package com.app.idoneos.servicio.Reporte;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Administrador;
import com.app.idoneos.modelo.Curso;
import com.app.idoneos.modelo.Reporte;
import com.app.idoneos.modelo.TipoReporte;
import com.app.idoneos.repositorio.ReporteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Reporte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ReporteServicio}.
 */
@Service
public class ReporteServicioImpl implements ReporteServicio, CrudServicio<Reporte> {

    @Autowired
    private ReporteRepositorio reporteRepositorio;

    /**
     * Guarda el reporte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Reporte guardar(Reporte entidad) {
        return reporteRepositorio.save(entidad);
    }

    /**
     * Busca el reporte por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Reporte> buscarPorId(Integer id) {
        return reporteRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Reporte> obtenerTodo() {
        return reporteRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Reporte modificar(Reporte entidad) {
        return reporteRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Reporte entidad) {
        reporteRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return reporteRepositorio.existsById(id);
    }

    /**
     * Busca los registros de reporte asociados a administrador.
     *
     * @param administrador el registro de Administrador asociado
     * @return una lista de Reporte asociados al Administrador indicado
     */
    @Override
    public List<Reporte> buscarPorAdministrador(Administrador administrador) {
        return reporteRepositorio.findByAdministrador(administrador);
    }

    /**
     * Busca los registros de reporte asociados a tipoReporte.
     *
     * @param tipoReporte el registro de TipoReporte asociado
     * @return una lista de Reporte asociados al TipoReporte indicado
     */
    @Override
    public List<Reporte> buscarPorTipoReporte(TipoReporte tipoReporte) {
        return reporteRepositorio.findByTipoReporte(tipoReporte);
    }

    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    @Override
    public List<Reporte> buscarPorCurso(Curso curso) {
        return reporteRepositorio.findByCurso(curso);
    }
}
