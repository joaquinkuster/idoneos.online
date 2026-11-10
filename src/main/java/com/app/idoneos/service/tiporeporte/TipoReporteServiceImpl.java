package com.app.idoneos.service.tiporeporte;

import com.app.idoneos.model.TipoReporte;
import com.app.idoneos.repository.TipoReporteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de TipoReporteService. */
@Service
public class TipoReporteServiceImpl implements TipoReporteService {

    @Autowired
    private TipoReporteRepository tipoReporteRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el tipo de reporte por su identificador. */
    @Override
    public Optional<TipoReporte> buscarPorId(Integer id) {
        return tipoReporteRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<TipoReporte> obtenerTodo() {
        return tipoReporteRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return tipoReporteRepository.existsById(id);
    }

    /** Busca el tipo de reporte por su atributo único 'nombre'. */
    @Override
    public Optional<TipoReporte> buscarPorNombre(String nombre) {
        return tipoReporteRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el tipo de reporte en la base de datos. */
    @Override
    public TipoReporte guardar(TipoReporte entidad) {
        return tipoReporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public TipoReporte modificar(TipoReporte entidad) {
        return tipoReporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(TipoReporte entidad) {
        tipoReporteRepository.delete(entidad);
    }
}
