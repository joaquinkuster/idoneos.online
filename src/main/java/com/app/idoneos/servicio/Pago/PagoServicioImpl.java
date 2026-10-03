package com.app.idoneos.servicio.Pago;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Descuento;
import com.app.idoneos.modelo.EstadoPago;
import com.app.idoneos.modelo.Inscripcion;
import com.app.idoneos.modelo.MetodoPago;
import com.app.idoneos.modelo.Pago;
import com.app.idoneos.repositorio.PagoRepositorio;
import com.app.idoneos.servicio.CrudServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio para gestionar las operaciones sobre la entidad {@link Pago}.
 * Proporciona las operaciones CRUD básicas y las consultas definidas en {@link PagoServicio}.
 */
@Service
public class PagoServicioImpl implements PagoServicio, CrudServicio<Pago> {

    @Autowired
    private PagoRepositorio pagoRepositorio;

    /**
     * Guarda el pago en la base de datos.
     *
     * @param entidad El registro a guardar.
     * @return El registro guardado.
     */
    @Override
    public Pago guardar(Pago entidad) {
        return pagoRepositorio.save(entidad);
    }

    /**
     * Busca el pago por su identificador. Devuelve el registro si existe.
     *
     * @param id El identificador del registro.
     * @return Un {@link Optional} con el registro encontrado.
     */
    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepositorio.findById(id);
    }

    /**
     * Obtiene todos los registros. Devuelve todos los registros.
     *
     * @return Una lista de registros.
     */
    @Override
    public List<Pago> obtenerTodo() {
        return pagoRepositorio.findAll();
    }

    /**
     * Modifica un registro existente.
     *
     * @param entidad El registro con los datos actualizados.
     * @return El registro modificado.
     */
    @Override
    public Pago modificar(Pago entidad) {
        return pagoRepositorio.save(entidad);
    }

    /**
     * Elimina el registro de la base de datos. Esta entidad no tiene baja lógica.
     *
     * @param entidad El registro a borrar.
     */
    @Override
    public void borrar(Pago entidad) {
        pagoRepositorio.delete(entidad);
    }

    /**
     * Verifica si existe un registro con el identificador dado.
     *
     * @param id El identificador del registro.
     * @return true si existe, false en caso contrario.
     */
    @Override
    public boolean existePorId(Integer id) {
        return pagoRepositorio.existsById(id);
    }

    /**
     * Busca los registros de pago asociados a estadoPago.
     *
     * @param estadoPago el registro de EstadoPago asociado
     * @return una lista de Pago asociados al EstadoPago indicado
     */
    @Override
    public List<Pago> buscarPorEstadoPago(EstadoPago estadoPago) {
        return pagoRepositorio.findByEstadoPago(estadoPago);
    }

    /**
     * Busca los registros de pago asociados a metodoPago.
     *
     * @param metodoPago el registro de MetodoPago asociado
     * @return una lista de Pago asociados al MetodoPago indicado
     */
    @Override
    public List<Pago> buscarPorMetodoPago(MetodoPago metodoPago) {
        return pagoRepositorio.findByMetodoPago(metodoPago);
    }

    /**
     * Busca los registros de pago asociados a inscripcion.
     *
     * @param inscripcion el registro de Inscripcion asociado
     * @return una lista de Pago asociados al Inscripcion indicado
     */
    @Override
    public List<Pago> buscarPorInscripcion(Inscripcion inscripcion) {
        return pagoRepositorio.findByInscripcion(inscripcion);
    }

    /**
     * Busca los registros de pago asociados a descuento.
     *
     * @param descuento el registro de Descuento asociado
     * @return una lista de Pago asociados al Descuento indicado
     */
    @Override
    public List<Pago> buscarPorDescuento(Descuento descuento) {
        return pagoRepositorio.findByDescuento(descuento);
    }

    /**
     * Busca el pago por su atributo único 'codigoReferencia'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param codigoReferencia el valor de 'codigoReferencia' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorCodigoReferencia(String codigoReferencia) {
        return pagoRepositorio.findByCodigoReferencia(codigoReferencia);
    }

    /**
     * Busca el pago por su atributo único 'idIntencionExterna'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idIntencionExterna el valor de 'idIntencionExterna' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorIdIntencionExterna(String idIntencionExterna) {
        return pagoRepositorio.findByIdIntencionExterna(idIntencionExterna);
    }

    /**
     * Busca el pago por su atributo único 'idSolicitudPago'. Incluye los registros dados de baja, para poder reactivarlos en lugar de duplicarlos.
     *
     * @param idSolicitudPago el valor de 'idSolicitudPago' a buscar
     * @return un {@link Optional} con el registro si existe, o vacío si no se encuentra
     */
    @Override
    public Optional<Pago> buscarPorIdSolicitudPago(String idSolicitudPago) {
        return pagoRepositorio.findByIdSolicitudPago(idSolicitudPago);
    }
}
