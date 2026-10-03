package com.app.idoneos.servicio.MetodoPago;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.repositorio.MetodoPagoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link MetodoPago}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link MetodoPagoServicio}.
 */
@Service
public class MetodoPagoServicioImpl implements MetodoPagoServicio, CrudServicio<MetodoPago> {

    @Autowired
    private MetodoPagoRepositorio metodoPagoRepositorio;

    /**
     * Guarda el método de pago en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public MetodoPago guardar(MetodoPago entidad) {
        return metodoPagoRepositorio.save(entidad);
    }

    /**
     * Busca el método de pago por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<MetodoPago> buscarPorId(Integer id) {
        return metodoPagoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<MetodoPago> obtenerTodo() {
        return metodoPagoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public MetodoPago modificar(MetodoPago entidad) {
        return metodoPagoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(MetodoPago entidad) {
        metodoPagoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return metodoPagoRepositorio.existsById(id);
    }

    /**
     * Busca el método de pago por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<MetodoPago> buscarPorNombre(String nombre) {
        return metodoPagoRepositorio.findByNombre(nombre);
    }
}
