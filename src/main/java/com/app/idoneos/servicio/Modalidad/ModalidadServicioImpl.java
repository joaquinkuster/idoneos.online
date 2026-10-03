package com.app.idoneos.servicio.Modalidad;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Modalidad;
import com.app.idoneos.repositorio.ModalidadRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Modalidad}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link ModalidadServicio}.
 */
@Service
public class ModalidadServicioImpl implements ModalidadServicio, CrudServicio<Modalidad> {

    @Autowired
    private ModalidadRepositorio modalidadRepositorio;

    /**
     * Guarda la modalidad en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Modalidad guardar(Modalidad entidad) {
        return modalidadRepositorio.save(entidad);
    }

    /**
     * Busca la modalidad por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Modalidad> buscarPorId(Integer id) {
        return modalidadRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Modalidad> obtenerTodo() {
        return modalidadRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Modalidad modificar(Modalidad entidad) {
        return modalidadRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Modalidad entidad) {
        modalidadRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return modalidadRepositorio.existsById(id);
    }

    /**
     * Busca la modalidad por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Modalidad> buscarPorNombre(String nombre) {
        return modalidadRepositorio.findByNombre(nombre);
    }
}
