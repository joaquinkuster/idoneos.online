package com.app.idoneos.service.docentecurso;

import com.app.idoneos.model.Curso;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.DocenteCurso;
import com.app.idoneos.repository.DocenteCursoRepository;
import com.app.idoneos.service.CrudService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de DocenteCursoService. */
@Service
public class DocenteCursoServiceImpl implements DocenteCursoService {

    @Autowired
    private DocenteCursoRepository docenteCursoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la participación docente por su identificador. */
    @Override
    public Optional<DocenteCurso> buscarPorId(Integer id) {
        return docenteCursoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<DocenteCurso> obtenerTodo() {
        return docenteCursoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return docenteCursoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de docenteCurso asociados a curso. */
    @Override
    public List<DocenteCurso> buscarEquipoPorCurso(Curso curso) {
        return buscarPorCurso(curso).stream().sorted(Comparator.comparing((DocenteCurso d) -> !d.getEsTitular())).toList();
    }

    @Override
    public List<DocenteCurso> buscarPorCurso(Curso curso) {
        return docenteCursoRepository.findByCursoAndBajaFalse(curso);
    }

    /** Busca los registros vigentes de docenteCurso asociados a docente. */
    @Override
    public List<DocenteCurso> buscarPorDocente(Docente docente) {
        return docenteCursoRepository.findByDocenteAndBajaFalse(docente);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la participación docente en la base de datos. */
    @Override
    public DocenteCurso guardar(DocenteCurso entidad) {
        return docenteCursoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public DocenteCurso modificar(DocenteCurso entidad) {
        return docenteCursoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(DocenteCurso entidad) {
        entidad.marcarInactivo(); // Baja lógica
        docenteCursoRepository.save(entidad);
    }
}
