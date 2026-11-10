package com.app.idoneos.service.reporte;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;
import com.app.idoneos.repository.ReporteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de ReporteService. */
@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el reporte por su identificador. */
    @Override
    public Optional<Reporte> buscarPorId(Integer id) {
        return reporteRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Reporte> obtenerTodo() {
        return reporteRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return reporteRepository.existsById(id);
    }

    /** Busca los registros de reporte asociados a curso. */
    @Override
    public List<Reporte> buscarPorCurso(Curso curso) {
        return reporteRepository.findByCurso(curso);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el reporte en la base de datos. */
    @Override
    public Reporte guardar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Reporte modificar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(Reporte entidad) {
        reporteRepository.delete(entidad);
    }
}
