package com.app.idoneos.servicio.TipoReporte;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TipoReporte;
import com.app.idoneos.repositorio.TipoReporteRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TipoReporte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TipoReporteServicio}.
 */
@Service
public class TipoReporteServicioImpl implements TipoReporteServicio, CrudServicio<TipoReporte> {

    @Autowired
    private TipoReporteRepositorio tipoReporteRepositorio;

    /**
     * Guarda el tipo de reporte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TipoReporte guardar(TipoReporte entidad) {
        return tipoReporteRepositorio.save(entidad);
    }

    /**
     * Busca el tipo de reporte por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TipoReporte> buscarPorId(Integer id) {
        return tipoReporteRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TipoReporte> obtenerTodo() {
        return tipoReporteRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TipoReporte modificar(TipoReporte entidad) {
        return tipoReporteRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TipoReporte entidad) {
        tipoReporteRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return tipoReporteRepositorio.existsById(id);
    }

    /**
     * Busca el tipo de reporte por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<TipoReporte> buscarPorNombre(String nombre) {
        return tipoReporteRepositorio.findByNombre(nombre);
    }
}
