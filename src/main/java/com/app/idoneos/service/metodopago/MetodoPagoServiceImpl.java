package com.app.idoneos.service.metodopago;

import com.app.idoneos.model.MetodoPago;
import com.app.idoneos.repository.MetodoPagoRepository;
import com.app.idoneos.service.CrudService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Implementación de MetodoPagoService. */
@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el método de pago por su identificador. */
    @Override
    public Optional<MetodoPago> buscarPorId(Integer id) {
        return metodoPagoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<MetodoPago> obtenerTodo() {
        return metodoPagoRepository.findAll();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return metodoPagoRepository.existsById(id);
    }

    /** Busca el método de pago por su atributo único 'nombre'. */
    @Override
    public Optional<MetodoPago> buscarPorNombre(String nombre) {
        return metodoPagoRepository.findByNombre(nombre);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el método de pago en la base de datos. */
    @Override
    public MetodoPago guardar(MetodoPago entidad) {
        return metodoPagoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public MetodoPago modificar(MetodoPago entidad) {
        return metodoPagoRepository.save(entidad);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Elimina el registro de la base de datos. */
    @Override
    public void borrar(MetodoPago entidad) {
        metodoPagoRepository.delete(entidad);
    }
}
