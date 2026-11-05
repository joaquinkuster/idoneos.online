package com.app.idoneos.service.rolusuario;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.idoneos.model.RolUsuario;
import com.app.idoneos.repository.RolUsuarioRepository;
import com.app.idoneos.service.CrudService;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link RolUsuario}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link RolUsuarioService}.
 */
@Service
public class RolUsuarioServiceImpl implements RolUsuarioService {

    @Autowired
    private RolUsuarioRepository rolUsuarioRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la asignación de rol por su identificador. Incluye los registros dados de baja o anulados, para poder consultarlos.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<RolUsuario> buscarPorId(Integer id) {
        return rolUsuarioRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<RolUsuario> obtenerTodo() {
        return rolUsuarioRepository.findByBajaFalse();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return rolUsuarioRepository.existsById(id);
    }




    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la asignación de rol en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public RolUsuario guardar(RolUsuario entidad) {
        return rolUsuarioRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public RolUsuario modificar(RolUsuario entidad) {
        return rolUsuarioRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(RolUsuario entidad) {
        entidad.marcarInactivo(); // Baja lógica
        rolUsuarioRepository.save(entidad);
    }
}
