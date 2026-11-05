package com.app.idoneos.service.reporte;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Reporte;
import com.app.idoneos.repository.ReporteRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Reporte}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ReporteService}.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el reporte por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Reporte> buscarPorId(Integer id) {
        return reporteRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Reporte> obtenerTodo() {
        return reporteRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return reporteRepository.existsById(id);
    }



    /**
     * Busca los registros de reporte asociados a curso.
     *
     * @param curso el registro de Curso asociado
     * @return una lista de Reporte asociados al Curso indicado
     */
    @Override
    public List<Reporte> buscarPorCurso(Curso curso) {
        return reporteRepository.findByCurso(curso);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el reporte en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Reporte guardar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Reporte modificar(Reporte entidad) {
        return reporteRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Reporte entidad) {
        reporteRepository.delete(entidad);
    }
}
