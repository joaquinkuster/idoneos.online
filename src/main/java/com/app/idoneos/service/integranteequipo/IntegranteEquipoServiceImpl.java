package com.app.idoneos.service.integranteequipo;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.IntegranteEquipo;
import com.app.idoneos.repository.IntegranteEquipoRepository;
import com.app.idoneos.service.CrudService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de IntegranteEquipoService. */
@Service
public class IntegranteEquipoServiceImpl implements IntegranteEquipoService {

    @Autowired
    private IntegranteEquipoRepository integranteEquipoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la participación docente por su identificador. */
    @Override
    public Optional<IntegranteEquipo> buscarPorId(Integer id) {
        return integranteEquipoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<IntegranteEquipo> obtenerTodo() {
        return integranteEquipoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return integranteEquipoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de integranteEquipo asociados a curso. */
    @Override
    public List<IntegranteEquipo> buscarEquipoPorCurso(Curso curso) {
        return buscarPorCurso(curso).stream().sorted(Comparator.comparing((IntegranteEquipo d) -> !d.getEsTitular())).toList();
    }

    @Override
    public List<IntegranteEquipo> buscarPorCurso(Curso curso) {
        return integranteEquipoRepository.findByCursoAndBajaFalse(curso);
    }

    /** Busca los registros vigentes de integranteEquipo asociados a docente. */
    @Override
    public List<IntegranteEquipo> buscarPorDocente(Docente docente) {
        return integranteEquipoRepository.findByDocenteAndBajaFalse(docente);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la participación docente en la base de datos. */
    @Override
    public IntegranteEquipo guardar(IntegranteEquipo entidad) {
        return integranteEquipoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public IntegranteEquipo modificar(IntegranteEquipo entidad) {
        return integranteEquipoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(IntegranteEquipo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        integranteEquipoRepository.save(entidad);
    }
}
