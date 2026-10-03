package com.app.idoneos.servicio.EstadoPago;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.repositorio.EstadoPagoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link EstadoPago}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link EstadoPagoServicio}.
 */
@Service
public class EstadoPagoServicioImpl implements EstadoPagoServicio, CrudServicio<EstadoPago> {

    @Autowired
    private EstadoPagoRepositorio estadoPagoRepositorio;

    /**
     * Guarda el estado del pago en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public EstadoPago guardar(EstadoPago entidad) {
        return estadoPagoRepositorio.save(entidad);
    }

    /**
     * Busca el estado del pago por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<EstadoPago> buscarPorId(Integer id) {
        return estadoPagoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<EstadoPago> obtenerTodo() {
        return estadoPagoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public EstadoPago modificar(EstadoPago entidad) {
        return estadoPagoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(EstadoPago entidad) {
        estadoPagoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return estadoPagoRepositorio.existsById(id);
    }

    /**
     * Busca el estado del pago por su atributo único 'nombre'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param nombre el valor de 'nombre' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<EstadoPago> buscarPorNombre(String nombre) {
        return estadoPagoRepositorio.findByNombre(nombre);
    }
}
