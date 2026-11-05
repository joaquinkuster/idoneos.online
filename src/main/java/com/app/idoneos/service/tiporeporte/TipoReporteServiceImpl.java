package com.app.idoneos.service.tiporeporte;

import com.app.idoneos.model.TipoReporte;
import com.app.idoneos.repository.TipoReporteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TipoReporte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TipoReporteService}.
 */
@Service
public class TipoReporteServiceImpl implements TipoReporteService {

    @Autowired
    private TipoReporteRepository tipoReporteRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el tipo de reporte por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TipoReporte> buscarPorId(Integer id) {
        return tipoReporteRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TipoReporte> obtenerTodo() {
        return tipoReporteRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return tipoReporteRepository.existsById(id);
    }

    /**
     * Busca el tipo de reporte por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<TipoReporte> buscarPorNombre(String nombre) {
        return tipoReporteRepository.findByNombre(nombre);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el tipo de reporte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TipoReporte guardar(TipoReporte entidad) {
        return tipoReporteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TipoReporte modificar(TipoReporte entidad) {
        return tipoReporteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TipoReporte entidad) {
        tipoReporteRepository.delete(entidad);
    }
}
