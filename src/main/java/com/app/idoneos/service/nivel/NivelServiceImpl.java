package com.app.idoneos.service.nivel;

import com.app.idoneos.model.Nivel;
import com.app.idoneos.repository.NivelRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Nivel}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link NivelService}.
 */
@Service
public class NivelServiceImpl implements NivelService {

    @Autowired
    private NivelRepository nivelRepository;

    // ---------------------------------------------------------------- Lecturas

    /**
     * Busca el nivel por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Nivel> buscarPorId(Integer id) {
        return nivelRepository.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Nivel> obtenerTodo() {
        return nivelRepository.findAll();
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return nivelRepository.existsById(id);
    }

    /**
     * Busca el nivel por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Nivel> buscarPorNombre(String nombre) {
        return nivelRepository.findByNombre(nombre);
    }

    // ---------------------------------------------------------------- Creaciones

    /**
     * Guarda el nivel en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Nivel guardar(Nivel entidad) {
        return nivelRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Modificaciones

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Nivel modificar(Nivel entidad) {
        return nivelRepository.save(entidad);
    }

    // ---------------------------------------------------------------- Bajas

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Nivel entidad) {
        nivelRepository.delete(entidad);
    }
}
