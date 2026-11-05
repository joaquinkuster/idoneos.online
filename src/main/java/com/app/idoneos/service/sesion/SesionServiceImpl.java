package com.app.idoneos.service.sesion;

import com.app.idoneos.model.Sesion;
import com.app.idoneos.repository.SesionRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Sesion}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link SesionService}.
 */
@Service
public class SesionServiceImpl implements SesionService {

    @Autowired
    private SesionRepository sesionRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la sesión por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Sesion> buscarPorId(Integer id) {
        return sesionRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Sesion> obtenerTodo() {
        return sesionRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return sesionRepository.existsById(id);
    }



    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la sesión en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Sesion guardar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Sesion modificar(Sesion entidad) {
        return sesionRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Sesion entidad) {
        sesionRepository.delete(entidad);
    }
}
