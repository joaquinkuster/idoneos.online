package com.app.idoneos.service.claseenvivo;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.repository.ClaseEnVivoRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link ClaseEnVivo}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ClaseEnVivoService}.
 */
@Service
public class ClaseEnVivoServiceImpl implements ClaseEnVivoService {

    @Autowired
    private ClaseEnVivoRepository claseEnVivoRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la clase en vivo por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<ClaseEnVivo> buscarPorId(Integer id) {
        return claseEnVivoRepository.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<ClaseEnVivo> obtenerTodo() {
        return claseEnVivoRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return claseEnVivoRepository.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }




    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    @Override
    public List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte) {
        return claseEnVivoRepository.findByCohorteAndBajaFalse(cohorte);
    }


    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la clase en vivo en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public ClaseEnVivo guardar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public ClaseEnVivo modificar(ClaseEnVivo entidad) {
        return claseEnVivoRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(ClaseEnVivo entidad) {
        entidad.marcarInactivo(); // Baja lógica
        claseEnVivoRepository.save(entidad);
    }
}
