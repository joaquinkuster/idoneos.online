package com.app.idoneos.servicio.TipoMaterial;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.TipoMaterial;
import com.app.idoneos.repositorio.TipoMaterialRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link TipoMaterial}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link TipoMaterialServicio}.
 */
@Service
public class TipoMaterialServicioImpl implements TipoMaterialServicio, CrudServicio<TipoMaterial> {

    @Autowired
    private TipoMaterialRepositorio tipoMaterialRepositorio;

    /**
     * Guarda el tipo de material en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public TipoMaterial guardar(TipoMaterial entidad) {
        return tipoMaterialRepositorio.save(entidad);
    }

    /**
     * Busca el tipo de material por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<TipoMaterial> buscarPorId(Integer id) {
        return tipoMaterialRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<TipoMaterial> obtenerTodo() {
        return tipoMaterialRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public TipoMaterial modificar(TipoMaterial entidad) {
        return tipoMaterialRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(TipoMaterial entidad) {
        tipoMaterialRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return tipoMaterialRepositorio.existsById(id);
    }

    /**
     * Busca el tipo de material por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<TipoMaterial> buscarPorNombre(String nombre) {
        return tipoMaterialRepositorio.findByNombre(nombre);
    }
}
