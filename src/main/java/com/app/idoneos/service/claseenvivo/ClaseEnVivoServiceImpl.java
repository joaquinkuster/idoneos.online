package com.app.idoneos.service.claseenvivo;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.service.CrudService;

/** Implementación de ClaseEnVivoService. */
@Service
public class ClaseEnVivoServiceImpl implements ClaseEnVivoService {

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca la clase en vivo por su identificador. */
    @Override
    public Optional<ClaseEnVivo> buscarPorId(Integer id) {
        return claseEnVivoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /** Obtiene todos los registros. */
    @Override
    public List<ClaseEnVivo> obtenerTodo() {
        return claseEnVivoRepository.findByBajaFalse();
    }

    /** Verifica si existe un registro con el identificador dado y está activo. */
    @Override
    public boolean existePorId(Integer id) {
        return claseEnVivoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /** Busca los registros vigentes de claseEnVivo asociados a cohorte. */
    @Override
    public List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte) {
        return claseEnVivoRepository.findByCohorteAndBajaFalse(cohorte);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda la clase en vivo en la base de datos. */
    @Override
    public ClaseEnVivo guardar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public ClaseEnVivo modificar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(ClaseEnVivo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        claseEnVivoRepository.save(entidad);
    }
}
