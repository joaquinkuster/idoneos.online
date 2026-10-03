package com.app.idoneos.servicio.Nivel;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Nivel;
import com.app.idoneos.repositorio.NivelRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Nivel}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link NivelServicio}.
 */
@Service
public class NivelServicioImpl implements NivelServicio, CrudServicio<Nivel> {

    @Autowired
    private NivelRepositorio nivelRepositorio;

    /**
     * Guarda el nivel en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Nivel guardar(Nivel entidad) {
        return nivelRepositorio.save(entidad);
    }

    /**
     * Busca el nivel por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Nivel> buscarPorId(Integer id) {
        return nivelRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Nivel> obtenerTodo() {
        return nivelRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Nivel modificar(Nivel entidad) {
        return nivelRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Nivel entidad) {
        nivelRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return nivelRepositorio.existsById(id);
    }

    /**
     * Busca el nivel por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Nivel> buscarPorNombre(String nombre) {
        return nivelRepositorio.findByNombre(nombre);
    }
}
