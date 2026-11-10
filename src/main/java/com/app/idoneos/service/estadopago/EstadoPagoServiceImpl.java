package com.app.idoneos.service.estadopago;

import com.app.idoneos.model.EstadoPago;
import com.app.idoneos.repository.EstadoPagoRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de EstadoPagoService. */
@Service
public class EstadoPagoServiceImpl implements EstadoPagoService {

    @Autowired
    private EstadoPagoRepository estadoPagoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el estado del pago por su identificador. */
    @Override
    public Optional<EstadoPago> buscarPorId(Integer id) {
        return estadoPagoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<EstadoPago> obtenerTodo() {
        return estadoPagoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return estadoPagoRepository.existsById(id);
    }

    /** Busca el estado del pago por su atributo único 'nombre'. */
    @Override
    public Optional<EstadoPago> buscarPorNombre(String nombre) {
        return estadoPagoRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el estado del pago en la base de datos. */
    @Override
    public EstadoPago guardar(EstadoPago entidad) {
        return estadoPagoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public EstadoPago modificar(EstadoPago entidad) {
        return estadoPagoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(EstadoPago entidad) {
        estadoPagoRepository.delete(entidad);
    }
}
