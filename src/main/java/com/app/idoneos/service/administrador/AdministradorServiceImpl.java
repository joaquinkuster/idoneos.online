package com.app.idoneos.service.administrador;

import com.app.idoneos.model.Administrador;
import com.app.idoneos.repository.AdministradorRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Administrador}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AdministradorService}.
 */
@Service
public class AdministradorServiceImpl implements AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el administrador por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Administrador> buscarPorId(Integer id) {
        return administradorRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Administrador> obtenerTodo() {
        return administradorRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return administradorRepository.existsById(id);
    }


    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el administrador en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Administrador guardar(Administrador entidad) {
        return administradorRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Administrador modificar(Administrador entidad) {
        return administradorRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Administrador entidad) {
        administradorRepository.delete(entidad);
    }
}
