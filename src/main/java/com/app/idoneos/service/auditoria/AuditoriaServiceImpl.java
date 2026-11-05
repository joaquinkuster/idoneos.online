package com.app.idoneos.service.auditoria;

import com.app.idoneos.model.Auditoria;
import com.app.idoneos.repository.AuditoriaRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Auditoria}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link AuditoriaService}.
 */
@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca la auditoría por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Auditoria> buscarPorId(Integer id) {
        return auditoriaRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Auditoria> obtenerTodo() {
        return auditoriaRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return auditoriaRepository.existsById(id);
    }



    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda la auditoría en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Auditoria guardar(Auditoria entidad) {
        return auditoriaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Auditoria modificar(Auditoria entidad) {
        return auditoriaRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Auditoria entidad) {
        auditoriaRepository.delete(entidad);
    }
}
