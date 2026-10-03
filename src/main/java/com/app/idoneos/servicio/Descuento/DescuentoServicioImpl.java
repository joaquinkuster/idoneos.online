package com.app.idoneos.servicio.Descuento;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.repositorio.DescuentoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Descuento}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link DescuentoServicio}.
 */
@Service
public class DescuentoServicioImpl implements DescuentoServicio, CrudServicio<Descuento> {

    @Autowired
    private DescuentoRepositorio descuentoRepositorio;

    /**
     * Guarda el descuento en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Descuento guardar(Descuento entidad) {
        return descuentoRepositorio.save(entidad);
    }

    /**
     * Busca el descuento por su identificador. Solo devuelve registros activos (no dados de baja).
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Descuento> buscarPorId(Integer id) {
        return descuentoRepositorio.findById(id)
                .filter(entidad -> !entidad.esInactivo()); // Solo devuelve registros activos
    }

    /**
     * Obtiene todos los registros. Solo devuelve registros activos.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Descuento> obtenerTodo() {
        return descuentoRepositorio.findByBajaFalse();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Descuento modificar(Descuento entidad) {
        return descuentoRepositorio.save(entidad);
    }

    /**
     * Da de baja el registro (baja lógica), en lugar de eliminarlo de la base de datos.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Descuento entidad) {
        entidad.marcarInactivo(); // Baja lógica
        descuentoRepositorio.save(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado y está activo.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return descuentoRepositorio.existsById(id) &&
                buscarPorId(id).isPresent(); // Solo cuenta registros activos
    }

    /**
     * Busca el descuento por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Descuento> buscarPorNombre(String nombre) {
        return descuentoRepositorio.findByNombre(nombre);
    }
}
